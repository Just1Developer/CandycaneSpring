package net.justonedev.candycane.lobbysession.world.element;

import net.justonedev.candycane.lobbysession.world.Position;
import net.justonedev.candycane.lobbysession.world.Size;

import java.util.List;

public interface WorldObject {
    String getUuid();
    String getMaterial();
    List<Position> getPositions();
    Size getSize();
}
