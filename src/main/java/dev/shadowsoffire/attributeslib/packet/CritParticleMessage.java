package dev.shadowsoffire.attributeslib.packet;

import dev.shadowsoffire.attributeslib.client.AttributesLibClient;
import dev.shadowsoffire.placebo.network.MessageHelper;
import dev.shadowsoffire.placebo.network.MessageProvider;
import java.util.function.Supplier;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class CritParticleMessage {

    protected final int entityId;

    public CritParticleMessage(int entityId) {
        this.entityId = entityId;
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
        public void handle(CritParticleMessage msg, Supplier<NetworkEvent.Context> ctx) {
            MessageHelper.handlePacket(
                    () -> {
                        AttributesLibClient.apothCrit(msg.entityId);
                    },
                    ctx);
        }
    }
}
