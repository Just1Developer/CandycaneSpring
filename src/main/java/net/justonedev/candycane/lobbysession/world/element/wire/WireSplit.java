package net.justonedev.candycane.lobbysession.world.element.wire;

import net.justonedev.candycane.lobbysession.packet.Packet;

import java.util.HashSet;
import java.util.Set;

public class WireSplit {
    private final Set<WireSplitUpdate> updates;

    public WireSplit() {
        updates = new HashSet<>();
    }

    public void addMovedOrigin(Wire<?> newWire) {
        updates.add(new WireSplitUpdate(newWire.getUuid(), SplitType.MOVED_TARGET, newWire));
    }

    public void addNewSplitWire(Wire<?> newWire) {
        updates.add(new WireSplitUpdate(newWire.getUuid(), SplitType.NEW_SPLIT_WIRE, newWire));
    }

    public Packet toPacket() {
        Packet packet = new Packet("type", "WIRESPLIT");
        packet.addAttribute("updates", "[%s]".formatted(String.join(",", updates.stream().map(WireSplitUpdate::toJSONString).toList())));
        return packet;
    }

    private record WireSplitUpdate(String uuid, SplitType type, Wire<?> newWire) {
        String toJSONString() {
            return switch (type) {
                case MOVED_TARGET -> "{\"uuid\":\"%s\",\"type\":\"MOVED\",\"toX\":%d,\"toY\":%d}".formatted(uuid, newWire.getTarget().x(), newWire.getTarget().y());
                case NEW_SPLIT_WIRE -> "{\"uuid\":\"%s\",\"type\":\"NEW\",\"fromX\":%d,\"fromY\":%d,\"toX\":%d,\"toY\":%d}"
                        .formatted(uuid, newWire.getOrigin().x(), newWire.getOrigin().y(), newWire.getTarget().x(), newWire.getTarget().y());
            };
        }
    }

    private enum SplitType {
        MOVED_TARGET,
        NEW_SPLIT_WIRE;
    }
}
