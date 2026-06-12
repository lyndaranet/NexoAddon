package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.protectionlib.ProtectionLib;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;
import zone.vao.nexoAddon.items.mechanics.AreaAbilityMechanic.AbilityEffect;
import zone.vao.nexoAddon.items.mechanics.TeleportMechanic.ParticleEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Generic simulated projectile mechanic — fires a tick-by-tick projectile in the player's look
 * direction with optional gravity, homing, bouncing, piercing and on-impact effects. Unlike
 * {@link BeamMechanic} (an instant ray), this projectile travels over time. One config block
 * ({@code Mechanics.projectile}).
 */
public record ProjectileMechanic(String trigger, int cooldownSeconds, double range, double speed,
                                 double hitRadius, int maxActive, double gravity, int bounces, int pierce,
                                 boolean homing, double homingRadius, double homingStrength, double damage,
                                 double knockback, List<AbilityEffect> effects, List<String> commands,
                                 List<String> blockCommands, double explosionRadius, double explosionDamage,
                                 List<AbilityEffect> explosionEffects, @Nullable Particle explosionParticle,
                                 ProjectileConditions conditions, List<TrailEntry> trail,
                                 List<ParticleEntry> impactParticles, @Nullable Sound soundLaunch,
                                 @Nullable Sound soundImpact) {

    public static final String TRIGGER_RIGHT_CLICK = "right_click";
    public static final String TRIGGER_SHIFT_RIGHT_CLICK = "shift_right_click";
    public static final String TRIGGER_LEFT_CLICK = "left_click";
    public static final String TRIGGER_ON_HIT = "on_hit";

    public record TrailEntry(Particle particle, int count, double offset) {
    }

    public record ProjectileConditions(boolean requireSneaking, int requireHealthBelowPercent,
                                       @Nullable String requirePermission) {
    }

    /** Mutable in-flight state for a single launched projectile. */
    public static final class ActiveProjectile {
        Location position;
        final Vector velocity;
        double distanceTravelled;
        int bounceCount;
        final Set<UUID> hitEntities;
        final UUID shooterUUID;
        final ProjectileMechanic mechanic;

        ActiveProjectile(Location position, Vector velocity, UUID shooterUUID, ProjectileMechanic mechanic) {
            this.position = position;
            this.velocity = velocity;
            this.distanceTravelled = 0;
            this.bounceCount = 0;
            this.hitEntities = new HashSet<>();
            this.shooterUUID = shooterUUID;
            this.mechanic = mechanic;
        }
    }

    public static class ProjectileMechanicListener implements Listener {

        private static final Map<UUID, Long> cooldowns = new HashMap<>();
        private static final Map<UUID, List<ActiveProjectile>> activeProjectiles = new HashMap<>();
        private static boolean taskStarted = false;

        /** Starts the single shared tick task advancing every active projectile. Called once. */
        public static void startProjectileTask() {
            if (taskStarted) {
                return;
            }
            taskStarted = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    tickAll();
                }
            }.runTaskTimer(NexoAddon.getInstance(), 0L, 1L);
        }

        // --- Triggers --------------------------------------------------------

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
            ProjectileMechanic mechanic = resolve(event.getItem());
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

            Location start = player.getEyeLocation().clone();
            handleFire(player, mechanic, start, player.getEyeLocation().getDirection());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEntityDamage(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player player)) {
                return;
            }
            if (!(event.getEntity() instanceof LivingEntity hit)) {
                return;
            }
            ProjectileMechanic mechanic = resolve(player.getEquipment().getItemInMainHand());
            if (mechanic == null || !TRIGGER_ON_HIT.equals(mechanic.trigger())) {
                return;
            }
            // Fire from the struck entity's location, travelling in the player's look direction.
            Location start = hit.getLocation().clone();
            start.setY(hit.getEyeLocation().getY());
            handleFire(player, mechanic, start, player.getEyeLocation().getDirection());
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            activeProjectiles.remove(event.getPlayer().getUniqueId());
        }

        // --- Fire ------------------------------------------------------------

        /**
         * Launches a projectile from the player's eyes in their look direction, bypassing this
         * mechanic's own cooldown/conditions. Used by other mechanics (e.g. {@link BowMechanic})
         * that embed a projectile config and manage their own gating.
         */
        public static void launchProjectile(Player player, ProjectileMechanic mechanic) {
            startProjectileTask();
            Vector dir = player.getEyeLocation().getDirection();
            if (dir.lengthSquared() < 1.0e-9) {
                return;
            }
            Location start = player.getEyeLocation().clone();
            Vector velocity = dir.normalize().multiply(mechanic.speed());

            UUID playerId = player.getUniqueId();
            List<ActiveProjectile> list = activeProjectiles.computeIfAbsent(playerId, k -> new ArrayList<>());
            int max = Math.max(1, mechanic.maxActive());
            while (list.size() >= max) {
                list.remove(0);
            }
            list.add(new ActiveProjectile(start, velocity, playerId, mechanic));

            if (mechanic.soundLaunch() != null) {
                player.playSound(player.getLocation(), mechanic.soundLaunch(), 1.0f, 1.0f);
            }
        }

        private static void handleFire(Player player, ProjectileMechanic mechanic, Location start, Vector direction) {
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

            Vector dir = direction.clone();
            if (dir.lengthSquared() < 1.0e-9) {
                return;
            }
            Vector velocity = dir.normalize().multiply(mechanic.speed());

            List<ActiveProjectile> list = activeProjectiles.computeIfAbsent(playerId, k -> new ArrayList<>());
            int max = Math.max(1, mechanic.maxActive());
            // Launching when already at the cap cancels the oldest projectile(s) in flight.
            while (list.size() >= max) {
                list.remove(0);
            }
            list.add(new ActiveProjectile(start.clone(), velocity, playerId, mechanic));

            if (mechanic.soundLaunch() != null) {
                player.playSound(player.getLocation(), mechanic.soundLaunch(), 1.0f, 1.0f);
            }

            if (mechanic.cooldownSeconds() > 0) {
                cooldowns.put(playerId, now);
            }
        }

        // --- Tick ------------------------------------------------------------

        private static void tickAll() {
            if (activeProjectiles.isEmpty()) {
                return;
            }
            Iterator<Map.Entry<UUID, List<ActiveProjectile>>> entries = activeProjectiles.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<UUID, List<ActiveProjectile>> entry = entries.next();
                List<ActiveProjectile> list = entry.getValue();
                Iterator<ActiveProjectile> it = list.iterator();
                while (it.hasNext()) {
                    ActiveProjectile proj = it.next();
                    boolean alive;
                    try {
                        alive = advanceProjectile(proj);
                    } catch (Exception ex) {
                        NexoAddon.getInstance().getLogger()
                            .warning("Error advancing projectile: " + ex.getMessage());
                        alive = false;
                    }
                    if (!alive) {
                        it.remove();
                    }
                }
                if (list.isEmpty()) {
                    entries.remove();
                }
            }
        }

        /** Advances one projectile by a single tick; returns {@code false} when it should be removed. */
        private static boolean advanceProjectile(ActiveProjectile proj) {
            ProjectileMechanic mechanic = proj.mechanic;
            Location pos = proj.position;
            World world = pos.getWorld();
            if (world == null) {
                return false;
            }

            if (mechanic.gravity() > 0) {
                proj.velocity.setY(proj.velocity.getY() - mechanic.gravity());
            }
            if (mechanic.homing()) {
                applyHoming(proj, mechanic, world);
            }

            Vector step = proj.velocity.clone();
            if (step.lengthSquared() < 1.0e-9) {
                return false;
            }
            step.normalize().multiply(mechanic.speed());

            Location prev = pos.clone();
            pos.add(step);
            proj.distanceTravelled += mechanic.speed();

            spawnTrail(pos, mechanic.trail());

            // Block collision — bounce, or final impact once bounces are exhausted.
            if (pos.getBlock().getType().isSolid()) {
                if (mechanic.bounces() > 0 && proj.bounceCount < mechanic.bounces()) {
                    reflect(proj, prev, step, world);
                    proj.bounceCount++;
                    proj.position = prev; // back the projectile out of the wall
                    return true;
                }
                applyImpact(proj, prev, null);
                return false;
            }

            // Entity collision.
            for (LivingEntity entity : world.getNearbyLivingEntities(pos, mechanic.hitRadius())) {
                if (entity.getUniqueId().equals(proj.shooterUUID)) {
                    continue;
                }
                if (!proj.hitEntities.add(entity.getUniqueId())) {
                    continue;
                }
                applyImpact(proj, pos.clone(), entity);
                if (proj.hitEntities.size() > mechanic.pierce()) {
                    return false;
                }
            }

            return proj.distanceTravelled < mechanic.range();
        }

        // --- Flight modifiers ------------------------------------------------

        private static void applyHoming(ActiveProjectile proj, ProjectileMechanic mechanic, World world) {
            LivingEntity target = null;
            double bestSq = mechanic.homingRadius() * mechanic.homingRadius();
            for (LivingEntity entity : world.getNearbyLivingEntities(proj.position, mechanic.homingRadius())) {
                if (entity.getUniqueId().equals(proj.shooterUUID)) {
                    continue;
                }
                double distSq = entity.getLocation().distanceSquared(proj.position);
                if (distSq < bestSq) {
                    bestSq = distSq;
                    target = entity;
                }
            }
            if (target == null) {
                return;
            }
            Vector diff = target.getEyeLocation().toVector().subtract(proj.position.toVector());
            if (diff.lengthSquared() < 1.0e-9) {
                return;
            }
            diff.normalize().multiply(mechanic.homingStrength());
            proj.velocity.add(diff);
            if (proj.velocity.lengthSquared() > 1.0e-9) {
                proj.velocity.normalize().multiply(mechanic.speed());
            }
        }

        /** Reflects the velocity off whichever block face(s) the projectile crossed into. */
        private static void reflect(ActiveProjectile proj, Location prev, Vector step, World world) {
            boolean solidX = isSolid(world, prev.getX() + step.getX(), prev.getY(), prev.getZ());
            boolean solidY = isSolid(world, prev.getX(), prev.getY() + step.getY(), prev.getZ());
            boolean solidZ = isSolid(world, prev.getX(), prev.getY(), prev.getZ() + step.getZ());

            Vector v = proj.velocity;
            if (!solidX && !solidY && !solidZ) {
                // Corner / diagonal hit — invert fully.
                v.multiply(-1);
                return;
            }
            if (solidX) {
                v.setX(-v.getX());
            }
            if (solidY) {
                v.setY(-v.getY());
            }
            if (solidZ) {
                v.setZ(-v.getZ());
            }
        }

        private static boolean isSolid(World world, double x, double y, double z) {
            Material type = new Location(world, x, y, z).getBlock().getType();
            return type.isSolid();
        }

        // --- Impact ----------------------------------------------------------

        private static void applyImpact(ActiveProjectile proj, Location impact, @Nullable LivingEntity target) {
            ProjectileMechanic mechanic = proj.mechanic;
            World world = impact.getWorld();
            if (world == null) {
                return;
            }
            Player shooter = Bukkit.getPlayer(proj.shooterUUID);

            for (ParticleEntry entry : mechanic.impactParticles()) {
                world.spawnParticle(entry.particle(), impact, entry.count(), 0.2, 0.2, 0.2, 0.0);
            }
            if (mechanic.soundImpact() != null) {
                world.playSound(impact, mechanic.soundImpact(), 1.0f, 1.0f);
            }

            if (target != null) {
                damageEntity(shooter, target, mechanic.damage());
                applyEffects(target, mechanic.effects());
                if (mechanic.knockback() > 0) {
                    Vector kb = target.getLocation().toVector().subtract(impact.toVector());
                    if (kb.lengthSquared() > 1.0e-6) {
                        target.setVelocity(target.getVelocity().add(kb.normalize().multiply(mechanic.knockback())));
                    }
                }
                runCommands(shooter, target, mechanic.commands());
            } else {
                runBlockCommands(shooter, impact, mechanic.blockCommands());
            }

            if (mechanic.explosionRadius() > 0) {
                for (LivingEntity entity : world.getNearbyLivingEntities(impact, mechanic.explosionRadius())) {
                    if (entity.getUniqueId().equals(proj.shooterUUID)) {
                        continue;
                    }
                    damageEntity(shooter, entity, mechanic.explosionDamage());
                    applyEffects(entity, mechanic.explosionEffects());
                }
                if (mechanic.explosionParticle() != null) {
                    world.spawnParticle(mechanic.explosionParticle(), impact, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }

        private static void damageEntity(@Nullable Player shooter, LivingEntity entity, double amount) {
            if (amount <= 0) {
                return;
            }
            if (shooter != null) {
                if (!ProtectionLib.canInteract(shooter, entity.getLocation())) {
                    return;
                }
                entity.damage(amount, shooter);
            } else {
                entity.damage(amount);
            }
        }

        // --- Visuals ---------------------------------------------------------

        private static void spawnTrail(Location loc, List<TrailEntry> trail) {
            World world = loc.getWorld();
            if (world == null) {
                return;
            }
            for (TrailEntry entry : trail) {
                double off = entry.offset();
                world.spawnParticle(entry.particle(), loc, entry.count(), off, off, off, 0.0);
            }
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

        private static void runCommands(@Nullable Player player, LivingEntity target, List<String> commands) {
            if (commands.isEmpty()) {
                return;
            }
            String name = player == null ? "" : player.getName();
            String targetName = target.getName();
            for (String raw : commands) {
                String command = raw.replace("{player}", name).replace("{target}", targetName);
                NexoAddon.getInstance().getFoliaLib().getScheduler()
                    .runNextTick(t -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
            }
        }

        private static void runBlockCommands(@Nullable Player player, Location impact, List<String> commands) {
            if (commands.isEmpty()) {
                return;
            }
            String name = player == null ? "" : player.getName();
            String x = String.valueOf(impact.getBlockX());
            String y = String.valueOf(impact.getBlockY());
            String z = String.valueOf(impact.getBlockZ());
            for (String raw : commands) {
                String command = raw.replace("{player}", name)
                    .replace("{x}", x).replace("{y}", y).replace("{z}", z);
                NexoAddon.getInstance().getFoliaLib().getScheduler()
                    .runNextTick(t -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
            }
        }

        private static boolean conditionsMet(Player player, ProjectileConditions c) {
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
        private static ProjectileMechanic resolve(@Nullable ItemStack item) {
            String id = NexoItems.idFromItem(item);
            if (id == null) {
                return null;
            }
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(id);
            if (mechanics == null) {
                return null;
            }
            return mechanics.getProjectileMechanic();
        }
    }
}
