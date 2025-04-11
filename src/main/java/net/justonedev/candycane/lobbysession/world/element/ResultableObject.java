package net.justonedev.candycane.lobbysession.world.element;

import net.justonedev.candycane.lobbysession.world.state.Powerstate;

import java.util.List;

public interface ResultableObject extends WorldObject {
    List<Powerstate<?>> getInputs();
    List<Powerstate<?>> getOutputs();
}
