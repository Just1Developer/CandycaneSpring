/* (C)2025 */
package net.justonedev.candycane.lobbysession.packet;

import net.justonedev.candycane.lobbysession.Player;

import java.util.UUID;

public final class PacketFormatter {
	public static final Packet PACKET_KEEP_ALIVE = new Packet("type", "ALIVE");

	private PacketFormatter() {
	}

	public static Packet positionPacket(String uuid, String positionX, String positionY) {
		Packet p = new Packet();
		p.addAttribute("type", "POSITION");
		p.addAttribute("uuid", uuid);
		p.addAttribute("x", positionX);
		p.addAttribute("y", positionY);
		return p;
	}

	public static Packet updatePlayerPacket(Player player) {
		return updatePlayerPacket(player.getUuid(), player.getName(), player.getColor(), player.getX(), player.getY());
	}

	public static Packet updatePlayerPacket(String uuid, String name, String color, String x, String y) {
		Packet p = new Packet();
		p.addAttribute("type", "PLAYER");
		p.addAttribute("uuid", uuid);
		p.addAttribute("name", name);
		p.addAttribute("color", color);
		p.addAttribute("x", x);
		p.addAttribute("y", y);
		return p;
	}

	public static Packet selfInfoPacket(String uuid, String name, String color) {
		Packet p = new Packet();
		p.addAttribute("type", "SELF");
		p.addAttribute("uuid", uuid);
		p.addAttribute("name", name);
		p.addAttribute("color", color);
		return p;
	}

	public static Packet playerDisconnectPacket(String uuid) {
		Packet p = new Packet();
		p.addAttribute("type", "DISCONNECT");
		p.addAttribute("uuid", uuid);
		return p;
	}

	public static Packet getRelayPacket(Packet receivedPacket, String senderUUID) {
		Packet packet = new Packet(receivedPacket);
		if (packet.getAttribute("uuid").isEmpty()) {
			// Inject UUID into packet
			packet.addAttribute("uuid", senderUUID);
		}
		return packet;
	}
}
