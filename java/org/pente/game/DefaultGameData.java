package org.pente.game;

import java.util.*;

/** Default implementation of GameData.
 *  @author dweebo
 *  @version 0.2 02/12/2001
 */
public class DefaultGameData implements GameData {

    /** Name of the game */
    protected String        game;

    /** Game unique id */
    protected long          gameID;

    /** Name of the site */
    protected String        site;

    /** Short name of the site */
    protected String        shortSite;

    /** www URL of a site */
    protected String        siteURL;

    /** Name of the event */
    protected String        event;

    /** Name of the round */
    protected String        round;

    /** Name of the section */
    protected String        section;

    /** Date of the game */
    protected Date          date;

    /** True if the game was timed */
    protected boolean       timed;

    /** The initial time allowed for the game */
    protected int           initialTime;

    /** The incremental time added after each move */
    protected int           incrementalTime;

    /** True if the game was rated */
    protected boolean       rated;

    /** Player 1 info */
    protected PlayerData    player1Data;

    /** Player 2 info */
    protected PlayerData    player2Data;

    /** Winner of the game */
    protected int           winner;

    /** Moves of the game */
    protected Vector        gameMoves;

    /** Did players swap */
    protected boolean       swapped;

    /** Is game private */
    protected boolean		privateGame;

    private long sid;
    private String status;

	private boolean editable;
	private boolean stored;

    /** Initialize moves vector, set winner to unkown */
    public DefaultGameData() {
        gameMoves = new Vector();

        player1Data = new DefaultPlayerData();
        player2Data = new DefaultPlayerData();
        winner = UNKNOWN;
    }

    /** Set the name of the game
     *  @param game The game name
     */
    public void setGame(String game) {
        this.game = game;
    }

    /** Set the name of the game
     *  @param game The game name
     */
    public String getGame() {
        return game;
    }


    /** Set the game unique identifier
     *  @param id The unique game id
     */
    public void setGameID(long id) {
        this.gameID = id;
    }

    /** Get the game unique identifier
     *  @return long The unique game id
     */
    public long getGameID() {
        return gameID;
    }


    /** Set the name of the site the game was played at
     *  @param site The name of the site
     */
    public void setSite(String site) {
        this.site = site;
    }

    /** Get the name of the site the game was played at
     *  @return String The name of the site
     */
    public String getSite() {
        return site;
    }


    /** Set the short name of the site the game was played at
     *  @param site The short name of the site
     */
    public void setShortSite(String site) {
        this.shortSite = site;
    }

    /** Get the short name of the site the game was played at
     *  @return String The short name of the site
     */
    public String getShortSite() {
        return shortSite;
    }


    /** Set the URL of the site the game was played at
     *  @param URL The URL of the site
     */
    public void setSiteURL(String URL) {
        this.siteURL = URL;
    }

    /** Get the URL of the site the game was played at
     *  @return String The URL of the site
     */
    public String getSiteURL() {
        return siteURL;
    }


    /** Set the name of the event the game was played in
     *  @param event The name of the event
     */
    public void setEvent(String event) {
        this.event = event;
    }

    /** Get the name of the event the game was played in
     *  @return String The name of the event
     */
    public String getEvent() {
        return event;
    }


    /** Set the round the game was played in
     *  @param round The name of the round
     */
    public void setRound(String round) {
        this.round = round;
    }

    /** Get the round the game was played in
     *  @return String The name of the round
     */
    public String getRound() {
        return round;
    }


    /** Set the section the game was played in
     *  @param round The name of the section
     */
    public void setSection(String section) {
        this.section = section;
    }

    /** Get the section the game was played in
     *  @return String The name of the section
     */
    public String getSection() {
        return section;
    }


    /** Set the date the game was played on
     *  @param date The date
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /** Get the date the game was played on
     *  @return Date The date
     */
    public Date getDate() {
        return date;
    }


    /** Set whether or not the game was timed
     *  @param timed Timed flag
     */
    public void setTimed(boolean timed) {
        this.timed = timed;
    }

    /** Get whether or not the game was timed
     *  @return boolean Timed flag
     */
    public boolean getTimed() {
        return timed;
    }


    /** Set the initial time allowed for the game
     *  @param initialTime The initial time
     */
    public void setInitialTime(int initialTime) {
        this.initialTime = initialTime;
    }

    /** Get the initial time allowed for the game
     *  @return int The initial time
     */
    public int getInitialTime() {
        return initialTime;
    }


    /** Set the incremental time allowed for the game
     *  @param incrementalTime The incremental time
     */
    public void setIncrementalTime(int incrementalTime) {
        this.incrementalTime = incrementalTime;
    }

    /** Get the incremental time allowed for the game
     *  @return int The incremental time
     */
    public int getIncrementalTime() {
        return incrementalTime;
    }


    /** Set whether or not the game was rated
     *  @param rated Rated flag
     */
    public void setRated(boolean rated) {
        this.rated = rated;
    }

    /** Get whether or not the game was rated
     *  @return boolean Rated flag
     */
    public boolean getRated() {
        return rated;
    }


