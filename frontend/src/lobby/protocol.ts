export type Lobby = {
  code: string;
  version: number;
  hostId: string;
  players: {
    id: string;
    name: string;
  }[];
};

export type ClientMessage =
  | {
      type: "CREATE_ROOM";
      name: string;
    }
  | {
      type: "JOIN_ROOM";
      name: string;
      code: string;
    };

export type ServerMessage =
  | {
      type: "LOBBY";
      you: string;
      lobby: Lobby;
    }
  | {
      type: "ERROR";
      message: string;
    };