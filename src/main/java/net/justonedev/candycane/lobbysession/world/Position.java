package net.justonedev.candycane.lobbysession.world;

public record Position(int x, int y) {
    public Position derive(int dx, int dy) {
        return new Position(this.x + dx, this.y + dy);
    }
}
