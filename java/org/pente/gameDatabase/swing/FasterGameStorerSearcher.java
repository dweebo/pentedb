package org.pente.gameDatabase.swing;

import java.sql.*;
import java.util.*;

import org.pente.database.*;
import org.pente.game.*;
import org.pente.gameDatabase.GameStorerSearchRequestData;
import org.pente.gameDatabase.GameStorerSearchRequestFilterData;
import org.pente.gameDatabase.GameStorerSearchResponseData;
import org.pente.gameDatabase.GameStorerSearchResponseMoveData;
import org.pente.gameDatabase.GameStorerSearcher;
import org.pente.gameDatabase.SimpleGameStorerSearchResponseMoveData;

import org.apache.log4j.*;

/**
 * @author dweebo
 */
public class FasterGameStorerSearcher implements GameStorerSearcher {

	private static final Category log4j = Category.getInstance(
		FasterGameStorerSearcher.class.getName());

    private DBHandler           dbHandler;
    private PlunkGameVenueStorer		gameVenueStorer;
    private boolean				hsql;
    private boolean				derby;

    private String game_table = "pente_game";
    private String move_table = "pente_move";

    public FasterGameStorerSearcher(DBHandler dbHandler,
    		PlunkGameVenueStorer gameVenueStorer) throws Exception {

        this.dbHandler = dbHandler;
        this.gameVenueStorer = gameVenueStorer;

        /*
        // attempt to cache the pente_move, pente_game pages
        long startTime = System.currentTimeMillis();
        Connection con = null;
        try {
        	con = dbHandler.getConnection();
        	Statement s = con.createStatement();
        	ResultSet r = s.executeQuery("select m.next_move, m.hash_key, m.move_num, m.game, m.play_date " +
        			  "from pente_move m");
        	while (r.next()) {}
        	r.close();

        	r = s.executeQuery("select g.gid, g.player1_pid from pente_game g");
        	while (r.next()) {}
        	r.close();


        	r = s.executeQuery("select p.pid, p.name from player p");
        	while (r.next()) {}
        	r.close();
        	s.close();
        }
        finally {
        	dbHandler.freeConnection(con);
        }
        log4j.debug("cached pente_move in " + (System.currentTimeMillis() - startTime) + " millis.");
        */
    }

    public void setHsql(boolean hsql) {
    	this.hsql = hsql;
    }
    public void setDerby(boolean derby) {
    	this.derby = derby;
    }


    public void search(GameStorerSearchRequestData requestData, GameStorerSearchResponseData responseData) throws Exception {
long startTime = System.currentTimeMillis();
        Connection con = null;

        responseData.setGameStorerSearchRequestData(requestData);

        // setup filter sql lines
        StringBuffer filterOptionsFrom = new StringBuffer();
        StringBuffer filterOptionsWhere = new StringBuffer();
        Vector filterOptionsParams = new Vector();
        StringBuffer filterOptionsFrom2 = new StringBuffer();
        StringBuffer filterOptionsWhere2 = new StringBuffer();
        Vector filterOptionsParams2 = new Vector();

        GameStorerSearchRequestFilterData filterData = requestData.getGameStorerSearchRequestFilterData();

        //call initFilterOptions 2 times if p1 seat is all or p2 seat is all
        // one filteroptionsFrom,where,params for each subquery
        boolean includeGameTable = initFilterOptions(
        	filterData, 1, filterOptionsFrom, filterOptionsWhere, filterOptionsParams);

        boolean union = false;
        if ((filterData.getPlayer1Name() != null &&
        	 filterData.getPlayer1Name().trim().length() > 0 &&
        	 filterData.getPlayer1Seat() == filterData.SEAT_ALL) ||
        	(filterData.getPlayer2Name() != null &&
             filterData.getPlayer2Name().trim().length() > 0 &&
        	 filterData.getPlayer2Seat() == filterData.SEAT_ALL)) {
        	union = true;
        	includeGameTable = true;
        	initFilterOptions(
                filterData, 2, filterOptionsFrom2, filterOptionsWhere2, filterOptionsParams2);
        }

        try {
            con = dbHandler.getConnection();

            int totalGameCount = 0;
            if (requestData.getGameStorerSearchRequestFilterData().doGetNextMoves()) {
	            totalGameCount = getSearchResults(requestData,
					responseData, filterOptionsFrom, includeGameTable,
					filterOptionsWhere, filterOptionsParams,
					filterOptionsFrom2, filterOptionsWhere2, filterOptionsParams2, union, con);
            }

            getMatchingGames(requestData, responseData, totalGameCount,
				filterOptionsFrom, includeGameTable, filterOptionsWhere,
				filterOptionsParams, filterOptionsFrom2, filterOptionsWhere2, filterOptionsParams2, union, con);

        } finally {
            dbHandler.freeConnection(con);
        }
long endTime = System.currentTimeMillis();
long totalTime = endTime - startTime;
log4j.debug("search time: " + totalTime);
    }

