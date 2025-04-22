package net.justonedev.candycane.lobbysession.world.element;

import net.justonedev.candycane.lobbysession.world.state.Powerstate;

public abstract class PowerStateable extends Resultable {
    public abstract Powerstate<?> getPowerState();
}
