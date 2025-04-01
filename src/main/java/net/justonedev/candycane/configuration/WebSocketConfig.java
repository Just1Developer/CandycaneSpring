/* (C)2025 */
package net.justonedev.candycane.configuration;

import net.justonedev.candycane.websockets.SocketSessionHandler;
import net.justonedev.candycane.websockets.SocketSessionHandlerOld;
import net.justonedev.candycane.websockets.WebSocketInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class configures WebSocket support for the application. Registers a
 * WebSocket handler and manages a socket session handler.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
	private final Map<String, Set<WebSocketSession>> userSockets = new ConcurrentHashMap<>();

	/**
	 * Creates a handler to manage WebSocket sessions.
	 *
	 * @return the {@link SocketSessionHandlerOld} instance
	 */
	@Bean
	public SocketSessionHandlerOld socketSessionHandlerOld() {
		return new SocketSessionHandlerOld(userSockets);
	}

	/**
	 * Creates a handler to manage WebSocket sessions.
	 *
	 * @return the {@link SocketSessionHandlerOld} instance
	 */
	@Bean
	public SocketSessionHandler socketSessionHandler() {
		return new SocketSessionHandler();
	}

	/**
	 * Registers WebSocket handlers
	 * @param registry the {@link WebSocketHandlerRegistry} to configure
	 */
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(socketSessionHandler(), "/ws").addInterceptors(new WebSocketInterceptor()).setAllowedOrigins("*");
	}
}
