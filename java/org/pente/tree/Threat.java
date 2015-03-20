package org.pente.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author dweebo
 */
public class Threat {

    public int moves[] = new int[20];
    public int numMoves = 0;
    public int threatMove; // for potential captures
    public int captureMove; // for potential captures

    public List resps = new ArrayList(5);
    public List next = new ArrayList(5);
    public int player = 0;
    public int type;
    public int movesToWin;
    public int blockedMovesToWin; // how many moves to win if not blocked

    public boolean blocked;
    public int typeSinceBlocked;
    public int numBlocks = 0;
    public int blockPositions[] = new int[5];

    public static final int TYPE_BLOCKED = 0;

    public static final int TYPE_POTENTIAL_CAPTURE = 1;
    public static final int TYPE_POTENTIAL_THREE_SPLIT = 2;
    public static final int TYPE_POTENTIAL_THREE_PAIR = 3;

    public static final int TYPE_POTENTIAL_FOUR = 4;
    public static final int TYPE_TRIA = 5;
    public static final int TYPE_CLOSED_FOUR = 6;
    public static final int TYPE_OPEN_FOUR = 7;


    public void addMove(int move) {
        // insertion sort
        int i = 0;
        for (i = 0; i < numMoves; i++) {
            if (move == moves[i]) return;
            else if (move < moves[i]) break;
        }

        for (int j = numMoves; j > i; j--) {
            moves[j] = moves[j - 1];
        }
        moves[i] = move;
        numMoves++;
    }
    public int[] getEnds() {
    	int min = 370;
    	int max = -1;
    	for (int i = 0; i < numMoves; i++) {
    		if (moves[i] < min) {
    			min = moves[i];
    		}
    		if (moves[i] > max) {
    			max = moves[i];
    		}
    	}
    	return new int[] { min, max };
    }
    public int getMove(int index) {
        return moves[index];
    }

    public void addResp(int move) {
        // make sure no duplicates
        if (!resps.contains(new Integer(move))) {
            resps.add(new Integer(move));
        }
    }
    public void insertNext(int move, int index) {
        if (!next.contains(new Integer(move))) {
            next.add(index, new Integer(move));
        }
    }
    public void addNext(int move) {
        // make sure no duplicates
        if (!next.contains(new Integer(move))) {
            next.add(new Integer(move));
        }
    }
    public void addBlock(int move) {
        blocked = true;
        blockPositions[numBlocks++] = move;
    }

    /** returns 0 if not similar
     *  1 if this threat is bigger and contains all of t
     *  2 if this threat is smaller and is contained by t
     */
    public int isSimilar(Threat t) {
        if (t.numMoves == numMoves) {
            return 0;
        }

        if (t.numMoves > numMoves) {
            int j = 0;
            for (int i = 0; i < t.numMoves; i++) {
                if (t.moves[i] == moves[j]) {
                    j++;
                }
            }
            if (j == numMoves) return 2;
        }
        else {
            int j = 0;
            for (int i = 0; i < numMoves; i++) {
                if (moves[i] == t.moves[j]) {
                    j++;
                }
            }
            if (j == t.numMoves) return 1;
        }

        return 0;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Threat)) return false;
        Threat t = (Threat) o;

        if (t.numMoves != numMoves) return false;
        for (int i = 0; i < numMoves; i++) {
            if (t.moves[i] != moves[i]) return false;
        }
        return true;
    }
    public int hashCode() {
        int r = 0;
        for (int i = 0; i < numMoves; i++) {
            r += moves[i] * 37;
        }
        return r;
    }

//    public String toString() {
//        String r = "[player=" + player + ", mtow=" + movesToWin + ", type=" + type + ", moves=";
//        for (int i = 0; i < numMoves; i++) {
//            r += Utils.printMove(moves[i]);
//            if (i < numMoves - 1) r+= ",";
//        }
//        r += " resps=";
//        for (Iterator it = resps.iterator(); it.hasNext();) {
//            r += Utils.printMove(((Integer) it.next()).intValue());
//            if (it.hasNext()) r+= ",";
//        }
//        r += " next=";
//        for (Iterator it = next.iterator(); it.hasNext();) {
//            r += Utils.printMove(((Integer) it.next()).intValue());
//            if (it.hasNext()) r+= ",";
//        }
//        if (blocked) {
//            r += " blocked=";
//            for (int i = 0; i < numBlocks; i++) {
//                r += Utils.printMove(blockPositions[i]);
//                if (i < numBlocks - 1) r+= ",";
//            }
//            r += " bmtow=" + blockedMovesToWin;
//            r += " type=" + typeSinceBlocked;
//        }
//
//        r += "]";
//        return r;
//    }
}