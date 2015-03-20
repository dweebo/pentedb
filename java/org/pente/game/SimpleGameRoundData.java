package org.pente.game;

import java.util.*;

/** Simple implementation of GameRoundData
 *
 *  @author dweebo
 */
public class SimpleGameRoundData implements GameRoundData, java.io.Serializable {

    /** The name of this round */
    private String name;

    /** The list of sections in this round */
    private Vector sections;


    /** Create new round with specified name and create empty list of sections
     *  @param name The name of this round
     */
    public SimpleGameRoundData(String name) {
        setName(name);
        this.sections = new Vector();
    }
    /** Set the name of the round
     *  @param name The round name
     */
    public void setName(String name) {
        this.name = name;
    }
    public String toString() {
    	return name;
    }

    /** Get the name of the round
     *  @return String The round name
     */
    public String getName() {
        return name;
    }


    /** Add a section to this round
     *  @param GameSectionData The section data
     */
    public void addGameSectionData(GameSectionData gameSectionData) {
        sections.addElement(gameSectionData);
    }

    /** Get the list of sections for this round
     *  @param Vector The list of sections
     */
    public Vector getGameSectionData() {
        return sections;
    }


    /** Create a copy of the data in this object in a new object
     *  @return Object A copy of this object.
     */
    public Object clone() {

        SimpleGameRoundData cloned = new SimpleGameRoundData(getName());

        for (int i = 0; i < sections.size(); i++) {
            GameSectionData s = (GameSectionData) sections.elementAt(i);
            cloned.addGameSectionData((GameSectionData) s.clone());
        }

        return cloned;
    }
}