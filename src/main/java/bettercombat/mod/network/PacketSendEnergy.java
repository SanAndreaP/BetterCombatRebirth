package bettercombat.mod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSendEnergy implements IMessage
{
    private int amount;

    @Override
    public void fromBytes(ByteBuf buf) {
        this.amount = ByteBufUtils.readVarInt(buf, 4);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarInt(buf, this.amount, 4);
    }

    @SuppressWarnings("unused")
    public PacketSendEnergy() {
        this.amount = 0;
    }

    public PacketSendEnergy(int amount) {
        this.amount = amount;
    }

    public static class Handler
            implements IMessageHandler<PacketSendEnergy, IMessage>
    {
        @Override
        public IMessage onMessage(final PacketSendEnergy message, final MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private static void handle(PacketSendEnergy message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            if( playerEntity != null ) {
                playerEntity.ticksSinceLastSwing = message.amount;
            }
        }
    }
}