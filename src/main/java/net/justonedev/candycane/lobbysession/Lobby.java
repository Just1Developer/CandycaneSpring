package net.justonedev.candycane.lobbysession;

import net.justonedev.candycane.lobbysession.world.PersistentWorldState;

import java.util.ArrayList;
import java.util.List;

public class Lobby {
    List<Player> players;
    PersistentWorldState world;

    public Lobby() {
        this.players = new ArrayList<>();
        this.world = new PersistentWorldState();
    }
}
