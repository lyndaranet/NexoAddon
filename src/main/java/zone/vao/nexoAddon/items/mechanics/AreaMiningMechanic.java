package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public record AreaMiningMechanic(Shape shape, boolean consumeDurability, int length, int maxBlocks, int radius,
                                 int depth, @Nullable List<String> toolTypes, @Nullable Set<Material> deniedBlocks) {

    public enum Shape {
        LINE, VEIN, CUBE
    }

    private static final BlockFace[] CARDINAL_FACES = {
        BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    public static class AreaMiningMechanicListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onBreak(BlockBreakEvent event) {
            Player player = event.getPlayer();
            ItemStack tool = player.getInventory().getItemInMainHand();

            String nexoItemId = NexoItems.idFromItem(tool);
            if (nexoItemId == null) {
                return;
            }

            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(nexoItemId);
            if (mechanics == null || mechanics.getAreaMiningMechanic() == null) {
                return;
            }

            AreaMiningMechanic mechanic = mechanics.getAreaMiningMechanic();

            // Optional tool-type filter — only activate for matching material keywords
            if (mechanic.toolTypes() != null && !mechanic.toolTypes().isEmpty()) {
                String toolName = tool.getType().name().toLowerCase();
                boolean matches = mechanic.toolTypes().stream().anyMatch(t -> toolName.contains(t.toLowerCase()));
                if (!matches) {
                    return;
                }
            }

            Block origin = event.getBlock();
            BlockFace face = resolveFace(player);

            List<Block> extraBlocks = switch (mechanic.shape()) {
                case LINE -> getLineBlocks(origin, face, mechanic.length());
                case VEIN -> getVeinBlocks(origin, mechanic.maxBlocks());
                case CUBE -> getCubeBlocks(origin, face, mechanic.radius(), mechanic.depth());
            };

            for (Block block : extraBlocks) {
                if (block.equals(origin)) {
                    continue;
                }
                if (block.getType().isAir() || block.getType().getHardness() < 0) {
                    continue;
                }
                if (mechanic.deniedBlocks() != null && mechanic.deniedBlocks().contains(block.getType())) {
                    continue;
                }

                block.breakNaturally(tool);

                if (mechanic.consumeDurability()) {
                    tool.damage(1, player);
                }
            }
        }

        private static BlockFace resolveFace(Player player) {
            RayTraceResult ray = player.rayTraceBlocks(6);
            if (ray != null && ray.getHitBlockFace() != null) {
                return ray.getHitBlockFace();
            }
            return BlockFace.UP;
        }

        private static List<Block> getLineBlocks(Block origin, BlockFace face, int length) {
            // Extend into the material, opposite the hit face (the direction the player is looking)
            BlockFace direction = face.getOppositeFace();
            List<Block> blocks = new ArrayList<>();
            for (int i = 1; i <= length; i++) {
                blocks.add(origin.getRelative(direction, i));
            }
            return blocks;
        }

        private static List<Block> getVeinBlocks(Block origin, int maxBlocks) {
            Material targetType = origin.getType();
            List<Block> result = new ArrayList<>();
            Set<Block> visited = new HashSet<>();
            Queue<Block> queue = new ArrayDeque<>();

            visited.add(origin);
            queue.add(origin);

            // visited includes the origin, so the cap counts the original block toward max_blocks
            while (!queue.isEmpty() && visited.size() < maxBlocks) {
                Block current = queue.poll();
                for (BlockFace face : CARDINAL_FACES) {
                    Block neighbor = current.getRelative(face);
                    if (visited.contains(neighbor) || neighbor.getType() != targetType) {
                        continue;
                    }
                    visited.add(neighbor);
                    result.add(neighbor);
                    queue.add(neighbor);
                    if (visited.size() >= maxBlocks) {
                        break;
                    }
                }
            }
            return result;
        }

        private static List<Block> getCubeBlocks(Block origin, BlockFace face, int radius, int depth) {
            // Inward direction = opposite the hit face; the flat layer is perpendicular to this axis
            BlockFace inward = face.getOppositeFace();
            int dirX = inward.getModX();
            int dirY = inward.getModY();
            int dirZ = inward.getModZ();

            List<Block> blocks = new ArrayList<>();
            for (int u = -radius; u <= radius; u++) {
                for (int v = -radius; v <= radius; v++) {
                    for (int d = 0; d < depth; d++) {
                        Block block;
                        if (dirY != 0) {
                            // hit face is on the Y axis → flat layer spans X/Z, depth along Y
                            block = origin.getRelative(u, dirY * d, v);
                        } else if (dirX != 0) {
                            // hit face is on the X axis → flat layer spans Y/Z, depth along X
                            block = origin.getRelative(dirX * d, u, v);
                        } else {
                            // hit face is on the Z axis → flat layer spans X/Y, depth along Z
                            block = origin.getRelative(u, v, dirZ * d);
                        }
                        blocks.add(block);
                    }
                }
            }
            return blocks;
        }
    }
}
