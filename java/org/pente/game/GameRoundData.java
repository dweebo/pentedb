package org.pente.game;

import java.util.*;

/** Interface for data structure to hold game round data.
 *  This interface provides a wrapper around the name of a round.  It also
 *  has methods to associate the sections played in this round and can be used
 *  to create a tree of venue information.
 *
 *  @see GameVenueStorer
 *  @see GameSectionData
 *  @see GameRoundData
 *  @author dweebo
 */
public interface GameRoundData {

    /** Useful for representing the concept of all rounds in searches */
    public static final String ALL_ROUNDS = "All Rounds";


    /** Set the name of the round
     *  @param name The round name
     */
    public void setName(String name);

    /** Get the name of the round
     *  @return String The round name
     */
    public String getName();


    /** Add a section to this round
     *  @param GameSectionData The section data
     */
    public void addGameSectionData(GameSectionData gameSectionData);

    /** Get the list of sections for this round
     *  @param Vector The list of sections
     */
    public Vector getGameSectionData();


    /** Create a copy of the data in this object in a new object
     *  @return Object A copy of this object.
     */
    public Object clone();
}