package bettercombat.mod.combat;

import bettercombat.mod.util.Reflections;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;

public class DefaultImplSecondHurtTimer
        implements ISecondHurtTimer
{
    private int hurtTimer;

    @Override
    public int getHurtTimerBCM() {
        return this.hurtTimer;
    }

    @Override
    public void setHurtTimerBCM(int hurtTimer) {
        this.hurtTimer = hurtTimer;
    }

    @Override
    public void tick() {
        if( this.hurtTimer > 0 ) {
            this.hurtTimer -= 1;
        }
    }

    @Override
    public boolean attackEntityFromOffhand(Entity target, DamageSource dmgSrc, float amount) {
        if( target.isEntityInvulnerable(dmgSrc) || this.hurtTimer > 0 ) {
            return false;
        }

        if( target.world.isRemote ) {
            return false;
        }

        if( !(target instanceof EntityLivingBase) ) {
            return false;
        }

        EntityLivingBase targetLiving = (EntityLivingBase) target;

        if( targetLiving.getHealth() <= 0.0F ) {
            return false;
        }

        boolean blocked = false;
        if( amount > 0.0F ) {
            if( Reflections.canBlockDamageSource(targetLiving, dmgSrc) ) {
                Reflections.damageShield(targetLiving, amount);
                if( dmgSrc.isProjectile() ) {
                    amount = 0.0F;
                } else {
                    amount *= 0.33F;
                    if( (dmgSrc.getImmediateSource() instanceof EntityLivingBase) ) {
                        ((EntityLivingBase) dmgSrc.getImmediateSource()).knockBack(targetLiving, 0.5F, targetLiving.posX - dmgSrc.getImmediateSource().posX, targetLiving.posZ - dmgSrc.getImmediateSource().posZ);
                    }
                }
                blocked = true;
            }
        }

        targetLiving.limbSwingAmount = 1.5F;

        if( this.hurtTimer <= 0 ) {
            Reflections.damageEntity(targetLiving, dmgSrc, amount);
            this.hurtTimer = 10;
        }

        targetLiving.attackedAtYaw = 0.0F;
        Entity entity = dmgSrc.getTrueSource();
        if( entity != null ) {
            if( (entity instanceof EntityLivingBase) ) {
                targetLiving.setRevengeTarget((EntityLivingBase) entity);
            }
            if( entity instanceof EntityPlayer ) {
                targetLiving.recentlyHit = 100;
                targetLiving.attackingPlayer = (EntityPlayer) entity;
            }
        }

        if( blocked ) {
            targetLiving.world.setEntityState(targetLiving, (byte) 29);
        } else {
            targetLiving.world.setEntityState(targetLiving, (byte) 2);
        }

        if( dmgSrc != DamageSource.DROWN && (!blocked || amount > 0.0F) ) {
            Reflections.markVelocityChanged(targetLiving);
        }

        if( entity != null ) {
            double dPosX = entity.posX - targetLiving.posX;
            double dPosZ = entity.posZ - targetLiving.posZ;
            for(; dPosX * dPosX + dPosZ * dPosZ < 1.0E-4D; dPosZ = (Math.random() - Math.random()) * 0.01D ) {
                dPosX = (Math.random() - Math.random()) * 0.01D;
            }

            targetLiving.attackedAtYaw = (float) (MathHelper.atan2(dPosZ, dPosX) * 57.29577951308232D - targetLiving.rotationYaw);
            targetLiving.knockBack(entity, 0.4F, dPosX, dPosZ);
        } else {
            targetLiving.attackedAtYaw = (int) (Math.random() * 2.0D) * 180;
        }

        if( targetLiving.getHealth() <= 0.0F ) {
            SoundEvent soundevent = Reflections.getDeathSound(targetLiving);
            if( soundevent != null ) {
                targetLiving.playSound(soundevent, Reflections.getSoundVolume(targetLiving), Reflections.getSoundPitch(targetLiving));
            }

            targetLiving.onDeath(dmgSrc);
        } else {
            Reflections.playHurtSound(targetLiving, dmgSrc);
        }

        return !blocked || amount > 0.0F;
    }
}