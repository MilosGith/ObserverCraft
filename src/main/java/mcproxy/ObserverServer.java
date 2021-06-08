package mcproxy;

import com.github.steveice10.mc.auth.data.GameProfile;

import mcproxy.Connection.ServerConnection;
import mcproxy.Spectator.Spectator;
import mcproxy.Spectator.SpectatorSession;
import mcproxy.measurements.EventFileLogger;
import mcproxy.measurements.EventLogger;
import mcproxy.util.SpawnLocation;

import mcproxy.util.WorldPosition;
import science.atlarge.opencraft.mcprotocollib.MinecraftConstants;
import science.atlarge.opencraft.mcprotocollib.MinecraftProtocol;
import science.atlarge.opencraft.mcprotocollib.ServerLoginHandler;
import science.atlarge.opencraft.mcprotocollib.data.game.entity.player.GameMode;
import science.atlarge.opencraft.mcprotocollib.data.game.setting.Difficulty;
import science.atlarge.opencraft.mcprotocollib.data.game.world.WorldType;
import science.atlarge.opencraft.mcprotocollib.data.message.*;
import science.atlarge.opencraft.mcprotocollib.data.status.PlayerInfo;
import science.atlarge.opencraft.mcprotocollib.data.status.ServerStatusInfo;
import science.atlarge.opencraft.mcprotocollib.data.status.VersionInfo;
import science.atlarge.opencraft.mcprotocollib.data.status.handler.ServerInfoBuilder;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.ServerJoinGamePacket;
import science.atlarge.opencraft.packetlib.Server;
import science.atlarge.opencraft.packetlib.Session;
import science.atlarge.opencraft.packetlib.event.server.ServerAdapter;
import science.atlarge.opencraft.packetlib.event.server.SessionAddedEvent;
import science.atlarge.opencraft.packetlib.event.server.SessionRemovedEvent;
import science.atlarge.opencraft.packetlib.tcp.TcpSessionFactory;


import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.Proxy;
import java.util.logging.SimpleFormatter;

public class ObserverServer {

    public static final Logger logger = Logger.getLogger("MCspectating");

    private WorldState worldState = new WorldState();

    private SessionRegistry sessionRegistry = new SessionRegistry();

    private static final AtomicInteger numSpectators = new AtomicInteger(0);

    private ServerConnection connection = null;

    private ServerTicker ticker = new ServerTicker(this);

    private String logFile = null;

    private EventLogger eventLogger;

    private static final boolean VERIFY_USERS = false;
    private String HOST = null;
    private String serverIP = null;
    private static final int PORT = 25566;
    private static final Proxy PROXY = Proxy.NO_PROXY;
    private static final Proxy AUTH_PROXY = Proxy.NO_PROXY;

    public ObserverServer(String ip, String log, String host) throws IOException {
        serverIP = ip;
        logFile = log;
        HOST = host;
        if (logFile != null) {
            File test = new File(logFile);
            if (test.exists()) {
                FileHandler fh = new FileHandler(logFile, true);   // true forces append mode
                SimpleFormatter sf = new SimpleFormatter();
                fh.setFormatter(sf);
                logger.addHandler(fh);
            }
        }
        initEventLogging();
    }

    private void setupServer() {
        logger.info("Setting up server. IP: " + HOST + " Port: " + PORT);
        Server server = new Server("0.0.0.0", PORT, MinecraftProtocol.class, new TcpSessionFactory(PROXY));
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
               // System.out.println("GOT A LOGGED IN");
                eventLogger.log("numspectators", numSpectators.getAndIncrement());
                session.send(new ServerJoinGamePacket(0, false, GameMode.CREATIVE, 0, Difficulty.PEACEFUL, 999, WorldType.DEFAULT, false));
            }
        });

        server.setGlobalFlag(MinecraftConstants.SERVER_COMPRESSION_THRESHOLD, 100);
        server.addListener(new ServerAdapter() {
            @Override
            public void sessionAdded(SessionAddedEvent event) {
               // System.out.println("GOT A SESSION ADDED");
                SpawnLocation spawn = worldState.getSpawn();
                Spectator newSpectator = new Spectator(new WorldPosition(spawn.getPosition().getX(), spawn.getPosition().getY(), spawn.getPosition().getZ()), 0);
                sessionRegistry.add(new SpectatorSession(event.getSession(), ObserverServer.this, newSpectator));
                event.getSession().addListener(new ObserverSessionListener(ObserverServer.this));
            }

            @Override
            public void sessionRemoved(SessionRemovedEvent event) {
                sessionRegistry.removeBySession(event.getSession());
                eventLogger.log("numspectators", numSpectators.getAndDecrement());
                logger.log(Level.INFO, "Removed session");
            }
        });

        server.bind();
    }

    public void run() throws InterruptedException {
        setupConnecton();
        setupServer();
        ticker.start();
    }

    public void shutDown() {
        ticker.stop();
        connection.disconnect();
    }

    private void setupConnecton() throws InterruptedException {
        this.connection = new ServerConnection(this, serverIP);
        connection.connect();
        if (connection.getSession().isConnected()) {
            TimeUnit.SECONDS.sleep(1);
            connection.chat("/observe");
        }
    }

    public int getSpectatorCount() {
        return numSpectators.get();
    }

    public WorldState getWorldState() { return worldState; }

    public ServerConnection getConnection() { return connection; }

    public SessionRegistry getSessionRegistry() {
        return sessionRegistry;
    }

    public EventLogger getEventLogger() {
        return eventLogger;
    }

    private void initEventLogging() {
        eventLogger = new EventFileLogger(new File("mcspectating-events.log"), this);
        try {
            eventLogger.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
