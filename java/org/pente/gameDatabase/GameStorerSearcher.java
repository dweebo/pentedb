package org.pente.gameDatabase;

/**
 * @author dweebo
 */
public interface GameStorerSearcher {
    public void search(GameStorerSearchRequestData data, GameStorerSearchResponseData responseData) throws Exception;
}