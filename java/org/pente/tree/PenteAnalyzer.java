package org.pente.tree;

import java.util.*;

//import org.apache.log4j.*;

import org.pente.game.*;

/**
 * @author dweebo
 */
// TODO need to write some junit tests
public class PenteAnalyzer {

//    private static final Category log4j = Category.getInstance(
//        PenteAnalyzer.class.getName());

    private PenteState penteState;
    public PenteAnalyzer(PenteState penteState) {
        this.penteState = penteState;

        	int x = penteState.getGridSizeX();
        	System.out.println("x="+x);
        	surrounding = new int[] {
        		x + 1, 1, -x + 1, x
        	};
        	FULL_SURROUND = new int[] {
        		x + 1, 1, -x + 1, x, -x - 1, -1, x - 1, -x
        	};
    }

    private int surrounding[];// = new int[] { 20, 1, -18, 19 };
    private int FULL_SURROUND[];// =
        //new int[] { 20, 1, -18, 19, -20, -1, 18, -19 };

    public PositionAnalysis analyzeMove() {

        int move = penteState.getMove(penteState.getNumMoves() - 1);
        int origMove = move;
        int p = penteState.getPosition(move); // player who just moved
        int op = 3 - p; // other player, player whose turn it is

        long startTime = System.currentTimeMillis();

        PositionAnalysis analysis = new PositionAnalysis(
            new int[] { 0, penteState.getNumCaptures(1),
                penteState.getNumCaptures(2) });
        analysis.move = move;
        analysis.player = p;

        // scan all moves for threats to ensure everything is caught
        // too hard to keep track of changes to threats between moves
        for (int i = 0; i < penteState.getNumMoves(); i++) {
            int m = penteState.getMove(i);
            int c = penteState.getPosition(m); //color
            if (c != (i % 2) + 1) {
                //System.out.println("not analyzing " + printMove(m) + " because it " +
                //    "should be " + ((i % 2) + 1) + " but it's " + c + ".");
                continue; // check that position hasn't been captured
            }
            findLines(m, c, analysis);
            findCaps(m, c, analysis);
        }
        analysis.analyzeCaptures();




        // TODO then need to recheck all threats against pairs and pot. caps. to
        // add results for 3's, 4's
        // TODO i guess i could rescan rethreats, checking to see if any potential captures
        // are a keystone pair and then setting the rank very high in that case...

        // print results
//        if (log4j.isDebugEnabled()) {
//            log4j.debug("analyze position " + Utils.printState(penteState));
//            log4j.debug(analysis);
//            log4j.debug("analysis took " +
//                (System.currentTimeMillis() - startTime) + " milliseconds.");
//        }

        return analysis;
    }

    public void findCaps(int move, int p, PositionAnalysis analysis) {

        int op = 3 - p;
        for (int i = 0; i < 8; i++) {
            int c1 = move + FULL_SURROUND[i];
            int c2 = c1 + FULL_SURROUND[i];
            int c3 = c2 + FULL_SURROUND[i];
            int c4 = move - FULL_SURROUND[i];
            if (!penteState.isValidPosition(c1, move)) continue;
            if (!penteState.isValidPosition(c2, move)) continue;
            if (!penteState.isValidPosition(c3, move)) continue;
            if (!penteState.isValidPosition(c4, move)) continue;

            int pos1 = penteState.getPosition(c1);
            int pos2 = penteState.getPosition(c2);
            int pos3 = penteState.getPosition(c3);
            int pos4 = penteState.getPosition(c4);
            if (pos1 == op && pos2 == op && pos3 == 0) {
                Threat t = new Threat();
                t.player = p;
                t.type = Threat.TYPE_POTENTIAL_CAPTURE;
                t.movesToWin = 10;
                t.addMove(c1);
                t.addMove(c2);
                t.addNext(c3);
                t.addResp(c3);
                System.out.println(c1+","+c2+","+c3+","+c4);
                t.captureMove = c3;
                t.threatMove = move;
                analysis.addThreat(t);
//                if (log4j.isDebugEnabled()) {
//                	log4j.debug("found potential cap " + t);
//                }
            }
            else if (pos4 == 0 && pos1 == p && pos2 == 0) {
                Threat t = new Threat();
                t.player = op;
                t.type = Threat.TYPE_POTENTIAL_THREE_PAIR;
                t.addMove(move);
                t.addMove(c1);
                t.addResp(c4);
                t.addResp(c2);
                t.threatMove = move;
                analysis.addPair(t);
//                if (log4j.isDebugEnabled()) {
//                	log4j.debug("found pair " + t);
//                }
            }
        }
    }


