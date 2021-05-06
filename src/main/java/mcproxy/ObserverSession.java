package mcproxy;

import com.flowpowered.network.Message;
import science.atlarge.opencraft.mcprotocollib.data.game.entity.metadata.Position;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.player.ServerPlayerAbilitiesPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
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
        TimeUnit.SECONDS.sleep(1);
        System.out.println("CHUNKS SIZE AFTER REQUEST= " +  server.getChunkQueue().size());
        server.getChunkQueue().forEach(session::send);
        System.out.print("SIZE OF PLAYER JOIN QUEUE: " + server.getPlayers().size() + "\n");
        sendWeather();
        spawnPlayers();
        this.isReady = true;
        //ticker.start();
    }


    private void spawnPlayers() {
        server.getPlayers().forEach((k,v) -> {
            if (v != null && k!= null) {
                System.out.println("trying to send spawn info");
                session.send(k);
                session.send(v);
                System.out.println("SEND BOTH SPAWN PACKETS");
            }
        });
    }

    private void sendWeather() {
        System.out.println("raining is currently: " + server.isRaining());
        if (server.isRaining()) {
            session.send(server.getRain());
            session.send(server.getRainStrength());
        }
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
