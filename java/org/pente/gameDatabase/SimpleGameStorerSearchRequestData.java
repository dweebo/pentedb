package org.pente.gameDatabase;

import org.pente.game.*;

/**
 * @author dweebo
 */
public class SimpleGameStorerSearchRequestData implements GameStorerSearchRequestData {

    protected GameData                          gameData;
    protected GameStorerSearchRequestFilterData filterData;
    protected String                            outFormat;
    protected int                               responseOrder;
    protected String                            responseParams;

    public SimpleGameStorerSearchRequestData() {
        gameData = new DefaultGameData();
    }

    public SimpleGameStorerSearchRequestData(MoveData moveData) {
        this();

        for (int i = 0; i < moveData.getNumMoves(); i++) {
            addMove(moveData.getMove(i));
        }
    }

    public SimpleGameStorerSearchRequestData(GameData gameData) {
        this((MoveData) gameData);

        filterData = new SimpleGameStorerSearchRequestFilterData();
        filterData.setSite(gameData.getSite());
        filterData.setEvent(gameData.getEvent());
        filterData.setRound(gameData.getRound());
        filterData.setSection(gameData.getSection());
        filterData.setPlayer1Name(gameData.getPlayer1Data().getUserIDName());
        filterData.setPlayer2Name(gameData.getPlayer2Data().getUserIDName());
        filterData.setWinner(gameData.getWinner());
    }

    public void setGameStorerSearchResponseFormat(String format) {
        this.outFormat = format;
    }
    public String getGameStorerSearchResponseFormat() {
        return outFormat;
    }

    public void setGameStorerSearchResponseParams(String params) {
        responseParams = params;
    }
    public String getGameStorerSearchResponseParams() {
        return responseParams;
    }

    public void setGameStorerSearchResponseOrder(int order) {
        this.responseOrder = order;
    }
    public int getGameStorerSearchResponseOrder() {
        return responseOrder;
    }

    public void setGameStorerSearchRequestFilterData(GameStorerSearchRequestFilterData filterData) {
        this.filterData = filterData;
    }
    public GameStorerSearchRequestFilterData getGameStorerSearchRequestFilterData() {
        return filterData;
    }

    public void addMove(int move) {
        gameData.addMove(move);
    }
    public void undoMove() {
        gameData.undoMove();
    }
    public int getMove(int num) {
        return gameData.getMove(num);
    }
    public int getNumMoves() {
        return gameData.getNumMoves();
    }
    public int[] getMoves() {
        return gameData.getMoves();
    }
}