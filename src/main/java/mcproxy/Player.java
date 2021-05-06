package mcproxy;

import science.atlarge.opencraft.mcprotocollib.data.game.entity.metadata.EntityMetadata;

import java.util.UUID;

public class Player {
    private UUID PID;
    private int entityId;
    private double X;
    private double Y;
    private double Z;
    private EntityMetadata[] metadata;

    public Player(UUID pid, int entityId, double moveX, double moveY, double moveZ, EntityMetadata[] metadata) {
        this.PID = pid;
        this.entityId = entityId;
        this.X = moveX;
        this.Y = moveY;
        this.Z = moveZ;
        this.metadata = metadata;
    }

    public double getX() {
        return X;
    }

    public double getY() {
        return Y;
    }

    public double getZ() {
        return Z;
    }

    public void updateX(double x) {
        this.X += x;
    }

    public void updateY(double y) {
        this.Y += y;
    }

    public void updateZ(double z) {
        this.Z += z;
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
