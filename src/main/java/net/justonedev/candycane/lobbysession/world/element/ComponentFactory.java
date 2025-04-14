package net.justonedev.candycane.lobbysession.world.element;

import net.justonedev.candycane.lobbysession.world.PersistentWorldState;
import net.justonedev.candycane.lobbysession.world.Position;
import net.justonedev.candycane.lobbysession.world.element.gate.NandGate;

import java.util.Random;

public enum ComponentFactory {
    ;
    private static final Random RANDOM = new Random();
    public static String generateComponentUUID() {
        return String.format("%08X-%04X-%08X-%04X-%04X-%012X", RANDOM.nextInt(0x7FFFFFFF), RANDOM.nextInt(0xFFFF), RANDOM.nextInt(0x7FFFFFFF), RANDOM.nextInt(0xFFFF), RANDOM.nextInt(0xFFFF),
                RANDOM.nextLong() & 0xFFFFFFFFFFFFL);
    }

    public static WorldObject createWorldObject(String type, PersistentWorldState world, Position position) {
        if (type.startsWith("NAND")) {
            int inputs = type.length() > 4 ? type.charAt(4) - '0' : 2;
            return new NandGate(world, position, inputs);
        }
        return null;
    }
}
