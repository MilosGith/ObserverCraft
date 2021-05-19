package mcproxy;

import com.github.steveice10.mc.auth.data.GameProfile;

import mcproxy.Connection.ServerConnection;
import mcproxy.Spectator.Spectator;
import mcproxy.Spectator.SpectatorSession;
import mcproxy.util.SpawnLocation;

import mcproxy.util.WorldPosition;
import org.bukkit.material.Observer;
import science.atlarge.opencraft.mcprotocollib.MinecraftConstants;
import science.atlarge.opencraft.mcprotocollib.MinecraftProtocol;
import science.atlarge.opencraft.mcprotocollib.ServerLoginHandler;
import science.atlarge.opencraft.mcprotocollib.data.game.entity.metadata.Position;
import science.atlarge.opencraft.mcprotocollib.data.game.entity.player.GameMode;
import science.atlarge.opencraft.mcprotocollib.data.game.setting.Difficulty;
import science.atlarge.opencraft.mcprotocollib.data.game.world.WorldType;
import science.atlarge.opencraft.mcprotocollib.data.message.*;
import science.atlarge.opencraft.mcprotocollib.data.status.PlayerInfo;
import science.atlarge.opencraft.mcprotocollib.data.status.ServerStatusInfo;
import science.atlarge.opencraft.mcprotocollib.data.status.VersionInfo;
import science.atlarge.opencraft.mcprotocollib.data.status.handler.ServerInfoBuilder;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.ServerJoinGamePacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerNotifyClientPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerUpdateTimePacket;
import science.atlarge.opencraft.packetlib.Server;
import science.atlarge.opencraft.packetlib.Session;
import science.atlarge.opencraft.packetlib.event.server.ServerAdapter;
import science.atlarge.opencraft.packetlib.event.server.SessionAddedEvent;
import science.atlarge.opencraft.packetlib.event.server.SessionRemovedEvent;
import science.atlarge.opencraft.packetlib.packet.Packet;
import science.atlarge.opencraft.packetlib.tcp.TcpSessionFactory;


import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.net.Proxy;

public class ObserverServer {

    WorldState worldState = new WorldState();

    private SessionRegistry sessionRegistry = new SessionRegistry();

    private int observerCount = 0;

    private ServerConnection connection = null;

    private ServerTicker ticker = new ServerTicker(this);

    public static final Logger logger = Logger.getLogger("Minecraft");

    private static final boolean VERIFY_USERS = false;
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 25566;
    private static final Proxy PROXY = Proxy.NO_PROXY;
    private static final Proxy AUTH_PROXY = Proxy.NO_PROXY;

    public ObserverServer() {
    }

    private void setupServer() {
        Server server = new Server(HOST, PORT, MinecraftProtocol.class, new TcpSessionFactory(PROXY));
        server.setGlobalFlag(MinecraftConstants.AUTH_PROXY_KEY, AUTH_PROXY);
        server.setGlobalFlag(MinecraftConstants.VERIFY_USERS_KEY, VERIFY_USERS);
        server.setGlobalFlag(MinecraftConstants.SERVER_INFO_BUILDER_KEY, new ServerInfoBuilder() {
            @Override
            public ServerStatusInfo buildInfo(Session session) {
                return new ServerStatusInfo(new VersionInfo(MinecraftConstants.GAME_VERSION, MinecraftConstants.PROTOCOL_VERSION), new PlayerInfo(100, 0, new GameProfile[0]), new TextMessage("Hello world!"), null);
            }
        });

        server.setGlobalFlag(MinecraftConstants.SERVER_LOGIN_HANDLER_KEY, new ServerLoginHandler() {
            @Override
            public void loggedIn(Session session) {
                System.out.println("WE LOGGED IN");
                session.send(new ServerJoinGamePacket(observerCount, false, GameMode.CREATIVE, 0, Difficulty.PEACEFUL, 999, WorldType.DEFAULT, false));
            }
        });

        server.setGlobalFlag(MinecraftConstants.SERVER_COMPRESSION_THRESHOLD, 100);
        server.addListener(new ServerAdapter() {
            @Override
            public void sessionAdded(SessionAddedEvent event) {
                SpawnLocation spawn = worldState.getSpawn();
                Spectator newSpectator = new Spectator(new WorldPosition(spawn.getPosition().getX(), spawn.getPosition().getY(), spawn.getPosition().getZ()), observerCount);
                sessionRegistry.add(new SpectatorSession(event.getSession(), ObserverServer.this, newSpectator));

                System.out.println("SESSION HAS BEEN ADDED TO SESSIONREGISTRY");
                event.getSession().addListener(new ObserverSessionListener(ObserverServer.this));
            }

            @Override
            public void sessionRemoved(SessionRemovedEvent event) {
                System.out.println("REMOVED SESSION");
                MinecraftProtocol protocol = (MinecraftProtocol) event.getSession().getPacketProtocol();
                sessionRegistry.removeBySession(event.getSession());
            }
        });

        server.bind();
    }

    public void run() throws InterruptedException {
        setupConnecton();
        setupServer();
        ticker.start();
    }

    private void setupConnecton() throws InterruptedException {
        this.connection = new ServerConnection(this);
        connection.connect();
        if (connection.getSession().isConnected()) {
            TimeUnit.SECONDS.sleep(1);
            connection.chat("/observe");
        }
    }

    public WorldState getWorldState() { return worldState; }

    public ServerConnection getConnection() { return connection; }

    public SessionRegistry getSessionRegistry() {
        return sessionRegistry;
    }

    public int getObservercount() {
        return observerCount;
    }

    public void incrementObserverCount() {
        observerCount++;
    }


}
