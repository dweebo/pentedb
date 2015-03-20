package org.pente.gameDatabase.swing;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.pente.game.*;
import org.pente.tree.PenteAnalyzer;
import org.pente.tree.PositionAnalysis;
import org.pente.tree.Threat;

/**
 * @author dweebo
 */
public class Utilities {

    private static DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	public static PlunkNode convertGame(GameData d) {
		PlunkNode movesRoot = new PlunkNode();
    	PlunkNode c = movesRoot;
    	PlunkNode p = null;
		GridState gs = GridStateFactory.createGridState(
			GridStateFactory.getGameId(d.getGame()), d);
    	for (int i = 0; i < gs.getNumMoves(); i++) {
    		c.setMove(gs.getMove(i));
    		c.setHash(gs.getHash(i));
    		c.setDepth(i);
    		c.setRotation(gs.getRotation(i));
    		if (p != null) {
    			c.setParent(p);
    		}
    		p = c;
    		c = new PlunkNode();
    	}
    	return movesRoot;
	}
	public static String getGameName(GameData d) {
		String s = null;
		if (d.getPlayer1Data() != null && d.getPlayer1Data().getUserIDName() != null &&
			d.getPlayer2Data() != null && d.getPlayer2Data().getUserIDName() != null) {
			s = d.getPlayer1Data().getUserIDName() + " vs. " + d.getPlayer2Data().getUserIDName();
			if (d.getDate() != null) {
				s += " " + dateFormat.format(d.getDate());
			}
		}
		else {
			s = "Unknown game";
		}

		return s;
	}
	public static byte[] readStream(InputStream i) throws IOException {

		ByteArrayOutputStream o = new ByteArrayOutputStream();

        while (true) {

            byte bytes[] = new byte[1024];
            int l = i.read(bytes);
            if (l == -1) {
                break;
            }
            else {
            	o.write(bytes, 0, l);
            }
        }

        return o.toByteArray();
	}
	public static void writeFile(File f, String s) throws IOException {
		FileWriter out = null;
		try {
			out = new FileWriter(f);
			out.write(s);

		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	public static List<PlunkNode> getAllNodes(PlunkNode root) {
		List<PlunkNode> nodes = new ArrayList<PlunkNode>();
		getAllNodes(root, nodes);
		return nodes;
	}
	private static void getAllNodes(PlunkNode n, List<PlunkNode> nodes) {
		nodes.add(n);
		if (n.hasChildren()) {
			for (PlunkNode c : n.getChildren()) {
				getAllNodes(c, nodes);
			}
		}
	}

	private static Color AC[] = new Color[] {
		null,
		new Color(1f, 0, 0, 1f),
		new Color(0, 0, 1f, 1f),
		new Color(1f, 0, 0, .5f),
		new Color(0, 0, 1f, .5f)
	};
	public static void analyzePosition(GridState state, PlunkPenteBoardLW board) {

        if (state.isGameOver()) return;
		PenteAnalyzer analyzer = new PenteAnalyzer((PenteState) state);
        //log4j.debug("count = " + scanCount + ", depth=" + scanDepth);
        PositionAnalysis a = analyzer.analyzeMove();
        for (Iterator it = a.getThreats(1); it.hasNext();) {
        	Threat t = (Threat) it.next();
        	if (t.type == Threat.TYPE_TRIA ||
        		t.type == Threat.TYPE_CLOSED_FOUR ||
        		t.type == Threat.TYPE_OPEN_FOUR ||
        		t.type == Threat.TYPE_POTENTIAL_CAPTURE) {
        		//System.out.println(t);
        		int ends[] = t.getEnds();
        		//System.out.println(ends[0]+","+ends[1]);
        		BoardLine l = new BoardLine();
        		l.setColor(AC[1]);
        		l.setX1(ends[0]%19);
        		l.setY1(18-ends[0]/19);
        		l.setX2(ends[1]%19);
        		l.setY2(18-ends[1]/19);
        		board.addLine(l);
        	}
//        	else if (t.type == Threat.TYPE_POTENTIAL_FOUR) {
//        		int ends[] = t.getEnds();
//        		//System.out.println(ends[0]+","+ends[1]);
//        		BoardLine l = new BoardLine();
//        		l.setColor(AC[3]);
//        		l.setX1(ends[0]%19);
//        		l.setY1(18-ends[0]/19);
//        		l.setX2(ends[1]%19);
//        		l.setY2(18-ends[1]/19);
//        		board.addLine(l);
//        	}
        }
        for (Iterator it = a.getThreats(2); it.hasNext();) {
        	Threat t = (Threat) it.next();
        	if (t.type == Threat.TYPE_TRIA ||
        		t.type == Threat.TYPE_CLOSED_FOUR ||
        		t.type == Threat.TYPE_OPEN_FOUR ||
        		t.type == Threat.TYPE_POTENTIAL_CAPTURE) {
        		//System.out.println(t);
        		int ends[] = t.getEnds();
        		//System.out.println(ends[0]+","+ends[1]);
        		BoardLine l = new BoardLine();
        		l.setColor(AC[2]);
        		l.setX1(ends[0]%19);
        		l.setY1(18-ends[0]/19);
        		l.setX2(ends[1]%19);
        		l.setY2(18-ends[1]/19);
        		board.addLine(l);
        	}
        }
	}
}
