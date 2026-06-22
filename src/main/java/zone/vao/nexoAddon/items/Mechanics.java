package zone.vao.nexoAddon.items;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.mechanics.*;
import zone.vao.nexoAddon.items.mechanics.InfiniteBucket;

import java.util.List;
import java.util.Map;

@Getter
public class Mechanics {

    private final String id;
    private Repair repair;
    private BigMining bigMining;
    private VeinMiner veinMiner;
    private Timber timber;
    private BedrockBreak bedrockBreak;
    private Aura aura;
    private SpawnerBreak spawnerBreak;
    private MiningTools miningTools;
    private DropExperience dropExperience;
    private Infested infested;
    private KillMessage killMessage;
    private Stackable stackable;
    private Decay decay;
    private ShiftBlock shiftBlock;
    private BottledExp bottledExp;
    private Unstackable unstackable;
    private BlockAura blockAura;
    private Signal signal;
    private Remember remember;
    private Enchantify enchantify;
    private AutoCatch autoCatch;
    private UniqueId uniqueId;
    private InventoryType inventoryType;
    private Telekinesis telekinesis;
    private LifeSteal lifeSteal;
    private Dash dash;
    private GlassBreaker glassBreaker;
    private SandSmelt sandSmelt;
    private InfiniteBucket infiniteBucket;
    private InfiniteFood infiniteFood;
    private InfiniteShears infiniteShears;
    private InfiniteFluidBucket infiniteFluidBucket;
    private WideHoe wideHoe;
    private Magnet magnet;
    private GrapplingHook grapplingHook;
    private SpiderMan spiderMan;
    private OnHitMechanic onHitMechanic;
    private AreaMiningMechanic areaMiningMechanic;
    private PassiveEffectMechanic passiveEffectMechanic;
    private ConsumableMechanic consumableMechanic;
    private AreaAbilityMechanic areaAbilityMechanic;
    private TeleportMechanic teleportMechanic;
    private BeamMechanic beamMechanic;
    private ParticleAuraMechanic particleAuraMechanic;
    private ProjectileMechanic projectileMechanic;
    private ShapeWaveMechanic shapeWaveMechanic;
    private BowMechanic bowMechanic;
    private DashMechanic dashMechanic;
    private BlockTriggerLaunchMechanic blockTriggerLaunchMechanic;

    public Mechanics(String id) {
        this.id = id;
    }

    public void setRepair(double ration, int fixedAmount, List<Material> materials, List<String> nexoIds,
        List<Material> materialsBlacklist, List<String> nexoIdsBlacklist) {
        this.repair = new Repair(ration, fixedAmount, materials, nexoIds, materialsBlacklist, nexoIdsBlacklist);
    }

  public void setBigMining(int radius, int depth, boolean switchable, List<Material> materials, Sound sound) {
    this.bigMining = new BigMining(radius, depth, switchable, materials, sound);
  }
    public void setBigMining(int radius, int depth, boolean switchable, List<Material> materials) {
        this.bigMining = new BigMining(radius, depth, switchable, materials);
    }

    public void setVeinMiner(int distance, boolean toggleable, boolean sameMaterial, int limit,
        List<Material> materials, List<String> nexoIds) {
        this.veinMiner = new VeinMiner(distance, toggleable, sameMaterial, limit, materials, nexoIds);
    }

  public void setBedrockBreak(int hardness, double probability, int durabilityCost, boolean disableOnFirstLayer, Sound sound) {
    this.bedrockBreak = new BedrockBreak(hardness, probability, durabilityCost, disableOnFirstLayer, sound);
  }
    public void setTimber(int limit, int maxHeight, boolean toggleable, boolean breakLeaves, List<Material> logs,
        List<String> nexoLogs) {
        this.timber = new Timber(limit, maxHeight, toggleable, breakLeaves, logs, nexoLogs);
    }

    public void setBedrockBreak(int hardness, double probability, int durabilityCost, boolean disableOnFirstLayer) {
        this.bedrockBreak = new BedrockBreak(hardness, probability, durabilityCost, disableOnFirstLayer);
    }

