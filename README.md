# Cambio

A small multiplayer Cambio app: Java 25, Spring Boot 4, React and TypeScript.

## Design

One backend process owns all rooms. React sends commands over native WebSockets.
Game rules belong in Java; future card views must be filtered per player.
Rooms are ephemeral and disappear when the backend restarts.

House rules and unresolved gameplay details are in RULES.md.