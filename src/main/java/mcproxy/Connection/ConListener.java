package mcproxy.Connection;


import mcproxy.Player;
import mcproxy.ObserverServer;
import mcproxy.WorldState;
import mcproxy.util.WorldPosition;
import science.atlarge.opencraft.mcprotocollib.MinecraftProtocol;
import science.atlarge.opencraft.mcprotocollib.data.SubProtocol;
import science.atlarge.opencraft.mcprotocollib.data.game.PlayerListEntry;
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
                if (s.isReady()) {
                    s.getMessageQueue().add(packet);
                }
            });

            if (packet instanceof ServerSpawnPositionPacket) {
                ServerSpawnPositionPacket p = (ServerSpawnPositionPacket) packet;
                System.out.println("SPAWN LOCATION RECEIVED");
                worldState.setSpawn(p.getPosition());
            }

            else if (packet instanceof ServerChunkDataPacket) {
                ServerChunkDataPacket p = (ServerChunkDataPacket) packet;
                worldState.getChunkQueue().add(p);
                //System.out.println("RECEIVED A CHUNK PACKET");
            }

            else if(packet instanceof ServerSpawnPlayerPacket) {
                System.out.println("RECEIVED SPAWN PLAYER PACKET");
                ServerSpawnPlayerPacket p = (ServerSpawnPlayerPacket) packet;
                if (connection.getServer().getPlayerPositionManager().findById(p.getEntityId()) == null) {
                    connection.getServer().getPlayerPositionManager().getEntityList().add(new Player(p.getUUID(), p.getEntityId(), new WorldPosition(p.getX(), p.getY(), p.getZ()), p.getMetadata()));
                    System.out.print("PLAYER ENTITY NR: " + p.getUUID() + " GOT ADDED AS A PLAYER ENTITY\n");
                } else {
                    System.out.println("DID NOT ADD PLAYER THAT ALREADY EXISTED");
                }

            }

            else if (packet instanceof ServerPlayerListEntryPacket) {
                System.out.println("RECEIVED PLAYER LIST ENTRY PACKET LALALALALA");
                ServerPlayerListEntryPacket p = (ServerPlayerListEntryPacket) packet;
                switch (p.getAction()) {
                    case REMOVE_PLAYER:
                        System.out.println("size before remove player: " + worldState.getPlayersToJoin().size() + "\n");
                        Queue<Packet> players = worldState.getPlayersToJoin();
                        System.out.println("ID OF PLAYER WE WANT TO REMOVE: " + p.getEntries()[0].getProfile().getId() + "\n");
                        for (Packet player : players) {
                            ServerPlayerListEntryPacket toTest = (ServerPlayerListEntryPacket) player;
                            if (toTest.getEntries()[0].getProfile().getId().equals(p.getEntries()[0].getProfile().getId())) {
                                worldState.getPlayersToJoin().remove(toTest);
                                connection.getServer().getPlayerPositionManager().removeEntity(p.getEntries()[0].getProfile().getId());
                                System.out.println("ID OF PLAYER WE REMOVED: " + toTest.getEntries()[0].getProfile().getId() + "\n");
                                break;
                            }
                        }
                        System.out.println("size after remove player: " + worldState.getPlayersToJoin().size() + "\n");
                    break;
                    case ADD_PLAYER:
                        System.out.println("size before adding player: " + worldState.getPlayersToJoin().size() + "\n");
                        worldState.getPlayersToJoin().add(p);
                        System.out.println("size after adding player: " + worldState.getPlayersToJoin().size() + "\n");

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
                connection.getServer().getPlayerPositionManager().updatEntityPosition(p.getEntityId(), p);
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
                if (connection.getServer().getPlayerPositionManager().findById(p.getEntityIds()[0]) != null) {
                    System.out.println("TRYING TO DESTROY A PLAYER ENTITY");
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
