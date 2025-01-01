package dev.shadowsoffire.placebo.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class MessageHelper {

    /**
     * Registers a message directly using the base FML systems. Requires providing each method individually.
     *
     * @param channel         Channel to register for.
     * @param id              Message id.
     * @param messageType     Class of the message object.
     * @param encoder         Method to write the method to buf.
     * @param decoder         Method to read the message from buf.
     * @param messageConsumer Executor for the read message.
     */
    public static <MSG> void registerMessage(SimpleChannel channel, int id, Class<MSG> messageType, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder,
        BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        channel.registerMessage(id, messageType, encoder, decoder, messageConsumer);
    }

    /**
     * Registers a message using {@link MessageProvider}.
     *
     * @param channel Channel to register for.
     * @param id      Message id.
     * @param prov    An instance of the message provider. Note that this object will be kept around, so try to keep all fields uninitialized if possible.
     */
    @SuppressWarnings("unchecked")
    public static <T> void registerMessage(SimpleChannel channel, int id, MessageProvider<T> prov) {
        channel.registerMessage(id, (Class<T>) prov.getMsgClass(), prov::write, prov::read, prov::handle, prov.getNetworkDirection());
    }

    /**
     * Handles a packet. Enqueues the runnable to run on the main thread and calls {@link NetworkEvent.Context#setPacketHandled(boolean)}
     *
     * @param r   Code to run to handle the packet. Will be executed on the main thread.
     * @param ctx Context object. Available from {@link MessageProvider#handle(Object, Supplier)}
     */
    public static void handlePacket(Runnable r, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(r);
        ctx.setPacketHandled(true);
    }

}
