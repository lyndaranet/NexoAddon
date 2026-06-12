package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.protectionlib.ProtectionLib;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;
import zone.vao.nexoAddon.items.mechanics.AreaAbilityMechanic.AbilityEffect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Generic beam mechanic — ray-marches a visible particle beam in the player's look direction,
 * applying hit actions to entities (and optionally piercing blocks/entities) along the path.
 * One config block ({@code Mechanics.beam}).
 */
public record BeamMechanic(String trigger, int cooldownSeconds, double range, double width, double height,
                           boolean pierce, boolean pierceBlocks, double damage, double knockback,
                           List<AbilityEffect> effects, List<String> commands, List<AbilityEffect> selfEffects,
                           double selfDamage, BeamConditions conditions, List<BeamSegment> beamSegments,
                           @Nullable Particle hitParticle, @Nullable Sound sound, @Nullable Sound soundHit) {

    public static final String TRIGGER_RIGHT_CLICK = "right_click";
    public static final String TRIGGER_SHIFT_RIGHT_CLICK = "shift_right_click";
    public static final String TRIGGER_LEFT_CLICK = "left_click";
    public static final String TRIGGER_ON_HIT = "on_hit";

    private static final double STEP_SIZE = 0.25;

    public record BeamSegment(int fromPct, int toPct, Particle particle, int count) {
    }

    public record BeamConditions(boolean requireSneaking, int requireHealthBelowPercent,
                                 @Nullable String requirePermission) {
    }

    public static class BeamMechanicListener implements Listener {

        private static final Map<UUID, Long> cooldowns = new HashMap<>();

        @EventHandler
        public void onInteract(PlayerInteractEvent event) {
            if (event.getHand() != EquipmentSlot.HAND) {
                return;
            }
            Action action = event.getAction();
            boolean right = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
            boolean left = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
            if (!right && !left) {
                return;
            }

            Player player = event.getPlayer();
            BeamMechanic mechanic = resolve(event.getItem());
            if (mechanic == null) {
                return;
            }

            switch (mechanic.trigger()) {
                case TRIGGER_RIGHT_CLICK -> {
                    if (!right) return;
                }
                case TRIGGER_SHIFT_RIGHT_CLICK -> {
                    if (!right || !player.isSneaking()) return;
                }
                case TRIGGER_LEFT_CLICK -> {
                    if (!left) return;
                }
                default -> {
                    return;
                }
            }

            handleBeam(player, mechanic, player.getEyeLocation());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEntityDamage(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player player)) {
                return;
            }
            if (!(event.getEntity() instanceof LivingEntity hit)) {
                return;
            }
            BeamMechanic mechanic = resolve(player.getEquipment().getItemInMainHand());
            if (mechanic == null || !TRIGGER_ON_HIT.equals(mechanic.trigger())) {
                return;
            }
            // Beam originates from the struck entity, travelling in the player's look direction.
            Location start = hit.getLocation().clone();
            start.setY(hit.getEyeLocation().getY());
            handleBeam(player, mechanic, start);
        }

        // --- Core ------------------------------------------------------------

        private static void handleBeam(Player player, BeamMechanic mechanic, Location start) {
            UUID playerId = player.getUniqueId();

            if (!conditionsMet(player, mechanic.conditions())) {
                actionBar(player, "<red>Du kannst das gerade nicht benutzen.");
                return;
            }

            long now = System.currentTimeMillis();
            long remainingMs = remainingCooldown(playerId, mechanic.cooldownSeconds(), now);
            if (remainingMs > 0) {
                long remainingSeconds = (remainingMs + 999) / 1000;
                actionBar(player, "<red>Noch <bold>" + remainingSeconds + "s</bold> Cooldown!");
                return;
            }

            if (mechanic.sound() != null) {
                player.playSound(player.getLocation(), mechanic.sound(), 1.0f, 1.0f);
            }

            // Self actions, once per use.
            applyEffects(player, mechanic.selfEffects());
            if (mechanic.selfDamage() > 0) {
                player.damage(mechanic.selfDamage());
            }

            fireBeam(player, mechanic, start);

            if (mechanic.cooldownSeconds() > 0) {
                cooldowns.put(playerId, now);
            }
        }

        private static void fireBeam(Player player, BeamMechanic mechanic, Location start) {
            Vector dir = player.getEyeLocation().getDirection().normalize();
            double halfWidth = mechanic.width() / 2;
            double halfHeight = mechanic.height() / 2;
            Set<UUID> hitEntities = new HashSet<>();
            int steps = (int) (mechanic.range() / STEP_SIZE);

            outer:
            for (int i = 0; i <= steps; i++) {
                double traveled = i * STEP_SIZE;
                Location pos = start.clone().add(dir.clone().multiply(traveled));

                if (pos.getBlock().getType().isSolid() && !mechanic.pierceBlocks()) {
                    break;
                }

                double progress = mechanic.range() <= 0 ? 0 : traveled / mechanic.range();
                BeamSegment segment = getSegmentForProgress(mechanic.beamSegments(), progress);
                if (segment != null) {
                    pos.getWorld().spawnParticle(segment.particle(), pos, segment.count(), 0.0, 0.0, 0.0, 0.0);
                }

                for (LivingEntity entity : pos.getWorld()
                    .getNearbyLivingEntities(pos, halfWidth, halfHeight, halfWidth)) {
                    if (entity.getUniqueId().equals(player.getUniqueId())) {
                        continue;
                    }
                    if (!hitEntities.add(entity.getUniqueId())) {
                        continue;
                    }
                    applyHit(player, entity, mechanic, dir);
                    if (!mechanic.pierce()) {
                        break outer;
                    }
                }
            }
        }

        // --- Hit handling ----------------------------------------------------

        private static void applyHit(Player player, LivingEntity entity, BeamMechanic mechanic, Vector dir) {
            if (mechanic.damage() > 0) {
                if (!ProtectionLib.canInteract(player, entity.getLocation())) {
                    return;
                }
                entity.damage(mechanic.damage(), player);
            }
            if (mechanic.knockback() > 0) {
                entity.setVelocity(entity.getVelocity().add(dir.clone().multiply(mechanic.knockback())));
            }
            applyEffects(entity, mechanic.effects());
            runCommands(player, entity, mechanic.commands());
            if (mechanic.hitParticle() != null) {
                entity.getWorld().spawnParticle(mechanic.hitParticle(),
                    entity.getLocation().add(0, entity.getHeight() / 2, 0), 10, 0.3, 0.3, 0.3, 0.0);
            }
            if (mechanic.soundHit() != null) {
                entity.getWorld().playSound(entity.getLocation(), mechanic.soundHit(), 1.0f, 1.0f);
            }
        }

        @Nullable
        private static BeamSegment getSegmentForProgress(List<BeamSegment> segments, double progress) {
            if (segments.isEmpty()) {
                return null;
            }
            for (BeamSegment segment : segments) {
                if (progress >= segment.fromPct() / 100.0 && progress <= segment.toPct() / 100.0) {
                    return segment;
                }
            }
            return segments.get(0);
        }

        // --- Shared helpers --------------------------------------------------

        private static void applyEffects(LivingEntity entity, List<AbilityEffect> effects) {
            for (AbilityEffect effect : effects) {
                if (effect.chance() < 1.0 && Math.random() >= effect.chance()) {
                    continue;
                }
                entity.addPotionEffect(new PotionEffect(
                    effect.type(), effect.durationSeconds() * 20, effect.amplifier()));
            }
        }

        private static void runCommands(Player player, LivingEntity target, List<String> commands) {
            if (commands.isEmpty()) {
                return;
            }
            String name = player.getName();
            String targetName = target.getName();
            for (String raw : commands) {
                String command = raw.replace("{player}", name).replace("{target}", targetName);
                NexoAddon.getInstance().getFoliaLib().getScheduler()
                    .runNextTick(t -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
            }
        }

        private static boolean conditionsMet(Player player, BeamConditions c) {
            if (c.requireSneaking() && !player.isSneaking()) {
                return false;
            }
            if (c.requirePermission() != null && !c.requirePermission().isEmpty()
                && !player.hasPermission(c.requirePermission())) {
                return false;
            }
            if (c.requireHealthBelowPercent() < 100) {
                double max = 20.0;
                AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealth != null && maxHealth.getValue() > 0) {
                    max = maxHealth.getValue();
                }
                double healthPercent = (player.getHealth() / max) * 100.0;
                if (healthPercent > c.requireHealthBelowPercent()) {
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

        @Nullable
        private static BeamMechanic resolve(@Nullable ItemStack item) {
            String id = NexoItems.idFromItem(item);
            if (id == null) {
                return null;
            }
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(id);
            if (mechanics == null) {
                return null;
            }
            return mechanics.getBeamMechanic();
        }
    }
}
