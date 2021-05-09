package mcproxy.util;

import science.atlarge.opencraft.mcprotocollib.util.ObjectUtil;

public class WorldPosition {
    private double x;
    private double y;
    private double z;

    public WorldPosition(double xPos, double yPos, double zPos) {
        this.x = xPos;
        this.y = yPos;
        this.z = zPos;
    }

    public void updatePosition(double xPos, double yPos, double zPos) {
        this.x = xPos;
        this.y = yPos;
        this.z = zPos;
    }

    public double getX() { return x;}
    public double getY() { return y;}
    public double getZ() { return z;}

    public String toString() {
        return ObjectUtil.toString(this);
    }
}
