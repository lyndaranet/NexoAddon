package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.protectionlib.ProtectionLib;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;
import zone.vao.nexoAddon.items.mechanics.AreaAbilityMechanic.AbilityEffect;
import zone.vao.nexoAddon.items.mechanics.ProjectileMechanic.TrailEntry;
import zone.vao.nexoAddon.items.mechanics.TeleportMechanic.ParticleEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic dash / movement-burst mechanic — launches the player in a configurable direction with an
 * optional flight or glide phase, trail particles, on-land impact and passive buffs while dashing.
 * Distinct from the older {@link Dash} mechanic ({@code Mechanics.dash.power}); this one is keyed on
 * {@code Mechanics.dash.mode}. One config block ({@code Mechanics.dash}).
 */
public record DashMechanic(String trigger, String mode, String direction, int cooldownSeconds,
                           int durationTicks, double speed, int charges, int chargeRechargeSeconds,
                           List<PhaseEffect> phaseEffects, int phaseInvincibilityTicks, boolean glowDuringDash,
                           List<TrailEntry> phaseParticles, int trailIntervalTicks,
                           List<AbilityEffect> activateEffects, double activateSelfDamage, double impactRadius,
                           double impactDamage, int impactDelayTicks, List<AbilityEffect> impactEffects,
                           List<ParticleEntry> impactParticles, @Nullable Sound impactSound,
                           DashConditions conditions, List<TrailEntry> activateParticles,
                           @Nullable Sound activateSound) {

    public static final String TRIGGER_RIGHT_CLICK = "right_click";
    public static final String TRIGGER_SHIFT_RIGHT_CLICK = "shift_right_click";
    public static final String TRIGGER_DOUBLE_JUMP = "double_jump";

    public static final String MODE_DASH = "dash";
    public static final String MODE_GLIDE = "glide";
    public static final String MODE_FLIGHT = "flight";
    public static final String MODE_BLINK_DASH = "blink_dash";

    public static final String DIRECTION_LOOK = "look";
    public static final String DIRECTION_HORIZONTAL = "horizontal";
    public static final String DIRECTION_FORWARD = "forward";
    public static final String DIRECTION_UP = "up";

    /** A phase buff whose duration is expressed in ticks (not seconds like {@link AbilityEffect}). */
    public record PhaseEffect(PotionEffectType type, int amplifier, int durationTicks) {
    }

    public record DashConditions(boolean requireSneaking, boolean requireSprinting, int requireHealthBelow,
                                 @Nullable String requirePermission, boolean requireOnGround,
                                 boolean requireInAir) {
    }

    /** Mutable per-player dash state: pre-dash flight snapshot, activity flag and charge tracking. */
    public static final class DashState {
        boolean hadFlight;
        boolean wasFlying;
        boolean isActive;
        int chargesRemaining;
        long lastChargeRechargeTime;

        public DashState(boolean hadFlight, boolean wasFlying, boolean isActive, int chargesRemaining,
            long lastChargeRechargeTime) {
            this.hadFlight = hadFlight;
            this.wasFlying = wasFlying;
            this.isActive = isActive;
            this.chargesRemaining = chargesRemaining;
            this.lastChargeRechargeTime = lastChargeRechargeTime;
        }
    }

    public static class DashMechanicListener implements Listener {

        private static final Map<UUID, DashState> playerStates = new ConcurrentHashMap<>();
        private static final Map<UUID, List<WrappedTask>> activeTasks = new ConcurrentHashMap<>();
        // Classic single-cooldown timestamps (used when charges <= 1).
        private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
        private static boolean rechargeTaskStarted = false;

        // --- Tasks -----------------------------------------------------------

        /** Starts the shared charge-recharge tick (every second). Called once from registerListeners. */
        public static void startRechargeTask() {
            if (rechargeTaskStarted) {
                return;
            }
            rechargeTaskStarted = true;
            NexoAddon.getInstance().getFoliaLib().getScheduler().runTimer(
                DashMechanicListener::tickRecharge, 20L, 20L);
        }

        private static void tickRecharge() {
            if (playerStates.isEmpty()) {
                return;
            }
            long now = System.currentTimeMillis();
            for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                DashState state = playerStates.get(player.getUniqueId());
                if (state == null || state.isActive) {
                    continue;
                }
                DashMechanic mechanic = resolveHeld(player);
                if (mechanic == null || mechanic.charges() <= 1) {
                    continue;
                }
                if (state.chargesRemaining >= mechanic.charges()) {
                    continue;
                }
                if (now - state.lastChargeRechargeTime >= mechanic.chargeRechargeSeconds() * 1000L) {
                    state.chargesRemaining++;
                    state.lastChargeRechargeTime = now;
                    actionBar(player, "<gold>Charges: " + state.chargesRemaining + "/" + mechanic.charges());
                }
            }
        }

        // --- Triggers --------------------------------------------------------

        @EventHandler
        public void onInteract(PlayerInteractEvent event) {
            if (event.getHand() != EquipmentSlot.HAND) {
                return;
            }
            Action action = event.getAction();
            boolean right = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
            if (!right) {
                return;
            }

            Player player = event.getPlayer();
            DashMechanic mechanic = resolve(event.getItem());
            if (mechanic == null) {
                return;
            }

            switch (mechanic.trigger()) {
                case TRIGGER_RIGHT_CLICK -> {
                    // any right click
                }
                case TRIGGER_SHIFT_RIGHT_CLICK -> {
                    if (!player.isSneaking()) return;
                }
                default -> {
                    return;
                }
            }

            handleActivation(player, mechanic);
        }

        @EventHandler
        public void onToggleFlight(PlayerToggleFlightEvent event) {
            Player player = event.getPlayer();
            // Double-tap space (toggling flight on) only counts in survival/adventure; creative and
            // spectator use vanilla flight, so we leave those alone.
            if (!event.isFlying() || player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR) {
                return;
            }
            DashMechanic mechanic = resolveHeld(player);
            if (mechanic == null || !TRIGGER_DOUBLE_JUMP.equals(mechanic.trigger())) {
                return;
            }
            // Swallow the vanilla flight toggle and dash instead.
            event.setCancelled(true);
            player.setFlying(false);
            handleActivation(player, mechanic);
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            UUID id = player.getUniqueId();
            DashState state = playerStates.remove(id);
            if (state != null && state.isActive) {
                player.setAllowFlight(state.hadFlight);
                if (state.hadFlight) {
                    player.setFlying(state.wasFlying);
                }
            }
            cancelTasks(id);
        }

        // --- Activation ------------------------------------------------------

        private static void handleActivation(Player player, DashMechanic mechanic) {
            UUID id = player.getUniqueId();

            if (!conditionsMet(player, mechanic.conditions())) {
                actionBar(player, "<red>Du kannst das gerade nicht benutzen.");
                return;
            }

            long now = System.currentTimeMillis();
            DashState state = playerStates.computeIfAbsent(id,
                k -> new DashState(false, false, false, Math.max(1, mechanic.charges()), now));
            if (state.isActive) {
                // Already mid-dash — ignore re-triggers.
                return;
            }

            // 2. Charge / cooldown gating.
            if (mechanic.charges() > 1) {
                if (state.chargesRemaining <= 0) {
                    long remainingMs = mechanic.chargeRechargeSeconds() * 1000L
                        - (now - state.lastChargeRechargeTime);
                    long remainingSeconds = Math.max(1, (remainingMs + 999) / 1000);
                    actionBar(player, "<red>Noch <bold>" + remainingSeconds + "s</bold> Cooldown!");
                    return;
                }
            } else {
                long remainingMs = remainingCooldown(id, mechanic.cooldownSeconds(), now);
                if (remainingMs > 0) {
                    long remainingSeconds = (remainingMs + 999) / 1000;
                    actionBar(player, "<red>Noch <bold>" + remainingSeconds + "s</bold> Cooldown!");
                    return;
                }
            }

            // Commit: consume a charge or set the cooldown timestamp.
            if (mechanic.charges() > 1) {
                state.chargesRemaining--;
                state.lastChargeRechargeTime = now;
                actionBar(player, "<gold>Charges: " + state.chargesRemaining + "/" + mechanic.charges());
            } else if (mechanic.cooldownSeconds() > 0) {
                cooldowns.put(id, now);
            }

            // 3. Snapshot the player's flight state so we can restore it exactly afterwards.
            state.hadFlight = player.getAllowFlight();
            state.wasFlying = player.isFlying();
            state.isActive = true;

            // 4. Activation visuals.
            spawnTrailEntries(player.getLocation(), mechanic.activateParticles());
            if (mechanic.activateSound() != null) {
                player.playSound(player.getLocation(), mechanic.activateSound(), 1.0f, 1.0f);
            }

            // 5. Self-effects on activation (before invincibility so recoil still lands).
            applyAbilityEffects(player, mechanic.activateEffects());
            if (mechanic.activateSelfDamage() > 0) {
                player.damage(mechanic.activateSelfDamage());
            }

            // 6. Phase buffs + invincibility window.
            applyPhaseEffects(player, mechanic.phaseEffects());
            if (mechanic.phaseInvincibilityTicks() > 0) {
                player.setNoDamageTicks(mechanic.phaseInvincibilityTicks());
            }

            // 7. Glow.
            if (mechanic.glowDuringDash()) {
                player.setGlowing(true);
            }

            // 8 + 9. Direction + mode dispatch.
            Vector dir = computeDirection(player, mechanic.direction());
            int duration = Math.max(1, mechanic.durationTicks());

            switch (mechanic.mode()) {
                case MODE_GLIDE -> {
                    grantFlight(player);
                    scheduleEnd(player, mechanic, duration);
                }
                case MODE_BLINK_DASH -> startBlink(player, mechanic, dir, duration);
                case MODE_DASH -> {
                    player.setVelocity(dir.clone().multiply(mechanic.speed()));
                    // Approximate landing: impact + cleanup after a short delay.
                    scheduleEnd(player, mechanic, Math.max(1, mechanic.impactDelayTicks()));
                }
                default -> { // MODE_FLIGHT
                    player.setVelocity(dir.clone().multiply(mechanic.speed()));
                    grantFlight(player);
                    scheduleEnd(player, mechanic, duration);
                }
            }

            // 10. Trail particles for the duration of the phase.
            startTrail(player, mechanic);
        }

        private static void grantFlight(Player player) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }

        // --- Mode: blink dash ------------------------------------------------

        private static void startBlink(Player player, DashMechanic mechanic, Vector dir, int duration) {
            double perStep = mechanic.speed() / duration;
            Vector stepVec = dir.clone().multiply(perStep);
            int[] tick = {0};
            WrappedTask task = NexoAddon.getInstance().getFoliaLib().getScheduler().runAtEntityTimer(player,
                () -> {
                    Location candidate = player.getLocation().add(stepVec);
                    if (isBlocked(candidate)) {
                        endDash(player, mechanic);
                        return;
                    }
                    candidate.setYaw(player.getLocation().getYaw());
                    candidate.setPitch(player.getLocation().getPitch());
                    player.teleport(candidate);
                    if (++tick[0] >= duration) {
                        endDash(player, mechanic);
                    }
                }, 1L, 1L);
            addTask(player.getUniqueId(), task);
        }

        /** Feet or head block at the destination is solid → cannot pass. */
        private static boolean isBlocked(Location feet) {
            return feet.getBlock().getType().isSolid()
                || feet.clone().add(0, 1, 0).getBlock().getType().isSolid();
        }

        // --- Trail -----------------------------------------------------------

        private static void startTrail(Player player, DashMechanic mechanic) {
            if (mechanic.phaseParticles().isEmpty()) {
                return;
            }
            int interval = Math.max(1, mechanic.trailIntervalTicks());
            WrappedTask task = NexoAddon.getInstance().getFoliaLib().getScheduler().runAtEntityTimer(player,
                () -> spawnTrailEntries(player.getLocation(), mechanic.phaseParticles()), 0L, interval);
            addTask(player.getUniqueId(), task);
        }

        // --- End / cleanup ---------------------------------------------------

        private static void scheduleEnd(Player player, DashMechanic mechanic, int ticks) {
            WrappedTask task = NexoAddon.getInstance().getFoliaLib().getScheduler().runAtEntityLater(player,
                () -> endDash(player, mechanic), Math.max(1, ticks));
            addTask(player.getUniqueId(), task);
        }

        private static void endDash(Player player, @Nullable DashMechanic mechanic) {
            UUID id = player.getUniqueId();
            DashState state = playerStates.get(id);
            if (state == null || !state.isActive) {
                return;
            }

            // Restore flight exactly as it was before the dash.
            player.setAllowFlight(state.hadFlight);
            if (state.hadFlight) {
                player.setFlying(state.wasFlying);
            }

            if (mechanic != null) {
                if (mechanic.glowDuringDash()) {
                    player.setGlowing(false);
                }
                removePhaseEffects(player, mechanic.phaseEffects());
                // Impact fires once when the phase ends. For dash mode "phase end" is the
                // impact_delay_ticks landing approximation; for the other modes it is the duration.
                if (mechanic.impactRadius() > 0) {
                    applyImpact(player, mechanic);
                }
            } else {
                player.setGlowing(false);
            }

            state.isActive = false;
            cancelTasks(id);
        }

        // --- Impact ----------------------------------------------------------

        private static void applyImpact(Player player, DashMechanic mechanic) {
            Location loc = player.getLocation();
            World world = loc.getWorld();
            if (world == null) {
                return;
            }
            for (LivingEntity entity : world.getNearbyLivingEntities(loc, mechanic.impactRadius())) {
                if (entity.getUniqueId().equals(player.getUniqueId())) {
                    continue;
                }
                if (!ProtectionLib.canInteract(player, entity.getLocation())) {
                    continue;
                }
                if (mechanic.impactDamage() > 0) {
                    entity.damage(mechanic.impactDamage(), player);
                }
                applyAbilityEffects(entity, mechanic.impactEffects());
            }
            spawnParticleEntries(loc, mechanic.impactParticles());
            if (mechanic.impactSound() != null) {
                world.playSound(loc, mechanic.impactSound(), 1.0f, 1.0f);
            }
        }

        // --- Direction -------------------------------------------------------

        private static Vector computeDirection(Player player, String direction) {
            Vector look = player.getEyeLocation().getDirection();
            return switch (direction) {
                case DIRECTION_UP -> new Vector(0, 1, 0);
                case DIRECTION_HORIZONTAL, DIRECTION_FORWARD -> {
                    Vector flat = look.clone();
                    flat.setY(0);
                    if (flat.lengthSquared() < 1.0e-9) {
                        yield new Vector(0, 0, 1);
                    }
                    yield flat.normalize();
                }
                default -> { // look
                    if (look.lengthSquared() < 1.0e-9) {
                        yield new Vector(0, 0, 1);
                    }
                    yield look.normalize();
                }
            };
        }

        // --- Task bookkeeping ------------------------------------------------

        private static void addTask(UUID id, WrappedTask task) {
            activeTasks.computeIfAbsent(id, k -> new ArrayList<>()).add(task);
        }

        private static void cancelTasks(UUID id) {
            List<WrappedTask> tasks = activeTasks.remove(id);
            if (tasks == null) {
                return;
            }
            for (WrappedTask task : tasks) {
                if (task != null && !task.isCancelled()) {
                    task.cancel();
                }
            }
        }

        // --- Effects ---------------------------------------------------------

        private static void applyPhaseEffects(Player player, List<PhaseEffect> effects) {
            for (PhaseEffect effect : effects) {
                player.addPotionEffect(new PotionEffect(effect.type(), effect.durationTicks(), effect.amplifier()));
            }
        }

        private static void removePhaseEffects(Player player, List<PhaseEffect> effects) {
            for (PhaseEffect effect : effects) {
                player.removePotionEffect(effect.type());
            }
        }

        private static void applyAbilityEffects(LivingEntity entity, List<AbilityEffect> effects) {
            for (AbilityEffect effect : effects) {
                if (effect.chance() < 1.0 && Math.random() >= effect.chance()) {
                    continue;
                }
                entity.addPotionEffect(new PotionEffect(
                    effect.type(), effect.durationSeconds() * 20, effect.amplifier()));
            }
        }

        // --- Particles -------------------------------------------------------

        private static void spawnTrailEntries(Location location, List<TrailEntry> entries) {
            World world = location.getWorld();
            if (world == null) {
                return;
            }
            for (TrailEntry entry : entries) {
                double off = entry.offset();
                world.spawnParticle(entry.particle(), location, entry.count(), off, off, off, 0.0);
            }
        }

        private static void spawnParticleEntries(Location location, List<ParticleEntry> entries) {
            World world = location.getWorld();
            if (world == null) {
                return;
            }
            for (ParticleEntry entry : entries) {
                world.spawnParticle(entry.particle(), location, entry.count(), 0.3, 0.5, 0.3, 0.0);
            }
        }

        // --- Conditions / cooldown -------------------------------------------

        private static boolean conditionsMet(Player player, DashConditions c) {
            if (c.requireSneaking() && !player.isSneaking()) {
                return false;
            }
            if (c.requireSprinting() && !player.isSprinting()) {
                return false;
            }
            if (c.requireOnGround() && !player.isOnGround()) {
                return false;
            }
            if (c.requireInAir() && player.isOnGround()) {
                return false;
            }
            if (c.requirePermission() != null && !c.requirePermission().isEmpty()
                && !player.hasPermission(c.requirePermission())) {
                return false;
            }
            if (c.requireHealthBelow() < 100) {
                double max = 20.0;
                AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealth != null && maxHealth.getValue() > 0) {
                    max = maxHealth.getValue();
                }
                double healthPercent = (player.getHealth() / max) * 100.0;
                if (healthPercent > c.requireHealthBelow()) {
                    return false;
                }
            }
            return true;
        }

        private static long remainingCooldown(UUID playerId, int cooldownSeconds, long now) {
            if (cooldownSeconds <= 0) {
                return 0;
            }
            Long lastUse = cooldowns.get(playerId);
            if (lastUse == null) {
                return 0;
            }
            long elapsed = now - lastUse;
            long total = cooldownSeconds * 1000L;
            return elapsed >= total ? 0 : total - elapsed;
        }

        private static void actionBar(Player player, String miniMessage) {
            player.sendActionBar(MiniMessage.miniMessage().deserialize(miniMessage));
        }

        // --- Resolution ------------------------------------------------------

        @Nullable
        private static DashMechanic resolve(@Nullable ItemStack item) {
            String id = NexoItems.idFromItem(item);
            if (id == null) {
                return null;
            }
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(id);
            if (mechanics == null) {
                return null;
            }
            return mechanics.getDashMechanic();
        }

        /** Resolves the dash mechanic from either hand (main hand takes precedence). */
        @Nullable
        private static DashMechanic resolveHeld(Player player) {
            DashMechanic mainHand = resolve(player.getInventory().getItemInMainHand());
            if (mainHand != null) {
                return mainHand;
            }
            return resolve(player.getInventory().getItemInOffHand());
        }
    }
}
