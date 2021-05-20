package mcproxy.Spectator;

import lombok.Getter;
import mcproxy.ObserverServer;
import mcproxy.Player;
import mcproxy.WorldState;
import science.atlarge.opencraft.mcprotocollib.data.game.PlayerListEntry;
import science.atlarge.opencraft.mcprotocollib.data.game.entity.metadata.Position;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.ServerPlayerListEntryPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.ServerEntityDestroyPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.player.ServerPlayerAbilitiesPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerChunkDataPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerSpawnPositionPacket;
import science.atlarge.opencraft.packetlib.Session;
import science.atlarge.opencraft.packetlib.packet.Packet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class SpectatorSession {
    private boolean isReady = false;
    private ObserverServer server;
    @Getter
    private final Queue<Packet> messageQueue = new ConcurrentLinkedDeque<>();
    private final Set<Packet> receivedChunks = new HashSet<>();
    private Session session;
    private Spectator spectator;
    private packetForwarder packetForwarder;

    public SpectatorSession(Session s, ObserverServer serv, Spectator spectator) {
        this.session = s;
        this.server = serv;
        this.spectator = spectator;
        this.packetForwarder = new packetForwarder(SpectatorSession.this);
    }

    public Session getSession() {
        return session;
    }

    public void joinSpectator() throws InterruptedException {
        //ObserverServer.logger.log(Level.INFO, "JOINED AN OBSERVER");
        WorldState worldState = server.getWorldState();
        Position pos =worldState.getSpawn().getPosition();
        session.send(new ServerSpawnPositionPacket(pos));
        session.send(new ServerPlayerPositionRotationPacket((double) pos.getX(), (double) pos.getY(), (double) pos.getZ(), 0f, 0f, 0));
        session.send(new ServerPlayerAbilitiesPacket(true, true, true, true, 0.2f, 1f));
       // System.out.println("CHUNKS SIZE = " +  worldState.getChunkQueue().size());
        worldState.getChunkQueue().clear();
        server.getConnection().chat("/chunks");
       // System.out.println("CHUNKS SIZE AFTER REQUEST= " +  worldState.getChunkQueue().size());
        TimeUnit.SECONDS.sleep(1);
        sendChunks();
       // System.out.print("SIZE OF PLAYER JOIN QUEUE: " + worldState.getPlayersToJoin().size() + "\n");
        sendWeather();
        spawnMobs();
        spawnPlayers();
        this.isReady = true;
    }

    private void spawnPlayers() {
        server.getWorldState().getPlayerPositionManager().printPlayerPositions();;
        Queue<Packet> toJoin = server.getWorldState().getPlayersToJoin();
        for (Packet p : toJoin) {
            // System.out.println("TRYINIG TO SEND PLAYER INFORMATION");
           // System.out.println("\n\nNUMBER OF PLAYERS: " +  server.getWorldState().getPlayerPositionManager().getEntityList().size());
          //  System.out.println("TO JOIN NUMBER: " + toJoin.size());
            ServerPlayerListEntryPacket lep = (ServerPlayerListEntryPacket) p;
            session.send(p);
            for (PlayerListEntry entry : lep.getEntries()) {
                Player player =  server.getWorldState().getPlayerPositionManager().findByUUID(entry.getProfile().getId());
                if (player != null && isInRange(player)) {
                 //   System.out.println("FOUND A PLAYER TO SPAWN IN");
                    //System.out.println(player.getPositon().toString());
                    ServerSpawnPlayerPacket packet = new ServerSpawnPlayerPacket(player.getId(), player.getUUID(), player.getPositon().getX(), player.getPositon().getY(), player.getPositon().getZ(), 0, 0, player.getMetadata());
                    session.send(packet);
                    spectator.getPlayersInRange().add(player);
                }
            }
        }
    }

    public boolean hasChunk (int x, int z) {
        return getReceivedChunks().stream().anyMatch(o -> ((ServerChunkDataPacket) o).getColumn().getX() == x && ((ServerChunkDataPacket) o).getColumn().getZ() == z);
    }

    public void sendChunks() {
      //  System.out.println("SENDING CHUNKS TO NEW CONNECTION");
       // System.out.println("CHUNK QUEUE SIZE: " + server.getWorldState().getChunkQueue().size());
        server.getWorldState().getChunkQueue().forEach(c -> {
           // System.out.println("WERE SENDING A CHUNK NOW");
            session.send(c);
            receivedChunks.add(c);
        });
    }

    public boolean isInRange(Player p) {
        double distance =  server.getWorldState().getPlayerPositionManager().getDistance(spectator.getPosition().getX(), spectator.getPosition().getZ(), p.getPositon().getX(),  p.getPositon().getZ());
        return distance < 175;
    }

    private boolean isAlreadyInRange(Player p) {
        return spectator.getPlayersInRange().contains(p);
    }

    private void updatePlayersInRange() {
        ArrayList<Player> players =   server.getWorldState().getPlayerPositionManager().getEntityList();
        ArrayList<Player> inRange = new ArrayList<>();

        for (Player player: spectator.getPlayersInRange()) {
            if (!isInRange(player) || player.getId() == packetForwarder.followId) {
                session.send(new ServerEntityDestroyPacket(player.getId()));
            }
        }

        for (Player player : players) {
            if (isInRange(player) && !(player.getId() == packetForwarder.followId)) {
                inRange.add(player);
                if (!isAlreadyInRange(player)) {
                    session.send(new ServerSpawnPlayerPacket(player.getId(), player.getUUID(), player.getPositon().getX(), player.getPositon().getY(), player.getPositon().getZ(), 0, 0, player.getMetadata()));
                }
            }
        }
        spectator.updatePlayersInRange(inRange);
    }


    private void spawnMobs() {
        server.getWorldState().getMobQueue().forEach(session::send);
    }

    private void sendWeather() {
        session.send(server.getWorldState().getServerTime());
       // System.out.println("raining is currently: " + server.getWorldState().isRaining());
        if (server.getWorldState().isRaining()) {
            session.send(server.getWorldState().getRain());
            session.send(server.getWorldState().getRainStrength());
        }
    }

    public void pulse() {
        if (isReady) {
            Queue<Packet> toRemove = messageQueue;
            toRemove.forEach(session::send);
            getMessageQueue().removeAll(toRemove);
            updatePlayersInRange();
        }
    }

    public Set<Packet> getReceivedChunks() {
        return receivedChunks;
    }

    public packetForwarder getPacketForwarder() {
        return packetForwarder;
    }


    public Spectator getSpectator() {
        return spectator;
    }

    public boolean isReady() {
        return isReady;
    }

    public Queue<Packet> getMessageQueue() {
        return messageQueue;
    }

    public ObserverServer getServer() { return server; }
}
