package bettercombat.mod.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;

public class DefaultImplSecondHurtTimer
        implements ISecondHurtTimer
{
    private int hurtTime;

    @Override
    public int getHurtTimerBCM() {
        return this.hurtTime;
    }

    @Override
    public void setHurtTimerBCM(int hurtTimer) {
        this.hurtTime = hurtTimer;
    }

    @Override
    public void tick() {
        if( this.hurtTime > 0 ) {
            this.hurtTime -= 1;
        }
    }

    @Override
    public boolean attackEntityFromOffhand(Entity target, DamageSource dmgSrc, float amount) {
        if( target.isEntityInvulnerable(dmgSrc) || this.hurtTime > 0 || target.world.isRemote || !(target instanceof EntityLivingBase) ) {
            return false;
        }

        EntityLivingBase targetLiving = (EntityLivingBase) target;

        if( targetLiving.getHealth() <= 0.0F ) {
            return false;
        }

        boolean successfulAttack = false;
        if( this.hurtTime <= 0 ) {
            Entity trueSrc = dmgSrc.getTrueSource();
            ItemStack buf;

            if( trueSrc instanceof EntityPlayer ) { // switch offhand item to mainhand, so entities can properly determine what item hit them
                EntityPlayer player = (EntityPlayer) trueSrc;
                buf = player.getHeldItemMainhand();
                player.setHeldItem(EnumHand.MAIN_HAND, player.getHeldItemOffhand());
                player.setHeldItem(EnumHand.OFF_HAND, buf);
            }

            // save current hit times and set the value to 0 for the entity to allow hitting with the off-hand
            int mainHurtTime = targetLiving.hurtTime;
            int mainHurtResistance = targetLiving.hurtResistantTime;
            targetLiving.hurtTime = 0;
            targetLiving.hurtResistantTime = 0;

            //attack entity
            successfulAttack = targetLiving.attackEntityFrom(dmgSrc, amount);
            if( successfulAttack ) {
                this.hurtTime = 10;
            }

            // reset current hit times to the entity
            targetLiving.hurtTime = mainHurtTime;
            targetLiving.hurtResistantTime = mainHurtResistance;

            if( trueSrc instanceof EntityPlayer ) { // reset held items to their proper slots
                EntityPlayer player = (EntityPlayer) trueSrc;
                buf = player.getHeldItemOffhand();
                player.setHeldItem(EnumHand.OFF_HAND, player.getHeldItemMainhand());
                player.setHeldItem(EnumHand.MAIN_HAND, buf);
            }
        }

        return successfulAttack;
    }
}