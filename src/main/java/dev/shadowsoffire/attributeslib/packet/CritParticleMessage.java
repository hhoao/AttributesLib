package dev.shadowsoffire.attributeslib.packet;

import dev.shadowsoffire.attributeslib.client.AttributesLibClient;
import dev.shadowsoffire.placebo.network.MessageHelper;
import dev.shadowsoffire.placebo.network.MessageProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class CritParticleMessage implements IMessage {

    protected int entityId;

    public CritParticleMessage() {}

    public CritParticleMessage(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
    }

    public static class Provider implements MessageProvider<CritParticleMessage> {

        @Override
        public Class<CritParticleMessage> getMsgClass() {
            return CritParticleMessage.class;
        }

        @Override
        public void write(CritParticleMessage msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
        }

        @Override
        public CritParticleMessage read(PacketBuffer buf) {
            return new CritParticleMessage(buf.readInt());
        }

        @Override
        public void handle(CritParticleMessage msg, MessageContext ctx) {
            MessageHelper.handlePacket(
                    () -> AttributesLibClient.apothCrit(msg.entityId), ctx);
        }

        @Override
        public Side getReceiveSide() {
            return Side.CLIENT;
        }
    }
}
