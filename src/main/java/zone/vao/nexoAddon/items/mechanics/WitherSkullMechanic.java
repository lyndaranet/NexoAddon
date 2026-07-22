package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Fires a wither skull in the player's look direction, optionally charged (blue, more destructive). Gated by a
 * per-player cooldown. One config block ({@code Mechanics.witherskull}).
 */
public record WitherSkullMechanic(String trigger, boolean charged, int cooldownSeconds, double velocity,
                                  @Nullable Sound sound) {

    public static final String TRIGGER_RIGHT_CLICK = "right_click";
    public static final String TRIGGER_SHIFT_RIGHT_CLICK = "shift_right_click";
    public static final String TRIGGER_LEFT_CLICK = "left_click";

    public static class WitherSkullMechanicListener implements Listener {

        private static final Object LOCK = new Object();
        private static final Map<UUID, Long> cooldowns = new HashMap<>();
        // PlayerInteractEvent can fire twice per right-click (not always on the same tick);
        // used to drop the duplicate dispatch instead of treating it as a second real use.
        private static final Map<UUID, Long> lastInteractMillis = new HashMap<>();
        private static final long DUPLICATE_EVENT_WINDOW_MS = 100;

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
            WitherSkullMechanic mechanic = resolve(event.getItem());
            if (mechanic == null) {
                return;
            }

            NexoAddon.getInstance().getLogger().info("[witherskull-debug] " + player.getName()
                                                     + " -> onInteract action=" + action);

            switch (mechanic.trigger()) {
                case TRIGGER_RIGHT_CLICK -> {
                    if (!right) {
                        return;
                    }
                }
                case TRIGGER_SHIFT_RIGHT_CLICK -> {
                    if (!right || !player.isSneaking()) {
                        return;
                    }
                }
                case TRIGGER_LEFT_CLICK -> {
                    if (!left) {
                        return;
                    }
                }
                default -> {
                    return;
                }
            }

            // Prevent the item's vanilla use action (e.g. throwing) firing alongside/instead of
            // our cooldown-gated ability, regardless of whether the cast actually goes through.
            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);

            fire(player, mechanic);
        }

        private void fire(Player player, WitherSkullMechanic mechanic) {
            UUID id = player.getUniqueId();
            long now = System.currentTimeMillis();

            synchronized (LOCK) {
                Long lastInteract = lastInteractMillis.get(id);
                if (lastInteract != null && now - lastInteract < DUPLICATE_EVENT_WINDOW_MS) {
                    NexoAddon.getInstance().getLogger().info("[witherskull-debug] " + player.getName()
                                                             + " -> dropped duplicate event (" + (now - lastInteract)
                                                             + "ms since last)");
                    return;
                }
                lastInteractMillis.put(id, now);

                long remainingMs = remainingCooldown(id, mechanic.cooldownSeconds(), now);
                if (remainingMs > 0) {
                    long remainingSeconds = (remainingMs + 999) / 1000;
                    NexoAddon.getInstance().getLogger().info("[witherskull-debug] " + player.getName()
                                                             + " -> on cooldown, " + remainingMs + "ms remaining");
                    actionBar(player, "<red>Noch <bold>" + remainingSeconds + "s</bold> Cooldown!");
                    return;
                }
                if (mechanic.cooldownSeconds() > 0) {
                    cooldowns.put(id, now);
                }
                NexoAddon.getInstance().getLogger().info("[witherskull-debug] " + player.getName()
                                                         + " -> casting (cooldownSeconds=" + mechanic.cooldownSeconds()
                                                         + ")");
            }

            Vector velocity = player.getEyeLocation().getDirection().normalize().multiply(mechanic.velocity());
            WitherSkull skull = player.launchProjectile(WitherSkull.class, velocity);
            skull.setShooter(player);
            skull.setCharged(mechanic.charged());

            if (mechanic.sound() != null) {
                player.playSound(player.getLocation(), mechanic.sound(), 1.0f, 1.0f);
            }
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
        private static WitherSkullMechanic resolve(@Nullable ItemStack item) {
            String id = NexoItems.idFromItem(item);
            if (id == null) {
                return null;
            }
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(id);
            if (mechanics == null) {
                return null;
            }
            return mechanics.getWitherSkullMechanic();
        }
    }
}
