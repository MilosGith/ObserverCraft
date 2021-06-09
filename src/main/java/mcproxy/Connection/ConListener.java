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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;


public class ConListener implements SessionListener {
    private ServerConnection connection;
    private long count = 0;

    public ConListener(ServerConnection connection) {
        this.connection = connection;
    }
    @Override
    public void packetReceived(PacketReceivedEvent pre) {
        Packet packet = pre.getPacket();
        //count++;
        //System.out.println("received package: ," + pre.getPacket().getClass().getName().toString() +  "   | Number packages:" + count);

        MinecraftProtocol pro = (MinecraftProtocol) pre.getSession().getPacketProtocol();

        if (pro.getSubProtocol() == SubProtocol.GAME) {
            connection.getToHandle().add(packet);
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
        connection.getServer().shutDown();
        ObserverServer.logger.info("total packets received: " + count);
        ObserverServer.logger.info("Disconnected from server\n");
    }
}
