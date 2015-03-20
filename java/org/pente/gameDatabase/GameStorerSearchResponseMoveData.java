package org.pente.gameDatabase;

/**
 * @author dweebo
 */
public interface GameStorerSearchResponseMoveData {

    public void setMove(int move);
    public int getMove();

    public void setGames(int games);
    public int getGames();

    public void setWins(int wins);
    public int getWins();

    public double getPercentage();

    public Object clone(GameStorerSearchResponseMoveData toFill);
}