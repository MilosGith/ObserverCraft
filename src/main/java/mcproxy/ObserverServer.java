package mcproxy;

import com.github.steveice10.mc.auth.data.GameProfile;

import mcproxy.Connection.ServerConnection;
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
import science.atlarge.opencraft.mcprotocollib.packet.ingame.client.ClientChatPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.ServerChatPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.ServerJoinGamePacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerNotifyClientPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerUpdateTimePacket;
import science.atlarge.opencraft.packetlib.Server;
import science.atlarge.opencraft.packetlib.Session;
import science.atlarge.opencraft.packetlib.event.server.ServerAdapter;
import science.atlarge.opencraft.packetlib.event.server.SessionAddedEvent;
import science.atlarge.opencraft.packetlib.event.server.SessionRemovedEvent;
import science.atlarge.opencraft.packetlib.event.session.PacketReceivedEvent;
import science.atlarge.opencraft.packetlib.event.session.PacketSentEvent;
import science.atlarge.opencraft.packetlib.event.session.SessionAdapter;
import science.atlarge.opencraft.packetlib.packet.Packet;
import science.atlarge.opencraft.packetlib.tcp.TcpSessionFactory;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.net.Proxy;

public class ObserverServer {

    Server testServer;

    private Queue<Packet> chunkQueue = new ConcurrentLinkedDeque<>();

    private Queue<Packet> mobQueue = new ConcurrentLinkedDeque<>();

    private HashMap<Packet, Packet> players = new HashMap<>();

    private Queue<Packet> playersToJoin = new ConcurrentLinkedDeque<>();

    private PlayerPositionManager playerManager = new PlayerPositionManager();

    private ServerNotifyClientPacket rain = null;

    private ServerNotifyClientPacket rainStrength = null;

    private ServerUpdateTimePacket serverTime = null;

    private boolean isRaining = false;

    public static final Logger logger = Logger.getLogger("Minecraft");

    private SessionRegistry sessionRegistry = new SessionRegistry();

    private SpawnLocation spawn;

    private int observerCount = 0;

    ServerConnection connection = null;

    private static final boolean VERIFY_USERS = false;
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 25566;
    private static final Proxy PROXY = Proxy.NO_PROXY;
    private static final Proxy AUTH_PROXY = Proxy.NO_PROXY;
    private static final String USERNAME = "Username";
    private static final String PASSWORD = "Password";

    public ObserverServer() {
    }

    public void setupServer() {
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
                session.send(new ServerJoinGamePacket(0, false, GameMode.CREATIVE, 0, Difficulty.PEACEFUL, 200, WorldType.DEFAULT, false));
            }
        });

        server.setGlobalFlag(MinecraftConstants.SERVER_COMPRESSION_THRESHOLD, 100);
        server.addListener(new ServerAdapter() {
            @Override
            public void sessionAdded(SessionAddedEvent event) {
                sessionRegistry.add(new ObserverSession(event.getSession(), ObserverServer.this));
                System.out.println("SESSION HAS BEEN ADDED TO SESSIONREGISTRY");
                event.getSession().addListener(new SessionAdapter() {
                    @Override
                    public void packetReceived(PacketReceivedEvent event) {
                        if (event.getPacket() instanceof ClientChatPacket) {
                            ClientChatPacket packet = event.getPacket();
                            GameProfile profile = event.getSession().getFlag(MinecraftConstants.PROFILE_KEY);
                            System.out.println(profile.getName() + ": " + packet.getMessage());
                            Message msg = new TextMessage("Hello, ").setStyle(new MessageStyle().setColor(ChatColor.GREEN));
                            Message name = new TextMessage(profile.getName()).setStyle(new MessageStyle().setColor(ChatColor.AQUA).addFormat(ChatFormat.UNDERLINED));
                            Message end = new TextMessage("!");
                            msg.addExtra(name);
                            msg.addExtra(end);
                            event.getSession().send(new ServerChatPacket(msg));
                        }
                    }

                    @Override
                    public void packetSent(PacketSentEvent event) {
                        if (event.getPacket() instanceof ServerJoinGamePacket) {
                            ObserverSession s =  sessionRegistry.findBySession(event.getSession());
                            try {
                                s.joinObserver();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }

            @Override
            public void sessionRemoved(SessionRemovedEvent event) {
                System.out.println("REMOVED SESSION");
                MinecraftProtocol protocol = (MinecraftProtocol) event.getSession().getPacketProtocol();
                //sessionRegistry.findBySession(event.getSession()).getTicker().stop();
                sessionRegistry.removeBySession(event.getSession());
            }
        });

        server.bind();
    }

    public Server getTestServer() {
        return testServer;
    }

    public void run() throws InterruptedException {
        setupConnecton();
        setupServer();
    }

    public void setupConnecton() throws InterruptedException {
        this.connection = new ServerConnection(this);
        connection.connect();
        if (connection.getSession().isConnected()) {
            TimeUnit.SECONDS.sleep(1);
            connection.chat("/observe");
        }
    }

    public void setSpawn(Position pos) {
        spawn = new SpawnLocation(pos);
        System.out.println("spawn is set");
    }

    public SpawnLocation getSpawn() {
        return spawn;
    }

    public SessionRegistry getSessionRegistry() {
        return sessionRegistry;
    }

    public Logger getLogger() {
        return logger;
    }

    public Queue<Packet> getChunkQueue() {
        return chunkQueue;
    }

    public Queue<Packet> getMobQueue() {
        return mobQueue;
    }

    public  Queue<Packet> getPlayersToJoin() { return playersToJoin; }

    public HashMap<Packet, Packet> getPlayers() {
        return players;
    }

    public ServerConnection getConnection() {
        return connection;
    }

    public void setRain(ServerNotifyClientPacket p) {
        this.rain = p;
    }

    public void setRainStrength(ServerNotifyClientPacket p) {
        this.rainStrength = p;
    }

    public ServerNotifyClientPacket getRain() {
        return rain;
    }

    public ServerNotifyClientPacket getRainStrength() {
        return rainStrength;
    }

    public PlayerPositionManager getPlayerPositionManager() {
        return playerManager;
    }

    public int getObservercount() {
        return observerCount;
    }

    public void setServerTime(ServerUpdateTimePacket p) {
        serverTime = p;
    }

    public ServerUpdateTimePacket getServerTime() {
        return serverTime;
    }

    public boolean isRaining() {
        return isRaining;
    }

    public void setRaining(boolean bool) {
        isRaining = bool;
    }
    public void incrementObserverCount() {
        observerCount++;
    }
}
