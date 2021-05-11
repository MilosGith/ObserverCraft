package mcproxy.Spectator;

import mcproxy.ObserverServer;
import mcproxy.Player;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.*;
import science.atlarge.opencraft.packetlib.packet.Packet;

public class packetForwarder {
    private boolean followMode = false;
    int followId = -1;
    private SpectatorSession session;

    public packetForwarder(SpectatorSession session) {
        this.session = session;
    }

    public void forwardPacket(Packet packet) {
        if (session.isReady()) {

            if (packet instanceof ServerEntityPositionPacket) {
                ServerEntityPositionPacket p = (ServerEntityPositionPacket) packet;
                Player player = session.getServer().getPlayerPositionManager().findById(p.getEntityId());
                if (player != null && session.isInRange(player)) {
                    if (followMode) {
                        if (p.getEntityId() == followId) {
                            ServerEntityPositionPacket toSend = new ServerEntityPositionPacket(session.getSpectator().getId(), p.getMovementX(), p.getMovementY(), p.getMovementZ(), false);
                            session.getMessageQueue().add(toSend);
                        } else {
                            session.getMessageQueue().add(packet);
                        }
                    } else {
                        session.getMessageQueue().add(packet);
                    }
                }


            }
            else if (packet instanceof ServerEntityRotationPacket) {
                ServerEntityRotationPacket p = (ServerEntityRotationPacket) packet;
                Player player = session.getServer().getPlayerPositionManager().findById(p.getEntityId());
                if (player != null && session.isInRange(player)) {
                    if (followMode) {
                        if (p.getEntityId() == followId) {
                            ServerEntityRotationPacket toSend = new ServerEntityRotationPacket(session.getSpectator().getId(), p.getYaw(), p.getPitch(), p.isOnGround());
                            session.getMessageQueue().add(toSend);
                        } else {
                            session.getMessageQueue().add(packet);
                        }
                    } else {
                        session.getMessageQueue().add(packet);
                    }
                }


            } else if (packet instanceof ServerEntityTeleportPacket){
                ServerEntityTeleportPacket p = (ServerEntityTeleportPacket) packet;
                Player player = session.getServer().getPlayerPositionManager().findById(p.getEntityId());
                if (player != null && session.isInRange(player)) {
                    if (followMode) {
                        if (p.getEntityId() == followId) {
                            ServerEntityTeleportPacket toSend = new ServerEntityTeleportPacket(session.getSpectator().getId(), p.getX(), p.getY(), p.getZ(), p.getYaw(), p.getPitch(), p.isOnGround());
                            session.getMessageQueue().add(toSend);
                        } else {
                            session.getMessageQueue().add(packet);
                        }
                    } else {
                        session.getMessageQueue().add(packet);
                    }
                }

            } else if (packet instanceof  ServerEntityHeadLookPacket) {
                ServerEntityHeadLookPacket p = (ServerEntityHeadLookPacket) packet;
                Player player = session.getServer().getPlayerPositionManager().findById(p.getEntityId());
                if (player != null && session.isInRange(player)) {
                    if (followMode) {
                        if (p.getEntityId() == followId) {
                            ServerEntityHeadLookPacket toSend = new ServerEntityHeadLookPacket(session.getSpectator().getId(), p.getHeadYaw());
                            session.getMessageQueue().add(toSend);
                        } else {
                            session.getMessageQueue().add(packet);
                        }
                    } else {
                        session.getMessageQueue().add(packet);
                    }
                }

            } else if (packet instanceof  ServerEntityAnimationPacket) {
                ServerEntityAnimationPacket p = (ServerEntityAnimationPacket) packet;
                Player player = session.getServer().getPlayerPositionManager().findById(p.getEntityId());
                if (player != null && session.isInRange(player)) {
                    if (followMode) {
                        if (p.getEntityId() == followId) {
                            ServerEntityAnimationPacket toSend = new ServerEntityAnimationPacket(session.getSpectator().getId(), p.getAnimation());
                            session.getMessageQueue().add(toSend);
                        } else {
                            session.getMessageQueue().add(packet);
                        }
                    } else {
                        session.getMessageQueue().add(packet);
                    }
                }
            } else if (packet instanceof ServerEntityPositionRotationPacket) {
                //System.out.println("GOT ENTITY POSTION ROTATION PACKET");
                ServerEntityPositionRotationPacket p = (ServerEntityPositionRotationPacket) packet;
                Player player = session.getServer().getPlayerPositionManager().findById(p.getEntityId());
                if (player != null && session.isInRange(player)) {
                    if (followMode) {
                        if (p.getEntityId() == followId) {
                            ServerEntityPositionRotationPacket toSend = new ServerEntityPositionRotationPacket(session.getSpectator().getId(), p.getMovementX(), p.getMovementY(), p.getMovementZ(), p.getYaw(), p.getPitch(), p.isOnGround());
                            session.getMessageQueue().add(toSend);
                        } else {
                            session.getMessageQueue().add(packet);
                        }
                    } else {
                        session.getMessageQueue().add(packet);
                    }
                }
            }
             else {
                session.getMessageQueue().add(packet);
            }
        }
    }

    public void setFollowMode(int id) {
        followMode = true;
        followId = id;
    }

    public void disableFollowMode() {
        followMode = false;
        followId = -1;
    }
}
