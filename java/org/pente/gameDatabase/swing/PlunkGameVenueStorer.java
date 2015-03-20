package org.pente.gameDatabase.swing;

import java.util.*;
import java.sql.*;

import org.apache.log4j.*;

import org.pente.database.*;
import org.pente.game.*;

/**
 * @author dweebo
 */
public class PlunkGameVenueStorer {

    private static Category log4j = Category.getInstance(
            PlunkGameVenueStorer.class.getName());

    public static final String GAME_SITE_TABLE = "game_site";
    public static final String GAME_EVENT_TABLE = "game_event";

    private DBHandler   dbHandler;
    private List<GameDbData> tree;

    private List<GameSiteData> siteData;
    private Map<Integer, GameEventData> eventData;


    public List<GameSiteData> getSites() {
		return siteData;
	}
    public Collection<GameEventData> getEvents() {
    	return eventData.values();
    }

	public PlunkGameVenueStorer(DBHandler dbHandler) throws Exception {

        this.dbHandler = dbHandler;

        tree = new ArrayList<GameDbData>();

        // update the tree initially so calls to getSiteData, getEventData
        // dont' fail.  don't need site tree until getSiteTree() is called.
        //try {
            updateGameTree(dbHandler);
        //} catch (Exception ex) {
        //    ex.printStackTrace();
        //}
    }
    public void addGameDbData(GameDbData db, int game)
    	throws Exception {

    	Connection con = null;
    	PreparedStatement stmt = null;
    	ResultSet result = null;

	    try {

	        con = dbHandler.getConnection();

	        // insert site info into db
	        stmt = con.prepareStatement("insert into game_db " +
	                                    "(name, version) " +
	                                    "values(?, ?)",
	                    				Statement.RETURN_GENERATED_KEYS);

	        stmt.setString(1, db.getName());
	        stmt.setString(2, "1.0");
	        stmt.executeUpdate();
	        result = stmt.getGeneratedKeys();
	        if (result.next()) {
	        	db.setID(result.getInt(1));
	        }

	        synchronized (tree) {
	        	tree.add(db);
	        	GameTreeData t = new SimpleGameTreeData();
	        	t.setID(game);
	        	t.setName(GridStateFactory.getGameName(game));
	        	db.addGameTreeData(t);
	        }

	    } finally {
	        if (result != null) { try { result.close(); } catch(SQLException ex) {} }
	        if (stmt != null) { try { stmt.close(); } catch(SQLException ex) {} }
	        if (con != null) { try { dbHandler.freeConnection(con); } catch (Exception ex) {} }
	    }
    }

    public void addGameTreeData(GameDbData dbData) {
    	tree.add(dbData);
    }

