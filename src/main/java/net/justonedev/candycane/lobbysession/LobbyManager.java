package net.justonedev.candycane.lobbysession;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LobbyManager {
    private final List<Lobby> lobbies;

    public LobbyManager() {
        this.lobbies = new ArrayList<>();
    }

    public Lobby createNew() {
        Lobby lobby = new Lobby();
        lobbies.add(lobby);
        return lobby;
    }

    public void removeLobby(Lobby lobby) {
        lobbies.remove(lobby);
    }

    public Lobby getFirstOrCreate() {
        if (lobbies.isEmpty()) {
            return createNew();
        }
        return lobbies.getFirst();
    }
}