    public void setAura(Particle particle, String type, String formula) {
        this.aura = new Aura(particle, type, formula);
    }

    public void setMiningTools(final List<Material> materials, final List<String> nexoIds, final String type) {
        this.miningTools = new MiningTools(materials, nexoIds, type);
    }

    public void setSpawnerBreak(double probability, boolean dropExperience) {
        this.spawnerBreak = new SpawnerBreak(probability, dropExperience);
    }

    public void setDropExperience(double experience) {
        this.dropExperience = new DropExperience(experience);
    }

    public void setInfested(List<EntityType> entities, List<String> mythicMobs, double probability, String selector,
        boolean particles, boolean drop, boolean safeSpawn) {
        this.infested = new Infested(entities, mythicMobs, probability, selector, particles, drop, safeSpawn);
    }

    public void setKillMessage(String deathMessage) {
        this.killMessage = new KillMessage(deathMessage);
    }

    public void setStackable(String next, String group) {
        this.stackable = new Stackable(next, group);
    }

    public void setDecay(int time, double chance, List<Material> base, List<String> nexoBase, int radius) {
        this.decay = new Decay(time, chance, base, nexoBase, radius);
    }

    public void setShiftBlock(String replaceTo, int time, List<Material> materials, List<String> nexoIds,
        boolean onInteract, boolean onDestroy, boolean onPlace) {
        this.shiftBlock = new ShiftBlock(replaceTo, time, materials, nexoIds, onInteract, onDestroy, onPlace);
    }

    public void setBottledExp(Double ration, int cost) {
        this.bottledExp = new BottledExp(ration, cost);
    }

    public void setUnstackable(String next, String give, List<Material> materials, List<String> nexoIds) {
        this.unstackable = new Unstackable(next, give, materials, nexoIds);
    }

    public void setBlockAura(Particle particle, String xOffset, String yOffset, String zOffset, int amount,
        double deltaX, double deltaY, double deltaZ, double speed, boolean force) {
        this.blockAura = new BlockAura(particle, xOffset, yOffset, zOffset, amount, deltaX, deltaY, deltaZ, speed,
            force);
    }

    public void setSignal(int radius, double channel, String role) {
        this.signal = new Signal(radius, channel, role);
    }

    public void setRemember(boolean isForRemember) {
        this.remember = new Remember(isForRemember);
    }

    public void setEnchantify(Map<Enchantment, Integer> enchants, Map<Enchantment, Integer> limits,
        List<Material> materials, List<String> nexoIds, List<Material> materialsBlacklist,
        List<String> nexoIdsBlacklist) {
        this.enchantify = new Enchantify(enchants, limits, materials, nexoIds, materialsBlacklist, nexoIdsBlacklist);
    }

    public void setAutoCatch(boolean toggable, boolean recast) {
        this.autoCatch = new AutoCatch(toggable, recast);
    }

    public void setUniqueId(boolean enabled) {
        this.uniqueId = new UniqueId(enabled);
    }

    public void setInventoryType(org.bukkit.event.inventory.InventoryType inventoryType, Component title) {
        this.inventoryType = new InventoryType(inventoryType, title);
    }

    public void setTelekinesis(boolean enabled, List<Material> materials, List<String> nexoIds) {
        this.telekinesis = new Telekinesis(enabled, materials, nexoIds);
    }

    public void setLifeSteal(double percentage, double minHeal, double maxHeal, boolean affectUndead) {
        this.lifeSteal = new LifeSteal(percentage, minHeal, maxHeal, affectUndead);
    }

    public void setDash(double power, double verticalBoost, int cooldown, boolean requireSneaking,
        String particleType, int particleAmount, String soundType, float soundVolume,
        float soundPitch, String cooldownMessage, int durabilityCost) {
        this.dash = new Dash(power, verticalBoost, cooldown, requireSneaking, particleType, particleAmount,
            soundType, soundVolume, soundPitch, cooldownMessage, durabilityCost);
    }

