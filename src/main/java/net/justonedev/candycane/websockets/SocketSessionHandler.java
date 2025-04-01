package net.justonedev.candycane.websockets;

import lombok.extern.slf4j.Slf4j;
import net.justonedev.candycane.lobbysession.LobbyManager;
import net.justonedev.candycane.lobbysession.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
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
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        String uuid = getUuid(session);
        if (uuid.isEmpty()) {
            Player p = new Player(session);
            session.getAttributes().put("uuid", uuid);
        }
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

    }

    /**
     * Extracts the String from the given Session. If there is no identification,
     * returns the default String.
     *
     * @param session The WebSocketSession
     * @return The String of the User of this session.
     */
    @NonNull private static String getUuid(@NonNull WebSocketSession session) {
        return session.getAttributes().getOrDefault("uuid", "").toString();
    }
}
