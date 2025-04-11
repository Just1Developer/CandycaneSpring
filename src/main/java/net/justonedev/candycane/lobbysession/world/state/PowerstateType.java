package net.justonedev.candycane.lobbysession.world.state;

public enum PowerstateType {
    NONE,
    POWER,
    BYTE,
    NUMERIC;

    public static <T> PowerstateType fromClass(Class<T> clazz) {
        if (clazz == Integer.class || clazz == int.class || clazz == Long.class || clazz == long.class) {
            return NUMERIC;
        }
        if (clazz == Byte.class || clazz == byte.class) {
            return BYTE;
        }
        if (clazz == Boolean.class || clazz == boolean.class) {
            return POWER;
        }
        return NONE;
    }
}
