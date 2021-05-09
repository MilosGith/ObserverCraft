package mcproxy;

import mcproxy.util.WorldPosition;
import org.bukkit.World;
import science.atlarge.opencraft.mcprotocollib.data.game.entity.metadata.EntityMetadata;

import java.util.UUID;

public class Player {
    private UUID PID;
    private int entityId;
    private WorldPosition positon;
    private EntityMetadata[] metadata;

    public Player(UUID pid, int entityId, WorldPosition pos, EntityMetadata[] metadata) {
        this.PID = pid;
        this.entityId = entityId;
        this.positon = pos;
        this.metadata = metadata;
    }

    public void updatePosition(double x, double y, double z) {
        double newX = positon.getX() + x;
        double newY = positon.getY() + y;
        double newZ = positon.getZ() + z;
        positon.updatePosition(newX, newY, newZ);
        //System.out.println("WorldPos after update: " + positon.toString());
    }

    public WorldPosition getPositon() {
        return positon;
    }

    public int getId() {
        return entityId;
    }

    public UUID getUUID() {
        return PID;
    }

    public EntityMetadata[] getMetadata() {
        return metadata;
    }

}
