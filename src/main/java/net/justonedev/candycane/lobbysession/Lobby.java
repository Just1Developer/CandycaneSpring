/* (C)2025 */
package net.justonedev.candycane.lobbysession;

import net.justonedev.candycane.lobbysession.packet.Packet;
import net.justonedev.candycane.lobbysession.packet.PacketFormatter;
import net.justonedev.candycane.lobbysession.world.PersistentWorldState;
import org.springframework.cglib.core.Block;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Lobby {
	private final ConcurrentLinkedQueue<Player> players;
	private final PersistentWorldState world;

	public Lobby() {
		this.players = new ConcurrentLinkedQueue<>();
		this.world = new PersistentWorldState();
	}

	public void addPlayer(Player player) {
		if (player == null)
			return;
		for (Player p : players) {
			if (p.getUuid().equals(player.getUuid())) {
				return; // Player already exists
			}
		}
		Packet playerPacket = PacketFormatter.updatePlayerPacket(player);
		players.forEach(p -> {
			player.sendPacket(PacketFormatter.updatePlayerPacket(p));
			p.sendPacket(playerPacket);
		});

		// todo properly
		primitiveWorldState.forEach((key, value) -> {
			value.forEach(val -> {
				Packet packet = PacketFormatter.buildPacket(key, "BLOCK", val[0], val[1]);
				player.sendPacket(packet);
			});
		});

		players.add(player);
	}

	public void removePlayer(Player player) {
		players.remove(player);
	}

	public void removePlayer(WebSocketSession session, String uuid) {
		Packet packet = PacketFormatter.playerDisconnectPacket(uuid);
		players.removeIf(player -> player.getSession().equals(session));
		players.forEach(p -> p.sendPacket(packet));
		processPacket(uuid, packet);
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

	private final ConcurrentHashMap<String, List<String[]>> primitiveWorldState = new ConcurrentHashMap<>();

	private void processPacket(String uuid, Packet packet) {
		switch (packet.getAttribute("type")) {
		case "POSITION":
			getPlayer(uuid).ifPresent(player -> {
				String x = packet.getAttribute("x");
				if (x.isEmpty())
					x = "0";
				String y = packet.getAttribute("y");
				if (y.isEmpty())
					y = "0";
				player.setX(x);
				player.setY(y);
			});
			break;
		case "BUILD":
			// ...
			var list = primitiveWorldState.get(uuid);
			if (list == null)
				list = new ArrayList<>();
			list.add(new String[] { packet.getAttribute("x"), packet.getAttribute("y") });
			primitiveWorldState.put(uuid, list);
			break;
		case "DISCONNECT":
			// ...
			System.out.printf("Player %s disconnected (%d)%n", uuid, primitiveWorldState.size());
			primitiveWorldState.remove(uuid);
			System.out.printf("=>> POST (%d)%n", primitiveWorldState.size());
			break;
		}
	}

	public void packetReceived(String uuid, Packet packet) {
		processPacket(uuid, packet);
		final Packet relayPacket = PacketFormatter.getRelayPacket(packet, uuid);
		boolean relayToSelf = Packet.shouldRelayToSelf(packet);
		players.parallelStream().forEach(player -> {
			if (relayToSelf || !player.getUuid().equals(uuid)) {
				player.sendPacket(relayPacket);
			}
		});
	}
}