    public void setGlassBreaker(List<Material> glassTypes, List<String> nexoGlassTypes, boolean enabled,
        int durabilityCost) {
        this.glassBreaker = new GlassBreaker(glassTypes, nexoGlassTypes, enabled, durabilityCost);
    }

    public void setSandSmelt(List<Material> sandTypes, boolean enabled, double probability) {
        this.sandSmelt = new SandSmelt(sandTypes, enabled, probability);
    }

    public void setInfiniteBucket(boolean enabled, int uses) {
        this.infiniteBucket = new InfiniteBucket(enabled, uses);
    }

    public void setInfiniteFood(boolean enabled, int uses) {
        this.infiniteFood = new InfiniteFood(enabled, uses);
    }

    public void setInfiniteShears(boolean enabled, int uses) {
        this.infiniteShears = new InfiniteShears(enabled, uses);
    }

    public void setInfiniteFluidBucket(boolean enabled, int uses, String waterLore, String lavaLore) {
        this.infiniteFluidBucket = new InfiniteFluidBucket(enabled, uses, waterLore, lavaLore);
    }

    public void setWideHoe(int radius, boolean switchable, boolean tillGrass, int durabilityCost) {
        this.wideHoe = new WideHoe(radius, switchable, tillGrass, durabilityCost);
    }

    public void setMagnet(boolean enabled, int radius, double pullSpeed,
        String particleType, int particleAmount, String soundType, float soundVolume, float soundPitch,
        String activeLore, String inactiveLore, boolean plotOnly, boolean trustedPlots, boolean wildernessOnly) {
        this.magnet = new Magnet(enabled, radius, pullSpeed, particleType, particleAmount,
            soundType, soundVolume, soundPitch, activeLore, inactiveLore, plotOnly, trustedPlots, wildernessOnly);
    }

    public void setGrapplingHook(boolean enabled, int maxDistance, double pullSpeed, int cooldown,
        String particleType, int particleAmount, String soundType, float soundVolume, float soundPitch,
        String cooldownMessage, int durabilityCost,
        String ropeColor, float ropeSize, double ropeSpacing) {
        this.grapplingHook = new GrapplingHook(enabled, maxDistance, pullSpeed, cooldown,
            particleType, particleAmount, soundType, soundVolume, soundPitch, cooldownMessage, durabilityCost,
            ropeColor, ropeSize, ropeSpacing);
    }

    public void setSpiderMan(boolean enabled,
        boolean wallClimbEnabled, double climbSpeed, String checkSlot,
        boolean webShotEnabled, int webMaxDistance, double webPullSpeed,
        double arcBoost, int webCooldown,
        String particleType, int particleAmount,
        String soundType, float soundVolume, float soundPitch,
        String cooldownMessage, int durabilityCost) {
        this.spiderMan = new SpiderMan(enabled, wallClimbEnabled, climbSpeed, checkSlot,
            webShotEnabled, webMaxDistance, webPullSpeed, arcBoost, webCooldown,
            particleType, particleAmount, soundType, soundVolume, soundPitch, cooldownMessage, durabilityCost);
    }

    public void setOnHitMechanic(List<OnHitMechanic.OnHitEffect> effects, int cooldownSeconds,
        org.bukkit.Particle particles) {
        this.onHitMechanic = new OnHitMechanic(effects, cooldownSeconds, particles);
    }

    public void setAreaMiningMechanic(AreaMiningMechanic.Shape shape, boolean consumeDurability, int length,
        int maxBlocks, int radius, int depth, List<String> toolTypes, java.util.Set<Material> deniedBlocks) {
        this.areaMiningMechanic = new AreaMiningMechanic(shape, consumeDurability, length, maxBlocks, radius, depth,
            toolTypes, deniedBlocks);
    }

