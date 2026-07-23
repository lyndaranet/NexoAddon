package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Zwei unabhängig togglebare Sub-Effekte fürs Abbauen, jeweils mit eigener Blockliste: faster_mining (Haste beim
 * Anschlagen gelisteter Blöcke) und bonus_drops (Chance auf zusätzliche Drops, als Multiplikator auf die bereits
 * berechneten Drops inkl. Fortune/Silk Touch).
 */
public record BetterMineMechanic(boolean enabled, @Nullable FasterMining fasterMining,
                                 @Nullable BonusDrops bonusDrops) {

    /**
     * Haste wird bei jedem BlockDamageEvent auf einen gelisteten Block für diese Dauer (neu) angewendet.
     */
    private static final int HASTE_DURATION_TICKS = 40;

    public record FasterMining(boolean enabled, double percentage, List<Material> blocks) {
        public boolean appliesTo(Material material) {
            return enabled && blocks != null && blocks.contains(material);
        }

        /**
         * Tier 1: Haste gestuft in 20%-Schritten (Vanilla-Formel). percentage=25 -> Level 1 (~20%).
         */
        public int hasteLevel() {
            return Math.max(0, (int) Math.round(percentage / 20.0) - 1);
        }
    }

    public record BonusDrops(boolean enabled, double chance, double multiplier, List<Material> blocks) {
        public boolean appliesTo(Material material) {
            return enabled && blocks != null && blocks.contains(material);
        }
    }

    public static class BetterMineMechanicListener implements Listener {

        private static final Map<UUID, Integer> activeHasteLevel = new ConcurrentHashMap<>();

        @EventHandler
        public void onBlockDamage(BlockDamageEvent event) {
            BetterMineMechanic mechanic = resolve(event.getPlayer().getInventory().getItemInMainHand());
            if (mechanic == null || !mechanic.enabled()) {
                return;
            }

            FasterMining fasterMining = mechanic.fasterMining();
            if (fasterMining == null || !fasterMining.appliesTo(event.getBlock().getType())) {
                return;
            }

            applyHaste(event.getPlayer(), fasterMining.hasteLevel());
        }

        @EventHandler
        public void onBlockDropItem(BlockDropItemEvent event) {
            Player player = event.getPlayer();
            BetterMineMechanic mechanic = resolve(player.getInventory().getItemInMainHand());
            if (mechanic == null || !mechanic.enabled()) {
                return;
            }

            BonusDrops bonusDrops = mechanic.bonusDrops();
            if (bonusDrops == null || !bonusDrops.appliesTo(event.getBlockState().getType())) {
                return;
            }
            if (ThreadLocalRandom.current().nextDouble(100) >= bonusDrops.chance()) {
                return;
            }

            int totalExtra = 0;
            ItemStack lastExtraStack = null;

            for (Item drop : event.getItems()) {
                ItemStack stack = drop.getItemStack().clone();
                int extraAmount = (int) Math.round(stack.getAmount() * (bonusDrops.multiplier() - 1));
                if (extraAmount <= 0) {
                    continue;
                }
                stack.setAmount(extraAmount);
                event.getBlock().getWorld().dropItemNaturally(drop.getLocation(), stack);
                totalExtra += extraAmount;
                lastExtraStack = stack;
            }

            if (totalExtra > 0) {
                playBonusFeedback(player, event.getBlock().getLocation(), totalExtra, lastExtraStack);
            }
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            activeHasteLevel.remove(event.getPlayer().getUniqueId());
        }

        private void applyHaste(Player player, int level) {
            PotionEffect current = player.getPotionEffect(PotionEffectType.HASTE);
            if (current != null && current.getAmplifier() > level) {
                // Stärkerer Haste-Effekt (extern oder anderes Item) bereits aktiv - nicht abschwächen
                return;
            }
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.HASTE, HASTE_DURATION_TICKS, level, true, false, false));
            activeHasteLevel.put(player.getUniqueId(), level);
        }

        private void playBonusFeedback(Player player, Location loc, int amount, @Nullable ItemStack stack) {
            // Sound nur für den Spieler selbst, +/- 10% Pitch gegen Wiederholungs-Ermüdung
            player.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS,
                1f, 0.9f + ThreadLocalRandom.current().nextFloat() * 0.2f);

            // Partikel am Block, für alle Nahestehenden sichtbar
            loc.getWorld().spawnParticle(Particle.DUST, loc.clone().add(0.5, 0.7, 0.5), 12,
                0.25, 0.25, 0.25, 0, new Particle.DustOptions(Color.fromRGB(255, 195, 0), 1.2f));

            // Actionbar statt Chat, damit's nicht spammt
            Component itemName = stack != null ? stack.displayName() : Component.text("Item");
            player.sendActionBar(Component.text("✦ ", NamedTextColor.GOLD)
                .append(Component.text("Bonus Drop!", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" +" + amount + " ", NamedTextColor.GRAY))
                .append(itemName.color(NamedTextColor.GRAY)));
        }

        @Nullable
        private static BetterMineMechanic resolve(@Nullable ItemStack item) {
            String id = NexoItems.idFromItem(item);
            if (id == null) {
                return null;
            }
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(id);
            if (mechanics == null) {
                return null;
            }
            return mechanics.getBetterMineMechanic();
        }
    }
}
