package org.pente.gameDatabase;

import java.util.*;

/**
 * @author dweebo
 */
public interface GameStorerSearchRequestFilterData extends Cloneable {

    public void setStartGameNum(int num);
    public int getStartGameNum();

    public void setEndGameNum(int num);
    public int getEndGameNum();

    public int getNumGames();

    public void setTotalGameNum(int num);
    public int getTotalGameNum();


    public void setPlayer1Name(String name);
    public String getPlayer1Name();

    public void setPlayer2Name(String name);
    public String getPlayer2Name();

    public static int SEAT_ALL = 0;
    public static int SEAT_1 = 1;
    public static int SEAT_2 = 2;
    public void setPlayer1Seat(int seat);
    public int getPlayer1Seat();

    public void setPlayer2Seat(int seat);
    public int getPlayer2Seat();

    public void setDb(int dbid);
    public int getDb();

    public void setGame(int gameNum);
    public int getGame();

    public void setSite(String site);
    public String getSite();

    public void setEvent(String event);
    public String getEvent();

    public void setRound(String round);
    public String getRound();

    public void setSection(String section);
    public String getSection();


    public void setAfterDate(Date date);
    public Date getAfterDate();

    public void setBeforeDate(Date date);
    public Date getBeforeDate();

    public void setWinner(int winner);
    public int getWinner();

    public void setGetNextMoves(boolean nextMoves);
    public boolean doGetNextMoves();

    public Object clone();
}