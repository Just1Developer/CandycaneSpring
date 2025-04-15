package net.justonedev.candycane.lobbysession.world.element.wire;

import java.util.Objects;

public record WireBrokennessStateEntry(String uuid, boolean isBroken, WireBrokennessState.UpdateType updateType) {
    public WireBrokennessStateEntry(String uuid, boolean isBroken) {
        this(uuid, isBroken, WireBrokennessState.UpdateType.NUL);
    }
    public WireBrokennessStateEntry(WireBrokennessStateEntry entry, WireBrokennessState.UpdateType updateType) {
        this(entry.uuid, entry.isBroken(), updateType);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WireBrokennessStateEntry that = (WireBrokennessStateEntry) o;
        return Objects.equals(uuid, that.uuid) && isBroken == that.isBroken;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, isBroken);
    }
}