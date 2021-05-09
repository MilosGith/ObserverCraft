package mcproxy;

import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.ServerEntityPositionPacket;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerPositionManager {
    private ArrayList<Player> entityList = new ArrayList<>();

    public ArrayList<Player> getEntityList() {
        return entityList;
    }

    public Player findByUUID(UUID id) {
        System.out.println("TRYING TO FIND PLAYER BY UUID, SIZE OF LIST: " + entityList.size() + "\n");
        Player found = null;
        for (Player entity : entityList) {
            System.out.println("ID TO FIND: " + id + "\n");
            System.out.println("COMPARING ID TO: " + entity.getUUID() + "\n");
            if (entity.getUUID().equals(id)) {
                //System.out.println("COMPARING ID TO: " + entity.getUUID() + "\n");
                System.out.println("FOUND THE PLAYER BY UUID");
                found = entity;
                break;
            }
        }
        return found;
    }

    public Player findById(int id) {
        Player found = null;
        for (Player entity : entityList) {
            if (entity.getId() == id) {
                found=  entity;
                break;
            }
        }
        return found;
    }

    public void removeEntity(UUID id) {
        Player toRemove = findByUUID(id);
        entityList.remove(toRemove);
    }

    public void updatEntityPosition(int ID, ServerEntityPositionPacket p) {
        Player toUpdate = findById(ID);
        if (toUpdate != null) {
            toUpdate.updatePosition(p.getMovementX(), p.getMovementY(), p.getMovementZ());
           // System.out.println("Updated player entity position, new coords are  x: " + toUpdate.getX() + " y: " +  toUpdate.getY() + " z: " + toUpdate.getZ());
        }
    }

    public double getDistance(double x1, double z1, double x2, double z2) {
        double ac = z2 - z1;
        double bc = x2 - x1;
        return Math.sqrt(Math.pow(ac, 2) + Math.pow(bc, 2));
    }

    public void printPlayerPositions() {
        entityList.forEach(p -> {
            System.out.println("player position request: " +  p.getPositon().toString() +  "\n");
        });
    }
}
