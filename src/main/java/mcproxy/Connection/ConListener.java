package mcproxy.Connection;


import com.flowpowered.network.ConnectionManager;
import mcproxy.ObserverServer;
import org.bukkit.event.entity.EntityTeleportEvent;
import science.atlarge.opencraft.mcprotocollib.MinecraftProtocol;
import science.atlarge.opencraft.mcprotocollib.data.SubProtocol;
import science.atlarge.opencraft.mcprotocollib.data.game.entity.player.GameMode;
import science.atlarge.opencraft.mcprotocollib.data.game.world.notify.ClientNotification;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.ServerPlayerListEntryPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.ServerEntityPositionRotationPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.ServerEntityTeleportPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerChunkDataPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerNotifyClientPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerSpawnPositionPacket;
import science.atlarge.opencraft.packetlib.event.session.*;
import science.atlarge.opencraft.packetlib.packet.Packet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;


public class ConListener implements SessionListener {
    private ServerConnection connection;
    private int count;
    private Lock lock = new ReentrantLock();

    public ConListener(ServerConnection connection) {
        this.connection = connection;
    }
    @Override
    public void packetReceived(PacketReceivedEvent pre) {
        Packet packet = pre.getPacket();
       // System.out.println("received package: ," + pre.getPacket().getClass().getName().toString() +  "   | Number packages:" + count);
        count++;
        MinecraftProtocol pro = (MinecraftProtocol) pre.getSession().getPacketProtocol();

        if (pro.getSubProtocol() == SubProtocol.GAME) {
            if (pro.getSubProtocol() == SubProtocol.GAME) {
                if (!(packet instanceof ServerEntityTeleportPacket)) {
                    connection.getServer().getSessionRegistry().getSessions().forEach(s -> {
                        if (s.isReady()) {
                            s.getSession().send(pre.getPacket());
                            //s.getMessageQueue().add(packet);
                        }
                    });
            }
                if (packet instanceof ServerSpawnPositionPacket) {
                    ServerSpawnPositionPacket p = (ServerSpawnPositionPacket) packet;
                    System.out.println("SPAWN LOCATION RECEIVED");
                    ObserverServer server = connection.getServer();
                    server.setSpawn(p.getPosition());

                }
                if (packet instanceof ServerChunkDataPacket) {
                    ServerChunkDataPacket p = (ServerChunkDataPacket) packet;
                   // System.out.println("CHUNK X: " + p.getColumn().getX() + " CHUNK Z: " + p.getColumn().getZ() + "\n");
                   // System.out.println("added CHUNK packet to queue");
                    connection.getServer().getChunkQueue().add(p);
                }

                if(packet instanceof ServerSpawnPlayerPacket) {
                    System.out.println("adding server join game packet to queueu");
                    ServerSpawnPlayerPacket p = (ServerSpawnPlayerPacket) packet;
                    UUID PID = p.getUUID();
                    connection.getServer().getPlayers().forEach((k,v) -> {
                        ServerPlayerListEntryPacket pack = (ServerPlayerListEntryPacket) k;
                        if (pack.getEntries()[0].getProfile().getId().equals(PID)) {
                            connection.getServer().getPlayers().replace(k, null, p);
                        }
                    });
                    System.out.print("ENTITY NR: " + p.getUUID() + " GOT ADDED\n");
                }

                if (packet instanceof ServerPlayerListEntryPacket) {
                    ServerPlayerListEntryPacket p = (ServerPlayerListEntryPacket) packet;
                    switch (p.getAction()) {
                        case REMOVE_PLAYER:

                            System.out.println("size before remove player: " + connection.getServer().getPlayers().size() + "\n");
                            Set<Packet> keyset =  connection.getServer().getPlayers().keySet();
                            System.out.println("ID OF PLAYER WE WANT TO REMOVE: " + p.getEntries()[0].getProfile().getId() + "\n");
                            for (Packet value : keyset) {
                                ServerPlayerListEntryPacket toTest = (ServerPlayerListEntryPacket) value;
                                if (toTest.getEntries()[0].getProfile().getId().equals(p.getEntries()[0].getProfile().getId())) {
                                    connection.getServer().getPlayers().remove(toTest);
                                    System.out.println("ID OF PLAYER WE REMOVED: " + toTest.getEntries()[0].getProfile().getId() + "\n");
                                    break;
                                }
                            }

                            System.out.println("size after remove player: " + connection.getServer().getPlayers().size() + "\n");
                        break;
                        case ADD_PLAYER:
                            System.out.println("size before adding player: " + connection.getServer().getPlayers().size() + "\n");
                            connection.getServer().getPlayers().put(p, null);
                            System.out.println("size after adding player: " + connection.getServer().getPlayers().size() + "\n");
                    }
                }
                if (packet instanceof ServerNotifyClientPacket) {
                    ServerNotifyClientPacket p = (ServerNotifyClientPacket) packet;
                    switch (p.getNotification()) {
                        case START_RAIN:
                            connection.getServer().setRain(p);
                            connection.getServer().setRaining(true);
                            break;
                        case RAIN_STRENGTH:
                            connection.getServer().setRainStrength(p);
                            break;
                        case STOP_RAIN:
                            connection.getServer().setRaining(false);
                    }
                }
                if(packet instanceof ServerEntityPositionRotationPacket) {
                    ServerEntityPositionRotationPacket p = (ServerEntityPositionRotationPacket) packet;
                    p.
                }
            }
        }
    }

    @Override
    public void packetSending(PacketSendingEvent packetSendingEvent) {
        System.out.println("Sending non-game packet: " + packetSendingEvent.getPacket().getClass().getName());
    }

    @Override
    public void packetSent(PacketSentEvent packetSentEvent) {

    }

    @Override
    public void connected(ConnectedEvent connectedEvent) {
        connection.getLogger().log(Level.INFO, "Connection to minecraft server established");
    }

    @Override
    public void disconnecting(DisconnectingEvent disconnectingEvent) {
        connection.getLogger().log(Level.INFO, "Disconnecting from server, reason:" + disconnectingEvent.getReason().toString() + "\n");
    }

    @Override
    public void disconnected(DisconnectedEvent disconnectedEvent) {
        connection.getLogger().log(Level.INFO, "Disconnected from server\n");
    }
}