    public void setPassiveEffectMechanic(String slot, int reapplyTicks,
        List<PassiveEffectMechanic.PassivePotionEffect> potionEffects,
        List<PassiveEffectMechanic.PassiveAttributeModifier> attributeModifiers,
        PassiveEffectMechanic.PassiveConditions conditions, org.bukkit.Particle ambientParticle,
        org.bukkit.Sound activateSound, org.bukkit.Sound deactivateSound) {
        this.passiveEffectMechanic = new PassiveEffectMechanic(slot, reapplyTicks, potionEffects, attributeModifiers,
            conditions, ambientParticle, activateSound, deactivateSound);
    }

    public void setConsumableMechanic(String trigger, int cooldownSeconds, boolean consumeItem, double instantHeal,
        double instantDamage, List<ConsumableMechanic.ConsumableEffect> effects, List<String> commands,
        ConsumableMechanic.ConsumableConditions conditions, org.bukkit.Sound sound, org.bukkit.Particle particle,
        String messageSelf, String messageBroadcast) {
        this.consumableMechanic = new ConsumableMechanic(trigger, cooldownSeconds, consumeItem, instantHeal,
            instantDamage, effects, commands, conditions, sound, particle, messageSelf, messageBroadcast);
    }

    public void setAreaAbilityMechanic(String trigger, int cooldownSeconds, double radius, String targets,
        boolean includeSelf, int maxTargets, double healAmount, double damageAmount, double launchVelocity,
        List<AreaAbilityMechanic.AbilityEffect> effects, List<String> commands, double selfHeal, double selfDamage,
        List<AreaAbilityMechanic.AbilityEffect> selfEffects, AreaAbilityMechanic.AbilityConditions conditions,
        org.bukkit.Particle particle, org.bukkit.Particle waveParticle, org.bukkit.Sound sound,
        org.bukkit.Sound soundTarget) {
        this.areaAbilityMechanic = new AreaAbilityMechanic(trigger, cooldownSeconds, radius, targets, includeSelf,
            maxTargets, healAmount, damageAmount, launchVelocity, effects, commands, selfHeal, selfDamage, selfEffects,
            conditions, particle, waveParticle, sound, soundTarget);
    }

    public void setTeleportMechanic(String trigger, String mode, int cooldownSeconds, double distance,
        boolean behindTarget, double arriveDamageRadius, double arriveDamage, double launchVelocity,
        List<AreaAbilityMechanic.AbilityEffect> effects, List<String> commands,
        TeleportMechanic.TeleportConditions conditions, List<TeleportMechanic.ParticleEntry> originParticles,
        List<TeleportMechanic.ParticleEntry> destinationParticles, org.bukkit.Particle trailParticle,
        org.bukkit.Sound soundOrigin, org.bukkit.Sound soundDestination) {
        this.teleportMechanic = new TeleportMechanic(trigger, mode, cooldownSeconds, distance, behindTarget,
            arriveDamageRadius, arriveDamage, launchVelocity, effects, commands, conditions, originParticles,
            destinationParticles, trailParticle, soundOrigin, soundDestination);
    }

    public void setBeamMechanic(String trigger, int cooldownSeconds, double range, double width, double height,
        boolean pierce, boolean pierceBlocks, double damage, double knockback,
        List<AreaAbilityMechanic.AbilityEffect> effects, List<String> commands,
        List<AreaAbilityMechanic.AbilityEffect> selfEffects, double selfDamage,
        BeamMechanic.BeamConditions conditions, List<BeamMechanic.BeamSegment> beamSegments,
        org.bukkit.Particle hitParticle, org.bukkit.Sound sound, org.bukkit.Sound soundHit) {
        this.beamMechanic = new BeamMechanic(trigger, cooldownSeconds, range, width, height, pierce, pierceBlocks,
            damage, knockback, effects, commands, selfEffects, selfDamage, conditions, beamSegments, hitParticle,
            sound, soundHit);
    }

    public void setParticleAuraMechanic(String slot, int intervalTicks,
        ParticleAuraMechanic.AuraConditions conditions, List<ParticleAuraMechanic.AuraLayer> layers) {
        this.particleAuraMechanic = new ParticleAuraMechanic(slot, intervalTicks, conditions, layers);
    }

