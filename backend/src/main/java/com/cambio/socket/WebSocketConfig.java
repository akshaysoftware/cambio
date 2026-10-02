package com.cambio.socket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  private final GameSocketHandler handler;
  private final String allowedOrigin;

  public WebSocketConfig(
      GameSocketHandler handler, @Value("${cambio.allowed-origin}") String allowedOrigin) {
    this.handler = handler;
    this.allowedOrigin = allowedOrigin;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(handler, "/ws").setAllowedOrigins(allowedOrigin);
  }
}
