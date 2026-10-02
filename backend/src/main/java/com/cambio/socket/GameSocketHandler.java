package com.cambio.socket;

import com.cambio.room.LobbyService;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class GameSocketHandler extends TextWebSocketHandler {

  public record Command(String type, String name, String code) {}

  private final LobbyService lobbies;
  private final JsonMapper json;

  private final Map<String, WebSocketSession> connections = new ConcurrentHashMap<>();

  public GameSocketHandler(LobbyService lobbies, JsonMapper json) {
    this.lobbies = lobbies;
    this.json = json;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    session.setTextMessageSizeLimit(4096);

    connections.put(session.getId(), new ConcurrentWebSocketSessionDecorator(session, 5000, 65536));
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    LobbyService.Lobby lobby;

    // A close callback must not remove membership halfway through joining.
    synchronized (session) {
      if (!session.isOpen() || !connections.containsKey(session.getId())) {
        return;
      }

      try {
        Command command = json.readValue(message.getPayload(), Command.class);

        if (command == null || command.type() == null) {
          throw new IllegalArgumentException("Missing command type.");
        }

        lobby =
            switch (command.type()) {
              case "CREATE_ROOM" -> lobbies.create(session.getId(), command.name());

              case "JOIN_ROOM" -> lobbies.join(session.getId(), command.code(), command.name());

              default -> throw new IllegalArgumentException("Unknown command.");
            };
      } catch (JacksonException exception) {
        send(
            session.getId(),
            Map.of(
                "type", "ERROR",
                "message", "Invalid message."));
        return;
      } catch (IllegalArgumentException exception) {
        send(session.getId(), Map.of("type", "ERROR", "message", exception.getMessage()));
        return;
      }
    }

    broadcast(lobby);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    Optional<LobbyService.Lobby> remaining;

    synchronized (session) {
      connections.remove(session.getId());
      remaining = lobbies.leave(session.getId());
    }

    remaining.ifPresent(this::broadcast);
  }

  private void broadcast(LobbyService.Lobby lobby) {
    for (LobbyService.Player player : lobby.players()) {
      send(player.id(), Map.of("type", "LOBBY", "you", player.id(), "lobby", lobby));
    }
  }

  private void send(String playerId, Object payload) {
    WebSocketSession target = connections.get(playerId);

    if (target == null || !target.isOpen()) {
      return;
    }

    try {
      target.sendMessage(new TextMessage(json.writeValueAsString(payload)));
    } catch (IOException | IllegalStateException exception) {
      try {
        target.close(CloseStatus.SERVER_ERROR);
      } catch (IOException ignored) {
        // Close callback handles membership cleanup.
      }
    }
  }
}
