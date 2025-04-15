package net.justonedev.candycane.lobbysession.world;

import net.justonedev.candycane.lobbysession.world.state.PowerstateType;

import java.util.Objects;

public record WorldPowerStateEntry(String uuid, PowerstateType powerstate, String stateValue, WorldPowerState.UpdateType updateType) {
    public WorldPowerStateEntry(String uuid, PowerstateType powerstate, String stateValue) {
        this(uuid, powerstate, stateValue, WorldPowerState.UpdateType.NUL);
    }
    public WorldPowerStateEntry(WorldPowerStateEntry entry, WorldPowerState.UpdateType updateType) {
        this(entry.uuid, entry.powerstate, entry.stateValue, updateType);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WorldPowerStateEntry that = (WorldPowerStateEntry) o;
        return Objects.equals(uuid, that.uuid) && Objects.equals(stateValue, that.stateValue) && powerstate == that.powerstate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, powerstate, stateValue);
    }
}
