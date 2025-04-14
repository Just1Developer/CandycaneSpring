package net.justonedev.candycane.lobbysession.world;

import net.justonedev.candycane.lobbysession.world.state.PowerstateType;

public record WorldPowerStateEntry(String uuid, PowerstateType powerstate, String stateValue, WorldPowerState.UpdateType updateType) {
    public WorldPowerStateEntry(String uuid, PowerstateType powerstate, String stateValue) {
        this(uuid, powerstate, stateValue, WorldPowerState.UpdateType.NUL);
    }
    public WorldPowerStateEntry(WorldPowerStateEntry entry, WorldPowerState.UpdateType updateType) {
        this(entry.uuid, entry.powerstate, entry.stateValue, updateType);
    }
}
