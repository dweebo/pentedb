package org.pente.game;

/** Simple implementation of GameSectionData
 *
 *  @author dweebo
 */
public class SimpleGameSectionData implements GameSectionData, java.io.Serializable {

    /** The name of this section */
    private String name;

    /** Create with this name
     *  @param name The name of this section
     */
    public SimpleGameSectionData(String name) {
        setName(name);
    }

    /** Set the name of the section
     *  @param name The section name
     */
    public void setName(String name) {
        this.name = name;
    }

    /** Get the name of the section
     *  @return String The section name
     */
    public String getName() {
        return name;
    }

    /** Create a copy of the data in this object in a new object
     *  @return Object A copy of this object.
     */
    public Object clone() {
        return new SimpleGameSectionData(getName());
    }
}