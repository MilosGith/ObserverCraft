package mcproxy;

import mcproxy.Spectator.SpectatorSession;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.client.player.ClientPlayerPositionPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.ServerJoinGamePacket;
import science.atlarge.opencraft.packetlib.event.session.PacketReceivedEvent;
import science.atlarge.opencraft.packetlib.event.session.PacketSentEvent;
import science.atlarge.opencraft.packetlib.event.session.SessionAdapter;
import science.atlarge.opencraft.packetlib.packet.Packet;

public class ObserverSessionListener extends SessionAdapter {
    private ObserverServer server;

    ObserverSessionListener(ObserverServer server) {
        this.server = server;
    }

    @Override
    public void packetReceived(PacketReceivedEvent event) {
        Packet packet = event.getPacket();
        if (packet instanceof ClientPlayerPositionPacket) {
            ClientPlayerPositionPacket p = (ClientPlayerPositionPacket) packet;
            SpectatorSession session = server.getSessionRegistry().findBySession(event.getSession());
            session.getSpectator().getPosition().updatePosition(p.getX(), p.getY(), p.getZ());
        }
    }

    @Override
    public void packetSent(PacketSentEvent event) {
        if (event.getPacket() instanceof ServerJoinGamePacket) {
            SpectatorSession s =  server.getSessionRegistry().findBySession(event.getSession());
            try {
                s.joinSpectator();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

