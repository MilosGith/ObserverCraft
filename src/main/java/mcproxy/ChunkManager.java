package mcproxy;

import mcproxy.Spectator.SpectatorSession;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.client.ClientChatPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerBlockChangePacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerChunkDataPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerUnloadChunkPacket;

import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ChunkManager {
    private ObserverServer server;
    private ConcurrentHashMap<ServerChunkDataPacket, Boolean> chunkMap = new ConcurrentHashMap<>();

    public ChunkManager(ObserverServer serv) {
        this.server = serv;
    }

    public void sendChunks(SpectatorSession session) {
        HashSet<ServerChunkDataPacket> toRefresh = new HashSet<>();
        //System.out.println(toRefresh.size());
        chunkMap.forEach((c, q) -> {
            if (!q) {
                session.getSession().send(c);
            } else {
                toRefresh.add(c);
            }
        });

        chunkMap.keySet().removeAll(toRefresh);
        requestChunks(toRefresh);
    }

    public void setModified(ServerBlockChangePacket packet) {
        int x = (packet.getRecord().getPosition().getX() >> 4);
        int z = (packet.getRecord().getPosition().getZ() >> 4);
        //System.out.println("block coords:" + x + "  " + z);

        //System.out.println(x + "  "+ z);
        ServerChunkDataPacket target = getChunk(x, z);
        if (target != null) chunkMap.replace(target, true);
    }

    public void removeChunk(ServerUnloadChunkPacket packet) {
        if (containsChunk(packet.getX(), packet.getZ())) {
            chunkMap.remove(getChunk(packet.getX(), packet.getZ()));
        }
    }

    private void requestChunks(HashSet<ServerChunkDataPacket> toRefresh) {
        StringBuilder builder = new StringBuilder();
        builder.append("/requestchunks");
        toRefresh.forEach(p -> {
            int x = p.getColumn().getX();
            int z = p.getColumn().getZ();
            builder.append(" ").append(x).append(",").append(z);
        });
        ObserverServer.logger.log(Level.INFO, builder.toString());
        server.getConnection().getSession().send(new ClientChatPacket(builder.toString()));
    }

    public void addChunk(ServerChunkDataPacket packet) {
        chunkMap.put(packet, false);
    }

    private ServerChunkDataPacket getChunk(int x, int z) {
        Optional<ServerChunkDataPacket> result =
                chunkMap.keySet().stream().filter(chunk -> chunk.getColumn().getX() == x && chunk.getColumn().getZ() == z).findFirst();
        return result.orElse(null);
    }

    public boolean containsChunk (int x, int z) {
        return chunkMap.keySet().stream().anyMatch(o -> o.getColumn().getX() == x && o.getColumn().getZ() == z);
    }
}
