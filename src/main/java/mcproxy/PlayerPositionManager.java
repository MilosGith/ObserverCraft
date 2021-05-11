package mcproxy;

import science.atlarge.opencraft.mcprotocollib.packet.ingame.server.entity.ServerEntityPositionPacket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
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
                //System.out.println("FOUND PLAYER BY ID");
                break;
            }
        }
        return found;
    }

    public void removeEntity(UUID id) {
        Player toRemove = findByUUID(id);
        entityList.remove(toRemove);
    }

    public void updatEntityPosition(int ID, double x, double y, double z) {
        Player toUpdate = findById(ID);
        if (toUpdate != null) {
            toUpdate.updatePosition(x,y,z);
        }
    }

    public double getDistance(double x1, double z1, double x2, double z2) {
        double ac = z2 - z1;
        double bc = x2 - x1;
        double distance = Math.sqrt(ac * ac + bc * bc);
        //System.out.println("DISTANCE: " + distance);
        return distance;
    }

    public void printPlayerPositions() {
        entityList.forEach(p -> {
            System.out.println("player position request: " +  p.getPositon().toString() +  "\n");
        });
    }
}
