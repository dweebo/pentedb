package org.pente.gameDatabase.swing.importer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Writer;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.pente.game.DefaultPlayerData;
import org.pente.game.GameData;
import org.pente.game.GridStateFactory;
import org.pente.game.PlayerData;
import org.pente.gameDatabase.swing.PlunkGameData;
import org.pente.gameDatabase.swing.PlunkNode;
import org.pente.gameDatabase.swing.PlunkTree;

/** A game formatter that formats/parses games in SGF format.
 * @author dweebo
 */
public class SGFGameFormat {

    /** Formatting string to format/parse dates */
    protected String   dateFormatStr =               "MM/dd/yyyy";
    private SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
    /** Formatting string to format/parse times */
    protected static final String   TIME_FORMAT =               "HH:mm:ss";

    /** The line separator used to format games */
    private String      lineSeparator;

    /** Use a default line separator */
    public SGFGameFormat() {
        this("\r\n");
    }
	public SGFGameFormat(String lineSeparator, String dateFormatStr) {
		this.lineSeparator = lineSeparator;
		this.dateFormatStr = dateFormatStr;
	}

    /** Specify the line seperator used to format games
     *  @param lineSeparator The line separator
     */
    public SGFGameFormat(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }


    private void formatMove(PlunkNode n, int game, Writer out) throws IOException {
    	if (n == null) {
    		System.out.println("problem");
    	}
    	out.write(GridStateFactory.getColor(n.getDepth(), game) == 1 ? "B" : "W");
    	out.write("[" + formatCoordinates(n.getMove()) + "]");
    	out.write("HASH[" + n.getHash() + "]");
    	out.write("ROT[" + n.getRotation() + "]");
    	if (n.getName() != null && !n.getName().equals("")) {
    		out.write("N[" + n.getName() + "]");
    	}
    	if (n.getComments() != null && !n.getComments().equals("")) {
    		out.write("C[" + n.getComments() + "]");
    	}
    	switch (n.getType()) {
    	case PlunkNode.BAD:
    		out.write("BM[1]"); break;
    	case PlunkNode.VERY_BAD:
    		out.write("BM[2]"); break;
    	case PlunkNode.GOOD:
    		out.write("TE[1]"); break;
    	case PlunkNode.VERY_GOOD:
    		out.write("TE[2]"); break;
    	case PlunkNode.INTERESTING:
    		out.write("IT[]"); break;
    	}

    	out.write("\n");
    }
    private void formatNode(PlunkNode n, int game, Writer out) throws IOException {

    	out.write(";");

    	formatMove(n, game, out);

		if (n.hasChildren()) {
			for (PlunkNode c : n.getChildren()) {

				if (n.getChildCount() > 1) {
					out.write("(");
				}

				formatNode(c, game, out);

				if (n.getChildCount() > 1) {
					out.write(")");
				}
			}
		}
    }

    public StringBuffer format(Object obj, StringBuffer buffer) {return null;};

