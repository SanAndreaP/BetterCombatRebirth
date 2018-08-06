package bettercombat.mod.util;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class ConfigurationHandler
{
    public static Configuration config;

    private static final int VERSION = 3;

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

    private static String[] itemClassWhitelist = new String[] {
            "net.minecraft.item.ItemSword",
            "net.minecraft.item.ItemAxe",
            "net.minecraft.item.ItemSpade",
            "net.minecraft.item.ItemPickaxe",
            "net.minecraft.item.ItemHoe",
            "slimeknights.tconstruct.library.tools.SwordCore",
            "slimeknights.tconstruct.library.tools.AoeToolCore"
    };
    private static String[] itemClassBlacklist = new String[] {
            "net.minecraft.item.ItemBlock",
            "net.minecraft.item.ItemEgg",
            "net.minecraft.item.ItemBoat",
            "net.minecraft.item.ItemWritableBook",
            "net.minecraft.item.ItemFood",
            "net.minecraft.item.ItemBucket",
            "net.minecraft.item.ItemSeedFood",
            "net.minecraft.item.ItemBow",
            "net.minecraft.item.ItemEnderPearl",
            "net.minecraft.item.ItemMonsterPlacer",
            "net.minecraft.item.ItemSnowball",
            "net.minecraft.item.ItemShears",
            "net.minecraft.item.ItemFishingRod",
            "net.minecraft.item.ItemFlintAndSteel",
            "net.minecraft.item.ItemBlockSpecial",
            "net.minecraft.item.ItemShield",
            "net.minecraftforge.common.IPlantable"
    };
    private static String[] itemInstWhitelist = new String[] {};
    private static String[] itemInstBlacklist = new String[] {
            "minecraft:wheat",
            "minecraft:bone"
    };
    private static String[] entityBlacklist = new String[] {
            "net.minecraft.entity.passive.EntityHorse",
            "net.minecraft.entity.item.EntityArmorStand",
            "net.minecraft.entity.passive.EntityVillager"
    };

    private static final String[] ICW_DEF = Arrays.copyOf(itemClassWhitelist, itemClassWhitelist.length);
    private static final String[] ICB_DEF = Arrays.copyOf(itemClassBlacklist, itemClassBlacklist.length);
    private static final String[] IIW_DEF = Arrays.copyOf(itemInstWhitelist, itemInstWhitelist.length);
    private static final String[] IIB_DEF = Arrays.copyOf(itemInstBlacklist, itemInstBlacklist.length);
    private static final String[] EB_DEF = Arrays.copyOf(entityBlacklist, entityBlacklist.length);

    private static Class<?>[] itemClassWhiteArray;
    private static Class<?>[] itemClassBlackArray;
    private static Item[] itemInstWhiteArray;
    private static Item[] itemInstBlackArray;
    private static Class<?>[] entityBlackArray;

    public static void init(File configFile) {
        if( config == null ) {
            config = new Configuration(configFile, Integer.toString(VERSION));
            loadConfiguration();
        }
    }

    private static void loadConfiguration() {
        String ver = config.getLoadedConfigVersion();
        int loadedVer = 0;
        try {
            if( ver != null ) {
                loadedVer = Integer.parseInt(ver);
            }
        } catch( NumberFormatException ignored ) { }

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
        itemClassWhitelist = config.getStringList("Item Class Whitelist", "general", ICW_DEF, "Whitelisted item classes for attacking. Item blacklist will overrule this for specific items!");
        itemClassBlacklist = config.getStringList("Item Class Blacklist", "general", ICB_DEF, "Blacklisted item classes for attacking. Item whitelist will overrule this for specific items!");
        itemInstWhitelist = config.getStringList("Item Whitelist", "general", IIW_DEF, "Whitelisted items in the format \"domain:itemname\" for attacking. This overrules the item class blacklist!");
        itemInstBlacklist = config.getStringList("Item Blacklist", "general", IIB_DEF, "Blacklisted items in the format \"domain:itemname\" for attacking. This overrules the item class whitelist!");
        enableOffHandAttack = config.getBoolean("Enable Offhand Attacks", "general", true, "Enables the capability to attack with your off-hand");
        offHandEfficiency = config.getFloat("Offhand Efficiency", "general", 0.5F, 0.0F, 16384.0F, "The efficiency of an attack with offhanded weapon in percent (attack damage * efficiency)");
        critChance = config.getFloat("Random Crit Chance", "general", 0.3F, 0.0F, 1.0F, "How likely it is to land a critical hit in percent");
        widerAttackWidth = config.getFloat("Wider Attack Width", "general", 1.5F, 0.0F, 64.0F, "How much bigger the hitbox will be extended for wider attacks. Vanilla is 0.5");
        entityBlacklist = config.getStringList("Entity Blacklist", "general", EB_DEF, "Blacklisted entity classes for attacking. You will not be able to attack any entity that extends this class! Please note that entities extending IEntityOwnable are by default blacklisted, when the entity is owned by the attacker.");

        if( loadedVer < VERSION ) {
            itemInstBlacklist = Stream.concat(Arrays.stream(itemInstBlacklist), Stream.of("minecraft:bone")).toArray(String[]::new);
            config.getCategory("general").get("Item Blacklist").set(itemInstBlacklist);
        }

        if( config.hasChanged() ) {
            config.save();
        }
    }

    public static void createInstLists() {
        List<Class<?>> classList = new ArrayList<>();
        for( String className : itemClassWhitelist ) {
            try {
                classList.add(Class.forName(className));
            } catch( ClassNotFoundException ignored ) { }
        }
        itemClassWhiteArray = classList.toArray(new Class<?>[0]);

        classList.clear();
        for( String className : itemClassBlacklist ) {
            try {
                classList.add(Class.forName(className));
            } catch( ClassNotFoundException e ) {
                e.printStackTrace();
            }
        }
        itemClassBlackArray = classList.toArray(new Class<?>[0]);

        classList.clear();
        for( String className : entityBlacklist ) {
            try {
                classList.add(Class.forName(className));
            } catch( ClassNotFoundException e ) {
                e.printStackTrace();
            }
        }
        entityBlackArray = classList.toArray(new Class<?>[0]);

        List<Item> itemList = new ArrayList<>();
        for( String itemName : itemInstWhitelist ) {
            Item itm = Item.REGISTRY.getObject(new ResourceLocation(itemName));
            if( itm != null ) {
                itemList.add(itm);
            }
        }
        itemInstWhiteArray = itemList.toArray(new Item[0]);

        itemList.clear();
        for( String itemName : itemInstBlacklist ) {
            Item itm = Item.REGISTRY.getObject(new ResourceLocation(itemName));
            if( itm != null ) {
                itemList.add(itm);
            }
        }
        itemInstBlackArray = itemList.toArray(new Item[0]);
    }

    public static boolean isItemAttackUsable(final Item item) {
        if( Arrays.stream(itemInstBlackArray).noneMatch(blItem -> blItem == item) ) {
            if( Arrays.stream(itemInstWhiteArray).anyMatch(blItem -> blItem == item) ) {
                return true;
            }
            if( Arrays.stream(itemClassWhiteArray).noneMatch(wlClass -> wlClass.isInstance(item)) ) {
                return Arrays.stream(itemClassBlackArray).noneMatch(blClass -> blClass.isInstance(item));
            }
            return true;
        }

        return false;
    }

    public static boolean isEntityAttackable(final Entity entity) {
        return Arrays.stream(entityBlackArray).noneMatch(eClass -> eClass.isInstance(entity));
    }

    @SubscribeEvent
    public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if( Reference.MOD_ID.equalsIgnoreCase(event.getModID()) ) {
            loadConfiguration();
            createInstLists();
        }
    }
}