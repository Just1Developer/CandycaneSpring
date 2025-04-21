/* (C)2025 */
package net.justonedev.candycane.lobbysession;

import net.justonedev.candycane.lobbysession.packet.Packet;
import net.justonedev.candycane.lobbysession.packet.PacketFormatter;
import net.justonedev.candycane.lobbysession.packet.PacketProcessResult;
import net.justonedev.candycane.lobbysession.packet.PacketProcessResultFlag;
import net.justonedev.candycane.lobbysession.packet.PacketProcessResultType;
import net.justonedev.candycane.lobbysession.world.PersistentWorldState;
import net.justonedev.candycane.lobbysession.world.Position;
import net.justonedev.candycane.lobbysession.world.WorldBuildingResponse;
import net.justonedev.candycane.lobbysession.world.element.ComponentFactory;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
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
		// Update Brokenness
		player.sendPacket(world.getCurrentBrokennessStatePacket());

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

	public void sendOutPacket(Packet packet) {
		for (Player player : players) {
			player.sendPacket(packet);
		}
	}

	private PacketProcessResult processPacket(String uuid, Packet packet) {
		WorldBuildingResponse result;
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
			var factoryObject = ComponentFactory.createWorldObject(packet, world);
			result = world.addWorldObject(factoryObject);
			if (!result.isSuccess()) return PacketProcessResult.swallow();
			return PacketProcessResult.relay(result, PacketProcessResultFlag.SEND_POWER_UPDATE).withAttribute("uuid", factoryObject.getUuid());
		case "DELETE":
			Position position = new Position(packet);
			result = world.removeWorldObject(packet.getAttribute("elementUUID"), packet.getAttribute("material"), position);
			if (!result.isSuccess()) return PacketProcessResult.swallow();
			return PacketProcessResult.relay(result, PacketProcessResultFlag.SEND_POWER_UPDATE);
		case "DISCONNECT":
			// ...
			break;
		}
		return PacketProcessResult.relay();
	}

	public void packetReceived(String uuid, Packet packet) {
		PacketProcessResult result = processPacket(uuid, packet);
		if (result.getType() == PacketProcessResultType.SWALLOW) return;

		final Packet relayPacket = PacketFormatter.getRelayPacket(packet, uuid);
		boolean relayToSelf = Packet.shouldRelayToSelf(packet);

		// Special Stuff:
		var resultUUID = result.getAttribute("uuid");
		if (packet.getAttribute("type").equals("BUILD")
			&& !resultUUID.isEmpty()) {
			// Inject our element uuid
			relayPacket.addAttribute("elementUUID", resultUUID);
		}

		boolean relayOriginal;

		List<Packet> otherPackets = new ArrayList<>();
		var worldBuildingReponse = result.getWorldBuildingResponse();
		if (worldBuildingReponse.isPresent()) {
			var res = worldBuildingReponse.get();
			relayOriginal = res.isSendOriginalPacket();
			otherPackets.addAll(res.getSendPackets());
		} else {
            relayOriginal = true;
        }

        players.parallelStream().forEach(player -> {
			if (relayOriginal && (relayToSelf || !player.getUuid().equals(uuid))) {
				player.sendPacket(relayPacket);
			}
			otherPackets.forEach(player::sendPacket);
		});

		// This triggers a re-building of the power state, for proper packet-sending,
		// this must be created exactly once, hence why it's generated here.
		if (result.isFlagSet(PacketProcessResultFlag.SEND_POWER_UPDATE)) {
			world.sendNewPowerState();
		}
	}
}
