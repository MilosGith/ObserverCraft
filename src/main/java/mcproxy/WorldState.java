package mcproxy;

import mcproxy.Connection.ServerConnection;
import mcproxy.util.SpawnLocation;
import org.bukkit.event.player.PlayerJoinEvent;
import science.atlarge.opencraft.mcprotocollib.data.game.entity.metadata.Position;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.ServerJoinGamePacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerNotifyClientPacket;
import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.world.ServerUpdateTimePacket;
import science.atlarge.opencraft.packetlib.packet.Packet;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class WorldState {

    public WorldState() {
    }

    public ReentrantLock chunkLock = new ReentrantLock();

    private PlayerPositionManager playerManager = new PlayerPositionManager();

    private Queue<Packet> chunkQueue = new ConcurrentLinkedDeque<>();

    private Queue<Packet> mobQueue = new ConcurrentLinkedDeque<>();

    private Queue<Packet> playersToJoin = new ConcurrentLinkedDeque<>();

    private SpawnLocation spawn;

    private ServerNotifyClientPacket rain = null;

    private ServerNotifyClientPacket rainStrength = null;

    private ServerUpdateTimePacket serverTime = null;

    private boolean isRaining = false;

    public SpawnLocation getSpawn() {
        return spawn;
    }

    public void setSpawn(Position pos) {
        spawn = new SpawnLocation(pos);
        //System.out.println("spawn is set");
    }

    public Queue<Packet> getChunkQueue() {
        return chunkQueue;
    }

    public ArrayList<Packet> getChunkCopy() {
        return new ArrayList<>(chunkQueue);
    }


    public Queue<Packet> getMobQueue() {
        return mobQueue;
    }

    public  Queue<Packet> getPlayersToJoin() { return playersToJoin; }


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

    public PlayerPositionManager getPlayerPositionManager() {
        return playerManager;
    }

    public ReentrantLock getChunkLock() {
        return chunkLock;
    }
}
