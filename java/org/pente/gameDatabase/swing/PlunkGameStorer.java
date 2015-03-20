

package org.pente.gameDatabase.swing;

import java.sql.*;
import java.util.*;

import org.pente.database.*;
import org.pente.game.*;


/**
 * @author dweebo
 */
public abstract class PlunkGameStorer {

    /** The name of the table with the player information */
    protected static final String PLAYER_TABLE = "player";
    public static final String GAME_SITE_TABLE = "game_site";

    protected static final Vector PLAYER_TABLES = new Vector();
    static {
        PLAYER_TABLES.addElement(PLAYER_TABLE);
        PLAYER_TABLES.addElement(GAME_SITE_TABLE);
    }

    protected DBHandler                     dbHandler;
    protected PlunkGameVenueStorer               gameVenueStorer;

    public PlunkGameStorer(DBHandler dbHandler, PlunkGameVenueStorer gameVenueStorer) throws Exception {
        this.dbHandler = dbHandler;
        this.gameVenueStorer = gameVenueStorer;
    }

    /** Make sure the database handler is destroyed
     */
    public void destroy() {
        dbHandler.destroy();
    }

    /** Store the game information
     *  @param data The GameData for a game
     *  @exception Exception If the game can't be stored
     */
    public void storeGame(PlunkGameData data, GameDbData db) throws Exception {

        Connection con = null;

        try {

            con = dbHandler.getConnection();


            if (data.getSite() == null || data.getSite().equals("")) {
            	data.setSite("Unknown");
            }
            GameSiteData siteData = gameVenueStorer.getGameSiteData(
               GridStateFactory.getGameId(data.getGame()), data.getSite());
            if (siteData == null) {
                siteData = new SimpleGameSiteData();
                siteData.setName(data.getSite());
                // this also updates the site id
                //TODO if a site already exists but a new game is being saved
                //with that site in a different database (that currently has no
                //games of that site, then we need to add the site data to the
                //dbdata...
                gameVenueStorer.addGameSiteData(db,
                    GridStateFactory.getGameId(data.getGame()), siteData);
            }

            if (data.getEvent() == null || data.getEvent().equals("")) {
            	data.setEvent("Unknown");
            }
            GameEventData eventData = gameVenueStorer.getGameEventData(
                GridStateFactory.getGameId(data.getGame()), data.getEvent(),
                data.getSite());
            if (eventData == null) {
                eventData = new SimpleGameEventData();
                eventData.setName(data.getEvent());
                // this also updates the event id
                gameVenueStorer.addGameEventData(
                    GridStateFactory.getGameId(data.getGame()),
                    eventData, siteData.getName());
            }
            if (data.getRound() != null && !data.getRound().equals("")) {
	            boolean newRound = true;
	            for (Iterator it = eventData.getGameRoundData().iterator(); it.hasNext();) {
	            	GameRoundData rd = (GameRoundData) it.next();
	            	if (rd.getName().equals(data.getRound())) {
	            		newRound = false;
	            		break;
	            	}
	            }
	            if (newRound) {
	            	eventData.addGameRoundData(new SimpleGameRoundData(data.getRound()));
	            }
            }

            if (data.getPlayer1Data() == null || data.getPlayer1Data().getUserIDName() == null) {
            	PlayerData p1 = new DefaultPlayerData();
            	p1.setUserIDName("Unknown");
            	data.setPlayer1Data(p1);
            }
            PlayerData p = loadPlayer(con, data.getPlayer1Data().getUserIDName(),
            	data.getSite());
            if (p == null) {
            	data.getPlayer1Data().setUserID(0);
                storePlayer(con, data.getPlayer1Data(), data.getSite());
            }
            else {
            	data.getPlayer1Data().setUserID(p.getUserID());
            }


            if (data.getPlayer2Data() == null || data.getPlayer2Data().getUserIDName() == null) {
            	PlayerData p2 = new DefaultPlayerData();
            	p2.setUserIDName("Unknown");
            	data.setPlayer2Data(p2);
            }
            p = loadPlayer(con, data.getPlayer2Data().getUserIDName(),
                data.getSite());
            if (p == null) {
            	data.getPlayer2Data().setUserID(0);
                storePlayer(con, data.getPlayer2Data(), data.getSite());
            }
            else {
            	data.getPlayer2Data().setUserID(p.getUserID());
            }

            // add the game
            storeGame(con, data, db.getID());

            data.setStored(true);

        } finally {
            if (con != null) {
                dbHandler.freeConnection(con);
            }
        }
    }


