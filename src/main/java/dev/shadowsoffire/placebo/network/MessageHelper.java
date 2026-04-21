package dev.shadowsoffire.placebo.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageHelper {

    /**
     * Registers a {@link MessageProvider} onto a {@link SimpleNetworkWrapper}. Internally wraps the
     * provider's {@code handle} in an {@link IMessageHandler} and relies on the message class
     * itself implementing {@link IMessage#fromBytes}/{@link IMessage#toBytes} (typically by
     * delegating back to {@code provider.read/write}).
     */
    public static <T extends IMessage> void registerMessage(
            SimpleNetworkWrapper channel, int id, MessageProvider<T> prov) {
        channel.registerMessage(new ProviderHandler<>(prov), prov.getMsgClass(), id, prov.getReceiveSide());
    }

    /** Runs {@code r} on the main thread for the side where the packet was received. */
    public static void handlePacket(Runnable r, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            scheduleClient(r);
        } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(r);
        }
    }

    @SideOnly(Side.CLIENT)
    private static void scheduleClient(Runnable r) {
        Minecraft.getMinecraft().addScheduledTask(r);
    }

    /**
     * Delegating handler — SimpleNetworkWrapper requires a concrete {@link IMessageHandler}, so we
     * capture the provider instance here and forward.
     */
    private static final class ProviderHandler<T extends IMessage> implements IMessageHandler<T, IMessage> {
        private final MessageProvider<T> provider;

        ProviderHandler(MessageProvider<T> provider) {
            this.provider = provider;
        }

        @Override
        public IMessage onMessage(T message, MessageContext ctx) {
            this.provider.handle(message, ctx);
            return null;
        }
    }
}
