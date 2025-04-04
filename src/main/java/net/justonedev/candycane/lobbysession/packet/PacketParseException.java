/* (C)2025 */
package net.justonedev.candycane.lobbysession.packet;

import java.text.ParseException;

public class PacketParseException extends ParseException {
	public PacketParseException(String message, int errorOffset) {
		super(message, errorOffset);
	}

	public PacketParseException(String message) {
		super(message, 0);
	}

	public PacketParseException() {
		super("Packet parsing error", 0);
	}
}
