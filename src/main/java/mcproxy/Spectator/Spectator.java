package mcproxy.Spectator;

import mcproxy.Player;
import mcproxy.util.WorldPosition;

import java.util.ArrayList;
import java.util.Set;

public class Spectator {
    private int Id;
    private WorldPosition pos;
    private ArrayList<Player> playersInRange;
    private String userName;

    public Spectator(WorldPosition pos, int id) {
        this.pos = pos;
        this.Id = id;
        playersInRange = new ArrayList<>();
    }

    public WorldPosition getPosition() {
        return pos;
    }

    public ArrayList<Player> getPlayersInRange() {
        return playersInRange;
    }

    public void updatePlayersInRange(ArrayList<Player> list) {
        playersInRange = list;
    }

    public int getId() {
        return Id;
    }

    public void setName(String name) {
        userName = name;
    }

    public String getname() {
        return userName;
    }
}