    /** Formats the game tree
     */
    public void format(PlunkNode root, int game,
    	PlunkTree tree, Writer out) throws IOException {


    	out.write("(;FF[4]AP[Unknown:0.1]");
    	out.write("CA[ISO-8859-1]");
    	out.write("GM[" + (100 + game) + "]");//TODO use 4 for gomoku/renju or 41+ ?
    	out.write("SZ[19]");
    	out.write("GN[" + tree.getName() + "]");
    	out.write("US[" + tree.getCreator() + "]");
    	//custom
    	out.write("TV[" + tree.getVersion() + "]");
    	out.write("\n");

    	if (root != null) {
    		formatNode(root, game, out);
    	}

    	out.write(")");
    }
    private String rn(String s) {
    	if (s == null) return "";
    	else return s;
    }
    public void format(PlunkGameData gameData, String dbName, Writer out) throws IOException {

    	if (gameData.getRoot() == null) return;

    	out.write("(;FF[4]AP[Unknown:0.1]");
    	out.write("CA[ISO-8859-1]");
    	out.write("GM[" + (100 + GridStateFactory.getGameId(gameData.getGame())) + "]");//TODO use 4 for gomoku/renju or 41+ ?
    	out.write("SZ[19]");
    	out.write("GN[" + gameData.getGameID() + "]\n");
    	//out.write("US[" + tree.getCreator() + "]");//TODO not stored currently
    	//custom
    	out.write("DBNM[" + dbName + "]");

    	out.write("PC[" + gameData.getSite() + "]\n");
    	out.write("EV[" + gameData.getEvent() + "]\n");
    	out.write("RO[" + rn(gameData.getRound()) + "]\n");
    	out.write("SC[" + rn(gameData.getSection()) + "]\n");
    	out.write("DT[" + dateFormat.format(gameData.getDate()) + "]\n");
    	out.write("PW[" + gameData.getPlayer1Data().getUserIDName() + "]\n");
    	out.write("WR[" + gameData.getPlayer1Data().getRating() + "]\n");
    	out.write("PB[" + gameData.getPlayer2Data().getUserIDName() + "]\n");
    	out.write("BR[" + gameData.getPlayer2Data().getRating() + "]\n");
    	out.write("RE[");
    	if (gameData.getWinner() == 0) out.write("Draw]\n");
    	else if (gameData.getWinner() == 1) out.write("W]\n");
    	else if (gameData.getWinner() == 2) out.write("B]\n");
    	out.write("RU[" + (gameData.getRated() ? "Rated]" : "Unrated]\n"));
    	out.write("TM[");
    	if (!gameData.getTimed()) out.write("-]\n");
        else if (gameData.getIncrementalTime() == 0) out.write(Integer.toString(gameData.getInitialTime()) + "]\n");
        else out.write(Integer.toString(gameData.getInitialTime()) + "+" + Integer.toString(gameData.getIncrementalTime()) + "]\n");
    	//TODO add time field

    	out.write("(");
    	PlunkNode p = gameData.getRoot();
    	while (p != null) {
    		out.write(";");
        	formatMove(p, GridStateFactory.getGameId(gameData.getGame()), out);
        	if (!p.hasChildren()) break;
        	p = p.getChildren().get(0);
    	}
    	out.write("))");
    }


    /** Format a string representation of the date
     *  @param data The game data
     *  @return String The date
     */
    protected String formatDate(GameData data) {
        StringBuffer date = new StringBuffer();

        return dateFormat.format(data.getDate(), date, new FieldPosition(0)).toString();
    }

