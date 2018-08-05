package bettercombat.mod.util;

import bettercombat.mod.combat.DefaultImplOffHandAttack;
import bettercombat.mod.combat.DefaultImplSecondHurtTimer;
import bettercombat.mod.combat.IOffHandAttack;
import bettercombat.mod.combat.ISecondHurtTimer;
import bettercombat.mod.network.PacketHandler;
import bettercombat.mod.capability.StorageOffHandAttack;
import bettercombat.mod.capability.StorageSecondHurtTimer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@SuppressWarnings("unused")
public class CommonProxy
{
    public void preInit(FMLPreInitializationEvent event) {
        PacketHandler.registerMessages(Reference.MOD_ID);
        CapabilityManager.INSTANCE.register(IOffHandAttack.class, new StorageOffHandAttack(), DefaultImplOffHandAttack::new);
        CapabilityManager.INSTANCE.register(ISecondHurtTimer.class, new StorageSecondHurtTimer(), DefaultImplSecondHurtTimer::new);
    }

    public void spawnSweep(EntityPlayer player) {}
}