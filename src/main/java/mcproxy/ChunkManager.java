package mcproxy;

import mcproxy.Spectator.SpectatorSession;
import mcproxy.util.Modified;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.client.ClientChatPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerBlockChangePacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerChunkDataPacket;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkManager {
    private ObserverServer server;
    private ConcurrentHashMap<ServerChunkDataPacket, Modified> chunkMap = new ConcurrentHashMap<>();

    public ChunkManager(ObserverServer serv) {
        this.server = serv;
    }

    public void sendChunks(SpectatorSession session) {
        HashSet<ServerChunkDataPacket> toRefresh = new HashSet<>();
        //System.out.println(toRefresh.size());
        chunkMap.forEach((c, q) -> {
            if (!q.getModified()) {
                session.getSession().send(c);
            } else {
                toRefresh.add(c);
            }
        });

        chunkMap.keySet().removeAll(toRefresh);
        toRefresh.forEach(p -> {
            int x = (int) Math.ceil(p.getColumn().getX());
            int y = (int) Math.ceil(p.getColumn().getZ());
            requestChunk(x, y);
        });
    }

    public void setModified(ServerBlockChangePacket packet) {
        int x = (packet.getRecord().getPosition().getX() >> 4);
        int z = (packet.getRecord().getPosition().getZ() >> 4);
        //System.out.println("block coords:" + x + "  " + z);

        //System.out.println(x + "  "+ z);
        ServerChunkDataPacket target = getChunk(x, z);
        if (target != null) chunkMap.get(target).setModified(true);
    }

    private void requestChunk(int x, int z) {
       // System.out.println("requesting chunk");
        server.getConnection().getSession().send(new ClientChatPacket("/requestchunk " + x + "," + z));
    }

    public void addChunk(ServerChunkDataPacket packet) {
        chunkMap.put(packet, new Modified(false));
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
