package zone.vao.nexoAddon.utils;

import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Components;
import zone.vao.nexoAddon.items.Mechanics;
import zone.vao.nexoAddon.items.mechanics.AreaAbilityMechanic;
import zone.vao.nexoAddon.items.mechanics.AreaMiningMechanic;
import zone.vao.nexoAddon.items.mechanics.BlockTriggerLaunchMechanic;
import zone.vao.nexoAddon.items.mechanics.BeamMechanic;
import zone.vao.nexoAddon.items.mechanics.BowMechanic;
import zone.vao.nexoAddon.items.mechanics.ConsumableMechanic;
import zone.vao.nexoAddon.items.mechanics.DashMechanic;
import zone.vao.nexoAddon.items.mechanics.OnHitMechanic;
import zone.vao.nexoAddon.items.mechanics.ParticleAuraMechanic;
import zone.vao.nexoAddon.items.mechanics.ProjectileMechanic;
import zone.vao.nexoAddon.items.mechanics.ShapeWaveMechanic;
import zone.vao.nexoAddon.items.mechanics.TeleportMechanic;
import zone.vao.nexoAddon.items.mechanics.PassiveEffectMechanic;

import java.io.File;
import java.util.*;

public class ItemConfigUtil {

    private static final Set<File> itemFiles = new HashSet<>();

    public static Set<File> getItemFiles() {
        itemFiles.clear();
        itemFiles.addAll(NexoItems.itemMap().keySet());
        return itemFiles;
    }

    public static void loadComponents() {
        NexoAddon.getInstance().getComponents().clear();

        for (File itemFile : getItemFiles()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(itemFile);

            config.getKeys(false).forEach(itemId -> {
                ConfigurationSection itemSection = config.getConfigurationSection(itemId);
                if (itemSection == null || !itemSection.contains("Components")) {
                    return;
                }

                Components component = NexoAddon.getInstance().getComponents()
                    .computeIfAbsent(itemId, Components::new);

                loadEquippableComponent(itemSection, component);
                loadFertilizerComponent(itemSection, component);
                loadSkullValueComponent(itemSection, component);
                loadNoteBlockSoundComponent(itemSection, component);
            });
        }
    }

    private static void loadEquippableComponent(ConfigurationSection section, Components component) {
        if (section.contains("Components.equippable")) {
            try {
                EquipmentSlot slot = EquipmentSlot.valueOf(
                    section.getString("Components.equippable.slot", "HEAD").toUpperCase()
                );
                component.setEquippable(slot);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private static void loadFertilizerComponent(ConfigurationSection section, Components component) {
        if (section.contains("Components.fertilizer.growth_speedup") && section.contains(
            "Components.fertilizer.usable_on")) {
            int growthSpeedup = section.getInt("Components.fertilizer.growth_speedup", 1000);
            List<String> usableOn = section.getStringList("Components.fertilizer.usable_on");
            component.setFertilizer(growthSpeedup, usableOn, section.getInt("Components.fertilizer.cooldown", 0));
        }
    }

    private static void loadSkullValueComponent(ConfigurationSection section, Components component) {
        if (section.contains("Components.skull_value")) {
            String value = section.getString("Components.skull_value", SkullUtil.NEXOADDON_HEAD_BASE64);
            component.setSkullValue(value);
        }
    }

    private static void loadNoteBlockSoundComponent(ConfigurationSection section, Components component) {
        if (section.contains("Components.note_block_sound")) {
            String soundId = section.getString("Components.note_block_sound");
            component.setNoteBlockSound(soundId);
        }
    }


    public static void loadMechanics() {
        NexoAddon.getInstance().getMechanics().clear();

        for (File itemFile : getItemFiles()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(itemFile);

            config.getKeys(false).forEach(itemId -> {
                ConfigurationSection itemSection = config.getConfigurationSection(itemId);
                if (itemSection == null || !itemSection.contains("Mechanics")) {
                    return;
                }

                Mechanics mechanic = NexoAddon.getInstance().getMechanics()
                    .computeIfAbsent(itemId, Mechanics::new);

                loadRepairMechanic(itemSection, mechanic);
                loadBigMiningMechanic(itemSection, mechanic);
                loadVeinMinerMechanic(itemSection, mechanic);
                loadTimberMechanic(itemSection, mechanic);
                loadBedrockBreakMechanic(itemSection, mechanic);
                loadAuraMechanic(itemSection, mechanic);
                loadSpawnerBreak(itemSection, mechanic);
                loadMiningToolsMechanic(itemSection, mechanic);
                loadDropExperienceMechanic(itemSection, mechanic);
                loadInfested(itemSection, mechanic);
                loadKillMessage(itemSection, mechanic);
                loadStackableMechanic(itemSection, mechanic);
                loadDecayMechanic(itemSection, mechanic);
                loadShiftBlockMechanic(itemSection, mechanic);
                loadBottledExpMechanic(itemSection, mechanic);
                loadUnstackableMechanic(itemSection, mechanic);
                loadBlockAuraMechanic(itemSection, mechanic);
                loadSignalMechanic(itemSection, mechanic);
                loadRememberMechanic(itemSection, mechanic);
                loadEnchantifyMechanic(itemSection, mechanic);
                loadAutoCatchMechanic(itemSection, mechanic);
                loadUniqueIdMechanic(itemSection, mechanic);
                loadInventoryType(itemSection, mechanic);
                loadTelekinessMechanic(itemSection, mechanic);
                loadLifeStealMechanic(itemSection, mechanic);
                loadDashMechanic(itemSection, mechanic);
                loadGlassBreakerMechanic(itemSection, mechanic);
                loadSandSmeltMechanic(itemSection, mechanic);
                loadInfiniteBucketMechanic(itemSection, mechanic);
                loadInfiniteFoodMechanic(itemSection, mechanic);
                loadInfiniteShearsMechanic(itemSection, mechanic);
                loadInfiniteFluidBucketMechanic(itemSection, mechanic);
                loadWideHoeMechanic(itemSection, mechanic);
                loadMagnetMechanic(itemSection, mechanic);
                loadGrapplingHookMechanic(itemSection, mechanic);
                loadSpiderManMechanic(itemSection, mechanic);
                loadOnHitMechanic(itemSection, mechanic);
                loadAreaMiningMechanic(itemSection, mechanic);
                loadPassiveEffectMechanic(itemSection, mechanic);
                loadConsumableMechanic(itemSection, mechanic);
                loadAreaAbilityMechanic(itemSection, mechanic);
                loadTeleportMechanic(itemSection, mechanic);
                loadBeamMechanic(itemSection, mechanic);
                loadParticleAuraMechanic(itemSection, mechanic);
                loadProjectileMechanic(itemSection, mechanic);
                loadShapeWaveMechanic(itemSection, mechanic);
                loadBowMechanic(itemSection, mechanic);
                loadDashAbilityMechanic(itemSection, mechanic);
                loadBlockTriggerLaunchMechanic(itemSection, mechanic);
            });
        }
    }

    private static void loadRepairMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (section.contains("Mechanics.repair.ratio") || section.contains("Mechanics.repair.fixed_amount")) {
            double ratio = section.getDouble("Mechanics.repair.ratio");
            int fixedAmount = section.getInt("Mechanics.repair.fixed_amount");
            List<String> rawItems = section.getStringList("Mechanics.repair.whitelist");
            List<String> rawItemsBlacklist = section.getStringList("Mechanics.repair.blacklist");
            List<Material> materials = new ArrayList<>();
            List<String> nexoIds = new ArrayList<>();
            List<Material> materialsBlacklist = new ArrayList<>();
            List<String> nexoIdsBlacklist = new ArrayList<>();
            if (!rawItems.isEmpty()) {
                for (String rawItem : rawItems) {
                    if (Material.matchMaterial(rawItem) != null) {
                        materials.add(Material.matchMaterial(rawItem));
                        continue;
                    }
                    if (NexoItems.itemFromId(rawItem) != null) {
                        nexoIds.add(rawItem);
                    }
                }
            }
            if (!rawItemsBlacklist.isEmpty()) {
                for (String rawItem : rawItemsBlacklist) {
                    if (Material.matchMaterial(rawItem) != null) {
                        materialsBlacklist.add(Material.matchMaterial(rawItem));
                        continue;
                    }
                    if (NexoItems.itemFromId(rawItem) != null) {
                        nexoIdsBlacklist.add(rawItem);
                    }
                }
            }

            mechanic.setRepair(ratio, fixedAmount, materials, nexoIds, materialsBlacklist, nexoIdsBlacklist);
        }
    }

    private static void loadBigMiningMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (section.contains("Mechanics.bigmining.radius") && section.contains("Mechanics.bigmining.depth")) {
            int radius = section.getInt("Mechanics.bigmining.radius", 1);
            int depth = section.getInt("Mechanics.bigmining.depth", 1);
            boolean switchable = section.getBoolean("Mechanics.bigmining.switchable", false);
            List<Material> materials = new ArrayList<>();
            for (String s : section.getStringList("Mechanics.bigmining.materials")) {
                Material material = Material.matchMaterial(s);
                if (material != null) {
                    materials.add(material);
                }
            }

            mechanic.setBigMining(radius, depth, switchable, materials);
        }
    }

    private static void loadVeinMinerMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (section.contains("Mechanics.veinminer.distance")) {
            int distance = section.getInt("Mechanics.veinminer.distance", 10);
            boolean toggleable = section.getBoolean("Mechanics.veinminer.toggleable", false);
            boolean sameMaterial = section.getBoolean("Mechanics.veinminer.same_material", true);
            int limit = section.getInt("Mechanics.veinminer.limit", 10);

            List<Material> materials = new ArrayList<>();
            List<String> nexoIds = new ArrayList<>();

            for (String s : section.getStringList("Mechanics.veinminer.whitelist")) {
                Material material = Material.matchMaterial(s);
                if (material != null) {
                    materials.add(material);
                } else if (NexoItems.itemFromId(s) != null) {
                    nexoIds.add(s);
                }
            }

            mechanic.setVeinMiner(distance, toggleable, sameMaterial, limit, materials, nexoIds);
        }
    }

