package mcproxy;

import com.flowpowered.network.Message;
import science.atlarge.opencraft.mcprotocollib.data.game.entity.metadata.Position;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.player.ServerPlayerAbilitiesPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerSpawnPositionPacket;
import science.atlarge.opencraft.packetlib.Session;
import science.atlarge.opencraft.packetlib.packet.Packet;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ObserverSession {
    private boolean isReady = false;
    private ObserverServer server;
    private final Queue<Packet> messageQueue = new ConcurrentLinkedDeque<>();
    private Session session;

    public ObserverSession(Session s, ObserverServer serv) {
        session = s;
        server = serv;
    }

    public Session getSession() {
        return session;
    }

    public void joinObserver() throws InterruptedException {
        ObserverServer.logger.log(Level.INFO, "JOINED AN OBSERVER");
        TimeUnit.SECONDS.sleep(2);
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
        TimeUnit.SECONDS.sleep(3);
        System.out.print("SIZE OF PLAYER JOIN QUEUE: " + server.getPlayers().size() + "\n");
//        server.getPlayers().forEach((k,v) -> {
//            session.send(k);
//            session.send(v);
//        });
        this.isReady = true;
    }

    public boolean isReady() {
        return isReady;
    }

    public Queue<Packet> getMessageQueue() {
        return messageQueue;
    }
}
