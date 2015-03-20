package org.pente.gameDatabase;

/**
 * @author dweebo
 */
public class SimpleGameStorerSearchResponseMoveData implements GameStorerSearchResponseMoveData {

    private int move;
    private int rotation;
    private int games;
    private int wins;

    public void setMove(int move) {
        this.move = move;
    }
    public int getMove() {
        return move;
    }

    public void setGames(int games) {
        this.games = games;
    }
    public int getGames() {
        return games;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }
    public int getWins() {
        return wins;
    }

    public double getPercentage() {
        return ((double) wins) / ((double) games);
    }

    public Object clone(GameStorerSearchResponseMoveData toFill) {

        toFill.setMove(getMove());
        toFill.setGames(getGames());
        toFill.setWins(getWins());

        return toFill;
    }
}