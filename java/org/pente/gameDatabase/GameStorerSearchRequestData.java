package org.pente.gameDatabase;

import org.pente.game.*;

/**
 * @author dweebo
 */
public interface GameStorerSearchRequestData extends MoveData {

    public void setGameStorerSearchResponseFormat(String format);
    public String getGameStorerSearchResponseFormat();

    public void setGameStorerSearchResponseParams(String params);
    public String getGameStorerSearchResponseParams();

    public void setGameStorerSearchResponseOrder(int order);
    public int getGameStorerSearchResponseOrder();

    public void setGameStorerSearchRequestFilterData(GameStorerSearchRequestFilterData filterData);
    public GameStorerSearchRequestFilterData getGameStorerSearchRequestFilterData();
}