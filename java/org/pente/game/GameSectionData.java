package org.pente.game;

/** Interface for data structure to hold game section data.
 *  This interface provides a wrapper around the name of a section.
 *
 *  @see GameVenueStorer
 *  @see GameRoundData
 *  @author dweebo
 */
public interface GameSectionData {

    /** Useful for representing the concept of all sections in searches */
    public static final String ALL_SECTIONS = "All Sections";

    /** Set the name of the section
     *  @param name The section name
     */
    public void setName(String name);

    /** Get the name of the section
     *  @return String The section name
     */
    public String getName();

    /** Create a copy of the data in this object in a new object
     *  @return Object A copy of this object.
     */
    public Object clone();
}