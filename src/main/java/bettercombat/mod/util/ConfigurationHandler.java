package bettercombat.mod.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class ConfigurationHandler
{
    public static Configuration config;

    private static final int VERSION = 5;

    public static boolean requireFullEnergy = false;
    public static boolean hitSound = true;
    public static boolean critSound = true;
    public static boolean moreSprint = true;
    public static boolean widerAttack = true;
    public static boolean longerAttack = true;
    public static boolean refoundEnergy = true;
    public static boolean moreSweep = true;
    public static boolean randomCrits = true;
    public static boolean weakerOffhand = true;
    public static boolean enableOffHandAttack = true;
    public static float offHandEfficiency = 0.5F;
    public static float critChance = 0.3F;
    public static float widerAttackWidth = 1.5F;

    private static String[] itemWhitelist = new String[] {
            "CLASS:net.minecraft.item.ItemSword",
            "CLASS:net.minecraft.item.ItemAxe",
            "CLASS:net.minecraft.item.ItemSpade",
            "CLASS:net.minecraft.item.ItemPickaxe",
            "CLASS:net.minecraft.item.ItemHoe",
            "CLASS:slimeknights.tconstruct.library.tools.SwordCore",
            "CLASS:slimeknights.tconstruct.library.tools.AoeToolCore"
    };
    private static String[] offhandBlacklist = new String[] {
            "ACTION:BOW",
            "ACTION:EAT",
            "ACTION:DRINK",
            "ACTION:BLOCK",
            "ENTITYCLASS:net.minecraft.entity.IEntityOwnable",
            "ENTITYCLASS:net.minecraft.entity.item.EntityArmorStand",
            "ENTITYCLASS:net.minecraft.entity.passive.EntityVillager",
            "ENTITYCLASS:net.minecraft.entity.passive.AbstractHorse"
    };

    private static final String[] IW_DEF = Arrays.copyOf(itemWhitelist, itemWhitelist.length);
    private static final String[] OB_DEF = Arrays.copyOf(offhandBlacklist, offhandBlacklist.length);

    private static final Map<WhitelistType, List<Object>> itemWhitelistMap = new EnumMap<>(WhitelistType.class);
    private static final Map<BlacklistType, List<Object>> offhandBlacklistMap = new EnumMap<>(BlacklistType.class);

    public static void init(File configFile) {
        if( config == null ) {
            config = new Configuration(configFile, Integer.toString(VERSION));
            loadConfiguration(configFile);
        }
    }

    private static void loadConfiguration(File configFile) {
        String ver = config.getLoadedConfigVersion();
        int loadedVer = 0;
        try {
            if( ver != null ) {
                loadedVer = Integer.parseInt(ver);
            }
        } catch( NumberFormatException ignored ) { }

        if( configFile != null && loadedVer > 0 && loadedVer < VERSION ) {
            File fileBak = new File(configFile.getAbsolutePath() + '_' + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".old");
//            FMLLog.log.fatal("An exception occurred while loading config file {}. This file will be renamed to {} " +
//                                     "and a new config file will be generated.", file.getName(), fileBak.getName(), e);

            configFile.renameTo(fileBak);
            BetterCombatMod.LOG.log(Level.WARN, String.format("You have an outdated configuration! This config file will be renamed to %s and a new file will be generated.", fileBak.getName()));
            config = null;
            init(configFile);

            return;
        }

        longerAttack = config.getBoolean("Longer Attack", "general", true, "Melee attacks receive +1(block) range");
        widerAttack = config.getBoolean("Wider Attack", "general", true, "Melee attacks hit in a wider area (easier to land hit)");
        randomCrits = config.getBoolean("Random Crits", "general", true, "Melee attacks have now 30% chance to critically strike, critical strikes can no longer be forced by falling");
        refoundEnergy = config.getBoolean("Refound energy on miss", "general", true, "Melee attacks that don't hit the target won't cause cooldown");
        weakerOffhand = config.getBoolean("Weaker Left Arm", "general", true, "Attacks with the Off-hand does 50% less damage");
        requireFullEnergy = config.getBoolean("Attacks require full energy", "general", false, "You may only attack if your energy is full");
        moreSprint = config.getBoolean("Attack and Sprint", "general", true, "Attacking an enemy while sprinting will no longer interrupt your sprint");
        moreSweep = config.getBoolean("More swipe animation", "general", true, "Every items can spawn the swipe animation");
        hitSound = config.getBoolean("Additional hit sound", "general", true, "Add an additional sound when striking a target");
        critSound = config.getBoolean("Additional crit sound", "general", true, "Add an additional sound when a critical strike happens");
        enableOffHandAttack = config.getBoolean("Enable Offhand Attacks", "general", true, "Enables the capability to attack with your off-hand");
        offHandEfficiency = config.getFloat("Offhand Efficiency", "general", 0.5F, 0.0F, 16384.0F, "The efficiency of an attack with offhanded weapon in percent (attack damage * efficiency)");
        critChance = config.getFloat("Random Crit Chance", "general", 0.3F, 0.0F, 1.0F, "How likely it is to land a critical hit in percent");
        widerAttackWidth = config.getFloat("Wider Attack Width", "general", 1.5F, 0.0F, 64.0F, "How much bigger the hitbox will be extended for wider attacks. Vanilla is 0.5");

        itemWhitelist = config.getStringList("Item Whitelist", "general", IW_DEF,
                                             "Whitelisted items for offhand attacking. Formatting is as follows:\n" +
                                                  " CLASS:{path.to.class} (e.g. CLASS:net.minecraft.item.ItemSword) - should whitelist everything that extends or is this class specified after the colon\n" +
                                                  " NAME:{domain:name}    (e.g. NAME:minecraft:stick)               - should whitelist a specific item name (The item name is the same as you would use with the /give command)");
        offhandBlacklist = config.getStringList("Offhand Blacklist", "general", OB_DEF,
                                                "Blacklisted things for offhand attacking. Formatting is as follows:\n" +
                                                  " ACTION:{type}               (e.g. ACTION:BOW)                                     - if a player has an item in their main hand that does this specific action, offhand attacking is disabled (for modders: see net.minecraft.item.EnumAction)\n" +
                                                  " ITEMCLASS:{path.to.class}   (e.g. ITEMCLASS:net.minecraft.item.ItemEgg)           - if a player holds an item in their main hand whose class is or extends the given class, offhand attacking is disabled\n" +
                                                  " ITEMNAME:{domain:name}      (e.g. ITEMNAME:minecraft:egg)                         - if a player has an item in their main hand with this name, offhand attacking is disabled (The item name is the same as you would use with the /give command)\n" +
                                                  " ENTITYCLASS:{path.to.class} (e.g. ENTITYCLASS:net.minecraft.entity.EntityAgeable) - if a player tries to attack an entity whose class is or extends the given class, offhand attacking is disabled\n" +
                                                  " ENTITYNAME:{name}           (e.g. ENTITYNAME:minecraft:cow)                       - if a player tries to attack an entity with this name, offhand attacking is disabled (The entity name is the same as you would use with the /summon command)");

//        if( loadedVer < VERSION ) {
//            config.getCategory("general").remove("Item Class Blacklist");
//            config.getCategory("general").remove("Item Blacklist");
//        }

        if( config.hasChanged() ) {
            config.save();
        }
    }

    public static void createInstLists() {
        Arrays.stream(WhitelistType.VALUES).forEach(t -> itemWhitelistMap.put(t, new ArrayList<>()));
        Arrays.stream(BlacklistType.VALUES).forEach(t -> offhandBlacklistMap.put(t, new ArrayList<>()));

        Arrays.stream(itemWhitelist).forEach(s -> {
            int colonIndex = s.indexOf(':');
            if( colonIndex > 0 ) {
                String typeStr = s.substring(0, colonIndex - 1);
                try {
                    WhitelistType type = WhitelistType.valueOf(typeStr);
                    String value = s.substring(colonIndex + 1);
                    switch( type ) {
                        case CLASS: {
                            try {
                                itemWhitelistMap.get(type).add(Class.forName(value));
                            } catch( ClassNotFoundException ignored ) { }
                        } break;
                        case NAME: {
                            Item item = Item.REGISTRY.getObject(new ResourceLocation(value));
                            if( item != null ) {
                                itemWhitelistMap.get(type).add(item);
                            }
                        } break;
                    }
                } catch( IllegalArgumentException ex ) {
                    BetterCombatMod.LOG.log(Level.WARN, String.format("Unknown whitelist type: %s", typeStr));
                }
            }
        });

        Arrays.stream(offhandBlacklist).forEach(s -> {
            int colonIndex = s.indexOf(':');
            if( colonIndex > 0 ) {
                String typeStr = s.substring(0, colonIndex - 1);
                try {
                    BlacklistType type = BlacklistType.valueOf(typeStr);
                    String value = s.substring(colonIndex + 1);
                    switch( type ) {
                        case ACTION: {
                            try {
                                offhandBlacklistMap.get(type).add(EnumAction.valueOf(value));
                            } catch( IllegalArgumentException ex ) {
                                BetterCombatMod.LOG.log(Level.WARN, String.format("Unknown action type: %s", value));
                            }
                        } break;
                        case ITEMCLASS: case ENTITYCLASS: {
                            try {
                                offhandBlacklistMap.get(type).add(Class.forName(value));
                            } catch( ClassNotFoundException ignored ) { }
                        } break;
                        case ITEMNAME: {
                            Item item = Item.REGISTRY.getObject(new ResourceLocation(value));
                            if( item != null ) {
                                offhandBlacklistMap.get(type).add(item);
                            }
                        } break;
                        case ENTITYNAME: {
                            Class<?> cls = EntityList.getClass(new ResourceLocation(value));
                            if( cls != null ) {
                                offhandBlacklistMap.get(type).add(cls);
                            }
                        } break;
                    }
                } catch( IllegalArgumentException ex ) {
                    BetterCombatMod.LOG.log(Level.WARN, String.format("Unknown blacklist type: %s", typeStr));
                }
            }
        });
    }

    public static boolean isItemAttackUsable(final Item item, final ItemStack mhItem) {
        if( offhandBlacklistMap.get(BlacklistType.ACTION).stream().anyMatch(a -> a == mhItem.getItemUseAction()) ) {
            return false;
        }
        if( offhandBlacklistMap.get(BlacklistType.ITEMCLASS).stream().anyMatch(c -> ((Class) c).isInstance(mhItem.getItem())) ) {
            return false;
        }
        if( offhandBlacklistMap.get(BlacklistType.ITEMNAME).stream().anyMatch(n -> n.equals(mhItem.getItem())) ) {
            return false;
        }

        return itemWhitelistMap.get(WhitelistType.CLASS).stream().anyMatch(c -> ((Class) c).isInstance(item))
               || itemWhitelistMap.get(WhitelistType.NAME).stream().anyMatch(n -> n.equals(item));
    }

    public static boolean isEntityAttackable(final Entity entity) {
        return offhandBlacklistMap.get(BlacklistType.ENTITYCLASS).stream().noneMatch(c -> ((Class) c).isInstance(entity))
               && offhandBlacklistMap.get(BlacklistType.ENTITYNAME).stream().noneMatch(c -> ((Class) c).isInstance(entity));
    }

    @SubscribeEvent
    public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if( Reference.MOD_ID.equalsIgnoreCase(event.getModID()) ) {
            loadConfiguration(null);
            createInstLists();
        }
    }

    private enum WhitelistType {
        CLASS,
        NAME;

        private static final WhitelistType[] VALUES = values();
    }

    private enum BlacklistType {
        ACTION,
        ITEMCLASS,
        ITEMNAME,
        ENTITYCLASS,
        ENTITYNAME;

        private static final BlacklistType[] VALUES = values();
    }
}