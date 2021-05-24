package mcproxy.Connection;


import mcproxy.ObserverServer;
import mcproxy.Player;
import mcproxy.WorldState;
import mcproxy.util.WorldPosition;
import org.bukkit.material.Observer;
import science.atlarge.opencraft.mcprotocollib.MinecraftProtocol;
import science.atlarge.opencraft.mcprotocollib.data.SubProtocol;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.ServerPlayerListEntryPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.*;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.spawn.ServerSpawnMobPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.*;
import science.atlarge.opencraft.packetlib.event.session.*;
import science.atlarge.opencraft.packetlib.packet.Packet;

import java.util.Queue;
import java.util.logging.Level;


public class ConListener implements SessionListener {
    private ServerConnection connection;
    private int count = 0;

    public ConListener(ServerConnection connection) {
        this.connection = connection;
    }
    @Override
    public void packetReceived(PacketReceivedEvent pre) {
        WorldState worldState = connection.getServer().getWorldState();

        Packet packet = pre.getPacket();
        //System.out.println("received package: ," + pre.getPacket().getClass().getName().toString() +  "   | Number packages:" + count);
        // count++;
        MinecraftProtocol pro = (MinecraftProtocol) pre.getSession().getPacketProtocol();

        if (pro.getSubProtocol() == SubProtocol.GAME) {
            connection.getServer().getSessionRegistry().getSessions().forEach(s -> {
                s.getPacketForwarder().forwardPacket(packet);
            });

            if (packet instanceof ServerSpawnPositionPacket) {
                ServerSpawnPositionPacket p = (ServerSpawnPositionPacket) packet;
                worldState.setSpawn(p.getPosition());
            }

            else if (packet instanceof ServerChunkDataPacket) {
                ServerChunkDataPacket p = (ServerChunkDataPacket) packet;
                worldState.getChunkQueue().add(p);
            }

            else if(packet instanceof ServerSpawnPlayerPacket) {
                ServerSpawnPlayerPacket p = (ServerSpawnPlayerPacket) packet;
                if (connection.getServer().getWorldState().getPlayerPositionManager().findById(p.getEntityId()) == null) {
                    connection.getServer().getWorldState().getPlayerPositionManager().getEntityList().add(new Player(p.getUUID(), p.getEntityId(), new WorldPosition(p.getX(), p.getY(), p.getZ()), p.getMetadata()));
                }

            }

            else if (packet instanceof ServerPlayerListEntryPacket) {
                ServerPlayerListEntryPacket p = (ServerPlayerListEntryPacket) packet;
                switch (p.getAction()) {
                    case REMOVE_PLAYER:
                 //       System.out.println("size before remove player: " + worldState.getPlayersToJoin().size() + "\n");
                        Queue<Packet> players = worldState.getPlayersToJoin();
                  //      System.out.println("ID OF PLAYER WE WANT TO REMOVE: " + p.getEntries()[0].getProfile().getId() + "\n");
                        for (Packet player : players) {
                            ServerPlayerListEntryPacket toTest = (ServerPlayerListEntryPacket) player;
                            if (toTest.getEntries()[0].getProfile().getId().equals(p.getEntries()[0].getProfile().getId())) {
                                worldState.getPlayersToJoin().remove(toTest);
                                connection.getServer().getWorldState().getPlayerPositionManager().removeEntity(p.getEntries()[0].getProfile().getId());
                        //        System.out.println("ID OF PLAYER WE REMOVED: " + toTest.getEntries()[0].getProfile().getId() + "\n");
                                break;
                            }
                        }
                     //   System.out.println("size after remove player: " + worldState.getPlayersToJoin().size() + "\n");
                    break;
                    case ADD_PLAYER:
                   //     System.out.println("size before adding player: " + worldState.getPlayersToJoin().size() + "\n");
                        worldState.getPlayersToJoin().add(p);
                     //   System.out.println("size after adding player: " + worldState.getPlayersToJoin().size() + "\n");
                }
            }

            else if (packet instanceof ServerNotifyClientPacket) {
                ServerNotifyClientPacket p = (ServerNotifyClientPacket) packet;
                switch (p.getNotification()) {
                    case START_RAIN:
                        worldState.setRain(p);
                        worldState.setRaining(true);
                        break;
                    case RAIN_STRENGTH:
                        worldState.setRainStrength(p);
                        break;
                    case STOP_RAIN:
                        worldState.setRaining(false);
                }
            }

            else if(packet instanceof ServerEntityPositionPacket) {
                ServerEntityPositionPacket p = (ServerEntityPositionPacket) packet;
                connection.getServer().getWorldState().getPlayerPositionManager().updatEntityPosition(p.getEntityId(), p.getMovementX(), p.getMovementY(), p.getMovementZ());
            }

            else if (packet instanceof  ServerEntityPositionRotationPacket) {
                ServerEntityPositionRotationPacket p = (ServerEntityPositionRotationPacket) packet;
                connection.getServer().getWorldState().getPlayerPositionManager().updatEntityPosition(p.getEntityId(), p.getMovementX(), p.getMovementY(), p.getMovementZ());
            }

            else if(packet instanceof ServerSpawnMobPacket) {
                ServerSpawnMobPacket p = (ServerSpawnMobPacket) packet;
                worldState.getMobQueue().add(p);
            }

            else if (packet instanceof ServerUpdateTimePacket) {
                ServerUpdateTimePacket p = (ServerUpdateTimePacket) packet;
                worldState.setServerTime(p);
            }

            else if (packet instanceof ServerEntityDestroyPacket) {
                ServerEntityDestroyPacket p = (ServerEntityDestroyPacket) packet;
                if (connection.getServer().getWorldState().getPlayerPositionManager().findById(p.getEntityIds()[0]) != null) {
                }
            }

            else if (packet instanceof ServerUnloadChunkPacket) {
                ServerUnloadChunkPacket  p = (ServerUnloadChunkPacket) packet;
            }

            else if (packet instanceof  ServerEntityTeleportPacket) {
                ServerEntityTeleportPacket p = (ServerEntityTeleportPacket) packet;
                Player player = connection.getServer().getWorldState().getPlayerPositionManager().findById(p.getEntityId());
                if (player != null) {
                    player.getPositon().updatePosition(p.getX(), p.getY(), p.getZ());
                }
            }
        }
    }

    @Override
    public void packetSending(PacketSendingEvent packetSendingEvent) {
       // System.out.println("Sending non-game packet: " + packetSendingEvent.getPacket().getClass().getName());
    }

    @Override
    public void packetSent(PacketSentEvent packetSentEvent) {

    }

    @Override
    public void connected(ConnectedEvent connectedEvent) {
        ObserverServer.logger.log(Level.INFO, "Connection to minecraft server established");
    }

    @Override
    public void disconnecting(DisconnectingEvent disconnectingEvent) {
        System.out.println(disconnectingEvent.getCause().toString());
        ObserverServer.logger.info( "Disconnecting from server, reason:" + disconnectingEvent.getReason().toString() + "\n");
    }

    @Override
    public void disconnected(DisconnectedEvent disconnectedEvent) {
        ObserverServer.logger.info("Disconnected from server\n");
    }
}
