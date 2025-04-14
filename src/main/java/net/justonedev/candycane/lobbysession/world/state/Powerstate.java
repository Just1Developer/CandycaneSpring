package net.justonedev.candycane.lobbysession.world.state;

import lombok.Getter;
import net.justonedev.candycane.Signature;

@Getter
public class Powerstate<T> {
    public static final Powerstate<Boolean> ON = new Powerstate<>(true);
    public static final Powerstate<Boolean> OFF = new Powerstate<>(false);
    public static final Powerstate<Byte> ZERO_BYTE = new Powerstate<>((byte) 0);
    public static final Powerstate<Integer> ZERO_INT = new Powerstate<>(0);
    public static final Powerstate<Float> ILLEGAL = new Powerstate<>(0f);

    private final T value;
    private final PowerstateType type;

    public Powerstate(T defaultValue) {
        this.value = defaultValue;
        this.type = PowerstateType.fromClass(defaultValue.getClass());
    }

    public boolean getBooleanValue() {
        if (type == PowerstateType.POWER) return (boolean) value;
        return false;
    }

    public byte getByteValue() {
        if (type == PowerstateType.BYTE) return (byte) value;
        return 0;
    }

    public int getIntegerValue() {
        if (type == PowerstateType.NUMERIC) return (int) value;
        return 0;
    }
}
