package mcproxy.Spectator;

import lombok.Getter;
import mcproxy.ObserverServer;
import mcproxy.Player;
import mcproxy.Spectator.SpectatorTicker;
import mcproxy.WorldState;
import science.atlarge.opencraft.mcprotocollib.data.game.PlayerListEntry;
import science.atlarge.opencraft.mcprotocollib.data.game.entity.metadata.Position;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.ServerPlayerListEntryPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.ServerEntityDestroyPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.player.ServerPlayerAbilitiesPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerSpawnPositionPacket;
import science.atlarge.opencraft.packetlib.Session;
import science.atlarge.opencraft.packetlib.packet.Packet;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class SpectatorSession {
    private boolean isReady = false;
    private ObserverServer server;
    @Getter
    private final Queue<Packet> messageQueue = new ConcurrentLinkedDeque<>();
    private Session session;
    private SpectatorTicker ticker;
    private Spectator spectator;

    public SpectatorSession(Session s, ObserverServer serv, Spectator spectator) {
        this.session = s;
        this.server = serv;
        this.spectator = spectator;
        this.ticker = new SpectatorTicker(this, server);
    }

    public Session getSession() {
        return session;
    }

    public void joinSpectator() throws InterruptedException {
        ObserverServer.logger.log(Level.INFO, "JOINED AN OBSERVER");
        WorldState worldState = server.getWorldState();
        Position pos =worldState.getSpawn().getPosition();
        session.send(new ServerSpawnPositionPacket(pos));
        session.send(new ServerPlayerPositionRotationPacket((double) pos.getX(), (double) pos.getY(), (double) pos.getZ(), 0f, 0f, 0));
        session.send(new ServerPlayerAbilitiesPacket(true, true, true, true, 0.2f, 1f));
        System.out.println("CHUNKS SIZE = " +  worldState.getChunkQueue().size());
        worldState.getChunkQueue().clear();
        server.getConnection().chat("/chunks");
        System.out.println("CHUNKS SIZE AFTER REQUEST= " +  worldState.getChunkQueue().size());
        TimeUnit.SECONDS.sleep(1);
        worldState.getChunkQueue().forEach(session::send);
        System.out.print("SIZE OF PLAYER JOIN QUEUE: " + worldState.getPlayersToJoin().size() + "\n");
        sendWeather();
        spawnMobs();
        spawnPlayers();
        ticker.start();
        this.isReady = true;
    }

    private void spawnPlayers() {
        server.getPlayerPositionManager().printPlayerPositions();;
        Queue<Packet> toJoin = server.getWorldState().getPlayersToJoin();
        for (Packet p : toJoin) {
            // System.out.println("TRYINIG TO SEND PLAYER INFORMATION");
            System.out.println("\n\nNUMBER OF PLAYERS: " + server.getPlayerPositionManager().getEntityList().size());
            System.out.println("TO JOIN NUMBER: " + toJoin.size());
            ServerPlayerListEntryPacket lep = (ServerPlayerListEntryPacket) p;
            session.send(p);
            for (PlayerListEntry entry : lep.getEntries()) {
                Player player = server.getPlayerPositionManager().findByUUID(entry.getProfile().getId());
                if (player != null && isInRange(player)) {
                    System.out.println("FOUND A PLAYER TO SPAWN IN");
                    System.out.println(player.getPositon().toString());
                    ServerSpawnPlayerPacket packet = new ServerSpawnPlayerPacket(player.getId(), player.getUUID(), player.getPositon().getX(), player.getPositon().getY(), player.getPositon().getZ(), 0, 0, player.getMetadata());
                    session.send(packet);
                    spectator.getPlayersInRange().add(player);
                }
            }
        }
    }

    public boolean isInRange(Player p) {
        double distance = server.getPlayerPositionManager().getDistance(spectator.getPosition().getX(), spectator.getPosition().getZ(), p.getPositon().getX(),  p.getPositon().getZ());
        return distance < 30;
    }

    private boolean isAlreadyInRange(Player p) {
        return spectator.getPlayersInRange().contains(p);
    }

    public void updatePlayersInRange() {
        ArrayList<Player> players =  server.getPlayerPositionManager().getEntityList();
        ArrayList<Player> inRange = new ArrayList<>();

        for (Player player: spectator.getPlayersInRange()) {
            if (!isInRange(player)) {
                session.send(new ServerEntityDestroyPacket(player.getId()));
            }
        }

        for (Player player : players) {
            if (isInRange(player)) {
                inRange.add(player);
                //System.out.println("PLAYER IS NOW IN RANGE");
                if (!isAlreadyInRange(player)) {
                   // System.out.println("PLAYER WASNT IN RANGE YET");
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
        System.out.println("raining is currently: " + server.getWorldState().isRaining());
        if (server.getWorldState().isRaining()) {
            session.send(server.getWorldState().getRain());
            session.send(server.getWorldState().getRainStrength());
        }
    }

    public void pulse() {
        Queue<Packet> toRemove = messageQueue;
        toRemove.forEach(session::send);
        getMessageQueue().removeAll(toRemove);
        updatePlayersInRange();
    }

    public SpectatorTicker getTicker() {
        return ticker;
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
}
