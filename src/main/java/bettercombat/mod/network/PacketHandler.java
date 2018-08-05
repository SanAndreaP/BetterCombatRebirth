package bettercombat.mod.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketHandler
{
    public static SimpleNetworkWrapper instance = null;

    public static void registerMessages(String channelName) {
        instance = NetworkRegistry.INSTANCE.newSimpleChannel(channelName);
        registerMessages();
    }

    public static void registerMessages() {
        instance.registerMessage(PacketSendEnergy.Handler.class, PacketSendEnergy.class, 1, Side.SERVER);
        instance.registerMessage(PacketOffhandAttack.Handler.class, PacketOffhandAttack.class, 2, Side.SERVER);
        instance.registerMessage(PacketMainhandAttack.Handler.class, PacketMainhandAttack.class, 3, Side.SERVER);
        instance.registerMessage(PacketOffhandCooldown.ServerHandler.class, PacketOffhandCooldown.class, 4, Side.SERVER);
    }

    @SideOnly(Side.CLIENT)
    public static void registerClientMessages() {
        instance.registerMessage(PacketOffhandCooldown.ClientHandler.class, PacketOffhandCooldown.class, 4, Side.CLIENT);
    }
}