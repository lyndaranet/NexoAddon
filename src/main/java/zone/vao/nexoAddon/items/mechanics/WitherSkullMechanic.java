package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feuert beim Rechtsklick einen einzelnen Wither-Schädel in Blickrichtung ab.
 * Cooldown ist an die Spieler-UUID gebunden (nicht an Item/Slot), damit er nicht durch
 * mehrere Item-Kopien oder schnelles Item-Wechseln umgangen werden kann.
 */
public record WitherSkullMechanic(String trigger, boolean charged, int cooldownSeconds, double velocity,
                                  @Nullable Sound sound) {

    public static final String TRIGGER_RIGHT_CLICK = "right_click";

    public static class WitherSkullMechanicListener implements Listener {
        private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

        @EventHandler
        public void onInteract(PlayerInteractEvent event) {
            if (event.getHand() != EquipmentSlot.HAND) return;
            Action action = event.getAction();
            if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

            Player player = event.getPlayer();
            WitherSkullMechanic mechanic = resolve(event.getItem());
            if (mechanic == null || !TRIGGER_RIGHT_CLICK.equals(mechanic.trigger())) return;

            // Vanilla-Verhalten des Basis-Items unterbinden, unabhängig davon, welches
            // Vanilla-Item Nexo für die Optik nutzt
            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);

            UUID id = player.getUniqueId();
            long now = System.currentTimeMillis();
            long remaining = remainingCooldown(id, mechanic.cooldownSeconds(), now);
            if (remaining > 0) {
                actionBar(player, "<red>Noch <bold>" + ((remaining + 999) / 1000) + "s</bold> Cooldown!");
                return;
            }
            if (mechanic.cooldownSeconds() > 0) {
                cooldowns.put(id, now);
            }

            fireSkull(player, mechanic);
            if (mechanic.sound() != null) {
                player.getWorld().playSound(player.getLocation(), mechanic.sound(), 1.0f, 1.0f);
            }
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            cooldowns.remove(event.getPlayer().getUniqueId());
        }

        private void fireSkull(Player player, WitherSkullMechanic mechanic) {
            Location eyeLoc = player.getEyeLocation();
            Vector direction = eyeLoc.getDirection().normalize();
            // Leichter Versatz nach vorn, damit der Schädel nicht in der eigenen
            // Hitbox spawnt und dort sofort explodiert (Selbstschaden-Bug)
            Location spawnLoc = eyeLoc.clone().add(direction.clone().multiply(1.0));

            WitherSkull skull = player.getWorld().spawn(spawnLoc, WitherSkull.class);
            skull.setShooter(player);
            skull.setDirection(direction);
            skull.setVelocity(direction.multiply(mechanic.velocity()));
            skull.setCharged(mechanic.charged());
        }

        private static long remainingCooldown(UUID id, int cooldownSeconds, long now) {
            if (cooldownSeconds <= 0) return 0;
            Long last = cooldowns.get(id);
            if (last == null) return 0;
            long elapsed = now - last;
            long total = cooldownSeconds * 1000L;
            return elapsed >= total ? 0 : total - elapsed;
        }

        private static void actionBar(Player player, String miniMessage) {
            player.sendActionBar(MiniMessage.miniMessage().deserialize(miniMessage));
        }

        @Nullable
        private static WitherSkullMechanic resolve(@Nullable ItemStack item) {
            String id = NexoItems.idFromItem(item);
            if (id == null) return null;
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(id);
            if (mechanics == null) return null;
            return mechanics.getWitherSkullMechanic();
        }
    }
}
