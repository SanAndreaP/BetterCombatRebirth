package bettercombat.mod.capability;

import bettercombat.mod.combat.IOffHandAttack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class StorageOffHandAttack
        implements Capability.IStorage<IOffHandAttack>
{
    @Override
    public NBTBase writeNBT(Capability<IOffHandAttack> capability, IOffHandAttack instance, EnumFacing side) {
        return new NBTTagInt(instance.getOffhandCooldown());
    }

    @Override
    public void readNBT(Capability<IOffHandAttack> capability, IOffHandAttack instance, EnumFacing side, NBTBase nbt) {
        instance.setOffhandCooldown(((NBTPrimitive) nbt).getInt());
    }
}