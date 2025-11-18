package com.example.battleship.domain;


public class Game {
    private final String id;
    private final Board p1Board = new Board();
    private final Board p2Board = new Board();
    private GameState state = GameState.PLACING_P1;
    private Player winner = null;
    private String p1Name = "Player 1";
    private String p2Name = "Player 2";

    public Game(String id) { this.id = id; }

    public String getId() { return id; }
    public Board getBoard(Player p) { return p == Player.P1 ? p1Board : p2Board; }
    public Board getOpponentBoard(Player p) { return p == Player.P1 ? p2Board : p1Board; }
    public GameState getState() { return state; }
    public void setState(GameState s) { state = s; }
    public Player getWinner() { return winner; }
    public void setWinner(Player w) { winner = w; }

    public String getPlayerName(Player p) { return p == Player.P1 ? p1Name : p2Name; }
    public void setPlayerName(Player p, String name) { if (p==Player.P1) p1Name=name; else p2Name=name; }
}
