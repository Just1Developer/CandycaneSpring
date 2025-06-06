package net.justonedev.candycane.lobbysession.world.element.gate;

import net.justonedev.candycane.lobbysession.packet.Packet;
import net.justonedev.candycane.lobbysession.world.PersistentWorldState;
import net.justonedev.candycane.lobbysession.world.Position;
import net.justonedev.candycane.lobbysession.world.Size;
import net.justonedev.candycane.lobbysession.world.element.ComponentFactory;
import net.justonedev.candycane.lobbysession.world.element.Resultable;
import net.justonedev.candycane.lobbysession.world.state.Powerstate;
import net.justonedev.candycane.lobbysession.world.state.PowerstateType;

import java.util.ArrayList;
import java.util.List;

public class NandGate extends Resultable {
    private final PersistentWorldState world;
    private final String objectUUID;
    private final int inputs;
    private final Size size;

    private List<Powerstate<?>> outputs;

    public NandGate(PersistentWorldState world, Position position) {
        this(world, position, 2);
    }

    public NandGate(PersistentWorldState world, Position position, int inputs) {
        this.world = world;
        setTopLeftCorner(position);
        this.objectUUID = ComponentFactory.generateComponentUUID();
        this.inputs = inputs;
        size = new Size(4, 2 * inputs - 1);
        updatePowerstate();
    }

    @Override
    public List<? extends Powerstate<?>> getInputs() {
        return getInputPositions().stream().map(world::getPowerState).toList();
    }

    @Override
    public List<Powerstate<?>> getOutputs() {
        return new ArrayList<>(outputs);
    }

    @Override
    public List<Position> getInputPositions() {
        ArrayList<Position> positions = new ArrayList<>();
        for (int i = 0; i < inputs * 2; i += 2) {
            positions.add(getTopLeftCorner().derive(0, i));
        }
        return positions;
    }

    @Override
    public List<Position> getOutputPositions() {
        return List.of(getTopLeftCorner().derive(3, inputs - 1));
    }

    @Override
    public String getUuid() {
        return objectUUID;
    }

    @Override
    public String getMaterial() {
        return "NAND" + inputs;
    }

    @Override
    public void updatePowerstate() {
        this.outputs = getCurrentOutputs();
    }

    private List<Powerstate<?>> getCurrentOutputs() {
        var inputs = getInputs();
        // If not all inputs are boolean, return none (invalid)
        if (inputs.stream().anyMatch(state -> state.getType() != PowerstateType.POWER)) {
            return List.of(Powerstate.ILLEGAL);
        }
        // If all inputs are boolean and false
        if (inputs.stream().noneMatch(Powerstate::getBooleanValue)) {
            return List.of(Powerstate.ON);
        }
        return List.of(Powerstate.OFF);
    }

    @Override
    public Size getSize() {
        return size;
    }
}
