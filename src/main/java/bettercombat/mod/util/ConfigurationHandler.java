package bettercombat.mod.util;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigurationHandler
{
    public static Configuration config;

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
            "minecraft:wheat"
    };

    private static final String[] ICW_DEF = Arrays.copyOf(itemClassWhitelist, itemClassWhitelist.length);
    private static final String[] ICB_DEF = Arrays.copyOf(itemClassBlacklist, itemClassBlacklist.length);
    private static final String[] IIW_DEF = Arrays.copyOf(itemInstWhitelist, itemInstWhitelist.length);
    private static final String[] IIB_DEF = Arrays.copyOf(itemInstBlacklist, itemInstBlacklist.length);

    private static Class<?>[] itemClassWhiteArray;
    private static Class<?>[] itemClassBlackArray;
    private static Item[] itemInstWhiteArray;
    private static Item[] itemInstBlackArray;

    public static void init(File configFile) {
        if( config == null ) {
            config = new Configuration(configFile);
            loadConfiguration();
        }
    }

    private static void loadConfiguration() {
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
        if( config.hasChanged() ) {
            config.save();
        }
    }

    public static void createItemLists() {
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
            return Arrays.stream(itemClassWhiteArray).anyMatch(wlClass -> wlClass.isInstance(item))
                   && Arrays.stream(itemClassBlackArray).noneMatch(blClass -> blClass.isInstance(item));
        }

        return false;
    }

    @SubscribeEvent
    public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if( Reference.MOD_ID.equalsIgnoreCase(event.getModID()) ) {
            loadConfiguration();
            createItemLists();
        }
    }
}