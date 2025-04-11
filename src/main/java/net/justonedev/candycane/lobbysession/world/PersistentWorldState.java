package net.justonedev.candycane.lobbysession.world;

import net.justonedev.candycane.lobbysession.packet.Packet;
import net.justonedev.candycane.lobbysession.world.element.Wire;
import net.justonedev.candycane.lobbysession.world.element.WorldObject;
import net.justonedev.candycane.lobbysession.world.state.Powerstate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PersistentWorldState {
    private final ConcurrentHashMap<Position, WorldObject> worldObjects;
    // Separated because one is used in an algorithm, the other isn't
    private final ConcurrentHashMap<Position, List<Wire<?>>> connectionPoints;
    private final ConcurrentHashMap<Position, List<Wire<?>>> wireIntermediates;

    public PersistentWorldState() {
        worldObjects = new ConcurrentHashMap<>();
        connectionPoints = new ConcurrentHashMap<>();
        wireIntermediates = new ConcurrentHashMap<>();
    }

    public void addLazyWorldObject(WorldObject worldObject) {
        if (worldObject == null) {
            return;
        }
        worldObject.getPositions().forEach(pos -> worldObjects.put(pos, worldObject));
    }

    public <T> Packet updatePower(Position position, Powerstate<T> newPowerstate) {
        Packet packet = new Packet();
        packet.addAttribute("data", updatePowerRecursively(position, newPowerstate));   // todo
        return packet;  // todo
    }

    public <T> String updatePowerRecursively(Position position, Powerstate<T> newPowerstate) {
        List<Wire<?>> wires = connectionPoints.get(position);
        if (wires == null) return "";
        // todo
        wires.forEach(wire -> {
            if (wire.getType() != newPowerstate.getType()) {
                wire.setBroken(true);
                return;   // todo
            }
            if (wire.getPower().getValue().equals(newPowerstate.getValue())) {
                return;
            }
            //wire.setPower(newPowerstate); // todo
            if (!wire.getOrigin().equals(position)) {
                updatePowerRecursively(wire.getOrigin(), newPowerstate);
            } else if (!wire.getTarget().equals(position)) {
                updatePowerRecursively(wire.getTarget(), newPowerstate);
            }
        });
        return "";  // todo
    }
}
