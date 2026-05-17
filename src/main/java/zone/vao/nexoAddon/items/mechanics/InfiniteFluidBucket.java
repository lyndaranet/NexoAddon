package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.protectionlib.ProtectionLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.ArrayList;
import java.util.List;

/**
 * Ein einzelnes Item, das per Rechtsklick-Luft zwischen Wasser- und Lava-Modus wechselt
 * und per Rechtsklick-Block die aktuelle Flüssigkeit unendlich platziert.
 */
public record InfiniteFluidBucket(boolean enabled, int uses, String waterLore, String lavaLore) {

    private static final NamespacedKey MODE_KEY =
        new NamespacedKey(NexoAddon.getInstance(), "fluid_bucket_mode");
    public static final NamespacedKey USES_KEY =
        new NamespacedKey(NexoAddon.getInstance(), "fluid_bucket_uses");
    private static final NamespacedKey LORE_INDEX_KEY =
        new NamespacedKey(NexoAddon.getInstance(), "fluid_bucket_lore_index");

    private static final MiniMessage MINI = MiniMessage.builder()
        .postProcessor(pro -> pro.decoration(TextDecoration.ITALIC, false)).build();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    public static void initUses(ItemStack item, int maxUses) {
        if (maxUses < 0) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(USES_KEY, PersistentDataType.INTEGER, maxUses);
        applyUsesLore(meta, maxUses);
        item.setItemMeta(meta);
    }

    private static void applyUsesLore(ItemMeta meta, int remaining) {
        List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        Component usesLine = MiniMessage.miniMessage()
            .deserialize("<!italic><gray>Verwendungen: <white>" + remaining + "</white></gray>");

        if (pdc.has(LORE_INDEX_KEY, PersistentDataType.INTEGER)) {
            Integer idxBoxed = pdc.get(LORE_INDEX_KEY, PersistentDataType.INTEGER);
            int idx = idxBoxed != null ? idxBoxed : -1;
            if (idx >= 0 && idx < lore.size()) {
                lore.set(idx, usesLine);
            } else {
                lore.add(usesLine);
                pdc.set(LORE_INDEX_KEY, PersistentDataType.INTEGER, lore.size() - 1);
            }
        } else {
            lore.add(usesLine);
            pdc.set(LORE_INDEX_KEY, PersistentDataType.INTEGER, lore.size() - 1);
        }

        meta.lore(lore);
    }

    /** Gibt den aktuellen Modus des Items zurück ("WATER" oder "LAVA"). */
    public static String getMode(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return "WATER";
        return meta.getPersistentDataContainer()
            .getOrDefault(MODE_KEY, PersistentDataType.STRING, "WATER");
    }

    private static void setMode(ItemStack item, String mode, InfiniteFluidBucket config) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String oldMode = pdc.getOrDefault(MODE_KEY, PersistentDataType.STRING, "WATER");
        pdc.set(MODE_KEY, PersistentDataType.STRING, mode);

        Component newLine = MINI.deserialize(mode.equals("LAVA") ? config.lavaLore() : config.waterLore());
        String newLinePlain = PLAIN.serialize(newLine);
        Component oldLine = MINI.deserialize(oldMode.equals("LAVA") ? config.lavaLore() : config.waterLore());
        String oldLinePlain = PLAIN.serialize(oldLine);

        List<Component> lore = meta.lore();
        if (lore == null) lore = new ArrayList<>();

        boolean replaced = false;
        for (int i = 0; i < lore.size(); i++) {
            String linePlain = PLAIN.serialize(lore.get(i));
            if (linePlain.equals(oldLinePlain) || linePlain.equals(newLinePlain)) {
                lore.set(i, newLine);
                replaced = true;
                break;
            }
        }
        if (!replaced) lore.add(newLine);

        meta.lore(lore);
        item.setItemMeta(meta);
    }

    public static class InfiniteFluidBucketListener implements Listener {

        @EventHandler(priority = EventPriority.HIGH)
        public void onInteract(PlayerInteractEvent event) {
            if (event.getHand() != EquipmentSlot.HAND) return;

            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();

            String nexoItemId = NexoItems.idFromItem(item);
            if (nexoItemId == null) return;

            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(nexoItemId);
            if (mechanics == null || mechanics.getInfiniteFluidBucket() == null) return;

            InfiniteFluidBucket fluidBucket = mechanics.getInfiniteFluidBucket();
            if (!fluidBucket.enabled()) return;

            Action action = event.getAction();

            if (action == Action.RIGHT_CLICK_AIR) {
                String newMode = getMode(item).equals("WATER") ? "LAVA" : "WATER";
                setMode(item, newMode, fluidBucket);
                player.getInventory().setItemInMainHand(item);
                event.setCancelled(true);
                return;
            }

            if (action == Action.RIGHT_CLICK_BLOCK) {
                Block clickedBlock = event.getClickedBlock();
                if (clickedBlock == null) return;

                BlockFace face = event.getBlockFace();
                if (face == BlockFace.SELF) return;

                Block targetBlock = clickedBlock.getRelative(face);

                if (!ProtectionLib.canBuild(player, targetBlock.getLocation())) {
                    event.setCancelled(true);
                    return;
                }

                Material targetType = targetBlock.getType();
                if (!targetType.isAir() && targetType != Material.WATER && targetType != Material.LAVA) {
                    return;
                }

                // Lese Modus VOR möglicher Meta-Änderung
                String mode = getMode(item);

                if (fluidBucket.uses() >= 0) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta == null) return;
                    PersistentDataContainer pdc = meta.getPersistentDataContainer();
                    int remaining = pdc.getOrDefault(USES_KEY, PersistentDataType.INTEGER, fluidBucket.uses());

                    if (remaining <= 0) return;

                    int newRemaining = remaining - 1;
                    pdc.set(USES_KEY, PersistentDataType.INTEGER, newRemaining);
                    applyUsesLore(meta, newRemaining);
                    item.setItemMeta(meta);
                    player.getInventory().setItemInMainHand(item);
                }

                targetBlock.setType(mode.equals("LAVA") ? Material.LAVA : Material.WATER);
                event.setCancelled(true);
            }
        }
    }
}
