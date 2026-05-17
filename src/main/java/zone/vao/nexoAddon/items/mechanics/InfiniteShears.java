package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.ArrayList;
import java.util.List;

public record InfiniteShears(boolean enabled, int uses) {

    public static final NamespacedKey USES_KEY =
        new NamespacedKey(NexoAddon.getInstance(), "infinite_shears_uses");

    private static final NamespacedKey LORE_INDEX_KEY =
        new NamespacedKey(NexoAddon.getInstance(), "infinite_shears_lore_index");

    public static void initUses(ItemStack item, int maxUses) {
        if (maxUses < 0) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(USES_KEY, PersistentDataType.INTEGER, maxUses);
        applyLore(meta, maxUses);
        item.setItemMeta(meta);
    }

    private static void applyLore(ItemMeta meta, int remaining) {
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

    public static class InfiniteShearsListener implements Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onItemDamage(PlayerItemDamageEvent event) {
            ItemStack item = event.getItem();

            String nexoItemId = NexoItems.idFromItem(item);
            if (nexoItemId == null) return;

            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(nexoItemId);
            if (mechanics == null || mechanics.getInfiniteShears() == null) return;

            InfiniteShears infiniteShears = mechanics.getInfiniteShears();
            if (!infiniteShears.enabled()) return;

            if (infiniteShears.uses() < 0) {
                event.setCancelled(true);
                return;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            int remaining = pdc.getOrDefault(USES_KEY, PersistentDataType.INTEGER, infiniteShears.uses());

            if (remaining <= 0) return;

            event.setCancelled(true);
            int newRemaining = remaining - 1;
            pdc.set(USES_KEY, PersistentDataType.INTEGER, newRemaining);
            applyLore(meta, newRemaining);
            item.setItemMeta(meta);
        }
    }
}
