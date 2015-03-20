package org.pente.game;

import java.util.*;

/** Interface for data structure to hold game event data.
 *  This interface provides methods to map a event name to an event id.  It also
 *  has methods to associate the rounds played in this event and can be used
 *  to create a tree of venue information.
 *
 *  @author dweebo
 */
public interface GameEventData {

    /** Useful for representing the concept of all events in searches */
    public static final String ALL_EVENTS = "All Events";


    /** Set the name of the event
     *  @param name The event name
     */
    public void setName(String name);

    /** Get the name of the event
     *  @return String The event name
     */
    public String getName();

    /** Set the event id
     *  @param eventID The id of the event
     */
    public void setEventID(int eventID);

    /** Get the event id
     *  @return int The event id
     */
    public int getEventID();

    public void setGame(int game);
    public int getGame();

    /** Add a round to this event
     *  @param GameRoundData The round data
     */
    public void addGameRoundData(GameRoundData gameRoundData);

    /** Get the list of rounds for this event
     *  @param Vector The list of rounds
     */
    public Vector getGameRoundData();

    public void clearGameRoundData();

    /** Create a copy of the data in this object in a new object
     *  @return Object A copy of this object.
     */
    public Object clone();

    public void setSiteData(GameSiteData siteData);
    public GameSiteData getSiteData();
}