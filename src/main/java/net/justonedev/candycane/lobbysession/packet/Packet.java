/* (C)2025 */
package net.justonedev.candycane.lobbysession.packet;

import java.util.HashMap;
import java.util.Map;

public class Packet {
	private final Map<String, String> attributes;

	public Packet() {
		this.attributes = new HashMap<>();
	}

	public Packet(String key, String value) {
		this();
		addAttribute(key, value);
	}

	public Packet(Packet copy) {
		this.attributes = new HashMap<>(copy.attributes);
	}

	public String getAttribute(String key) {
		return attributes.getOrDefault(key, "");
	}

	public void addAttribute(String key, String value) {
		attributes.put(key, value);
	}

	@Override
	public String toString() {
		return "{%s}"
				.formatted(
						String.join(", ",
								attributes.entrySet().stream()
										.map(entry -> "\"%s\":%s".formatted(entry.getKey(),
												entry.getValue().matches("-?\\d+(?:\\.\\d+)?") ? entry.getValue() : "\"%s\"".formatted(entry.getValue())))
										.toList()));
	}

	public static Packet parseFromJSON(String json) throws PacketParseException {
		try {
			Packet packet = new Packet();
			String[] pairs = json.replaceAll("[{}\"]", "").split(",");
			for (String pair : pairs) {
				String[] keyValue = pair.split(":");
				if (keyValue.length == 2) {
					packet.addAttribute(keyValue[0].trim(), keyValue[1].trim());
				}
			}
			return packet;
		} catch (Exception e) {
			throw new PacketParseException();
		}
	}

	public static boolean shouldRelayToSelf(Packet packet) {
		return switch (packet.getAttribute("type")) {
		case "POSITION", "PLAYER", "DISCONNECT" -> false;
		default -> true;
		};
	}
}
