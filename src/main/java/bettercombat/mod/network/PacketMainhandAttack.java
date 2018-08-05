package bettercombat.mod.network;

import bettercombat.mod.util.Helpers;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketMainhandAttack implements IMessage
{
    private int entityId;

    @SuppressWarnings("unused")
    public PacketMainhandAttack() {}

    public PacketMainhandAttack(int parEntityId) {
        this.entityId = parEntityId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = ByteBufUtils.readVarInt(buf, 4);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarInt(buf, this.entityId, 4);
    }

    public static class Handler
            implements IMessageHandler<PacketMainhandAttack, IMessage>
    {
        @Override
        public IMessage onMessage(final PacketMainhandAttack message, final MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private static void handle(PacketMainhandAttack message, MessageContext ctx) {
            EntityPlayerMP thePlayer = ctx.getServerHandler().player;
            Entity theEntity = thePlayer.world.getEntityByID(message.entityId);
            if( theEntity != null ) {
                if( thePlayer.interactionManager.getGameType() == GameType.SPECTATOR ) {
                    thePlayer.setSpectatingEntity(theEntity);
                } else {
                    Helpers.attackTargetEntityItem(thePlayer, theEntity, false);
                }
            }
        }
    }
}