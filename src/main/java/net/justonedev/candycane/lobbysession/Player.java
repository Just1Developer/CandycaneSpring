package net.justonedev.candycane.lobbysession;

import lombok.Getter;
import net.justonedev.candycane.lobbysession.packet.Packet;
import net.justonedev.candycane.lobbysession.packet.PacketFormatter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Getter
public class Player {
    private static final PlayerInfoGenerator playerInfoGenerator = new PlayerInfoGenerator();

    private final String uuid;
    private final String name;
    private final String color;
    private final WebSocketSession session;

    public Player(WebSocketSession session) {
        this(
                session,
                playerInfoGenerator.generateUUID(),
                playerInfoGenerator.generateName(),
                playerInfoGenerator.generateColor()
        );
    }

    public Player(WebSocketSession session, String uuid, String name, String color) {
        this.session = session;
        this.uuid = uuid;
        this.name = name;
        this.color = color;
    }

    public void sendPacket(Packet packet) {
        try {
            session.sendMessage(new TextMessage(packet.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
