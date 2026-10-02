import { useEffect, useRef, useState } from "react";
import type { ClientMessage, ServerMessage } from "./protocol";

type Joined = Extract<ServerMessage, { type: "LOBBY" }>;

export function useLobby() {
  const socketRef = useRef<WebSocket | null>(null);
  const pendingRef = useRef(false);

  const [attempt, setAttempt] = useState(0);
  const [status, setStatus] = useState<
    "connecting" | "open" | "closed"
  >("connecting");

  const [joined, setJoined] = useState<Joined | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;

    setStatus("connecting");
    setJoined(null);
    setError(null);
    setBusy(false);
    pendingRef.current = false;

    const scheme = location.protocol === "https:" ? "wss" : "ws";
    const socket = new WebSocket(
      `${scheme}://${location.host}/ws`,
    );

    socketRef.current = socket;

    socket.onopen = () => {
      if (active) setStatus("open");
    };

    socket.onmessage = (event) => {
      if (!active) return;

      pendingRef.current = false;
      setBusy(false);

      try {
        const message = JSON.parse(event.data) as ServerMessage;

        if (message.type === "ERROR") {
          setError(message.message);
        } else if (message.type === "LOBBY") {
          setJoined((previous) =>
            previous?.lobby.code === message.lobby.code &&
            previous.lobby.version >= message.lobby.version
              ? previous
              : message,
          );
          setError(null);
        } else {
          setError("Unexpected server response.");
        }
      } catch {
        setError("Could not read the server response.");
      }
    };

    socket.onerror = () => {
      if (active) setError("Could not connect to the server.");
    };

    socket.onclose = () => {
      if (!active) return;

      pendingRef.current = false;
      setBusy(false);
      setStatus("closed");
      setJoined(null);
      setError(
        "Disconnected. Connect again to create or join a room.",
      );
    };

    return () => {
      active = false;
      socketRef.current = null;
      socket.close();
    };
  }, [attempt]);

  function send(message: ClientMessage) {
    const socket = socketRef.current;

    if (pendingRef.current) return;

    if (!socket || socket.readyState !== WebSocket.OPEN) {
      setError("Wait until you are connected.");
      return;
    }

    try {
      socket.send(JSON.stringify(message));
      pendingRef.current = true;
      setBusy(true);
      setError(null);
    } catch {
      setError("Could not send your request. Try again.");
    }
  }

  function reset() {
    socketRef.current?.close();
    setJoined(null);
    setStatus("connecting");
    setAttempt((value) => value + 1);
  }

  return {
    status,
    joined,
    busy,
    error,
    send,
    reset,
  };
}