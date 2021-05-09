package mcproxy;

import com.flowpowered.network.Message;
import it.unimi.dsi.fastutil.ints.IntSets;
import lombok.Getter;
import science.atlarge.opencraft.mcprotocollib.data.game.entity.metadata.Position;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.ServerPlayerListEntryPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.player.ServerPlayerAbilitiesPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerSpawnPositionPacket;
import science.atlarge.opencraft.packetlib.Session;
import science.atlarge.opencraft.packetlib.packet.Packet;

import java.sql.Time;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ObserverSession {
    private boolean isReady = false;
    private ObserverServer server;
    @Getter
    private final Queue<Packet> messageQueue = new ConcurrentLinkedDeque<>();
    private Session session;
    private ObserverTicker ticker;

    public ObserverSession(Session s, ObserverServer serv) {
        this.session = s;
        this.server = serv;
        this.ticker = new ObserverTicker(this, server);
    }

    public Session getSession() {
        return session;
    }

    public void joinObserver() throws InterruptedException {
        ObserverServer.logger.log(Level.INFO, "JOINED AN OBSERVER");
        Position pos = server.getSpawn().getPosition();
        session.send(new ServerSpawnPositionPacket(pos));
        session.send(new ServerPlayerPositionRotationPacket((double) pos.getX(), (double) pos.getY(), (double) pos.getZ(), 0f, 0f, 0));
        session.send(new ServerPlayerAbilitiesPacket(true, true, true, true, 0.2f, 1f));
        System.out.println("CHUNKS SIZE = " +  server.getChunkQueue().size());
        server.getChunkQueue().clear();
        server.getConnection().chat("/chunks");
        System.out.println("CHUNKS SIZE AFTER REQUEST= " +  server.getChunkQueue().size());
        server.getChunkQueue().forEach(session::send);
        System.out.print("SIZE OF PLAYER JOIN QUEUE: " + server.getPlayersToJoin().size() + "\n");
        sendWeather();
        spawnMobs();
        spawnPlayers();
        ticker.start();
        this.isReady = true;
    }

    private void spawnPlayers() {
        server.getPlayerPositionManager().printPlayerPositions();;
        Queue<Packet> toJoin = server.getPlayersToJoin();
        for (Packet p : toJoin) {
            System.out.println("TRYINIG TO SEND PLAYER INFORMATION");   ServerPlayerListEntryPacket lep = (ServerPlayerListEntryPacket) p;
            Player player = server.getPlayerPositionManager().findByUUID(lep.getEntries()[0].getProfile().getId());
            if (player != null) {
                System.out.println(player.getX() + "   " + player.getY() + "   " + player.getZ());
                ServerSpawnPlayerPacket packet = new ServerSpawnPlayerPacket(player.getId(), player.getUUID(), player.getX(), player.getY(), player.getZ(), 0, 0, player.getMetadata());
                session.send(p);
                session.send(packet);
            }
        }
    }

    public void spawnMobs() {
        server.getMobQueue().forEach(session::send);
    }

    private void sendWeather() {
        session.send(server.getServerTime());
        System.out.println("raining is currently: " + server.isRaining());
        if (server.isRaining()) {
            session.send(server.getRain());
            session.send(server.getRainStrength());
        }
    }

    public void pulse() {
        Queue<Packet> toRemove = messageQueue;
        toRemove.forEach(session::send);
        server.getSessionRegistry().getSessions().forEach(s -> {
            s.getMessageQueue().removeAll(toRemove);
        });
    }

    public ObserverTicker getTicker() {
        return ticker;
    }

    public boolean isReady() {
        return isReady;
    }

    public Queue<Packet> getMessageQueue() {
        return messageQueue;
    }
}
