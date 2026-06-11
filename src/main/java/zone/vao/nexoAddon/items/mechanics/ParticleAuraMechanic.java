package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Renders a continuous animated particle aura around the player while the configured item is held in a given slot.
 * Built from one or more independent animated {@link AuraLayer}s, driven by a single per-player repeating task. One
 * config block ({@code Mechanics.particle_aura}).
 */
public record ParticleAuraMechanic(String slot, int intervalTicks, AuraConditions conditions,
                                   List<AuraLayer> layers) {

    public record AuraLayer(String shape, Particle particle, double radius, int count, double rotationSpeed,
                            double yOffset, double height, int orbCount, boolean onlyOnSprint, boolean onlyOnSneak,
                            boolean onlyOnDamage, double countSprintMultiplier,
                            Color dustColor, Color dustColorTo, float dustSize,
                            double topRadius, double turns, boolean clockwise,
                            double scatter, int scatterCount) {
    }

    public record AuraConditions(boolean requireSneaking, boolean requireSprinting, Set<String> worlds) {
    }

    /**
     * Mutable per-player animation state. {@code phase} is shared; each layer scales it by its own speed.
     */
    private static final class AuraState {
        double phase;
        final double baseStep;

        AuraState(double baseStep) {
            this.baseStep = baseStep;
        }
    }

    /**
     * Resolved active item for a player: its Nexo id plus the mechanic instance.
     */
    private record Active(String id, ParticleAuraMechanic mechanic) {
    }

    public static class ParticleAuraMechanicListener implements Listener {

        private static final Map<UUID, BukkitTask> activeTasks = new ConcurrentHashMap<>();
        private static final Map<UUID, Active> activeData = new ConcurrentHashMap<>();
        private static final Map<UUID, AuraState> states = new ConcurrentHashMap<>();

        private static final int MAX_POINTS = 200;

        // --- Events ----------------------------------------------------------

        @EventHandler
        public void onItemHeld(PlayerItemHeldEvent event) {
            scheduleRefresh(event.getPlayer());
        }

        @EventHandler
        public void onSwapHands(PlayerSwapHandItemsEvent event) {
            scheduleRefresh(event.getPlayer());
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (event.getWhoClicked() instanceof Player player) {
                scheduleRefresh(player);
            }
        }

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            scheduleRefresh(event.getPlayer());
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            deactivateFor(event.getPlayer());
        }

        @EventHandler
        public void onDamage(EntityDamageEvent event) {
            if (!(event.getEntity() instanceof Player player)) {
                return;
            }
            Active active = activeData.get(player.getUniqueId());
            if (active == null) {
                return;
            }
            ParticleAuraMechanic mechanic = active.mechanic();
            if (!globalConditionsMet(player, mechanic.conditions())) {
                return;
            }
            AuraState state = states.get(player.getUniqueId());
            Location center = player.getLocation();
            boolean sprint = player.isSprinting();
            for (AuraLayer layer : mechanic.layers()) {
                if (!layer.onlyOnDamage()) {
                    continue;
                }
                renderLayer(center.getWorld(), center, layer, layerPhase(state, layer),
                    effectiveCount(layer, sprint));
            }
        }

        // --- Lifecycle -------------------------------------------------------

        /**
         * Re-evaluate one tick later, so inventory/slot changes have settled.
         */
        private void scheduleRefresh(Player player) {
            Bukkit.getScheduler().runTaskLater(NexoAddon.getInstance(), () -> refresh(player), 1L);
        }

        private void refresh(Player player) {
            if (!player.isOnline()) {
                return;
            }
            Active found = findActive(player);
            Active current = activeData.get(player.getUniqueId());

            if (found == null) {
                if (current != null) {
                    deactivateFor(player);
                }
                return;
            }
            if (current == null || !current.id().equals(found.id())) {
                if (current != null) {
                    deactivateFor(player);
                }
                activateFor(player, found);
            }
        }

        private void activateFor(Player player, Active active) {
            UUID id = player.getUniqueId();
            cancelTask(id);
            activeData.put(id, active);

            double base = active.mechanic().layers().stream()
                .map(l -> Math.abs(l.rotationSpeed()))
                .filter(s -> s > 1.0e-9)
                .min(Double::compareTo)
                .orElse(0.0);
            states.put(id, new AuraState(base));

            long period = Math.max(1, active.mechanic().intervalTicks());
            BukkitTask task = Bukkit.getScheduler()
                .runTaskTimer(NexoAddon.getInstance(), () -> tick(player, active), 1L, period);
            activeTasks.put(id, task);
        }

        private void deactivateFor(Player player) {
            UUID id = player.getUniqueId();
            cancelTask(id);
            activeData.remove(id);
            states.remove(id);
        }

        private void cancelTask(UUID id) {
            BukkitTask task = activeTasks.remove(id);
            if (task != null) {
                task.cancel();
            }
        }

        private void tick(Player player, Active active) {
            if (!player.isOnline()) {
                deactivateFor(player);
                return;
            }
            // Item must still be in its configured slot (and be the same item).
            Active now = findActive(player);
            if (now == null || !now.id().equals(active.id())) {
                deactivateFor(player);
                return;
            }

            ParticleAuraMechanic mechanic = active.mechanic();
            if (!globalConditionsMet(player, mechanic.conditions())) {
                return; // paused — keep the task alive, just render nothing this tick
            }

            AuraState state = states.get(player.getUniqueId());
            if (state == null) {
                return;
            }
            state.phase += state.baseStep;

            Location center = player.getLocation();
            World world = center.getWorld();
            boolean sprint = player.isSprinting();
            boolean sneak = player.isSneaking();

            for (AuraLayer layer : mechanic.layers()) {
                if (layer.onlyOnDamage()) {
                    continue; // damage-driven layers fire from the EntityDamageEvent instead
                }
                if (layer.onlyOnSprint() && !sprint) {
                    continue;
                }
                if (layer.onlyOnSneak() && !sneak) {
                    continue;
                }
                renderLayer(world, center, layer, layerPhase(state, layer), effectiveCount(layer, sprint));
            }
        }

        // --- Slot resolution (mirrors PassiveEffectMechanic) -----------------

        private static Active findActive(Player player) {
            PlayerInventory inv = player.getInventory();
            Active a;
            if ((a = matchSlot(inv.getItemInMainHand(), "mainhand")) != null) {
                return a;
            }
            if ((a = matchSlot(inv.getItemInOffHand(), "offhand")) != null) {
                return a;
            }
            if ((a = matchSlot(inv.getHelmet(), "armor_head")) != null) {
                return a;
            }
            if ((a = matchSlot(inv.getChestplate(), "armor_chest")) != null) {
                return a;
            }
            if ((a = matchSlot(inv.getLeggings(), "armor_legs")) != null) {
                return a;
            }
            if ((a = matchSlot(inv.getBoots(), "armor_feet")) != null) {
                return a;
            }
            return null;
        }

        private static Active matchSlot(ItemStack item, String physicalSlot) {
            String id = NexoItems.idFromItem(item);
            if (id == null) {
                return null;
            }
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(id);
            if (mechanics == null || mechanics.getParticleAuraMechanic() == null) {
                return null;
            }
            ParticleAuraMechanic mechanic = mechanics.getParticleAuraMechanic();
            if (slotMatches(mechanic.slot(), physicalSlot)) {
                return new Active(id, mechanic);
            }
            return null;
        }

        private static boolean slotMatches(String configSlot, String physicalSlot) {
            // Strip underscores so OFF_HAND/off_hand/offhand all match "offhand", etc.
            String c = configSlot.toLowerCase().replace("_", "");
            String p = physicalSlot.replace("_", "");
            return switch (c) {
                case "any" -> p.equals("mainhand") || p.equals("offhand");
                default -> c.equals(p);
            };
        }

        // --- Conditions / helpers -------------------------------------------

        private static boolean globalConditionsMet(Player player, AuraConditions c) {
            if (c.requireSneaking() && !player.isSneaking()) {
                return false;
            }
            if (c.requireSprinting() && !player.isSprinting()) {
                return false;
            }
            return c.worlds().isEmpty() || c.worlds().contains(player.getWorld().getName());
        }

        private static int effectiveCount(AuraLayer layer, boolean sprint) {
            double mult = (sprint && layer.countSprintMultiplier() > 1.0) ? layer.countSprintMultiplier() : 1.0;
            return Math.max(1, (int) Math.round(layer.count() * mult));
        }

        /**
         * Scale the shared phase by this layer's rotation speed (and direction).
         */
        private static double layerPhase(AuraState state, AuraLayer layer) {
            if (state == null || state.baseStep <= 0) {
                return 0.0;
            }
            return state.phase * (layer.rotationSpeed() / state.baseStep);
        }

        private static int clampPoints(int value) {
            return Math.max(1, Math.min(value, MAX_POINTS));
        }

        // --- Shape rendering -------------------------------------------------

        private static void renderLayer(World world, Location center, AuraLayer layer, double phase, int effCount) {
            switch (layer.shape()) {
                case "ring" -> renderRing(world, center, layer, phase, effCount);
                case "helix" -> renderHelix(world, center, layer, phase, effCount);
                case "sphere" -> renderSphere(world, center, layer, phase, effCount);
                case "random" -> renderRandom(world, center, layer, effCount);
                case "tornado" -> renderTornado(world, center, layer, phase, effCount);
                case "orbit" -> renderOrbit(world, center, layer, phase, effCount);
                case "vortex" -> renderVortex(world, center, layer, phase, effCount);
                case "wings" -> renderWings(world, center, layer, phase, effCount);
                default -> { /* unknown shape — render nothing */ }
            }
        }

        // --- Particle spawn helpers (handle dust/transition particles that require extra data) ---

        @SuppressWarnings("unchecked")
        private static <T> T dustOptions(AuraLayer layer) {
            Class<?> type = layer.particle().getDataType();
            Color from = layer.dustColor() != null ? layer.dustColor() : Color.RED;
            float size = layer.dustSize() > 0 ? layer.dustSize() : 1.0f;
            if (type == Particle.DustOptions.class) {
                return (T) new Particle.DustOptions(from, size);
            }
            if (type == Particle.DustTransition.class) {
                Color to = layer.dustColorTo() != null ? layer.dustColorTo() : Color.fromRGB(0, 0, 0);
                return (T) new Particle.DustTransition(from, to, size);
            }
            return null;
        }

        private static void spawnOne(World world, Location loc, AuraLayer layer) {
            Object data = dustOptions(layer);
            int sc = layer.scatterCount() > 0 ? layer.scatterCount() : 1;
            double off = Math.max(0.0, layer.scatter());
            world.spawnParticle(layer.particle(), loc, sc, off, off, off, 0.0, data);
        }

        private static void spawnCluster(World world, Location loc, AuraLayer layer, int count) {
            Object data = dustOptions(layer);
            world.spawnParticle(layer.particle(), loc, count, 0.05, 0.05, 0.05, 0.0, data);
        }

        // --- Shape rendering -------------------------------------------------

        private static void renderRing(World world, Location center, AuraLayer layer, double phase, int effCount) {
            int points = clampPoints(effCount);
            for (int i = 0; i < points; i++) {
                double angle = phase + (2 * Math.PI * i / points);
                double x = Math.cos(angle) * layer.radius();
                double z = Math.sin(angle) * layer.radius();
                spawnOne(world, center.clone().add(x, layer.yOffset() + 1.0, z), layer);
            }
        }

        private static void renderHelix(World world, Location center, AuraLayer layer, double phase, int effCount) {
            int steps = clampPoints(effCount * 8);
            for (int i = 0; i < steps; i++) {
                double t = i / (double) steps;
                double y = t * layer.height();
                double angle = phase + t * 4 * Math.PI;
                for (double arm = 0; arm < 2 * Math.PI; arm += Math.PI) {
                    double x = Math.cos(angle + arm) * layer.radius();
                    double z = Math.sin(angle + arm) * layer.radius();
                    spawnOne(world, center.clone().add(x, y, z), layer);
                }
            }
        }

        private static void renderSphere(World world, Location center, AuraLayer layer, double phase, int effCount) {
            int n = clampPoints(effCount * 8);
            if (n < 2) {
                n = 2;
            }
            double golden = Math.PI * (3 - Math.sqrt(5));
            Location body = center.clone().add(0, 1, 0);
            for (int i = 0; i < n; i++) {
                double y = 1 - (i / (double) (n - 1)) * 2; // 1 .. -1
                double r = Math.sqrt(1 - y * y);
                double theta = golden * i + phase;
                double x = Math.cos(theta) * r;
                double z = Math.sin(theta) * r;
                spawnOne(world, body.clone().add(x * layer.radius(), y * layer.radius(), z * layer.radius()), layer);
            }
        }

        private static void renderRandom(World world, Location center, AuraLayer layer, int effCount) {
            int n = clampPoints(effCount);
            Location body = center.clone().add(0, 1, 0);
            for (int i = 0; i < n; i++) {
                double u = Math.random() * 2 - 1;
                double t = Math.random() * 2 * Math.PI;
                double r = Math.sqrt(1 - u * u);
                double dist = Math.random() * layer.radius();
                Vector v = new Vector(r * Math.cos(t), u, r * Math.sin(t)).multiply(dist);
                spawnOne(world, body.clone().add(v), layer);
            }
        }

        private static void renderTornado(World world, Location center, AuraLayer layer, double phase, int effCount) {
            int steps = clampPoints(effCount * 8);
            double height = layer.height();
            // Vertical scroll: the whole vortex rises with phase and wraps at `height`.
            double scroll = height <= 0 ? 0 : ((phase * 0.5) % 1.0 + 1.0) % 1.0 * height;
            for (int i = 0; i < steps; i++) {
                double t = i / (double) steps;
                double y = height <= 0 ? 0 : (t * height + scroll) % height;
                double angle = phase + t * 4 * Math.PI;
                double radiusAtY = layer.radius() * (0.3 + 0.7 * (height <= 0 ? 1 : y / height));
                double x = Math.cos(angle) * radiusAtY;
                double z = Math.sin(angle) * radiusAtY;
                spawnOne(world, center.clone().add(x, y, z), layer);
            }
        }

        private static void renderOrbit(World world, Location center, AuraLayer layer, double phase, int effCount) {
            int orbs = Math.max(1, layer.orbCount());
            for (int k = 0; k < orbs; k++) {
                double angle = phase + (2 * Math.PI * k / orbs);
                double x = Math.cos(angle) * layer.radius();
                double z = Math.sin(angle) * layer.radius();
                spawnCluster(world, center.clone().add(x, layer.yOffset() + 1.0, z), layer, effCount);
            }
        }

        /**
         * Helix-Vortex um den Spieler. Radius interpoliert linear von {@code radius} (Basis) zu {@code top_radius}
         * (Spitze):
         * <ul>
         *   <li>{@code top_radius < radius}  → Trichter, oben schmaler</li>
         *   <li>{@code top_radius > radius}  → unten schmal, oben breit</li>
         *   <li>{@code top_radius == radius} → gerader Zylinder</li>
         *   <li>{@code top_radius} nicht gesetzt (−1) → Zylinder mit {@code radius}</li>
         * </ul>
         * {@code orb_count} = Anzahl Helices, {@code turns} = volle Umdrehungen Basis→Spitze,
         * {@code clockwise} = Wicklungsrichtung umkehren.
         */
        private static void renderVortex(World world, Location center, AuraLayer layer, double phase, int effCount) {
            int arms = layer.orbCount() > 0 ? layer.orbCount() : 2;
            int steps = clampPoints(effCount * 6);
            double height = layer.height() > 0 ? layer.height() : 2.0;
            double yBase = layer.yOffset();

            double rBottom = layer.radius();
            double rTop = layer.topRadius() >= 0 ? layer.topRadius() : rBottom;
            double turns = layer.turns() > 0 ? layer.turns() : 2.0;
            double wind = layer.clockwise() ? -1.0 : 1.0;

            for (int arm = 0; arm < arms; arm++) {
                double armOffset = 2 * Math.PI * arm / arms;
                for (int i = 0; i < steps; i++) {
                    double t = i / (double) steps;
                    double r = rBottom + (rTop - rBottom) * t;
                    double angle = phase + armOffset + wind * t * turns * 2 * Math.PI;
                    double y = yBase + t * height;
                    spawnOne(world, center.clone().add(Math.cos(angle) * r, y, Math.sin(angle) * r), layer);
                }
            }
        }

        /**
         * Two symmetrical wings rendered relative to the player's facing direction. Wings flap up and down driven by
         * {@code phase}. {@code radius} controls wingspan, {@code height} controls vertical arc, {@code y_offset}
         * shifts the attachment point.
         */
        private static void renderWings(World world, Location center, AuraLayer layer, double phase, int effCount) {
            double yawRad = Math.toRadians(center.getYaw());
            // Right unit vector (perpendicular, pointing player's right)
            double rx = Math.cos(yawRad);
            double rz = Math.sin(yawRad);
            // Back unit vector
            double bx = Math.sin(yawRad);
            double bz = -Math.cos(yawRad);

            int points = clampPoints(Math.max(6, effCount * 4));
            double span = layer.radius();
            double wingHeight = layer.height() > 0 ? layer.height() : 1.2;
            double yBase = layer.yOffset() + 1.3; // shoulder height

            // Flap: oscillates between raised (+lift) and lowered (-lift)
            double flapLift = Math.sin(phase * 0.6) * 0.35;

            for (int side = -1; side <= 1; side += 2) { // -1 = left wing, +1 = right wing
                for (int i = 0; i <= points; i++) {
                    double t = i / (double) points; // 0 = root (body), 1 = tip
                    // Lateral extent grows linearly to full span
                    double lateral = t * span;
                    // Vertical arc: peaks at mid-wing, tapers at root and tip
                    double arc = Math.sin(t * Math.PI) * wingHeight * (1.0 + flapLift);
                    // Sweep back: tip trails slightly behind shoulder
                    double sweep = (1.0 - Math.cos(t * Math.PI)) * span * 0.25;

                    double px = side * lateral * rx + sweep * bx;
                    double pz = side * lateral * rz + sweep * bz;
                    double py = yBase + arc;

                    spawnOne(world, center.clone().add(px, py, pz), layer);
                }
            }
        }
    }
}
