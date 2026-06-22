package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.protectionlib.ProtectionLib;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;
import zone.vao.nexoAddon.items.mechanics.AreaAbilityMechanic.AbilityEffect;
import zone.vao.nexoAddon.items.mechanics.ProjectileMechanic.TrailEntry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Generic bow mechanic with two independent, optional parts:
 * <ul>
 *   <li><b>Arrow Passive</b> — modifies every arrow fired from the bow and applies on-hit effects.</li>
 *   <li><b>Special Shot</b> — a {@code shift_right_click} ability that fires a powerful special
 *       projectile with its own cooldown.</li>
 * </ul>
 * One config block ({@code Mechanics.bow}).
 */
public record BowMechanic(@Nullable ArrowPassive arrowPassive, @Nullable SpecialShot specialShot) {

    public static final String TYPE_FIREBALL = "fireball";
    public static final String TYPE_LIGHTNING = "lightning";
    public static final String TYPE_CLUSTER = "cluster";
    public static final String TYPE_EXPLOSIVE_ARROW = "explosive_arrow";
    public static final String TYPE_PROJECTILE = "projectile";

    /** Modifiers + on-hit effects applied to every arrow fired from the bow. */
    public record ArrowPassive(int fireTicks, double damageMultiplier, double velocityMultiplier, int knockback,
                               int piercing, boolean critical, boolean glowing, List<AbilityEffect> hitEffects,
                               double hitDamageBonus, int hitFireDuration, double hitExplosionRadius,
                               double hitExplosionDamage, List<String> hitCommands, List<TrailEntry> hitParticles,
                               @Nullable Sound hitSound, List<TrailEntry> launchParticles,
                               @Nullable Sound launchSound) {
    }

    /** Separate {@code shift_right_click} special ability with its own cooldown. */
    public record SpecialShot(int cooldownSeconds, String type, double yield, boolean incendiary, double speed,
                              double range, double damageLightning, int count, double spreadAngle,
                              @Nullable ProjectileMechanic projectileConfig, SpecialConditions conditions,
                              List<TrailEntry> launchParticles, @Nullable Sound launchSound, String cooldownMessage) {
    }

    public record SpecialConditions(String requirePermission, boolean requireArrows, int requireHealthBelow) {
    }

    public static class BowMechanicListener implements Listener {

        /** arrow entity UUID -> Nexo item id of the bow that fired it. */
        private static final Map<UUID, String> arrowItemMap = new HashMap<>();
        /** arrow entity UUIDs that should explode on impact (special explosive_arrow shot). */
        private static final Set<UUID> explosiveArrows = new HashSet<>();
        /** per-player special shot cooldown timestamps. */
        private static final Map<UUID, Long> cooldowns = new HashMap<>();

        private static final double CLUSTER_ARROW_SPEED = 2.5;
        private static final double LIGHTNING_BASE_DAMAGE = 5.0;
        private static final double DEFAULT_EXPLOSION_RADIUS = 3.0;
        private static final double DEFAULT_EXPLOSION_DAMAGE = 6.0;

        // --- Arrow passive: launch -------------------------------------------

        @EventHandler(ignoreCancelled = true)
        public void onShoot(EntityShootBowEvent event) {
            if (!(event.getEntity() instanceof Player player)) {
                return;
            }
            String id = NexoItems.idFromItem(event.getBow());
            if (id == null) {
                return;
            }
            BowMechanic mechanic = resolve(id);
            if (mechanic == null || mechanic.arrowPassive() == null) {
                return;
            }
            if (!(event.getProjectile() instanceof Arrow arrow)) {
                return;
            }
            ArrowPassive passive = mechanic.arrowPassive();
            applyArrowModifiers(arrow, passive);
            arrowItemMap.put(arrow.getUniqueId(), id);

            Location loc = player.getLocation();
            spawnParticles(loc, passive.launchParticles());
            if (passive.launchSound() != null) {
                player.getWorld().playSound(loc, passive.launchSound(), 1.0f, 1.0f);
            }
        }

        private static void applyArrowModifiers(Arrow arrow, ArrowPassive p) {
            if (p.fireTicks() > 0) {
                arrow.setFireTicks(p.fireTicks());
            }
            if (p.damageMultiplier() != 1.0) {
                arrow.setDamage(arrow.getDamage() * p.damageMultiplier());
            }
            if (p.velocityMultiplier() != 1.0) {
                arrow.setVelocity(arrow.getVelocity().multiply(p.velocityMultiplier()));
            }
            if (p.knockback() > 0) {
                arrow.setKnockbackStrength(p.knockback());
            }
            if (p.piercing() > 0) {
                arrow.setPierceLevel(p.piercing());
            }
            if (p.critical()) {
                arrow.setCritical(true);
            }
            if (p.glowing()) {
                arrow.setGlowing(true);
            }
        }

