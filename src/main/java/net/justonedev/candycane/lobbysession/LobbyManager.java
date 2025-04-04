package net.justonedev.candycane.lobbysession;

import net.justonedev.candycane.lobbysession.packet.Packet;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

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
        player.setLobbyManager(this);
        lobby.addPlayer(player);
        perUUIDLobbies.put(player.getUuid(), lobby);
    }

    public void removePlayerFromLobby(WebSocketSession session) {
        String uuid = session.getAttributes().getOrDefault("uuid", "-").toString();
        Lobby lobby = perUUIDLobbies.get(uuid);
        if (uuid == null) {
            Optional<Lobby> optionalLobby = perUUIDLobbies.values().stream().filter(
                    l -> l.containsPlayer(session)
            ).findFirst();
            if (optionalLobby.isPresent()) lobby = optionalLobby.get();
            else return;
        }
        // todo lobby is null on shutdown here
        lobby.removePlayer(session, uuid);
        perUUIDLobbies.remove(uuid);
    }

    /**
     * Gets or creates a new player. If the given UUID is associated with
     * a player, the player is returned (regardless of lobby).
     * If the UUID is not registered, it will create a new player,
     * add it to the first lobby and return it.
     * If no lobby exists yet, one will be created as well.
     *
     * @param uuid The UUID of the player.
     * @param session The session of the player that will be associated if the player does not exist.
     * @return The player associated with the given UUID.
     */
    public Player getOrCreatePlayer(String uuid, WebSocketSession session) {
        return getOrCreatePlayer(uuid, session, getFirstOrCreate());
    }

    /**
     * Gets or creates a new player. If the given UUID is associated with
     * a player, the player is returned (regardless of lobby).
     * If the UUID is not registered, it will create a new player,
     * add it to the given lobby and return it.
     *
     * @param uuid The UUID of the player.
     * @param session The session of the player that will be associated if the player does not exist.
     * @param lobby The lobby to add the player to if it is not already registered.
     * @return The player associated with the given UUID.
     */
    public Player getOrCreatePlayer(String uuid, WebSocketSession session, Lobby lobby) {
        if (perUUIDLobbies.containsKey(uuid)) {
            Optional<Player> player = perUUIDLobbies.get(uuid).getPlayer(uuid);
            if (player.isPresent()) return player.get();
        }
        Player newPlayer = new Player(session, uuid);
        addNewPlayerToLobby(lobby, newPlayer);
        return newPlayer;
    }

    @Scheduled(fixedRate = 29000)
    public void keepAlive() {
        lobbies.parallelStream().forEach(Lobby::keepAlive);
    }

    public void relayPacketReceived(String uuid, Packet packet) {
        Lobby lobby = perUUIDLobbies.get(uuid);
        if (lobby != null) {
            lobby.packetReceived(uuid, packet);
        }
    }
}