    public void setProjectileMechanic(String trigger, int cooldownSeconds, double range, double speed,
        double hitRadius, int maxActive, double gravity, int bounces, int pierce, boolean homing,
        double homingRadius, double homingStrength, double damage, double knockback,
        List<AreaAbilityMechanic.AbilityEffect> effects, List<String> commands, List<String> blockCommands,
        double explosionRadius, double explosionDamage, List<AreaAbilityMechanic.AbilityEffect> explosionEffects,
        org.bukkit.Particle explosionParticle, ProjectileMechanic.ProjectileConditions conditions,
        List<ProjectileMechanic.TrailEntry> trail, List<TeleportMechanic.ParticleEntry> impactParticles,
        org.bukkit.Sound soundLaunch, org.bukkit.Sound soundImpact) {
        this.projectileMechanic = new ProjectileMechanic(trigger, cooldownSeconds, range, speed, hitRadius,
            maxActive, gravity, bounces, pierce, homing, homingRadius, homingStrength, damage, knockback, effects,
            commands, blockCommands, explosionRadius, explosionDamage, explosionEffects, explosionParticle,
            conditions, trail, impactParticles, soundLaunch, soundImpact);
    }

    public void setProjectileMechanic(ProjectileMechanic projectileMechanic) {
        this.projectileMechanic = projectileMechanic;
    }

    public void setBowMechanic(BowMechanic bowMechanic) {
        this.bowMechanic = bowMechanic;
    }

    public void setDashMechanic(DashMechanic dashMechanic) {
        this.dashMechanic = dashMechanic;
    }

    public void setShapeWaveMechanic(String trigger, int cooldownSeconds, String shape, int maxTargets,
        double range, double angle, double radius, double height, double minRadius, int rays, double arcDegrees,
        double damage, int fireDurationSeconds, double knockback, List<AreaAbilityMechanic.AbilityEffect> effects,
        List<String> commands, List<AreaAbilityMechanic.AbilityEffect> selfEffects, double selfDamage,
        double selfHeal, ShapeWaveMechanic.ShapeConditions conditions, double particleDensity, boolean animate,
        int animateTicks, List<ProjectileMechanic.TrailEntry> fillParticles, org.bukkit.Sound sound,
        org.bukkit.Sound soundHit) {
        this.shapeWaveMechanic = new ShapeWaveMechanic(trigger, cooldownSeconds, shape, maxTargets, range, angle,
            radius, height, minRadius, rays, arcDegrees, damage, fireDurationSeconds, knockback, effects, commands,
            selfEffects, selfDamage, selfHeal, conditions, particleDensity, animate, animateTicks, fillParticles,
            sound, soundHit);
    }

    public void setBlockTriggerLaunchMechanic(int activationCooldownSeconds, int durationSeconds,
        double perLaunchCooldownSeconds, java.util.Set<Material> triggerBlocks, int checkBlockOffset,
        double launchPower, double horizontalPower, String horizontalSource,
        List<AreaAbilityMechanic.AbilityEffect> activeEffects, List<AreaAbilityMechanic.AbilityEffect> launchEffects,
        int noFallDamageTicks, List<ParticleAuraMechanic.AuraLayer> auraLayers,
        List<BlockTriggerLaunchMechanic.LaunchParticle> launchParticles, org.bukkit.Sound launchSound,
        List<BlockTriggerLaunchMechanic.LaunchParticle> activateParticles, org.bukkit.Sound activateSound,
        boolean showActionBar) {
        this.blockTriggerLaunchMechanic = new BlockTriggerLaunchMechanic(activationCooldownSeconds, durationSeconds,
            perLaunchCooldownSeconds, triggerBlocks, checkBlockOffset, launchPower, horizontalPower, horizontalSource,
            activeEffects, launchEffects, noFallDamageTicks, auraLayers, launchParticles, launchSound,
            activateParticles, activateSound, showActionBar);
    }

