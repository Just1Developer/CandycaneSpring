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
        for (var entry : newState.entries) {
            var matchingState = oldStateCopy.stream().filter(entry1 -> entry1.uuid().equals(entry.uuid())).findFirst();
            // If present in both but not equal:
            if (matchingState.isPresent() && !matchingState.get().equals(entry)) {
                difference.addEntry(new WorldPowerStateEntry(entry, UpdateType.MOD));
                oldStateCopy.remove(matchingState.get());
            } else if (matchingState.isEmpty()) {
                difference.addEntry(new WorldPowerStateEntry(entry, UpdateType.ADD));
            }
        }
        for (var remaining : oldStateCopy) {
            difference.addEntry(new WorldPowerStateEntry(remaining, UpdateType.DEL));
        }
        return difference;
    }

    public enum UpdateType {
        ADD,
        MOD,
        DEL,
        NUL,
    }
}
