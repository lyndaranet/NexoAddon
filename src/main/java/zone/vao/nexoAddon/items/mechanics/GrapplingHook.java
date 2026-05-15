package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record GrapplingHook(boolean enabled, int maxDistance, double pullSpeed, int cooldown,
                             String particleType, int particleAmount,
                             String soundType, float soundVolume, float soundPitch,
                             String cooldownMessage, int durabilityCost,
                             String ropeColor, float ropeSize, double ropeSpacing) {

    public static class GrapplingHookListener implements Listener {
        private static final Map<UUID, Long> cooldowns = new HashMap<>();
        private static final Map<UUID, BukkitRunnable> activePulls = new HashMap<>();
        // Stores the fixed hook anchor point per player
        private static final Map<UUID, Location> hookAnchors = new HashMap<>();
        private static final MiniMessage miniMessage = MiniMessage.miniMessage();

        @EventHandler(priority = EventPriority.HIGH)
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (event.getHand() != EquipmentSlot.HAND) return;
            if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            String nexoItemId = NexoItems.idFromItem(item);
            if (nexoItemId == null) return;

            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(nexoItemId);
            if (mechanics == null || mechanics.getGrapplingHook() == null) return;

            GrapplingHook hook = mechanics.getGrapplingHook();
            if (!hook.enabled()) return;

            // Prevent fishing rod animation and bobber spawn regardless of outcome
            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);

            UUID uid = player.getUniqueId();

            // Toggle off if already pulling
            if (activePulls.containsKey(uid)) {
                cancelPull(player);
                return;
            }

            // Check cooldown
            long now = System.currentTimeMillis();
            if (cooldowns.containsKey(uid)) {
                long left = hook.cooldown() - (now - cooldowns.get(uid)) / 1000;
                if (left > 0) {
                    if (hook.cooldownMessage() != null && !hook.cooldownMessage().isEmpty()) {
                        player.sendMessage(miniMessage.deserialize(
                            hook.cooldownMessage().replace("{time}", String.valueOf(left))));
                    }
                    return;
                }
            }

            // Raycast to find target block
            RayTraceResult result = player.getWorld().rayTraceBlocks(
                player.getEyeLocation(), player.getLocation().getDirection(),
                hook.maxDistance(), FluidCollisionMode.NEVER, true);

            if (result == null || result.getHitBlock() == null) return;

            Block hitBlock = result.getHitBlock();
            Location anchor = hitBlock.getLocation().add(0.5, 0.5, 0.5);

            cooldowns.put(uid, now);
            hookAnchors.put(uid, anchor);

            playSound(player, hook);

            // setAllowFlight prevents Vulcan from flagging the aerial movement
            if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                player.setAllowFlight(true);
            }

            if (hook.durabilityCost() > 0 && item.getType().getMaxDurability() > 0) {
                applyDurability(item, hook.durabilityCost(), player);
            }

            startPull(player, anchor, hook);
        }

        // Catch any fishing bobber that still gets spawned (client-side edge cases)
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPlayerFish(PlayerFishEvent event) {
            if (!hookAnchors.containsKey(event.getPlayer().getUniqueId())) return;
            String nexoItemId = NexoItems.idFromItem(event.getPlayer().getInventory().getItemInMainHand());
            if (nexoItemId == null) return;
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(nexoItemId);
            if (mechanics == null || mechanics.getGrapplingHook() == null) return;
            event.setCancelled(true);
        }

        // Prevent double-jump activation while grappling
        @EventHandler
        public void onToggleFlight(PlayerToggleFlightEvent event) {
            if (activePulls.containsKey(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            Player player = event.getPlayer();
            if (!activePulls.containsKey(player.getUniqueId())) return;
            if (player.isOnGround()) cancelPull(player);
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            cancelPull(event.getPlayer());
        }

        private void startPull(Player player, Location anchor, GrapplingHook hook) {
            UUID uid = player.getUniqueId();
            Color ropeColor = parseColor(hook.ropeColor());

            BukkitRunnable task = new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (!player.isOnline() || ticks > 80) {
                        cancelPull(player);
                        return;
                    }

                    Location handLoc = player.getLocation().add(0, 1.1, 0);
                    Vector direction = anchor.toVector().subtract(handLoc.toVector());
                    double distance = direction.length();

                    if (distance < 1.5) {
                        cancelPull(player);
                        return;
                    }

                    // Pull velocity
                    direction.normalize().multiply(hook.pullSpeed());
                    player.setVelocity(direction);
                    player.setFallDistance(0);

                    // Live rope: draw every tick from hand to anchor
                    drawRope(player, handLoc, anchor, ropeColor, hook.ropeSize(), hook.ropeSpacing());

                    // Optional trail particle around the player
                    if (hook.particleType() != null && !hook.particleType().isEmpty()) {
                        try {
                            Particle particle = Particle.valueOf(hook.particleType().toUpperCase());
                            player.getWorld().spawnParticle(particle,
                                player.getLocation().add(0, 1, 0),
                                hook.particleAmount(), 0.1, 0.1, 0.1, 0);
                        } catch (IllegalArgumentException ignored) {}
                    }

                    ticks++;
                }
            };
            task.runTaskTimer(NexoAddon.getInstance(), 0L, 1L);
            activePulls.put(uid, task);
        }

        private static void drawRope(Player player, Location from, Location to,
                                     Color color, float size, double spacing) {
            Particle.DustOptions dust = new Particle.DustOptions(color, size);
            Vector dir = to.toVector().subtract(from.toVector());
            double length = dir.length();
            if (length == 0) return;
            Vector step = dir.clone().normalize().multiply(spacing);
            int steps = (int) (length / spacing);
            for (int i = 0; i <= steps; i++) {
                Location point = from.clone().add(step.clone().multiply(i));
                player.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, dust);
            }
        }

        private static Color parseColor(String raw) {
            if (raw != null && !raw.isEmpty()) {
                String[] parts = raw.split(",");
                if (parts.length == 3) {
                    try {
                        return Color.fromRGB(
                            Integer.parseInt(parts[0].trim()),
                            Integer.parseInt(parts[1].trim()),
                            Integer.parseInt(parts[2].trim()));
                    } catch (NumberFormatException ignored) {}
                }
            }
            // Default: rope brown
            return Color.fromRGB(139, 90, 43);
        }

        static void cancelPull(Player player) {
            UUID uid = player.getUniqueId();
            BukkitRunnable task = activePulls.remove(uid);
            if (task != null && !task.isCancelled()) task.cancel();
            hookAnchors.remove(uid);
            player.setFallDistance(0);
            if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }

        private void playSound(Player player, GrapplingHook hook) {
            if (hook.soundType() == null || hook.soundType().isEmpty()) return;
            try {
                Sound sound = Sound.valueOf(hook.soundType().toUpperCase());
                player.getWorld().playSound(player.getLocation(), sound,
                    hook.soundVolume(), hook.soundPitch());
            } catch (IllegalArgumentException e) {
                NexoAddon.getInstance().getLogger()
                    .warning("Invalid sound for GrapplingHook: " + hook.soundType());
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
