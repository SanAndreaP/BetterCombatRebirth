package bettercombat.mod.util;

import bettercombat.mod.handler.EventHandlers;
import bettercombat.mod.network.PacketOffhandCooldown;
import bettercombat.mod.util.BetterCombatMod;
import bettercombat.mod.util.ConfigurationHandler;
import bettercombat.mod.util.Helpers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;

public class ScheduledPacketTask
        implements Runnable
{
    private EntityPlayer player;
    private PacketOffhandCooldown message;

    public ScheduledPacketTask(EntityPlayer player, PacketOffhandCooldown message) {
        this.player = player;
        this.message = message;
    }

    @Override
    public void run() {
        if( this.player == null ) {
            return;
        }

        if( ConfigurationHandler.moreSweep || this.player.getHeldItemOffhand().getItem() instanceof ItemSword ) {
            BetterCombatMod.proxy.spawnSweep(this.player);
        }

        Helpers.execNullable(this.player.getCapability(EventHandlers.TUTO_CAP, null), stg -> stg.setOffhandCooldown(this.message.cooldown));
    }
}