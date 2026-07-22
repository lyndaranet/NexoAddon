package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Strikes lightning at the block the player is looking at (ray-traced), spawning a configurable
 * number of bolts with random horizontal scatter. Gated by a per-player cooldown.
 * One config block ({@code Mechanics.thor}).
 */
public record ThorMechanic(String trigger, int lightningBoltsAmount, double randomLocationVariation,
                           double range, int cooldownSeconds, boolean visualOnly, @Nullable Sound sound) {

    public static final String TRIGGER_RIGHT_CLICK = "right_click";
    public static final String TRIGGER_SHIFT_RIGHT_CLICK = "shift_right_click";
    public static final String TRIGGER_LEFT_CLICK = "left_click";

    public static class ThorMechanicListener implements Listener {

        private static final Map<UUID, Long> cooldowns = new HashMap<>();
        // PlayerInteractEvent fires twice per right-click on the same tick; used to drop the duplicate.
        private static final Map<UUID, Integer> lastInteractTick = new HashMap<>();

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
            ThorMechanic mechanic = resolve(event.getItem());
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

            // Drop the duplicate PlayerInteractEvent dispatch on the same tick to avoid a false
            // cooldown message flashing on every successful use.
            int tick = Bukkit.getServer().getCurrentTick();
            Integer prev = lastInteractTick.get(player.getUniqueId());
            if (prev != null && prev == tick) {
                return;
            }
            lastInteractTick.put(player.getUniqueId(), tick);

            strike(player, mechanic);
        }

        private void strike(Player player, ThorMechanic mechanic) {
            UUID id = player.getUniqueId();
            long now = System.currentTimeMillis();
            long remainingMs = remainingCooldown(id, mechanic.cooldownSeconds(), now);
            if (remainingMs > 0) {
                long remainingSeconds = (remainingMs + 999) / 1000;
                actionBar(player, "<red>Noch <bold>" + remainingSeconds + "s</bold> Cooldown!");
                return;
            }

            Location target = targetLocation(player, mechanic.range());
            World world = target.getWorld();
            if (world == null) {
                return;
            }

            int bolts = Math.max(1, mechanic.lightningBoltsAmount());
            double variation = mechanic.randomLocationVariation();
            for (int i = 0; i < bolts; i++) {
                Location at = target.clone();
                if (variation > 0) {
                    at.add(randomOffset(variation), 0, randomOffset(variation));
                }
                if (mechanic.visualOnly()) {
                    world.strikeLightningEffect(at);
                } else {
                    world.strikeLightning(at);
                }
            }

            if (mechanic.sound() != null) {
                player.playSound(target, mechanic.sound(), 1.0f, 1.0f);
            }
            if (mechanic.cooldownSeconds() > 0) {
                cooldowns.put(id, now);
            }
        }

        /** Ray-traces from the player's eyes to the first block hit, falling back to the range's end point. */
        private static Location targetLocation(Player player, double range) {
            Location eye = player.getEyeLocation();
            Vector direction = eye.getDirection();
            World world = player.getWorld();
            RayTraceResult result = world.rayTraceBlocks(eye, direction, range, FluidCollisionMode.NEVER, true);
            if (result != null && result.getHitPosition() != null) {
                return result.getHitPosition().toLocation(world);
            }
            return eye.add(direction.multiply(range));
        }

        private static double randomOffset(double variation) {
            return ThreadLocalRandom.current().nextDouble(-variation, variation);
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
        private static ThorMechanic resolve(@Nullable ItemStack item) {
            String id = NexoItems.idFromItem(item);
            if (id == null) {
                return null;
            }
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(id);
            if (mechanics == null) {
                return null;
            }
            return mechanics.getThorMechanic();
        }
    }
}
