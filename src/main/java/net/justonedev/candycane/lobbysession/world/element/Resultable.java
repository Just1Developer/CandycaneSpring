package net.justonedev.candycane.lobbysession.world.element;

import net.justonedev.candycane.lobbysession.packet.Packet;
import net.justonedev.candycane.lobbysession.world.Position;
import net.justonedev.candycane.lobbysession.world.state.Powerstate;

import java.util.List;

public interface Resultable extends WorldObject {
    List<? extends Powerstate<?>> getInputs();
    List<? extends Powerstate<?>> getOutputs();
    List<Position> getInputPositions();
    List<Position> getOutputPositions();
    Packet updatePowerstate();
}