    public List<GameDbData> getDbTree() {
    	if (tree == null) {
	        try {
	            updateGameTree(dbHandler);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
    	}
    	return tree;
    }

    public Vector getLoadedGameTree() {
    	if (tree == null) {

            try {
                updateGameTree(dbHandler);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
    	}
        // return the game data for the pente.org online db=2
        for (GameDbData db : tree) {
        	if (db.getID() == 2) {
        		return new Vector(db.getGameTreeData());
        	}
        }
        return null;
    }
    /** Calling this can take awhile since the tree is completely regenerated
     *  this is the old way, before db's were added, this is used by pente.org online db*/
    public Vector getGameTree() {

        try {
            updateGameTree(dbHandler);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // return the game data for the pente.org online db=2
        for (GameDbData db : tree) {
        	if (db.getID() == 2) {
        		return new Vector(db.getGameTreeData());
        	}
        }
        return null;
    }

	class Data {
		int dbid;
		int game;
		GameTreeData treeData;
		int siteId;
		GameSiteData siteData;
		int eventId;
		GameEventData eventData;
		String eventStr;
		String round;
		String section;
	}

    private void updateGameTree(DBHandler dbHandler) throws Exception {

    	long startTime = System.currentTimeMillis();
        log4j.info("MySQLGameVenueStorer.updateGameTree() started");
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;

        List<GameDbData> newTree = new ArrayList<GameDbData>();

        try {
            con = dbHandler.getConnection();


			// get all combinations of games+sites+events+rounds+sections
			// uses index on pente_game, pretty quick
			stmt = con.prepareStatement(
				"select distinct dbid, game, site_id, event_id, round, section " +
				"from pente_game " +
				"order by dbid, game, site_id, event_id, round, section");
			result = stmt.executeQuery();
			List<Data> data = new ArrayList<Data>(3000);
            while (result.next()) {
				Data d = new Data();
				d.dbid = result.getInt(1);
				d.game = result.getInt(2);
				d.siteId = result.getInt(3);
				d.eventId = result.getInt(4);
				d.round = result.getString(5);
				d.section = result.getString(6);
				data.add(d);
            }
			result.close();
			stmt.close();


//			// get all unique db data
			stmt = con.prepareStatement(
				"select dbid, name, version from game_db");
			List<GameDbData> dbData = new ArrayList<GameDbData>(5);
			result = stmt.executeQuery();
			while (result.next()) {
				GameDbData s = new SimpleGameDbData();
				s.setID(result.getInt(1));
				s.setName(result.getString(2));
				dbData.add(s);
			}
			result.close();
			stmt.close();

			// get all unique site data
			stmt = con.prepareStatement(
				"select sid, name, short_name, URL from game_site");
			siteData = new ArrayList<GameSiteData>(5);
			result = stmt.executeQuery();
			while (result.next()) {
				GameSiteData s = new SimpleGameSiteData();
				s.setSiteID(result.getInt(1));
				s.setName(result.getString(2));
				s.setShortSite(result.getString(3));
				s.setURL(result.getString(4));
				siteData.add(s);
			}
			result.close();
			stmt.close();

			stmt = con.prepareStatement("select eid, name, game from game_event " +
				"order by eid");


			long start = System.currentTimeMillis();

			// get all events in order of eid
			eventData = new HashMap<Integer, GameEventData>(400);
			result = stmt.executeQuery();
			while (result.next()) {
                GameEventData e = new SimpleGameEventData();
				e.setEventID(result.getInt(1));
				e.setName(result.getString(2));
				e.setGame(result.getInt(3));
				eventData.put(e.getEventID(), e);
			}


			GameEventData ced = null;
			int ceid = -1;

			// associate sitedata, eventdata with data
			outer2: for (Iterator<Data> it = data.iterator(); it.hasNext();) {

				Data d = it.next();
				// associate sitedata
				outer : for (GameSiteData s : siteData) {
					if (d.siteId == s.getSiteID()) {
						d.siteData = s;
						break outer;
					}
				}
				//site not found
				if (d.siteData == null) {
					it.remove();
					continue outer2;
				}

				if (ceid == d.eventId) {
					d.eventData = ced;
				}
				else {
					ceid = d.eventId;
					ced = eventData.get(ceid);
					d.eventData = ced;
					if (ced == null) {
						it.remove();
						ceid = -1;
						continue outer2;
					}
				}


//				if (ceid == d.eventId) {
//					d.eventData = ced;
//				}
//				else {
//					ceid = d.eventId;
//					// get event name from db, associate
//					stmt.setInt(1, d.eventId);
//					result = stmt.executeQuery();
//					if (result.next()) {
//	                    d.eventData = new SimpleGameEventData();
//						d.eventData.setEventID(d.eventId);
//						d.eventData.setName(result.getString(1));
//						d.eventData.setGame(d.game);
//						ced = d.eventData;
//					}
//					else {
//						it.remove();
//						ceid = -1;
//					}
//					result.close();
//				}
			}
			log4j.debug("get eid time=" + (System.currentTimeMillis() - start));

			start = System.currentTimeMillis();
			// sort by dbid, game, site name, event name, round, section
			Collections.sort(data, new Comparator<Data>() {
				public int compare(Data d1, Data d2) {
					if (d1.dbid != d2.dbid) {
						return d1.dbid - d2.dbid;
					}
					else {
						if (d1.game != d2.game) {
							return d1.game - d2.game;
						}
						else {
							if (d1.siteData != d2.siteData) {
								return d1.siteData.getName().compareTo(
									d2.siteData.getName());
							}
							else {
								if (d1.eventData != d2.eventData) {
									return d1.eventData.getName().compareTo(
										d2.eventData.getName());
								}
								else {
									if (d1.round == null) {
										if (d2.round == null) return 1;
										else return 0;
									}
									else if (d2.round == null) {
										return 1;
									}
									else if (!d1.round.equals(d2.round)) {
										return d1.round.compareTo(d2.round);
									}
									else {
										if (d1.section == null) {
											if (d2.section == null) return 1;
											else return 0;
										}
										else if (d2.section == null) {
											return 1;
										}
										else {
											return d1.section.compareTo(d2.section);
										}
									}
								}
							}
						}
					}
				}
			});

			log4j.debug("sort time=" + (System.currentTimeMillis() - start));


/* old way
            stmt = con.prepareStatement(
                "select distinct game_event.game, game_site.sid, " +
                "game_site.name, game_site.short_name, game_site.URL, " +
                "game_event.eid, game_event.name, pente_game.round, " +                "pente_game.section " +
                "from game_site, game_event, pente_game " +
                "where game_site.sid = pente_game.site_id " +
                "and game_site.sid = game_event.site_id " +
                "and game_event.eid = pente_game.event_id " +
                "and game_event.game = pente_game.game " +
                "order by game_event.game, game_site.name, game_event.name, " +                "pente_game.round, pente_game.section");


            result = stmt.executeQuery();

            while (result.next()) {
*/
            GameDbData		lastDb = null;
			GameTreeData    lastGame = null;
            GameSiteData    lastSite = null;
            GameEventData   lastEvent = null;
            GameRoundData   lastRound = null;

			for (Data d : data) {

				int dbid = d.dbid;
				if (lastDb == null || dbid != lastDb.getID())
				{
					for (GameDbData gdb : dbData) {
						if (gdb.getID() == dbid) {
							lastDb = gdb;
							break;
						}
					}
					newTree.add(lastDb);
					lastGame = null;
					lastSite = null;
					lastEvent = null;
					lastRound = null;
				}

				int game = d.game;
                if (lastGame == null || game != lastGame.getID()) {
                    GameTreeData newGame = new SimpleGameTreeData();
                    newGame.setID(game);
                    newGame.setName(GridStateFactory.getGameName(game));

                    lastDb.addGameTreeData(newGame);
                    //newTree.addElement(newGame);
                    lastGame = newGame;
                    lastSite = null;
                    lastEvent = null;
                    lastRound = null;
                }

                int sid = d.siteId;
                if (lastSite == null || sid != lastSite.getSiteID()) {

					GameSiteData sd = (GameSiteData) d.siteData.clone();
                    lastGame.addGameSiteData(sd);
                    lastSite = sd;
                    lastEvent = null;
                    lastRound = null;
                }

                int eid = d.eventId;
                if (lastEvent == null || eid != lastEvent.getEventID()) {

                    lastSite.addGameEventData(d.eventData);
                    lastEvent = d.eventData;
                    lastRound = null;
                }

                String round = d.round;
                if (round != null && (lastRound == null || !round.equals(lastRound.getName()))) {

                    GameRoundData newRound = new SimpleGameRoundData(round);

                    lastEvent.addGameRoundData(newRound);
                    lastRound = newRound;
                }

                String section = d.section;
                if (section != null) {
                    lastRound.addGameSectionData(new SimpleGameSectionData(section));
                }

                //log4j.debug("Loaded game=" + game + " site=" + sid + " event=" +
                //    eid + " round=" + round + " section=" + section);
            }

        } finally {

            if (result != null) {
                result.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (con != null) {
                dbHandler.freeConnection(con);
            }
        }

        synchronized (tree) {
            tree = newTree;
        }

        log4j.info("MySQLGameVenueStorer.updateGameTree() done");
        log4j.info("loaded in " + (System.currentTimeMillis() - startTime));
    }

    public int getSiteID(String name) {

    	for (GameSiteData s : siteData) {
    		if (s.getName().equals(name)) return s.getSiteID();
    	}
//        synchronized (tree) {
//
//            for (int i = 0; i < tree.size(); i++) {
//                GameTreeData gameTreeData = (GameTreeData) tree.get(i);
//                for (Iterator sites = gameTreeData.getGameSiteData().iterator(); sites.hasNext();) {
//                    GameSiteData gameSiteData = (GameSiteData) sites.next();
//                    if (gameSiteData.getName().equals(name)) {
//                        return gameSiteData.getSiteID();
//                    }
//                }
//            }
//        }

        return -1;
    }

    public GameSiteData getGameSiteData(int game, int sid) {

    	for (GameSiteData s : siteData) {
    		if (s.getSiteID() == sid) return s;
    	}
//        synchronized (tree) {
//
//            GameTreeData gameTreeData = (GameTreeData) tree.get(game- 1);
//            if (gameTreeData == null) {
//                return null;
//            }
//            for (Iterator sites = gameTreeData.getGameSiteData().iterator(); sites.hasNext();) {
//                GameSiteData gameSiteData = (GameSiteData) sites.next();
//                if (gameSiteData.getSiteID() == sid) {
//                    return gameSiteData;
//                }
//            }
//        }

        return null;
    }

    public GameSiteData getGameSiteData(int game, String name) {

    	for (GameSiteData s : siteData) {
    		if (s.getName().equals(name)) return s;
    	}

//        synchronized (tree) {
//            GameTreeData gameTreeData = (GameTreeData) tree.get(game - 1);
//            if (gameTreeData == null) {
//                return null;
//            }
//            for (Iterator sites = gameTreeData.getGameSiteData().iterator(); sites.hasNext();) {
//                GameSiteData gameSiteData = (GameSiteData) sites.next();
//                if (gameSiteData.getName().equals(name)) {
//                    return gameSiteData;
//                }
//            }
//        }

        return null;
    }

    public void addGameSiteData(GameDbData db, int game, GameSiteData gameSiteData)
        throws Exception {

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;

        try {

            con = dbHandler.getConnection();

            // insert site info into db
            stmt = con.prepareStatement("insert into " + GAME_SITE_TABLE + " " +
                                        "(name, short_name, URL) " +
                                        "values(?, ?, ?)",
                        				Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, gameSiteData.getName());
            stmt.setString(2, gameSiteData.getShortSite());
            stmt.setString(3, gameSiteData.getURL());
            stmt.executeUpdate();
            result = stmt.getGeneratedKeys();
            if (result.next()) {
            	gameSiteData.setSiteID(result.getInt(1));
            }
//            // get site id for new site
//            if (stmt != null) {
//                stmt.close();
//            }
//            stmt = con.prepareStatement("select sid " +
//                                        "from " + GAME_SITE_TABLE + " " +
//                                        "where name = ?");
//            stmt.setString(1, gameSiteData.getName());
//            result = stmt.executeQuery();
//            if (result.next()) {
//                gameSiteData.setSiteID(result.getInt(1));
//            }

            // add site to data tree
            synchronized (tree) {
            	for (GameTreeData t : db.getGameTreeData()) {
            		if (t.getID() == game) {
                        t.addGameSiteData(gameSiteData);
                        break;
            		}
            	}
            }
            siteData.add(gameSiteData);

        } finally {
            if (result != null) { try { result.close(); } catch(SQLException ex) {} }
            if (stmt != null) { try { stmt.close(); } catch(SQLException ex) {} }
            if (con != null) { try { dbHandler.freeConnection(con); } catch (Exception ex) {} }
        }
    }

    public GameEventData getGameEventData(int game, int eid, String site) {

    	return eventData.get(eid);
//        GameSiteData gameSiteData = getGameSiteData(game, site);
//        if (gameSiteData == null) {
//            return null;
//        }
//
//        Vector events = gameSiteData.getGameEventData();
//        synchronized (events) {
//            for (int j = 0; j < events.size(); j++) {
//                GameEventData gameEventData = (GameEventData) events.elementAt(j);
//                if (gameEventData.getEventID() == eid) {
//                    return gameEventData;
//                }
//            }
//        }
//
//        return null;
    }

    public GameEventData getGameEventData(int game, String eventName, String site) {

    	for (GameEventData e : eventData.values()) {
    		if (e.getName().equals(eventName)) return e;
    	}
//        GameSiteData gameSiteData = getGameSiteData(game, site);
//        if (gameSiteData == null) {
//            return null;
//        }
//
//        Vector events = gameSiteData.getGameEventData();
//        synchronized (events) {
//            for (int j = 0; j < events.size(); j++) {
//                GameEventData gameEventData = (GameEventData) events.elementAt(j);
//                if (gameEventData.getName().equals(eventName)) {
//                    return gameEventData;
//                }
//            }
//        }
//
        return null;
    }

    public void addGameEventData(int game,
        GameEventData gameEventData, String site) throws Exception {

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;

        try {
            // assume the site data exists
            GameSiteData siteData = getGameSiteData(game, site);

            // insert event info into db
            con = dbHandler.getConnection();
            stmt = con.prepareStatement("insert into " + GAME_EVENT_TABLE + " " +
                                        "(name, site_id, game) " +
                                        "values(?, ?, ?)");

            stmt.setString(1, gameEventData.getName());
            stmt.setInt(2, siteData.getSiteID());
            stmt.setInt(3, game);
            stmt.executeUpdate();

            // get event id for new event
            if (stmt != null) {
                stmt.close();
            }
            stmt = con.prepareStatement("select eid " +
                                        "from " + GAME_EVENT_TABLE + " " +
                                        "where name = ? " +
                                        "and site_id = ? " +
                                        "and game = ?");
            stmt.setString(1, gameEventData.getName());
            stmt.setInt(2, siteData.getSiteID());
            stmt.setInt(3, game);
            result = stmt.executeQuery();
            if (result.next()) {
                gameEventData.setEventID(result.getInt(1));
            }

            // add event to data tree
            if (siteData != null) {
                siteData.addGameEventData(gameEventData);
            }
			eventData.put(gameEventData.getEventID(), gameEventData);

        } finally {
            if (result != null) { try { result.close(); } catch(SQLException ex) {} }
            if (stmt != null) { try { stmt.close(); } catch(SQLException ex) {} }
            if (con != null) { try { dbHandler.freeConnection(con); } catch (Exception ex) {} }
        }
    }
}