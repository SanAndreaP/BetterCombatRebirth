package bettercombat.mod.combat;

import net.minecraft.entity.player.EntityPlayer;

public interface IOffHandAttack
{
    int getOffhandCooldown();

    void setOffhandCooldown(int amount);

    void tick();

    void swingOffHand(EntityPlayer player);
}