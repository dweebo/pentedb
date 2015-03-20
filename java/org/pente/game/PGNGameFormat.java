package org.pente.game;

import java.io.*;
import java.util.*;
import java.text.*;

import org.pente.gameDatabase.swing.*;

/** A game formatter that formats/parses games in a PNG like format.  PNG stands
 *  for Portable Game Notation and is used mostly for online chess games.  I have
 *  adapted the standard somewhat to work for pente games.
 *  @since 0.2
 *  @author dweebo
 *  @version 0.2 02/12/2001
 */
public class PGNGameFormat implements GameFormat {

    /** Header name for game name */
    protected static final String   HEADER_GAME =               "Game";

    /** Header name for site */
    protected static final String   HEADER_SITE =               "Site";

    /** Header name for event */
    protected static final String   HEADER_EVENT =              "Event";

    /** Header name for round */
    protected static final String   HEADER_ROUND =              "Round";

    /** Header name for section */
    protected static final String   HEADER_SECTION =            "Section";

    /** Header name for date */
    protected static final String   HEADER_DATE =               "Date";

    /** Header name for time */
    protected static final String   HEADER_TIME =               "Time";

    /** Header name for time control */
    protected static final String   HEADER_TIME_CONTROL =       "TimeControl";

    /** Header name for rated */
    protected static final String   HEADER_RATED =              "Rated";

    /** Header name for player 1 name */
    protected static final String   HEADER_PLAYER_1_NAME =      "Player 1 Name";

    /** Header name for player 1 rating */
    protected static final String   HEADER_PLAYER_1_RATING =    "Player 1 Rating";

    /** Header name for player 1 type */
    protected static final String   HEADER_PLAYER_1_TYPE =      "Player 1 Type";

    /** Header name for player 2 name */
    protected static final String   HEADER_PLAYER_2_NAME =      "Player 2 Name";

    /** Header name for player 2 rating */
    protected static final String   HEADER_PLAYER_2_RATING =    "Player 2 Rating";

    /** Header name for player 2 type */
    protected static final String   HEADER_PLAYER_2_TYPE =      "Player 2 Type";

    /** Header name for result */
    protected static final String   HEADER_RESULT =             "Result";

    /** Header name for swapped */
    protected static final String   HEADER_SWAPPED =            "Player's Swapped?";

    /** Formatting string to format/parse dates */
    protected String   dateFormatStr =               "MM/dd/yyyy";
    protected String   dateFormatStr2=				 "yyyy.MM.dd";
    /** Formatting string to format/parse times */
    protected static final String   TIME_FORMAT =               "HH:mm:ss";

    /** The line separator used to format games */
    private String      lineSeparator;

    /** Use a default line separator */
    public PGNGameFormat() {
        this("\r\n");
    }
	public PGNGameFormat(String lineSeparator, String dateFormatStr) {
		this.lineSeparator = lineSeparator;
		this.dateFormatStr = dateFormatStr;
	}

