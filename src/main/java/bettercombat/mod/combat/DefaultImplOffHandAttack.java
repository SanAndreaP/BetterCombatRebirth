package bettercombat.mod.combat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;

public class DefaultImplOffHandAttack
        implements IOffHandAttack
{
    private int offhandCooldown;

    @Override
    public int getOffhandCooldown() {
        return this.offhandCooldown;
    }

    @Override
    public void setOffhandCooldown(int amount) {
        this.offhandCooldown = amount;
    }

    @Override
    public void tick() {
        if( this.offhandCooldown > 0 ) {
            this.offhandCooldown -= 1;
        }
    }

    @Override
    public void swingOffHand(EntityPlayer player) {
        player.swingArm(EnumHand.OFF_HAND);
    }
}