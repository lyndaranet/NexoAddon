package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.protectionlib.ProtectionLib;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Generic teleport mechanic — blink in look direction, to a target, upward, or randomly,
 * with on-arrive effects/damage/launch and particle/sound feedback. One config block
 * ({@code Mechanics.teleport}).
 */
public record TeleportMechanic(String trigger, String mode, int cooldownSeconds, double distance,
                               boolean behindTarget, double arriveDamageRadius, double arriveDamage,
                               double launchVelocity, List<AbilityEffect> effects, List<String> commands,
                               TeleportConditions conditions, List<ParticleEntry> originParticles,
                               List<ParticleEntry> destinationParticles, @Nullable Particle trailParticle,
                               @Nullable Sound soundOrigin, @Nullable Sound soundDestination) {

    public static final String TRIGGER_RIGHT_CLICK = "right_click";
    public static final String TRIGGER_SHIFT_RIGHT_CLICK = "shift_right_click";
    public static final String TRIGGER_ON_HIT = "on_hit";

    private static final double STEP_SIZE = 0.25;
    private static final double EYE_HEIGHT = 1.62;

    public record ParticleEntry(Particle particle, int count) {
    }

    public record TeleportConditions(boolean requireSneaking, int requireHealthBelowPercent,
                                     @Nullable String requirePermission, boolean requireLineOfSight) {
    }

    public static class TeleportMechanicListener implements Listener {

        private static final Map<UUID, Long> cooldowns = new HashMap<>();

        @EventHandler(ignoreCancelled = true)
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
            TeleportMechanic mechanic = resolve(event.getItem());
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

            handleTeleport(player, mechanic);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEntityDamage(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player player)) {
                return;
            }
            if (!(event.getEntity() instanceof LivingEntity)) {
                return;
            }
            TeleportMechanic mechanic = resolve(player.getEquipment().getItemInMainHand());
            if (mechanic == null || !TRIGGER_ON_HIT.equals(mechanic.trigger())) {
                return;
            }
            handleTeleport(player, mechanic);
        }

        // --- Core ------------------------------------------------------------

        private static void handleTeleport(Player player, TeleportMechanic mechanic) {
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

            Location origin = player.getLocation().clone();
            Location destination = computeDestination(player, mechanic);
            if (destination == null) {
                actionBar(player, "<red>Kein Ziel gefunden.");
                return;
            }

            spawnParticles(origin, mechanic.originParticles());
            if (mechanic.trailParticle() != null) {
                spawnTrail(origin, destination, mechanic.trailParticle());
            }

            player.teleport(destination);

            spawnParticles(destination, mechanic.destinationParticles());
            if (mechanic.soundOrigin() != null) {
                origin.getWorld().playSound(origin, mechanic.soundOrigin(), 1.0f, 1.0f);
            }
            if (mechanic.soundDestination() != null) {
                destination.getWorld().playSound(destination, mechanic.soundDestination(), 1.0f, 1.0f);
            }

            for (AbilityEffect effect : mechanic.effects()) {
                player.addPotionEffect(new PotionEffect(
                    effect.type(), effect.durationSeconds() * 20, effect.amplifier()));
            }

            if (mechanic.arriveDamageRadius() > 0 && mechanic.arriveDamage() > 0) {
                for (Entity entity : destination.getWorld()
                    .getNearbyLivingEntities(destination, mechanic.arriveDamageRadius())) {
                    if (entity.getUniqueId().equals(playerId)) {
                        continue;
                    }
                    if (!ProtectionLib.canInteract(player, entity.getLocation())) {
                        continue;
                    }
                    ((LivingEntity) entity).damage(mechanic.arriveDamage(), player);
                }
            }

            if (mechanic.launchVelocity() > 0) {
                player.setVelocity(player.getLocation().getDirection().multiply(mechanic.launchVelocity()));
            }

            runCommands(player, mechanic.commands());

            if (mechanic.cooldownSeconds() > 0) {
                cooldowns.put(playerId, now);
            }
        }

        // --- Destination computation ----------------------------------------

        @Nullable
        private static Location computeDestination(Player player, TeleportMechanic mechanic) {
            return switch (mechanic.mode()) {
                case "look_direction" -> computeLookDirection(player, mechanic.distance());
                case "to_surface" -> computeToSurface(player, mechanic.distance());
                case "to_target" -> computeToTarget(player, mechanic);
                case "upward" -> computeUpward(player, mechanic.distance());
                case "random" -> computeRandom(player, mechanic.distance());
                default -> null;
            };
        }

        private static Location computeLookDirection(Player player, double distance) {
            Location eye = player.getEyeLocation();
            Vector dir = eye.getDirection().normalize();
            Location lastSafe = eye.clone();
            int steps = (int) (distance / STEP_SIZE);
            for (int i = 1; i <= steps; i++) {
                Location next = eye.clone().add(dir.clone().multiply(i * STEP_SIZE));
                if (next.getBlock().getType().isSolid()) {
                    break;
                }
                lastSafe = next;
            }
            // Drop from eye level to feet.
            Location feet = lastSafe.clone().subtract(0, EYE_HEIGHT, 0);
            feet.setYaw(player.getLocation().getYaw());
            feet.setPitch(player.getLocation().getPitch());
            return isSafeStanding(feet) ? feet : null;
        }

        private static Location computeToSurface(Player player, double distance) {
            Location eye = player.getEyeLocation();
            Vector dir = eye.getDirection().normalize();
            int steps = (int) (distance / STEP_SIZE);
            for (int i = 1; i <= steps; i++) {
                Location next = eye.clone().add(dir.clone().multiply(i * STEP_SIZE));
                if (next.getBlock().getType().isSolid()) {
                    // Stand on top of the block we just hit.
                    Location surface = next.getBlock().getLocation().add(0.5, 1.0, 0.5);
                    surface.setYaw(player.getLocation().getYaw());
                    surface.setPitch(player.getLocation().getPitch());
                    return isSafeStanding(surface) ? surface : null;
                }
            }
            return null;
        }

        @Nullable
        private static Location computeToTarget(Player player, TeleportMechanic mechanic) {
            Entity target = player.getTargetEntity((int) mechanic.distance());
            if (!(target instanceof LivingEntity living)) {
                return null;
            }
            if (mechanic.conditions().requireLineOfSight() && !player.hasLineOfSight(living)) {
                return null;
            }

            Location targetLoc = living.getLocation();
            Vector facing = targetLoc.getDirection().setY(0).normalize();
            if (Double.isNaN(facing.getX())) {
                facing = new Vector(0, 0, 1);
            }

            Location destination;
            if (mechanic.behindTarget()) {
                // 1.5 blocks behind the target, facing the same direction (looking at its back).
                destination = targetLoc.clone().subtract(facing.clone().multiply(1.5));
                destination.setYaw(targetLoc.getYaw());
                destination.setPitch(targetLoc.getPitch());
            } else {
                // Next to the target (to its side).
                Vector side = new Vector(-facing.getZ(), 0, facing.getX());
                destination = targetLoc.clone().add(side.multiply(1.5));
                destination.setYaw(player.getLocation().getYaw());
                destination.setPitch(player.getLocation().getPitch());
            }
            return isSafeStanding(destination) ? destination : targetLoc;
        }

        private static Location computeUpward(Player player, double distance) {
            Location feet = player.getLocation().clone();
            World world = feet.getWorld();
            Location lastSafe = feet.clone();
            int steps = (int) (distance / STEP_SIZE);
            for (int i = 1; i <= steps; i++) {
                Location next = feet.clone().add(0, i * STEP_SIZE, 0);
                // Need clearance for the player's body: both feet and head block must be free.
                if (next.getBlock().getType().isSolid()
                    || next.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
                    break;
                }
                lastSafe = next;
            }
            lastSafe.setYaw(feet.getYaw());
            lastSafe.setPitch(feet.getPitch());
            // World height clamp.
            if (lastSafe.getY() > world.getMaxHeight()) {
                lastSafe.setY(world.getMaxHeight() - 2);
            }
            return lastSafe;
        }

        @Nullable
        private static Location computeRandom(Player player, double distance) {
            Location base = player.getLocation();
            World world = base.getWorld();
            for (int attempt = 0; attempt < 10; attempt++) {
                double angle = Math.random() * 2 * Math.PI;
                double dist = 1 + Math.random() * (distance - 1);
                double x = base.getX() + Math.cos(angle) * dist;
                double z = base.getZ() + Math.sin(angle) * dist;

                // Scan a few blocks around the player's elevation for solid ground with air above.
                for (int dy = 2; dy >= -4; dy--) {
                    Location feet = new Location(world, Math.floor(x) + 0.5, base.getBlockY() + dy,
                        Math.floor(z) + 0.5, base.getYaw(), base.getPitch());
                    Location below = feet.clone().subtract(0, 1, 0);
                    if (below.getBlock().getType().isSolid() && isSafeStanding(feet)) {
                        return feet;
                    }
                }
            }
            return null;
        }

        /** Feet block and the block above (head) must both be non-solid. */
        private static boolean isSafeStanding(Location feet) {
            return !feet.getBlock().getType().isSolid()
                && !feet.clone().add(0, 1, 0).getBlock().getType().isSolid();
        }

        // --- Feedback / actions ---------------------------------------------

        private static void spawnParticles(Location location, List<ParticleEntry> entries) {
            for (ParticleEntry entry : entries) {
                location.getWorld().spawnParticle(entry.particle(), location, entry.count(), 0.5, 0.5, 0.5, 0.0);
            }
        }

        private static void spawnTrail(Location origin, Location destination, Particle trailParticle) {
            if (origin.getWorld() != destination.getWorld()) {
                return;
            }
            Vector delta = destination.toVector().subtract(origin.toVector());
            double length = delta.length();
            if (length < 1.0e-6) {
                return;
            }
            Vector step = delta.normalize().multiply(0.5);
            Location point = origin.clone();
            for (double traveled = 0; traveled <= length; traveled += 0.5) {
                origin.getWorld().spawnParticle(trailParticle, point, 1, 0.0, 0.0, 0.0, 0.0);
                point.add(step);
            }
        }

        private static void runCommands(Player player, List<String> commands) {
            if (commands.isEmpty()) {
                return;
            }
            String name = player.getName();
            for (String raw : commands) {
                String command = raw.replace("{player}", name);
                NexoAddon.getInstance().getFoliaLib().getScheduler()
                    .runNextTick(t -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
            }
        }

        private static boolean conditionsMet(Player player, TeleportConditions c) {
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
        private static TeleportMechanic resolve(@Nullable ItemStack item) {
            String id = NexoItems.idFromItem(item);
            if (id == null) {
                return null;
            }
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(id);
            if (mechanics == null) {
                return null;
            }
            return mechanics.getTeleportMechanic();
        }
    }
}