    /** Specify the line seperator used to format games
     *  @param lineSeparator The line separator
     */
    public PGNGameFormat(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    public static void main(String args[]) throws Exception {

    /*
        String key = HEADER_PLAYER_1_NAME;//args[0];
        String value = "Pente Champ";//args[1];

        PNGGameFormat gameFormat = new PNGGameFormat("\n");

        String header = gameFormat.formatHeader(key, value);
        System.out.println(header);
        Hashtable headers = new Hashtable();
        gameFormat.parseHeader(header, headers);

        Enumeration keys = headers.keys();
        while (keys.hasMoreElements()) {
            String k = (String) keys.nextElement();
            String v = (String) headers.get(k);
            System.out.println("parsed " + k + " = " + v);
        }
    */
    	/*
        String fileName = args[0];
        FileReader reader = new FileReader(fileName);
        PGNGameFormat gameFormat = new PGNGameFormat("\n");
        GameData gameData = new DefaultGameData();

        StringBuffer buffer = new StringBuffer();
        char chars[];

        // read game into a StringBuffer
        while (true) {

            chars = new char[1024];
            int length = reader.read(chars);
            if (length == -1) {
                break;
            }
            else {
                buffer.append(chars);
            }
        }

        gameFormat.parse(gameData, buffer);

        buffer = new StringBuffer();
        gameFormat.format(gameData, buffer);
        System.out.print(buffer.toString());
        */
    }

    private GameData convertObject(Object obj) {

        if (obj == null) {
            return null;
        }
        else if (!(obj instanceof GameData)) {
            throw new IllegalArgumentException("Object not GameData");
        }

        return (GameData) obj;
    }

    /** Format the game data into a buffer
     *  @param data The game data
     *  @param buffer The buffer to format into
     *  @return StringBuffer The buffer containing the formatted game
     */
    public StringBuffer format(Object obj, StringBuffer buffer) {

    	GameData data = convertObject(obj);

        // print headers
        buffer.append(formatGame(data) + lineSeparator);
        buffer.append(formatSite(data) + lineSeparator);
        buffer.append(formatEvent(data) + lineSeparator);
        buffer.append(formatRound(data) + lineSeparator);
        buffer.append(formatSection(data) + lineSeparator);
        buffer.append(formatDate(data) + lineSeparator);
        buffer.append(formatTime(data) + lineSeparator);
        buffer.append(formatTimeControl(data) + lineSeparator);
        buffer.append(formatRated(data) + lineSeparator);
        buffer.append(formatPlayer1Name(data) + lineSeparator);
        buffer.append(formatPlayer2Name(data) + lineSeparator);
        buffer.append(formatPlayer1Rating(data) + lineSeparator);
        buffer.append(formatPlayer2Rating(data) + lineSeparator);
        buffer.append(formatPlayer1Type(data) + lineSeparator);
        buffer.append(formatPlayer2Type(data) + lineSeparator);
        buffer.append(formatResult(data, true) + lineSeparator);
        if (data.getGame().equals(GridStateFactory.DPENTE_GAME.getName()) ||
            data.getGame().equals(GridStateFactory.SPEED_DPENTE_GAME.getName())) {
            buffer.append(formatSwapped(data) + lineSeparator);
        }
        buffer.append(lineSeparator);

        // print move list
        String moveList = "";

        int j = 1;
        for(int i = 0; i < data.getNumMoves(); i++) {

            int move = data.getMove(i);
            String moveStr = formatCoordinates(move);

            // whites move
            if(i % 2 == 0) {
                moveList += Integer.toString(j++) + ". ";
            }
            moveList += moveStr + " ";

        }
        moveList += formatResult(data, false);

        // move list lines can't be longer than 80 chars
        while (moveList.length() > 80) {
            int index = moveList.lastIndexOf(' ', 80);
            buffer.append(moveList.substring(0, index) + lineSeparator);
            moveList = moveList.substring(index + 1);
        }

        buffer.append(moveList + lineSeparator);

        return buffer;
    }

    public GameData parse(File f) throws IOException, ParseException {
    	return parse(new FileInputStream(f));
    }
    public PlunkGameData parse(InputStream in) throws IOException, ParseException {

    	PlunkGameData gameData  = null;

        try {
	    	// read game into a StringBuffer
	        StringBuffer buffer = new StringBuffer(new String(
	        	org.pente.gameDatabase.swing.Utilities.readStream(in)));

	        // parse the game data
	        gameData = new PlunkGameData();
	        gameData = (PlunkGameData) parse(gameData, buffer);

        }
        finally {
        	if (in != null) {
        		in.close();
        	}
        }

        return gameData;
    }

    public PlunkGameData parse(StringBuffer s) throws ParseException {
    	PlunkGameData gameData = new PlunkGameData();
        gameData = (PlunkGameData) parse(gameData, s);

        return gameData;
    }
    /** Parse the game data from a buffer
     *  @param data The game data to parse into
     *  @param buffer The buffer to parse from
     *  @return Object The game data parsed
     *  @exception ParseException If the game cannot be parsed
     */
    public Object parse(Object obj, StringBuffer buffer) throws ParseException {

    	GameData data = convertObject(obj);

        try {

            String bufferString = buffer.toString().trim();

            // parse out headers
            Hashtable headers = new Hashtable();
            int beginLineIndex = 0;
            boolean parsingHeaders = true;
            boolean moreLines = true;
            int tokenNum = 0;
            while (moreLines) {

                int endLineIndex = bufferString.indexOf(lineSeparator, beginLineIndex);
                if (endLineIndex == -1) {
                    endLineIndex = bufferString.length();
                    moreLines = false;
                }
                String line = bufferString.substring(beginLineIndex, endLineIndex);
                beginLineIndex = endLineIndex + lineSeparator.length();

                // parse out header names and values
                if (parsingHeaders) {
                    try {
                        parseHeader(line, headers);
                    } catch(ParseException ex) {
                        parsingHeaders = false;
                    }
                }
                // process moves
                else {
                    StringTokenizer moveTokenizer = new StringTokenizer(line, " ");
                    while (moveTokenizer.hasMoreTokens()) {
                        String move = moveTokenizer.nextToken();
                        if (tokenNum++ % 3 != 0) {
                            int moveInt = parseCoordinates(move);
                            if (moveInt != -1) {
                                data.addMove(moveInt);
                            }
                        }
                    }
                }
            }

            // parse individual headers
            String game = (String) headers.get(HEADER_GAME);
            if (game == null) {
            	game = (String) headers.get("GameType");//marks
            }
            parseGame(game, data);
            parseSite((String) headers.get(HEADER_SITE), data);
            parseEvent((String) headers.get(HEADER_EVENT), data);
            parseRound((String) headers.get(HEADER_ROUND), data);
            parseSection((String) headers.get(HEADER_SECTION), data);
            parseDate((String) headers.get(HEADER_DATE), data);
            parseTime((String) headers.get(HEADER_TIME), data);
            parseTimeControl((String) headers.get(HEADER_TIME_CONTROL), data);
            parseRated((String) headers.get(HEADER_RATED), data);
            String p1 = (String) headers.get(HEADER_PLAYER_1_NAME);
            if (p1 == null) {
            	p1 = (String) headers.get("White");
            }
            parsePlayer1Name(p1, data);
            String p1R = (String) headers.get(HEADER_PLAYER_1_RATING);
            if (p1R == null) {
            	p1R = (String) headers.get("WhiteRating");
            }
            parsePlayer1Rating(p1R, data);
            parsePlayer1Type((String) headers.get(HEADER_PLAYER_1_TYPE), data);
            String p2 = (String) headers.get(HEADER_PLAYER_2_NAME);
            if (p2 == null) {
            	p2 = (String) headers.get("Black");
            }
            parsePlayer2Name(p2, data);
            String p2R = (String) headers.get(HEADER_PLAYER_2_RATING);
            if (p2R == null) {
            	p2R = (String) headers.get("BlackRating");
            }
            parsePlayer2Rating(p2R, data);
            parsePlayer2Type((String) headers.get(HEADER_PLAYER_2_TYPE), data);
            parseResult((String) headers.get(HEADER_RESULT), data);

        } catch(Exception ex) {
            //ex.printStackTrace();
            throw new ParseException("Parse Exception", 0);
        }

        return data;
    }


    /** Format a string representation of the game
     *  @param data The game data
     *  @return String The game
     */
    protected String formatGame(GameData data) {
        return formatHeader(HEADER_GAME, data.getGame());
    }

    /** Parse a string representation of the game
     *  @param game The game
     *  @param data The game data
     */
    protected void parseGame(String game, GameData data) {
        data.setGame(game);
    }


    /** Format a string representation of the site
     *  @param data The game data
     *  @return String The site
     */
    protected String formatSite(GameData data) {
        return formatHeader(HEADER_SITE, data.getSite());
    }

    /** Parse a string representation of the site
     *  @param site The site
     *  @param data The game data
     */
    protected void parseSite(String site, GameData data) {
        data.setSite(site);
    }


    /** Format a string representation of the event
     *  @param data The game data
     *  @return String The event
     */
    protected String formatEvent(GameData data) {
        return formatHeader(HEADER_EVENT, (data.getEvent() == null ? "-" : data.getEvent()));
    }

    /** Parse a string representation of the event
     *  @param event The event
     *  @param data The game data
     */
    protected void parseEvent(String event, GameData data) {
        data.setEvent(event);
    }


    /** Format a string representation of the round
     *  @param data The game data
     *  @return String The round
     */
    protected String formatRound(GameData data) {
        return formatHeader(HEADER_ROUND, (data.getRound() == null ? "-" : data.getRound()));
    }

    /** Parse a string representation of the round
     *  @param round The round
     *  @param data The game data
     */
    protected void parseRound(String round, GameData data) {

        if (round != null && round.equals("-")) {
            round = null;
        }
        data.setRound(round);
    }


    /** Format a string representation of the section
     *  @param data The game data
     *  @return String The section
     */
    protected String formatSection(GameData data) {
        return formatHeader(HEADER_SECTION, (data.getSection() == null ? "-" : data.getSection()));
    }

    /** Parse a string representation of the section
     *  @param section The section
     *  @param data The game data
     */
    protected void parseSection(String section, GameData data) {

        if (section != null && section.equals("-")) {
            section = null;
        }
        data.setSection(section);
    }


    /** Format a string representation of the date
     *  @param data The game data
     *  @return String The date
     */
    protected String formatDate(GameData data) {
        StringBuffer date = new StringBuffer();
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
        return formatHeader(HEADER_DATE, dateFormat.format(data.getDate(), date, new FieldPosition(0)).toString());
    }

    /** Parse a string representation of the date
     *  @param date The date
     *  @param data The game data
     *  @param ParseException Thrown by DateFormat
     */
    protected void parseDate(String date, GameData data) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStr);

