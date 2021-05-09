package mcproxy.Connection;


import mcproxy.Player;
import mcproxy.ObserverServer;
import science.atlarge.opencraft.mcprotocollib.MinecraftProtocol;
import science.atlarge.opencraft.mcprotocollib.data.SubProtocol;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.ServerPlayerListEntryPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.ServerEntityDestroyPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.ServerEntityPositionPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.ServerEntityTeleportPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.spawn.ServerSpawnMobPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerChunkDataPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerNotifyClientPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerSpawnPositionPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerUpdateTimePacket;
import science.atlarge.opencraft.packetlib.event.session.*;
import science.atlarge.opencraft.packetlib.packet.Packet;

import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;


public class ConListener implements SessionListener {
    private ServerConnection connection;
    private int count = 0;

    public ConListener(ServerConnection connection) {
        this.connection = connection;
    }
    @Override
    public void packetReceived(PacketReceivedEvent pre) {
        Packet packet = pre.getPacket();
       // System.out.println("received package: ," + pre.getPacket().getClass().getName().toString() +  "   | Number packages:" + count);
       // count++;
        MinecraftProtocol pro = (MinecraftProtocol) pre.getSession().getPacketProtocol();

        if (pro.getSubProtocol() == SubProtocol.GAME) {
            if (pro.getSubProtocol() == SubProtocol.GAME) {
                if (!(packet instanceof ServerEntityTeleportPacket)) {
                    connection.getServer().getSessionRegistry().getSessions().forEach(s -> {
                        if (s.isReady()) {
                            //s.getSession().send(pre.getPacket());
                            s.getMessageQueue().add(packet);
                        }
                    });
                } else {
                   // System.out.println("RECEIVED TELEPORT PACKET\n");
                    ServerEntityTeleportPacket p = (ServerEntityTeleportPacket) packet;
                    //System.out.println("ID OF ENTITY WE WANT TO TELEPORT: " + p.getEntityId());
                    if (connection.getServer().getPlayerPositionManager().findById(p.getEntityId()) != null) {
                       // System.out.println("WERE TRYING TO TELEPORT A PLAYER NOW!!!!!!!!!!!!!!!!!!");
                    }
                }
                if (packet instanceof ServerSpawnPositionPacket) {
                    ServerSpawnPositionPacket p = (ServerSpawnPositionPacket) packet;
                    System.out.println("SPAWN LOCATION RECEIVED");
                    ObserverServer server = connection.getServer();
                    server.setSpawn(p.getPosition());
                }
                if (packet instanceof ServerChunkDataPacket) {
                    ServerChunkDataPacket p = (ServerChunkDataPacket) packet;
                    connection.getServer().getChunkQueue().add(p);
                }

                if(packet instanceof ServerSpawnPlayerPacket) {
                    System.out.println("adding server join game packet to queueu");
                    ServerSpawnPlayerPacket p = (ServerSpawnPlayerPacket) packet;
                    connection.getServer().getPlayerPositionManager().getEntityList().add(new Player(p.getUUID(), p.getEntityId(),  p.getX(), p.getY(), p.getZ(), p.getMetadata()));
                    System.out.print("ENTITY NR: " + p.getUUID() + " GOT ADDED\n");
                }

                if (packet instanceof ServerPlayerListEntryPacket) {
                    ServerPlayerListEntryPacket p = (ServerPlayerListEntryPacket) packet;
                    switch (p.getAction()) {
                        case REMOVE_PLAYER:
                            System.out.println("size before remove player: " + connection.getServer().getPlayersToJoin().size() + "\n");
                            Queue<Packet> players = connection.getServer().getPlayersToJoin();
                            System.out.println("ID OF PLAYER WE WANT TO REMOVE: " + p.getEntries()[0].getProfile().getId() + "\n");
                            for (Packet player : players) {
                                ServerPlayerListEntryPacket toTest = (ServerPlayerListEntryPacket) player;
                                if (toTest.getEntries()[0].getProfile().getId().equals(p.getEntries()[0].getProfile().getId())) {
                                    connection.getServer().getPlayersToJoin().remove(toTest);
                                    connection.getServer().getPlayerPositionManager().removeEntity(p.getEntries()[0].getProfile().getId());
                                    System.out.println("ID OF PLAYER WE REMOVED: " + toTest.getEntries()[0].getProfile().getId() + "\n");
                                    break;
                                }
                            }
                            System.out.println("size after remove player: " + connection.getServer().getPlayersToJoin().size() + "\n");
                        break;
                        case ADD_PLAYER:
                            System.out.println("size before adding player: " + connection.getServer().getPlayersToJoin().size() + "\n");
                            connection.getServer().getPlayersToJoin().add(p);
                            System.out.println("size after adding player: " + connection.getServer().getPlayersToJoin().size() + "\n");
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

                if(packet instanceof ServerEntityPositionPacket) {
                    ServerEntityPositionPacket p = (ServerEntityPositionPacket) packet;
                    connection.getServer().getPlayerPositionManager().updatEntityPosition(p.getEntityId(), p);
                    count++;
                    System.out.println("Number of position packets: " + count);
                }

                if(packet instanceof ServerSpawnMobPacket) {
                    ServerSpawnMobPacket p = (ServerSpawnMobPacket) packet;
                    connection.getServer().getMobQueue().add(p);
                }

                if (packet instanceof ServerEntityDestroyPacket) {
                    ServerEntityDestroyPacket p = (ServerEntityDestroyPacket) packet;
                    //connection.getServer().getEntityManager().removeEntity(p.getEntityIds()[0]);
                }

                if (packet instanceof ServerUpdateTimePacket) {
                    ServerUpdateTimePacket p = (ServerUpdateTimePacket) packet;
                    connection.getServer().setServerTime(p);
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
