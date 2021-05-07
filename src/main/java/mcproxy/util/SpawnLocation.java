package mcproxy.util;

import science.atlarge.opencraft.mcprotocollib.data.game.entity.metadata.Position;

public class SpawnLocation {
    private final Position position;

    public SpawnLocation(Position pos) {
        this.position = pos;
    }

    public Position getPosition() {
        return position;
    }

}