        Date timeDate = data.getDate();
        Date dateDate = null;
        try {
        	dateDate = dateFormat.parse(date);
        } catch (ParseException e) {
        	dateDate = new SimpleDateFormat(dateFormatStr2).parse(date);
        }

        // if already parsed time, add time to date
        if (timeDate != null) {
            Calendar dateCalendar = new GregorianCalendar();
            dateCalendar.setTime(dateDate);

            Calendar timeCalendar = new GregorianCalendar();
            timeCalendar.setTime(timeDate);

            dateCalendar.add(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
            dateCalendar.add(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
            dateCalendar.add(Calendar.SECOND, timeCalendar.get(Calendar.SECOND));

            data.setDate(dateCalendar.getTime());
        }
        else {
            data.setDate(dateDate);
        }
    }


    /** Format a string representation of the time
     *  @param data The game data
     *  @return String The time
     */
    protected String formatTime(GameData data) {
        StringBuffer time = new StringBuffer();
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
        return formatHeader(HEADER_TIME, timeFormat.format(data.getDate(), time, new FieldPosition(0)).toString());
    }

    /** Parse a string representation of the time
     *  @param date The time
     *  @param data The game data
     *  @param ParseException Thrown by DateFormat
     */
    protected void parseTime(String time, GameData data) throws ParseException {

        if (time == null) return;

        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);

        Date dateDate = data.getDate();
        Date timeDate = timeFormat.parse(time);

        // if already parsed time, add time to date
        if (dateDate != null) {

            Calendar dateCalendar = new GregorianCalendar();
            dateCalendar.setTime(dateDate);

            Calendar timeCalendar = new GregorianCalendar();
            timeCalendar.setTime(timeDate);

            dateCalendar.add(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
            dateCalendar.add(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
            dateCalendar.add(Calendar.SECOND, timeCalendar.get(Calendar.SECOND));

            data.setDate(dateCalendar.getTime());
        }
        else {
            data.setDate(timeDate);
        }
    }


    /** Format a string representation of the time control
     *  @param data The game data
     *  @return String The time control
     */
    protected String formatTimeControl(GameData data) {
        String timeControl = "-";
        if (!data.getTimed()) timeControl = "-";
        else if (data.getIncrementalTime() == 0) timeControl = Integer.toString(data.getInitialTime());
        else timeControl = Integer.toString(data.getInitialTime()) + "+" + Integer.toString(data.getIncrementalTime());

        return formatHeader(HEADER_TIME_CONTROL, timeControl);
    }

    /** Parse a string representation of the time control
     *  @return timeControl The time control
     *  @param data The game data
     */
    protected void parseTimeControl(String timeControl, GameData data) {

        if (timeControl != null) {

            // added for games created by mark at bk
            if (timeControl.endsWith("day")) {
                timeControl = timeControl.substring(0, timeControl.length() - 3).trim();
            }
            if (timeControl.endsWith("days")) {
                timeControl = timeControl.substring(0, timeControl.length() - 4).trim();
            }

            if (timeControl.equals("-")) {
                timeControl = null;
            }
            else {
                int plusIndex = timeControl.indexOf("+");
                if (plusIndex != -1) {
                    data.setInitialTime(Integer.parseInt(timeControl.substring(0, plusIndex)));
                    data.setIncrementalTime(Integer.parseInt(timeControl.substring(plusIndex + 1)));
                }
                else {

                    data.setInitialTime(Integer.parseInt(timeControl));
                }
            }
        }

        data.setTimed(timeControl != null);
    }

    /** Format a string representation of the rated flag
     *  @param data The game data
     *  @return String The rated flag
     */
    protected String formatRated(GameData data) {

        String rated = "Y";
        if (!data.getRated()) {
            rated = "N";
        }

        return formatHeader(HEADER_RATED, rated);
    }

    /** Parse a string representation of the rated flag
     *  @param rated The rated string
     *  @param data The game data
     */
    protected void parseRated(String rated, GameData data) {

        if (rated == null) {
            data.setRated(true);
        }
        else {
            if (rated.equals("Y")) {
                data.setRated(true);
            }
            else {
                data.setRated(false);
            }
        }
    }

    /** Format a string representation of the 1st players name
     *  @param data The game data
     *  @return String The 1st players name
     */
    protected String formatPlayer1Name(GameData data) {
        return formatHeader(HEADER_PLAYER_1_NAME, (data.getPlayer1Data() == null ? "?" : data.getPlayer1Data().getUserIDName()));
    }

    /** Format a string representation of the 1st players name
     *  @return player1Name The 1st players name
     *  @param data The game data
     */
    protected void parsePlayer1Name(String player1Name, GameData data) {
        if (player1Name != null && player1Name.equals("?")) {
            player1Name = null;
        }

        data.getPlayer1Data().setUserIDName(player1Name);
    }


    /** Format a string representation of the 2nd players name
     *  @param data The game data
     *  @return String The 2nd players name
     */
    protected String formatPlayer2Name(GameData data) {
        return formatHeader(HEADER_PLAYER_2_NAME, (data.getPlayer2Data() == null ? "?" : data.getPlayer2Data().getUserIDName()));
    }

    /** Format a string representation of the 2nd players name
     *  @return player2Name The 2nd players name
     *  @param data The game data
     */
    protected void parsePlayer2Name(String player2Name, GameData data) {
        if (player2Name != null && player2Name.equals("?")) {
            player2Name = null;
        }

        data.getPlayer2Data().setUserIDName(player2Name);
    }


    /** Format a string representation of the 1st players rating
     *  @param data The game data
     *  @return String The 1st players rating
     */
    protected String formatPlayer1Rating(GameData data) {
        String player1Rating = "?";
        if (data.getPlayer1Data() != null) {
            player1Rating = data.getPlayer1Data().getRating() == 0 ? "?" : Integer.toString(data.getPlayer1Data().getRating());
        }

        return formatHeader(HEADER_PLAYER_1_RATING, player1Rating);
    }

    /** Parse a string representation of the 1st players rating
     *  @return player1Rating The 1st players rating
     *  @param data The game data
     */
    protected void parsePlayer1Rating(String player1Rating, GameData data) {

        if (player1Rating != null && (player1Rating.equals("?") || player1Rating.equals("-"))) {
            player1Rating = null;
        }

        if (player1Rating != null) {
            data.getPlayer1Data().setRating(Integer.parseInt(player1Rating));
        }
    }


    /** Format a string representation of the 2nd players rating
     *  @param data The game data
     *  @return String The 2nd players rating
     */
    protected String formatPlayer2Rating(GameData data) {
        String player2Rating = "?";
        if (data.getPlayer2Data() != null) {
            player2Rating = data.getPlayer2Data().getRating() == 0 ? "?" : Integer.toString(data.getPlayer2Data().getRating());
        }

        return formatHeader(HEADER_PLAYER_2_RATING, player2Rating);
    }

    /** Parse a string representation of the 2nd players rating
     *  @return player2Rating The 1st players rating
     *  @param data The game data
     */
    protected void parsePlayer2Rating(String player2Rating, GameData data) {

        if (player2Rating != null && (player2Rating.equals("?") || player2Rating.equals("-"))) {
            player2Rating = null;
        }

        if (player2Rating != null) {
            data.getPlayer2Data().setRating(Integer.parseInt(player2Rating));
        }
    }


    /** Format a string representation of the 1st players type
     *  @param data The game data
     *  @return String The 1st players type
     */
    protected String formatPlayer1Type(GameData data) {
        String type = "?";
        if (data.getPlayer1Data() != null) {
            type = data.getPlayer1Data().getType() == PlayerData.HUMAN ? "Human" : "Computer";
        }
        return formatHeader(HEADER_PLAYER_1_TYPE, type);
    }

    /** Parse a string representation of the 1st players type
     *  @return String The 1st players type
     *  @param data The game data
     */
    protected void parsePlayer1Type(String player1Type, GameData data) {

        if (player1Type != null && player1Type.equals("?")) {
            player1Type = null;
        }

        if (player1Type != null) {
            int type = (player1Type.equals("Human")) ? PlayerData.HUMAN : PlayerData.COMPUTER;
            data.getPlayer1Data().setType(type);
        }
    }


    /** Format a string representation of the 2nd players type
     *  @param data The game data
     *  @return String The 2nd players type
     */
    protected String formatPlayer2Type(GameData data) {
        String type = "?";
        if (data.getPlayer2Data() != null) {
            type = data.getPlayer2Data().getType() == PlayerData.HUMAN ? "Human" : "Computer";
        }
        return formatHeader(HEADER_PLAYER_2_TYPE, type);
    }

    /** Parse a string representation of the 2nd players type
     *  @return String The 2nd players type
     *  @param data The game data
     */
    protected void parsePlayer2Type(String player2Type, GameData data) {

        if (player2Type != null && player2Type.equals("?")) {
            player2Type = null;
        }

        if (player2Type != null) {
            int type = (player2Type.equals("Human")) ? PlayerData.HUMAN : PlayerData.COMPUTER;
            data.getPlayer2Data().setType(type);
        }
    }


    /** Format a string representation of the result
     *  @param data The game data
     *  @param header Format into a header line or not
     *  @return String The result
     */
    protected String formatResult(GameData data, boolean header) {

        String result = "?";
        if (data.getWinner() == GameData.PLAYER1) {
            result = "1-0";
        }
        else if (data.getWinner() == GameData.PLAYER2) {
            result = "0-1";
        }
        else if (data.getWinner() == GameData.DRAW) {
            result = "1/2-1/2";
        }

        return (header) ? formatHeader(HEADER_RESULT, result) : result;
    }

    /** Format a string representation of the swapped
     *  @param data The game data
     *  @return String The result
     */
    protected String formatSwapped(GameData data) {
        return formatHeader(HEADER_SWAPPED, data.didPlayersSwap() ? "Yes" : "No");
    }

    /** Parse a string representation of the result
     *  @return String The result
     *  @param data The game data
     */
    protected void parseResult(String resultStr, GameData data) {

        int result = GameData.UNKNOWN;

        if (resultStr != null && resultStr.equals("?")) {
            result = GameData.UNKNOWN;
        }
        else if (resultStr.equals("1-0")) {
            result = GameData.PLAYER1;
        }
        else if (resultStr.equals("0-1")) {
            result = GameData.PLAYER2;
        }
        else if (resultStr.equals("0-0") || resultStr.equals("1/2-1/2")) {
            result = GameData.DRAW;
        }

        data.setWinner(result);
    }


    /** Format a header for the game
     *  @param key The name of the header
     *  @param value The value of the header
     */
    protected String formatHeader(String key, String val) {
        return "[" + key + " \"" + val + "\"]";
    }


    /** Parse a header for the game
     *  @param header The header line to parse
     *  @param headers The hashtable to put the parsed header into
     *  @exception ParseException If the header cannot be parsed
     */
    protected void parseHeader(String header, Hashtable headers) throws ParseException {

        if (header == null || header.length() == 0) {
            throw new ParseException("Invalid header, empty", 0);
        }
        else if (header.charAt(0) != '[') {
            throw new ParseException("Invalid header, missing [", 0);
        }
        else if (header.charAt(header.length() - 1) != ']') {
            throw new ParseException("Invalid header, missing ]", header.length());
        }
        else {
            int lastQuote = header.lastIndexOf('"');
            if (lastQuote == -1) {
                throw new ParseException("Invalid header, can't find quote on value", 1);
            }

            int firstQuote = header.indexOf('"');
            if (firstQuote == -1) {
                throw new ParseException("Invalid header, can't find quote on value", 1);
            }

            int space = header.lastIndexOf(' ', firstQuote - 1);
            if (space == -1) {
                throw new ParseException("Invalid header, can't split key/value", 1);
            }

            String key = header.substring(1, space).trim();
            String value = header.substring(firstQuote + 1, lastQuote).trim();

            headers.put(key, value);
        }
    }


    /** Format a Alpha-Numeric coordinate string for a move
     *  @param p The move
     *  @return String The coordinate
     */
    public static String formatCoordinates(int p) {
        int x = p % 19;
        int y = p / 19;

        char xx[] = new char[1];
        xx[0] = (char) (65 + x);
        if (xx[0] > 72) xx[0]++;
        return new String(xx) + (19 - y);
    }

    /** Parse a Alpha-Numeric coordinate string for a move
     *  @param p The coordinate
     *  @return int The move
     */
    public static int parseCoordinates(String p) {

    	if (p != null) {
    		p = p.toUpperCase();
    	}
        // invalid coordinate
        if (p == null || p.length() < 2 || p.charAt(0) < 'A' || p.charAt(0) > 'Z') {
            return -1;
        }

        int x = (int) (p.charAt(0) - 65);
        if (x > 8) x--;
        int y = 19 - Integer.parseInt(p.substring(1));

        return y * 19 + x;
    }
}