	private void addGameTable(StringBuffer filterOptionsFrom,
		StringBuffer filterOptionsWhere) {

		filterOptionsFrom.append(", " + game_table + " g ");
		filterOptionsWhere.append("and m.gid = g.gid ");
	}

    protected boolean initFilterOptions(GameStorerSearchRequestFilterData filterData,
    								 int unionIndex,
                                     StringBuffer filterOptionsFrom,
                                     StringBuffer filterOptionsWhere,
                                     Vector filterOptionsParams) throws Exception {

		boolean includeGameTable = false;
		GameSiteData siteData = null;
        if (filterData.getSite() != null && filterData.getSite().trim().length() > 0) {

            siteData = gameVenueStorer.getGameSiteData(
                filterData.getGame(), filterData.getSite());
            if (siteData != null) {
				addGameTable(filterOptionsFrom, filterOptionsWhere);
				includeGameTable = true;

				//TESTING REPLACING PARAMS WITH HARD-CODED
                //filterOptionsWhere.append("and g.site_id = ? ");
                //filterOptionsParams.addElement(new Integer(siteData.getSiteID()));

				filterOptionsWhere.append("and g.site_id = " + siteData.getSiteID() + " ");
            }
        }
        if (filterData.getEvent() != null && filterData.getEvent().trim().length() > 0 &&
            !filterData.getEvent().equals(GameEventData.ALL_EVENTS) && !filterData.getEvent().equals("-")) {

			if (!includeGameTable) {
				addGameTable(filterOptionsFrom, filterOptionsWhere);
				includeGameTable = true;
			}

            //filterOptionsWhere.append(" and g.event_id = ? ");
			GameEventData e = gameVenueStorer.getGameEventData(filterData.getGame(),
				filterData.getEvent(), filterData.getSite());
            //filterOptionsParams.addElement(e.getEventID());
            filterOptionsWhere.append(" and g.event_id = " + e.getEventID() + " ");
        }
        if (filterData.getRound() != null && filterData.getRound().trim().length() > 0 &&
            !filterData.getRound().equals(GameRoundData.ALL_ROUNDS) && !filterData.getRound().equals("-")) {

			if (!includeGameTable) {
				addGameTable(filterOptionsFrom, filterOptionsWhere);
				includeGameTable = true;
			}
            filterOptionsWhere.append("and g.round = '" + filterData.getRound() + "' ");
            //filterOptionsParams.addElement(filterData.getRound());
        }
        if (filterData.getSection() != null && filterData.getSection().trim().length() > 0 &&
            !filterData.getSection().equals(GameSectionData.ALL_SECTIONS) && !filterData.getSection().equals("-")) {

			if (!includeGameTable) {
				addGameTable(filterOptionsFrom, filterOptionsWhere);
				includeGameTable = true;
			}
            filterOptionsWhere.append("and g.section = '" + filterData.getSection() + "' ");
            //filterOptionsParams.addElement(filterData.getSection());
        }


        //player possibilities
        //p1 name and all
        //  p2 blank
        //  p2 name and all
        //  p2 name and 1 or p2 name and 2 will not be allowed?
        //p1 name and 1
        //  p2 blank
        //  p2 name and 2
        //p1 name and 2
        //  p2 blank
        //  p2 name and 1

        if (filterData.getPlayer1Name() != null && filterData.getPlayer1Name().trim().length() > 0) {
            filterOptionsFrom.append(", player p1 ");

			if (!includeGameTable) {
				addGameTable(filterOptionsFrom, filterOptionsWhere);
				includeGameTable = true;
			}
			if (filterData.getPlayer1Seat() == GameStorerSearchRequestFilterData.SEAT_ALL) {
				filterOptionsWhere.append("and g.player" + unionIndex + "_pid = p1.pid ");
			}
			else {
				filterOptionsWhere.append("and g.player" + filterData.getPlayer1Seat() + "_pid = p1.pid ");
			}

			if (siteData != null) {
				filterOptionsWhere.append("and p1.site_id = " + siteData.getSiteID() + " ");
			}
			filterOptionsWhere.append("and p1.name_lower = ?  ");
			//filterOptionsWhere.append("and p1.name_lower = '" +
			//	filterData.getPlayer1Name().toLowerCase() + "' ");

            filterOptionsParams.addElement(filterData.getPlayer1Name().toLowerCase());
        }
        if (filterData.getPlayer2Name() != null &&
			filterData.getPlayer2Name().trim().length() > 0) {
            filterOptionsFrom.append(", player p2 ");

			if (!includeGameTable) {
				addGameTable(filterOptionsFrom, filterOptionsWhere);
				includeGameTable = true;
			}
			if (filterData.getPlayer2Seat() == GameStorerSearchRequestFilterData.SEAT_ALL) {
				filterOptionsWhere.append("and g.player" + (3 - unionIndex) + "_pid = p2.pid ");
			}
			else {
				filterOptionsWhere.append("and g.player" + filterData.getPlayer2Seat() + "_pid = p2.pid ");
			}

			if (siteData != null) {
				filterOptionsWhere.append("and p2.site_id = " + siteData.getSiteID() + " ");
			}

            filterOptionsWhere.append("and p2.name_lower = ? ");
            //filterOptionsWhere.append("and p2.name_lower = '" +
           	//	filterData.getPlayer2Name().toLowerCase() + "' ");

            filterOptionsParams.addElement(filterData.getPlayer2Name().toLowerCase());
        }

        if (filterData.getAfterDate() != null) {
			if (!includeGameTable) {
				addGameTable(filterOptionsFrom, filterOptionsWhere);
				includeGameTable = true;
			}
			//TODO make this a string
            filterOptionsWhere.append("and g.play_date > ? ");
            filterOptionsParams.addElement(new Timestamp(filterData.getAfterDate().getTime()));
        }
        if (filterData.getBeforeDate() != null) {
			if (!includeGameTable) {
				addGameTable(filterOptionsFrom, filterOptionsWhere);
				includeGameTable = true;
			}
			//TODO make this a string
            filterOptionsWhere.append("and g.play_date < ? ");
            filterOptionsParams.addElement(new Timestamp(filterData.getBeforeDate().getTime()));
        }

        if (filterData.getWinner() != GameData.UNKNOWN) {
            filterOptionsWhere.append("and m.winner = " + new Integer(filterData.getWinner()) + " ");
            //filterOptionsParams.addElement(new Integer(filterData.getWinner()));
        }
		return includeGameTable;
    }

