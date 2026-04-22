package dev.shadowsoffire.placebo.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class PacketDistro {

    /**
     * Sends a packet to every player currently tracking the chunk that contains {@code pos} on the
     * given server world.
     */
    public static void sendToTracking(
            SimpleNetworkWrapper channel, IMessage packet, WorldServer world, BlockPos pos) {
        Chunk chunk = world.getChunkFromBlockCoords(pos);
        for (EntityPlayer p : world.playerEntities) {
            if (!(p instanceof EntityPlayerMP)) {
                continue;
            }
            EntityPlayerMP mp = (EntityPlayerMP) p;
            if (world.getPlayerChunkMap().isPlayerWatchingChunk(mp, chunk.x, chunk.z)) {
                channel.sendTo(packet, mp);
            }
        }
    }

    /** Sends a packet to a specific player. No-op if the player is a non-MP (i.e. client) player. */
    public static void sendTo(SimpleNetworkWrapper channel, IMessage packet, EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            channel.sendTo(packet, (EntityPlayerMP) player);
        }
    }

    /** Sends a packet to every player connected to the server. */
    public static void sendToAll(SimpleNetworkWrapper channel, IMessage packet) {
        channel.sendToAll(packet);
    }

    /** Sends a packet from client to server. */
    public static void sendToServer(SimpleNetworkWrapper channel, IMessage packet) {
        channel.sendToServer(packet);
    }
}
