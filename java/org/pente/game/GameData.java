package org.pente.game;

import java.util.*;

/** Interface for data structure that holds information on a game.  Written
 *  originally to handle Pro Pente games at iyt.  Probably can be used for
 *  other games, and other sites.
 *  @author dweebo
 *  @version 0.2 02/12/2001
 */
public interface GameData extends MoveData {

    /** Set the name of the game
     *  @param game The game name
     */
    public void setGame(String game);

    /** Get the name of the game
     *  @return String The name of the game
     */
    public String getGame();


    /** Set the game unique identifier
     *  @param id The unique game id
     */
    public void setGameID(long id);

    /** Get the game unique identifier
     *  @return long The unique game id
     */
    public long getGameID();


    /** Set the name of the site the game was played at
     *  @param site The name of the site
     */
    public void setSite(String site);

    /** Get the name of the site the game was played at
     *  @return String The name of the site
     */
    public String getSite();


    /** Set the short name of the site the game was played at
     *  @param site The short name of the site
     */
    public void setShortSite(String site);

    /** Get the short name of the site the game was played at
     *  @return String The short name of the site
     */
    public String getShortSite();


    /** Set the URL of the site the game was played at
     *  @param URL The URL of the site
     */
    public void setSiteURL(String URL);

    /** Get the URL of the site the game was played at
     *  @return String The URL of the site
     */
    public String getSiteURL();


    /** Set the name of the event the game was played in
     *  @param event The name of the event
     */
    public void setEvent(String event);

    /** Get the name of the event the game was played in
     *  @return String The name of the event
     */
    public String getEvent();


    /** Set the round the game was played in
     *  @param round The name of the round
     */
    public void setRound(String round);

    /** Get the round the game was played in
     *  @return String The name of the round
     */
    public String getRound();


    /** Set the section the game was played in
     *  @param round The name of the section
     */
    public void setSection(String section);

    /** Get the section the game was played in
     *  @return String The name of the section
     */
    public String getSection();


    /** Set the date the game was played on
     *  @param date The date
     */
    public void setDate(Date date);

    /** Get the date the game was played on
     *  @return Date The date
     */
    public Date getDate();


    /** Set whether or not the game was timed
     *  @param timed Timed flag
     */
    public void setTimed(boolean timed);

    /** Get whether or not the game was timed
     *  @return boolean Timed flag
     */
    public boolean getTimed();


    /** Set the initial time allowed for the game
     *  @param initialTime The initial time
     */
    public void setInitialTime(int initialTime);

    /** Get the initial time allowed for the game
     *  @return int The initial time
     */
    public int getInitialTime();


    /** Set the incremental time allowed for the game
     *  @param incrementalTime The incremental time
     */
    public void setIncrementalTime(int incrementalTime);

    /** Get the incremental time allowed for the game
     *  @return int The incremental time
     */
    public int getIncrementalTime();


    /** Set whether or not the game was rated
     *  @param rated Rated flag
     */
    public void setRated(boolean rated);

    /** Get whether or not the game was rated
     *  @return boolean Rated flag
     */
    public boolean getRated();


    /** Set the 1st players info
     *  @param player The 1st players info
     */
    public void setPlayer1Data(PlayerData player);

    /** Get the 1st players info
     *  @return PlayerData The 1st players info
     */
    public PlayerData getPlayer1Data();


    /** Set the 2nd players info
     *  @param player The 2nd players info
     */
    public void setPlayer2Data(PlayerData player);

    /** Get the 2nd players info
     *  @return PlayerData The 2nd players info
     */
    public PlayerData getPlayer2Data();

    /** Determine if the game was vs a computer
     *  @return boolean True if player 1 or player 2 is a computer
     */
    public boolean isVsComputer();

    /** If the game isn't over */
    public final int UNKNOWN = 0;
    /** If the game was won by player 1 */
    public final int PLAYER1 = 1;
    /** If the game was won by player 2 */
    public final int PLAYER2 = 2;
    /** If the game was a draw */
    public final int DRAW = 3;

    /** Set the winner of the game
     *  @param winner One of the constants above
     */
    public void setWinner(int winner);

    /** Get the winner of the game
     *  @return int One of the constants above
     */
    public int getWinner();

    /** Set whether or not the players swapped in the game (D-Pente)
     *  @param swapped True if players swapped.
     */
    public void setSwapped(boolean swapped);

    /** Return whether or not players swapped in the game (D-Pente)
     *  @return boolean True if players swapped
     */
    public boolean didPlayersSwap();


	public boolean isPrivateGame();
	public void setPrivateGame(boolean privateGame);

    /** Determine if this game data equals another game data
     *  @param obj The object to compare this to
     *  @return boolean True if the obj equals this game data
     */
    public boolean equals(Object obj);

    public void setSid(long sid);
    public long getSid();

    public static final String STATUS_TIMEOUT = "T";
    public static final String STATUS_WIN = "W";
    public static final String STATUS_RESIGN = "R";
    public static final String STATUS_FORCE_RESIGN = "F";
    public String getStatus();
    public void setStatus(String status);

    public void setMoveTimes(List<Time> times);
    public List<Time> getMoveTimes();

//	public boolean isEditable();
//
//	public void setEditable(boolean editable);
//
//	public boolean isStored();
//
//	public void setStored(boolean stored);
}