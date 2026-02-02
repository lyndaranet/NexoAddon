package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.Collection;
import java.util.HashMap;

public record Telekinesis(boolean enabled) {

    public static boolean hasTelekinesis(String toolId) {
        if (toolId == null) {
            return false;
        }
        Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(toolId);
        return mechanics != null && mechanics.getTelekinesis() != null && mechanics.getTelekinesis().enabled();
    }

    public static class TelekinesisListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public static void onBlockBreak(BlockBreakEvent event) {
            Player player = event.getPlayer();

            ItemStack tool = player.getInventory().getItemInMainHand();
            String toolId = NexoItems.idFromItem(tool);

            // Prüfe ob das Tool Telekinese hat
            if (!hasTelekinesis(toolId)) {
                return;
            }

            // Prüfe ob Drops aktiviert sind
            if (!event.isDropItems()) {
                return;
            }

            Block block = event.getBlock();

            // Verhindere normale Drops
            event.setDropItems(false);

            // Sammle die Drops die der Block normalerweise geben würde
            Collection<ItemStack> drops = block.getDrops(tool, player);

            // Füge die Items direkt zum Inventar hinzu
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(drops.toArray(new ItemStack[0]));

            // Wenn das Inventar voll ist, droppe die übrigen Items
            if (!leftover.isEmpty()) {
                for (ItemStack item : leftover.values()) {
                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                }
            }
        }
    }
}
