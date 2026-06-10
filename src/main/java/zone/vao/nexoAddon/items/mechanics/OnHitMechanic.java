package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.protectionlib.ProtectionLib;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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

public record OnHitMechanic(List<OnHitEffect> effects, int cooldownSeconds, @Nullable Particle particles) {

    public record OnHitEffect(PotionEffectType type, int amplifier, int durationSeconds) {
    }

    public static class OnHitMechanicListener implements Listener {

        private static final Map<UUID, Long> cooldowns = new HashMap<>();

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEntityDamage(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player player)) {
                return;
            }
            if (!(event.getEntity() instanceof LivingEntity target)) {
                return;
            }

            ItemStack weapon = player.getEquipment().getItemInMainHand();
            String nexoItemId = NexoItems.idFromItem(weapon);
            if (nexoItemId == null) {
                return;
            }

            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(nexoItemId);
            if (mechanics == null || mechanics.getOnHitMechanic() == null) {
                return;
            }

            OnHitMechanic onHit = mechanics.getOnHitMechanic();
            if (onHit.effects().isEmpty()) {
                return;
            }

            // Respect region protection — if the player can't interact here, deal damage but no effects
            if (!ProtectionLib.canInteract(player, target.getLocation())) {
                return;
            }

            // Cooldown is per-player: while active, the hit deals normal damage but applies no effects
            UUID playerId = player.getUniqueId();
            long now = System.currentTimeMillis();
            Long lastUse = cooldowns.get(playerId);
            if (lastUse != null && (now - lastUse) < onHit.cooldownSeconds() * 1000L) {
                return;
            }

            for (OnHitEffect effect : onHit.effects()) {
                target.addPotionEffect(new PotionEffect(
                    effect.type(), effect.durationSeconds() * 20, effect.amplifier()));
            }

            if (onHit.particles() != null) {
                Location loc = target.getLocation().add(0, target.getHeight() / 2, 0);
                target.getWorld().spawnParticle(onHit.particles(), loc, 15, 0.3, 0.5, 0.3, 0.0);
            }

            cooldowns.put(playerId, now);
        }
    }
}
