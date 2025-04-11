package net.justonedev.candycane.lobbysession.world.element;

import lombok.Getter;
import lombok.Setter;
import net.justonedev.candycane.lobbysession.world.Position;
import net.justonedev.candycane.lobbysession.world.state.Powerstate;
import net.justonedev.candycane.lobbysession.world.state.PowerstateType;

import java.util.ArrayList;
import java.util.List;

public class Wire<T> implements WorldObject {
    private final String uuid;
    private final String material;
    @Getter
    private final Position origin;
    @Getter
    @Setter
    private Position target;
    private final List<Position> positions;

    @Getter
    @Setter
    private Powerstate<T> power;
    @Getter
    private boolean isTransmitting;
    @Getter
    @Setter
    private boolean isBroken;

    public Wire(String uuid, String material, Position origin, Position target) {
        this.uuid = uuid;
        this.material = material;
        this.origin = origin;
        this.target = target;

        int diffX = Math.abs(origin.x() - target.x());
        int diffY = Math.abs(origin.x() - target.y());
        int diagonalLength = Math.min(diffX, diffY);
        int straightLength = Math.max(diffX, diffY) - diagonalLength;
        boolean verticalStraight = diffX < diffY;
        positions = new ArrayList<>();
        int dx = target.x() - origin.x() < 0 ? -1 : 1;
        int dy = target.y() - origin.y() < 0 ? -1 : 1;
        for (int i = 0, x = origin.x(), y = origin.y(); i < straightLength + diagonalLength; ++i) {
            positions.add(new Position(x, y));
            if (straightLength < positions.size()) {
                x += dx;
                y += dy;
            } else {
                if (verticalStraight) {
                    y += dy;
                } else {
                    x += dx;
                }
            }
        }
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public String getMaterial() {
        return material;
    }

    @Override
    public List<Position> getPositions() {
        return List.copyOf(positions);
    }

    public PowerstateType getType() {
        return power.getType();
    }
}
