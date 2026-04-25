package dev.shadowsoffire.placebo.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketDistro {

    /** Sends a packet to all players who are watching a specific chunk. */
    public static void sendToTracking(
            SimpleChannel channel, Object packet, ServerWorld world, BlockPos pos) {
        world.getChunkProvider()
                .chunkManager
                .getTrackingPlayers(new ChunkPos(pos), false)
                .forEach(
                        p -> {
                            channel.sendTo(
                                    packet,
                                    p.connection.netManager,
                                    NetworkDirection.PLAY_TO_CLIENT);
                        });
    }

    /** Sends a packet to a specific player. */
    public static void sendTo(SimpleChannel channel, Object packet, PlayerEntity player) {
        channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), packet);
    }

    /** Sends a packet to all players on the server. */
    public static void sendToAll(SimpleChannel channel, Object packet) {
        channel.send(PacketDistributor.ALL.noArg(), packet);
    }
}
