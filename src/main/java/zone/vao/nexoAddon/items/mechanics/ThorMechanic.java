package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ruft beim Rechtsklick Blitzschläge an der Stelle hervor, auf die der Spieler blickt.
 * Cooldown ist an die Spieler-UUID gebunden, nicht an Item/Slot. Cooldown wird erst
 * verbraucht, wenn der Raycast tatsächlich einen Block trifft - "ins Leere schauen"
 * kostet nichts, macht aber auch nichts.
 */
public record ThorMechanic(String trigger, int lightningBoltsAmount, double randomLocationVariation,
                           double range, int cooldownSeconds, boolean visualOnly, @Nullable Sound sound) {

    public static final String TRIGGER_RIGHT_CLICK = "right_click";

    public static class ThorMechanicListener implements Listener {
        private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

        @EventHandler
        public void onInteract(PlayerInteractEvent event) {
            if (event.getHand() != EquipmentSlot.HAND) return;
            Action action = event.getAction();
            if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

            Player player = event.getPlayer();
            ThorMechanic mechanic = resolve(event.getItem());
            if (mechanic == null || !TRIGGER_RIGHT_CLICK.equals(mechanic.trigger())) return;

            // Ziel zuerst ermitteln - erst bei echtem Treffer wird der Cooldown verbraucht
            RayTraceResult result = player.getWorld().rayTraceBlocks(
                player.getEyeLocation(), player.getEyeLocation().getDirection(),
                mechanic.range(), FluidCollisionMode.NEVER, true);
            if (result == null || result.getHitBlock() == null) return;

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

            Location target = result.getHitBlock().getLocation().add(0.5, 1.0, 0.5);
            strike(target, mechanic);
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            cooldowns.remove(event.getPlayer().getUniqueId());
        }

        private void strike(Location target, ThorMechanic mechanic) {
            World world = target.getWorld();
            if (world == null) return;

            int amount = Math.max(1, mechanic.lightningBoltsAmount());
            double variation = mechanic.randomLocationVariation();
            for (int i = 0; i < amount; i++) {
                Location strikeLoc = target.clone();
                if (variation > 0) {
                    strikeLoc.add((Math.random() * 2 - 1) * variation, 0, (Math.random() * 2 - 1) * variation);
                }
                if (mechanic.visualOnly()) {
                    world.strikeLightningEffect(strikeLoc);
                } else {
                    world.strikeLightning(strikeLoc);
                }
            }

            if (mechanic.sound() != null) {
                world.playSound(target, mechanic.sound(), 1.0f, 1.0f);
            }
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
        private static ThorMechanic resolve(@Nullable ItemStack item) {
            String id = NexoItems.idFromItem(item);
            if (id == null) return null;
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(id);
            if (mechanics == null) return null;
            return mechanics.getThorMechanic();
        }
    }
}
