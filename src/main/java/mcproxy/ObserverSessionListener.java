package mcproxy;

import mcproxy.Spectator.Spectator;
import mcproxy.Spectator.SpectatorSession;
import mcproxy.util.WorldPosition;
import science.atlarge.opencraft.mcprotocollib.data.game.PlayerListEntry;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.client.ClientChatPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.client.ClientSettingsPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.client.player.ClientPlayerActionPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.client.player.ClientPlayerPlaceBlockPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.client.player.ClientPlayerPositionPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.ServerJoinGamePacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.ServerPlayerListEntryPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.ServerEntityTeleportPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerNotifyClientPacket;
import science.atlarge.opencraft.mcprotocollib.packet.login.client.LoginStartPacket;
import science.atlarge.opencraft.packetlib.event.session.PacketReceivedEvent;
import science.atlarge.opencraft.packetlib.event.session.PacketSentEvent;
import science.atlarge.opencraft.packetlib.event.session.SessionAdapter;
import science.atlarge.opencraft.packetlib.packet.Packet;

import java.sql.SQLSyntaxErrorException;
import java.util.Queue;

public class ObserverSessionListener extends SessionAdapter {
    private ObserverServer server;

    ObserverSessionListener(ObserverServer server) {
        this.server = server;
    }

    @Override
    public void packetReceived(PacketReceivedEvent event) {
        Packet packet = event.getPacket();
        //System.out.println(event.getPacket().toString());

        if (packet instanceof LoginStartPacket) {
            LoginStartPacket p = (LoginStartPacket) packet;
            Spectator spectator = server.getSessionRegistry().findBySession(event.getSession()).getSpectator();
            spectator.setName(p.getUsername());
        }


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
            Spectator spectator = server.getSessionRegistry().findBySession(event.getSession()).getSpectator();
            switch (tokenString[0]) {
                case "/unfollow":
                    if (tokenString.length == 1) {
                        ;
                        SpectatorSession session = server.getSessionRegistry().findBySession(event.getSession());
                        session.getPacketForwarder().disableFollowMode();
                    }
                    break;
                case "/teleport":
                case "/follow":
                    if (tokenString.length == 2) {
                        Player target = null;
                        WorldPosition pos = null;
                        Queue<Packet> players = server.getWorldState().getPlayersToJoin();
                        for (Packet player : players) {
                            ServerPlayerListEntryPacket toCheck = (ServerPlayerListEntryPacket) player;
                            for (PlayerListEntry entry : toCheck.getEntries()) {
                                if (entry.getProfile().getName().equals(tokenString[1])) {
                                    target = server.getWorldState().getPlayerPositionManager().findByUUID(entry.getProfile().getId());
                                    pos = target.getPositon();
                                }
                            }
                        }
                        if (target != null) {
                            if (tokenString[0].equals("/teleport")) {
                                event.getSession().send(new ServerEntityTeleportPacket(spectator.getId(), pos.getX(), pos.getY(), pos.getZ(), 0, 0, false));
                            } else if (tokenString[0].equals("/follow")) {
                                SpectatorSession session = server.getSessionRegistry().findBySession(event.getSession());
                                event.getSession().send(new ServerEntityTeleportPacket(spectator.getId(), pos.getX(), pos.getY(), pos.getZ(), 0, 0, false));
                                session.getPacketForwarder().setFollowMode(target.getId());
                            }
                        }
                    }

                    break;
                case "/chat":
                    StringBuilder message = new StringBuilder();
                    message.append(spectator.getname().toUpperCase()).append(": ");
                    for (int i = 1; i < tokenString.length; i++) {
                        System.out.println("appending: " + tokenString[i]);
                        message.append(tokenString[i]).append("  ");
                    }
                    String toSend = message.toString();
                    server.getConnection().chat(toSend);
                    break;
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

