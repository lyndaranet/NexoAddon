package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.*;

/**
 * SpiderMan mechanic:
 * - Wall Climbing: Sneak against a solid wall while holding item → stick and climb slowly upward.
 * - Web Shot: Right-click to shoot a web to target block; launched toward it with an arc boost.
 */
public record SpiderMan(boolean enabled,
                        boolean wallClimbEnabled, double climbSpeed, String checkSlot,
                        boolean webShotEnabled, int webMaxDistance, double webPullSpeed,
                        double arcBoost, int webCooldown,
                        String particleType, int particleAmount,
                        String soundType, float soundVolume, float soundPitch,
                        String cooldownMessage, int durabilityCost) {

    private static final BlockFace[] HORIZONTAL = {
        BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    public static class SpiderManListener implements Listener {
        private static final Map<UUID, Long> webCooldowns = new HashMap<>();
        private static final Map<UUID, BukkitRunnable> activeSwings = new HashMap<>();
        // Tracks players currently wall-climbing (flight was granted by us)
        private static final Set<UUID> climbingPlayers = new HashSet<>();
        private static final MiniMessage miniMessage = MiniMessage.miniMessage();

        // Runs every tick to handle wall climbing
        public static void startClimbTask() {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : NexoAddon.getInstance().getServer().getOnlinePlayers()) {
                        if (player.isDead()) continue;
                        if (player.getGameMode() == GameMode.CREATIVE
                            || player.getGameMode() == GameMode.SPECTATOR) continue;

                        SpiderMan mechanic = findMechanic(player);
                        if (mechanic == null || !mechanic.enabled() || !mechanic.wallClimbEnabled()) {
                            clearClimbing(player);
                            continue;
                        }

                        if (player.isSneaking() && isAgainstWall(player)) {
                            if (!climbingPlayers.contains(player.getUniqueId())) {
                                climbingPlayers.add(player.getUniqueId());
                                player.setAllowFlight(true);
                            }
                            // Slow upward drift; setFallDistance prevents damage on detach
                            Vector vel = player.getVelocity();
                            vel.setY(mechanic.climbSpeed());
                            player.setVelocity(vel);
                            player.setFallDistance(0);
                        } else {
                            clearClimbing(player);
                        }
                    }
                }
            }.runTaskTimer(NexoAddon.getInstance(), 0L, 1L);
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            clearClimbing(player);
            cancelSwing(player);
        }

        // Prevent double-jump activation during swing or wall climb
        @EventHandler
        public void onToggleFlight(PlayerToggleFlightEvent event) {
            Player player = event.getPlayer();
            UUID uid = player.getUniqueId();
            if (climbingPlayers.contains(uid) || activeSwings.containsKey(uid)) {
                event.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (event.getHand() != EquipmentSlot.HAND) return;
            if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            String nexoItemId = NexoItems.idFromItem(item);
            if (nexoItemId == null) return;

            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(nexoItemId);
            if (mechanics == null || mechanics.getSpiderMan() == null) return;

            SpiderMan spiderMan = mechanics.getSpiderMan();
            if (!spiderMan.enabled() || !spiderMan.webShotEnabled()) return;

            UUID uid = player.getUniqueId();

            // Toggle off active swing
            if (activeSwings.containsKey(uid)) {
                cancelSwing(player);
                return;
            }

            // Check cooldown
            long now = System.currentTimeMillis();
            if (webCooldowns.containsKey(uid)) {
                long left = spiderMan.webCooldown() - (now - webCooldowns.get(uid)) / 1000;
                if (left > 0) {
                    if (spiderMan.cooldownMessage() != null && !spiderMan.cooldownMessage().isEmpty()) {
                        player.sendMessage(miniMessage.deserialize(
                            spiderMan.cooldownMessage().replace("{time}", String.valueOf(left))));
                    }
                    return;
                }
            }

            // Raycast for target
            RayTraceResult result = player.getWorld().rayTraceBlocks(
                player.getEyeLocation(), player.getLocation().getDirection(),
                spiderMan.webMaxDistance(), FluidCollisionMode.NEVER, true);

            if (result == null || result.getHitBlock() == null) return;

            Location target = result.getHitBlock().getLocation().add(0.5, 0.5, 0.5);

            event.setCancelled(true);
            webCooldowns.put(uid, now);

            playSound(player, spiderMan);
            drawWebLine(player, spiderMan, target);

            if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                player.setAllowFlight(true);
            }

            if (spiderMan.durabilityCost() > 0 && item.getType().getMaxDurability() > 0) {
                applyDurability(item, spiderMan.durabilityCost(), player);
            }

            startSwing(player, target, spiderMan);
        }

        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            Player player = event.getPlayer();
            UUID uid = player.getUniqueId();
            if (activeSwings.containsKey(uid) && player.isOnGround()) {
                cancelSwing(player);
            }
        }

        private void startSwing(Player player, Location target, SpiderMan spiderMan) {
            UUID uid = player.getUniqueId();
            BukkitRunnable task = new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (!player.isOnline() || ticks > 80) {
                        cancelSwing(player);
                        return;
                    }

                    Location playerLoc = player.getLocation().add(0, 1, 0);
                    Vector direction = target.toVector().subtract(playerLoc.toVector());
                    double distance = direction.length();

                    if (distance < 1.5) {
                        cancelSwing(player);
                        return;
                    }

                    // Arc: upward boost on the first ticks, then straighten out
                    direction.normalize().multiply(spiderMan.webPullSpeed());
                    if (ticks < 5) {
                        direction.setY(direction.getY() + spiderMan.arcBoost());
                    }
                    player.setVelocity(direction);
                    player.setFallDistance(0);

                    if (spiderMan.particleType() != null && !spiderMan.particleType().isEmpty()) {
                        try {
                            Particle particle = Particle.valueOf(spiderMan.particleType().toUpperCase());
                            player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0),
                                spiderMan.particleAmount(), 0.15, 0.15, 0.15, 0);
                        } catch (IllegalArgumentException ignored) {}
                    }

                    ticks++;
                }
            };
            task.runTaskTimer(NexoAddon.getInstance(), 0L, 1L);
            activeSwings.put(uid, task);
        }

        private static void cancelSwing(Player player) {
            UUID uid = player.getUniqueId();
            BukkitRunnable task = activeSwings.remove(uid);
            if (task != null && !task.isCancelled()) task.cancel();
            player.setFallDistance(0);
            if (!climbingPlayers.contains(uid)
                && player.getGameMode() != GameMode.CREATIVE
                && player.getGameMode() != GameMode.SPECTATOR) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }

        private static void clearClimbing(Player player) {
            UUID uid = player.getUniqueId();
            if (climbingPlayers.remove(uid)) {
                if (!activeSwings.containsKey(uid)
                    && player.getGameMode() != GameMode.CREATIVE
                    && player.getGameMode() != GameMode.SPECTATOR) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }
            }
        }

        // Check if there is a solid block horizontally adjacent to the player
        private static boolean isAgainstWall(Player player) {
            Location loc = player.getLocation();
            for (BlockFace face : HORIZONTAL) {
                Block adjacent = loc.getBlock().getRelative(face);
                Block adjacent2 = loc.clone().add(0, 1, 0).getBlock().getRelative(face);
                if (adjacent.getType().isSolid() || adjacent2.getType().isSolid()) {
                    return true;
                }
            }
            return false;
        }

        // Find the SpiderMan mechanic for the item in the relevant slot
        private static SpiderMan findMechanic(Player player) {
            ItemStack item = player.getInventory().getItemInMainHand();
            String id = NexoItems.idFromItem(item);
            if (id != null) {
                Mechanics m = NexoAddon.getInstance().getMechanics().get(id);
                if (m != null && m.getSpiderMan() != null) return m.getSpiderMan();
            }

            // Also check off-hand
            ItemStack offHand = player.getInventory().getItemInOffHand();
            String offId = NexoItems.idFromItem(offHand);
            if (offId != null) {
                Mechanics m = NexoAddon.getInstance().getMechanics().get(offId);
                if (m != null && m.getSpiderMan() != null) return m.getSpiderMan();
            }

            // Check armor slots for wearable spiderman suit
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                String armorId = NexoItems.idFromItem(armor);
                if (armorId == null) continue;
                Mechanics m = NexoAddon.getInstance().getMechanics().get(armorId);
                if (m != null && m.getSpiderMan() != null) return m.getSpiderMan();
            }
            return null;
        }

        private void drawWebLine(Player player, SpiderMan spiderMan, Location target) {
            if (spiderMan.particleType() == null || spiderMan.particleType().isEmpty()) return;
            try {
                Particle particle = Particle.valueOf(spiderMan.particleType().toUpperCase());
                Location from = player.getEyeLocation();
                Vector step = target.toVector().subtract(from.toVector()).normalize().multiply(0.5);
                double length = from.distance(target);
                for (double d = 0; d < length; d += 0.5) {
                    player.getWorld().spawnParticle(particle, from.clone().add(step.clone().multiply(d / 0.5)), 1, 0, 0, 0, 0);
                }
            } catch (IllegalArgumentException ignored) {}
        }

        private void playSound(Player player, SpiderMan spiderMan) {
            if (spiderMan.soundType() == null || spiderMan.soundType().isEmpty()) return;
            try {
                Sound sound = Sound.valueOf(spiderMan.soundType().toUpperCase());
                player.getWorld().playSound(player.getLocation(), sound, spiderMan.soundVolume(), spiderMan.soundPitch());
            } catch (IllegalArgumentException e) {
                NexoAddon.getInstance().getLogger().warning("Invalid sound for SpiderMan: " + spiderMan.soundType());
            }
        }

        private void applyDurability(ItemStack item, int amount, Player player) {
            if (item.getItemMeta() == null) return;
            var meta = item.getItemMeta();
            if (meta.isUnbreakable()) return;
            if (!(meta instanceof org.bukkit.inventory.meta.Damageable damageable)) return;
            int newDamage = damageable.getDamage() + amount;
            if (newDamage >= item.getType().getMaxDurability()) {
                player.getInventory().setItemInMainHand(null);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            } else {
                damageable.setDamage(newDamage);
                item.setItemMeta(meta);
            }
        }
    }
}
