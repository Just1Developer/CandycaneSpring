package net.justonedev.candycane.lobbysession;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@EnableScheduling
public class LobbyManager {
    private final Map<String, Lobby> perUUIDLobbies;
    private final List<Lobby> lobbies;

    public LobbyManager() {
        this.perUUIDLobbies = new HashMap<>();
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

    public void addNewPlayerToLobby(Lobby lobby, Player player) {
        lobby.addPlayer(player);
        perUUIDLobbies.put(player.getUuid(), lobby);
    }

    public void removePlayerFromLobby(WebSocketSession session) {
        String uuid = session.getAttributes().getOrDefault("uuid", "-").toString();
        Lobby lobby = perUUIDLobbies.get(uuid);;
        if (uuid == null) {
            Optional<Lobby> optionalLobby = perUUIDLobbies.values().stream().filter(
                    l -> l.containsPlayer(session)
            ).findFirst();
            if (optionalLobby.isPresent()) lobby = optionalLobby.get();
            else return;
        }
        lobby.removePlayer(session);
        perUUIDLobbies.remove(uuid);
    }

    @Scheduled(fixedRate = 29000)
    public void keepAlive() {
        lobbies.parallelStream().forEach(Lobby::keepAlive);
    }
}
