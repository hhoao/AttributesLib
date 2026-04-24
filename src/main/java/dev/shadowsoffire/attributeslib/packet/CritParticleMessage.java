package dev.shadowsoffire.attributeslib.packet;

import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.client.AttributesLibClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CritParticleMessage(int entityId) implements CustomPacketPayload {

    public static final Type<CritParticleMessage> TYPE =
            new Type<>(AttributesLib.loc("crit_particle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CritParticleMessage> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, CritParticleMessage::entityId, CritParticleMessage::new);

    @Override
    public Type<CritParticleMessage> type() {
        return TYPE;
    }

    public static void handle(CritParticleMessage msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> AttributesLibClient.apothCrit(msg.entityId()));
    }
}
