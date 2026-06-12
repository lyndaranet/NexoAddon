package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Generic mechanic for any usable/consumable item — food, potions, skulls, runes, scrolls, etc.
 * One config block ({@code Mechanics.consumable}) drives instant heal/damage, potion effects,
 * console commands, messages and visual feedback, gated by per-player cooldown and conditions.
 */
public record ConsumableMechanic(String trigger, int cooldownSeconds, boolean consumeItem, double instantHeal,
                                 double instantDamage, List<ConsumableEffect> effects, List<String> commands,
                                 ConsumableConditions conditions, @Nullable Sound sound, @Nullable Particle particle,
                                 @Nullable String messageSelf, @Nullable String messageBroadcast) {

    public static final String TRIGGER_EAT = "eat";
    public static final String TRIGGER_RIGHT_CLICK = "right_click";
    public static final String TRIGGER_RIGHT_CLICK_BLOCK = "right_click_block";

    public record ConsumableEffect(PotionEffectType type, int amplifier, int durationSeconds, double chance) {
    }

    public record ConsumableConditions(boolean requireSneaking, int requireHealthBelowPercent,
                                       @Nullable String requirePermission) {
    }

    public static class ConsumableMechanicListener implements Listener {

        private static final Map<UUID, Long> cooldowns = new HashMap<>();

        @EventHandler(ignoreCancelled = true)
        public void onConsume(PlayerItemConsumeEvent event) {
            Player player = event.getPlayer();
            ItemStack item = event.getItem();
            ConsumableMechanic mechanic = resolve(item);
            if (mechanic == null || !TRIGGER_EAT.equals(mechanic.trigger())) {
                return;
            }
            // Item is already consumed by Minecraft for the eat trigger — never consume manually.
            handleUse(player, item, mechanic, event, false);
        }

        @EventHandler
        public void onInteract(PlayerInteractEvent event) {
            // Only react to the main-hand invocation to avoid the off-hand double fire.
            if (event.getHand() != EquipmentSlot.HAND) {
                return;
            }
            Action action = event.getAction();
            boolean rightClickBlock = action == Action.RIGHT_CLICK_BLOCK;
            boolean rightClickAir = action == Action.RIGHT_CLICK_AIR;
            if (!rightClickBlock && !rightClickAir) {
                return;
            }

            Player player = event.getPlayer();
            ItemStack item = event.getItem();
            ConsumableMechanic mechanic = resolve(item);
            if (mechanic == null) {
                return;
            }

            String triggerType = mechanic.trigger();
            if (TRIGGER_RIGHT_CLICK.equals(triggerType)) {
                // Both air and block clicks are valid here.
            } else if (TRIGGER_RIGHT_CLICK_BLOCK.equals(triggerType)) {
                if (!rightClickBlock) {
                    return;
                }
            } else {
                return;
            }

            handleUse(player, item, mechanic, event, mechanic.consumeItem());
        }

        // --- Core ------------------------------------------------------------

        private static void handleUse(Player player, ItemStack item, ConsumableMechanic mechanic,
            Cancellable event, boolean consume) {

            if (!conditionsMet(player, mechanic.conditions())) {
                player.sendActionBar(MiniMessage.miniMessage()
                    .deserialize("<red>Du kannst das gerade nicht benutzen."));
                event.setCancelled(true);
                return;
            }

            long now = System.currentTimeMillis();
            long remainingMs = remainingCooldown(player.getUniqueId(), mechanic.cooldownSeconds(), now);
            if (remainingMs > 0) {
                long remainingSeconds = (remainingMs + 999) / 1000;
                player.sendActionBar(MiniMessage.miniMessage()
                    .deserialize("<red>Noch <bold>" + remainingSeconds + "s</bold> Cooldown!"));
                event.setCancelled(true);
                return;
            }

            if (consume && item != null) {
                item.setAmount(item.getAmount() - 1);
            }

            applyInstantHealth(player, mechanic);
            applyEffects(player, mechanic);
            runCommands(player, mechanic);
            sendMessages(player, mechanic);
            playFeedback(player, mechanic);

            if (mechanic.cooldownSeconds() > 0) {
                cooldowns.put(player.getUniqueId(), now);
            }
        }

        // --- Conditions ------------------------------------------------------

        private static boolean conditionsMet(Player player, ConsumableConditions c) {
            if (c == null) {
                return true;
            }
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

        // --- Effect application ---------------------------------------------

        private static void applyInstantHealth(Player player, ConsumableMechanic mechanic) {
            double max = 20.0;
            AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null && maxHealth.getValue() > 0) {
                max = maxHealth.getValue();
            }
            double health = player.getHealth();
            if (mechanic.instantHeal() > 0) {
                health = Math.min(max, health + mechanic.instantHeal());
            }
            if (mechanic.instantDamage() > 0) {
                health = Math.max(0, health - mechanic.instantDamage());
            }
            if (health != player.getHealth()) {
                player.setHealth(health);
            }
        }

        private static void applyEffects(Player player, ConsumableMechanic mechanic) {
            for (ConsumableEffect effect : mechanic.effects()) {
                if (effect.chance() < 1.0 && Math.random() >= effect.chance()) {
                    continue;
                }
                player.addPotionEffect(new PotionEffect(
                    effect.type(), effect.durationSeconds() * 20, effect.amplifier()));
            }
        }

        private static void runCommands(Player player, ConsumableMechanic mechanic) {
            if (mechanic.commands().isEmpty()) {
                return;
            }
            String name = player.getName();
            for (String raw : mechanic.commands()) {
                String command = raw.replace("{player}", name);
                NexoAddon.getInstance().getFoliaLib().getScheduler()
                    .runNextTick(t -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
            }
        }

        private static void sendMessages(Player player, ConsumableMechanic mechanic) {
            if (mechanic.messageSelf() != null && !mechanic.messageSelf().isEmpty()) {
                player.sendMessage(MiniMessage.miniMessage().deserialize(mechanic.messageSelf()));
            }
            if (mechanic.messageBroadcast() != null && !mechanic.messageBroadcast().isEmpty()) {
                String broadcast = mechanic.messageBroadcast().replace("{player}", player.getName());
                Bukkit.broadcast(MiniMessage.miniMessage().deserialize(broadcast));
            }
        }

        private static void playFeedback(Player player, ConsumableMechanic mechanic) {
            Location loc = player.getLocation();
            if (mechanic.sound() != null) {
                player.playSound(loc, mechanic.sound(), 1.0f, 1.0f);
            }
            if (mechanic.particle() != null) {
                player.getWorld().spawnParticle(mechanic.particle(), loc.add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.0);
            }
        }

        // --- Resolution ------------------------------------------------------

        @Nullable
        private static ConsumableMechanic resolve(@Nullable ItemStack item) {
            String id = NexoItems.idFromItem(item);
            if (id == null) {
                return null;
            }
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(id);
            if (mechanics == null) {
                return null;
            }
            return mechanics.getConsumableMechanic();
        }
    }
}