    /** Set the 1st players info
     *  @param player The 1st players info
     */
    public void setPlayer1Data(PlayerData playerData) {
        this.player1Data = playerData;
    }

    /** Get the 1st players info
     *  @return PlayerData The 1st players info
     */
    public PlayerData getPlayer1Data() {
        return player1Data;
    }


    /** Set the 2nd players info
     *  @param player The 2nd players info
     */
    public void setPlayer2Data(PlayerData playerData) {
        this.player2Data = playerData;
    }

    /** Get the 2nd players info
     *  @return PlayerData The 2nd players info
     */
    public PlayerData getPlayer2Data() {
        return player2Data;
    }

    public boolean isVsComputer() {
        return player1Data.getType() == PlayerData.COMPUTER ||
               player2Data.getType() == PlayerData.COMPUTER;
    }


    /** Set the winner of the game
     *  @param winner One of the constants above
     */
    public void setWinner(int winner) {
        this.winner = winner;
    }

    /** Get the winner of the game
     *  @return int One of the constants above
     */
    public int getWinner() {
        return winner;
    }

    /** Set whether or not the players swapped in the game (D-Pente)
     *  @param swapped True if players swapped.
     */
    public void setSwapped(boolean swapped) {
        this.swapped = swapped;
    }

    /** Return whether or not players swapped in the game (D-Pente)
     *  @return boolean True if players swapped
     */
    public boolean didPlayersSwap() {
        return swapped;
    }


    /** Add a move for this game
     *  @param move An integer representation of a move
     */
    public void addMove(int move) {
        gameMoves.addElement(new Integer(move));
    }

    /** Undo the last move */
    public void undoMove() {
        gameMoves.removeElementAt(gameMoves.size() - 1);
    }

    /** Get a move for this game
     *  @param num The sequence number of the move
     */
    public int getMove(int num) {
        return ((Integer)gameMoves.elementAt(num)).intValue();
    }

    /** Get the number of moves for this game
     *  @return The number of moves
     */
    public int getNumMoves() {
        return gameMoves.size();
    }

    public int[] getMoves() {
        int m[] = new int[getNumMoves()];
        for (int i = 0; i < m.length; i++) {
            m[i] = getMove(i);
        }
        return m;
    }

    /** Determine if this game data equals another game data
     *  @param obj The object to compare this to
     *  @return boolean True if the obj equals this game data
     */
    public boolean equals(Object obj) {

        // make sure the object to compare is an implementation
        // of GameData
        if (!(obj instanceof GameData)) {
            return false;
        }

        GameData data = (GameData) obj;

        if (!nullSafeEquals(getDate(), data.getDate())) {
            return false;
        }

        if (!nullSafeEquals(getEvent(), data.getEvent())) {
            return false;
        }

        if (!nullSafeEquals(getGame(), data.getGame())) {

            return false;
        }

        if (getIncrementalTime() != data.getIncrementalTime()) {
            return false;
        }

        if (getInitialTime() != data.getInitialTime()) {
            return false;
        }

        if (getNumMoves() != data.getNumMoves()) {
            return false;
        }
        for (int i = 0; i < getNumMoves(); i++) {
            if (getMove(i) != data.getMove(i)) {
                return false;
            }
        }

        if (!nullSafeEquals(getPlayer1Data().getUserIDName(), data.getPlayer1Data().getUserIDName())) {
            return false;
        }

        if (!nullSafeEquals(getPlayer2Data().getUserIDName(), data.getPlayer2Data().getUserIDName())) {
            return false;
        }

        if (getRated() != data.getRated()) {
            return false;
        }

        if (!nullSafeEquals(getRound(), data.getRound())) {
            return false;
        }

        if (!nullSafeEquals(getSection(), data.getSection())) {
            return false;
        }

        if (!nullSafeEquals(getSite(), data.getSite())) {
            return false;
        }

        if (getTimed() != data.getTimed()) {
            return false;
        }

        if (getWinner() != data.getWinner()) {
            return false;
        }

        return true;
    }

    /** Helper method to check if to objects are equals taking into account that
     *  1 or the other could be null.
     *  @param obj1 The first object
     *  @param obj2 The second object
     *  @return boolean True if the objects are equal.
     */
    protected boolean nullSafeEquals(Object obj1, Object obj2) {

        if (obj1 == null) {
            return obj2 == null;
        }
        else {
            return obj1.equals(obj2);
        }
    }

	public boolean isPrivateGame() {
		return privateGame;
	}

	public void setPrivateGame(boolean privateGame) {
		this.privateGame = privateGame;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public boolean isStored() {
		return stored;
	}

	public void setStored(boolean stored) {
		this.stored = stored;
	}

	public long getSid() {
		return sid;
	}

	public void setSid(long sid) {
		this.sid = sid;
	}

    public String getStatus() {
    	return status;
    }
    public void setStatus(String status) {
    	this.status = status;
    }

    private List<Time> moveTimes;
    public void setMoveTimes(List<Time> times) {
    	this.moveTimes = times;
    }
    public List<Time> getMoveTimes() {
    	return moveTimes;
    }
}