package net.justonedev.candycane.lobbysession;

import lombok.Getter;
import lombok.Setter;
import net.justonedev.candycane.lobbysession.packet.Packet;
import net.justonedev.candycane.lobbysession.packet.PacketFormatter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Objects;

@Getter
public class Player {
    @Setter
    private LobbyManager lobbyManager;
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

    public Player(WebSocketSession session, String uuid) {
        this(
                session,
                uuid,
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
        } catch (IOException | IllegalStateException e) {
            if (lobbyManager != null && !session.isOpen()) {
                lobbyManager.removePlayerFromLobby(session);
            }
        }
    }

    public Packet getSelfPacket() {
        return PacketFormatter.selfInfoPacket(uuid, name, color);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(uuid, player.uuid) && Objects.equals(session, player.session);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, session);
    }
}
