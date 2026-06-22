package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;
import zone.vao.nexoAddon.items.mechanics.AreaAbilityMechanic.AbilityEffect;
import zone.vao.nexoAddon.items.mechanics.ParticleAuraMechanic.AuraLayer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Two-step buff/launch mechanic — right-click activates a timed buff, then stepping onto a configured trigger block
 * (e.g. WATER, LAVA, POWDER_SNOW) launches the player while the buff is active. One config block
 * ({@code Mechanics.block_trigger_launch}).
 */
public record BlockTriggerLaunchMechanic(int activationCooldownSeconds, int durationSeconds,
                                         double perLaunchCooldownSeconds, Set<Material> triggerBlocks,
                                         int checkBlockOffset, double launchPower, double horizontalPower,
                                         String horizontalSource, List<AbilityEffect> activeEffects,
                                         List<AbilityEffect> launchEffects, int noFallDamageTicks,
                                         List<AuraLayer> auraLayers, List<LaunchParticle> launchParticles,
                                         @Nullable Sound launchSound, List<LaunchParticle> activateParticles,
                                         @Nullable Sound activateSound, boolean showActionBar) {

    public static final String SOURCE_MOVEMENT = "movement";
    public static final String SOURCE_LOOK = "look";

    /** A particle burst spawned at the launch/activation point. */
    public record LaunchParticle(Particle particle, int count, double offset) {
    }

    /** Mutable per-player buff state for one active buff window, driven by a single repeating task. */
    private static final class ActiveBuffState {
        final long expiresAt;
        long lastLaunchTime;
        double phaseTicks;
        BukkitTask task;

        ActiveBuffState(long expiresAt) {
            this.expiresAt = expiresAt;
            this.lastLaunchTime = 0L;
        }
    }

    public static class BlockTriggerLaunchMechanicListener implements Listener {

        private static final Map<UUID, ActiveBuffState> activeBuffs = new ConcurrentHashMap<>();
        private static final Map<UUID, Long> noFallDamageUntil = new ConcurrentHashMap<>();
        // Absolute timestamp (ms) at which the per-player activation cooldown expires.
        private static final Map<UUID, Long> cooldownUntil = new ConcurrentHashMap<>();

        // --- Activation ------------------------------------------------------

        // NOT ignoreCancelled: Bukkit delivers RIGHT_CLICK_AIR as "cancelled", so ignoreCancelled would
        // silently skip air clicks and only block clicks would activate.
        @EventHandler
        public void onInteract(PlayerInteractEvent event) {
            if (event.getHand() != EquipmentSlot.HAND) {
                return;
            }
            Action action = event.getAction();
            if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
                return;
            }

            Player player = event.getPlayer();
            BlockTriggerLaunchMechanic mechanic = resolve(event.getItem());
            if (mechanic == null) {
                return;
            }

            UUID id = player.getUniqueId();
            ActiveBuffState current = activeBuffs.get(id);
            long now = System.currentTimeMillis();

            // activation_cooldown blocks re-activation while a buff is running or recently activated.
            if (current != null) {
                return; // already active — let it expire naturally
            }
            Long cdDeadline = cooldownUntil.get(id);
            if (cdDeadline != null && cdDeadline > now) {
                if (mechanic.showActionBar()) {
                    long remaining = (cdDeadline - now + 999) / 1000;
                    actionBar(player, "<red>Noch <bold>" + remaining + "s</bold> Cooldown!");
                }
                return;
            }

            activate(player, mechanic, now);
        }

        private void activate(Player player, BlockTriggerLaunchMechanic mechanic, long now) {
            UUID id = player.getUniqueId();

            if (mechanic.activationCooldownSeconds() > 0) {
                cooldownUntil.put(id, now + mechanic.activationCooldownSeconds() * 1000L);
            }

            applyEffects(player, mechanic.activeEffects());
            spawnParticles(player.getLocation(), mechanic.activateParticles());
            if (mechanic.activateSound() != null) {
                player.playSound(player.getLocation(), mechanic.activateSound(), 1.0f, 1.0f);
            }

            ActiveBuffState state = new ActiveBuffState(now + mechanic.durationSeconds() * 1000L);
            // Single task drives aura rendering, the action-bar countdown and self-expiry.
            state.task = Bukkit.getScheduler().runTaskTimer(NexoAddon.getInstance(),
                () -> buffTick(player, mechanic), 1L, 2L);
            activeBuffs.put(id, state);
        }

        // --- Launch trigger --------------------------------------------------

        @EventHandler(ignoreCancelled = true)
        public void onMove(PlayerMoveEvent event) {
            if (event.getTo() == null || event.getFrom().getBlock().equals(event.getTo().getBlock())) {
                return; // block-position-only filter
            }

            Player player = event.getPlayer();
            ActiveBuffState state = activeBuffs.get(player.getUniqueId());
            if (state == null) {
                return;
            }

            BlockTriggerLaunchMechanic mechanic = resolveHeld(player);
            if (mechanic == null) {
                return;
            }

            Location feet = event.getTo();
            Block block = feet.clone().add(0, -mechanic.checkBlockOffset(), 0).getBlock();
            if (!mechanic.triggerBlocks().contains(block.getType())) {
                return;
            }

            long now = System.currentTimeMillis();
            if (now - state.lastLaunchTime < (long) (mechanic.perLaunchCooldownSeconds() * 1000)) {
                return;
            }

            launch(player, mechanic, event.getFrom(), event.getTo());
            state.lastLaunchTime = now;
        }

        private void launch(Player player, BlockTriggerLaunchMechanic mechanic, Location from, Location to) {
            Vector horizontal = horizontalDirection(player, mechanic, from, to);
            player.setVelocity(new Vector(
                horizontal.getX() * mechanic.horizontalPower(),
                mechanic.launchPower(),
                horizontal.getZ() * mechanic.horizontalPower()));

            applyEffects(player, mechanic.launchEffects());

            if (mechanic.noFallDamageTicks() > 0) {
                noFallDamageUntil.put(player.getUniqueId(),
                    (long) Bukkit.getServer().getCurrentTick() + mechanic.noFallDamageTicks());
            }

            spawnParticles(player.getLocation(), mechanic.launchParticles());
            if (mechanic.launchSound() != null) {
                player.playSound(player.getLocation(), mechanic.launchSound(), 1.0f, 1.0f);
            }
        }

        private static Vector horizontalDirection(Player player, BlockTriggerLaunchMechanic mechanic,
            Location from, Location to) {
            Vector dir;
            if (SOURCE_LOOK.equals(mechanic.horizontalSource())) {
                dir = player.getLocation().getDirection();
            } else {
                dir = to.toVector().subtract(from.toVector());
            }
            dir.setY(0);
            if (dir.lengthSquared() < 1.0e-6) {
                return new Vector(0, 0, 0);
            }
            return dir.normalize();
        }

        // --- Fall damage cancel ----------------------------------------------

        @EventHandler(ignoreCancelled = true)
        public void onDamage(EntityDamageEvent event) {
            if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
                return;
            }
            if (!(event.getEntity() instanceof Player player)) {
                return;
            }
            Long deadline = noFallDamageUntil.get(player.getUniqueId());
            if (deadline == null) {
                return;
            }
            if (Bukkit.getServer().getCurrentTick() <= deadline) {
                event.setCancelled(true);
            } else {
                noFallDamageUntil.remove(player.getUniqueId());
            }
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            clearState(event.getPlayer().getUniqueId());
        }

        // --- Expiry / cleanup ------------------------------------------------

        private void expire(Player player, BlockTriggerLaunchMechanic mechanic) {
            for (AbilityEffect effect : mechanic.activeEffects()) {
                player.removePotionEffect(effect.type());
            }
            clearState(player.getUniqueId());
        }

        private static void clearState(UUID id) {
            ActiveBuffState state = activeBuffs.remove(id);
            if (state != null && state.task != null) {
                state.task.cancel();
            }
            noFallDamageUntil.remove(id);
        }

        // --- Buff tick (aura + action-bar countdown + self-expiry) -----------

        private void buffTick(Player player, BlockTriggerLaunchMechanic mechanic) {
            ActiveBuffState state = activeBuffs.get(player.getUniqueId());
            if (state == null) {
                return;
            }
            if (!player.isOnline()) {
                clearState(player.getUniqueId());
                return;
            }
            long now = System.currentTimeMillis();
            if (now >= state.expiresAt) {
                expire(player, mechanic);
                return;
            }

            renderAura(player, mechanic, state);

            if (mechanic.showActionBar()) {
                long remaining = (state.expiresAt - now + 999) / 1000;
                actionBar(player, "<aqua>Aktiv: <bold>" + remaining + "s</bold>");
            }
        }

        // --- Visuals ---------------------------------------------------------

        private static void renderAura(Player player, BlockTriggerLaunchMechanic mechanic, ActiveBuffState state) {
            if (mechanic.auraLayers().isEmpty()) {
                return;
            }
            state.phaseTicks += 1;
            Location center = player.getLocation();
            World world = center.getWorld();
            for (AuraLayer layer : mechanic.auraLayers()) {
                double phase = state.phaseTicks * layer.rotationSpeed();
                ParticleAuraMechanic.ParticleAuraMechanicListener
                    .renderLayer(world, center, layer, phase, Math.max(1, layer.count()));
            }
        }

        private static void spawnParticles(Location at, List<LaunchParticle> particles) {
            World world = at.getWorld();
            if (world == null) {
                return;
            }
            for (LaunchParticle p : particles) {
                double off = p.offset();
                world.spawnParticle(p.particle(), at.clone().add(0, 1, 0), p.count(), off, off, off, 0.0);
            }
        }

        // --- Shared helpers --------------------------------------------------

        private static void applyEffects(Player player, List<AbilityEffect> effects) {
            for (AbilityEffect effect : effects) {
                if (effect.chance() < 1.0 && Math.random() >= effect.chance()) {
                    continue;
                }
                player.addPotionEffect(new PotionEffect(
                    effect.type(), effect.durationSeconds() * 20, effect.amplifier()));
            }
        }

        private static void actionBar(Player player, String miniMessage) {
            player.sendActionBar(MiniMessage.miniMessage().deserialize(miniMessage));
        }

        @Nullable
        private static BlockTriggerLaunchMechanic resolveHeld(Player player) {
            BlockTriggerLaunchMechanic main = resolve(player.getInventory().getItemInMainHand());
            return main != null ? main : resolve(player.getInventory().getItemInOffHand());
        }

        @Nullable
        private static BlockTriggerLaunchMechanic resolve(@Nullable ItemStack item) {
            String id = NexoItems.idFromItem(item);
            if (id == null) {
                return null;
            }
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(id);
            if (mechanics == null) {
                return null;
            }
            return mechanics.getBlockTriggerLaunchMechanic();
        }
    }
}
