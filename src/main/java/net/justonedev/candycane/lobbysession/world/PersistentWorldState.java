package net.justonedev.candycane.lobbysession.world;

import net.justonedev.candycane.lobbysession.Lobby;
import net.justonedev.candycane.lobbysession.packet.Packet;
import net.justonedev.candycane.lobbysession.world.element.PowerStateable;
import net.justonedev.candycane.lobbysession.world.element.Resultable;
import net.justonedev.candycane.lobbysession.world.element.Wire;
import net.justonedev.candycane.lobbysession.world.element.WorldObject;
import net.justonedev.candycane.lobbysession.world.state.Powerstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PersistentWorldState {
    private final ConcurrentHashMap<Position, WorldObject> worldObjects;
    private final ConcurrentHashMap<Position, Resultable> inputPositionRefs;
    // Separated because one is used in an algorithm, the other isn't
    private final ConcurrentHashMap<Position, List<Wire<?>>> connectionPoints;
    private final ConcurrentHashMap<Position, List<Wire<?>>> wireIntermediates;
    private final ConcurrentHashMap<Position, Powerstate<?>> powerState;

    private final Lobby lobby;
    private WorldPowerState currentPowerState = new WorldPowerState();

    public PersistentWorldState(Lobby lobby) {
        this.lobby = lobby;
        worldObjects = new ConcurrentHashMap<>();
        inputPositionRefs = new ConcurrentHashMap<>();
        connectionPoints = new ConcurrentHashMap<>();
        wireIntermediates = new ConcurrentHashMap<>();
        powerState = new ConcurrentHashMap<>();
    }

    public synchronized boolean addWorldObject(WorldObject worldObject) {
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
            if (worldObject instanceof Resultable resultable) {
                worldObject.getPositions().forEach(pos -> worldObjects.put(pos, worldObject));
                resultable.getInputPositions().forEach(pos -> inputPositionRefs.put(pos, resultable));
                resultable.updatePowerstate();
            }
        }
        reevaluateWireBrokenness();
        return true;
    }

    public synchronized void sendNewPowerState() {
        var newPowerState = generatePowerState();
        var difference = WorldPowerState.difference(currentPowerState, newPowerState);
        currentPowerState = newPowerState;
        lobby.sendOutPacket(difference.toPacket());
    }

    public Packet getCurrentPowerStatePacket() {
        return WorldPowerState.difference(null, currentPowerState).toPacket();
    }

    public Packet getCurrentWorldStatePacket() {
        // todo
        return new Packet("type", "NONE");
    }

    private synchronized void reevaluateWireBrokenness() {
        connectionPoints.forEach((_, list) -> list.forEach(Wire::resetBrokennessState));
        Map<Position, List<Position>> inputPositions = getPositionListMap();

        Set<Wire<?>> previous = new HashSet<>();
        Set<Wire<?>> current = new HashSet<>();

        Map<Position, Set<Wire<?>>> outputMaps = new HashMap<>();
        Set<Position> brokenOutputs = new HashSet<>();

        for (var entry : inputPositions.entrySet()) {
            var output = entry.getKey();
            var inputs = entry.getValue();
            var allWires = floodFillWires(output);
            outputMaps.put(output, allWires);

            for (var wire : allWires) {
                if (
                    // Mark Short Circuits
                        (current.add(wire) && !previous.add(wire))
                    // Mark Circular Dependencies
                        || inputs.contains(wire.getOrigin()) || inputs.contains(wire.getTarget())
                ) {
                    brokenOutputs.add(output);
                    break;
                }
            }
        }

        brokenOutputs.forEach(position -> outputMaps.get(position).forEach(wire -> wire.setBroken(true)));
    }

    private synchronized Map<Position, List<Position>> getPositionListMap() {
        Map<Position, List<Position>> inputPositions = new HashMap<>();
        // See if we can distinctly map all wires to 0-1 outputs
        for (var object : worldObjects.values()) {
            if (!(object instanceof Resultable resultable)) continue;
            var outputPositions = resultable.getOutputPositions();
            var inputs = resultable.getInputPositions();
            outputPositions.forEach(position -> inputPositions.put(position, inputs));
        }
        return inputPositions;
    }

    private interface WireMethod {
        void run(Wire<?> wire);
    }

    private void forEachWireFrom(Position position, WireMethod method) {
        floodFillWires(position).forEach(method::run);
    }

    private synchronized Set<Wire<?>> floodFillWires(Position startPosition) {
        HashSet<Wire<?>> wires = new HashSet<>();
        floodFillWiresRec(startPosition, wires);
        return wires;
    }

    private synchronized void floodFillWiresRec(Position startPosition, Set<Wire<?>> currentWires) {
        List<Wire<?>> wires = connectionPoints.get(startPosition);
        if (wires == null) return;
        for (Wire<?> wire : wires) {
            if (wire.isSingleWire()) continue;
            if (currentWires.add(wire)) floodFillWiresRec(wire.getOpposite(startPosition), currentWires);
        }
    }

    public synchronized boolean objectCollides(WorldObject worldObject) {
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

    public synchronized void updatePowerstate() {
        Queue<Resultable> queue = new LinkedList<>();
        connectionPoints.forEach((_, list) -> list.forEach(Wire::resetEvaluation));
        Map<Position, Set<Wire<?>>> allWiresPerOutput = new HashMap<>();
        for (var obj : worldObjects.values()) {
            if (obj instanceof Resultable resultable) {
                resultable.getOutputPositions().forEach(position -> {
                    var allWiresForOutput = floodFillWires(position);
                    // Don't add broken wires
                    if (!allWiresForOutput.isEmpty() && allWiresForOutput.stream().anyMatch(Wire::isBroken)) {
                        allWiresPerOutput.put(position, new HashSet<>());
                        return;
                    }
                    allWiresPerOutput.put(position, allWiresForOutput);
                    allWiresForOutput.forEach(wire -> wire.setConnectedToOutput(true));
                });
                queue.add(resultable);
            }
        }

        Set<Resultable> processedObjects = new HashSet<>();

        // The plan: Loop through all resultables

        while (!queue.isEmpty()) {
            var resultable = queue.remove();
            boolean shouldSkip = false;
            for (var input : resultable.getInputPositions()) {
                var connectedWire = connectionPoints.get(input);
                if (connectedWire == null) continue;
                for (var wire : connectedWire) {
                    if (wire.evalNeedsAwaiting()) {
                        shouldSkip = true;
                        break;
                    }
                }
                if (shouldSkip) break;
            }
            if (shouldSkip) {
                queue.add(resultable);
                continue;
            }
            processedObjects.add(resultable);
            processObject(resultable, allWiresPerOutput);
        }

        for (var obj : worldObjects.values()) {
            if (obj instanceof Resultable resultable && !processedObjects.contains(resultable)) {
                processObject(resultable, allWiresPerOutput);
            }
        }
    }

    private synchronized void processObject(Resultable resultable, Map<Position, Set<Wire<?>>> allWiresPerOutput) {
        resultable.updatePowerstate();
        var outputs = resultable.getOutputs();
        var outputPos = resultable.getOutputPositions();
        for (int i = 0; i < Math.min(outputs.size(), outputPos.size()); i++) {
            var output = outputs.get(i);
            var position = outputPos.get(i);
            allWiresPerOutput.get(position).forEach(wire -> wire.setPower(output));
        }
    }

    private WorldPowerState generatePowerState() {
        WorldPowerState powerState = new WorldPowerState();
        for (var wires : connectionPoints.values()) {
            for (var wire : wires) {
                powerState.addEntry(new WorldPowerStateEntry(wire.getUuid(), wire.getType(), wire.getPower().getValue().toString()));
            }
        }
        for (var obj : worldObjects.values()) {
            if (obj instanceof PowerStateable state) {
                powerState.addEntry(new WorldPowerStateEntry(state.getUuid(), state.getPowerState().getType(), state.getPowerState().getValue().toString()));
            }
        }
        return powerState;
    }

    public Powerstate<?> getPowerState(Position position) {
        return powerState.getOrDefault(position, Powerstate.OFF);
    }
}
