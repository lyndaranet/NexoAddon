package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import java.util.Set;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

public record LifeSteal(double percentage, double minHeal, double maxHeal, boolean affectUndead) {

    public static class LifeStealListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEntityDamage(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player player)) {
                return;
            }
            if (!(event.getEntity() instanceof LivingEntity victim)) {
                return;
            }

            ItemStack weapon = player.getEquipment().getItemInMainHand();
            String nexoItemId = NexoItems.idFromItem(weapon);
            if (nexoItemId == null) {
                return;
            }

            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(nexoItemId);
            if (mechanics == null || mechanics.getLifeSteal() == null) {
                return;
            }

            LifeSteal lifeSteal = mechanics.getLifeSteal();

            // Check if mechanic affects undead mobs
            if (!lifeSteal.affectUndead() && isUndead(victim.getType())) {
                return;
            }

            // Calculate heal amount based on damage dealt
            double damage = event.getFinalDamage();
            double healAmount = damage * (lifeSteal.percentage() / 100.0);

            // Apply min/max limits
            if (lifeSteal.minHeal() > 0) {
                healAmount = Math.max(healAmount, lifeSteal.minHeal());
            }
            if (lifeSteal.maxHeal() > 0) {
                healAmount = Math.min(healAmount, lifeSteal.maxHeal());
            }

            // Apply healing to player
            double currentHealth = player.getHealth();
            AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealthAttr == null) {
                return;
            }
            double maxHealth = maxHealthAttr.getValue();
            double newHealth = Math.min(currentHealth + healAmount, maxHealth);

            player.setHealth(newHealth);
        }

        private static final Set<EntityType> UNDEAD_ENTITIES = Set.of(
            EntityType.ZOMBIE,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.HUSK,
            EntityType.DROWNED,
            EntityType.SKELETON,
            EntityType.STRAY,
            EntityType.WITHER_SKELETON,
            EntityType.PHANTOM,
            EntityType.WITHER,
            EntityType.ZOGLIN,
            EntityType.ZOMBIFIED_PIGLIN
        );

        private static boolean isUndead(EntityType type) {
            return UNDEAD_ENTITIES.contains(type);
        }
    }
}
