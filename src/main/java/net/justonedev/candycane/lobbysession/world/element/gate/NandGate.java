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

    private List<Powerstate<?>> outputs;

    public NandGate(PersistentWorldState world, Position position) {
        this(world, position, 2);
    }

    public NandGate(PersistentWorldState world, Position position, int inputs) {
        this.world = world;
        this.position = position;
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
    public void updatePowerstate() {
        this.outputs = getCurrentOutputs();
    }

    private List<Powerstate<?>> getCurrentOutputs() {
        System.out.printf(">>> Evaluating Nand Gate @ %s%n", position);
        var inputs = getInputs();
        System.out.printf(">>> Inputs: %s%n", String.join(", ", inputs.stream().map(input -> "%s => %s".formatted(input.getType(), input.getValue())).toList()));
        // If not all inputs are boolean, return none (invalid)
        if (inputs.stream().anyMatch(state -> state.getType() != PowerstateType.POWER)) {
            System.out.println("An Input has an illegal powerstate (!= POWER)");
            return List.of(Powerstate.ILLEGAL);
        }
        // If all inputs are boolean and false
        if (inputs.stream().noneMatch(Powerstate::getBooleanValue)) {
            System.out.println("All are off. Powerstate of NAND is ON");
            return List.of(Powerstate.ON);
        }
        System.out.println("One or more is on. Powerstate of NAND is OFF");
        return List.of(Powerstate.OFF);
    }

    @Override
    public Size getSize() {
        return size;
    }
}
