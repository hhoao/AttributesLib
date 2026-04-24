package dev.shadowsoffire.placebo.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;

public class PacketDistro {

    /** Sends a packet to all players who are watching a specific chunk. */
    public static void sendToTracking(CustomPacketPayload packet, ServerLevel world, BlockPos pos) {
        PacketDistributor.sendToPlayersTrackingChunk(world, new ChunkPos(pos), packet);
    }

    /** Sends a packet to a specific player. */
    public static void sendTo(CustomPacketPayload packet, Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, packet);
    }

    /** Sends a packet to all players on the server. */
    public static void sendToAll(CustomPacketPayload packet) {
        PacketDistributor.sendToAllPlayers(packet);
    }
}
