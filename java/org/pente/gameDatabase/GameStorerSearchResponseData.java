package org.pente.gameDatabase;

import java.util.*;

import org.pente.game.*;

/**
 * @author dweebo
 */
public interface GameStorerSearchResponseData {

    public void setGameStorerSearchRequestData(GameStorerSearchRequestData requestData);
    public GameStorerSearchRequestData getGameStorerSearchRequestData();

    public void addSearchResponseMoveData(GameStorerSearchResponseMoveData data);
    public GameStorerSearchResponseMoveData getSearchResponseMoveData(int move);
    public Vector searchResponseMoveData();
    public int getNumSearchResponseMoves();

    public void setRotation(int rotation);
    public int getRotation();

    public void addGame(GameData data);
    public Vector getGames();

    public boolean containsGame(GameData d);
}