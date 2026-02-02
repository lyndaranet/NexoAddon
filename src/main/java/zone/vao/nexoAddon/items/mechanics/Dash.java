package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record Dash(double power, double verticalBoost, int cooldown, boolean requireSneaking,
                   String particleType, int particleAmount, String soundType, float soundVolume,
                   float soundPitch, String cooldownMessage, int durabilityCost) {

    public static class DashListener implements Listener {
        private static final NamespacedKey COOLDOWN_KEY = new NamespacedKey(NexoAddon.getInstance(), "dash_cooldown");
        private static final Map<UUID, Long> cooldowns = new HashMap<>();
        private static final MiniMessage miniMessage = MiniMessage.miniMessage();

        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (event.getHand() != EquipmentSlot.HAND) {
                return;
            }

            if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }

            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();

            String nexoItemId = NexoItems.idFromItem(item);
            if (nexoItemId == null) {
                return;
            }

            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(nexoItemId);
            if (mechanics == null || mechanics.getDash() == null) {
                return;
            }

            Dash dash = mechanics.getDash();

            // Check if sneaking is required
            if (dash.requireSneaking() && !player.isSneaking()) {
                return;
            }

            // Check cooldown
            UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();

            if (cooldowns.containsKey(playerId)) {
                long lastUse = cooldowns.get(playerId);
                long timePassed = (currentTime - lastUse) / 1000;
                long timeLeft = dash.cooldown() - timePassed;

                if (timeLeft > 0) {
                    if (dash.cooldownMessage() != null && !dash.cooldownMessage().isEmpty()) {
                        String message = dash.cooldownMessage()
                            .replace("{time}", String.valueOf(timeLeft));
                        player.sendMessage(miniMessage.deserialize(message));
                    }
                    return;
                }
            }

            // Cancel event to prevent other interactions
            event.setCancelled(true);

            // Perform dash
            executeDash(player, dash);

            // Apply durability cost if applicable
            if (dash.durabilityCost() > 0 && item.getType().getMaxDurability() > 0) {
                applyDurability(item, dash.durabilityCost(), player);
            }

            // Set cooldown
            cooldowns.put(playerId, currentTime);
        }

        private void executeDash(Player player, Dash dash) {
            // Get player's direction
            Vector direction = player.getLocation().getDirection().normalize();

            // Apply horizontal boost
            Vector velocity = direction.multiply(dash.power());

            // Apply vertical boost
            velocity.setY(velocity.getY() + dash.verticalBoost());

            // Set player velocity
            player.setVelocity(velocity);

            // Play sound
            if (dash.soundType() != null && !dash.soundType().isEmpty()) {
                try {
                    Sound sound = Sound.valueOf(dash.soundType().toUpperCase());
                    player.getWorld().playSound(player.getLocation(), sound,
                        dash.soundVolume(), dash.soundPitch());
                } catch (IllegalArgumentException e) {
                    NexoAddon.getInstance().getLogger().warning(
                        "Invalid sound type for Dash mechanic: " + dash.soundType());
                }
            }

            // Spawn particles
            if (dash.particleType() != null && !dash.particleType().isEmpty()) {
                spawnDashParticles(player, dash);
            }
        }

        private void spawnDashParticles(Player player, Dash dash) {
            try {
                Particle particle = Particle.valueOf(dash.particleType().toUpperCase());
                Location loc = player.getLocation();

                // Spawn particles at player location
                player.getWorld().spawnParticle(particle, loc, dash.particleAmount(),
                    0.5, 0.5, 0.5, 0.1);

                // Spawn particle trail
                new BukkitRunnable() {
                    int ticks = 0;
                    final Location startLoc = loc.clone();

                    @Override
                    public void run() {
                        if (ticks >= 10) { // 0.5 seconds
                            cancel();
                            return;
                        }

                        Location particleLoc = player.getLocation();
                        player.getWorld().spawnParticle(particle, particleLoc,
                            dash.particleAmount() / 2, 0.3, 0.3, 0.3, 0.05);

                        ticks++;
                    }
                }.runTaskTimer(NexoAddon.getInstance(), 0L, 1L);

            } catch (IllegalArgumentException e) {
                NexoAddon.getInstance().getLogger().warning(
                    "Invalid particle type for Dash mechanic: " + dash.particleType());
            }
        }

        private void applyDurability(ItemStack item, int amount, Player player) {
            if (item.getItemMeta() == null) {
                return;
            }

            var meta = item.getItemMeta();

            // Check if item is unbreakable
            if (meta.isUnbreakable()) {
                return;
            }

            // Get current damage
            if (!(meta instanceof org.bukkit.inventory.meta.Damageable damageable)) {
                return;
            }

            int newDamage = damageable.getDamage() + amount;
            int maxDurability = item.getType().getMaxDurability();

            // Check if item should break
            if (newDamage >= maxDurability) {
                player.getInventory().setItemInMainHand(null);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            } else {
                damageable.setDamage(newDamage);
                item.setItemMeta(meta);
            }
        }

        // Clean up old cooldowns periodically
        public static void startCooldownCleanup() {
            new BukkitRunnable() {
                @Override
                public void run() {
                    long currentTime = System.currentTimeMillis();
                    cooldowns.entrySet().removeIf(entry ->
                        (currentTime - entry.getValue()) / 1000 > 3600); // Remove after 1 hour
                }
            }.runTaskTimer(NexoAddon.getInstance(), 0L, 12000L); // Every 10 minutes
        }
    }
}
