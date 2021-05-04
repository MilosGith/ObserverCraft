package mcproxy.Connection;

import com.ning.http.client.ProxyServer;
import mcproxy.ObserverServer;
import science.atlarge.opencraft.mcprotocollib.MinecraftProtocol;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.client.ClientChatPacket;
import science.atlarge.opencraft.packetlib.Client;
import science.atlarge.opencraft.packetlib.Session;
import science.atlarge.opencraft.packetlib.tcp.TcpSessionFactory;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerConnection {
    private ObserverServer server;
    private MinecraftProtocol protocol = new MinecraftProtocol(UUID.randomUUID().toString().substring(0, 6));
    private String host = "127.0.0.1";
    private int port = 25565;
    private Client client;
    public static final Logger logger = Logger.getLogger("Proxy");

    public ServerConnection(ObserverServer server) {
        this.server = server;
        this.client = new Client(host, port, protocol, new TcpSessionFactory(true));
        this.client.getSession().addListener(new ConListener(this));
    }


    public Session getSession() {
        return client.getSession();
    }

    public Logger getLogger() {
        return logger;
    }

    public void connect() {
        client.getSession().connect();
        if (client.getSession().isConnected()) {
            logger.log(Level.INFO,"established connection to MC server");
        } else {
            logger.log(Level.WARNING,"mc server not online, cant connect");
        }
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
