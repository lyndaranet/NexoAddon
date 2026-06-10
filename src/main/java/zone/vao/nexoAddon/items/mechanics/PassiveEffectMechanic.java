package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public record PassiveEffectMechanic(String slot, int reapplyTicks, List<PassivePotionEffect> potionEffects,
                                    List<PassiveAttributeModifier> attributeModifiers, PassiveConditions conditions,
                                    @Nullable Particle ambientParticle, @Nullable Sound activateSound,
                                    @Nullable Sound deactivateSound) {

    public record PassivePotionEffect(PotionEffectType type, int amplifier) {
    }

    public record PassiveAttributeModifier(Attribute attribute, AttributeModifier.Operation operation, double amount) {
    }

    public record PassiveConditions(boolean onSneak, boolean onSprint, int healthBelowPercent, Set<Biome> biomes,
                                    String worldTime) {
    }

    /** Resolved active item for a player: its Nexo id plus the mechanic instance. */
    private record Active(String id, PassiveEffectMechanic mechanic) {
    }

    public static class PassiveEffectMechanicListener implements Listener {

        private static final Map<UUID, BukkitTask> activeTasks = new ConcurrentHashMap<>();
        private static final Map<UUID, Active> activeData = new ConcurrentHashMap<>();

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

        // --- Core ------------------------------------------------------------

        /** Re-evaluate one tick later, so inventory/slot changes have settled. */
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

            // Newly active, or a different passive item moved into the slot
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

            playSound(player, active.mechanic().activateSound());

            long period = Math.max(1, active.mechanic().reapplyTicks());
            BukkitTask task = Bukkit.getScheduler()
                .runTaskTimer(NexoAddon.getInstance(), () -> tick(player, active), 1L, period);
            activeTasks.put(id, task);
        }

        private void deactivateFor(Player player) {
            UUID id = player.getUniqueId();
            cancelTask(id);
            Active prev = activeData.remove(id);
            if (prev != null) {
                removeEffects(player, prev);
                playSound(player, prev.mechanic().deactivateSound());
            }
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

            // Item must still be in its configured slot (and be the same item)
            Active now = findActive(player);
            if (now == null || !now.id().equals(active.id())) {
                deactivateFor(player);
                return;
            }

            PassiveEffectMechanic mechanic = active.mechanic();
            if (conditionsMet(player, mechanic.conditions())) {
                applyPotionEffects(player, mechanic);
                applyAttributeModifiers(player, active.id(), mechanic);
                spawnParticle(player, mechanic);
            } else {
                // Conditions no longer hold — strip effects but keep watching the slot
                removeEffects(player, active);
            }
        }

        // --- Slot resolution -------------------------------------------------

        private static Active findActive(Player player) {
            PlayerInventory inv = player.getInventory();
            Active a;
            if ((a = matchSlot(inv.getItemInMainHand(), "mainhand")) != null) return a;
            if ((a = matchSlot(inv.getItemInOffHand(), "offhand")) != null) return a;
            if ((a = matchSlot(inv.getHelmet(), "armor_head")) != null) return a;
            if ((a = matchSlot(inv.getChestplate(), "armor_chest")) != null) return a;
            if ((a = matchSlot(inv.getLeggings(), "armor_legs")) != null) return a;
            if ((a = matchSlot(inv.getBoots(), "armor_feet")) != null) return a;
            return null;
        }

        private static Active matchSlot(ItemStack item, String physicalSlot) {
            String id = NexoItems.idFromItem(item);
            if (id == null) {
                return null;
            }
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(id);
            if (mechanics == null || mechanics.getPassiveEffectMechanic() == null) {
                return null;
            }
            PassiveEffectMechanic mechanic = mechanics.getPassiveEffectMechanic();
            if (slotMatches(mechanic.slot(), physicalSlot)) {
                return new Active(id, mechanic);
            }
            return null;
        }

        private static boolean slotMatches(String configSlot, String physicalSlot) {
            return switch (configSlot.toLowerCase()) {
                case "any" -> physicalSlot.equals("mainhand") || physicalSlot.equals("offhand");
                default -> configSlot.equalsIgnoreCase(physicalSlot);
            };
        }

        // --- Conditions ------------------------------------------------------

        private static boolean conditionsMet(Player player, PassiveConditions c) {
            if (c.onSneak() && !player.isSneaking()) {
                return false;
            }
            if (c.onSprint() && !player.isSprinting()) {
                return false;
            }

            double max = 20.0;
            AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null && maxHealth.getValue() > 0) {
                max = maxHealth.getValue();
            }
            double healthPercent = (player.getHealth() / max) * 100.0;
            if (healthPercent > c.healthBelowPercent()) {
                return false;
            }

            if (!c.biomes().isEmpty() && !c.biomes().contains(player.getLocation().getBlock().getBiome())) {
                return false;
            }

            String worldTime = c.worldTime();
            if (worldTime != null && !worldTime.equals("any")) {
                boolean isDay = player.getWorld().getTime() < 12000L;
                if (worldTime.equals("day") && !isDay) {
                    return false;
                }
                if (worldTime.equals("night") && isDay) {
                    return false;
                }
            }
            return true;
        }

        // --- Effect application ---------------------------------------------

        private static void applyPotionEffects(Player player, PassiveEffectMechanic mechanic) {
            int duration = mechanic.reapplyTicks() + 20;
            for (PassivePotionEffect effect : mechanic.potionEffects()) {
                player.addPotionEffect(new PotionEffect(effect.type(), duration, effect.amplifier(), true, false, true));
            }
        }

        private static void applyAttributeModifiers(Player player, String itemId, PassiveEffectMechanic mechanic) {
            for (PassiveAttributeModifier mod : mechanic.attributeModifiers()) {
                AttributeInstance instance = player.getAttribute(mod.attribute());
                if (instance == null) {
                    continue;
                }
                NamespacedKey key = modifierKey(itemId, mod.attribute());
                boolean present = instance.getModifiers().stream().anyMatch(m -> m.getKey().equals(key));
                if (!present) {
                    // Transient: never written to player data, so it can't leak across restarts
                    instance.addTransientModifier(new AttributeModifier(key, mod.amount(), mod.operation()));
                }
            }
        }

        private static void removeEffects(Player player, Active active) {
            PassiveEffectMechanic mechanic = active.mechanic();
            for (PassivePotionEffect effect : mechanic.potionEffects()) {
                player.removePotionEffect(effect.type());
            }
            for (PassiveAttributeModifier mod : mechanic.attributeModifiers()) {
                AttributeInstance instance = player.getAttribute(mod.attribute());
                if (instance == null) {
                    continue;
                }
                NamespacedKey key = modifierKey(active.id(), mod.attribute());
                for (AttributeModifier m : new ArrayList<>(instance.getModifiers())) {
                    if (m.getKey().equals(key)) {
                        instance.removeModifier(m);
                    }
                }
            }
        }

        private static NamespacedKey modifierKey(String itemId, Attribute attribute) {
            String raw = (itemId + "_" + attribute.getKey().getKey()).toLowerCase().replaceAll("[^a-z0-9._-]", "_");
            return new NamespacedKey(NexoAddon.getInstance(), raw);
        }

        // --- Feedback --------------------------------------------------------

        private static void spawnParticle(Player player, PassiveEffectMechanic mechanic) {
            if (mechanic.ambientParticle() != null) {
                player.getWorld().spawnParticle(mechanic.ambientParticle(),
                    player.getLocation().add(0, 1, 0), 3, 0.3, 0.5, 0.3, 0.0);
            }
        }

        private static void playSound(Player player, @Nullable Sound sound) {
            if (sound != null) {
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            }
        }
    }
}
