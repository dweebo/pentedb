package org.pente.gameDatabase;

import java.util.*;
import org.pente.game.*;

/**
 * @author dweebo
 */
public class SimpleGameStorerSearchResponseData implements GameStorerSearchResponseData {

    protected GameStorerSearchRequestData   requestData;
    protected Vector                        searchResultMoves;
    protected Vector                        matchedGames;
    protected int rotation;

    public SimpleGameStorerSearchResponseData() {
        searchResultMoves = new Vector();
        matchedGames = new Vector();
    }

    public void setRotation(int rotation) {
    	this.rotation = rotation;
    }
    public int getRotation() {
    	return rotation;
    }

    public void setGameStorerSearchRequestData(GameStorerSearchRequestData requestData) {
        this.requestData = requestData;
    }
    public GameStorerSearchRequestData getGameStorerSearchRequestData() {
        return requestData;
    }

    public void addSearchResponseMoveData(GameStorerSearchResponseMoveData data) {
        if (!searchResultMoves.contains(data)) {
            searchResultMoves.addElement(data);
        }
    }
    public GameStorerSearchResponseMoveData getSearchResponseMoveData(int move) {

        for (int i = 0; i < searchResultMoves.size(); i++) {
            GameStorerSearchResponseMoveData moveData = (GameStorerSearchResponseMoveData) searchResultMoves.elementAt(i);
            if (moveData.getMove() == move) {
                return moveData;
            }
        }

        return null;
    }

    public Vector searchResponseMoveData() {
        return searchResultMoves;
    }

    public int getNumSearchResponseMoves() {
        return searchResultMoves.size();
    }

    public void addGame(GameData data) {
        matchedGames.addElement(data);
    }

    public Vector getGames() {
       return matchedGames;
    }
    public boolean containsGame(GameData d) {
    	for (int i = 0; i < matchedGames.size(); i++) {
    		GameData dd = (GameData)  matchedGames.elementAt(i);
    		if (dd.getGameID() == d.getGameID()) return true;
    	}
    	return false;
    }
}