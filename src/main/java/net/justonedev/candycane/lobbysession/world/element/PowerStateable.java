package net.justonedev.candycane.lobbysession.world.element;

import net.justonedev.candycane.lobbysession.world.state.Powerstate;

public interface PowerStateable extends Resultable {
    Powerstate<?> getPowerState();
}
