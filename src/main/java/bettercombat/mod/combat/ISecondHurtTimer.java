package bettercombat.mod.combat;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;

public interface ISecondHurtTimer
{
    int getHurtTimerBCM();

    void setHurtTimerBCM(int hurtTimer);

    void tick();

    boolean attackEntityFromOffhand(Entity target, DamageSource dmgSrc, float amount);
}