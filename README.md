# BattleShipGame

## About The Game
Battleship is a turn-based strategy game for two players.  
Each player secretly places a fleet of ships on their own 10×10 grid. The goal is to be the first to sink all of the opponent’s ships.

### Game Rules

**Board**
  -Each player has a 10 × 10 grid.

**Fleet**
  - Carrier – length 5  
  - Battleship – length 4  
  - Cruiser – length 3  
  - Submarine – length 3  
  - Destroyer – length 2  

**Placing ships**
  - Ships can be placed horizontally or vertically.
  - Ships cannot overlap.

**Taking turns**
  - Players take turns choosing one coordinate on the opponent’s board to fire at.
  - The result of a shot is:
    - **Hit** – the shot hits part of a ship.
    - **Miss** – the shot hits water.
    - **Sunk** – the last remaining cell of a ship is hit.
  - The game continues with players alternating turns.

- **Winning**
  - A player wins when all parts of all enemy ships have been hit (the entire fleet is sunk).


------------------------------


## Tech Stack 

**Backend**

  - Java 
  - Spring Boot
  - Maven
  - JUnit tests


**Frontend**

  - React  
  - Vite  
  - JavaScript  
  - Bootstrap 5 
  - HTML/CSS

------------------------------

## How to Run the Game