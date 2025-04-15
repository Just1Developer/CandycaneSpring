package net.justonedev.candycane.lobbysession.world.element.wire;

import lombok.Getter;
import lombok.Setter;
import net.justonedev.candycane.lobbysession.world.Position;
import net.justonedev.candycane.lobbysession.world.Size;
import net.justonedev.candycane.lobbysession.world.element.ComponentFactory;
import net.justonedev.candycane.lobbysession.world.element.WorldObject;
import net.justonedev.candycane.lobbysession.world.state.Powerstate;
import net.justonedev.candycane.lobbysession.world.state.PowerstateType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Wire<T> implements WorldObject {
    private static final Size SIZE = new Size(1, 1);

    private final String uuid;
    private final String material;
    @Getter
    private final Position origin;
    @Getter
    private Position target;
    private final List<Position> positions;

    @Getter
    @Setter
    private Powerstate<?> power;
    @Getter
    private boolean isTransmitting;
    @Getter
    @Setter
    private boolean isBroken;
    @Getter
    @Setter
    private boolean isConnectedToOutput;
    @Getter
    @Setter
    private boolean isEvaluated;
    @Getter
    private boolean isSingleWire;

    public Wire(Position origin, Position target) {
        this(ComponentFactory.generateComponentUUID(), origin, target);
    }
    public Wire(String uuid, Position origin, Position target) {
        this.uuid = uuid;
        this.material = "WIRE";
        this.origin = origin;
        this.target = target;

        int diffX = Math.abs(origin.x() - target.x());
        int diffY = Math.abs(origin.y() - target.y());
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

        // Off by default
        power = Powerstate.OFF;
    }

    public Wire<T> splitAt(Position splitPoint) {
        Wire<T> newWire = new Wire<>(splitPoint, this.target);
        newWire.setPower(new Powerstate<>(this.power));
        newWire.setBroken(isBroken);
        newWire.setConnectedToOutput(isConnectedToOutput);
        newWire.setEvaluated(isEvaluated);
        // Update own target:
        setTarget(splitPoint);
        return newWire;
    }

    public void setTarget(Position target) {
        this.target = target;
        isSingleWire = target.equals(this.origin);
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

    @Override
    public Size getSize() {
        return SIZE;
    }

    public void resetEvaluation() {
        isConnectedToOutput = false;
        isEvaluated = false;
    }

    public void resetBrokennessState() {
        isBroken = false;
    }

    public boolean evalNeedsAwaiting() {
        return isConnectedToOutput && !isEvaluated;
    }

    public Position getOpposite(Position position) {
        if (position.equals(target)) return origin;
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Wire<?> wire = (Wire<?>) o;
        return Objects.equals(uuid, wire.uuid) && Objects.equals(origin, wire.origin) && Objects.equals(target, wire.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, origin, target);
    }
}