    /** Add the game to the database, implemented by individual game subclasses
     *  and called by storeGame(GameData)
     *  @param con A database connection
     *  @param data The GameData for a game
     *  @exception Exception If the game can't be added
     */
    public abstract void storeGame(Connection con, PlunkGameData data, int db) throws Exception;


    /** Checks to see if the player has already been stored
     *  @param playerID The unique player id
     *  @param site The site the player is registered for
     *  @return boolean Flag if player has been stored
     *  @exception Exception If the player cannot be checked
     */
    public boolean playerAlreadyStored(long playerID, String site) throws Exception {

        Connection con = null;
        boolean stored = false;

        try {
            con = dbHandler.getConnection();

            stored = playerAlreadyStored(con, playerID, site);

        } finally {
            if (con != null) {
                dbHandler.freeConnection(con);
            }
        }

        return stored;
    }

    /** Checks if a player exists in the database
     *  @param con A database connection
     *  @param playerID The unique player id
     *  @param site The site the player is registered for
     *  @exception Exception If there is a problem checking for a player
     */
    public boolean playerAlreadyStored(Connection con, long playerID, String site) throws Exception {

        PreparedStatement stmt = null;
        ResultSet result = null;
        boolean exists = true;

        try {

            int siteID = gameVenueStorer.getSiteID(site);
            if (siteID == -1) {
                exists = false;
            }
            else {
                stmt = con.prepareStatement("select 1 " +
                                            "from " + PLAYER_TABLE + " " +
                                            "where pid = ? " +
                                            "and site_id = ?");
                stmt.setLong(1, playerID);
                stmt.setInt(2, siteID);

                result = stmt.executeQuery();
                exists = result.next();
            }

        } finally {
            if (result != null) { try { result.close(); } catch(SQLException ex) {} }
            if (stmt != null) { try { stmt.close(); } catch(SQLException ex) {} }
        }

        return exists;
    }

    /** Checks to see if the player has already been stored
     *  @param name The players name
     *  @param site The site the player is registered for
     *  @return boolean Flag if player has been stored
     *  @exception Exception If the player cannot be checked
     */
    public boolean playerAlreadyStored(String name, String site) throws Exception {

        Connection con = null;
        boolean stored = false;

        try {
            con = dbHandler.getConnection();

            stored = playerAlreadyStored(con, name, site);

        } finally {
            if (con != null) {
                dbHandler.freeConnection(con);
            }
        }

        return stored;
    }

    /** Checks to see if the player has already been stored
     *  @param con A database connection
     *  @param name The players name
     *  @param site The site the player is registered for
     *  @return boolean Flag if player has been stored
     *  @exception Exception If the player cannot be checked
     */
    public boolean playerAlreadyStored(Connection con, String name, String site) throws Exception {

        PreparedStatement stmt = null;
        ResultSet result = null;
        boolean exists = true;

        try {
            int siteID = gameVenueStorer.getSiteID(site);
            if (siteID == -1) {
                exists = false;
            }
            else {
                stmt = con.prepareStatement("select 1 " +
                                            "from " + PLAYER_TABLE + " " +
                                            "where name = ? " +
                                            "and site_id = ?");
                stmt.setString(1, name);
                stmt.setInt(2, siteID);

                result = stmt.executeQuery();
                exists = result.next();
            }

        } finally {
            if (result != null) { try { result.close(); } catch(SQLException ex) {} }
            if (stmt != null) { try { stmt.close(); } catch(SQLException ex) {} }
        }

        return exists;
    }

    /** Stores the player information
     *  @param data The PlayerData for a game
     *  @exception If the player cannot be stored
     */
    public void storePlayer(PlayerData data, String site) throws Exception {

        Connection con = null;

        try {
            con = dbHandler.getConnection();

            storePlayer(con, data, site);

        } finally {
            if (con != null) {
                dbHandler.freeConnection(con);
            }
        }
    }

