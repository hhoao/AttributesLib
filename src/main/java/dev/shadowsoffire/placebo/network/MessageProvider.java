package dev.shadowsoffire.placebo.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 1.12.2-flavoured message provider. The message type must itself implement {@link IMessage}; the
 * provider exposes encode/decode/handle hooks that {@link MessageHelper} stitches together when
 * registering on a {@link net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper}.
 *
 * @param <T> The message class. Must implement {@link IMessage}.
 */
public interface MessageProvider<T extends IMessage> {

    /** @return The class of the message being provided, for registration to the channel. */
    Class<T> getMsgClass();

    /** Writes the message payload. Called from {@code IMessage#toBytes}. */
    void write(T msg, PacketBuffer buf);

    /** Reads a fresh message from the buffer. Called from {@code IMessage#fromBytes}. */
    T read(PacketBuffer buf);

    /** Handle the deserialised message. Use {@link MessageHelper#handlePacket} to dispatch. */
    void handle(T msg, MessageContext ctx);

    /** Side on which the message is received (i.e. where {@link #handle} will run). */
    Side getReceiveSide();
}
