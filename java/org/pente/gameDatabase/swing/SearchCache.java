package org.pente.gameDatabase.swing;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Category;
import org.pente.game.GameData;
import org.pente.gameDatabase.GameStorerSearchRequestFilterData;
import org.pente.gameDatabase.GameStorerSearchResponseData;

/**
 * @author dweebo
 */
public class SearchCache {

    private static Category log4j = Category.getInstance(SearchCache.class.getName());

	private Map<GameStorerSearchRequestFilterData, Map<Long, GameStorerSearchResponseData>> searchCache =
		new HashMap<GameStorerSearchRequestFilterData, Map<Long, GameStorerSearchResponseData>>();

	private Map<Long, GameData> gameCache = new HashMap<Long, GameData>();

	public synchronized void addResults(GameStorerSearchRequestFilterData filter,
		GameStorerSearchResponseData response, long hash) {

		Map<Long, GameStorerSearchResponseData> c = searchCache.get(filter);
		if (c == null) {
			c = new HashMap<Long, GameStorerSearchResponseData>();
			searchCache.put((GameStorerSearchRequestFilterData) filter.clone(), c);
		}
		c.put(hash, response);
		for (int i = 0; i < response.getGames().size(); i++) {
			GameData d = (GameData) response.getGames().elementAt(i);
			if (getGame(d.getGameID()) == null) {
				addGame(d);
			}
		}
	}

	public synchronized GameStorerSearchResponseData getResults(GameStorerSearchRequestFilterData filter, long hash) {

		Map<Long, GameStorerSearchResponseData> c = searchCache.get(filter);
		if (c == null) {
			return null;
		}
		else {
			return c.get(hash);
		}
	}

	/*
	 *     cached search results may now be invalid
       if game existed before the change
          for i=0 to # old moves
            if old move isn't same as new move or new move doesn't exist (deleted)
              remove all cached search results with the same hash as old move AND new move
              and with filterdata that matches the game data
          for i to # new moves
            remove all cached search results with the same new hash
            and with filterdata that matches the game data
       else if new game
          for i=0 to # moves
            remove all cached search results with the same hash as move and
            with filterdata that matches the game data
	 */
	public synchronized void updateGame(PlunkGameData newGame, PlunkGameData oldGame) {
		if (oldGame == null) {
			PlunkNode p = newGame.getRoot();
			while (p != null) {
				clearCache(p.getHash());//for now don't match up with filterdata
				if (!p.hasChildren()) break;
				p = p.getChildren().get(0);
			}
		}
		else {
			PlunkNode op = oldGame.getRoot();
			PlunkNode np = newGame.getRoot();
			while (op != null) {
				if (np == null || op.getMove() != np.getMove()) {
					clearCache(op.getHash());//for now don't match up with filterdata
					if (np != null) {
						clearCache(np.getHash());
					}
				}
				if (!op.hasChildren()) break;
				op = op.getChildren().get(0);
				if (np != null && np.hasChildren()) {
					np = np.getChildren().get(0);
				}
			}
			while (np != null) {
				clearCache(np.getHash());//for now don't match up with filterdata
				if (!np.hasChildren()) break;
				np = np.getChildren().get(0);
			}
		}
	}
	private void clearCache(long hash) {
		for (Map<Long, GameStorerSearchResponseData> m : searchCache.values()) {
			GameStorerSearchResponseData r = m.get(hash);
			// don't clear games from pente.org
			if (r != null && r.getGameStorerSearchRequestData().getGameStorerSearchRequestFilterData().getDb() != 2) {
				log4j.debug("Clear cache " + hash);
				m.remove(hash);
			}
		}
	}

	public synchronized void addGame(GameData gd) {
		gameCache.put(gd.getGameID(), gd);
	}
	public synchronized GameData getGame(long gid) {
		return gameCache.get(gid);
	}
	public synchronized void deleteGame(GameData gd) {
		gameCache.remove(gd.getGameID());
		for (Map<Long, GameStorerSearchResponseData> m : searchCache.values()) {
			for (GameStorerSearchResponseData r : m.values()) {
				if (r.getGames().remove(gd)) {
					int total = r.getGameStorerSearchRequestData().getGameStorerSearchRequestFilterData().getTotalGameNum();
					r.getGameStorerSearchRequestData().getGameStorerSearchRequestFilterData().setTotalGameNum(total - 1);
				}
			}
		}
	}
}