        // --- Arrow passive: hit ----------------------------------------------

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onArrowDamage(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Arrow arrow)) {
                return;
            }
            if (!(arrow.getShooter() instanceof Player player)) {
                return;
            }
            String id = arrowItemMap.get(arrow.getUniqueId());
            if (id == null) {
                return;
            }
            BowMechanic mechanic = resolve(id);
            if (mechanic == null || mechanic.arrowPassive() == null) {
                return;
            }
            ArrowPassive p = mechanic.arrowPassive();

            if (p.hitDamageBonus() > 0) {
                event.setDamage(event.getDamage() + p.hitDamageBonus());
            }

            Location loc = event.getEntity().getLocation();
            World world = loc.getWorld();
            if (event.getEntity() instanceof LivingEntity target) {
                applyEffects(target, p.hitEffects());
                if (p.hitFireDuration() > 0) {
                    target.setFireTicks(p.hitFireDuration() * 20);
                }
                runHitCommands(player, target, p.hitCommands());
            }
            spawnParticles(loc, p.hitParticles());
            if (p.hitSound() != null && world != null) {
                world.playSound(loc, p.hitSound(), 1.0f, 1.0f);
            }
            if (p.hitExplosionRadius() > 0) {
                doExplosion(player, loc, p.hitExplosionRadius(), p.hitExplosionDamage());
            }
        }

        // --- Arrow tracking cleanup + explosive arrow ------------------------

        @EventHandler
        public void onProjectileHit(ProjectileHitEvent event) {
            if (!(event.getEntity() instanceof Arrow arrow)) {
                return;
            }
            UUID uuid = arrow.getUniqueId();
            String id = arrowItemMap.remove(uuid);
            if (!explosiveArrows.remove(uuid)) {
                return;
            }
            BowMechanic mechanic = id == null ? null : resolve(id);
            double radius = DEFAULT_EXPLOSION_RADIUS;
            double damage = DEFAULT_EXPLOSION_DAMAGE;
            if (mechanic != null) {
                if (mechanic.specialShot() != null && mechanic.specialShot().yield() > 0) {
                    radius = mechanic.specialShot().yield();
                }
                ArrowPassive p = mechanic.arrowPassive();
                if (p != null && p.hitExplosionRadius() > 0) {
                    radius = p.hitExplosionRadius();
                    damage = p.hitExplosionDamage();
                }
            }
            Player shooter = arrow.getShooter() instanceof Player p ? p : null;
            doExplosion(shooter, arrow.getLocation(), radius, damage);
        }

        // --- Special shot ----------------------------------------------------

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
            if (!player.isSneaking()) {
                return;
            }
            String id = NexoItems.idFromItem(event.getItem());
            if (id == null) {
                return;
            }
            BowMechanic mechanic = resolve(id);
            if (mechanic == null || mechanic.specialShot() == null) {
                return;
            }
            SpecialShot shot = mechanic.specialShot();

            if (!conditionsMet(player, shot.conditions())) {
                return;
            }

            long now = System.currentTimeMillis();
            long remainingMs = remainingCooldown(player.getUniqueId(), shot.cooldownSeconds(), now);
            if (remainingMs > 0) {
                long remainingSeconds = (remainingMs + 999) / 1000;
                String message = shot.cooldownMessage().replace("{remaining}", String.valueOf(remainingSeconds));
                player.sendActionBar(MiniMessage.miniMessage().deserialize(message));
                return;
            }

            Location eye = player.getEyeLocation();
            spawnParticles(eye, shot.launchParticles());
            if (shot.launchSound() != null) {
                player.getWorld().playSound(eye, shot.launchSound(), 1.0f, 1.0f);
            }

            boolean fired = dispatchSpecial(player, id, mechanic, shot, eye);
            if (fired) {
                // Stop the bow from also starting to draw a normal arrow.
                event.setCancelled(true);
                if (shot.cooldownSeconds() > 0) {
                    cooldowns.put(player.getUniqueId(), now);
                }
            }
        }

        private static boolean dispatchSpecial(Player player, String id, BowMechanic mechanic, SpecialShot shot,
            Location eye) {
            World world = eye.getWorld();
            if (world == null) {
                return false;
            }
            Vector direction = eye.getDirection().normalize();
            switch (shot.type()) {
                case TYPE_FIREBALL -> {
                    if (!ProtectionLib.canInteract(player, eye)) {
                        return false;
                    }
                    Fireball fireball = world.spawn(eye, Fireball.class);
                    fireball.setShooter(player);
                    fireball.setYield((float) shot.yield());
                    fireball.setIsIncendiary(shot.incendiary());
                    fireball.setDirection(direction.clone().multiply(shot.speed()));
                }
                case TYPE_LIGHTNING -> {
                    Location strike = rayMarch(player, shot.range());
                    if (!ProtectionLib.canInteract(player, strike)) {
                        return false;
                    }
                    world.strikeLightningEffect(strike);
                    double damage = LIGHTNING_BASE_DAMAGE * shot.damageLightning();
                    if (damage > 0) {
                        for (LivingEntity entity : world.getNearbyLivingEntities(strike, 3.0)) {
                            if (entity.getUniqueId().equals(player.getUniqueId())) {
                                continue;
                            }
                            damageEntity(player, entity, damage);
                        }
                    }
                }
                case TYPE_CLUSTER -> {
                    ArrowPassive passive = mechanic.arrowPassive();
                    double spread = Math.tan(Math.toRadians(shot.spreadAngle()));
                    int amount = Math.max(1, shot.count());
                    for (int i = 0; i < amount; i++) {
                        Vector v = direction.clone().add(new Vector(
                            (Math.random() - 0.5) * 2 * spread,
                            (Math.random() - 0.5) * 2 * spread,
                            (Math.random() - 0.5) * 2 * spread));
                        if (v.lengthSquared() < 1.0e-9) {
                            v = direction.clone();
                        }
                        spawnArrow(player, world, eye, v.normalize().multiply(CLUSTER_ARROW_SPEED), id, passive, false);
                    }
                }
                case TYPE_EXPLOSIVE_ARROW -> {
                    ArrowPassive passive = mechanic.arrowPassive();
                    spawnArrow(player, world, eye, direction.clone().multiply(CLUSTER_ARROW_SPEED), id, passive, true);
                }
                case TYPE_PROJECTILE -> {
                    if (shot.projectileConfig() == null) {
                        return false;
                    }
                    ProjectileMechanic.ProjectileMechanicListener.launchProjectile(player, shot.projectileConfig());
                }
                default -> {
                    return false;
                }
            }
            return true;
        }

        private static void spawnArrow(Player player, World world, Location eye, Vector velocity, String id,
            @Nullable ArrowPassive passive, boolean explosive) {
            Arrow arrow = world.spawn(eye, Arrow.class);
            arrow.setShooter(player);
            arrow.setVelocity(velocity);
            if (passive != null) {
                applyArrowModifiers(arrow, passive);
            }
            arrowItemMap.put(arrow.getUniqueId(), id);
            if (explosive) {
                explosiveArrows.add(arrow.getUniqueId());
            }
        }

        /** Ray-marches in the player's look direction, returning the first solid block / entity hit. */
        private static Location rayMarch(Player player, double range) {
            World world = player.getWorld();
            Location eye = player.getEyeLocation();
            Vector dir = eye.getDirection().normalize();
            RayTraceResult result = world.rayTrace(eye, dir, range, FluidCollisionMode.NEVER, true, 0.5,
                entity -> entity instanceof LivingEntity && !entity.equals(player));
            if (result != null) {
                if (result.getHitEntity() != null) {
                    return result.getHitEntity().getLocation();
                }
                if (result.getHitPosition() != null) {
                    return result.getHitPosition().toLocation(world);
                }
            }
            return eye.clone().add(dir.multiply(range));
        }

        // --- Shared helpers --------------------------------------------------

        private static void doExplosion(@Nullable Player shooter, Location loc, double radius, double damage) {
            World world = loc.getWorld();
            if (world == null || radius <= 0) {
                return;
            }
            world.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1, 0.0, 0.0, 0.0, 0.0);
            world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            for (LivingEntity entity : world.getNearbyLivingEntities(loc, radius)) {
                if (shooter != null && entity.getUniqueId().equals(shooter.getUniqueId())) {
                    continue;
                }
                damageEntity(shooter, entity, damage);
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

        private static void applyEffects(LivingEntity entity, List<AbilityEffect> effects) {
            for (AbilityEffect effect : effects) {
                if (effect.chance() < 1.0 && Math.random() >= effect.chance()) {
                    continue;
                }
                entity.addPotionEffect(new PotionEffect(
                    effect.type(), effect.durationSeconds() * 20, effect.amplifier()));
            }
        }

        private static void spawnParticles(Location loc, List<TrailEntry> particles) {
            World world = loc.getWorld();
            if (world == null) {
                return;
            }
            for (TrailEntry entry : particles) {
                double off = entry.offset();
                world.spawnParticle(entry.particle(), loc, entry.count(), off, off, off, 0.0);
            }
        }

        private static void runHitCommands(Player player, LivingEntity target, List<String> commands) {
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

        private static boolean conditionsMet(Player player, SpecialConditions c) {
            if (c.requirePermission() != null && !c.requirePermission().isEmpty()
                && !player.hasPermission(c.requirePermission())) {
                return false;
            }
            if (c.requireArrows() && !hasArrows(player)) {
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

        private static boolean hasArrows(Player player) {
            return player.getInventory().contains(Material.ARROW)
                || player.getInventory().contains(Material.SPECTRAL_ARROW)
                || player.getInventory().contains(Material.TIPPED_ARROW);
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

        @Nullable
        private static BowMechanic resolve(@Nullable String id) {
            if (id == null) {
                return null;
            }
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(id);
            if (mechanics == null) {
                return null;
            }
            return mechanics.getBowMechanic();
        }
    }
}
