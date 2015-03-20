
package org.pente.gameDatabase.swing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.apache.log4j.Category;
import org.pente.database.DBHandler;
import org.pente.game.GameData;
import org.pente.game.GameEventData;
import org.pente.game.GameSiteData;
import org.pente.game.GridStateFactory;
import org.pente.game.PlayerData;


/**
 * @author dweebo
 */
public class PlunkPenteGameStorer extends PlunkGameStorer {

    /** The name of the table with the game information */
    protected static final String GAME_TABLE = "pente_game";
    /** The name of the table with the move information */
    protected static final String MOVE_TABLE = "pente_move";



    private static Category log4j = Category.getInstance(PlunkPenteGameStorer.class.getName());

    private PlunkDbUtil plunkDbUtil;

    /** Needed because super class throws Exception in default constructor
     *  @exception If super class throws Exception during initialization
     */
    public PlunkPenteGameStorer(DBHandler dbHandler, PlunkGameVenueStorer gameVenueStorer) throws Exception {
        super(dbHandler, gameVenueStorer);

        //this.gameVenueStorer = gameVenueStorer;
    }


    /** Checks to see if the game has already been stored
     *  This method looks for games that occurred at the same time and then
     *  if any are found gets the data from the db and compares the games with
     *  equals().  If in the future there are games stored with only date info,
     *  not time info, a better strategy might be needed.
     *  @param con A database connection to get check for the game
     *  @param gameData The game data
     *  @return boolean Flag if game has been stored
     *  @exception Exception If the game cannot be checked
     */
    public boolean gameAlreadyStored(PlunkGameData gameData, int dbid) throws Exception {
        return getGid(gameData, dbid) != -1;
    }

    /** Checks to see if the game has already been stored
     *  This method looks for games that occurred at the same time and then
     *  if any are found gets the data from the db and compares the games with
     *  equals().  If in the future there are games stored with only date info,
     *  not time info, a better strategy might be needed.
     *  @param con A database connection to get check for the game
     *  @param gameData The game data
     *  @return long The game id
     *  @exception Exception If the game cannot be checked
     */
    public long getGid(PlunkGameData gameData, int dbid) throws Exception {

        PreparedStatement stmt = null;
        ResultSet result = null;
        long gid = -1;

        Connection con = null;

        try {

            con = dbHandler.getConnection();

            // look for games ending in same hash
            stmt = con.prepareStatement("select gid " +
                                        "from pente_move " +
                                        "where hash_key = ? " +
                                        "and next_move = 361 " +
                                        "and game = ? " +
                                        "and dbid = ? " +
                                        "and move_num = ?");

            PlunkNode l = gameData.getRoot();
            if (l == null) return -1;
            while (l.hasChildren()) {
            	l = l.getChildren().get(0);
            }
			stmt.setLong(1, l.getHash());
			stmt.setInt(2, GridStateFactory.getGameId(gameData.getGame()));
            stmt.setInt(3, dbid);
            stmt.setInt(4, l.getDepth());

            result = stmt.executeQuery();

            while (result.next()) {

                long mgid = result.getLong(1);
                log4j.debug("possibly matched gid = " + mgid);
                try {
	                PlunkGameData storedGameData = new PlunkGameData();
	                loadGame(con, mgid, storedGameData);

	                if (storedGameData.equals(gameData)) {
	                    gid = mgid;
	                    log4j.debug("matched gid = " + gid);
	                    break;
	                }
                } catch (Throwable t) {
                	log4j.debug("failed to load " + mgid);
                }
            }

        } finally {
            if (result != null) { try { result.close(); } catch(SQLException ex) {} }
            if (stmt != null) { try { stmt.close(); } catch(SQLException ex) {} }
            if (con != null) {
                dbHandler.freeConnection(con);
            }
        }

        return gid;
    }

