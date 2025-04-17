package net.justonedev.candycane.lobbysession.world;

import net.justonedev.candycane.lobbysession.Lobby;
import net.justonedev.candycane.lobbysession.packet.Packet;
import net.justonedev.candycane.lobbysession.world.algorithm.TarjansSCC;
import net.justonedev.candycane.lobbysession.world.element.ComponentFactory;
import net.justonedev.candycane.lobbysession.world.element.PowerStateable;
import net.justonedev.candycane.lobbysession.world.element.Resultable;
import net.justonedev.candycane.lobbysession.world.element.wire.Wire;
import net.justonedev.candycane.lobbysession.world.element.WorldObject;
import net.justonedev.candycane.lobbysession.world.element.wire.WireBrokennessState;
import net.justonedev.candycane.lobbysession.world.element.wire.WireBrokennessStateEntry;
import net.justonedev.candycane.lobbysession.world.element.wire.WireSplit;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PersistentWorldState {
    private final ConcurrentHashMap<Position, WorldObject> worldObjects;
    // Separated because one is used in an algorithm, the other isn't
    private final ConcurrentHashMap<Position, Set<Wire<?>>> connectionPoints;
    private final ConcurrentHashMap<Position, Set<Wire<?>>> wireIntermediates;

    private final Lobby lobby;
    private WorldPowerState currentPowerState = new WorldPowerState();
    private WireBrokennessState currentBrokennessState = new WireBrokennessState();

    public PersistentWorldState(Lobby lobby) {
        this.lobby = lobby;
        worldObjects = new ConcurrentHashMap<>();
        connectionPoints = new ConcurrentHashMap<>();
        wireIntermediates = new ConcurrentHashMap<>();
    }

    public synchronized WorldBuildingResponse addWorldObject(WorldObject worldObject) {
        if (worldObject == null) {
            return WorldBuildingResponse.dont();
        }

        WorldBuildingResponse response = WorldBuildingResponse.sendOriginal();

        if (worldObject instanceof Wire<?> wire) {
            var origin = wire.getOrigin();
            var originList = connectionPoints.get(origin);
            boolean hasPreviousOrigin = originList != null && !originList.isEmpty();
            if (!hasPreviousOrigin) originList = new HashSet<>();

            var target = wire.getTarget();
            var targetList = connectionPoints.get(target);
            boolean hasPreviousTarget = targetList != null && !targetList.isEmpty();
            if (!hasPreviousTarget) targetList = new HashSet<>();

            // Wire Splitting: If collides: Move target of intercepting wire, create new wire from intercept to every other one
            // Basically: Remove all previous wires, create new wires with old data
            var interceptOrigin = wireIntermediates.get(origin);
            WireSplit wireSplit = new WireSplit();
            if (!hasPreviousOrigin && interceptOrigin != null && !interceptOrigin.isEmpty()) {
                // We have a list of all wires that run along this point.
                // We need to:
                // - Move wire target to intercept point
                // - Create new wire from intercept to old target, with wire data (though that will probably be overwritten anyway)
                // - Send this BUILD Packet with the wire data immediately (as collection of all the new wires, as WIRESPLIT package)
                splitWires(interceptOrigin, origin, originList, wireSplit);
            }

            var interceptTarget = wireIntermediates.get(target);
            if (!hasPreviousTarget && interceptTarget != null && !interceptTarget.isEmpty()) {
                // We have a list of all wires that run along this point.
                splitWires(interceptTarget, target, targetList, wireSplit);
            }

            if (!hasPreviousOrigin || !hasPreviousTarget) response = WorldBuildingResponse.sendThisAfter(wireSplit.toPacket());

            // Add the wire (original)

            originList.add(wire);
            connectionPoints.put(wire.getOrigin(), originList);

            targetList.add(wire);
            connectionPoints.put(wire.getTarget(), targetList);

            addIntermediates(wire);
        } else {
            // Collision is (currently) only relevant for non-wires
            if (objectCollides(worldObject)) {
                return WorldBuildingResponse.dont();
            }

            worldObject.getPositions().forEach(pos -> worldObjects.put(pos, worldObject));
            if (worldObject instanceof Resultable resultable) {
                resultable.getInputPositions().forEach(pos -> worldObjects.put(pos, worldObject));
                resultable.getOutputPositions().forEach(pos -> worldObjects.put(pos, worldObject));
                resultable.updatePowerstate();
            }
        }
        reevaluateWireBrokenness();
        updatePowerstate();

        var difference = updateBrokennessState();
        if (!difference.isEmpty()) response.addPacket(difference.toPacket());

        return response;
    }

    public WorldBuildingResponse removeWorldObject(String objectUUID, Position probablePosition) {
        // Get the world object:
        var ifObject = worldObjects.get(probablePosition);
        if (ifObject != null) {
            if (ifObject.getUuid().equals(objectUUID)) {
                worldObjects.remove(probablePosition);
            } else {
                worldObjects.entrySet()
                        .stream()
                        .filter(o -> o.getValue().getUuid().equals(objectUUID))
                        .findFirst()
                        .ifPresent(newObject -> worldObjects.remove(newObject.getKey()));
            }
        } else {
            // Probably a wire
            // todo Cannot invoke "java.util.Set.stream()" because "wireList" is null: removeWorldObject(PersistentWorldState.java:133)
            var wireList = connectionPoints.get(probablePosition);
            var wire = wireList
                    .stream()
                    .filter(o -> o.getUuid().equals(objectUUID))
                    .findFirst();
            if (wire.isPresent()) {
                removeWire(wire.get());
            } else {
                // Wire not found. Search more extensively.
                connectionPoints.values()
                        .stream()
                        .mapMulti((BiConsumer<? super Set<Wire<?>>, ? super Consumer<Wire<?>>>) Iterable::forEach)
                        .filter(o -> o.getUuid().equals(objectUUID))
                        .toList()
                        .forEach(this::removeWire);
            }
        }

        WorldBuildingResponse response = WorldBuildingResponse.sendOriginal();

        reevaluateWireBrokenness();
        updatePowerstate();

        var difference = updateBrokennessState();
        if (!difference.isEmpty()) response.addPacket(difference.toPacket());
        return response;
    }

    private void removeWire(Wire<?> wireToRemove) {
        var originList = connectionPoints.get(wireToRemove.getOrigin());
        if (originList != null) {
            originList.remove(wireToRemove);
            connectionPoints.put(wireToRemove.getOrigin(), originList);
        }
        var targetList = connectionPoints.get(wireToRemove.getTarget());
        if (targetList != null) {
            targetList.remove(wireToRemove);
            connectionPoints.put(wireToRemove.getTarget(), targetList);
        }
        wireIntermediates.forEach((position, wires) -> wires.remove(wireToRemove));
    }

    private void addIntermediates(Wire<?> wire) {
        wire.getPositions().forEach(position -> {
            var wireList = wireIntermediates.get(position);
            if (wireList == null) wireList = new HashSet<>();
            wireList.add(wire);
            wireIntermediates.put(position, wireList);
        });
    }

    private synchronized void splitWires(Set<Wire<?>> wireList, Position splitPoint, Set<Wire<?>> wireListAtSplitPoint, WireSplit wireSplit) {
        for (var otherWire : new ArrayList<>(wireList)) {
            var oldTarget = otherWire.getTarget();
            var oldTargetList = connectionPoints.get(oldTarget);
            if (oldTargetList == null) oldTargetList = new HashSet<>();
            oldTargetList.remove(otherWire);
            Wire<?> newWire = otherWire.splitAt(splitPoint);
            oldTargetList.add(newWire);
            // Add both to split point connection
            wireListAtSplitPoint.add(otherWire);
            wireListAtSplitPoint.add(newWire);

            // Remove old intermediates and add new ones:
            newWire.getPositions().forEach(position -> {
                var interWireList = wireIntermediates.get(position);
                if (interWireList == null) interWireList = new HashSet<>();
                interWireList.add(newWire);
                if (!position.equals(splitPoint)) interWireList.remove(otherWire);
                wireIntermediates.put(position, interWireList);
            });

            connectionPoints.put(oldTarget, oldTargetList);

            // Add old and new wires to wiresplit
            wireSplit.addMovedOrigin(otherWire);
            wireSplit.addNewSplitWire(newWire);
        }
    }

    public synchronized void sendNewPowerState() {
        var newPowerState = generatePowerState();
        var difference = WorldPowerState.difference(currentPowerState, newPowerState);
        currentPowerState = newPowerState;
        if (!difference.isEmpty()) lobby.sendOutPacket(difference.toPacket());
    }

    private synchronized WireBrokennessState updateBrokennessState() {
        var newBrokennessState = generateBrokennessState();
        var difference = WireBrokennessState.difference(currentBrokennessState, newBrokennessState);
        currentBrokennessState = newBrokennessState;
        return difference;
    }

    public Packet getCurrentPowerStatePacket() {
        return WorldPowerState.difference(null, currentPowerState).toPacket();
    }

    public Packet getCurrentBrokennessStatePacket() {
        return WireBrokennessState.difference(null, currentBrokennessState).toPacket();
    }

    public Packet getCurrentWorldStatePacket() {
        Packet packet = new Packet("type", "WORLDSTATE");
        List<String> objects = new ArrayList<>();
        final String format = "{\"uuid\":\"%s\",\"material\":\"%s\",\"fromX\":%d,\"fromY\":%d,\"toX\":%d,\"toY\":%d}";
        worldObjects.values().forEach(o -> {
            var positions = o.getPositions();
            var pos = positions.isEmpty() ? new Position(0, 0) : positions.getFirst();  // If there is no position, it won't matter in the frontend
            objects.add(format.formatted(
                    o.getUuid(),
                    o.getMaterial(),
                    pos.x(),
                    pos.y(),
                    pos.x(),
                    pos.y()
            ));
        });
        connectionPoints.values().stream().mapMulti((BiConsumer<? super Set<Wire<?>>, ? super Consumer<Wire<?>>>) Iterable::forEach).forEach(wire -> {
            objects.add(format.formatted(
                    wire.getUuid(),
                    wire.getMaterial(),
                    wire.getOrigin().x(),
                    wire.getOrigin().y(),
                    wire.getTarget().x(),
                    wire.getTarget().y()
            ));
        });
        packet.addAttribute("worldState", "[%s]".formatted(String.join(",", objects)));
        return packet;
    }

    private record OutputWireFloodfillResult(Set<Wire<?>> allWires, Set<Position> directlyReachableOutputs) { }

    private synchronized void reevaluateWireBrokenness() {
        connectionPoints.forEach((_, list) -> list.forEach(Wire::resetBrokennessState));
        Map<Position, List<Position>> inputPositions = getPositionListMap();

        Set<Wire<?>> previous = new HashSet<>();
        Set<Wire<?>> current = new HashSet<>();

        Map<Position, Set<Wire<?>>> outputMaps = new HashMap<>();
        Map<Position, Set<Position>> dependencyGraph = new HashMap<>();
        Set<Position> brokenOutputs = new HashSet<>();

        for (var entry : inputPositions.entrySet()) {
            var output = entry.getKey();
            var inputs = entry.getValue();
            var allWiresAndOutputs = floodFillWiresAndOutputs(output);
            var allWires = allWiresAndOutputs.allWires;
            outputMaps.put(output, allWires);
            dependencyGraph.put(output, allWiresAndOutputs.directlyReachableOutputs);
            current.clear();

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

        // Indirect Circular Dependencies
        TarjansSCC<Position> graphSCC = new TarjansSCC<>(dependencyGraph);
        brokenOutputs.addAll(graphSCC.getNodesInCycles());
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
        Set<Wire<?>> wires = connectionPoints.get(startPosition);
        if (wires == null) return;
        for (Wire<?> wire : wires) {
            if (wire.isSingleWire()) continue;
            if (currentWires.add(wire)) floodFillWiresRec(wire.getOpposite(startPosition), currentWires);
        }
    }

    private synchronized OutputWireFloodfillResult floodFillWiresAndOutputs(Position startPosition) {
        HashSet<Wire<?>> wires = new HashSet<>();
        HashSet<Position> outputs = new HashSet<>();
        floodFillWiresAndOutputsRec(startPosition, wires, outputs);
        return new OutputWireFloodfillResult(wires, outputs);
    }

    private synchronized void floodFillWiresAndOutputsRec(Position startPosition, Set<Wire<?>> currentWires, Set<Position> outputs) {
        Set<Wire<?>> wires = connectionPoints.get(startPosition);
        if (wires == null) return;
        for (Wire<?> wire : wires) {
            if (wire.isSingleWire()) continue;
            if (currentWires.add(wire)) {
                var newPos = wire.getOpposite(startPosition);
                var object = worldObjects.get(newPos);
                if (object instanceof Resultable resultable) {
                    outputs.addAll(resultable.getOutputPositions());
                }
                floodFillWiresAndOutputsRec(newPos, currentWires, outputs);
            }
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
                    (xDiff > 0 && xDiff < objSize.width()
                    || xDiff < 0 && -xDiff < size.width())
                    &&
                    (yDiff > 0 && yDiff < objSize.height()
                    || yDiff < 0 && -yDiff < size.height())
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
        String uuid = ComponentFactory.generateComponentUUID();
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
            allWiresPerOutput.get(position).forEach(wire -> {
                wire.setPower(output);
                wire.setEvaluated(true);
            });
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

    private WireBrokennessState generateBrokennessState() {
        WireBrokennessState brokennessState = new WireBrokennessState();
        for (var wires : connectionPoints.values()) {
            for (var wire : wires) {
                brokennessState.addEntry(new WireBrokennessStateEntry(wire.getUuid(), wire.isBroken()));
            }
        }
        return brokennessState;
    }

    public Powerstate<?> getPowerState(Position position) {
        var wires = connectionPoints.get(position);
        if (wires == null || wires.isEmpty()) return Powerstate.OFF;
        // all should be equals
        return wires.stream().toList().getFirst().getPower();
    }
}
