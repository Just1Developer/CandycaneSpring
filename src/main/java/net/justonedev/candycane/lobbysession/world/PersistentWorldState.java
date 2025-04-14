package net.justonedev.candycane.lobbysession.world;

import net.justonedev.candycane.lobbysession.packet.Packet;
import net.justonedev.candycane.lobbysession.world.element.ResultableObject;
import net.justonedev.candycane.lobbysession.world.element.Wire;
import net.justonedev.candycane.lobbysession.world.element.WorldObject;
import net.justonedev.candycane.lobbysession.world.state.Powerstate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PersistentWorldState {
    private final ConcurrentHashMap<Position, WorldObject> worldObjects;
    private final ConcurrentHashMap<Position, ResultableObject> inputPositionRefs;
    // Separated because one is used in an algorithm, the other isn't
    private final ConcurrentHashMap<Position, List<Wire<?>>> connectionPoints;
    private final ConcurrentHashMap<Position, List<Wire<?>>> wireIntermediates;
    private final ConcurrentHashMap<Position, Powerstate<?>> powerState;

    public PersistentWorldState() {
        worldObjects = new ConcurrentHashMap<>();
        inputPositionRefs = new ConcurrentHashMap<>();
        connectionPoints = new ConcurrentHashMap<>();
        wireIntermediates = new ConcurrentHashMap<>();
        powerState = new ConcurrentHashMap<>();
    }

    public boolean addWorldObject(WorldObject worldObject) {
        if (worldObject == null) {
            return false;
        }
        if (objectCollides(worldObject)) {
            return false;
        }
        if (worldObject instanceof Wire<?> wire) {
            var list = connectionPoints.get(wire.getOrigin());
            if (list == null) list = new ArrayList<>();
            list.add(wire);
            connectionPoints.put(wire.getOrigin(), list);

            list = connectionPoints.get(wire.getTarget());
            if (list == null) list = new ArrayList<>();
            list.add(wire);
            connectionPoints.put(wire.getTarget(), list);

            wire.getPositions().forEach(position -> {
                var wireList = wireIntermediates.get(position);
                if (wireList == null) wireList = new ArrayList<>();
                wireList.add(wire);
                wireIntermediates.put(position, wireList);
            });
        } else {
            worldObject.getPositions().forEach(pos -> worldObjects.put(pos, worldObject));
            if (worldObject instanceof ResultableObject resultable) {
                worldObject.getPositions().forEach(pos -> worldObjects.put(pos, worldObject));
                resultable.getInputPositions().forEach(pos -> inputPositionRefs.put(pos, resultable));
                resultable.updatePowerstate();
            }
        }
        return true;
    }

    public boolean objectCollides(WorldObject worldObject) {
        var positions = worldObject.getPositions();
        if (positions.isEmpty()) return false;
        Position pos = positions.getFirst();
        Size size = worldObject.getSize();
        for (var entry : worldObjects.entrySet()) {
            var obj = entry.getValue();
            if (obj == worldObject) return true;
            Position objPos = entry.getKey();
            Size objSize = obj.getSize();
            // true if x collides and y collides:
            int xDiff = pos.x() - objPos.x();
            int yDiff = pos.y() - objPos.y();
            if (
                    // x collides:
                    (xDiff > 0 && xDiff <= objSize.width()
                    || xDiff < 0 && -xDiff <= size.width())
                    &&
                    (yDiff > 0 && yDiff <= objSize.height()
                    || yDiff < 0 && -yDiff <= size.height())
            ) {
                return true;
            }
        }
        /*
        // Check if any cable spots collide with inner (currently don't care)
        for (var entry : connectionPoints.entrySet()) {
            Position objPos = entry.getKey();
            for (var obj : entry.getValue()) {
                if (obj == worldObject) return true;
                Size objSize = obj.getSize();
                // true if x collides and y collides:
                int xDiff = pos.x() - objPos.x();
                int yDiff = pos.y() - objPos.y();
                if (
                    // x collides:
                        (xDiff > 0 && xDiff <= objSize.width()
                                || xDiff < 0 && -xDiff <= size.width())
                                &&
                                (yDiff > 0 && yDiff <= objSize.height()
                                        || yDiff < 0 && -yDiff <= size.height())
                ) {
                    return true;
                }
            }
        }
         */
        return false;
    }

    public <T> Packet invokePowerUpdate(Position position, Powerstate<T> newPowerstate) {
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

    public Powerstate<?> getPowerState(Position position) {
        return powerState.getOrDefault(position, Powerstate.OFF);
    }
}
