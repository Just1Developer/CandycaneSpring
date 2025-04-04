package net.justonedev.candycane.websockets;

import lombok.extern.slf4j.Slf4j;
import net.justonedev.candycane.lobbysession.LobbyManager;
import net.justonedev.candycane.lobbysession.Player;
import net.justonedev.candycane.lobbysession.packet.Packet;
import net.justonedev.candycane.lobbysession.packet.PacketFormatter;
import net.justonedev.candycane.lobbysession.packet.PacketParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@Slf4j
@EnableScheduling
public class SocketSessionHandler extends TextWebSocketHandler {
    @Autowired
    private LobbyManager lobbyManager;

    /**
     * When a websocket is connected, this registers it into the internal maps to
     * keep track when update commands are to be sent out.
     *
     * @param session The session.
     */
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws IOException {
        String uuid = getUuid(session);
        if (uuid.isEmpty()) {
            session.close(CloseStatus.BAD_DATA);
            log.info("Closing Session with no UUID");
            return;
        }
        // Send self packet after connection
        Player player = lobbyManager.getOrCreatePlayer(uuid, session);
        player.sendPacket(player.getSelfPacket());
        lobbyManager.relayPacketReceived(uuid, PacketFormatter.updatePlayerPacket(uuid, player.getName(), player.getColor()));
        log.info("Player {} connected (Name: {}, Color: {})", uuid, player.getName(), player.getColor());
    }

    /**
     * Closes a given session and removes it from the internal session handler
     * storage.
     *
     * @param session The session to close.
     * @param status  The CloseStatus to close the connection with internally.
     * @throws IOException If an IOException is thrown while closing the session.
     */
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws IOException {
        String uuid = getUuid(session);
        if (!uuid.isEmpty()) {
            lobbyManager.removePlayerFromLobby(session);
        }
        log.info("Player {} disconnected", uuid);
    }

    @Override
    public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) throws Exception {
        String uuid = getUuid(session);
        if (uuid.isEmpty()) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        String payload = message.getPayload().toString();
        Packet packet;
        try {
            packet = Packet.parseFromJSON(payload);
        } catch (PacketParseException e) {
            log.error("Packet parsing error", e);
            return;
        }
        lobbyManager.relayPacketReceived(uuid, packet);
    }

    /**
     * Extracts the String from the given Session. If there is no identification,
     * returns the default String.
     *
     * @param session The WebSocketSession
     * @return The String of the User of this session.
     */
    @NonNull
    private static String getUuid(@NonNull WebSocketSession session) {
        return session.getAttributes().getOrDefault("uuid", "").toString();
    }
}
