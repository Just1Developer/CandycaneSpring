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

public class NandGate implements Resultable {
    private final PersistentWorldState world;
    private final Position position;
    private final String objectUUID;
    private final int inputs;
    private final Size size;

    public NandGate(PersistentWorldState world, Position position) {
        this(world, position, 2);
    }

    public NandGate(PersistentWorldState world, Position position, int inputs) {
        this.world = world;
        this.position = position;
        this.objectUUID = ComponentFactory.generateComponentUUID();
        this.inputs = inputs;
        size = new Size(4, 2 * inputs - 1);
    }

    @Override
    public List<? extends Powerstate<?>> getInputs() {
        return getInputPositions().stream().map(world::getPowerState).toList();
    }

    @Override
    public List<Powerstate<?>> getOutputs() {
        var inputs = getInputs();
        // If not all inputs are boolean, return none (invalid)
        if (inputs.stream().anyMatch(state -> state.getType() != PowerstateType.POWER)) return List.of(Powerstate.ILLEGAL);
        // If all inputs are boolean and false
        if (inputs.stream().noneMatch(Powerstate::getBooleanValue)) return List.of(Powerstate.ON);
        return List.of(Powerstate.OFF);
    }

    @Override
    public List<Position> getInputPositions() {
        ArrayList<Position> positions = new ArrayList<>();
        for (int i = 0; i < inputs * 2; i += 2) {
            positions.add(position.derive(0, i));
        }
        return positions;
    }

    @Override
    public List<Position> getOutputPositions() {
        return List.of(position.derive(3, inputs - 1));
    }

    @Override
    public String getUuid() {
        return objectUUID;
    }

    @Override
    public String getMaterial() {
        return "NAND";
    }

    @Override
    public List<Position> getPositions() {
        return List.of(position);
    }

    @Override
    public Packet updatePowerstate() {
        var outputs = getOutputs();
        var outputPositions = getOutputPositions();
        Packet packet = new Packet();   // todo
        for (int i = 0; i < Math.min(outputs.size(), outputPositions.size()); i++) {
            packet = world.invokePowerUpdate(outputPositions.get(i), outputs.get(i));
        }
        return packet;
    }

    @Override
    public Size getSize() {
        return size;
    }
}
