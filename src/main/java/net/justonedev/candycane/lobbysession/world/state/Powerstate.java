package net.justonedev.candycane.lobbysession.world.state;

import lombok.Getter;

@Getter
public class Powerstate<T> {
    private final T value;
    private final PowerstateType type;

    public Powerstate(T defaultValue) {
        this.value = defaultValue;
        this.type = PowerstateType.fromClass(defaultValue.getClass());
    }
}
