import { useState } from "react";
import { useLobby } from "./lobby/useLobby";

export default function App() {
  const [name, setName] = useState("");
  const [code, setCode] = useState("");
  const [copyStatus, setCopyStatus] = useState("");

  const {
    status,
    joined,
    busy,
    error,
    send,
    reset,
  } = useLobby();

  const disabled =
    status !== "open" || busy || !name.trim();

  async function copyCode() {
    if (!joined) return;

    try {
      await navigator.clipboard.writeText(joined.lobby.code);
      setCopyStatus("Copied");
    } catch {
      setCopyStatus("Select the room code to copy it.");
    }
  }

  return (
    <main>
      <header>
        <span className="eyebrow">
          A table for your friends
        </span>

        <h1>
          Cambio<span>.</span>
        </h1>

        <p className="connection" role="status">
          {status === "open"
            ? "Connected"
            : status === "connecting"
              ? "Connecting…"
              : "Offline"}
        </p>
      </header>

      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}

      {joined ? (
        <section className="panel" aria-label="Room lobby">
          <div className="room-heading">
            <div>
              <span className="eyebrow">Room code</span>
              <h2 className="code">{joined.lobby.code}</h2>
            </div>

            <button
              className="secondary"
              onClick={copyCode}
            >
              Copy code
            </button>
          </div>

          <p role="status">
            {copyStatus || "Share this code with your friends."}
          </p>

          <h3>
            Players{" "}
            <span className="muted">
              {joined.lobby.players.length}/6
            </span>
          </h3>

          <ul aria-live="polite">
            {joined.lobby.players.map((player) => (
              <li key={player.id}>
                <span>
                  {player.name}
                  {player.id === joined.you ? " (you)" : ""}
                </span>

                {player.id === joined.lobby.hostId && (
                  <span className="badge">Host</span>
                )}
              </li>
            ))}
          </ul>

          <p className="muted">
            The lobby is ready. Gameplay is coming next.
          </p>

          <button
            className="secondary full"
            onClick={() => {
              setCopyStatus("");
              reset();
            }}
          >
            Leave room
          </button>
        </section>
      ) : (
        <form
          className="panel"
          onSubmit={(event) => {
            event.preventDefault();

            send({
              type: "JOIN_ROOM",
              name: name.trim(),
              code: code.trim().toUpperCase(),
            });
          }}
        >
          <label htmlFor="name">Your name</label>

          <input
            id="name"
            autoComplete="nickname"
            maxLength={24}
            required
            value={name}
            onChange={(event) => setName(event.target.value)}
          />

          <button
            className="full"
            type="button"
            disabled={disabled}
            onClick={() =>
              send({
                type: "CREATE_ROOM",
                name: name.trim(),
              })
            }
          >
            Create room
          </button>

          <div className="divider">or join your friends</div>

          <label htmlFor="code">Room code</label>

          <input
            id="code"
            className="code-input"
            autoComplete="off"
            spellCheck={false}
            minLength={6}
            maxLength={6}
            required
            value={code}
            onChange={(event) =>
              setCode(event.target.value.toUpperCase())
            }
          />

          <button
            className="secondary full"
            disabled={disabled || code.trim().length !== 6}
          >
            Join room
          </button>
        </form>
      )}

      {status === "closed" && (
        <button className="full" onClick={reset}>
          Connect again
        </button>
      )}

      <footer>
        No account needed. Just bring your friends.
      </footer>
    </main>
  );
}