package org.pente.tree;

import java.util.*;

/**
 * @author dweebo
 */
class Line implements Cloneable {

//    private static final Category log4j = Category.getInstance(
//        Line.class.getName());

    int orientation;
    int len;
    int myLen;
    int gaps;
    int p;
    int op;
    int positions[] = new int[20];
    int moves[] = new int[20];
    int move = 0;

    //TODO get rid of this, maybe just put them in moves
    int thirdOuterMoves[] = new int[2]; //hack to get move 3rd out
    int thirdOuterPositions[] = new int[2];

    int groups[] = new int[3];
    int numGroups = 0;

    // can't find X___X (move to middle would be good)
    // a 0X_X or 0X__X isn't considered a threat yet either
    // X_XXX_X was correctly classified as 2 closed-fours
    // X_XX_X was correctly classified as 2 open 3's
    // junit check!
    public List findThreat() {

        op = 3 - p;

        List threats = new ArrayList(1);

        Threat t = new Threat();
        t.movesToWin = 10;
        t.player = p;
        // add standard moves and responses
        for (int i = 2; i < len - 2; i++) {
            if (moves[i] == p) {
                t.addMove(positions[i]);
            }
            // gaps are always next moves or responses
            else if (moves[i] == 0) {
                t.addResp(positions[i]);
                t.addNext(positions[i]);
            }
        }

        // junit check
        if (myLen == 1) {
            return null;
        }
        // junit check
        else if (myLen == 2) {
            if (len == 6) { //**XX**
                // WXX** or **XXW hit a wall
                // junit check
                if (moves[1] == 4 || moves[len - 2] == 4) {
                    return null;
                }
                // *0XX0* - play inside capture
                // junit check
                else if (moves[1] == op && moves[len - 2] == op) {
                    return null;
                }
                // *0XX**
                // junit check
                else if (moves[1] == op) {

                    //_X0XX or XX0XX, not 0X0XX
                    // junit check
                    if (moves[0] == p && (thirdOuterMoves[0] == p ||
                                          thirdOuterMoves[0] == 0)) {
                        //threats.add(t);
                        Threat t2 = new Threat();
                        t2.player = p;
                        t2.type = Threat.TYPE_BLOCKED;
                        t2.movesToWin = 10;
                        t2.blockedMovesToWin = 3;
                        t2.addBlock(positions[1]);
                        t2.addMove(positions[0]);
                        t2.addMove(positions[2]);
                        t2.addMove(positions[3]);
                        // XX0XX
                        // junit check
                        if (thirdOuterMoves[0] == p) {
                            t2.blockedMovesToWin = 1;
                            t2.addMove(thirdOuterPositions[0]);
                        }
                        threats.add(t2);
                        return threats;
                    }
                    else {
                        return null;
                    }
                }
                // **XX0*
                // junit check
                else if (moves[len - 2] == op) {

                    //XX0X, XX0XX yes, XX0XX0 not
                    // junit check
                    if (moves[len - 1] == p && (thirdOuterMoves[1] == p ||
                                                thirdOuterMoves[1] == 0)) {
                        //threats.add(t);
                        Threat t2 = new Threat();
                        t2.player = p;
                        t2.type = Threat.TYPE_BLOCKED;
                        t2.movesToWin = 10;
                        t2.blockedMovesToWin = 3;
                        t2.addBlock(positions[len - 2]);
                        t2.addMove(positions[len - 1]);
                        t2.addMove(positions[len - 3]);
                        t2.addMove(positions[len - 4]);
                        //XX0XX
                        // junit check
                        if (thirdOuterMoves[1] == p) {
                            t2.blockedMovesToWin = 1;
                            t2.addMove(thirdOuterPositions[1]);
                        }
                        threats.add(t2);
                        return threats;
                    }
                    else {
                        return null;
                    }
                }
                // junit check
                // 0_XX_0 or W_XX_0, etc.
                else if ((moves[0] == op || moves[0] == 4) &&
                         (moves[len - 1] == op || moves[len - 1] == 4)) {
                    return null;
                }
                // *_XX_*
                // junit check
                else {
                    t.type = Threat.TYPE_POTENTIAL_THREE_PAIR;
                    t.addNext(positions[1]);
                    t.addNext(positions[len - 2]);
                    t.addResp(positions[1]);
                    t.addResp(positions[len - 2]);
                    if (moves[0] != op && moves[0] != 4) {
                        t.addNext(positions[0]);
                    }
                    if (moves[len - 1] != op && moves[len - 1] != 4) {
                        t.addNext(positions[len - 1]);
                    }
                    t.movesToWin = 5;
                }
            }
            // junit check
            else {
                // 0X_X or 0X__X, don't worry about it for now i guess
                // junit check
                if (moves[1] == op || moves[len - 2] == op ||
                    moves[1] == 4 || moves[len - 2] == 4) {
                    return null;
                }
                t.type = Threat.TYPE_POTENTIAL_THREE_SPLIT;
                t.movesToWin = 5;
                t.addNext(positions[1]);
                t.addResp(positions[1]);
                t.addNext(positions[len - 2]);
                t.addResp(positions[len - 2]);
                if (gaps == 1 && moves[0] != op) { // make a X_X_X
                    t.addNext(positions[0]);
                }
                if (gaps == 1 && moves[len - 1] != op) { // make a X_X_X
                    t.addNext(positions[len - 1]);
                }
            }
        }
        //*X_X_X* or *X__XX*
        // junit check
        else if (myLen == 3 && len == 9 && gaps == 2) {
            t.type = Threat.TYPE_POTENTIAL_FOUR;
            t.movesToWin = 3;

            // TODO in case of X__XX, would maybe consider
            // playing X__XX0 in response...
        }
        // *X000X* = maybe a blocked 4 (0X000X*)
        // junit check
        else if (myLen == 3 && len == 7 && gaps == 0 && moves[len - 2] == op
                && moves[1] == op) {
            t.typeSinceBlocked = Threat.TYPE_TRIA;
            t.blockedMovesToWin = 1;
            t.movesToWin = 0;
            // junit check
            if (moves[0] == p) {
                t.type = Threat.TYPE_BLOCKED;
                t.addBlock(positions[1]);
                t.addMove(positions[0]);
            }
            // junit check
            else if (moves[len - 1] == p) {
                t.type = Threat.TYPE_BLOCKED;
                t.addBlock(positions[len - 2]);
                t.addMove(positions[len - 1]);
            }
            // junit check
            else {
                return null;
            }
        }
        //*OXXX__ = potential 4
        //*0XXX_0 = perhaps a block
        // junit check
        else if (myLen == 3 && len == 7 && gaps == 0 && moves[1] == op
            && moves[len - 2] == 0) {

            //*OXXX__
            // junit check
            if (moves[len - 1] == 0) {
                t.type = Threat.TYPE_POTENTIAL_FOUR;
                t.movesToWin = 3;
                t.addNext(positions[len - 2]);
                t.addResp(positions[len - 2]);
                t.addNext(positions[len - 1]);
                t.addResp(positions[len - 1]);
            }
            //*0XXX_0
            // junit check
            else {
                t.movesToWin = 0;//no direct way to win
                t.type = Threat.TYPE_BLOCKED;
            }

            //X0XXX_*
            // junit check
            if (moves[0] == p) {
                t.typeSinceBlocked = Threat.TYPE_TRIA;
                t.blockedMovesToWin = 1;
                t.addMove(positions[0]);
            }
            //_0XXX_*
            // junit check
            else {
                t.typeSinceBlocked = Threat.TYPE_POTENTIAL_THREE_SPLIT;
                t.blockedMovesToWin = 3;
            }
            t.addBlock(positions[1]);
        }
        //__XXX0* = potential 4
        //0_XXX0* = perhaps a blocked tria
        // junit check
        else if (myLen == 3 && len == 7 && gaps == 0 && moves[len - 2] == op
                && moves[1] == 0) {
            //__XXX0*
            // junit check
            if (moves[0] == 0) {
                t.type = Threat.TYPE_POTENTIAL_FOUR;
                t.movesToWin = 3;
                t.addNext(positions[1]);
                t.addResp(positions[1]);
                t.addNext(positions[0]);
                t.addResp(positions[0]);
            }
            //0_XXX0*
            // junit check
            else {
                t.movesToWin = 0;//no direct way to win
                t.type = Threat.TYPE_BLOCKED;
            }

            //*_XXX0X
            // junit check
            if (moves[len - 1] == p) {
                t.typeSinceBlocked = Threat.TYPE_TRIA;
                t.blockedMovesToWin = 1;
                t.addMove(positions[len - 1]);
            }
            //_0XXX_*
            // junit check
            else {
                t.typeSinceBlocked = Threat.TYPE_POTENTIAL_THREE_SPLIT;
                t.blockedMovesToWin = 3;
            }
            t.addBlock(positions[len - 2]);
        }
        // *X0_00_* or *X00_0_* or
        // *0_00X*  or *00_0X*
        // junit check
        else if (myLen == 3 && gaps == 1 &&
                 ((moves[1] == op && moves[len - 2] == 0) ||
                  (moves[1] == 0 && moves[len - 2] == op))) {

            t.type = Threat.TYPE_POTENTIAL_FOUR;
            t.typeSinceBlocked = Threat.TYPE_POTENTIAL_THREE_SPLIT;
            t.movesToWin = 3;
            t.blockedMovesToWin = 3;
            // _00_0X or _0_00X
            // junit check
            if (moves[1] == 0) {
                t.addNext(positions[1]);
                t.addResp(positions[1]);
                t.addBlock(positions[len - 2]);
            }
            // X00_0_ or X0_00_
            // junit check
            else {
                t.addNext(positions[len - 2]);
                t.addResp(positions[len - 2]);
                t.addBlock(positions[1]);
            }
        }
        // trias ?_XXX_? or _X_XX_, 0_XXX_0, yes 0_XX_X_0
        // junit check
        else if (myLen == 3 && gaps < 2 && moves[1] == 0 && moves[len - 2] == 0) {

            // junit check
            if (gaps == 0 &&
                (moves[0] == op || moves[0] == 4) &&
                (moves[len - 1] == op || moves[len - 1] == 4)) { // 0_XXX_0
                t.type = Threat.TYPE_POTENTIAL_FOUR;
                t.movesToWin = 3;
                t.addNext(positions[1]);
                t.addNext(positions[len - 2]);
                t.addResp(positions[1]);
                t.addResp(positions[len - 2]);
            }
            else {

                // junit check
                t.type = Threat.TYPE_TRIA;
                t.movesToWin = 3;

                if (gaps == 0) {
                    //0_XXX__
                    // junit check
                    if (moves[0] == op || moves[0] == 4) {
                        t.addResp(positions[len - 1]);
                    }
                    else { // 12XXX3?
                        t.addNext(positions[1]);
                        t.addNext(positions[0]);
                        t.addNext(positions[len - 2]);

                        t.addResp(positions[1]);
                        t.addResp(positions[0]);
                        t.addResp(positions[len - 2]);
                    }
                    //__XXX_0
                    // junit check
                    if (moves[len - 1] == op || moves[len - 1] == 4) {
                        t.addResp(positions[0]);
                    }
                    else { //?3XXX12
                        t.addNext(positions[len - 2]);
                        t.addNext(positions[len - 1]);
                        t.addNext(positions[1]);

                        t.addResp(positions[len - 2]);
                        t.addResp(positions[len - 1]);
                        t.addResp(positions[1]);
                    }
                }
                //__X_XX__ have to respond next to it
                else {

                    t.addNext(positions[1]);
                    t.addResp(positions[1]);
                    t.addNext(positions[len - 2]);
                    t.addResp(positions[len - 2]);
                }
            }

        }

        // detects _XXXX_ and 0XXXX_ (four needs to be blocked)
        // detects against 0XXXX0 (doesn't need block)
        // detects *X_XXX* or *XX_XX* (needs a block)
        else if (myLen >= 4) {

            // 0XXXX0
            // junit check
            if (gaps == 0 &&
                (moves[1] == op || moves[1] == 4) &&
                (moves[len - 2] == op || moves[len - 2] == 4)) {
                t.type = Threat.TYPE_BLOCKED;
                t.typeSinceBlocked = Threat.TYPE_TRIA;
                t.blockedMovesToWin = 1;
                t.addBlock(positions[1]);
                t.addBlock(positions[len - 2]);
                threats.add(t);
                return threats;
            }
            // *OOOO__O or *OOOO_O
            int l = 0;
            for (int i = 0; i < numGroups; i++) {
                if (groups[i] == 4) {
                    t.next.clear();
                    t.resps.clear();
                    t.movesToWin = 1;
                    if (moves[l+1] == 0 && moves[l+6] == 0) {
                        t.type = Threat.TYPE_OPEN_FOUR;
                        t.addNext(positions[l+1]);
                        t.addNext(positions[l+6]);
                        t.addResp(positions[l+1]);
                        t.addResp(positions[l+6]);
                    }
                    else if (moves[l+1] == 0){
                        t.type = Threat.TYPE_CLOSED_FOUR;
                        t.addNext(positions[l+1]);
                        t.addResp(positions[l+1]);
                    }
                    else { //moves[l+6]==0
                        t.type = Threat.TYPE_CLOSED_FOUR;
                        t.addNext(positions[l+6]);
                        t.addResp(positions[l+6]);
                    }
                    threats.add(t);
                    return threats;
                }
                l += groups[i] + 1;
            }

            // XX__XX
            if (gaps == 2 && numGroups == 3 && groups[1] == 0) {
                t.type = Threat.TYPE_POTENTIAL_FOUR;
                t.movesToWin = 3;

                if (moves[1] == 0) {
                    t.addNext(positions[1]);
                    t.addResp(positions[1]);
                }
                if (moves[len - 2] == 0) {
                    t.addNext(positions[len - 2]);
                    t.addResp(positions[len - 2]);
                }
                threats.add(t);
                return threats;
            }




            // junit check
            if (gaps == 0 && moves[1] == 0 && moves[len - 2] == 0) {
                t.type = Threat.TYPE_OPEN_FOUR;
            }
            else {
                t.type = Threat.TYPE_CLOSED_FOUR;
            }
            t.movesToWin = 1;

            // junit check
            if (gaps == 0) {
                // junit check
                if (moves[1] != op && moves[1] != 4) {
                    t.addNext(positions[1]);
                    t.addResp(positions[1]);
                }
                // junit check
                if (moves[len - 2] != op && moves[len - 2] != 4) {
                    t.addNext(positions[len - 2]);
                    t.addResp(positions[len - 2]);
                }
            }
        }
        else {
            return null;
        }

        threats.add(t);
        return threats;
    }

