package mcproxy.Connection;

import mcproxy.ObserverServer;
import mcproxy.Player;
import mcproxy.WorldState;
import mcproxy.util.WorldPosition;
import org.bukkit.World;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.ServerPlayerListEntryPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.ServerEntityDestroyPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.ServerEntityPositionPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.ServerEntityPositionRotationPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.ServerEntityTeleportPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.spawn.ServerSpawnMobPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerNotifyClientPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerSpawnPositionPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerUpdateTimePacket;
import science.atlarge.opencraft.packetlib.packet.Packet;

import java.util.Queue;

public class ServerMessageHandler {
    ObserverServer server;

    public ServerMessageHandler(ObserverServer serv) {
        this.server = serv;
    }

    public void handlePacket(Packet packet) {
        WorldState worldState = server.getWorldState();

        server.getSessionRegistry().getSessions().keySet().forEach(s -> {
            s.getPacketForwarder().forwardPacket(packet);
        });

        if (packet instanceof ServerSpawnPositionPacket) {
            ServerSpawnPositionPacket p = (ServerSpawnPositionPacket) packet;
            worldState.setSpawn(p.getPosition());
        }

        else if(packet instanceof ServerSpawnPlayerPacket) {
            ServerSpawnPlayerPacket p = (ServerSpawnPlayerPacket) packet;
            if (worldState.getPlayerPositionManager().findById(p.getEntityId()) == null) {
                worldState.getPlayerPositionManager().getEntityList().add(new Player(p.getUUID(), p.getEntityId(), new WorldPosition(p.getX(), p.getY(), p.getZ()), p.getMetadata()));
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
                            worldState.getPlayerPositionManager().removeEntity(p.getEntries()[0].getProfile().getId());
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
            worldState.getPlayerPositionManager().updatEntityPosition(p.getEntityId(), p.getMovementX(), p.getMovementY(), p.getMovementZ());
        }

        else if (packet instanceof ServerEntityPositionRotationPacket) {
            ServerEntityPositionRotationPacket p = (ServerEntityPositionRotationPacket) packet;
            worldState.getPlayerPositionManager().updatEntityPosition(p.getEntityId(), p.getMovementX(), p.getMovementY(), p.getMovementZ());
        }

        else if(packet instanceof ServerSpawnMobPacket) {
            //System.out.println("GOT MOB PACKET");
            ServerSpawnMobPacket p = (ServerSpawnMobPacket) packet;
            worldState.getMobQueue().add(p);
        }

        else if (packet instanceof ServerUpdateTimePacket) {
            ServerUpdateTimePacket p = (ServerUpdateTimePacket) packet;
            worldState.setServerTime(p);
        }

        else if (packet instanceof ServerEntityDestroyPacket) {
            //System.out.println("GOT DESTROY PACKET");
            ServerEntityDestroyPacket p = (ServerEntityDestroyPacket) packet;
            if (worldState.getPlayerPositionManager().findById(p.getEntityIds()[0]) != null) {
            }
        }

        else if (packet instanceof ServerEntityTeleportPacket) {
            ServerEntityTeleportPacket p = (ServerEntityTeleportPacket) packet;
            Player player = worldState.getPlayerPositionManager().findById(p.getEntityId());
            if (player != null) {
                player.getPositon().updatePosition(p.getX(), p.getY(), p.getZ());
            }
        }
    }
}
