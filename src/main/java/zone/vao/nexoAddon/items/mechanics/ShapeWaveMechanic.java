package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.protectionlib.ProtectionLib;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;
import zone.vao.nexoAddon.items.mechanics.AreaAbilityMechanic.AbilityEffect;
import zone.vao.nexoAddon.items.mechanics.ProjectileMechanic.TrailEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Generic shape-wave mechanic — instantly affects every living entity inside a configurable 3D
 * shape (cone, cylinder, wedge, nova, fan or line) cast in the player's look direction (or around
 * the player), with an optional tick-by-tick particle expansion animation. Unlike
 * {@link ProjectileMechanic} (a travelling projectile) or {@link BeamMechanic} (an instant ray),
 * this affects a whole volume at once. One config block ({@code Mechanics.shape_wave}).
 */
public record ShapeWaveMechanic(String trigger, int cooldownSeconds, String shape, int maxTargets,
                                double range, double angle, double radius, double height, double minRadius,
                                int rays, double arcDegrees, double damage, int fireDurationSeconds,
                                double knockback, List<AbilityEffect> effects, List<String> commands,
                                List<AbilityEffect> selfEffects, double selfDamage, double selfHeal,
                                ShapeConditions conditions, double particleDensity, boolean animate,
                                int animateTicks, List<TrailEntry> fillParticles, @Nullable Sound sound,
                                @Nullable Sound soundHit) {

    public static final String TRIGGER_RIGHT_CLICK = "right_click";
    public static final String TRIGGER_SHIFT_RIGHT_CLICK = "shift_right_click";
    public static final String TRIGGER_LEFT_CLICK = "left_click";
    public static final String TRIGGER_ON_HIT = "on_hit";

    public static final String SHAPE_CONE = "cone";
    public static final String SHAPE_CYLINDER = "cylinder";
    public static final String SHAPE_WEDGE = "wedge";
    public static final String SHAPE_NOVA = "nova";
    public static final String SHAPE_FAN = "fan";
    public static final String SHAPE_LINE = "line";

    public record ShapeConditions(boolean requireSneaking, int requireHealthBelowPercent,
                                  @Nullable String requirePermission, int minTargetsRequired) {
    }

    public static class ShapeWaveMechanicListener implements Listener {

        private static final Map<UUID, Long> cooldowns = new HashMap<>();
        // Guards against the on_hit trigger re-entering itself when it damages its own targets.
        private static final Set<UUID> processing = new HashSet<>();

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
            ShapeWaveMechanic mechanic = resolve(event.getItem());
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

            Location origin = player.getEyeLocation().clone();
            handleActivation(player, mechanic, origin, player.getEyeLocation().getDirection());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEntityDamage(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player player)) {
                return;
            }
            if (!(event.getEntity() instanceof LivingEntity hit)) {
                return;
            }
            ShapeWaveMechanic mechanic = resolve(player.getEquipment().getItemInMainHand());
            if (mechanic == null || !TRIGGER_ON_HIT.equals(mechanic.trigger())) {
                return;
            }
            // The shape originates at the struck entity, cast in the player's look direction.
            Location origin = hit.getLocation().clone();
            origin.setY(hit.getEyeLocation().getY());
            handleActivation(player, mechanic, origin, player.getEyeLocation().getDirection());
        }

        // --- Core ------------------------------------------------------------

        private static void handleActivation(Player player, ShapeWaveMechanic mechanic, Location origin,
            Vector direction) {
            UUID playerId = player.getUniqueId();
            if (!processing.add(playerId)) {
                return;
            }
            try {
                World world = origin.getWorld();
                if (world == null) {
                    return;
                }

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

                Vector lookDir = direction.clone();
                if (lookDir.lengthSquared() < 1.0e-9) {
                    return;
                }
                lookDir.normalize();

                List<LivingEntity> targets = collectTargets(player, mechanic, origin, lookDir);

                if (targets.size() < mechanic.conditions().minTargetsRequired()) {
                    // Not enough valid targets — fizzle silently, no cooldown consumed.
                    return;
                }

                for (LivingEntity target : targets) {
                    applyToTarget(player, target, origin, mechanic);
                }
                applyToSelf(player, mechanic);

                if (mechanic.sound() != null) {
                    player.playSound(player.getLocation(), mechanic.sound(), 1.0f, 1.0f);
                }

                spawnFill(world, origin, lookDir, mechanic);

                if (mechanic.cooldownSeconds() > 0) {
                    cooldowns.put(playerId, now);
                }
            } finally {
                processing.remove(playerId);
            }
        }

        // --- Target collection ----------------------------------------------

        private static List<LivingEntity> collectTargets(Player player, ShapeWaveMechanic mechanic,
            Location origin, Vector lookDir) {
            double extent = SHAPE_NOVA.equals(mechanic.shape()) ? mechanic.radius() : mechanic.range();
            extent = Math.max(extent, 0.1);

            List<LivingEntity> candidates = new ArrayList<>();
            for (LivingEntity entity : origin.getWorld().getNearbyLivingEntities(origin, extent)) {
                if (entity.getUniqueId().equals(player.getUniqueId())) {
                    continue;
                }
                if (isInShape(origin, lookDir, entity, mechanic)) {
                    candidates.add(entity);
                }
            }

            candidates.sort(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(origin)));
            if (mechanic.maxTargets() > 0 && candidates.size() > mechanic.maxTargets()) {
                return new ArrayList<>(candidates.subList(0, mechanic.maxTargets()));
            }
            return candidates;
        }

        // --- Shape filters ---------------------------------------------------

        private static boolean isInShape(Location origin, Vector lookDir, LivingEntity entity,
            ShapeWaveMechanic m) {
            Location target = entity.getLocation().clone().add(0, entity.getHeight() * 0.5, 0);
            return switch (m.shape()) {
                case SHAPE_CYLINDER -> filterCylinder(origin, lookDir, target, m.range(), m.radius());
                case SHAPE_WEDGE -> filterWedge(origin, lookDir, target, m);
                case SHAPE_NOVA -> filterNova(origin, target, m);
                case SHAPE_FAN -> filterFan(origin, lookDir, target, m);
                case SHAPE_LINE -> filterCylinder(origin, lookDir, target, m.range(), m.radius());
                default -> filterCone(origin, lookDir, target, m.range(), m.angle());
            };
        }

        private static boolean filterCone(Location origin, Vector lookDir, Location target, double range,
            double angleDeg) {
            Vector toEntity = target.toVector().subtract(origin.toVector());
            double dist = toEntity.length();
            if (dist > range) {
                return false;
            }
            if (dist < 1.0e-6) {
                return true;
            }
            return toEntity.angle(lookDir) <= Math.toRadians(angleDeg);
        }

        private static boolean filterCylinder(Location origin, Vector lookDir, Location target, double range,
            double radius) {
            Vector toEntity = target.toVector().subtract(origin.toVector());
            double projection = toEntity.dot(lookDir);
            if (projection < 0 || projection > range) {
                return false;
            }
            Vector axisPoint = lookDir.clone().multiply(projection);
            double perp = toEntity.clone().subtract(axisPoint).length();
            return perp <= radius;
        }

        private static boolean filterWedge(Location origin, Vector lookDir, Location target, ShapeWaveMechanic m) {
            double dy = target.getY() - origin.getY();
            if (Math.abs(dy) > m.height()) {
                return false;
            }
            Vector toFlat = target.toVector().subtract(origin.toVector());
            toFlat.setY(0);
            double dist = toFlat.length();
            if (dist > m.range()) {
                return false;
            }
            if (dist < 1.0e-6) {
                return true;
            }
            Vector lookFlat = lookDir.clone();
            lookFlat.setY(0);
            if (lookFlat.lengthSquared() < 1.0e-9) {
                return true;
            }
            return toFlat.angle(lookFlat) <= Math.toRadians(m.angle());
        }

        private static boolean filterNova(Location origin, Location target, ShapeWaveMechanic m) {
            double dist = target.toVector().subtract(origin.toVector()).length();
            return dist >= m.minRadius() && dist <= m.radius();
        }

        private static boolean filterFan(Location origin, Vector lookDir, Location target, ShapeWaveMechanic m) {
            int rays = Math.max(1, m.rays());
            for (int i = 0; i < rays; i++) {
                double offsetDeg = rays == 1 ? 0 : -m.arcDegrees() / 2 + m.arcDegrees() * i / (rays - 1);
                Vector rayDir = rotateAroundY(lookDir, Math.toRadians(offsetDeg));
                if (filterCone(origin, rayDir, target, m.range(), m.angle())) {
                    return true;
                }
            }
            return false;
        }

        // --- Per-target / self actions --------------------------------------

        private static void applyToTarget(Player player, LivingEntity target, Location origin,
            ShapeWaveMechanic m) {
            if (m.damage() > 0) {
                if (ProtectionLib.canInteract(player, target.getLocation())) {
                    target.damage(m.damage(), player);
                }
            }
            if (m.fireDurationSeconds() > 0) {
                target.setFireTicks(m.fireDurationSeconds() * 20);
            }
            if (m.knockback() > 0) {
                Vector kb = target.getLocation().toVector().subtract(origin.toVector());
                if (kb.lengthSquared() > 1.0e-6) {
                    target.setVelocity(target.getVelocity().add(kb.normalize().multiply(m.knockback())));
                }
            }
            applyEffects(target, m.effects());
            runCommands(player, target, m.commands());
            if (m.soundHit() != null) {
                target.getWorld().playSound(target.getLocation(), m.soundHit(), 1.0f, 1.0f);
            }
        }

        private static void applyToSelf(Player player, ShapeWaveMechanic m) {
            if (m.selfHeal() > 0) {
                heal(player, m.selfHeal());
            }
            if (m.selfDamage() > 0) {
                player.damage(m.selfDamage());
            }
            applyEffects(player, m.selfEffects());
        }

        // --- Particle fill ---------------------------------------------------

        private static void spawnFill(World world, Location origin, Vector lookDir, ShapeWaveMechanic m) {
            if (m.fillParticles().isEmpty()) {
                return;
            }
            if (!m.animate()) {
                fillShape(world, origin, lookDir, m, 1.0);
                return;
            }
            int ticks = Math.max(1, m.animateTicks());
            Location originFinal = origin.clone();
            int[] tick = {1};
            NexoAddon.getInstance().getFoliaLib().getScheduler().runAtLocationTimer(origin, task -> {
                double progress = (double) tick[0] / ticks;
                fillShape(originFinal.getWorld(), originFinal, lookDir, m, progress);
                if (tick[0]++ >= ticks) {
                    task.cancel();
                }
            }, 1L, 1L);
        }

        /** Renders the particle volume up to {@code progress} (0..1) of the shape's full extent. */
        private static void fillShape(World world, Location origin, Vector lookDir, ShapeWaveMechanic m,
            double progress) {
            if (world == null) {
                return;
            }
            switch (m.shape()) {
                case SHAPE_CYLINDER -> fillCylinder(world, origin, lookDir, m, m.range(), m.radius(), progress);
                case SHAPE_WEDGE -> fillWedge(world, origin, lookDir, m, progress);
                case SHAPE_NOVA -> fillNova(world, origin, m, progress);
                case SHAPE_FAN -> fillFan(world, origin, lookDir, m, progress);
                case SHAPE_LINE -> fillLine(world, origin, lookDir, m, progress);
                default -> fillCone(world, origin, lookDir, m, lookDir, progress);
            }
        }

        private static void fillCone(World world, Location origin, Vector lookDir, ShapeWaveMechanic m,
            Vector rayDir, double progress) {
            double maxD = m.range() * progress;
            double tan = Math.tan(Math.toRadians(m.angle()));
            Vector[] basis = perpBasis(rayDir);
            for (double d = 0.5; d <= maxD; d += 0.75) {
                double ringR = d * tan;
                Location center = origin.clone().add(rayDir.clone().multiply(d));
                spawnFillParticles(world, center, m.fillParticles());
                int ringPts = Math.min(16, Math.max(1, (int) (ringR * m.particleDensity())));
                for (int i = 0; i < ringPts; i++) {
                    double a = 2 * Math.PI * i / ringPts;
                    Vector off = basis[0].clone().multiply(Math.cos(a) * ringR)
                        .add(basis[1].clone().multiply(Math.sin(a) * ringR));
                    spawnFillParticles(world, center.clone().add(off), m.fillParticles());
                }
            }
        }

        private static void fillCylinder(World world, Location origin, Vector lookDir, ShapeWaveMechanic m,
            double range, double radius, double progress) {
            double maxLen = range * progress;
            Vector[] basis = perpBasis(lookDir);
            for (double d = 0.0; d <= maxLen; d += 0.75) {
                Location center = origin.clone().add(lookDir.clone().multiply(d));
                spawnFillParticles(world, center, m.fillParticles());
                int ringPts = Math.min(16, Math.max(1, (int) (radius * m.particleDensity() * 2)));
                for (int i = 0; i < ringPts; i++) {
                    double a = 2 * Math.PI * i / ringPts;
                    Vector off = basis[0].clone().multiply(Math.cos(a) * radius)
                        .add(basis[1].clone().multiply(Math.sin(a) * radius));
                    spawnFillParticles(world, center.clone().add(off), m.fillParticles());
                }
            }
        }

        private static void fillWedge(World world, Location origin, Vector lookDir, ShapeWaveMechanic m,
            double progress) {
            double maxD = m.range() * progress;
            double angleRad = Math.toRadians(m.angle());
            Vector lookFlat = lookDir.clone();
            lookFlat.setY(0);
            if (lookFlat.lengthSquared() < 1.0e-9) {
                return;
            }
            lookFlat.normalize();
            int arcSteps = Math.max(2, (int) (Math.toDegrees(angleRad) / 6) * 2);
            for (double d = 0.5; d <= maxD; d += 0.75) {
                for (int i = 0; i <= arcSteps; i++) {
                    double a = -angleRad + 2 * angleRad * i / arcSteps;
                    Vector dir = rotateAroundY(lookFlat, a);
                    Location base = origin.clone().add(dir.multiply(d));
                    for (double y = -m.height(); y <= m.height(); y += Math.max(0.4, m.height())) {
                        spawnFillParticles(world, base.clone().add(0, y, 0), m.fillParticles());
                    }
                }
            }
        }

        private static void fillNova(World world, Location origin, ShapeWaveMechanic m, double progress) {
            double r = m.radius() * progress;
            if (r < m.minRadius()) {
                return;
            }
            int points = Math.min(220, Math.max(24, (int) (r * r * m.particleDensity())));
            double goldenAngle = Math.PI * (3 - Math.sqrt(5));
            for (int i = 0; i < points; i++) {
                double y = 1 - (i / (double) (points - 1)) * 2; // 1 .. -1
                double radiusAtY = Math.sqrt(Math.max(0, 1 - y * y));
                double theta = goldenAngle * i;
                double x = Math.cos(theta) * radiusAtY;
                double z = Math.sin(theta) * radiusAtY;
                spawnFillParticles(world, origin.clone().add(x * r, y * r, z * r), m.fillParticles());
            }
        }

        private static void fillFan(World world, Location origin, Vector lookDir, ShapeWaveMechanic m,
            double progress) {
            int rays = Math.max(1, m.rays());
            for (int i = 0; i < rays; i++) {
                double offsetDeg = rays == 1 ? 0 : -m.arcDegrees() / 2 + m.arcDegrees() * i / (rays - 1);
                Vector rayDir = rotateAroundY(lookDir, Math.toRadians(offsetDeg));
                fillCone(world, origin, lookDir, m, rayDir, progress);
            }
        }

        private static void fillLine(World world, Location origin, Vector lookDir, ShapeWaveMechanic m,
            double progress) {
            double maxLen = m.range() * progress;
            for (double d = 0.0; d <= maxLen; d += 0.4) {
                spawnFillParticles(world, origin.clone().add(lookDir.clone().multiply(d)), m.fillParticles());
            }
        }

        private static void spawnFillParticles(World world, Location point, List<TrailEntry> fills) {
            for (TrailEntry entry : fills) {
                double off = entry.offset();
                world.spawnParticle(entry.particle(), point, entry.count(), off, off, off, 0.0);
            }
        }

        // --- Geometry helpers ------------------------------------------------

        private static Vector rotateAroundY(Vector v, double theta) {
            double cos = Math.cos(theta);
            double sin = Math.sin(theta);
            double x = v.getX() * cos - v.getZ() * sin;
            double z = v.getX() * sin + v.getZ() * cos;
            return new Vector(x, v.getY(), z);
        }

        /** Two orthonormal vectors spanning the plane perpendicular to {@code dir}. */
        private static Vector[] perpBasis(Vector dir) {
            Vector normalized = dir.clone().normalize();
            Vector up = Math.abs(normalized.getY()) > 0.99 ? new Vector(1, 0, 0) : new Vector(0, 1, 0);
            Vector right = normalized.clone().crossProduct(up).normalize();
            Vector realUp = right.clone().crossProduct(normalized).normalize();
            return new Vector[]{right, realUp};
        }

        // --- Shared helpers --------------------------------------------------

        private static void heal(LivingEntity entity, double amount) {
            double max = 20.0;
            AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null && maxHealth.getValue() > 0) {
                max = maxHealth.getValue();
            }
            entity.setHealth(Math.min(entity.getHealth() + amount, max));
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

        private static boolean conditionsMet(Player player, ShapeConditions c) {
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
        private static ShapeWaveMechanic resolve(@Nullable ItemStack item) {
            String id = NexoItems.idFromItem(item);
            if (id == null) {
                return null;
            }
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(id);
            if (mechanics == null) {
                return null;
            }
            return mechanics.getShapeWaveMechanic();
        }
    }
}
