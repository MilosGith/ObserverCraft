package mcproxy.Spectator;

import mcproxy.Player;
import mcproxy.util.WorldPosition;

import java.util.Set;

public class Spectator {
    private WorldPosition pos;
    private Set<Player> playersInRange;

    public Spectator(WorldPosition pos) {
        this.pos = pos;
    }

    public WorldPosition getPosition() {
        return pos;
    }
}
