export const VIEWERS = {
  P1: "P1",
  P2: "P2",
};

// state of each cell in the grid
export const CELL_STATES = {
  EMPTY: "EMPTY",
  SHIP: "SHIP",
  HIT: "HIT",
  MISS: "MISS",
  SUNK: "SUNK",
};

// game status
export const GAME_STATES = {
  PLACING_P1: "PLACING_P1",
  PLACING_P2: "PLACING_P2",
  TURN_P1: "TURN_P1",
  TURN_P2: "TURN_P2",
  FINISHED: "FINISHED",
};

// type of the ships
export const SHIP_TYPES = {
  CARRIER: "CARRIER",
  BATTLESHIP: "BATTLESHIP",
  CRUISER: "CRUISER",
  SUBMARINE: "SUBMARINE",
  DESTROYER: "DESTROYER",
};
