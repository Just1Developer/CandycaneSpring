package net.justonedev.candycane.lobbysession.world;

import lombok.Getter;
import net.justonedev.candycane.lobbysession.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class WorldBuildingResponse {
    @Getter
    private final boolean success;
    @Getter
    private final boolean sendOriginalPacket;

    private final List<Packet> sendPackets;

    public WorldBuildingResponse(boolean success, boolean sendOriginalPacket) {
        this.success = success;
        this.sendOriginalPacket = sendOriginalPacket;
        this.sendPackets = new ArrayList<>();
    }

    private WorldBuildingResponse(boolean success, boolean sendOriginalPacket, Packet packet) {
        this.success = success;
        this.sendOriginalPacket = sendOriginalPacket;
        this.sendPackets = new ArrayList<>();
        this.sendPackets.add(packet);
    }

    public void addPacket(Packet packet) {
        this.sendPackets.add(packet);
    }

    public List<Packet> getSendPackets() {
        return new ArrayList<>(sendPackets);
    }

    public static WorldBuildingResponse dont() {
        return new WorldBuildingResponse(false, false);
    }

    public static WorldBuildingResponse sendOriginal() {
        return new WorldBuildingResponse(true, true);
    }

    public static WorldBuildingResponse sendInstead(Packet packet) {
        return new WorldBuildingResponse(true, false, packet);
    }

    public static WorldBuildingResponse sendThisAfter(Packet packet) {
        return new WorldBuildingResponse(true, true, packet);
    }

    @Override
    public String toString() {
        return "[Success: %b, Send Original: %b, %d additional packets]".formatted(success, sendOriginalPacket, sendPackets.size());
    }
}
