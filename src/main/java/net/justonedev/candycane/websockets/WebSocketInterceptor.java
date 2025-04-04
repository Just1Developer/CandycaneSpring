/* (C)2025 */
package net.justonedev.candycane.websockets;

import lombok.extern.slf4j.Slf4j;
import net.justonedev.candycane.lobbysession.Player;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
public class WebSocketInterceptor implements HandshakeInterceptor {

	@Override
	public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler,
			@NonNull Map<String, Object> attributes) {
		if (request instanceof ServletServerHttpRequest servletRequest) {
			String uuid = servletRequest.getServletRequest().getParameter("uuid");

			if (uuid != null)
				attributes.put("uuid", uuid);
			else
				return false;
		}
		return true;
	}

	@Override
	public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler,
			Exception exception) {
		// No op
	}
}
