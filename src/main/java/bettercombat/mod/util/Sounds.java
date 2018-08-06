package bettercombat.mod.util;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class Sounds
{
    public static final SoundEvent SWORD_SLASH = registerSound("player.swordslash");
    public static final SoundEvent CRITICAL_STRIKE = registerSound("player.criticalstrike");

    private static SoundEvent registerSound(String soundName) {
        ResourceLocation soundID = new ResourceLocation(Reference.MOD_ID, soundName);
        return new SoundEvent(soundID).setRegistryName(soundID);
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(SWORD_SLASH, CRITICAL_STRIKE);
    }
}