    /** Stores the game information
     *  @param con A database connection to store the game in
     *  @param data The game data
     *  @exception Exception If the game cannot be stored
     */
    public void storeGame(Connection con, PlunkGameData data, int db) throws Exception {

        PreparedStatement stmt = null;
        ResultSet result = null;

        try {
            String timer = "N";
            if (data.getTimed()) {
                if (data.getIncrementalTime() == 0) {
                    timer = "S";
                }
                else {
                    timer = "I";
                }
            }

            GameSiteData siteData = gameVenueStorer.getGameSiteData(
                GridStateFactory.getGameId(data.getGame()), data.getSite());

            GameEventData eventData = gameVenueStorer.getGameEventData(
                GridStateFactory.getGameId(data.getGame()), data.getEvent(),
                data.getSite());

            if (data.getGameID() == 0) {

                // enter game info
                stmt = con.prepareStatement("insert into " + GAME_TABLE + " " +
                    "(site_id, event_id, round, section, play_date, timer, rated, " +
                    " initial_time, incremental_time, player1_pid, player2_pid, " +
                    " player1_rating, player2_rating, player1_type, player2_type, " +
                    " winner, game, swapped, private, dbid) " +
                    "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
    				Statement.RETURN_GENERATED_KEYS);

                stmt.setInt(1, siteData.getSiteID());
                stmt.setInt(2, eventData.getEventID());
                stmt.setString(3, data.getRound());
                stmt.setString(4, data.getSection());
                if (data.getDate() == null) {
                	data.setDate(new java.util.Date());
                }
                //TODO replace this, should be able to have null date
                stmt.setTimestamp(5, new Timestamp(data.getDate().getTime()));
                stmt.setString(6, timer);
                stmt.setString(7, data.getRated() ? "Y" : "N");
                stmt.setInt(8, data.getInitialTime());
                stmt.setInt(9, data.getIncrementalTime());
                stmt.setLong(10, data.getPlayer1Data().getUserID());
                stmt.setLong(11, data.getPlayer2Data().getUserID());
                stmt.setInt(12, data.getPlayer1Data().getRating());
                stmt.setInt(13, data.getPlayer2Data().getRating());
                stmt.setInt(14, data.getPlayer1Data().getType());
                stmt.setInt(15, data.getPlayer2Data().getType());
                stmt.setInt(16, data.getWinner());
                stmt.setInt(17, GridStateFactory.getGameId(data.getGame()));
                stmt.setString(18, data.didPlayersSwap() ? "Y" : "N");
                stmt.setString(19, data.isPrivateGame() ? "Y" : "N");
                stmt.setInt(20, db);
                stmt.executeUpdate();
    	        result = stmt.getGeneratedKeys();
    	        if (result.next()) {
    	        	data.setGameID(result.getLong(1));
    	        }
                stmt.close();
            }
            else {
                stmt = con.prepareStatement("update " + GAME_TABLE + " " +
                    "set site_id = ?, event_id = ?, round = ?, section = ?, " +
                    "play_date = ?, timer = ?, rated = ?, " +
                    "initial_time = ?, incremental_time = ?, player1_pid = ?, " +
                    "player2_pid = ?, player1_rating = ?, player2_rating = ?, " +
                    "player1_type = ?, player2_type = ?, winner = ?, swapped = ? " +
                    "where gid = ? and dbid = ?");
                stmt.setInt(1, siteData.getSiteID());
                stmt.setInt(2, eventData.getEventID());
                stmt.setString(3, data.getRound());
                stmt.setString(4, data.getSection());
                stmt.setTimestamp(5, new Timestamp(data.getDate().getTime()));
                stmt.setString(6, timer);
                stmt.setString(7, data.getRated() ? "Y" : "N");
                stmt.setInt(8, data.getInitialTime());
                stmt.setInt(9, data.getIncrementalTime());
                stmt.setLong(10, data.getPlayer1Data().getUserID());
                stmt.setLong(11, data.getPlayer2Data().getUserID());
                stmt.setInt(12, data.getPlayer1Data().getRating());
                stmt.setInt(13, data.getPlayer2Data().getRating());
                stmt.setInt(14, data.getPlayer1Data().getType());
                stmt.setInt(15, data.getPlayer2Data().getType());
                stmt.setInt(16, data.getWinner());
                stmt.setString(17, data.didPlayersSwap() ? "Y" : "N");
                stmt.setLong(18, data.getGameID());
                stmt.setInt(19, db);
                stmt.executeUpdate();

                stmt.close();
                stmt = con.prepareStatement("delete from pente_move where gid = ? " +
                	"and dbid = ?");
                stmt.setLong(1, data.getGameID());
                stmt.setInt(2, db);
                stmt.executeUpdate();
            }

            insertMoves(con, data, db);

        } finally {

            if (stmt != null) { try { stmt.close(); } catch(SQLException ex) {} }
        }
    }

