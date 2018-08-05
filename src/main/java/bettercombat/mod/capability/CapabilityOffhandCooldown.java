package bettercombat.mod.capability;

import bettercombat.mod.handler.EventHandlers;
import bettercombat.mod.network.PacketHandler;
import bettercombat.mod.network.PacketOffhandCooldown;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

public class CapabilityOffhandCooldown
        implements ICapabilityProvider, INBTSerializable<NBTTagCompound>
{
    public int offhandCooldown;
    private EntityPlayer player;

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability != null && capability == EventHandlers.TUTO_CAP;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability != null && capability == EventHandlers.TUTO_CAP ? (T) this : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("offhandCooldown", this.offhandCooldown);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        this.offhandCooldown = compound.getInteger("offhandCooldown");
    }

    public void tick() {
        if( this.offhandCooldown > 0 ) {
            this.offhandCooldown -= 1;
        }
    }

    public void setOffhandCooldown(int money) {
        this.offhandCooldown = money;
    }

    public int getOffhandCooldown() {
        return this.offhandCooldown;
    }

    public static class Storage
            implements net.minecraftforge.common.capabilities.Capability.IStorage<CapabilityOffhandCooldown>
    {
        @Override
        public NBTBase writeNBT(Capability<CapabilityOffhandCooldown> capability, CapabilityOffhandCooldown instance, EnumFacing side) {
            return null;
        }

        @Override
        public void readNBT(Capability<CapabilityOffhandCooldown> capability, CapabilityOffhandCooldown instance, EnumFacing side, NBTBase nbt) {}
    }

    public static class Factory
            implements Callable<CapabilityOffhandCooldown>
    {
        @Override
        public CapabilityOffhandCooldown call() {
            return null;
        }
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(CapabilityOffhandCooldown.class, new Storage(), new Factory());
    }

    public CapabilityOffhandCooldown(@Nonnull EntityPlayer player) {
        this.offhandCooldown = 0;
        this.player = player;
    }

    public void sync() {
        PacketOffhandCooldown packet = new PacketOffhandCooldown(this.offhandCooldown);
        if( !this.player.world.isRemote ) {
            EntityPlayerMP playerMP = (EntityPlayerMP) this.player;
            PacketHandler.instance.sendTo(packet, playerMP);
        } else {
            PacketHandler.instance.sendToServer(packet);
        }
    }
}