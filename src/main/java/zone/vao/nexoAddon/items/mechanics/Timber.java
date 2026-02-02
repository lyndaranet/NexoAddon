package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.nexomc.protectionlib.ProtectionLib;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.utils.BlockUtil;
import zone.vao.nexoAddon.utils.EventUtil;

import java.util.*;

import static zone.vao.nexoAddon.utils.BlockUtil.isInteractable;

public record Timber(int limit, int maxHeight, boolean toggleable, boolean breakLeaves, List<Material> logs,
                     List<String> nexoLogs) {

    private static final Set<Material> TREE_LOGS = new HashSet<>(Arrays.asList(
        Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG,
        Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
        Material.MANGROVE_LOG, Material.CHERRY_LOG, Material.CRIMSON_STEM,
        Material.WARPED_STEM, Material.STRIPPED_OAK_LOG, Material.STRIPPED_SPRUCE_LOG,
        Material.STRIPPED_BIRCH_LOG, Material.STRIPPED_JUNGLE_LOG,
        Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_DARK_OAK_LOG,
        Material.STRIPPED_MANGROVE_LOG, Material.STRIPPED_CHERRY_LOG,
        Material.STRIPPED_CRIMSON_STEM, Material.STRIPPED_WARPED_STEM
    ));

    private static final Set<Material> TREE_LEAVES = new HashSet<>(Arrays.asList(
        Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.BIRCH_LEAVES,
        Material.JUNGLE_LEAVES, Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES,
        Material.MANGROVE_LEAVES, Material.CHERRY_LEAVES, Material.AZALEA_LEAVES,
        Material.FLOWERING_AZALEA_LEAVES, Material.NETHER_WART_BLOCK,
        Material.WARPED_WART_BLOCK
    ));

    public static boolean isTimberTool(String toolId) {
        return toolId != null && NexoAddon.getInstance().getMechanics().containsKey(toolId)
               && NexoAddon.getInstance().getMechanics().get(toolId).getTimber() != null;
    }

    public static class TimberListener implements Listener {
        private static int activeBlockBreaks = 0;
        private static final NamespacedKey key = new NamespacedKey(NexoAddon.getInstance(), "timberToggleable");

        @EventHandler
        public static void onBreak(BlockBreakEvent event) {
            Player player = event.getPlayer();
            ItemStack tool = player.getInventory().getItemInMainHand();

            String toolId = NexoItems.idFromItem(tool);
            if (!Timber.isTimberTool(toolId)) {
                return;
            }

            if (activeBlockBreaks > 0) {
                activeBlockBreaks--;
                return;
            }

            Timber timberMechanic = NexoAddon.getInstance()
                .getMechanics()
                .get(toolId)
                .getTimber();
            if (timberMechanic == null) {
                return;
            }

            PersistentDataContainer pdc = tool.getItemMeta().getPersistentDataContainer();

            if (timberMechanic.toggleable()
                && pdc.has(key, PersistentDataType.BOOLEAN)
                && Boolean.FALSE.equals(pdc.get(key, PersistentDataType.BOOLEAN))) {
                return;
            }

            Block originBlock = event.getBlock();
            if (!isValidLog(timberMechanic, originBlock)) {
                return;
            }

            chopTree(player, originBlock, timberMechanic, tool);
            activeBlockBreaks = 0;
        }

        private static boolean isValidLog(Timber timber, Block block) {
            Material material = block.getType();

            // Check if it's in the whitelist
            if (!timber.logs().isEmpty() && timber.logs().contains(material)) {
                return true;
            }

            // Check Nexo blocks
            CustomBlockMechanic mechanic = NexoBlocks.customBlockMechanic(block);
            if (mechanic != null) {
                String nexoId = mechanic.getItemID();
                if (nexoId != null && timber.nexoLogs().contains(nexoId)) {
                    return true;
                }
            }

            // If whitelist is empty, use default tree logs
            if (timber.logs().isEmpty() && timber.nexoLogs().isEmpty()) {
                return TREE_LOGS.contains(material);
            }

            return false;
        }

        private static boolean isValidLeaf(Block block) {
            return TREE_LEAVES.contains(block.getType());
        }

        private static void chopTree(Player player, Block origin, Timber mechanic, ItemStack tool) {
            Set<Block> treeBlocks = new HashSet<>();
            Queue<Block> blocksToCheck = new LinkedList<>();
            Material originMaterial = origin.getType();
            String originNexoId = getNexoId(origin);

            blocksToCheck.add(origin);
            treeBlocks.add(origin);

            // Find all connected logs
            while (!blocksToCheck.isEmpty() && treeBlocks.size() < mechanic.limit()) {
                Block current = blocksToCheck.poll();

                // Check if we exceeded max height
                if (current.getY() - origin.getY() > mechanic.maxHeight()) {
                    continue;
                }

                for (Block relative : getAdjacentBlocks(current)) {
                    if (treeBlocks.size() >= mechanic.limit()) {
                        break;
                    }

                    if (!treeBlocks.contains(relative) && isValidLog(mechanic, relative)) {
                        // Check if same log type
                        if (shouldAddLog(relative, originMaterial, originNexoId)) {
                            treeBlocks.add(relative);
                            blocksToCheck.add(relative);
                        }
                    }
                }
            }

            // Break leaves if enabled
            if (mechanic.breakLeaves()) {
                findAndBreakLeaves(treeBlocks, mechanic);
            }

            // Break all tree blocks
            breakTreeBlocks(player, origin, treeBlocks, tool);
        }

        private static boolean shouldAddLog(Block block, Material originMaterial, String originNexoId) {
            Material blockMaterial = block.getType();
            String blockNexoId = getNexoId(block);

            // If both are Nexo blocks, check if same ID
            if (originNexoId != null && blockNexoId != null) {
                return originNexoId.equals(blockNexoId);
            }

            // If both are vanilla, check if same material
            if (originNexoId == null && blockNexoId == null) {
                return originMaterial == blockMaterial;
            }

            return false;
        }

        private static void findAndBreakLeaves(Set<Block> treeBlocks, Timber mechanic) {
            Set<Block> leaves = new HashSet<>();
            int searchRadius = 5; // Radius to search for leaves

            for (Block log : new ArrayList<>(treeBlocks)) {
                for (int x = -searchRadius; x <= searchRadius; x++) {
                    for (int y = -searchRadius; y <= searchRadius; y++) {
                        for (int z = -searchRadius; z <= searchRadius; z++) {
                            Block relative = log.getRelative(x, y, z);
                            if (isValidLeaf(relative) && !leaves.contains(relative)) {
                                leaves.add(relative);
                                if (treeBlocks.size() + leaves.size() >= mechanic.limit()) {
                                    break;
                                }
                            }
                        }
                        if (treeBlocks.size() + leaves.size() >= mechanic.limit()) {
                            break;
                        }
                    }
                    if (treeBlocks.size() + leaves.size() >= mechanic.limit()) {
                        break;
                    }
                }
            }

            treeBlocks.addAll(leaves);
        }

        private static String getNexoId(Block block) {
            CustomBlockMechanic mechanic = NexoBlocks.customBlockMechanic(block);
            return mechanic != null ? mechanic.getItemID() : null;
        }

        private static void breakTreeBlocks(Player player, Block origin, Set<Block> treeBlocks, ItemStack tool) {
            for (Block block : treeBlocks) {
                if (block.equals(origin)) {
                    continue;
                }
                attemptBlockBreak(player, block, tool);
            }
        }

        private static void attemptBlockBreak(Player player, Block block, ItemStack tool) {
            if (isUnbreakableBlock(player, block)) {
                return;
            }

            activeBlockBreaks++;
            BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);

            if (!EventUtil.callEvent(blockBreakEvent)) {
                return;
            }

            if (blockBreakEvent.isDropItems()) {
                block.breakNaturally(tool, true, true);
            } else {
                block.setType(Material.AIR);
            }
        }

        private static List<Block> getAdjacentBlocks(Block block) {
            List<Block> adjacent = new ArrayList<>();
            // Check all 6 directions + diagonals for logs (trees can grow diagonally)
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) {
                            continue;
                        }
                        adjacent.add(block.getRelative(x, y, z));
                    }
                }
            }
            return adjacent;
        }

        private static boolean isUnbreakableBlock(Player player, Block block) {
            return block.isLiquid()
                   || BlockUtil.UNBREAKABLE_BLOCKS.contains(block.getType())
                   || !ProtectionLib.canBreak(player, block.getLocation());
        }

        @EventHandler
        public static void onToggle(final PlayerInteractEvent event) {
            Player player = event.getPlayer();
            ItemStack tool = player.getInventory().getItemInMainHand();

            String toolId = NexoItems.idFromItem(tool);
            if (!Timber.isTimberTool(toolId) || event.getHand() != EquipmentSlot.HAND) {
                return;
            }
            if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }
            if (event.getClickedBlock() != null && isInteractable(event.getClickedBlock())) {
                return;
            }

            Timber timberMechanic = NexoAddon.getInstance()
                .getMechanics()
                .get(toolId)
                .getTimber();

            if (!timberMechanic.toggleable() || tool.getItemMeta() == null) {
                return;
            }

            var meta = tool.getItemMeta();
            PersistentDataContainer pdc = meta.getPersistentDataContainer();

            if (!pdc.has(key, PersistentDataType.BOOLEAN)) {
                pdc.set(key, PersistentDataType.BOOLEAN, true);
                tool.setItemMeta(meta);
                turnOn(player, pdc);
                return;
            }

            Boolean isOnValue = pdc.get(key, PersistentDataType.BOOLEAN);
            boolean isOn = isOnValue != null && isOnValue;
            if (isOn) {
                turnOff(player, pdc);
            } else {
                turnOn(player, pdc);
            }

            tool.setItemMeta(meta);
        }

        private static void turnOff(final Player player, PersistentDataContainer pdc) {
            pdc.set(key, PersistentDataType.BOOLEAN, false);
            Audience.audience(player)
                .sendActionBar(MiniMessage.miniMessage().deserialize(NexoAddon.getInstance().getGlobalConfig()
                    .getString("messages.timber.disabled", "<red>Timber disabled")));
        }

        private static void turnOn(final Player player, PersistentDataContainer pdc) {
            pdc.set(key, PersistentDataType.BOOLEAN, true);
            Audience.audience(player)
                .sendActionBar(MiniMessage.miniMessage().deserialize(NexoAddon.getInstance().getGlobalConfig()
                    .getString("messages.timber.enabled", "<green>Timber enabled")));
        }
    }
}
