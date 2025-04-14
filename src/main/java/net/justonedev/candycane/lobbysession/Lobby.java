/* (C)2025 */
package net.justonedev.candycane.lobbysession;

import net.justonedev.candycane.lobbysession.packet.Packet;
import net.justonedev.candycane.lobbysession.packet.PacketFormatter;
import net.justonedev.candycane.lobbysession.world.PersistentWorldState;
import net.justonedev.candycane.lobbysession.world.element.ComponentFactory;
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
		this.world = new PersistentWorldState(this);
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

		// Update World
		player.sendPacket(world.getCurrentWorldStatePacket());
		// Update Power
		player.sendPacket(world.getCurrentPowerStatePacket());

		// todo properly
		primitiveWorldState.forEach((key, value) -> {
			value.forEach(player::sendPacket);
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

	private final ConcurrentHashMap<String, List<Packet>> primitiveWorldState = new ConcurrentHashMap<>();

	public void sendOutPacket(Packet packet) {
		for (Player player : players) {
			player.sendPacket(packet);
		}
	}

	private boolean processPacket(String uuid, Packet packet) {
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
			boolean result = world.addWorldObject(ComponentFactory.createWorldObject(packet, world));
			// ...
			var list = primitiveWorldState.get(uuid);
			if (list == null)
				list = new ArrayList<>();
			list.add(PacketFormatter.getRelayPacket(packet, uuid));
			primitiveWorldState.put(uuid, list);
			// end stuff
			return result;
		case "DISCONNECT":
			// ...
			primitiveWorldState.remove(uuid);
			break;
		}
		return true;
	}

	public void packetReceived(String uuid, Packet packet) {
		boolean relay = processPacket(uuid, packet);
		if (!relay) return;
		final Packet relayPacket = PacketFormatter.getRelayPacket(packet, uuid);
		boolean relayToSelf = Packet.shouldRelayToSelf(packet);
		players.parallelStream().forEach(player -> {
			if (relayToSelf || !player.getUuid().equals(uuid)) {
				player.sendPacket(relayPacket);
			}
		});
	}
}
