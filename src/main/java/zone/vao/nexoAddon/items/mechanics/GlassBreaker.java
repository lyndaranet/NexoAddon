package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.protectionlib.ProtectionLib;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record GlassBreaker(List<Material> glassTypes, List<String> nexoGlassTypes, boolean enabled, int durabilityCost) {

    private static final Set<Material> DEFAULT_GLASS_BLOCKS = new HashSet<>();

    static {
        DEFAULT_GLASS_BLOCKS.add(Material.GLASS);
        DEFAULT_GLASS_BLOCKS.add(Material.GLASS_PANE);
        DEFAULT_GLASS_BLOCKS.add(Material.WHITE_STAINED_GLASS);
        DEFAULT_GLASS_BLOCKS.add(Material.WHITE_STAINED_GLASS_PANE);
        DEFAULT_GLASS_BLOCKS.add(Material.ORANGE_STAINED_GLASS);
        DEFAULT_GLASS_BLOCKS.add(Material.ORANGE_STAINED_GLASS_PANE);
        DEFAULT_GLASS_BLOCKS.add(Material.MAGENTA_STAINED_GLASS);
        DEFAULT_GLASS_BLOCKS.add(Material.MAGENTA_STAINED_GLASS_PANE);
        DEFAULT_GLASS_BLOCKS.add(Material.LIGHT_BLUE_STAINED_GLASS);
        DEFAULT_GLASS_BLOCKS.add(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        DEFAULT_GLASS_BLOCKS.add(Material.YELLOW_STAINED_GLASS);
        DEFAULT_GLASS_BLOCKS.add(Material.YELLOW_STAINED_GLASS_PANE);
        DEFAULT_GLASS_BLOCKS.add(Material.LIME_STAINED_GLASS);
        DEFAULT_GLASS_BLOCKS.add(Material.LIME_STAINED_GLASS_PANE);
        DEFAULT_GLASS_BLOCKS.add(Material.PINK_STAINED_GLASS);
        DEFAULT_GLASS_BLOCKS.add(Material.PINK_STAINED_GLASS_PANE);
        DEFAULT_GLASS_BLOCKS.add(Material.GRAY_STAINED_GLASS);
        DEFAULT_GLASS_BLOCKS.add(Material.GRAY_STAINED_GLASS_PANE);
        DEFAULT_GLASS_BLOCKS.add(Material.LIGHT_GRAY_STAINED_GLASS);
        DEFAULT_GLASS_BLOCKS.add(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        DEFAULT_GLASS_BLOCKS.add(Material.CYAN_STAINED_GLASS);
        DEFAULT_GLASS_BLOCKS.add(Material.CYAN_STAINED_GLASS_PANE);
        DEFAULT_GLASS_BLOCKS.add(Material.PURPLE_STAINED_GLASS);
        DEFAULT_GLASS_BLOCKS.add(Material.PURPLE_STAINED_GLASS_PANE);
        DEFAULT_GLASS_BLOCKS.add(Material.BLUE_STAINED_GLASS);
        DEFAULT_GLASS_BLOCKS.add(Material.BLUE_STAINED_GLASS_PANE);
        DEFAULT_GLASS_BLOCKS.add(Material.BROWN_STAINED_GLASS);
        DEFAULT_GLASS_BLOCKS.add(Material.BROWN_STAINED_GLASS_PANE);
        DEFAULT_GLASS_BLOCKS.add(Material.GREEN_STAINED_GLASS);
        DEFAULT_GLASS_BLOCKS.add(Material.GREEN_STAINED_GLASS_PANE);
        DEFAULT_GLASS_BLOCKS.add(Material.RED_STAINED_GLASS);
        DEFAULT_GLASS_BLOCKS.add(Material.RED_STAINED_GLASS_PANE);
        DEFAULT_GLASS_BLOCKS.add(Material.BLACK_STAINED_GLASS);
        DEFAULT_GLASS_BLOCKS.add(Material.BLACK_STAINED_GLASS_PANE);
        DEFAULT_GLASS_BLOCKS.add(Material.TINTED_GLASS);
    }

    public static class GlassBreakerListener implements Listener {

        @EventHandler(priority = EventPriority.HIGH)
        public void onBlockDamage(BlockDamageEvent event) {
            Player player = event.getPlayer();
            Block block = event.getBlock();
            ItemStack tool = player.getInventory().getItemInMainHand();

            // Skip if player is in creative mode
            if (player.getGameMode() == GameMode.CREATIVE) {
                return;
            }

            // Check if tool has glass breaker mechanic
            String itemId = NexoItems.idFromItem(tool);
            if (itemId == null) {
                return;
            }

            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(itemId);
            if (mechanics == null || mechanics.getGlassBreaker() == null) {
                return;
            }

            GlassBreaker glassBreaker = mechanics.getGlassBreaker();
            if (!glassBreaker.enabled()) {
                return;
            }

            // Check if block can be broken with protection
            if (!ProtectionLib.canBreak(player, block.getLocation())) {
                return;
            }

            // Check if block is a glass type
            boolean isGlass;

            // Check vanilla glass blocks
            if (!glassBreaker.glassTypes().isEmpty()) {
                isGlass = glassBreaker.glassTypes().contains(block.getType());
            } else {
                isGlass = DEFAULT_GLASS_BLOCKS.contains(block.getType());
            }

            // Check nexo glass blocks
            if (!isGlass && NexoBlocks.isCustomBlock(block)) {
                var customBlockMechanic = NexoBlocks.customBlockMechanic(block);
                if (customBlockMechanic != null) {
                    String blockId = customBlockMechanic.getItemID();
                    isGlass = glassBreaker.nexoGlassTypes().contains(blockId);
                }
            }

            if (!isGlass) {
                return;
            }

            // Instantly break the glass block
            event.setInstaBreak(true);

            // Apply durability cost
            if (glassBreaker.durabilityCost() > 0 && tool.getItemMeta() instanceof Damageable damageable) {
                // Check if tool has Unbreaking enchantment
                int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.UNBREAKING);
                boolean shouldDamage = true;

                if (unbreakingLevel > 0) {
                    // Unbreaking has a chance to prevent durability loss
                    // Formula: (100 / (unbreakingLevel + 1))% chance to take damage
                    int chance = 100 / (unbreakingLevel + 1);
                    shouldDamage = Math.random() * 100 < chance;
                }

                if (shouldDamage) {
                    int newDamage = damageable.getDamage() + glassBreaker.durabilityCost();
                    if (newDamage >= tool.getType().getMaxDurability()) {
                        // Tool breaks
                        player.getInventory().setItemInMainHand(null);
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    } else {
                        damageable.setDamage(newDamage);
                        tool.setItemMeta(damageable);
                    }
                }
            }
        }
    }
}