    private static void loadTimberMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (section.contains("Mechanics.timber.limit")) {
            int limit = section.getInt("Mechanics.timber.limit", 100);
            int maxHeight = section.getInt("Mechanics.timber.max_height", 32);
            boolean toggleable = section.getBoolean("Mechanics.timber.toggleable", false);
            boolean breakLeaves = section.getBoolean("Mechanics.timber.break_leaves", false);

            List<Material> logs = new ArrayList<>();
            List<String> nexoLogs = new ArrayList<>();

            for (String s : section.getStringList("Mechanics.timber.whitelist")) {
                Material material = Material.matchMaterial(s);
                if (material != null) {
                    logs.add(material);
                } else if (NexoItems.itemFromId(s) != null) {
                    nexoLogs.add(s);
                }
            }

            mechanic.setTimber(limit, maxHeight, toggleable, breakLeaves, logs, nexoLogs);
        }
    }

    private static void loadBedrockBreakMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (section.contains("Mechanics.bedrockbreak.hardness") && section.contains(
            "Mechanics.bedrockbreak.probability")) {
            int hardness = section.getInt("Mechanics.bedrockbreak.hardness");
            double probability = section.getDouble("Mechanics.bedrockbreak.probability");
            int durabilityCost = section.getInt("Mechanics.bedrockbreak.durability_cost", 1);
            boolean disableOnFirstLayer = section.getBoolean("Mechanics.bedrockbreak.disable_on_first_layer", true);
            mechanic.setBedrockBreak(hardness, probability, durabilityCost, disableOnFirstLayer);
        }
    }

    private static void loadAuraMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (section.contains("Mechanics.aura.type") && section.contains("Mechanics.aura.particle")) {
            Particle particle = Particle.valueOf(section.getString("Mechanics.aura.particle", "FLAME").toUpperCase());
            String type = section.getString("Mechanics.aura.type");
            String customFormula = section.getString("Mechanics.aura.custom", null);
            mechanic.setAura(particle, type, customFormula);
        }
    }

    private static void loadSpawnerBreak(ConfigurationSection section, Mechanics mechanic) {
        if (section.contains("Mechanics.spawnerbreak.probability")) {
            double probability = section.getDouble("Mechanics.spawnerbreak.probability");
            boolean dropExperience = section.getBoolean("Mechanics.spawnerbreak.dropExperience", false);
            mechanic.setSpawnerBreak(probability, dropExperience);
        }
    }

    private static void loadMiningToolsMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (section.contains("Mechanics.custom_block.miningtools.items")) {
            List<String> values = section.getStringList("Mechanics.custom_block.miningtools.items");
            List<Material> materials = new ArrayList<>();
            List<String> nexoIds = new ArrayList<>();

            for (String value : values) {
                Material material = Material.matchMaterial(value);
                if (material != null) {
                    materials.add(material);
                }
                if (NexoItems.itemFromId(value) != null) {
                    nexoIds.add(value);
                }
            }

            mechanic.setMiningTools(materials, nexoIds,
                section.getString("Mechanics.custom_block.miningtools.type", "CANCEL_EVENT"));
        }
    }

    private static void loadDropExperienceMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (section.contains("Mechanics.custom_block.drop.experience")) {
            double experience = section.getDouble("Mechanics.custom_block.drop.experience", 0.0);
            mechanic.setDropExperience(experience);
        }
    }

    private static void loadInfested(ConfigurationSection section, Mechanics mechanic) {
        if (section.contains("Mechanics.custom_block.infested.entities") || section.contains(
            "Mechanics.custom_block.infested.mythic-mobs")) {
            List<String> values = section.getStringList("Mechanics.custom_block.infested.entities");
            List<EntityType> entities = new ArrayList<>();
            for (String value : values) {
                try {
                    EntityType entityType = EntityType.valueOf(value.toUpperCase());
                    entities.add(entityType);
                } catch (IllegalArgumentException e) {
                    NexoAddon.getInstance().getLogger().info("Invalid EntityType: " + value);
                }
            }

            List<String> mythicMobs = List.of();
            if (NexoAddon.getInstance().isMythicMobsLoaded() && section.contains(
                "Mechanics.custom_block.infested.mythic-mobs")) {
                mythicMobs = section.getStringList("Mechanics.custom_block.infested.mythic-mobs");
            } else if (!NexoAddon.getInstance().isMythicMobsLoaded() && section.contains(
                "Mechanics.custom_block.infested.mythic-mobs")) {
                NexoAddon.getInstance().getLogger()
                    .warning("MythicMobs is not loaded! Skipping `infested.mythic-mobs`");
            }

            double probability = section.getDouble("Mechanics.custom_block.infested.probability", 1.0);
            String selector = section.getString("Mechanics.custom_block.infested.selector", "all");
            boolean particles = section.getBoolean("Mechanics.custom_block.infested.particles", false);
            boolean drop = section.getBoolean("Mechanics.custom_block.infested.drop-loot", true);
            boolean safeSpawn = section.getBoolean("Mechanics.custom_block.infested.safe-spawn", false);

            mechanic.setInfested(entities, mythicMobs, probability, selector, particles, drop, safeSpawn);
        }
    }

    private static void loadKillMessage(ConfigurationSection section, Mechanics mechanic) {
        if (section.contains("Mechanics.kill_message")) {
            String deathMessage = section.getString("Mechanics.kill_message", null);
            mechanic.setKillMessage(deathMessage);
        }
    }

    private static void loadStackableMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (section.contains("Mechanics.stackable.next")
            && section.contains("Mechanics.stackable.group")
        ) {
            mechanic.setStackable(section.getString("Mechanics.stackable.next"),
                section.getString("Mechanics.stackable.group"));
        }
    }

    private static void loadDecayMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (section.contains("Mechanics.custom_block.decay.base")
            && section.contains("Mechanics.custom_block.decay.time")
            && section.contains("Mechanics.custom_block.decay.chance")
            && section.contains("Mechanics.custom_block.decay.radius")
        ) {
            int time = section.getInt("Mechanics.custom_block.decay.time", 5);
            double chance = section.getDouble("Mechanics.custom_block.decay.chance", 0.3);
            List<String> base = section.getStringList("Mechanics.custom_block.decay.base");
            int radius = section.getInt("Mechanics.custom_block.decay.radius", 5);

            List<String> nexoBaseFinal = new ArrayList<>();
            List<Material> baseFinal = new ArrayList<>();
            for (String s : base) {
                if (NexoItems.itemFromId(s) != null) {
                    nexoBaseFinal.add(s);
                } else if (Material.matchMaterial(s) != null) {
                    baseFinal.add(Material.matchMaterial(s));
                }
            }
            mechanic.setDecay(time, chance, baseFinal, nexoBaseFinal, radius);
            NexoAddon.getInstance().setIsDecay(true);
        }
    }

    private static void loadShiftBlockMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (section.contains("Mechanics.shiftblock.time")
            && section.contains("Mechanics.shiftblock.replace_to")
        ) {
            List<Material> materials = new ArrayList<>();
            List<String> nexoIds = new ArrayList<>();
            if (section.contains("Mechanics.shiftblock.items")) {
                for (String s : section.getStringList("Mechanics.shiftblock.items")) {
                    if (NexoItems.itemFromId(s) != null) {
                        nexoIds.add(s);
                    } else if (Material.matchMaterial(s) != null) {
                        materials.add(Material.matchMaterial(s));
                    }
                }
            }

            mechanic.setShiftBlock(section.getString("Mechanics.shiftblock.replace_to"),
                section.getInt("Mechanics.shiftblock.time", 200), materials, nexoIds,
                section.getBoolean("Mechanics.shiftblock.on_interact", true),
                section.getBoolean("Mechanics.shiftblock.on_break", false),
                section.getBoolean("Mechanics.shiftblock.on_place", false));
        }
    }

    private static void loadBottledExpMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (section.contains("Mechanics.bottledexp.ratio")) {
            mechanic.setBottledExp((Double) section.getDouble("Mechanics.bottledexp.ratio", 0.5),
                section.getInt("Mechanics.bottledexp.cost", 1));
        }
    }

    private static void loadUnstackableMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (section.contains("Mechanics.unstackable.next")
            && section.contains("Mechanics.unstackable.give")
        ) {
            List<String> rawItems = section.getStringList("Mechanics.unstackable.items");
            List<Material> materials = new ArrayList<>();
            List<String> nexoIds = new ArrayList<>();

            for (String rawItem : rawItems) {
                if (Material.matchMaterial(rawItem) != null) {
                    materials.add(Material.matchMaterial(rawItem));
                    continue;
                }
                if (NexoItems.itemFromId(rawItem) != null) {
                    nexoIds.add(rawItem);
                }
            }

            mechanic.setUnstackable(section.getString("Mechanics.unstackable.next"),
                section.getString("Mechanics.unstackable.give"), materials, nexoIds);
        }
    }

    private static void loadBlockAuraMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (section.contains("Mechanics.block_aura.particle")) {
            Particle particle = Particle.valueOf(
                section.getString("Mechanics.block_aura.particle", "FLAME").toUpperCase());
            String xOffset = section.getString("Mechanics.block_aura.xOffset", "0.5");
            String yOffset = section.getString("Mechanics.block_aura.yOffset", "0.5");
            String zOffset = section.getString("Mechanics.block_aura.zOffset", "0.5");
            int amount = section.getInt("Mechanics.block_aura.amount", 10);
            double deltaX = section.getDouble("Mechanics.block_aura.deltaX", 0.6);
            double deltaY = section.getDouble("Mechanics.block_aura.deltaY", 0.6);
            double deltaZ = section.getDouble("Mechanics.block_aura.deltaZ", 0.6);
            double speed = section.getDouble("Mechanics.block_aura.speed", 0.05);
            boolean force = section.getBoolean("Mechanics.block_aura.force", true);
            mechanic.setBlockAura(particle, xOffset, yOffset, zOffset, amount, deltaX, deltaY, deltaZ, speed, force);
        }
    }

    private static void loadSignalMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (section.contains("Mechanics.furniture.signal.role")
            && section.contains("Mechanics.furniture.signal.channel")
        ) {
            int radius = section.getInt("Mechanics.furniture.signal.radius", 16);
            double channel = section.getDouble("Mechanics.furniture.signal.channel");
            String role = section.getString("Mechanics.furniture.signal.role");
            mechanic.setSignal(radius, channel, role);
        }
    }

    private static void loadRememberMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (section.contains("Mechanics.furniture.remember")) {

            mechanic.setRemember(section.getBoolean("Mechanics.furniture.remember", true));
        }
    }

    private static void loadEnchantifyMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.enchantify.enchants")) {
            return;
        }

        Map<Enchantment, Integer> enchants = new HashMap<>();
        Map<Enchantment, Integer> limits = new HashMap<>();
        parseEnchantments(section.getMapList("Mechanics.enchantify.enchants"), enchants, limits);

        List<Material> materials = new ArrayList<>();
        List<String> nexoIds = new ArrayList<>();
        parseItemList(section.getStringList("Mechanics.enchantify.whitelist"), materials, nexoIds);

        List<Material> materialsBlacklist = new ArrayList<>();
        List<String> nexoIdsBlacklist = new ArrayList<>();
        parseItemList(section.getStringList("Mechanics.enchantify.blacklist"), materialsBlacklist, nexoIdsBlacklist);

        mechanic.setEnchantify(enchants, limits, materials, nexoIds, materialsBlacklist, nexoIdsBlacklist);
    }

    private static void loadAutoCatchMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.autocatch")) {
            return;
        }

        mechanic.setAutoCatch(section.getBoolean("Mechanics.autocatch.toggable", false),
            section.getBoolean("Mechanics.autocatch.recast", true));
    }

    private static void loadUniqueIdMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.uniqueid")) {
            return;
        }

        mechanic.setUniqueId(section.getBoolean("Mechanics.uniqueid.enabled", true));
    }

    private static void parseEnchantments(List<Map<?, ?>> enchantList, Map<Enchantment, Integer> enchants,
        Map<Enchantment, Integer> limits) {
        for (Map<?, ?> map : enchantList) {
            String enchantName = String.valueOf(map.get("enchant")).toLowerCase();
            if (enchantName == null || enchantName.isEmpty()) {
                continue;
            }

            if (!enchantName.contains(":")) {
                enchantName = "minecraft:" + enchantName;
            }

            int level = (map.get("level") instanceof Number number) ? number.intValue() : 0;
            int limit = (map.get("limit") instanceof Number l) ? l.intValue() : 0;

            Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.fromString(enchantName));
            if (enchantment != null && level > 0) {
                enchants.put(enchantment, Integer.valueOf(level));
                if (limit > 0) {
                    limits.put(enchantment, Integer.valueOf(limit));
                }
            }
        }
    }

    private static void loadInventoryType(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.inventoryType")) {
            return;
        }

        InventoryType type = null;
        try {
            type = InventoryType.valueOf(section.getString("Mechanics.inventoryType.type", "WORKBENCH").toUpperCase());
        } catch (IllegalArgumentException e) {
            NexoAddon.getInstance().getLogger()
                .warning("Invalid InventoryType: " + section.getString("Mechanics.inventoryType.type"));
        }

        if (type == null) {
            NexoAddon.getInstance().getLogger()
                .warning("Invalid InventoryType: " + section.getString("Mechanics.inventoryType.type"));
            return;
        }

        String titleRaw = section.getString("Mechanics.inventoryType.title");
        Component title = null;
        if (titleRaw == null || titleRaw.isEmpty()) {
            title = type.defaultTitle();
        } else {
            title = MiniMessage.miniMessage().deserialize(titleRaw);
        }

        mechanic.setInventoryType(type, title);
    }

    private static void loadTelekinessMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.telekinesis")) {
            return;
        }

        boolean enabled = section.getBoolean("Mechanics.telekinesis.enabled", true);

        List<Material> materials = section.getStringList("Mechanics.telekinesis.materials")
            .stream()
            .map(s -> Material.matchMaterial(s.toUpperCase()))
            .filter(java.util.Objects::nonNull)
            .collect(java.util.stream.Collectors.toList());

        List<String> nexoIds = section.getStringList("Mechanics.telekinesis.nexo_ids");

        mechanic.setTelekinesis(enabled, materials, nexoIds);
    }

    private static void loadLifeStealMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.lifesteal.percentage")) {
            return;
        }

        double percentage = section.getDouble("Mechanics.lifesteal.percentage", 10.0);
        double minHeal = section.getDouble("Mechanics.lifesteal.min_heal", 0.0);
        double maxHeal = section.getDouble("Mechanics.lifesteal.max_heal", 0.0);
        boolean affectUndead = section.getBoolean("Mechanics.lifesteal.affect_undead", false);

        mechanic.setLifeSteal(percentage, minHeal, maxHeal, affectUndead);
    }

    private static void loadDashMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.dash.power")) {
            return;
        }

        double power = section.getDouble("Mechanics.dash.power", 2.0);
        double verticalBoost = section.getDouble("Mechanics.dash.vertical_boost", 0.5);
        int cooldown = section.getInt("Mechanics.dash.cooldown", 5);
        boolean requireSneaking = section.getBoolean("Mechanics.dash.require_sneaking", false);
        String particleType = section.getString("Mechanics.dash.particle.type", "CLOUD");
        int particleAmount = section.getInt("Mechanics.dash.particle.amount", 20);
        String soundType = section.getString("Mechanics.dash.sound.type", "ENTITY_ENDER_DRAGON_FLAP");
        float soundVolume = (float) section.getDouble("Mechanics.dash.sound.volume", 1.0);
        float soundPitch = (float) section.getDouble("Mechanics.dash.sound.pitch", 1.5);
        String cooldownMessage = section.getString("Mechanics.dash.cooldown_message",
            "<red>Dash is on cooldown! Wait {time} seconds.");
        int durabilityCost = section.getInt("Mechanics.dash.durability_cost", 0);

        mechanic.setDash(power, verticalBoost, cooldown, requireSneaking, particleType, particleAmount,
            soundType, soundVolume, soundPitch, cooldownMessage, durabilityCost);
    }

    private static void loadGlassBreakerMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.glassbreaker")) {
            return;
        }

        boolean enabled = section.getBoolean("Mechanics.glassbreaker.enabled", true);
        int durabilityCost = section.getInt("Mechanics.glassbreaker.durability_cost", 1);

        List<Material> glassTypes = new ArrayList<>();
        List<String> nexoGlassTypes = new ArrayList<>();

        if (section.contains("Mechanics.glassbreaker.glass_types")) {
            parseItemList(section.getStringList("Mechanics.glassbreaker.glass_types"), glassTypes, nexoGlassTypes);
        }

        mechanic.setGlassBreaker(glassTypes, nexoGlassTypes, enabled, durabilityCost);
    }

    private static void loadSandSmeltMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.sandsmelt")) {
            return;
        }

        boolean enabled = section.getBoolean("Mechanics.sandsmelt.enabled", true);
        double probability = section.getDouble("Mechanics.sandsmelt.probability", 1.0);

        List<Material> sandTypes = new ArrayList<>();

        if (section.contains("Mechanics.sandsmelt.sand_types")) {
            for (String raw : section.getStringList("Mechanics.sandsmelt.sand_types")) {
                Material mat = Material.matchMaterial(raw);
                if (mat != null) {
                    sandTypes.add(mat);
                }
            }
        }

        if (sandTypes.isEmpty()) {
            sandTypes.add(Material.SAND);
            sandTypes.add(Material.RED_SAND);
        }

        mechanic.setSandSmelt(sandTypes, enabled, probability);
    }

    private static void loadInfiniteBucketMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.infinite_bucket")) {
            return;
        }

        boolean enabled = section.getBoolean("Mechanics.infinite_bucket.enabled", true);
        int uses = section.getInt("Mechanics.infinite_bucket.uses", -1);
        mechanic.setInfiniteBucket(enabled, uses);
    }

    private static void loadInfiniteFoodMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.infinite_food")) {
            return;
        }

        boolean enabled = section.getBoolean("Mechanics.infinite_food.enabled", true);
        int uses = section.getInt("Mechanics.infinite_food.uses", -1);
        mechanic.setInfiniteFood(enabled, uses);
    }

    private static void loadInfiniteShearsMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.infinite_shears")) {
            return;
        }

        boolean enabled = section.getBoolean("Mechanics.infinite_shears.enabled", true);
        int uses = section.getInt("Mechanics.infinite_shears.uses", -1);
        mechanic.setInfiniteShears(enabled, uses);
    }

    private static void loadInfiniteFluidBucketMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.infinite_fluid_bucket")) {
            return;
        }

        boolean enabled = section.getBoolean("Mechanics.infinite_fluid_bucket.enabled", true);
        int uses = section.getInt("Mechanics.infinite_fluid_bucket.uses", -1);
        String waterLore = section.getString("Mechanics.infinite_fluid_bucket.water_lore", "<aqua>Modus: Wasser");
        String lavaLore = section.getString("Mechanics.infinite_fluid_bucket.lava_lore", "<red>Modus: Lava");
        mechanic.setInfiniteFluidBucket(enabled, uses, waterLore, lavaLore);
    }

    private static void loadWideHoeMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.widehoe")) {
            return;
        }

        int radius = section.getInt("Mechanics.widehoe.radius", 3);
        // Ensure radius is always odd so it forms a centred square
        if (radius % 2 == 0) {
            radius += 1;
        }
        boolean switchable = section.getBoolean("Mechanics.widehoe.switchable", false);
        boolean tillGrass = section.getBoolean("Mechanics.widehoe.till_grass", true);
        int durabilityCost = section.getInt("Mechanics.widehoe.durability_cost", 1);

        mechanic.setWideHoe(radius, switchable, tillGrass, durabilityCost);
    }

    private static void loadMagnetMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.magnet")) {
            return;
        }

        boolean enabled = section.getBoolean("Mechanics.magnet.enabled", true);
        int radius = section.getInt("Mechanics.magnet.radius", 5);
        double pullSpeed = section.getDouble("Mechanics.magnet.pull_speed", 0.3);
        String particleType = section.getString("Mechanics.magnet.particle.type", "");
        int particleAmount = section.getInt("Mechanics.magnet.particle.amount", 3);
        String soundType = section.getString("Mechanics.magnet.sound.type", "");
        float soundVolume = (float) section.getDouble("Mechanics.magnet.sound.volume", 0.3);
        float soundPitch = (float) section.getDouble("Mechanics.magnet.sound.pitch", 1.5);
        String activeLore = section.getString("Mechanics.magnet.active_lore", "<green>Magnet: AN");
        String inactiveLore = section.getString("Mechanics.magnet.inactive_lore", "<red>Magnet: AUS");
        boolean plotOnly = section.getBoolean("Mechanics.magnet.plot_only", false);
        boolean trustedPlots = section.getBoolean("Mechanics.magnet.trusted_plots", false);
        boolean wildernessOnly = section.getBoolean("Mechanics.magnet.wilderness_only", false);

        mechanic.setMagnet(enabled, radius, pullSpeed, particleType, particleAmount,
            soundType, soundVolume, soundPitch, activeLore, inactiveLore, plotOnly, trustedPlots, wildernessOnly);
    }

    private static void loadGrapplingHookMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.grappling_hook")) return;

        boolean enabled = section.getBoolean("Mechanics.grappling_hook.enabled", true);
        int maxDistance = section.getInt("Mechanics.grappling_hook.max_distance", 30);
        double pullSpeed = section.getDouble("Mechanics.grappling_hook.pull_speed", 0.8);
        int cooldown = section.getInt("Mechanics.grappling_hook.cooldown", 3);
        String particleType = section.getString("Mechanics.grappling_hook.particle.type", "CRIT");
        int particleAmount = section.getInt("Mechanics.grappling_hook.particle.amount", 3);
        String soundType = section.getString("Mechanics.grappling_hook.sound.type", "ENTITY_FISHING_BOBBER_THROW");
        float soundVolume = (float) section.getDouble("Mechanics.grappling_hook.sound.volume", 1.0);
        float soundPitch = (float) section.getDouble("Mechanics.grappling_hook.sound.pitch", 1.2);
        String cooldownMessage = section.getString("Mechanics.grappling_hook.cooldown_message", "<red>Noch {time}s Abklingzeit!");
        int durabilityCost = section.getInt("Mechanics.grappling_hook.durability_cost", 1);
        String ropeColor = section.getString("Mechanics.grappling_hook.rope.color", "139,90,43");
        float ropeSize = (float) section.getDouble("Mechanics.grappling_hook.rope.size", 0.5);
        double ropeSpacing = section.getDouble("Mechanics.grappling_hook.rope.spacing", 0.35);

        mechanic.setGrapplingHook(enabled, maxDistance, pullSpeed, cooldown,
            particleType, particleAmount, soundType, soundVolume, soundPitch, cooldownMessage, durabilityCost,
            ropeColor, ropeSize, ropeSpacing);
    }

    private static void loadSpiderManMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.spiderman")) return;

        boolean enabled = section.getBoolean("Mechanics.spiderman.enabled", true);
        boolean wallClimbEnabled = section.getBoolean("Mechanics.spiderman.wall_climb.enabled", true);
        double climbSpeed = section.getDouble("Mechanics.spiderman.wall_climb.climb_speed", 0.25);
        String checkSlot = section.getString("Mechanics.spiderman.wall_climb.check_slot", "ANY");
        boolean webShotEnabled = section.getBoolean("Mechanics.spiderman.web_shot.enabled", true);
        int webMaxDistance = section.getInt("Mechanics.spiderman.web_shot.max_distance", 20);
        double webPullSpeed = section.getDouble("Mechanics.spiderman.web_shot.pull_speed", 1.0);
        double arcBoost = section.getDouble("Mechanics.spiderman.web_shot.arc_boost", 0.4);
        int webCooldown = section.getInt("Mechanics.spiderman.web_shot.cooldown", 2);
        String particleType = section.getString("Mechanics.spiderman.particle.type", "CLOUD");
        int particleAmount = section.getInt("Mechanics.spiderman.particle.amount", 3);
        String soundType = section.getString("Mechanics.spiderman.sound.type", "ENTITY_FISHING_BOBBER_THROW");
        float soundVolume = (float) section.getDouble("Mechanics.spiderman.sound.volume", 1.0);
        float soundPitch = (float) section.getDouble("Mechanics.spiderman.sound.pitch", 1.5);
        String cooldownMessage = section.getString("Mechanics.spiderman.cooldown_message", "<red>Noch {time}s Abklingzeit!");
        int durabilityCost = section.getInt("Mechanics.spiderman.durability_cost", 0);

        mechanic.setSpiderMan(enabled, wallClimbEnabled, climbSpeed, checkSlot,
            webShotEnabled, webMaxDistance, webPullSpeed, arcBoost, webCooldown,
            particleType, particleAmount, soundType, soundVolume, soundPitch, cooldownMessage, durabilityCost);
    }

    private static void loadOnHitMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.on_hit.effects")) {
            return;
        }

        int cooldownSeconds = section.getInt("Mechanics.on_hit.cooldown", 0);

        Particle particles = null;
        String particleRaw = section.getString("Mechanics.on_hit.particles");
        if (particleRaw != null && !particleRaw.isEmpty()) {
            try {
                particles = Particle.valueOf(particleRaw.toUpperCase());
            } catch (IllegalArgumentException e) {
                NexoAddon.getInstance().getLogger().warning("Invalid Particle for on_hit mechanic: " + particleRaw);
            }
        }

        List<OnHitMechanic.OnHitEffect> effects = new ArrayList<>();
        for (Map<?, ?> entry : section.getMapList("Mechanics.on_hit.effects")) {
            Object typeRaw = entry.get("type");
            if (typeRaw == null) {
                continue;
            }

            PotionEffectType type = resolvePotionEffectType(String.valueOf(typeRaw));
            if (type == null) {
                NexoAddon.getInstance().getLogger()
                    .warning("Invalid PotionEffectType for on_hit mechanic: " + typeRaw);
                continue;
            }

            int amplifier = (entry.get("amplifier") instanceof Number a) ? a.intValue() : 0;
            int duration = (entry.get("duration") instanceof Number d) ? d.intValue() : 1;

            effects.add(new OnHitMechanic.OnHitEffect(type, amplifier, duration));
        }

        if (effects.isEmpty()) {
            return;
        }

        mechanic.setOnHitMechanic(effects, cooldownSeconds, particles);
    }

    private static void loadAreaMiningMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.area_mining")) {
            return;
        }

        AreaMiningMechanic.Shape shape;
        try {
            shape = AreaMiningMechanic.Shape.valueOf(
                section.getString("Mechanics.area_mining.shape", "LINE").toUpperCase());
        } catch (IllegalArgumentException e) {
            NexoAddon.getInstance().getLogger().warning(
                "Invalid area_mining shape: " + section.getString("Mechanics.area_mining.shape"));
            return;
        }

        boolean consumeDurability = section.getBoolean("Mechanics.area_mining.consume_durability", false);
        int length = section.getInt("Mechanics.area_mining.length", 1);
        int maxBlocks = section.getInt("Mechanics.area_mining.max_blocks", 64);
        int radius = section.getInt("Mechanics.area_mining.radius", 1);
        int depth = section.getInt("Mechanics.area_mining.depth", 1);

        List<String> toolTypes = null;
        if (section.contains("Mechanics.area_mining.tool_types")) {
            toolTypes = section.getStringList("Mechanics.area_mining.tool_types");
        }

        Set<Material> deniedBlocks = null;
        if (section.contains("Mechanics.area_mining.denied_blocks")) {
            deniedBlocks = new HashSet<>();
            for (String raw : section.getStringList("Mechanics.area_mining.denied_blocks")) {
                Material material = Material.matchMaterial(raw);
                if (material != null) {
                    deniedBlocks.add(material);
                } else {
                    NexoAddon.getInstance().getLogger()
                        .warning("Invalid denied_blocks material for area_mining: " + raw);
                }
            }
        }

        mechanic.setAreaMiningMechanic(shape, consumeDurability, length, maxBlocks, radius, depth, toolTypes,
            deniedBlocks);
    }

    private static void loadPassiveEffectMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.passive_effect")) {
            return;
        }

        String base = "Mechanics.passive_effect.";
        String slot = section.getString(base + "slot", "mainhand").toLowerCase();
        int reapplyTicks = section.getInt(base + "reapply_ticks", 40);

        List<PassiveEffectMechanic.PassivePotionEffect> potionEffects = new ArrayList<>();
        for (Map<?, ?> entry : section.getMapList(base + "potion_effects")) {
            Object typeRaw = entry.get("type");
            if (typeRaw == null) {
                continue;
            }
            PotionEffectType type = PotionEffectType.getByName(String.valueOf(typeRaw));
            if (type == null) {
                NexoAddon.getInstance().getLogger()
                    .warning("Invalid PotionEffectType for passive_effect: " + typeRaw);
                continue;
            }
            int amplifier = (entry.get("amplifier") instanceof Number n) ? n.intValue() : 0;
            potionEffects.add(new PassiveEffectMechanic.PassivePotionEffect(type, amplifier));
        }

        List<PassiveEffectMechanic.PassiveAttributeModifier> attributeModifiers = new ArrayList<>();
        for (Map<?, ?> entry : section.getMapList(base + "attribute_modifiers")) {
            Object attrRaw = entry.get("attribute");
            if (attrRaw == null) {
                continue;
            }
            Attribute attribute = resolveAttribute(String.valueOf(attrRaw));
            if (attribute == null) {
                NexoAddon.getInstance().getLogger()
                    .warning("Invalid Attribute for passive_effect: " + attrRaw);
                continue;
            }
            Object operationRaw = entry.get("operation");
            String operationName = operationRaw == null ? "ADD_NUMBER" : String.valueOf(operationRaw);
            AttributeModifier.Operation operation;
            try {
                operation = AttributeModifier.Operation.valueOf(operationName.toUpperCase());
            } catch (IllegalArgumentException e) {
                NexoAddon.getInstance().getLogger()
                    .warning("Invalid attribute operation for passive_effect: " + entry.get("operation"));
                continue;
            }
            double amount = (entry.get("amount") instanceof Number n) ? n.doubleValue() : 0.0;
            attributeModifiers.add(new PassiveEffectMechanic.PassiveAttributeModifier(attribute, operation, amount));
        }

        boolean onSneak = false;
        boolean onSprint = false;
        int healthBelow = 100;
        Set<Biome> biomes = new HashSet<>();
        String worldTime = "any";
        if (section.contains(base + "conditions")) {
            String cond = base + "conditions.";
            onSneak = section.getBoolean(cond + "on_sneak", false);
            onSprint = section.getBoolean(cond + "on_sprint", false);
            healthBelow = section.getInt(cond + "health_below", 100);
            worldTime = section.getString(cond + "world_time", "any").toLowerCase();
            for (String raw : section.getStringList(cond + "biomes")) {
                Biome biome = resolveBiome(raw);
                if (biome != null) {
                    biomes.add(biome);
                } else {
                    NexoAddon.getInstance().getLogger().warning("Invalid Biome for passive_effect: " + raw);
                }
            }
        }
        PassiveEffectMechanic.PassiveConditions conditions =
            new PassiveEffectMechanic.PassiveConditions(onSneak, onSprint, healthBelow, biomes, worldTime);

        Particle ambientParticle = resolveParticle(section.getString(base + "ambient_particle"), "passive_effect");
        Sound activateSound = resolveSound(section.getString(base + "activate_sound"), "passive_effect");
        Sound deactivateSound = resolveSound(section.getString(base + "deactivate_sound"), "passive_effect");

        mechanic.setPassiveEffectMechanic(slot, reapplyTicks, potionEffects, attributeModifiers, conditions,
            ambientParticle, activateSound, deactivateSound);
    }

    private static void loadConsumableMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.consumable")) {
            return;
        }

        String base = "Mechanics.consumable.";
        String trigger = section.getString(base + "trigger", "right_click").toLowerCase();
        int cooldownSeconds = section.getInt(base + "cooldown", 0);
        boolean consumeItem = section.getBoolean(base + "consume_item", false);
        double instantHeal = section.getDouble(base + "instant_heal", 0.0);
        double instantDamage = section.getDouble(base + "instant_damage", 0.0);

        List<ConsumableMechanic.ConsumableEffect> effects = new ArrayList<>();
        for (Map<?, ?> entry : section.getMapList(base + "effects")) {
            Object typeRaw = entry.get("type");
            if (typeRaw == null) {
                continue;
            }
            PotionEffectType type = resolvePotionEffectType(String.valueOf(typeRaw));
            if (type == null) {
                NexoAddon.getInstance().getLogger()
                    .warning("Invalid PotionEffectType for consumable mechanic: " + typeRaw);
                continue;
            }
            int amplifier = (entry.get("amplifier") instanceof Number n) ? n.intValue() : 0;
            int duration = (entry.get("duration") instanceof Number n) ? n.intValue() : 1;
            double chance = (entry.get("chance") instanceof Number n) ? n.doubleValue() : 1.0;
            effects.add(new ConsumableMechanic.ConsumableEffect(type, amplifier, duration, chance));
        }

        List<String> commands = section.getStringList(base + "commands");

        // Conditions may live under a `conditions:` sub-section or directly on the consumable block.
        boolean requireSneaking = readBoolean(section, base, "conditions.require_sneaking", "require_sneaking", false);
        int requireHealthBelow = readInt(section, base, "conditions.require_health_below", "require_health_below", 100);
        String requirePermission = readString(section, base, "conditions.require_permission", "require_permission", "");
        ConsumableMechanic.ConsumableConditions conditions =
            new ConsumableMechanic.ConsumableConditions(requireSneaking, requireHealthBelow, requirePermission);

        Sound sound = resolveSound(section.getString(base + "sound"), "consumable");
        Particle particle = resolveParticle(section.getString(base + "particle"), "consumable");
        String messageSelf = section.getString(base + "message_self", "");
        String messageBroadcast = section.getString(base + "message_broadcast", "");

        mechanic.setConsumableMechanic(trigger, cooldownSeconds, consumeItem, instantHeal, instantDamage, effects,
            commands, conditions, sound, particle, messageSelf, messageBroadcast);
    }

    private static boolean readBoolean(ConfigurationSection section, String base, String nested, String flat,
        boolean def) {
        if (section.contains(base + nested)) {
            return section.getBoolean(base + nested, def);
        }
        return section.getBoolean(base + flat, def);
    }

    private static int readInt(ConfigurationSection section, String base, String nested, String flat, int def) {
        if (section.contains(base + nested)) {
            return section.getInt(base + nested, def);
        }
        return section.getInt(base + flat, def);
    }

    private static String readString(ConfigurationSection section, String base, String nested, String flat,
        String def) {
        if (section.contains(base + nested)) {
            return section.getString(base + nested, def);
        }
        return section.getString(base + flat, def);
    }

    private static void loadAreaAbilityMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.area_ability")) {
            return;
        }

        String base = "Mechanics.area_ability.";
        String trigger = section.getString(base + "trigger", "right_click").toLowerCase();
        int cooldownSeconds = section.getInt(base + "cooldown", 0);
        double radius = section.getDouble(base + "radius", 5.0);
        String targets = section.getString(base + "targets", "players").toLowerCase();
        boolean includeSelf = section.getBoolean(base + "include_self", true);
        int maxTargets = section.getInt(base + "max_targets", 0);

        double healAmount = section.getDouble(base + "heal_amount", 0.0);
        double damageAmount = section.getDouble(base + "damage_amount", 0.0);
        double launchVelocity = section.getDouble(base + "launch_velocity", 0.0);

        List<AreaAbilityMechanic.AbilityEffect> effects = parseAbilityEffects(section, base + "effects");
        List<String> commands = section.getStringList(base + "commands");

        double selfHeal = section.getDouble(base + "self_heal", 0.0);
        double selfDamage = section.getDouble(base + "self_damage", 0.0);
        List<AreaAbilityMechanic.AbilityEffect> selfEffects = parseAbilityEffects(section, base + "self_effects");

        String cond = base + "conditions.";
        boolean requireSneaking = section.getBoolean(cond + "require_sneaking", false);
        int requireHealthBelow = section.getInt(cond + "require_health_below", 100);
        String requirePermission = section.getString(cond + "require_permission", "");
        int minTargetsRequired = section.getInt(cond + "min_targets_required", 0);
        AreaAbilityMechanic.AbilityConditions conditions = new AreaAbilityMechanic.AbilityConditions(
            requireSneaking, requireHealthBelow, requirePermission, minTargetsRequired);

        Particle particle = resolveParticle(section.getString(base + "particle"), "area_ability");
        Particle waveParticle = resolveParticle(section.getString(base + "wave_particle"), "area_ability");
        Sound sound = resolveSound(section.getString(base + "sound"), "area_ability");
        Sound soundTarget = resolveSound(section.getString(base + "sound_target"), "area_ability");

        mechanic.setAreaAbilityMechanic(trigger, cooldownSeconds, radius, targets, includeSelf, maxTargets,
            healAmount, damageAmount, launchVelocity, effects, commands, selfHeal, selfDamage, selfEffects,
            conditions, particle, waveParticle, sound, soundTarget);
    }

    private static List<AreaAbilityMechanic.AbilityEffect> parseAbilityEffects(ConfigurationSection section,
        String path) {
        List<AreaAbilityMechanic.AbilityEffect> list = new ArrayList<>();
        for (Map<?, ?> entry : section.getMapList(path)) {
            Object typeRaw = entry.get("type");
            if (typeRaw == null) {
                continue;
            }
            PotionEffectType type = resolvePotionEffectType(String.valueOf(typeRaw));
            if (type == null) {
                NexoAddon.getInstance().getLogger()
                    .warning("Invalid PotionEffectType for area_ability: " + typeRaw);
                continue;
            }
            int amplifier = (entry.get("amplifier") instanceof Number n) ? n.intValue() : 0;
            int duration = (entry.get("duration") instanceof Number n) ? n.intValue() : 1;
            double chance = (entry.get("chance") instanceof Number n) ? n.doubleValue() : 1.0;
            list.add(new AreaAbilityMechanic.AbilityEffect(type, amplifier, duration, chance));
        }
        return list;
    }

    private static void loadTeleportMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.teleport")) {
            return;
        }

        String base = "Mechanics.teleport.";
        String trigger = section.getString(base + "trigger", "right_click").toLowerCase();
        String mode = section.getString(base + "mode", "look_direction").toLowerCase();
        int cooldownSeconds = section.getInt(base + "cooldown", 0);
        double distance = section.getDouble(base + "distance", 15.0);
        boolean behindTarget = section.getBoolean(base + "behind_target", false);

        double arriveDamageRadius = section.getDouble(base + "arrive_damage_radius", 0.0);
        double arriveDamage = section.getDouble(base + "arrive_damage", 0.0);
        double launchVelocity = section.getDouble(base + "launch_velocity", 0.0);

        List<AreaAbilityMechanic.AbilityEffect> effects = parseAbilityEffects(section, base + "effects");
        List<String> commands = section.getStringList(base + "commands");

        String cond = base + "conditions.";
        boolean requireSneaking = section.getBoolean(cond + "require_sneaking", false);
        int requireHealthBelow = section.getInt(cond + "require_health_below", 100);
        String requirePermission = section.getString(cond + "require_permission", "");
        boolean requireLineOfSight = section.getBoolean(cond + "require_line_of_sight", false);
        TeleportMechanic.TeleportConditions conditions = new TeleportMechanic.TeleportConditions(
            requireSneaking, requireHealthBelow, requirePermission, requireLineOfSight);

        List<TeleportMechanic.ParticleEntry> originParticles =
            parseParticleEntries(section, base + "origin_particles");
        List<TeleportMechanic.ParticleEntry> destinationParticles =
            parseParticleEntries(section, base + "destination_particles");
        Particle trailParticle = resolveParticle(section.getString(base + "trail_particle"), "teleport");
        Sound soundOrigin = resolveSound(section.getString(base + "sound_origin"), "teleport");
        Sound soundDestination = resolveSound(section.getString(base + "sound_destination"), "teleport");

        mechanic.setTeleportMechanic(trigger, mode, cooldownSeconds, distance, behindTarget, arriveDamageRadius,
            arriveDamage, launchVelocity, effects, commands, conditions, originParticles, destinationParticles,
            trailParticle, soundOrigin, soundDestination);
    }

    private static List<TeleportMechanic.ParticleEntry> parseParticleEntries(ConfigurationSection section,
        String path) {
        List<TeleportMechanic.ParticleEntry> list = new ArrayList<>();
        for (Map<?, ?> entry : section.getMapList(path)) {
            Object particleRaw = entry.get("particle");
            if (particleRaw == null) {
                continue;
            }
            Particle particle = resolveParticle(String.valueOf(particleRaw), "teleport");
            if (particle == null) {
                continue;
            }
            int count = (entry.get("count") instanceof Number n) ? n.intValue() : 1;
            list.add(new TeleportMechanic.ParticleEntry(particle, count));
        }
        return list;
    }

    private static void loadBeamMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.beam")) {
            return;
        }

        String base = "Mechanics.beam.";
        String trigger = section.getString(base + "trigger", "right_click").toLowerCase();
        int cooldownSeconds = section.getInt(base + "cooldown", 0);
        double range = section.getDouble(base + "range", 10.0);
        double width = section.getDouble(base + "width", 0.4);
        double height = section.getDouble(base + "height", 0.4);
        boolean pierce = section.getBoolean(base + "pierce", true);
        boolean pierceBlocks = section.getBoolean(base + "pierce_blocks", false);

        double damage = section.getDouble(base + "damage", 0.0);
        double knockback = section.getDouble(base + "knockback", 0.0);
        List<AreaAbilityMechanic.AbilityEffect> effects = parseAbilityEffects(section, base + "effects");
        List<String> commands = section.getStringList(base + "commands");

        List<AreaAbilityMechanic.AbilityEffect> selfEffects = parseAbilityEffects(section, base + "self_effects");
        double selfDamage = section.getDouble(base + "self_damage", 0.0);

        String cond = base + "conditions.";
        boolean requireSneaking = section.getBoolean(cond + "require_sneaking", false);
        int requireHealthBelow = section.getInt(cond + "require_health_below", 100);
        String requirePermission = section.getString(cond + "require_permission", "");
        BeamMechanic.BeamConditions conditions = new BeamMechanic.BeamConditions(
            requireSneaking, requireHealthBelow, requirePermission);

        List<BeamMechanic.BeamSegment> beamSegments = parseBeamSegments(section, base + "beam_segments");
        Particle hitParticle = resolveParticle(section.getString(base + "hit_particle"), "beam");
        Sound sound = resolveSound(section.getString(base + "sound"), "beam");
        Sound soundHit = resolveSound(section.getString(base + "sound_hit"), "beam");

        mechanic.setBeamMechanic(trigger, cooldownSeconds, range, width, height, pierce, pierceBlocks, damage,
            knockback, effects, commands, selfEffects, selfDamage, conditions, beamSegments, hitParticle, sound,
            soundHit);
    }

    private static List<BeamMechanic.BeamSegment> parseBeamSegments(ConfigurationSection section, String path) {
        List<BeamMechanic.BeamSegment> list = new ArrayList<>();
        for (Map<?, ?> entry : section.getMapList(path)) {
            Object particleRaw = entry.get("particle");
            Particle particle = particleRaw == null
                ? null : resolveParticle(String.valueOf(particleRaw), "beam");
            if (particle == null) {
                continue;
            }
            int fromPct = (entry.get("from_pct") instanceof Number n) ? n.intValue() : 0;
            int toPct = (entry.get("to_pct") instanceof Number n) ? n.intValue() : 100;
            int count = (entry.get("count") instanceof Number n) ? n.intValue() : 1;
            list.add(new BeamMechanic.BeamSegment(fromPct, toPct, particle, count));
        }
        if (list.isEmpty()) {
            // Default: single CRIT segment spanning the full beam.
            list.add(new BeamMechanic.BeamSegment(0, 100, Particle.CRIT, 1));
        }
        return list;
    }

    private static void loadProjectileMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.projectile")) {
            return;
        }
        mechanic.setProjectileMechanic(parseProjectile(section, "Mechanics.projectile."));
    }

    /** Parses a {@link ProjectileMechanic} record from the given config base path. */
    private static ProjectileMechanic parseProjectile(ConfigurationSection section, String base) {
        String trigger = section.getString(base + "trigger", "right_click").toLowerCase();
        int cooldownSeconds = section.getInt(base + "cooldown", 0);
        double range = section.getDouble(base + "range", 20.0);
        double speed = section.getDouble(base + "speed", 1.5);
        double hitRadius = section.getDouble(base + "hit_radius", 0.5);
        int maxActive = section.getInt(base + "max_active", 1);

        double gravity = section.getDouble(base + "gravity", 0.0);
        int bounces = section.getInt(base + "bounces", 0);
        int pierce = section.getInt(base + "pierce", 0);
        boolean homing = section.getBoolean(base + "homing", false);
        double homingRadius = section.getDouble(base + "homing_radius", 8.0);
        double homingStrength = section.getDouble(base + "homing_strength", 0.08);

        double damage = section.getDouble(base + "damage", 0.0);
        double knockback = section.getDouble(base + "knockback", 0.0);
        List<AreaAbilityMechanic.AbilityEffect> effects = parseAbilityEffects(section, base + "effects");
        List<String> commands = section.getStringList(base + "commands");
        List<String> blockCommands = section.getStringList(base + "block_commands");

        double explosionRadius = section.getDouble(base + "explosion_radius", 0.0);
        double explosionDamage = section.getDouble(base + "explosion_damage", 0.0);
        List<AreaAbilityMechanic.AbilityEffect> explosionEffects =
            parseAbilityEffects(section, base + "explosion_effects");
        Particle explosionParticle = resolveParticle(section.getString(base + "explosion_particle"), "projectile");

        String cond = base + "conditions.";
        boolean requireSneaking = section.getBoolean(cond + "require_sneaking", false);
        int requireHealthBelow = section.getInt(cond + "require_health_below", 100);
        String requirePermission = section.getString(cond + "require_permission", "");
        ProjectileMechanic.ProjectileConditions conditions = new ProjectileMechanic.ProjectileConditions(
            requireSneaking, requireHealthBelow, requirePermission);

        List<ProjectileMechanic.TrailEntry> trail = parseTrailEntries(section, base + "trail");
        List<TeleportMechanic.ParticleEntry> impactParticles =
            parseParticleEntries(section, base + "impact_particles");
        Sound soundLaunch = resolveSound(section.getString(base + "sound_launch"), "projectile");
        Sound soundImpact = resolveSound(section.getString(base + "sound_impact"), "projectile");

        return new ProjectileMechanic(trigger, cooldownSeconds, range, speed, hitRadius, maxActive, gravity,
            bounces, pierce, homing, homingRadius, homingStrength, damage, knockback, effects, commands,
            blockCommands, explosionRadius, explosionDamage, explosionEffects, explosionParticle, conditions,
            trail, impactParticles, soundLaunch, soundImpact);
    }

    private static List<ProjectileMechanic.TrailEntry> parseTrailEntries(ConfigurationSection section, String path) {
        List<ProjectileMechanic.TrailEntry> list = new ArrayList<>();
        for (Map<?, ?> entry : section.getMapList(path)) {
            Object particleRaw = entry.get("particle");
            if (particleRaw == null) {
                continue;
            }
            Particle particle = resolveParticle(String.valueOf(particleRaw), "projectile");
            if (particle == null) {
                continue;
            }
            int count = (entry.get("count") instanceof Number n) ? n.intValue() : 1;
            double offset = (entry.get("offset") instanceof Number n) ? n.doubleValue() : 0.0;
            list.add(new ProjectileMechanic.TrailEntry(particle, count, offset));
        }
        return list;
    }

    private static void loadShapeWaveMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.shape_wave")) {
            return;
        }

        String base = "Mechanics.shape_wave.";
        String trigger = section.getString(base + "trigger", "right_click").toLowerCase();
        int cooldownSeconds = section.getInt(base + "cooldown", 0);
        String shape = section.getString(base + "shape", "cone").toLowerCase();
        int maxTargets = section.getInt(base + "max_targets", 0);

        double range = section.getDouble(base + "range", 8.0);
        double angle = section.getDouble(base + "angle", 30.0);
        // A line is a very narrow cylinder, so it defaults to a much smaller radius.
        double defaultRadius = shape.equals("line") ? 0.3 : 3.0;
        double radius = section.getDouble(base + "radius", defaultRadius);
        double height = section.getDouble(base + "height", 1.0);
        double minRadius = section.getDouble(base + "min_radius", 0.0);
        int rays = section.getInt(base + "rays", 3);
        double arcDegrees = section.getDouble(base + "arc_degrees", 90.0);

        double damage = section.getDouble(base + "damage", 0.0);
        int fireDurationSeconds = section.getInt(base + "fire_duration", 0);
        double knockback = section.getDouble(base + "knockback", 0.0);
        List<AreaAbilityMechanic.AbilityEffect> effects = parseAbilityEffects(section, base + "effects");
        List<String> commands = section.getStringList(base + "commands");

        List<AreaAbilityMechanic.AbilityEffect> selfEffects = parseAbilityEffects(section, base + "self_effects");
        double selfDamage = section.getDouble(base + "self_damage", 0.0);
        double selfHeal = section.getDouble(base + "self_heal", 0.0);

        String cond = base + "conditions.";
        boolean requireSneaking = section.getBoolean(cond + "require_sneaking", false);
        int requireHealthBelow = section.getInt(cond + "require_health_below", 100);
        String requirePermission = section.getString(cond + "require_permission", "");
        int minTargetsRequired = section.getInt(cond + "min_targets_required", 0);
        ShapeWaveMechanic.ShapeConditions conditions = new ShapeWaveMechanic.ShapeConditions(
            requireSneaking, requireHealthBelow, requirePermission, minTargetsRequired);

        double particleDensity = section.getDouble(base + "particle_density", 3.0);
        boolean animate = section.getBoolean(base + "animate", false);
        int animateTicks = section.getInt(base + "animate_ticks", 6);
        List<ProjectileMechanic.TrailEntry> fillParticles = parseTrailEntries(section, base + "fill_particles");
        Sound sound = resolveSound(section.getString(base + "sound"), "shape_wave");
        Sound soundHit = resolveSound(section.getString(base + "sound_hit"), "shape_wave");

        mechanic.setShapeWaveMechanic(trigger, cooldownSeconds, shape, maxTargets, range, angle, radius, height,
            minRadius, rays, arcDegrees, damage, fireDurationSeconds, knockback, effects, commands, selfEffects,
            selfDamage, selfHeal, conditions, particleDensity, animate, animateTicks, fillParticles, sound,
            soundHit);
    }

    private static void loadBowMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.bow")) {
            return;
        }

        String base = "Mechanics.bow.";

        BowMechanic.ArrowPassive arrowPassive = null;
        if (section.contains(base + "arrow_passive")) {
            String ap = base + "arrow_passive.";
            int fireTicks = section.getInt(ap + "fire_ticks", 0);
            double damageMultiplier = section.getDouble(ap + "damage_multiplier", 1.0);
            double velocityMultiplier = section.getDouble(ap + "velocity_multiplier", 1.0);
            int knockback = section.getInt(ap + "knockback", 0);
            int piercing = section.getInt(ap + "piercing", 0);
            boolean critical = section.getBoolean(ap + "critical", false);
            boolean glowing = section.getBoolean(ap + "glowing", false);
            List<AreaAbilityMechanic.AbilityEffect> hitEffects = parseAbilityEffects(section, ap + "hit_effects");
            double hitDamageBonus = section.getDouble(ap + "hit_damage_bonus", 0.0);
            int hitFireDuration = section.getInt(ap + "hit_fire_duration", 0);
            double hitExplosionRadius = section.getDouble(ap + "hit_explosion_radius", 0.0);
            double hitExplosionDamage = section.getDouble(ap + "hit_explosion_damage", 0.0);
            List<String> hitCommands = section.getStringList(ap + "hit_commands");
            List<ProjectileMechanic.TrailEntry> hitParticles = parseTrailEntries(section, ap + "hit_particles");
            Sound hitSound = resolveSound(section.getString(ap + "hit_sound"), "bow");
            List<ProjectileMechanic.TrailEntry> launchParticles = parseTrailEntries(section, ap + "launch_particles");
            Sound launchSound = resolveSound(section.getString(ap + "launch_sound"), "bow");

            arrowPassive = new BowMechanic.ArrowPassive(fireTicks, damageMultiplier, velocityMultiplier, knockback,
                piercing, critical, glowing, hitEffects, hitDamageBonus, hitFireDuration, hitExplosionRadius,
                hitExplosionDamage, hitCommands, hitParticles, hitSound, launchParticles, launchSound);
        }

        BowMechanic.SpecialShot specialShot = null;
        if (section.contains(base + "special_shot")) {
            String ss = base + "special_shot.";
            int cooldownSeconds = section.getInt(ss + "cooldown", 0);
            String type = section.getString(ss + "type", "fireball").toLowerCase();
            double yield = section.getDouble(ss + "yield", 2.0);
            boolean incendiary = section.getBoolean(ss + "incendiary", false);
            double speed = section.getDouble(ss + "speed", 1.5);
            double range = section.getDouble(ss + "range", 30.0);
            double damageLightning = section.getDouble(ss + "damage_lightning", 1.0);
            int count = section.getInt(ss + "count", 5);
            double spreadAngle = section.getDouble(ss + "spread_angle", 15.0);

            ProjectileMechanic projectileConfig = null;
            if (section.contains(ss + "projectile_config")) {
                projectileConfig = parseProjectile(section, ss + "projectile_config.");
            }

            String cond = ss + "conditions.";
            String requirePermission = section.getString(cond + "require_permission", "");
            boolean requireArrows = section.getBoolean(cond + "require_arrows", false);
            int requireHealthBelow = section.getInt(cond + "require_health_below", 100);
            BowMechanic.SpecialConditions conditions =
                new BowMechanic.SpecialConditions(requirePermission, requireArrows, requireHealthBelow);

            List<ProjectileMechanic.TrailEntry> launchParticles =
                parseTrailEntries(section, ss + "special_launch_particles");
            Sound launchSound = resolveSound(section.getString(ss + "special_launch_sound"), "bow");
            String cooldownMessage = section.getString(ss + "special_cooldown_message",
                "<red>Noch <bold>{remaining}s</bold> Cooldown!");

            specialShot = new BowMechanic.SpecialShot(cooldownSeconds, type, yield, incendiary, speed, range,
                damageLightning, count, spreadAngle, projectileConfig, conditions, launchParticles, launchSound,
                cooldownMessage);
        }

        if (arrowPassive == null && specialShot == null) {
            return;
        }
        mechanic.setBowMechanic(new BowMechanic(arrowPassive, specialShot));
    }

    /**
     * Loads the MMO-style {@link DashMechanic}. Keyed on {@code Mechanics.dash.mode} so it does not
     * collide with the legacy {@code Mechanics.dash.power} mechanic ({@link #loadDashMechanic}).
     */
    private static void loadDashAbilityMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.dash.mode")) {
            return;
        }

        String base = "Mechanics.dash.";
        String trigger = section.getString(base + "trigger", "right_click").toLowerCase();
        String mode = section.getString(base + "mode", "flight").toLowerCase();
        String direction = section.getString(base + "direction", "look").toLowerCase();
        int cooldownSeconds = section.getInt(base + "cooldown", 0);
        int durationTicks = section.getInt(base + "duration", 40);
        double speed = section.getDouble(base + "speed", 2.5);

        int charges = Math.max(1, section.getInt(base + "charges", 1));
        int chargeRechargeSeconds = section.getInt(base + "charge_recharge_seconds", 8);

        List<DashMechanic.PhaseEffect> phaseEffects = parsePhaseEffects(section, base + "phase_effects");
        int phaseInvincibilityTicks = section.getInt(base + "phase_invincibility_ticks", 0);
        boolean glowDuringDash = section.getBoolean(base + "glow_during_dash", false);

        List<ProjectileMechanic.TrailEntry> phaseParticles = parseTrailEntries(section, base + "phase_particles");
        int trailIntervalTicks = section.getInt(base + "trail_interval_ticks", 1);

        List<AreaAbilityMechanic.AbilityEffect> activateEffects =
            parseAbilityEffects(section, base + "activate_effects");
        double activateSelfDamage = section.getDouble(base + "activate_self_damage", 0.0);

        double impactRadius = section.getDouble(base + "impact_radius", 0.0);
        double impactDamage = section.getDouble(base + "impact_damage", 0.0);
        int impactDelayTicks = section.getInt(base + "impact_delay_ticks", 10);
        List<AreaAbilityMechanic.AbilityEffect> impactEffects =
            parseAbilityEffects(section, base + "impact_effects");
        List<TeleportMechanic.ParticleEntry> impactParticles =
            parseParticleEntries(section, base + "impact_particles");
        Sound impactSound = resolveSound(section.getString(base + "impact_sound"), "dash");

        String cond = base + "conditions.";
        boolean requireSneaking = section.getBoolean(cond + "require_sneaking", false);
        boolean requireSprinting = section.getBoolean(cond + "require_sprinting", false);
        int requireHealthBelow = section.getInt(cond + "require_health_below", 100);
        String requirePermission = section.getString(cond + "require_permission", "");
        boolean requireOnGround = section.getBoolean(cond + "require_on_ground", false);
        boolean requireInAir = section.getBoolean(cond + "require_in_air", false);
        DashMechanic.DashConditions conditions = new DashMechanic.DashConditions(requireSneaking, requireSprinting,
            requireHealthBelow, requirePermission, requireOnGround, requireInAir);

        List<ProjectileMechanic.TrailEntry> activateParticles =
            parseTrailEntries(section, base + "activate_particles");
        Sound activateSound = resolveSound(section.getString(base + "activate_sound"), "dash");

        mechanic.setDashMechanic(new DashMechanic(trigger, mode, direction, cooldownSeconds, durationTicks, speed,
            charges, chargeRechargeSeconds, phaseEffects, phaseInvincibilityTicks, glowDuringDash, phaseParticles,
            trailIntervalTicks, activateEffects, activateSelfDamage, impactRadius, impactDamage, impactDelayTicks,
            impactEffects, impactParticles, impactSound, conditions, activateParticles, activateSound));
    }

    /** Parses a phase-effect list whose durations are given in ticks (key {@code duration_ticks}). */
    private static List<DashMechanic.PhaseEffect> parsePhaseEffects(ConfigurationSection section, String path) {
        List<DashMechanic.PhaseEffect> list = new ArrayList<>();
        for (Map<?, ?> entry : section.getMapList(path)) {
            Object typeRaw = entry.get("type");
            if (typeRaw == null) {
                continue;
            }
            PotionEffectType type = resolvePotionEffectType(String.valueOf(typeRaw));
            if (type == null) {
                NexoAddon.getInstance().getLogger().warning("Invalid PotionEffectType for dash: " + typeRaw);
                continue;
            }
            int amplifier = (entry.get("amplifier") instanceof Number n) ? n.intValue() : 0;
            int durationTicks = (entry.get("duration_ticks") instanceof Number n) ? n.intValue() : 20;
            list.add(new DashMechanic.PhaseEffect(type, amplifier, durationTicks));
        }
        return list;
    }

    private static void loadParticleAuraMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.particle_aura")) {
            return;
        }

        String base = "Mechanics.particle_aura.";
        String slot = section.getString(base + "slot", "offhand").toLowerCase();
        int intervalTicks = section.getInt(base + "interval_ticks", 2);

        String cond = base + "conditions.";
        boolean requireSneaking = section.getBoolean(cond + "require_sneaking", false);
        boolean requireSprinting = section.getBoolean(cond + "require_sprinting", false);
        Set<String> worlds = new HashSet<>(section.getStringList(cond + "worlds"));
        ParticleAuraMechanic.AuraConditions conditions =
            new ParticleAuraMechanic.AuraConditions(requireSneaking, requireSprinting, worlds);

        List<ParticleAuraMechanic.AuraLayer> layers = parseAuraLayers(section, base + "layers", "particle_aura");

        if (layers.isEmpty()) {
            return;
        }

        mechanic.setParticleAuraMechanic(slot, intervalTicks, conditions, layers);
    }

    private static List<ParticleAuraMechanic.AuraLayer> parseAuraLayers(ConfigurationSection section, String path,
        String context) {
        List<ParticleAuraMechanic.AuraLayer> layers = new ArrayList<>();
        for (Map<?, ?> entry : section.getMapList(path)) {
            Object shapeRaw = entry.get("shape");
            String shape = shapeRaw == null ? "ring" : String.valueOf(shapeRaw).toLowerCase();
            Object particleRaw = entry.get("particle");
            Particle particle = particleRaw == null ? null : resolveParticle(String.valueOf(particleRaw), context);
            if (particle == null) {
                NexoAddon.getInstance().getLogger()
                    .warning("Invalid or missing Particle for " + context + " layer: " + particleRaw);
                continue;
            }
            double radius = numberOr(entry.get("radius"), 1.0);
            int count = (int) numberOr(entry.get("count"), 1);
            double rotationSpeed = numberOr(entry.get("rotation_speed"), 0.1);
            double yOffset = numberOr(entry.get("y_offset"), 0.0);
            double height = numberOr(entry.get("height"), 2.0);
            int orbCount = (int) numberOr(entry.get("orb_count"), 3);
            boolean onlyOnSprint = entry.get("only_on_sprint") instanceof Boolean b && b;
            boolean onlyOnSneak = entry.get("only_on_sneak") instanceof Boolean b && b;
            boolean onlyOnDamage = entry.get("only_on_damage") instanceof Boolean b && b;
            double countSprintMultiplier = numberOr(entry.get("count_sprint_multiplier"), 1.0);
            Object dustColorRaw = entry.get("dust_color");
            Color dustColor = dustColorRaw != null ? parseColor(String.valueOf(dustColorRaw)) : null;
            Object dustColorToRaw = entry.get("dust_color_to");
            Color dustColorTo = dustColorToRaw != null ? parseColor(String.valueOf(dustColorToRaw)) : null;
            float dustSize = (float) numberOr(entry.get("dust_size"), 1.0);
            double topRadius = numberOr(entry.get("top_radius"), -1.0);
            double turns = numberOr(entry.get("turns"), 2.0);
            boolean clockwise = entry.get("clockwise") instanceof Boolean b && b;
            double scatter = numberOr(entry.get("scatter"), 0.0);
            int scatterCount = (int) numberOr(entry.get("scatter_count"), 0);

            layers.add(new ParticleAuraMechanic.AuraLayer(shape, particle, radius, count, rotationSpeed, yOffset,
                height, orbCount, onlyOnSprint, onlyOnSneak, onlyOnDamage, countSprintMultiplier,
                dustColor, dustColorTo, dustSize, topRadius, turns, clockwise, scatter, scatterCount));
        }
        return layers;
    }

    private static void loadBlockTriggerLaunchMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.block_trigger_launch")) {
            return;
        }

        String base = "Mechanics.block_trigger_launch.";
        int activationCooldown = (int) Math.round(section.getDouble(base + "activation_cooldown", 0));
        int duration = (int) Math.round(section.getDouble(base + "duration", 8));
        double perLaunchCooldown = section.getDouble(base + "per_launch_cooldown", 0.3);

        Set<Material> triggerBlocks = new HashSet<>();
        for (String raw : section.getStringList(base + "trigger_blocks")) {
            try {
                triggerBlocks.add(Material.valueOf(raw.toUpperCase()));
            } catch (IllegalArgumentException e) {
                NexoAddon.getInstance().getLogger()
                    .warning("Invalid Material for block_trigger_launch trigger_blocks: " + raw);
            }
        }

        int checkBlockOffset = section.getInt(base + "check_block_offset", 0);
        double launchPower = section.getDouble(base + "launch_power", 1.0);
        double horizontalPower = section.getDouble(base + "horizontal_power", 0.0);
        String horizontalSource = section.getString(base + "horizontal_source", "movement").toLowerCase();

        List<AreaAbilityMechanic.AbilityEffect> activeEffects = parseAbilityEffects(section, base + "active_effects");
        List<AreaAbilityMechanic.AbilityEffect> launchEffects = parseAbilityEffects(section, base + "launch_effects");
        int noFallDamageTicks = section.getInt(base + "no_fall_damage_ticks", 0);

        List<ParticleAuraMechanic.AuraLayer> auraLayers =
            parseAuraLayers(section, base + "aura_layers", "block_trigger_launch");
        List<BlockTriggerLaunchMechanic.LaunchParticle> launchParticles =
            parseLaunchParticles(section, base + "launch_particles");
        List<BlockTriggerLaunchMechanic.LaunchParticle> activateParticles =
            parseLaunchParticles(section, base + "activate_particles");

        Sound launchSound = resolveSound(section.getString(base + "launch_sound"), "block_trigger_launch");
        Sound activateSound = resolveSound(section.getString(base + "activate_sound"), "block_trigger_launch");
        boolean showActionBar = section.getBoolean(base + "action_bar", true);

        mechanic.setBlockTriggerLaunchMechanic(activationCooldown, duration, perLaunchCooldown, triggerBlocks,
            checkBlockOffset, launchPower, horizontalPower, horizontalSource, activeEffects, launchEffects,
            noFallDamageTicks, auraLayers, launchParticles, launchSound, activateParticles, activateSound,
            showActionBar);
    }

    private static List<BlockTriggerLaunchMechanic.LaunchParticle> parseLaunchParticles(ConfigurationSection section,
        String path) {
        List<BlockTriggerLaunchMechanic.LaunchParticle> list = new ArrayList<>();
        for (Map<?, ?> entry : section.getMapList(path)) {
            Object particleRaw = entry.get("particle");
            if (particleRaw == null) {
                continue;
            }
            Particle particle = resolveParticle(String.valueOf(particleRaw), "block_trigger_launch");
            if (particle == null) {
                continue;
            }
            int count = (entry.get("count") instanceof Number n) ? n.intValue() : 1;
            double offset = (entry.get("offset") instanceof Number n) ? n.doubleValue() : 0.0;
            list.add(new BlockTriggerLaunchMechanic.LaunchParticle(particle, count, offset));
        }
        return list;
    }

    private static double numberOr(Object raw, double def) {
        return (raw instanceof Number n) ? n.doubleValue() : def;
    }

    private static Color parseColor(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        if (raw.startsWith("#")) {
            try {
                int hex = Integer.parseInt(raw.substring(1), 16);
                return Color.fromRGB((hex >> 16) & 0xFF, (hex >> 8) & 0xFF, hex & 0xFF);
            } catch (NumberFormatException ignored) {}
        }
        return switch (raw.toUpperCase()) {
            case "RED" -> Color.RED;
            case "BLUE" -> Color.BLUE;
            case "GREEN" -> Color.GREEN;
            case "WHITE" -> Color.WHITE;
            case "BLACK" -> Color.BLACK;
            case "YELLOW" -> Color.YELLOW;
            case "ORANGE" -> Color.ORANGE;
            case "PURPLE" -> Color.PURPLE;
            case "AQUA" -> Color.AQUA;
            case "FUCHSIA" -> Color.FUCHSIA;
            case "LIME" -> Color.LIME;
            case "MAROON" -> Color.MAROON;
            case "NAVY" -> Color.NAVY;
            case "OLIVE" -> Color.OLIVE;
            case "SILVER" -> Color.SILVER;
            case "TEAL" -> Color.TEAL;
            default -> {
                NexoAddon.getInstance().getLogger().warning("Unknown dust color: " + raw);
                yield null;
            }
        };
    }

    private static Attribute resolveAttribute(String raw) {
        // Accept legacy enum names (e.g. GENERIC_MAX_HEALTH) as well as registry keys (max_health)
        String name = raw.toLowerCase().replaceFirst("^(generic|player|zombie|horse)[._]", "");
        NamespacedKey key = NamespacedKey.fromString(name.contains(":") ? name : "minecraft:" + name);
        Attribute attribute = key == null ? null : Registry.ATTRIBUTE.get(key);
        if (attribute != null) {
            return attribute;
        }
        try {
            return Attribute.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Biome resolveBiome(String raw) {
        String name = raw.toLowerCase();
        NamespacedKey key = NamespacedKey.fromString(name.contains(":") ? name : "minecraft:" + name);
        Biome biome = key == null ? null : Registry.BIOME.get(key);
        if (biome != null) {
            return biome;
        }
        try {
            return Biome.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Particle resolveParticle(String raw, String context) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        // 1. Bukkit enum name (FLAME, CRIT, ENCHANTMENT_TABLE, …)
        try {
            return Particle.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ignored) {}
        // 2. Minecraft namespaced key as on the wiki (flame, enchanted_hit, minecraft:flame, …)
        String key = raw.toLowerCase().startsWith("minecraft:") ? raw.substring(10).toLowerCase() : raw.toLowerCase();
        for (Particle p : Particle.values()) {
            if (p.getKey().getKey().equals(key)) {
                return p;
            }
        }
        NexoAddon.getInstance().getLogger().warning("Invalid Particle for " + context + ": " + raw);
        return null;
    }

    private static Sound resolveSound(String raw, String context) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        try {
            return Sound.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            NexoAddon.getInstance().getLogger().warning("Invalid Sound for " + context + ": " + raw);
            return null;
        }
    }

    private static PotionEffectType resolvePotionEffectType(String raw) {
        String name = raw.toLowerCase();
        if (!name.contains(":")) {
            name = "minecraft:" + name;
        }
        NamespacedKey key = NamespacedKey.fromString(name);
        PotionEffectType type = key == null ? null : Registry.EFFECT.get(key);
        if (type != null) {
            return type;
        }
        // Legacy field names (e.g. SLOW, INCREASE_DAMAGE) don't match registry keys.
        return PotionEffectType.getByName(raw);
    }

    private static void parseItemList(List<String> rawItems, List<Material> materials, List<String> nexoIds) {
        for (String rawItem : rawItems) {
            Material material = Material.matchMaterial(rawItem);
            if (material != null) {
                materials.add(material);
                continue;
            }
            if (NexoItems.itemFromId(rawItem) != null) {
                nexoIds.add(rawItem);
            }
        }
    }

}
