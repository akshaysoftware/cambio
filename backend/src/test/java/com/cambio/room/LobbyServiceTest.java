package com.cambio.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LobbyServiceTest {

  private final LobbyService service = new LobbyService();

  @Test
  void joiningUpdatesOnlyTheTargetRoom() {
    var first = service.create("a", "Akshay");
    var other = service.create("b", "Will");

    var joined = service.join("c", " " + first.code().toLowerCase() + " ", "Flo");

    assertThat(joined.players()).hasSize(2);
    assertThat(joined.hostId()).isEqualTo("a");
    assertThat(joined.version() > first.version()).isTrue();

    // Earlier snapshots remain unchanged.
    assertThat(first.players()).hasSize(1);
    assertThat(other.players()).hasSize(1);

    assertThat(service.join("d", other.code(), "Saige").players()).hasSize(2);
  }

  @Test
  void rejectsInvalidNamesMissingRoomsAndDuplicateMembership() {
    assertThrows(IllegalArgumentException.class, () -> service.create("a", "  "));

    assertThrows(IllegalArgumentException.class, () -> service.join("a", "XXXXXX", "Akshay"));

    var room = service.create("a", "Akshay");

    assertThrows(IllegalArgumentException.class, () -> service.create("a", "Again"));

    assertThrows(IllegalArgumentException.class, () -> service.join("a", room.code(), "Again"));
  }

  @Test
  void rejectsSeventhPlayerWithoutTakingTheirSeat() {
    var room = service.create("0", "Host");

    for (int i = 1; i < 6; i++) {
      service.join("" + i, room.code(), "Player " + i);
    }

    assertThrows(IllegalArgumentException.class, () -> service.join("6", room.code(), "Extra"));

    assertThat(service.create("6", "Extra").hostId()).isEqualTo("6");
  }

  @Test
  void disconnectTransfersHostAndLastDepartureDeletesRoom() {
    var room = service.create("a", "Akshay");
    service.join("b", room.code(), "Beth");

    var remaining = service.leave("a").orElseThrow();

    assertThat(remaining.hostId()).isEqualTo("b");
    assertThat(remaining.players()).hasSize(1);

    assertThat(service.leave("b")).isEmpty();

    assertThrows(IllegalArgumentException.class, () -> service.join("c", room.code(), "Rob"));
  }
}