    protected void setFilterOptionsParams(PreparedStatement stmt, Vector filterOptionsParams, int startParam) throws SQLException {

        for (int i = 0; i < filterOptionsParams.size(); i++) {
            Object param = filterOptionsParams.elementAt(i);

            if (param instanceof Integer) {
                Integer p = (Integer) param;
                stmt.setInt(startParam + i, p.intValue());
            }
            else if (param instanceof Timestamp) {
                Timestamp t = (Timestamp) param;
                stmt.setTimestamp(startParam + i, t);
            }
            else {
                stmt.setString(startParam + i, param.toString());
            }
        }
    }

    protected int getSearchResults(GameStorerSearchRequestData requestData,
                                    GameStorerSearchResponseData responseData,
                                    StringBuffer filterOptionsFrom,
                                    boolean includeGameTable,
                                    StringBuffer filterOptionsWhere,
                                    Vector filterOptionsParams,
                                    StringBuffer filterOptionsFrom2,
                                    StringBuffer filterOptionsWhere2,
                                    Vector filterOptionsParams2,
                                    boolean union,
                                    Connection con) throws Exception {

        PreparedStatement stmt = null;
        ResultSet result = null;
        Hashtable moveResponses = new Hashtable();
		int totalGameCount = 0;
		long startTime = System.currentTimeMillis();


        int db = requestData.getGameStorerSearchRequestFilterData().getDb();
        int game = requestData.getGameStorerSearchRequestFilterData().getGame();
        GridState state = GridStateFactory.createGridState(
                requestData.getGameStorerSearchRequestFilterData().getGame(),
                requestData);
        responseData.setRotation(state.getRotation());
        long hash = state.getHash();
		int currentPlayer = requestData.getNumMoves() % 2 + 1;

        try {

			String qryString = null;
        	if (!union) {
				qryString =
					"select m.next_move, m.rotation, m.winner, count(*) " +
					"from " + move_table + " m " + filterOptionsFrom.toString() +
					"where m.hash_key = " + hash + " " +
					"and m.move_num = " + (requestData.getNumMoves() - 1) + " " +
					"and m.game = " + game + " " +
					(includeGameTable ? "and g.game = " + game + " " : "") + //thinking is that the game table might be primary table if filtering by certain things
					"and m.dbid = " + db + " " +
					(includeGameTable ? "and g.dbid = " + db + " " : "") +
					filterOptionsWhere.toString() +
					"group by m.next_move, m.rotation, m.winner";
        	}
        	else {
				qryString =
					"select nextm, rot, win, count(*) " +
					"from ( " +
					"select * from (select m.next_move as nextm, m.rotation as rot, m.winner as win " +
					"from " + move_table + " m " + filterOptionsFrom.toString() +
					"where m.hash_key = " + hash + " " +
					"and m.move_num = " + (requestData.getNumMoves() - 1) + " " +
					"and m.game = " + game + " " +
					"and g.game = " + game + " " +
					"and m.dbid = " + db + " " +
					"and g.dbid = " + db + " " +
					filterOptionsWhere.toString() + ") as d1 " +
					"union all " +
					"select * from (select m.next_move as nextm, m.rotation as rot, m.winner as win " +
					"from " + move_table + " m " + filterOptionsFrom2.toString() +
					"where m.hash_key = " + hash + " " +
					"and m.move_num = " + (requestData.getNumMoves() - 1) + " " +
					"and m.game = " + game + " " +
					"and g.game = " + game + " " +
					"and m.dbid = " + db + " " +
					"and g.dbid = " + db + " " +
					filterOptionsWhere2.toString() + ") as d2 " +
					") as main group by nextm, rot, win";
			}

			log4j.debug(qryString);

            stmt = con.prepareStatement(qryString);


		    log4j.debug("hash_key = " + hash);
		    log4j.debug("rotation = " + state.getRotation());
		    log4j.debug("move_num = " + (requestData.getNumMoves() - 1));

            int i = 1;
            //stmt.setLong(i++, hash);
            //stmt.setInt(i++, requestData.getNumMoves() - 1);
            //stmt.setInt(i++, game);
            //if (includeGameTable) stmt.setInt(i++, game);
			//stmt.setInt(i++, db);
            //if (includeGameTable) stmt.setInt(i++, db);
            setFilterOptionsParams(stmt, filterOptionsParams, i);

            if (union) {
                i += filterOptionsParams.size();
                //stmt.setLong(i++, hash);
            	//stmt.setInt(i++, requestData.getNumMoves() - 1);
                //stmt.setInt(i++, game);
                //if (includeGameTable) stmt.setInt(i++, game);
    			//stmt.setInt(i++, db);
                //if (includeGameTable) stmt.setInt(i++, db);
                setFilterOptionsParams(stmt, filterOptionsParams2, i);
            }

            result = stmt.executeQuery();


            while (result.next()) {

                int move = result.getInt(1);
                int rotation = result.getInt(2);
                int winner = result.getInt(3);
                int count = result.getInt(4);
				totalGameCount += count;

				if (move == 361) continue;
				//indicates game ended at this point
				//but still include in total game count

                move = state.rotateMoveToLocalRotation(move, rotation);


                if (winner != GameData.UNKNOWN) {

                    boolean alreadyStored = true;
                    GameStorerSearchResponseMoveData moveData = (GameStorerSearchResponseMoveData) moveResponses.get(new Integer(move));
                    if (moveData == null) {
                        alreadyStored = false;
                        moveData = new SimpleGameStorerSearchResponseMoveData();
                    }

                    moveData.setMove(move);
                    moveData.setGames(moveData.getGames() + count);
                    if (currentPlayer == winner) {
                        moveData.setWins(moveData.getWins() + count);
                    }

                    if (!alreadyStored) {
                        moveResponses.put(new Integer(move), moveData);
                    }
                }
            }

            Enumeration e = moveResponses.elements();
            while (e.hasMoreElements()) {
                GameStorerSearchResponseMoveData moveData = (GameStorerSearchResponseMoveData) e.nextElement();
                responseData.addSearchResponseMoveData(moveData);
            }

        } finally {
            if (result != null) {
                result.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }

        log4j.debug("results time: " + (System.currentTimeMillis() - startTime));
		return totalGameCount;
    }



    protected void getMatchingGames(GameStorerSearchRequestData requestData,
                                    GameStorerSearchResponseData responseData,
                                    int totalGameCount,
                                    StringBuffer filterOptionsFrom,
                                    boolean includeGameTable,
                                    StringBuffer filterOptionsWhere,
                                    Vector filterOptionsParams,
                                    StringBuffer filterOptionsFrom2,
                                    StringBuffer filterOptionsWhere2,
                                    Vector filterOptionsParams2,
                                    boolean union,
                                    Connection con) throws Exception {

        PreparedStatement stmt = null;
        PreparedStatement moveStmt = null;
        ResultSet results = null;
        ResultSet moveResults = null;
        Vector gids = new Vector();

		long startTime = System.currentTimeMillis();
        try {

            // setup the limit to which games get selected
            int startGameNum = requestData.getGameStorerSearchRequestFilterData().getStartGameNum();
            int endGameNum = requestData.getGameStorerSearchRequestFilterData().getEndGameNum();

            String limitStart = Integer.toString(startGameNum);
            String limitLen = Integer.toString(endGameNum - startGameNum);

            GridState state = GridStateFactory.createGridState(
                    requestData.getGameStorerSearchRequestFilterData().getGame(),
                    requestData);
            long hash = state.getHash();
            int gm = requestData.getGameStorerSearchRequestFilterData().getGame();
            int db = requestData.getGameStorerSearchRequestFilterData().getDb();


			String qryString = null;

        	if (!union) {
        		qryString =
					"select m.gid " +
					"from " + move_table + " m " + filterOptionsFrom.toString() +
					"where m.hash_key = " + hash + " " +
					"and m.move_num = " + (requestData.getNumMoves() - 1) + " " +
					"and m.dbid = " + db + " " +
					(includeGameTable ? "and g.dbid = " + db + " " : "") +
					"and m.game = " + gm + " " +
					(includeGameTable ? "and g.game = " + gm + " " : "") +
					filterOptionsWhere.toString() +
					"order by m.play_date desc ";

    			if (hsql) {
    				qryString += "limit " + limitLen + " offset " + limitStart;
    			} else if (!derby) {
    				qryString += "limit " + limitStart + ", " + limitLen;
    			}
        	}
        	else {
        		qryString =
					"select gid from ( " +
					"select m.gid as gid, m.play_date as play_d " +
					"from " + move_table + " m " + filterOptionsFrom.toString() +
					"where m.hash_key = " + hash + " " +
					"and m.move_num = " + (requestData.getNumMoves() - 1) + " " +
					"and m.dbid = " + db + " " +
					"and g.dbid = " + db + " " +
					"and m.game = " + gm + " " +
					"and g.game = " + gm + " " +
					filterOptionsWhere.toString() +
					"union all " +
					"select m.gid, m.play_date as play_d " +
					"from " + move_table + " m " + filterOptionsFrom2.toString() +
					"where m.hash_key = " + hash + " " +
					"and m.move_num = " + (requestData.getNumMoves() - 1) + " " +
					"and m.dbid = " + db + " " +
					"and g.dbid = " + db + " " +
					"and m.game = " + gm + " " +
					"and g.game = " + gm + " " +
					filterOptionsWhere2.toString() +
					") as main order by play_d desc ";
    			if (hsql) {
    				qryString += "limit " + limitLen + " offset " + limitStart;
    			} else if (!derby) {
    				qryString += "limit " + limitStart + ", " + limitLen;
    			}
        	}

			log4j.debug(qryString);

            stmt = con.prepareStatement(qryString);
			if (derby) {
				log4j.debug("end game="+endGameNum);
				stmt.setMaxRows(endGameNum);
			}



log4j.debug("hash="+hash);
log4j.debug("move_num="+ (requestData.getNumMoves() - 1));

			int i = 1;
            //stmt.setLong(i++, hash);
            //stmt.setInt(i++, requestData.getNumMoves() - 1);
            //stmt.setInt(i++, db);
            //if (includeGameTable) stmt.setInt(i++, db);
			//stmt.setInt(i++, gm);
			//if (includeGameTable) stmt.setInt(i++, gm);
            setFilterOptionsParams(stmt, filterOptionsParams, i);
            if (union) {
                i += filterOptionsParams.size();
                //stmt.setLong(i++, hash);
                //stmt.setInt(i++, requestData.getNumMoves() - 1);
                //stmt.setInt(i++, db);
                //stmt.setInt(i++, db);
    			//stmt.setInt(i++, gm);
    			//stmt.setInt(i++, gm);
                setFilterOptionsParams(stmt, filterOptionsParams2, i++);
            }


            results = stmt.executeQuery();

            List<PlunkGameData> games = new ArrayList<PlunkGameData>(endGameNum - startGameNum);

            int cnt = 0;
            while (results.next()) {
            	if ((derby && cnt++ >= startGameNum) || !derby) {
            		long gid = results.getLong(1);
log4j.debug("gid="+gid+", " + cnt);
            		PlunkGameData d = new PlunkGameData();
            		d.setStored(true);
            		d.setGameID(gid);
            		games.add(d);
            	}
            }

            if (results != null) {
                results.close();
            }
            if (stmt != null) {
                stmt.close();
            }


			requestData.getGameStorerSearchRequestFilterData().setTotalGameNum(
				totalGameCount);
log4j.debug("total matched games = " + requestData.getGameStorerSearchRequestFilterData().getTotalGameNum());

			stmt = con.prepareStatement(
                "select g.site_id, g.event_id, g.round, g.section, " +
                "g.play_date, g.timer, g.rated, g.initial_time, " +
                "g.incremental_time, p1.name, p2.name, " +
                "g.player1_rating, g.player2_rating, g.winner, g.game, " +
                "g.swapped, g.private " +
                "from (pente_game g left outer join player p1 on g.player1_pid = p1.pid) " +
                "left outer join player p2 on g.player2_pid = p2.pid " +
                "where g.gid = ?");
			    //"and p1.pid = g.player1_pid " +
                //"and p2.pid = g.player2_pid");

            // load matched games into response object
            for (PlunkGameData g : games) {
            	stmt.setLong(1, g.getGameID());
                results = stmt.executeQuery();

                if (results.next()) {
                	log4j.debug("start loading game " + g.getGameID());
                	int siteID = results.getInt(1);
                    int game = results.getInt(15);

                    GameSiteData siteData = gameVenueStorer.getGameSiteData(
                        game, siteID);
                    g.setSite(siteData.getName());
                    g.setShortSite(siteData.getShortSite());
                    g.setSiteURL(siteData.getURL());

                    // games in database Pro-Pente and Pente.org Online
                    // not editable
                    if (db > 2) {
                    	g.setEditable(true);
                    }

                    int eventID = results.getInt(2);
                    GameEventData eventData = gameVenueStorer.getGameEventData(
                        game, eventID, siteData.getName());
                    g.setEvent(eventData.getName());


                    g.setRound(results.getString(3));
                    g.setSection(results.getString(4));
                    Timestamp playDate = results.getTimestamp(5);
                    g.setDate(new java.util.Date(playDate.getTime()));

                    String timed = results.getString(6);
                    if (timed.equals("S") || timed.equals("I")) {
                        g.setTimed(true);
                    }
                    else {
                        g.setTimed(false);
                    }

                    //g.setRated(MySQLDBHandler.getBooleanValueFromDBString(results.getString(7)));
                    g.setInitialTime(results.getInt(8));
                    g.setIncrementalTime(results.getInt(9));

                    PlayerData player1Data = new DefaultPlayerData();
                    player1Data.setUserIDName(results.getString(10));
                    player1Data.setRating(results.getInt(12));
                    g.setPlayer1Data(player1Data);

                    PlayerData player2Data = new DefaultPlayerData();
                    player2Data.setUserIDName(results.getString(11));
                    player2Data.setRating(results.getInt(13));
                    g.setPlayer2Data(player2Data);

                    g.setWinner(results.getInt(14));
                    g.setGame(GridStateFactory.getGameName(game));
                    g.setSwapped(results.getString(16).equals("Y"));

                    g.setPrivateGame(results.getString(17).equals("Y"));

                    responseData.addGame(g);
                	log4j.debug("done loading game " + g.getGameID());
                }
                else {
                	log4j.debug("no results for game " + g.getGameID());
                }

                if (results != null) {
                	results.close();
                }
                if (moveResults != null) {
                	moveResults.close();
                }
            }
log4j.debug("loaded games = " + responseData.getGames().size());

        } finally {

            if (results != null) {
                results.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (moveResults != null) {
            	moveResults.close();
            }
            if (moveStmt != null) {
            	moveStmt.close();
            }
        }
        log4j.debug("game time: " + (System.currentTimeMillis() - startTime));
    }
}