    public void findLines(int move, int p, PositionAnalysis analysis) {
        int origMove = move;
        int op = 3 - p; // other player
        for (int i = 0; i < 4; i++) {
            move = origMove;
            Line line = new Line();
            line.move = move;
            line.orientation = i;
            line.p = p;

            // first scan left as far as possible
            // stop when encounter wall, opponent or 3rd gap total
            int startRightScan = move;
            int gapsTotal = 0;
            int gapsSinceStart = 0;
            line.myLen = 1;
            while (true) {
                int newMove = move - surrounding[i];
                boolean endOp = false;
                int val = 0;
                if (!penteState.isValidPosition(newMove, move)) {
                    endOp = true;
                }
                else {
                     val = penteState.getPosition(newMove);
                     if (val == op) {
                         endOp = true;
                     }
                }
                move = newMove;

                if (endOp) {
                    markExtendedEnd(startRightScan - 3 * surrounding[i], startRightScan, 0, line);
                    markEnd(startRightScan - 2 * surrounding[i], startRightScan, 0, line);
                    markEnd(startRightScan - surrounding[i], startRightScan, 1, line);
                    break;
                }
                else if (val == p) {
                    startRightScan = move;
                    gapsSinceStart = 0;
                    line.myLen++;
                }
                // gap
                else if (val == 0) {
                    gapsTotal++;
                    gapsSinceStart++;
                }

                if (gapsTotal == 3) {
                    markExtendedEnd(startRightScan - 3 * surrounding[i], startRightScan, 0, line);
                    markEnd(startRightScan - 2 * surrounding[i], startRightScan, 0, line);
                    markEnd(startRightScan - surrounding[i], startRightScan, 1, line);
                    break;
                }
            }
            line.len = 3;
            line.positions[2] = startRightScan;
            line.moves[2] = p;
            line.myLen = 1;

            // then scan right as far as possible
            move = startRightScan;
            gapsTotal = 0;
            int gapsSinceEnd = 0;
            int endRightScan = move;
            while (true) {
                int newMove = move + surrounding[i];
                boolean endOp = false;
                int val = 0;
                if (!penteState.isValidPosition(newMove, move)) {
                    endOp = true;
                }
                else {
                     val = penteState.getPosition(newMove);
                     if (val == op) {
                         endOp = true;
                     }
                }
                move = newMove;

                if (endOp) {
                    line.len = line.myLen + line.gaps + 4 - gapsSinceEnd;
                    markEnd(endRightScan + surrounding[i], endRightScan, line.len - 2, line);
                    markEnd(endRightScan + 2 * surrounding[i], endRightScan, line.len - 1, line);
                    markExtendedEnd(endRightScan + 3 * surrounding[i], endRightScan, 1, line);
                    break;
                }
                else if (val == p) {
                    endRightScan = move;
                    gapsSinceEnd = 0;
                    line.myLen++;
                }
                // gap
                else if (val == 0) {
                    gapsTotal++;
                    gapsSinceEnd++;
                    line.gaps++;
                }

                line.moves[line.len] = val;
                line.positions[line.len] = move;

                if (gapsTotal == 3) {
                    line.len = line.myLen + line.gaps + 4 - gapsSinceEnd;
                    markEnd(endRightScan + surrounding[i], endRightScan, line.len - 2, line);
                    markEnd(endRightScan + 2 * surrounding[i], endRightScan, line.len - 1, line);
                    markExtendedEnd(endRightScan + 3 * surrounding[i], endRightScan, 1, line);
                    break;
                }

                line.len++;
            }
            line.gaps = gapsTotal - gapsSinceEnd;

//            if (log4j.isDebugEnabled()) {
//                log4j.debug(line);
//                log4j.debug("split");
//            }
//
            List lines = line.split();
            for (Iterator it = lines.iterator(); it.hasNext();) {
                Line l = (Line) it.next();
//                if (log4j.isDebugEnabled()) {
//                    log4j.debug(l);
//                }
                List ts = l.findThreat();
                if (ts != null) {
                    for (int k = 0; k < ts.size(); k++) {
                        Threat t = (Threat) ts.get(k);
                        analysis.addThreat(t);
                    }
                }
            }
        }
    }

    private static final int WALL = 4;
    private void markEnd(int newMove, int oldMove, int endPos, Line line) {
        if (penteState.isValidPosition(newMove, oldMove)) {
            line.positions[endPos] = newMove;
            line.moves[endPos] = penteState.getPosition(line.positions[endPos]);
        }
        else {
            line.positions[endPos] = 0;
            line.moves[endPos] = WALL;
        }
    }
    private void markExtendedEnd(int newMove, int oldMove, int endPos, Line line) {
        if (penteState.isValidPosition(newMove, oldMove)) {
            line.thirdOuterPositions[endPos] = newMove;
            line.thirdOuterMoves[endPos] = penteState.getPosition(line.thirdOuterPositions[endPos]);
        }
        else {
            line.thirdOuterPositions[endPos] = 0;
            line.thirdOuterMoves[endPos] = WALL;
        }
    }
}
