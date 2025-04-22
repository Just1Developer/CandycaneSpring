package net.justonedev.candycane.lobbysession.world.element;

import net.justonedev.candycane.lobbysession.world.Position;
import net.justonedev.candycane.lobbysession.world.state.Powerstate;

import java.util.ArrayList;
import java.util.List;

public abstract class Resultable implements WorldObject {
    private Position topLeftCorner;
    public abstract List<? extends Powerstate<?>> getInputs();
    public abstract List<? extends Powerstate<?>> getOutputs();
    public abstract List<Position> getInputPositions();
    public abstract List<Position> getOutputPositions();
    public abstract void updatePowerstate();

    @Override
    public List<Position> getPositions() {
        ArrayList<Position> positions = new ArrayList<>();
        positions.add(topLeftCorner);
        positions.addAll(getInputPositions());
        positions.addAll(getOutputPositions());
        return positions;
    }

    protected void setTopLeftCorner(Position topLeftCorner) {
        this.topLeftCorner = topLeftCorner;
    }

    @Override
    public Position getTopLeftCorner() {
        return topLeftCorner;
    }
}