    public List split() {

        // after creating line, scan moves in line
        // record groups and gaps
        // _X_XX_XX would be 1g2g2 at a high level
        // using that i see that the pieces around the 1st gap are a tria
        // and the pieces around the 2nd gap are a closed4, so i'll create 2 separate threats
        // XX_X_XX would be 2g1g2 or XX_X and X_XX
        // XX_XX_XX would be 2g2g2 or XX_XX and XX_XX
        // X_X_X would be 1g1g1
        // 0XXX_X_XXX0 would be 3g1g3 or XXX_X and X_XXX
        // XXXX_X would be 4g1, what to do here? might just XXXX
        // XXX__X would be 3g0g1, what to do here?  rule might still work XXX_
        // XX__XX = 2g0g2, using rule would stay like this, is actually a pot4 and 2 pot3's
        // XXX_XX = 3g2, = closed 4
        // XXX__XXX = 3g0g3 = separate trias
        // XXX__XX = 3g0g2 = XXX_ and XX

        // rules for splitting lines
        // 1. if less than 3 groups (less than 2 gaps) return line
        // 2. if group of line == 4, keep only those 4 in line
        // 3. if 2 groups add up to > 3, create a line out of those pieces and continue
        // 4. if 2 groups add up to > 2, create a line out of those pieces and continue
        // 5. if none are > 2, keep the line out of the whole thing (for X_X_X)

        // scan groups
        for (int i = 2; i < len - 2; i++) {
            if (moves[i] == p) groups[numGroups]++;
            else if (moves[i] == 0) numGroups++;
        }
        numGroups++;


        List lines = new ArrayList(1);

        if (numGroups < 3) {
            lines.add(this);
            return lines;
        }

        for (int i = 0; i < numGroups; i++) {
            if (groups[i] == 4) {
                // create just a threat using the one group
            }
        }

        boolean split = false;

        //XX_XX_***, cut out the ***
        if (groups[1] != 0 && (groups[0] + groups[1] == 4)) {
            split = true;
            try {
                Line l = (Line) clone();
                l.len = 9;
                l.myLen = 4;
                l.gaps = 1;
                l.thirdOuterMoves[1] = l.moves[10];
                l.thirdOuterPositions[1] = l.positions[10];
                lines.add(l);

            } catch (CloneNotSupportedException c) {}
        }
        //***_X_XXX, cut out the ***
        if (groups[1] != 0 && (groups[1] + groups[2] == 4)) {

            split = true;
            try {
                Line l = (Line) clone();
                int d = l.len - 9;
                // shift moves to left
                for (int i = 0; i < 20 - d; i++) {
                    l.moves[i] = l.moves[i + d];
                    l.positions[i] = l.positions[i + d];
                }
                l.thirdOuterMoves[1] = l.moves[10];
                l.thirdOuterPositions[1] = l.positions[10];
                l.len = 9;
                l.myLen = 4;
                l.gaps = 1;
                lines.add(l);

            } catch (CloneNotSupportedException c) {}
        }

        if (groups[0] + groups[1] == 3) {
            split = true;
            try {
                Line l = (Line) clone();
                // just _XXX_
                if (groups[1] == 0) {
                    l.len = 7;
                    l.gaps = 0;
                }
                // else _X_XX or XX_X
                else {
                    l.len = 8;
                    l.gaps = 1;
                }
                l.thirdOuterMoves[1] = l.moves[l.len + 1];
                l.thirdOuterPositions[1] = l.positions[l.len + 1];
                l.myLen = 3;
                lines.add(l);

            } catch (CloneNotSupportedException c) {}
        }
        if (groups[1] + groups[2] == 3) {
            split = true;
            try {
                Line l = (Line) clone();
                // just _XXX_
                if (groups[1] == 0) {
                    l.len = 7;
                    l.gaps = 0;
                    int d = len - 7;
                    for (int i = 0; i < 20 - d; i++) {
                        l.moves[i] = l.moves[i + d];
                        l.positions[i] = l.positions[i + d];
                    }
                }
                // else _X_XX or XX_X
                else {
                    l.len = 8;
                    l.gaps = 1;
                    int d = len - 8;
                    for (int i = 0; i < 20 - d; i++) {
                        l.moves[i] = l.moves[i + d];
                        l.positions[i] = l.positions[i + d];
                    }
                }
                l.thirdOuterMoves[1] = l.moves[l.len + 1];
                l.thirdOuterPositions[1] = l.positions[l.len + 1];
                l.myLen = 3;
                lines.add(l);

            } catch (CloneNotSupportedException c) {}
        }

        if (!split) {
            lines.add(this);
        }

        return lines;
    }

    public Object clone() throws CloneNotSupportedException {
        Line l = (Line) super.clone();
        l.moves = new int[20];
        l.positions = new int[20];
        for (int i = 0; i < len; i++) {
            l.moves[i] = moves[i];
            l.positions[i] = positions[i];
        }

        l.thirdOuterMoves = new int[2];
        l.thirdOuterPositions = new int[2];
        l.thirdOuterMoves[0] = thirdOuterMoves[0];
        l.thirdOuterMoves[1] = thirdOuterMoves[1];
        l.thirdOuterPositions[0] = thirdOuterPositions[0];
        l.thirdOuterPositions[1] = thirdOuterPositions[1];

        return l;
    }

//    public String toString() {
//        //if (!log4j.isDebugEnabled()) return "";
//
//        StringBuffer buf = new StringBuffer("[move=" + Utils.printMove(move) + ", orientation=" + orientation + ", len=" + len + ",myLen=" +
//            myLen + ", gaps=" + gaps + ",p=" + p + ", pieces=[");
//        for (int i = 0; i < len; i++) {
//            buf.append(Utils.printMove(positions[i]) + "=" + moves[i]);
//            if (i != positions.length) buf.append(",");
//        }
//        buf.append("]]");
//        return buf.toString();
//    }
}