    /** Parse a string representation of the date
     *  @param date The date
     *  @param data The game data
     *  @param ParseException Thrown by DateFormat
     */
    protected void parseDate(String date, GameData data) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStr);

        Date timeDate = data.getDate();
        Date dateDate = dateFormat.parse(date);

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
        return timeFormat.format(data.getDate(), time, new FieldPosition(0)).toString();
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

    private boolean valid = false;
    public boolean parse2(InputStream in, GameImporterListener l) throws Exception {
      PushbackReader pushrdr = new PushbackReader (new InputStreamReader(in));
      PlunkNode root = new PlunkNode();
      root.setDepth(-2);
      valid = false;
      parse (pushrdr, root, l);

      return valid;

    }
	private void processTree(GameImporterListener l, PlunkNode c) {
		valid = true;
		PlunkTree curTree;
		curTree = new PlunkTree();
		  curTree.setVersion(c.props.get("TV"));
		  curTree.setName(c.props.get("GN"));
		  curTree.setCreator(c.props.get("US"));
		  curTree.setRoot(c.getChildren().get(0));
		  curTree.getRoot().setParent(null);
		  curTree.setCanEditProps(true);
		  //TODO set game in tree?
		  //TODO associate a game database with tree?
		  l.analysisRead(curTree, "SGF");
	}
	private void processGame(GameImporterListener l, PlunkNode c) {
		valid = true;
		try
		{
			PlunkGameData curGame = new PlunkGameData();
			curGame.setEditable(true);
			curGame.setDbName(c.props.get("DBNM"));
			//curGame.setGameID(Long.parseLong(c.props.get("GN")));
			curGame.setGame(GridStateFactory.getDisplayName(Integer.parseInt(
			    c.props.get("GM")) - 100));

			curGame.setSite(c.props.get("PC"));
			curGame.setEvent(c.props.get("EV"));
			curGame.setRound(c.props.get("RO"));
			curGame.setSection(c.props.get("SC"));
			curGame.setSite(c.props.get("PC"));

			curGame.setDate(dateFormat.parse(c.props.get("DT")));

			PlayerData p1 = new DefaultPlayerData();
			p1.setUserIDName(c.props.get("PW"));
			p1.setRating(Integer.parseInt(c.props.get("WR")));
			curGame.setPlayer1Data(p1);

			PlayerData p2 = new DefaultPlayerData();
			p2.setUserIDName(c.props.get("PB"));
			p2.setRating(Integer.parseInt(c.props.get("BR")));
			curGame.setPlayer2Data(p2);

			if (c.props.get("RE").equals("W")) {
			    curGame.setWinner(1);
			}
			else if (c.props.get("RE").equals("B")) {
				curGame.setWinner(2);
			}

			curGame.setRated("Rated".equals(c.props.get("RU")));
			parseTimeControl(c.props.get("TM"), curGame);

			curGame.setRoot(c.getChildren().get(0));

			PlunkNode p = curGame.getRoot();
			while (true) {
			    curGame.addMove(p.getMove());
				if (!p.hasChildren()) break;
				p = p.getChildren().get(0);
			}

			curGame.getRoot().setParent(null);

			l.gameRead(curGame, "SGF");

		} catch (ParseException p) {
			p.printStackTrace();
		}
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

    private void parse (PushbackReader rdr, PlunkNode currentnode,GameImporterListener l)
    {
      int ch;
      PlunkNode new_node;
      try {
        while ((ch = rdr.read()) != -1)
        {
  	if (whitespace ((char) ch)) // skip whitespace
  	  continue;

  	if (isUppercase ((char) ch)) {  // is it a property?
  	  rdr.unread(ch);
  	  String key = get_property_name(rdr); // read whole property NAME
  	  String val = get_property_value(rdr);  // read entire [value]
  	  if ((key == null) || (val == null)) {
  	    parse_debug("Error reading property:");
  	    parse_debug("  ...either property name or value was null;");
  	    parse_debug("  ...property will be ignored");
  	    continue;
  	  }
  	  // else, add the property to our new node.
  	  set_prop_literal(key, val, currentnode);
  	  continue;
  	}

  	// else
  	switch (ch)
  	{
  	case '(':
  	  currentnode.head_p = true;  // mark this node as Head of a subtree
  	  continue;

  	case ')':  // pop back!
  	  return;

  	case ';':  // build a new node and recurse
  	  //if ((currentnode.getParent() == null) && firsttime) {
  	  //  new_node = currentnode;
  	//	currentnode.setDepth(-2);
  	  //  firsttime = false;
  	  //}
  	  //else {
  	    new_node = new PlunkNode();
  	  	if (currentnode.getDepth() != -2) {
  	  		new_node.setParent(currentnode);
  	  		new_node.setDepth(new_node.getParent().getDepth() + 1);
  	  	}
  	  	else {
  	  		new_node.setDepth(-1);
  	  	}
  	  //}
  	  parse(rdr, new_node, l);
  	  // after recursion returns:
  	  if (! currentnode.head_p)  // keep popping up the stack
  	    return;
  	  else if (currentnode.getDepth() == -2) {
	  	  if (new_node.props == null) {

	  	  }
	  	  else if (new_node.props.containsKey("TV")) {
	  		  processTree(l, new_node);
	  	  }
	  	  else if (new_node.props.containsKey("DBNM")) {
	  		  processGame(l, new_node);
	  	  }
  		  //this might be where each item in file is complete
  		  //so could store game/analysis here before continuing on
  		  //and then don't bother keeping data around (and make sure not to
  		  //cache it in searchcache?)
  	    continue;
  	  }

  	default:
  	  parse_debug("Unrecognized char in SGF: '" + (char) ch + "'");
  	  continue;
  	}
        }
      }
      catch (IOException e) {
        //System.out.println("PlunkNode.parse(): IOException: " + e);
      }
    }



    // We get here because parse_node found an uppercase char and pushed
    // it back;  returns the full name of the property.

    private String get_property_name (PushbackReader rdr)
    {
      int ch;
      StringBuffer sbuf = new StringBuffer();
      try {
        ch = rdr.read();  // read the first uppercase char back again
        sbuf.append((char)ch);
        while ((ch = rdr.read()) != -1) // start reading chars
        {
  	if (isUppercase((char)ch)) { // and append uppercase ones
  	  sbuf.append((char)ch);
  	  continue;
  	}
  	else {              // until we encounter a non-uppercase!
  	  rdr.unread(ch);   // push the non-uppercase letter back
  	  break;            // and stop reading
  	}
        }
      }
      catch (IOException e) {
        //System.out.println(".parse_property_name(): IOException: " + e);
      }
      parse_debug("parse_property_name is returning '"+sbuf.toString()+"'");
      return sbuf.toString();
    }

    // Read a entire property value in square brackets, return in a string.
    // Be sure to *preserve* any escaped characters!

    private String get_property_value (PushbackReader rdr)
    {
      int ch;
      StringBuffer sbuf = new StringBuffer();
      try {

        while ((ch = rdr.read()) != -1) // first, skip all whitespace
        {
  	if (! whitespace((char)ch)) {
  	  rdr.unread(ch);
  	  break;
  	}
        }

        ch = rdr.read();  // read the first "real" char -- it better be '['
        if (ch != '[') {
  	//System.out.println("parse_property_value(): error:");
  	//System.out.println("  ... didn't find a '[' after property name!");
  	return null;
        }
        // else, we *did* read a '[', as expected...
        while ((ch = rdr.read()) != -1) // start reading chars
        {
  	if (ch == ']') { // all done.  don't need to push it back, either.
  	  break;
  	}
  	else if (ch == '\\') { // if we hit a single backslash
  	  sbuf.append((char)ch); // write it to our StringBuffer
  	  ch = rdr.read();  // and immediately snarf the *next* character!
  	}
  	// finally, write this character to our StringBuffer
  	sbuf.append((char)ch);
  	continue;
        }
      }
      catch (IOException e) {
        //System.out.println("parse_property_value(): IOException: " + e);
      }
      parse_debug("parse_property_value is returning '"+sbuf.toString()+"'");
      return sbuf.toString();
    }




    private boolean whitespace (char ch)
    {
      switch (ch)
      {
      case ' ':
      case '\t':
      case '\r':
      case '\n':
      case '\f':
        return true;
      default:
        return false;
      }
    }

    private boolean isUppercase(char ch)
    {
      return Character.isUpperCase(ch);
    }



    public void set_prop_literal (String key, String value, PlunkNode n) {

    	if (key.equals("N")) {
    		n.setName(value);
    	}
    	else if (key.equals("C")) {
    		n.setComments(value);
    	}
    	else if (key.equals("B") || key.equals("W")) {
    		n.setMove(parseCoordinates(value));
    	}
    	else if (key.equals("HASH")) {
    		n.setHash(Long.parseLong(value));
    	}
    	else if (key.equals("ROT")) {
    		n.setRotation(Integer.parseInt(value));
    	}
    	else if (key.equals("IT")) {
    		n.setType(PlunkNode.INTERESTING);
    	}
    	else if (key.equals("BM")) {
    		n.setType(value.equals("1") ? PlunkNode.BAD : PlunkNode.VERY_BAD);
    	}
    	else if (key.equals("TE")) {
    		n.setType(value.equals("1") ? PlunkNode.GOOD : PlunkNode.VERY_GOOD);
    	}
    	else {
    		if (n.props == null) {
    			n.props = new HashMap<String, String>();
    		}
    		n.props.put(key, value);
    	}
    }


    // Debugging utility to examine the parser's recursion!
    // To turn off debugging, just comment-out this function's one line.

    private void parse_debug(String str)
    {
      // System.err.println("Parser: " + str);
    }

}