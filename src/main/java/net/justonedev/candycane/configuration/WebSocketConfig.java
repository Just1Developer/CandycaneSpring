/* (C)2025 */
package net.justonedev.candycane.configuration;

import net.justonedev.candycane.websockets.SocketSessionHandler;
import net.justonedev.candycane.websockets.WebSocketInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * This class configures WebSocket support for the application. Registers a
 * WebSocket handler and manages a socket session handler.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	/**
	 * Creates a handler to manage WebSocket sessions.
	 *
	 * @return the {@link SocketSessionHandler} instance
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
