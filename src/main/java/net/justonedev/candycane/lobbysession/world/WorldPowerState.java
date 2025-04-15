package net.justonedev.candycane.lobbysession.world;

import net.justonedev.candycane.lobbysession.packet.Packet;

import java.util.HashSet;
import java.util.Set;

public class WorldPowerState {
    private final Set<WorldPowerStateEntry> entries;
    public WorldPowerState() {
        entries = new HashSet<>();
    }

    public void addEntry(WorldPowerStateEntry entry) {
        if (entry != null) entries.add(entry);
    }

    public Packet toPacket() {
        Packet packet = new Packet();
        packet.addAttribute("type", "POWERWORLDSTATE");
        packet.addAttribute("state", "[%s]".formatted(String.join(",", entries.stream().map(entry -> "{\"uuid\":\"%s\",\"powerType\":\"%s\",\"value\":\"%s\",\"updateType\":\"%s\"}".formatted(
                entry.uuid(), entry.powerstate(), entry.stateValue(), entry.updateType()
        )).toList())));
        return packet;
    }

    public static WorldPowerState difference(WorldPowerState oldState, WorldPowerState newState) {
        var difference = new WorldPowerState();
        if (oldState == null) {
            newState.entries.forEach(entry -> {
                difference.addEntry(new WorldPowerStateEntry(entry, UpdateType.ADD));
            });
            return difference;
        }

        var oldStateCopy = new HashSet<>(oldState.entries);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>BEGIN");
        for (var entry : newState.entries) {
            var matchingState = oldStateCopy.stream().filter(entry1 -> entry1.uuid().equals(entry.uuid())).findFirst();
            System.out.printf("State of UUID %s (state: %s, value: %s), present: %b%n", entry.uuid(), entry.powerstate(), entry.stateValue(), matchingState.isPresent());
            // If present in both but not equal:
            if (matchingState.isPresent()) {
                if (!matchingState.get().equals(entry)) {
                    System.out.printf("A matching state has been found, but it is different: UUID %s (state: %s, value: %s)%n", matchingState.get().uuid(), matchingState.get().powerstate(), matchingState.get().stateValue());
                    difference.addEntry(new WorldPowerStateEntry(entry, UpdateType.MOD));
                } else System.out.println("The two are equal");
                oldStateCopy.remove(matchingState.get());
            } else {
                System.out.println("No matching state found for UUID " + entry.uuid());
                difference.addEntry(new WorldPowerStateEntry(entry, UpdateType.ADD));
            }
        }
        System.out.println("All that are in the old state but not in the new must be marked as removed:");
        for (var remaining : oldStateCopy) {
            System.out.printf("Removing state of UUID %s (state: %s, value: %s)%n", remaining.uuid(), remaining.powerstate(), remaining.stateValue());
            difference.addEntry(new WorldPowerStateEntry(remaining, UpdateType.DEL));
        }
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>DONE");
        return difference;
    }

    public enum UpdateType {
        ADD,
        MOD,
        DEL,
        NUL,
    }
}
