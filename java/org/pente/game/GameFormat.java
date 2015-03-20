package org.pente.game;

import java.text.*;

/** Interface to classes that know how to format and parse games from/into
 *  GameData information.
 *  @author dweebo
 *  @version 0.2 02/12/2001
 */
public interface GameFormat extends ObjectFormat {

    /** Format the game data into a buffer
     *  @param data The game data
     *  @param buffer The buffer to format into
     *  @return StringBuffer The buffer containing the formatted game
     */
    public StringBuffer format(Object data, StringBuffer buffer);

    /** Parse the game data from a buffer
     *  @param data The game data to parse into
     *  @param buffer The buffer to parse from
     *  @return Object The game data parsed
     *  @exception ParseException If the game cannot be parsed
     */
    public Object parse(Object data, StringBuffer buffer) throws ParseException;
}