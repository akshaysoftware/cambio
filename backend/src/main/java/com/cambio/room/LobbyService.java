package com.cambio.room;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class LobbyService {

  public record Player(String id, String name) {}

  public record Lobby(String code, long version, String hostId, List<Player> players) {}

  private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ123456789";

  private final SecureRandom random = new SecureRandom();
  private final Map<String, Room> rooms = new HashMap<>();
  private final Map<String, String> memberships = new HashMap<>();

  private static final class Room {
    final String code;
    final LinkedHashMap<String, Player> players = new LinkedHashMap<>();
    long version;

    Room(String code) {
      this.code = code;
    }

    Lobby snapshot() {
      return new Lobby(
          code, version, players.keySet().iterator().next(), List.copyOf(players.values()));
    }
  }

  // One short lock protects lobby mutations. No network I/O happens here.
  public synchronized Lobby create(String playerId, String name) {
    requireUnseated(playerId);
    name = validName(name);

    if (rooms.size() >= 200) {
      throw new IllegalArgumentException("Too many rooms. Try later.");
    }

    String code;
    do {
      StringBuilder value = new StringBuilder();
      for (int i = 0; i < 6; i++) {
        value.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
      }
      code = value.toString();
    } while (rooms.containsKey(code));

    Room room = new Room(code);
    rooms.put(code, room);

    return seat(room, playerId, name);
  }

  public synchronized Lobby join(String playerId, String code, String name) {
    requireUnseated(playerId);
    name = validName(name);
    code = code == null ? "" : code.strip().toUpperCase(Locale.ROOT);

    Room room = rooms.get(code);

    if (room == null) {
      throw new IllegalArgumentException("Room not found.");
    }

    if (room.players.size() >= 6) {
      throw new IllegalArgumentException("Room is full.");
    }

    return seat(room, playerId, name);
  }

  public synchronized Optional<Lobby> leave(String playerId) {
    String code = memberships.remove(playerId);

    if (code == null) {
      return Optional.empty();
    }

    Room room = rooms.get(code);
    room.players.remove(playerId);
    room.version++;

    if (room.players.isEmpty()) {
      rooms.remove(code);
      return Optional.empty();
    }

    return Optional.of(room.snapshot());
  }

  private Lobby seat(Room room, String playerId, String name) {
    room.players.put(playerId, new Player(playerId, name));
    memberships.put(playerId, room.code);
    room.version++;

    return room.snapshot();
  }

  private void requireUnseated(String playerId) {
    if (memberships.containsKey(playerId)) {
      throw new IllegalArgumentException("Leave your current room first.");
    }
  }

  private String validName(String name) {
    String value = name == null ? "" : name.strip();

    if (value.isEmpty()
        || value.length() > 24
        || value.codePoints().anyMatch(Character::isISOControl)) {
      throw new IllegalArgumentException("Use a name between 1 and 24 characters.");
    }

    return value;
  }
}
