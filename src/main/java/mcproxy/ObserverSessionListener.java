package mcproxy;

import mcproxy.Spectator.Spectator;
import mcproxy.Spectator.SpectatorSession;
import mcproxy.util.WorldPosition;
import science.atlarge.opencraft.mcprotocollib.data.game.PlayerListEntry;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.client.ClientChatPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.client.player.ClientPlayerPositionPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.ServerJoinGamePacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.ServerPlayerListEntryPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.ServerEntityTeleportPacket;
import science.atlarge.opencraft.packetlib.event.session.PacketReceivedEvent;
import science.atlarge.opencraft.packetlib.event.session.PacketSentEvent;
import science.atlarge.opencraft.packetlib.event.session.SessionAdapter;
import science.atlarge.opencraft.packetlib.packet.Packet;

import java.util.Queue;

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

        if (packet instanceof ClientChatPacket) {
            ClientChatPacket p = (ClientChatPacket) packet;
            System.out.println(p.getMessage());
            String chatMsg = p.getMessage();
            String delims = " ";
            String[] tokenString = chatMsg.split(delims);
            if (tokenString.length == 2) {
                System.out.println("TOKEN 1: " + tokenString[0]);
                System.out.println("TOKEN 2: " + tokenString[1]);
                if (tokenString[0].equals("/teleport")) {
                    Queue<Packet> players =  server.getWorldState().getPlayersToJoin();
                    for (Packet player : players) {
                        ServerPlayerListEntryPacket toCheck = (ServerPlayerListEntryPacket) player;
                        for (PlayerListEntry entry : toCheck.getEntries()) {
                            if (entry.getProfile().getName().equals(tokenString[1])) {
                                Spectator spectator = server.getSessionRegistry().findBySession(event.getSession()).getSpectator();
                                Player toTeleport = server.getPlayerPositionManager().findByUUID(entry.getProfile().getId());
                                WorldPosition pos = toTeleport.getPositon();
                                event.getSession().send(new ServerEntityTeleportPacket(spectator.getId(), pos.getX(), pos.getY(), pos.getZ(), 0, 0, false));
                            }
                        }
                    }
                }
            }


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

