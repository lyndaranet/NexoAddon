package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Generic area-of-effect ability — heals/damages/buffs nearby entities (plus self-actions)
 * when the player triggers the item. One config block ({@code Mechanics.area_ability}).
 */
public record AreaAbilityMechanic(String trigger, int cooldownSeconds, double radius, String targets,
                                  boolean includeSelf, int maxTargets, double healAmount, double damageAmount,
                                  double launchVelocity, List<AbilityEffect> effects, List<String> commands,
                                  double selfHeal, double selfDamage, List<AbilityEffect> selfEffects,
                                  AbilityConditions conditions, @Nullable Particle particle,
                                  @Nullable Particle waveParticle, @Nullable Sound sound,
                                  @Nullable Sound soundTarget) {

    public static final String TRIGGER_RIGHT_CLICK = "right_click";
    public static final String TRIGGER_SHIFT_RIGHT_CLICK = "shift_right_click";
    public static final String TRIGGER_LEFT_CLICK = "left_click";
    public static final String TRIGGER_ON_HIT = "on_hit";

    public record AbilityEffect(PotionEffectType type, int amplifier, int durationSeconds, double chance) {
    }

    public record AbilityConditions(boolean requireSneaking, int requireHealthBelowPercent,
                                    @Nullable String requirePermission, int minTargetsRequired) {
    }

    public static class AreaAbilityMechanicListener implements Listener {

        private static final Map<UUID, Long> cooldowns = new HashMap<>();
        // Guards against the on_hit trigger re-entering itself when it deals damage to targets.
        private static final Set<UUID> processing = new HashSet<>();

        @EventHandler
        public void onInteract(PlayerInteractEvent event) {
            if (event.getHand() != EquipmentSlot.HAND) {
                return;
            }
            Action action = event.getAction();
            boolean right = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
            boolean left = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
            if (!right && !left) {
                return;
            }

            Player player = event.getPlayer();
            AreaAbilityMechanic mechanic = resolve(event.getItem());
            if (mechanic == null) {
                return;
            }

            switch (mechanic.trigger()) {
                case TRIGGER_RIGHT_CLICK -> {
                    if (!right) return;
                }
                case TRIGGER_SHIFT_RIGHT_CLICK -> {
                    if (!right || !player.isSneaking()) return;
                }
                case TRIGGER_LEFT_CLICK -> {
                    if (!left) return;
                }
                default -> {
                    return;
                }
            }

            handleAbility(player, mechanic, player.getLocation());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEntityDamage(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player player)) {
                return;
            }
            if (!(event.getEntity() instanceof LivingEntity hit)) {
                return;
            }

            AreaAbilityMechanic mechanic = resolve(player.getEquipment().getItemInMainHand());
            if (mechanic == null || !TRIGGER_ON_HIT.equals(mechanic.trigger())) {
                return;
            }

            // Center the area on the struck entity, not the caster.
            handleAbility(player, mechanic, hit.getLocation());
        }

        // --- Core ------------------------------------------------------------

        private static void handleAbility(Player player, AreaAbilityMechanic mechanic, Location center) {
            UUID playerId = player.getUniqueId();
            if (!processing.add(playerId)) {
                return;
            }
            try {
                if (!conditionsMet(player, mechanic.conditions())) {
                    actionBar(player, "<red>Du kannst das gerade nicht benutzen.");
                    return;
                }

                long now = System.currentTimeMillis();
                long remainingMs = remainingCooldown(playerId, mechanic.cooldownSeconds(), now);
                if (remainingMs > 0) {
                    long remainingSeconds = (remainingMs + 999) / 1000;
                    actionBar(player, "<red>Noch <bold>" + remainingSeconds + "s</bold> Cooldown!");
                    return;
                }

                List<LivingEntity> selected = collectTargets(player, mechanic, center);

                if (selected.size() < mechanic.conditions().minTargetsRequired()) {
                    // Not enough valid targets — fizzle silently, no cooldown consumed.
                    return;
                }

                for (LivingEntity target : selected) {
                    applyToTarget(player, target, mechanic);
                }

                applyToSelf(player, mechanic);

                if (mechanic.waveParticle() != null) {
                    playWave(player.getLocation(), mechanic.waveParticle(), mechanic.radius());
                }
                if (mechanic.sound() != null) {
                    player.playSound(player.getLocation(), mechanic.sound(), 1.0f, 1.0f);
                }

                if (mechanic.cooldownSeconds() > 0) {
                    cooldowns.put(playerId, now);
                }
            } finally {
                processing.remove(playerId);
            }
        }

        // --- Target collection ----------------------------------------------

        private static List<LivingEntity> collectTargets(Player player, AreaAbilityMechanic mechanic,
            Location center) {
            List<LivingEntity> candidates = new ArrayList<>();
            for (LivingEntity entity : center.getWorld().getNearbyLivingEntities(center, mechanic.radius())) {
                boolean isSelf = entity.getUniqueId().equals(player.getUniqueId());
                if (isSelf && !mechanic.includeSelf()) {
                    continue;
                }
                if (!matchesTargetMode(player, entity, mechanic.targets(), isSelf)) {
                    continue;
                }
                candidates.add(entity);
            }

            candidates.sort(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(center)));
            if (mechanic.maxTargets() > 0 && candidates.size() > mechanic.maxTargets()) {
                return new ArrayList<>(candidates.subList(0, mechanic.maxTargets()));
            }
            return candidates;
        }

        private static boolean matchesTargetMode(Player player, LivingEntity entity, String mode, boolean isSelf) {
            return switch (mode) {
                case "players" -> entity instanceof Player;
                case "allies" -> entity instanceof Player && sameTeam(player, entity);
                case "enemies" -> !isSelf && !sameTeam(player, entity);
                default -> true; // "all"
            };
        }

        private static boolean sameTeam(Player player, LivingEntity entity) {
            Team a = teamOf(player);
            if (a == null) {
                return false;
            }
            return a.equals(teamOf(entity));
        }

        @Nullable
        private static Team teamOf(LivingEntity entity) {
            Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
            String entry = (entity instanceof Player p) ? p.getName() : entity.getUniqueId().toString();
            return board.getEntryTeam(entry);
        }

        // --- Per-target actions ---------------------------------------------

        private static void applyToTarget(Player player, LivingEntity target, AreaAbilityMechanic mechanic) {
            if (mechanic.healAmount() > 0) {
                heal(target, mechanic.healAmount());
            }
            if (mechanic.damageAmount() > 0) {
                target.damage(mechanic.damageAmount(), player);
            }
            applyEffects(target, mechanic.effects());
            if (mechanic.launchVelocity() > 0) {
                Vector dir = target.getLocation().toVector().subtract(player.getLocation().toVector());
                if (dir.lengthSquared() > 1.0e-6) {
                    target.setVelocity(target.getVelocity().add(dir.normalize().multiply(mechanic.launchVelocity())));
                }
            }
            runCommands(player, target, mechanic.commands());
            if (mechanic.soundTarget() != null) {
                target.getWorld().playSound(target.getLocation(), mechanic.soundTarget(), 1.0f, 1.0f);
            }
            if (mechanic.particle() != null) {
                target.getWorld().spawnParticle(mechanic.particle(),
                    target.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.0);
            }
        }

        private static void applyToSelf(Player player, AreaAbilityMechanic mechanic) {
            if (mechanic.selfHeal() > 0) {
                heal(player, mechanic.selfHeal());
            }
            if (mechanic.selfDamage() > 0) {
                player.damage(mechanic.selfDamage());
            }
            applyEffects(player, mechanic.selfEffects());
        }

        // --- Shared helpers --------------------------------------------------

        private static void heal(LivingEntity entity, double amount) {
            double max = 20.0;
            AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null && maxHealth.getValue() > 0) {
                max = maxHealth.getValue();
            }
            entity.setHealth(Math.min(entity.getHealth() + amount, max));
        }

        private static void applyEffects(LivingEntity entity, List<AbilityEffect> effects) {
            for (AbilityEffect effect : effects) {
                if (effect.chance() < 1.0 && Math.random() >= effect.chance()) {
                    continue;
                }
                entity.addPotionEffect(new PotionEffect(
                    effect.type(), effect.durationSeconds() * 20, effect.amplifier()));
            }
        }

        private static void runCommands(Player player, LivingEntity target, List<String> commands) {
            if (commands.isEmpty()) {
                return;
            }
            String name = player.getName();
            String targetName = target.getName();
            for (String raw : commands) {
                String command = raw.replace("{player}", name).replace("{target}", targetName);
                NexoAddon.getInstance().getFoliaLib().getScheduler()
                    .runNextTick(t -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
            }
        }

        private static boolean conditionsMet(Player player, AbilityConditions c) {
            if (c.requireSneaking() && !player.isSneaking()) {
                return false;
            }
            if (c.requirePermission() != null && !c.requirePermission().isEmpty()
                && !player.hasPermission(c.requirePermission())) {
                return false;
            }
            if (c.requireHealthBelowPercent() < 100) {
                double max = 20.0;
                AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealth != null && maxHealth.getValue() > 0) {
                    max = maxHealth.getValue();
                }
                double healthPercent = (player.getHealth() / max) * 100.0;
                if (healthPercent > c.requireHealthBelowPercent()) {
                    return false;
                }
            }
            return true;
        }

        private static long remainingCooldown(UUID playerId, int cooldownSeconds, long now) {
            if (cooldownSeconds <= 0) {
                return 0;
            }
            Long lastUse = cooldowns.get(playerId);
            if (lastUse == null) {
                return 0;
            }
            long elapsed = now - lastUse;
            long total = cooldownSeconds * 1000L;
            return elapsed >= total ? 0 : total - elapsed;
        }

        // --- Wave animation --------------------------------------------------

        private static void playWave(Location origin, Particle waveParticle, double radius) {
            Location center = origin.clone();
            double[] current = {0.5};
            NexoAddon.getInstance().getFoliaLib().getScheduler().runAtLocationTimer(center, task -> {
                double r = current[0];
                if (r > radius) {
                    task.cancel();
                    return;
                }
                int points = Math.max(8, (int) (r * 8));
                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI * i / points;
                    Location point = center.clone().add(Math.cos(angle) * r, 0.5, Math.sin(angle) * r);
                    center.getWorld().spawnParticle(waveParticle, point, 1, 0.0, 0.0, 0.0, 0.0);
                }
                current[0] += 0.5;
            }, 0L, 2L);
        }

        private static void actionBar(Player player, String miniMessage) {
            player.sendActionBar(MiniMessage.miniMessage().deserialize(miniMessage));
        }

        @Nullable
        private static AreaAbilityMechanic resolve(@Nullable ItemStack item) {
            String id = NexoItems.idFromItem(item);
            if (id == null) {
                return null;
            }
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(id);
            if (mechanics == null) {
                return null;
            }
            return mechanics.getAreaAbilityMechanic();
        }
    }
}
