package net.justonedev.candycane.lobbysession.world.element.wire;

import net.justonedev.candycane.lobbysession.packet.Packet;

import java.util.HashSet;
import java.util.Set;

public class WireBrokennessState {
    private final Set<WireBrokennessStateEntry> entries;
    public WireBrokennessState() {
        entries = new HashSet<>();
    }

    public void addEntry(WireBrokennessStateEntry entry) {
        if (entry != null) entries.add(entry);
    }

    public Packet toPacket() {
        Packet packet = new Packet();
        packet.addAttribute("type", "WIREBROKENSTATE");
        packet.addAttribute("brokenState", "[%s]".formatted(String.join(",", entries.stream().map(entry -> "{\"uuid\":\"%s\",\"broken\":%b,\"updateType\":\"%s\"}".formatted(
                entry.uuid(), entry.isBroken(), entry.updateType()
        )).toList())));
        return packet;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public static WireBrokennessState difference(WireBrokennessState oldState, WireBrokennessState newState) {
        var difference = new WireBrokennessState();
        if (oldState == null) {
            newState.entries.forEach(entry -> {
                difference.addEntry(new WireBrokennessStateEntry(entry, WireBrokennessState.UpdateType.ADD));
            });
            return difference;
        }

        var oldStateCopy = new HashSet<>(oldState.entries);
        for (var entry : newState.entries) {
            var matchingState = oldStateCopy.stream().filter(entry1 -> entry1.uuid().equals(entry.uuid())).findFirst();
            // If present in both but not equal:
            if (matchingState.isPresent()) {
                if (!matchingState.get().equals(entry)) {
                    difference.addEntry(new WireBrokennessStateEntry(entry, WireBrokennessState.UpdateType.MOD));
                }
                oldStateCopy.remove(matchingState.get());
            } else {
                difference.addEntry(new WireBrokennessStateEntry(entry, WireBrokennessState.UpdateType.ADD));
            }
        }
        for (var remaining : oldStateCopy) {
            difference.addEntry(new WireBrokennessStateEntry(remaining, WireBrokennessState.UpdateType.DEL));
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
