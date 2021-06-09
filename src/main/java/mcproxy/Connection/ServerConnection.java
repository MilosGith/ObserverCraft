package mcproxy.Connection;

import com.ning.http.client.ProxyServer;
import mcproxy.ObserverServer;
import science.atlarge.opencraft.mcprotocollib.MinecraftProtocol;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.client.ClientChatPacket;
import science.atlarge.opencraft.packetlib.Client;
import science.atlarge.opencraft.packetlib.Session;
import science.atlarge.opencraft.packetlib.packet.Packet;
import science.atlarge.opencraft.packetlib.tcp.TcpSessionFactory;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerConnection {
    private ObserverServer server;
    private MinecraftProtocol protocol = new MinecraftProtocol("OBSERVER-" + UUID.randomUUID().toString().substring(0, 6));
    private String host = null;
    private int port = 25565;
    private Client client;
    private ConcurrentLinkedQueue<Packet> toHandle = new ConcurrentLinkedQueue<>();

    public ServerConnection(ObserverServer server, String serverIP) {
        this.server = server;
        this.host = serverIP;

        this.client = new Client(host, port, protocol, new TcpSessionFactory(true));
        this.client.getSession().addListener(new ConListener(this));
    }


    public Session getSession() {
        return client.getSession();
    }

    public void connect() {
        client.getSession().connect();
        if (client.getSession().isConnected()) {
            ObserverServer.logger.log(Level.INFO,"established connection to MC server");
        } else {
            ObserverServer.logger.log(Level.WARNING,"mc server not online, cant connect");
        }
    }

    public ConcurrentLinkedQueue<Packet> getToHandle() {
        return toHandle;
    }

    public void chat (String msg) {
        client.getSession().send(new ClientChatPacket(msg));
    }

    public ObserverServer getServer() {
        return server;
    }

    public void disconnect() {
        client.getSession().disconnect("disconnected");
    }
}
