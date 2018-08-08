package bettercombat.mod.handler;

import bettercombat.mod.capability.CapabilityOffhandCooldown;
import bettercombat.mod.combat.IOffHandAttack;
import bettercombat.mod.combat.ISecondHurtTimer;
import bettercombat.mod.network.PacketHandler;
import bettercombat.mod.network.PacketSendEnergy;
import bettercombat.mod.util.ConfigurationHandler;
import bettercombat.mod.util.Helpers;
import bettercombat.mod.util.Reference;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandlers
{
    public static final EventHandlers INSTANCE = new EventHandlers();

    public int energyToGive;
    public boolean giveEnergy;
    public int offhandCooldown;

    @CapabilityInject(IOffHandAttack.class)
    public static final Capability<IOffHandAttack> OFFHAND_CAP = Helpers.getNull();
    @CapabilityInject(ISecondHurtTimer.class)
    public static final Capability<ISecondHurtTimer> SECONDHURTTIMER_CAP = Helpers.getNull();
    @CapabilityInject(CapabilityOffhandCooldown.class)
    public static final Capability<CapabilityOffhandCooldown> TUTO_CAP = Helpers.getNull();

    private EventHandlers() {}

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if( event.getTarget() == null ) {
            return;
        }
        giveEnergy = false;
        if( event.getTarget().hurtResistantTime <= 10 ) {
            if( ConfigurationHandler.moreSweep ) {
                ((EntityPlayer) event.getEntityLiving()).spawnSweepParticles();
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        ISecondHurtTimer sht = event.getEntityLiving().getCapability(SECONDHURTTIMER_CAP, null);
        if( sht != null && sht.getHurtTimerBCM() > 0 ) {
            sht.tick();
        }

        if( event.getEntityLiving() instanceof EntityPlayer ) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            IOffHandAttack oha = event.getEntityLiving().getCapability(OFFHAND_CAP, null);
            CapabilityOffhandCooldown cof = player.getCapability(TUTO_CAP, null);
            Helpers.execNullable(oha, IOffHandAttack::tick);

            if( cof != null ) {
                cof.tick();
                if( this.offhandCooldown > 0 ) {
                    cof.setOffhandCooldown(this.offhandCooldown);
                    if( !player.world.isRemote ) {
                        cof.sync();
                    }
                    this.offhandCooldown = 0;
                }
            }
        }

        if( event.getEntityLiving() instanceof EntityPlayer && this.giveEnergy ) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            if( player == null ) {
                return;
            }

            if( player.ticksSinceLastSwing == 0 ) {
                player.ticksSinceLastSwing = this.energyToGive;
                PacketHandler.instance.sendToServer(new PacketSendEnergy(this.energyToGive));
            }
        }
    }

    @SubscribeEvent
    public void onEntityConstruct(AttachCapabilitiesEvent event) {
        if( event.getGenericType() != Entity.class ) {
            return;
        }
        if( event.getObject() instanceof EntityPlayer ) {
            event.addCapability(new ResourceLocation(Reference.MOD_ID, "TUTO_CAP"), new CapabilityOffhandCooldown((EntityPlayer) event.getObject()));
        }

        event.addCapability(new ResourceLocation(Reference.MOD_ID, "IOffHandAttack"), new ICapabilitySerializable()
        {
            IOffHandAttack inst = EventHandlers.OFFHAND_CAP.getDefaultInstance();

            @Override
            public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                return capability == EventHandlers.OFFHAND_CAP;
            }

            @Override
            public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                return capability == EventHandlers.OFFHAND_CAP ? EventHandlers.OFFHAND_CAP.cast(this.inst) : null;
            }

            @Override
            public NBTPrimitive serializeNBT() {
                return (NBTPrimitive) EventHandlers.OFFHAND_CAP.getStorage().writeNBT(EventHandlers.OFFHAND_CAP, this.inst, null);
            }

            @Override
            public void deserializeNBT(NBTBase nbt) {
                EventHandlers.OFFHAND_CAP.getStorage().readNBT(EventHandlers.OFFHAND_CAP, this.inst, null, nbt);
            }
        });

        event.addCapability(new ResourceLocation(Reference.MOD_ID, "ISecondHurtTimer"), new ICapabilitySerializable()
        {
            ISecondHurtTimer inst = EventHandlers.SECONDHURTTIMER_CAP.getDefaultInstance();

            @Override
            public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                return capability == EventHandlers.SECONDHURTTIMER_CAP;
            }

            @Override
            public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                return capability == EventHandlers.SECONDHURTTIMER_CAP ? EventHandlers.SECONDHURTTIMER_CAP.cast(this.inst) : null;
            }

            @Override
            public NBTPrimitive serializeNBT() {
                return (NBTPrimitive) EventHandlers.SECONDHURTTIMER_CAP.getStorage().writeNBT(EventHandlers.SECONDHURTTIMER_CAP, this.inst, null);
            }

            @Override
            public void deserializeNBT(NBTBase nbt) {
                EventHandlers.SECONDHURTTIMER_CAP.getStorage().readNBT(EventHandlers.SECONDHURTTIMER_CAP, this.inst, null, nbt);
            }
        });
    }
}