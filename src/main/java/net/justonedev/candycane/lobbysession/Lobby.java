package net.justonedev.candycane.lobbysession;

import net.justonedev.candycane.lobbysession.packet.Packet;
import net.justonedev.candycane.lobbysession.packet.PacketFormatter;
import net.justonedev.candycane.lobbysession.world.PersistentWorldState;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Lobby {
    private final ConcurrentLinkedQueue<Player> players;
    private final PersistentWorldState world;

    public Lobby() {
        this.players = new ConcurrentLinkedQueue<>();
        this.world = new PersistentWorldState();
    }

    public void addPlayer(Player player) {
        if (player != null) players.add(player);
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

    public Optional<Player> getPlayer(String uuid) {
        return players.stream().filter(p -> p.getUuid().equals(uuid)).findFirst();
    }

    public boolean containsPlayer(WebSocketSession session) {
        return players.stream().anyMatch(player -> player.getSession().equals(session));
    }

    public void keepAlive() {
        players.parallelStream().forEach(player -> player.sendPacket(PacketFormatter.PACKET_KEEP_ALIVE));
    }

    public void packetReceived(String uuid, Packet packet) {
        final Packet relayPacket = PacketFormatter.getRelayPacket(packet, uuid);
        players.parallelStream().forEach(player -> {
            if (player.getUuid().equals(uuid)) {
                player.sendPacket(relayPacket);
            }
        });
    }
}
