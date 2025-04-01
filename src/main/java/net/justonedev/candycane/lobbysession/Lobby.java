package net.justonedev.candycane.lobbysession;

import net.justonedev.candycane.lobbysession.packet.PacketFormatter;
import net.justonedev.candycane.lobbysession.world.PersistentWorldState;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

public class Lobby {
    private final List<Player> players;
    private final PersistentWorldState world;

    public Lobby() {
        this.players = new ArrayList<>();
        this.world = new PersistentWorldState();
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public void removePlayer(WebSocketSession session) {
        players.removeIf(player -> player.getSession().equals(session));
    }

    public boolean containsPlayer(Player player) {
        return players.contains(player);
    }

    public boolean containsPlayer(WebSocketSession session) {
        return players.stream().anyMatch(player -> player.getSession().equals(session));
    }

    public void keepAlive() {
        players.parallelStream().forEach(player -> player.sendPacket(PacketFormatter.PACKET_KEEP_ALIVE));
    }
}