    /** Add a player to the database
     *  @param con A database connection
     *  @param playerData Information about a player
     *  @exception Exception If the player can't be added
     */
    public void storePlayer(Connection con, PlayerData playerData,
        String site) throws Exception {

        PreparedStatement stmt = null;
        ResultSet result = null;
        boolean newPlayer = false;

        try {

            int siteID = gameVenueStorer.getSiteID(site);

            if (playerAlreadyStored(con, playerData.getUserIDName(), site) ||
                (playerData.getUserID() != 0 && playerAlreadyStored(
                 con, playerData.getUserID(), site))) {
                return;
            }

            // if this is a new player
            if (playerData.getUserID() == 0) {

                stmt = con.prepareStatement("select max(pid) + 1 " +
                                            "from " + PLAYER_TABLE + " " +
                                            "where pid >= 60000000000000");

                result = stmt.executeQuery();
                if (result.next()) {
                	long l = result.getLong(1);
                	if (l < 60000000000000L) {
                		l = 60000000000000L;
                	}
                    playerData.setUserID(l);
                }

                newPlayer = true;

                if (result != null) {
                    result.close();
                }

                if (stmt != null) {
                    stmt.close();
                }
            }

            stmt = con.prepareStatement("insert into " + PLAYER_TABLE + " " +
                                        "(pid, name, site_id, name_lower) " +
                                        "values(?, ?, ?, lower(?))");

            stmt.setLong(1, playerData.getUserID());
            stmt.setString(2, playerData.getUserIDName());
            stmt.setInt(3, siteID);
            stmt.setString(4, playerData.getUserIDName());

            stmt.executeUpdate();

        } finally {
            if (result != null) { try { result.close(); } catch(SQLException ex) {} }
            if (stmt != null) { try { stmt.close(); } catch(SQLException ex) {} }
        }
    }

    /** Loads the player information
     *  @param playerID The unique player id
     *  @return PlayerData The player data
     *  @exception If the player cannot be stored
     */
    public PlayerData loadPlayer(long playerID, String site) throws Exception {

        Connection con = null;
        PlayerData playerData = null;

        try {
            con = dbHandler.getConnection();

            playerData = loadPlayer(con, playerID, site);

        } finally {
            if (con != null) {
                dbHandler.freeConnection(con);
            }
        }

        return playerData;
    }

    /** Loads the player information
     *  @param con A database connection to load the player from
     *  @param playerID The unique player id
     *  @return PlayerData The player data
     *  @exception If the player cannot be stored
     */
    public PlayerData loadPlayer(Connection con, long playerID, String site) throws Exception {

        PreparedStatement stmt = null;
        ResultSet result = null;
        PlayerData playerData = null;

        try {

            int siteID = gameVenueStorer.getSiteID(site);
            if (siteID != -1) {

                stmt = con.prepareStatement("select name " +
                                            "from " + PLAYER_TABLE + " " +
                                            "where pid = ? " +
                                            "and site_id = ?");
                stmt.setLong(1, playerID);
                stmt.setInt(2, siteID);

                result = stmt.executeQuery();

                if (result.next()) {

                    playerData = new DefaultPlayerData();
                    playerData.setUserID(playerID);
                    playerData.setUserIDName(result.getString(1));
                }
            }

        } finally {
            if (stmt != null) { try { stmt.close(); } catch(SQLException ex) {} }
            if (result != null) { try { result.close(); } catch(SQLException ex) {} }
        }

        return playerData;
    }

    /** Loads the player information
     *  @param name The players name
     *  @param site The site the player is registered for
     *  @return PlayerData The player data
     *  @exception If the player cannot be stored
     */
    public PlayerData loadPlayer(String name, String site) throws Exception {

        Connection con = null;
        PlayerData playerData = null;

        try {
            con = dbHandler.getConnection();

            playerData = loadPlayer(con, name, site);

        } finally {
            if (con != null) {
                dbHandler.freeConnection(con);
            }
        }

        return playerData;
    }

    /** Loads the player information
     *  @param con A database connection to load the player from
     *  @param name The players name
     *  @param site The site the player is registered for
     *  @return PlayerData The player data
     *  @exception If the player cannot be stored
     */
    public PlayerData loadPlayer(Connection con, String name, String site) throws Exception {

        PreparedStatement stmt = null;
        ResultSet result = null;
        PlayerData playerData = null;

        try {
            int siteID = gameVenueStorer.getSiteID(site);
            if (siteID > 0) {

                stmt = con.prepareStatement("select pid " +
                                            "from " + PLAYER_TABLE + " " +
                                            "where name = ? " +
                                            "and site_id = ?");
                stmt.setString(1, name);
                stmt.setInt(2, siteID);

                //MySQLDBHandler.lockTable(PLAYER_TABLE, con);
                result = stmt.executeQuery();

                if (result.next()) {

                    playerData = new DefaultPlayerData();
                    playerData.setUserID(result.getLong(1));
                    playerData.setUserIDName(name);
                }
            }

        } finally {
            //if (con != null) {
            //    MySQLDBHandler.unLockTables(con);
            //}
            if (stmt != null) { try { stmt.close(); } catch(SQLException ex) {} }
            if (result != null) { try { result.close(); } catch(SQLException ex) {} }
        }

        return playerData;
    }
}