    public void insertMoves(Connection con, PlunkGameData data, int db)
    	throws SQLException
    {
    	PreparedStatement stmt = null;
        // create hash keys for all moves
		int game = GridStateFactory.getGameId(data.getGame());
        //GridState state = GridStateFactory.createGridState(
        //    game, data);

    	try {
    		PlunkNode n = data.getRoot();
	//    	 store moves
	        stmt = con.prepareStatement("insert into " + MOVE_TABLE + " " +
	            "(gid, move_num, next_move, hash_key, rotation, game, winner, " +
	            " play_date, dbid, type, name, comments) " +
	        	"values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
	        stmt.setLong(1, data.getGameID());
	        int i = 0;
	        while (true) {
	        //for (int i = 0; i < state.getNumMoves(); i++) {
	            stmt.setInt(2, i++);
				// last move sets next_move to 361
				if (!n.hasChildren()) {
					stmt.setInt(3, 361);
				}
				else {
	                stmt.setInt(3, n.getChildren().get(0).getMove());
				}
	            stmt.setLong(4, n.getHash());
	            stmt.setInt(5, n.getRotation());
				stmt.setInt(6, game);
				stmt.setInt(7, data.getWinner());
				stmt.setTimestamp(8, new Timestamp(data.getDate().getTime()));
				stmt.setInt(9, db);
		        stmt.setInt(10, n.getType());
		        stmt.setString(11, n.getName());
		        stmt.setString(12, n.getComments());
	            stmt.executeUpdate();

	            if (!n.hasChildren()) break;
	            n = n.getChildren().get(0);
	        }

	    } finally {

	        if (stmt != null) { try { stmt.close(); } catch(SQLException ex) {} }
	    }
    }


    /** Loads the game information
     *  @param gameID The unique game id
     *  @param data To store the game data in
     *  @return GameData The game data
     *  @exception Exception If the game cannot be loaded
     */
    public GameData loadGame(long gameID, PlunkGameData data) throws Exception {

        Connection con = null;
        GameData gameData = null;

        try {
            con = dbHandler.getConnection();

            gameData = loadGame(con, gameID, data);

        } finally {
            if (con != null) {
                dbHandler.freeConnection(con);
            }
        }

        return gameData;
    }

    /** Loads the game information
     *  @param con A database connection used to load the game
     *  @param gameID The unique game id
     *  @param data To load the game data into
     *  @return GameData The game data
     *  @exception Exception If the game cannot be loaded
     */
    public GameData loadGame(Connection con, long gameID, PlunkGameData gameData) throws Exception {

        PreparedStatement gameStmt = null;
        ResultSet gameResult = null;

        PreparedStatement moveStmt = null;
        ResultSet moveResult = null;
log4j.debug("loadGame(" + gameID + ") start");
        try {

            gameStmt = con.prepareStatement(
                "select site_id, event_id, round, section, play_date, timer, " +
                "rated, initial_time, incremental_time, player1_pid, " +
                "player2_pid, player1_rating, player2_rating, winner, game, swapped, private, name " +
                "from pente_game, game_db " +
                "where pente_game.gid = ? " +
                "and pente_game.dbid = game_db.dbid");
            gameStmt.setLong(1, gameID);
            gameResult = gameStmt.executeQuery();
log4j.debug("select data complete");
            if (gameResult.next()) {

                if (gameData == null) {
                    gameData = new PlunkGameData();
                }
                gameData.setStored(true);
                gameData.setGameID(gameID);

                int siteID = gameResult.getInt(1);
                int game = gameResult.getInt(15);

                log4j.debug("getGameSiteData for " + gameID + ", " + game + ", " + siteID);
                GameSiteData siteData = gameVenueStorer.getGameSiteData(
                    game, siteID);
                gameData.setSite(siteData.getName());
                gameData.setShortSite(siteData.getShortSite());
                gameData.setSiteURL(siteData.getURL());
				log4j.debug("done get site data");

                int eventID = gameResult.getInt(2);
				log4j.debug("get event data");
                GameEventData eventData = gameVenueStorer.getGameEventData(
                    game, eventID, siteData.getName());
                gameData.setEvent(eventData.getName());
				log4j.debug("done get event data");

                gameData.setRound(gameResult.getString(3));
                gameData.setSection(gameResult.getString(4));

                Timestamp playDate = gameResult.getTimestamp(5);
                gameData.setDate(new java.util.Date(playDate.getTime()));

                String timed = gameResult.getString(6);
                if (timed.equals("S") || timed.equals("I")) {
                    gameData.setTimed(true);
                }
                else {
                    gameData.setTimed(false);
                }

                //gameData.setRated(MySQLDBHandler.getBooleanValueFromDBString(gameResult.getString(7)));
                gameData.setInitialTime(gameResult.getInt(8));
                gameData.setIncrementalTime(gameResult.getInt(9));

				log4j.debug("get p1 data");
                long player1_pid = gameResult.getLong(10);
                int player1_rating = gameResult.getInt(12);
                PlayerData player1Data = loadPlayer(con, player1_pid, gameData.getSite());
                player1Data.setRating(player1_rating);
                gameData.setPlayer1Data(player1Data);
				log4j.debug("done get p1 data");

                long player2_pid = gameResult.getLong(11);
                PlayerData player2Data = loadPlayer(con, player2_pid, gameData.getSite());

                int player2_rating = gameResult.getInt(13);
                player2Data.setRating(player2_rating);
                gameData.setPlayer2Data(player2Data);
				log4j.debug("done get p2 data");

                gameData.setWinner(gameResult.getInt(14));
                gameData.setSwapped(gameResult.getString(16).equals("Y"));

                gameData.setPrivateGame(gameResult.getString(17).equals("Y"));

                gameData.setDbName(gameResult.getString(18));

                gameData.setGame(GridStateFactory.getGameName(game));

				log4j.debug("get moves");
                moveStmt = con.prepareStatement("select next_move " +
                                                "from " + MOVE_TABLE + " " +
                                                "where gid = ? " +
                                                "and next_move != 361 " +
                                                "order by move_num");
                moveStmt.setLong(1, gameID);

                moveResult = moveStmt.executeQuery();
				gameData.addMove(180);//180 is always 1st move
                while (moveResult.next()) {
                    gameData.addMove(moveResult.getInt(1));
                }
            }

            log4j.debug("loadGame(" + gameID + ") end");

        } finally {

            if (gameResult != null) {
                gameResult.close();
            }
            if (gameStmt != null) {
                gameStmt.close();
            }

            if (moveResult != null) {
                moveResult.close();
            }
            if (moveStmt != null) {
                moveStmt.close();
            }
        }

        return gameData;
    }
}