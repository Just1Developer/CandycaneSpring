package net.justonedev.candycane.lobbysession.world;

import net.justonedev.candycane.lobbysession.packet.Packet;

public record Position(int x, int y) {
    public Position derive(int dx, int dy) {
        return new Position(this.x + dx, this.y + dy);
    }
    private Position(String x, String y) {
        this(parse(x), parse(y));
    }
    public Position(Packet packet) {
        this(packet.getAttribute("fromX"), packet.getAttribute("fromY"));
    }
    private static int parse(String s) {
        s = s.trim();
        if (s.isEmpty()) return 0;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