    public static void registerListeners(NexoAddon plugin) {

        registerListener(new AutoCatch.AutoCatchListener(), plugin);

        registerListener(new BigMining.BigMiningListener(), plugin);
        registerListener(new BlockAura.BlockAuraListener(), plugin);
        registerListener(new BottledExp.BottledExpListener(), plugin);

        registerListener(new Dash.DashListener(), plugin);
        registerListener(new Decay.DecayListener(), plugin);
        registerListener(new DropExperience.DropExperienceListener(), plugin);

        registerListener(new Enchantify.EnchantifyListener(), plugin);

        registerListener(new GlassBreaker.GlassBreakerListener(), plugin);

        registerListener(new Infested.InfestedListener(), plugin);
        registerListener(new InventoryType.InventoryTypeListener(), plugin);

        registerListener(new KillMessage.KillMessageListener(), plugin);

        registerListener(new LifeSteal.LifeStealListener(), plugin);

        registerListener(new MiningTools.MiningToolsListener(), plugin);

        registerListener(new UniqueId.UniqueIdListener(), plugin);

        registerListener(new Remember.RememberListener(), plugin);
        registerListener(new Repair.RepairListener(), plugin);

        registerListener(new ShiftBlock.ShiftBlockListener(), plugin);
        registerListener(new SpawnerBreak.SpawnerBreakListener(), plugin);
        registerListener(new Stackable.StackableListener(), plugin);

        registerListener(new Telekinesis.TelekinesisListener(), plugin);
        registerListener(new Timber.TimberListener(), plugin);

        registerListener(new Unstackable.UnstackableListener(), plugin);

        registerListener(new Signal.SignalListener(), plugin);
        registerListener(new VeinMiner.VeinMinerListener(), plugin);
        registerListener(new SandSmelt.SandSmeltListener(), plugin);
        registerListener(new InfiniteBucket.InfiniteBucketListener(), plugin);
        registerListener(new InfiniteFood.InfiniteFoodListener(), plugin);
        registerListener(new InfiniteShears.InfiniteShearsListener(), plugin);
        registerListener(new InfiniteFluidBucket.InfiniteFluidBucketListener(), plugin);
        registerListener(new WideHoe.WideHoeListener(), plugin);

        Magnet.MagnetListener magnetListener = new Magnet.MagnetListener();
        registerListener(magnetListener, plugin);
        Magnet.MagnetListener.startMagnetTask();

        registerListener(new GrapplingHook.GrapplingHookListener(), plugin);

        SpiderMan.SpiderManListener spiderManListener = new SpiderMan.SpiderManListener();
        registerListener(spiderManListener, plugin);
        SpiderMan.SpiderManListener.startClimbTask();

        registerListener(new OnHitMechanic.OnHitMechanicListener(), plugin);
        registerListener(new AreaMiningMechanic.AreaMiningMechanicListener(), plugin);
        registerListener(new PassiveEffectMechanic.PassiveEffectMechanicListener(), plugin);
        registerListener(new ConsumableMechanic.ConsumableMechanicListener(), plugin);
        registerListener(new AreaAbilityMechanic.AreaAbilityMechanicListener(), plugin);
        registerListener(new TeleportMechanic.TeleportMechanicListener(), plugin);
        registerListener(new BeamMechanic.BeamMechanicListener(), plugin);
        registerListener(new ParticleAuraMechanic.ParticleAuraMechanicListener(), plugin);
        registerListener(new ProjectileMechanic.ProjectileMechanicListener(), plugin);
        ProjectileMechanic.ProjectileMechanicListener.startProjectileTask();
        registerListener(new ShapeWaveMechanic.ShapeWaveMechanicListener(), plugin);
        registerListener(new BowMechanic.BowMechanicListener(), plugin);
        registerListener(new DashMechanic.DashMechanicListener(), plugin);
        DashMechanic.DashMechanicListener.startRechargeTask();
        registerListener(new BlockTriggerLaunchMechanic.BlockTriggerLaunchMechanicListener(), plugin);
    }

    private static void registerListener(Listener listener, NexoAddon plugin) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
}