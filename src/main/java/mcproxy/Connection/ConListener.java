package mcproxy.Connection;


import mcproxy.ObserverServer;
import science.atlarge.opencraft.mcprotocollib.MinecraftProtocol;
import science.atlarge.opencraft.mcprotocollib.data.SubProtocol;
import science.atlarge.opencraft.mcprotocollib.data.game.PlayerListEntry;
import science.atlarge.opencraft.mcprotocollib.data.game.PlayerListEntryAction;
import science.atlarge.opencraft.mcprotocollib.data.message.Message;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.ServerJoinGamePacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.ServerPlayerListEntryPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerChunkDataPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerSpawnPositionPacket;
import science.atlarge.opencraft.packetlib.event.session.*;
import science.atlarge.opencraft.packetlib.packet.Packet;

import java.util.HashMap;
import java.util.UUID;


public class ConListener implements SessionListener {
    private ServerConnection connection;
    private int count;

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
                connection.getServer().getSessionRegistry().getSessions().forEach(s -> {
                    if (s.isReady()) {
                        s.getSession().send(pre.getPacket());
                    }
                });
                if (packet instanceof ServerSpawnPositionPacket) {
                    ServerSpawnPositionPacket p = (ServerSpawnPositionPacket) packet;
                    System.out.println("SPAWN LOCATION RECEIVED");
                    ObserverServer server = connection.getServer();
                    server.setSpawn(p.getPosition());

                }
                if (packet instanceof ServerChunkDataPacket) {
                    ServerChunkDataPacket p = (ServerChunkDataPacket) packet;
                    System.out.println("added CHUNK packet to queue");
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
                            connection.getServer().getPlayers().remove(p);
                            System.out.println("size after remove player: " + connection.getServer().getPlayers().size() + "\n");
                        System.out.println(p.getAction().toString().toLowerCase());
                        break;
                        case ADD_PLAYER:
                            System.out.println("size before adding player: " + connection.getServer().getPlayers().size() + "\n");
                            connection.getServer().getPlayers().put(p, null);
                            System.out.println("size after adding player: " + connection.getServer().getPlayers().size() + "\n");
                    }
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

    }

    @Override
    public void disconnecting(DisconnectingEvent disconnectingEvent) {

    }

    @Override
    public void disconnected(DisconnectedEvent disconnectedEvent) {

    }
}
