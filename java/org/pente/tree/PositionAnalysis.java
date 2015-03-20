package org.pente.tree;

import java.util.*;

/**
 * @author dweebo
 */
public class PositionAnalysis {

//    private static final Category log4j = Category.getInstance(
//        PositionAnalysis.class.getName());


    int move; // move that was just made
    int player; // player that just moved
    int maxCaps = 0;
    int maxCapsPos = 0;

    int caps[] = null;

    private Set<Threat> threats[] = new HashSet[3];
    private Set<Threat> pairs[] = new HashSet[3];

    public PositionAnalysis(int caps[]) {
        for (int i = 1; i < 3; i++) {
            threats[i] = new HashSet<Threat>();
            pairs[i] = new HashSet<Threat>();
        }
        this.caps = caps;
    }

    //K10, J10, O10, J9, M10, J8, N11, N9, M8, C17, O8
    public void addThreat(Threat t) {

        for (Iterator it = threats[t.player].iterator(); it.hasNext();) {
            Threat e = (Threat) it.next();
            if (e.equals(t)) return;
            int sim = e.isSimilar(t);
            if (sim == 1) { // already have a threat that contains this new one
                return;
            }
            else if (sim == 2) { // new threat contains existing one, swap them
                it.remove();
                break;
            }
        }
        // either new threat doesn't exist or is better than an existing one
        threats[t.player].add(t);
    }

    public void addPair(Threat t) {
        pairs[t.player].add(t);
    }

    public int getNumThreats(int player) {
        return threats[player].size();
    }
    public Iterator getThreats(int player) {
        return threats[player].iterator();
    }

    //inefficient implementation
    //looks at all potential caps, stores counts of all capture positions
    //in a hashmap, then looks at all counts in the hashmap to find max
    public void findMaxCaps() {
//            HashMap m = new HashMap(10);
//            for (Iterator it = threats[player].iterator(); it.hasNext();) {
//                Threat t = (Threat) it.next();
//                Integer i = (Integer) m.get(t.getMove());
//                if (i == null) {
//                    m.put(t.getMove(), new Integer(1));
//                }
//                else {
//                    m.put(t.getMove(), new Integer(i.intValue() + 1));
//                }
//            }
//            for (Iterator it = m.keySet().iterator(); it.hasNext();) {
//                Integer key = (Integer) it.next();
//                Integer val = (Integer) m.get(key);
//                if (val.intValue() > maxCaps) {
//                    maxCaps = val.intValue();
//                    maxCapsPos = key.intValue();
//                }
//            }
    }

    // need to redo this, have it return int moves[] instead of threats
    // the key to this ai working well is searching the best moves 1st
    // need to look at all threats and determine which moves are the best
    // consider the type of threat and whose turn it is
    // also consider if a move exists in 2+ threats (for me)
    // or if it blocks a threat of opponent AND creates a threat for me
    // if opponent has a threat you must respond to, consider attacking
    // potential capture, or attacking pairs across threat 1st, then block
    // when ranking captures, if a single move captures more than once
    // rank it higher.
    //
    // rankings and groups
    // group 1
    // 1. check if I have a 4 threat, if so it's a win so follow it
    // 2. check if I have a win by captures, if so it's a win so follow it
    // ---- (should always rank the above higher than rest, even if combined)
    // group 2
    // 3. check if opponent has a 4 threat, if so we must defend it
    //    if we can capture across it do so 1st, else block it
    // 4. check if opponent has a win by captures, try to capture it's attacker
    //    or block the capture
    // ---- (should always rank the above higher than rest, even if combined)
    // group 3
    // 5. check if I have a tria (potential open four), consider split 4 as well
    // 6. check if i have a potential 4 (split 4)
    // group 4
    // 7. check if opponent has a tria, capture, threaten capture, then block
    // ---- (should always rank the above higher than rest, even if combined)
    // group 5
    // 8. check if opponent has a potential 4 (split 4), capture, then block
    // 9. check if i can capture a pair
    // 10. check if I have a potential 3
    // 11. check if i can block a capture of my pieces

    // if max is group 1, remove everything else
    // if max is group 2, remove everything else
    // if max is group 3
    //   if nextmax is group 4
    //     remove group 5
    // if max is group 4
    //   remove group 5

    // maybe the rankings work by creating a list, inserting moves with
    // a ranking value for each threat.  1st check if the move exists, if
    // so add to the ranking value.  Then sort in the end.
    // since there are lines that separate possible moves, rankings should
    // be in stages, like 10000 for a 4 threat, 1000 for block to opponent's 4 threat
    // 100 for a tria, 10 for a potential 3, 2 for a capture, 2 for a block of a capture
    //
    // probably don't want to go this far -
    // 12. check if my opponent has a potential 3, block it.

    // rankings for offensive and defensive moves
    // note that potential captures are a little backward.
    // it seems like it will be hard to tell generically when it is a good
    // time to attempt to capture.  in the case of unblocking a 4 it is good,
    // but if it's just a simple capture, and instead you could create a 3 it's bad...
    // i also don't have a way to ramp up capture rank if it will end the game
    // i guess i could rescan rethreats, checking to see if any potential captures
    // are a keystone pair and then setting the rank very high in that case...
    // TODO tweek these rankings a bit

    // another problem with rankings.  should check offensive moves for opponent
    // better (if opponent has a pot4 + pot3 w/ one move, it needs to be defended!)

    //TYPE_BLOCKED = 0; //ignore except for capture analysis
    //TYPE_POTENTIAL_CAPTURE = 1;
    //TYPE_POTENTIAL_THREE_SPLIT = 2;
    //TYPE_POTENTIAL_THREE_PAIR = 3;
    //TYPE_POTENTIAL_FOUR = 4;
    //TYPE_TRIA = 5;
    //TYPE_CLOSED_FOUR = 6;
    //TYPE_OPEN_FOUR = 7;
    final int[] RANKS_OFFENSE = new int[] {
        1, 2, 10, 10, 1000, 1000, 100000, 100000
    };
    //final int[] GROUPS_OFFENSE = new int[] {
    //   6, 5, 5, 5, 3, 3, 1, 1
    //};
    final int[] RANKS_DEFENSE = new int[] {
        1, 2, 5, 5, 5, 100, 10000, 10000
    };
    //final int[] GROUPS_DEFENSE = new int[] {
    //    6, 6, 6, 6, 5, 4, 2, 2
    //};

    final int[] TYPE_PRIOR = new int[] {
        0, 0, 0, 0, 0, 3, 5, 5
    };

    public List<Rank> getNextMoveRanks() {


        // experimental
        // this is for the cases where all moves have at least the offensive
        // potential of the creation of the maximum offensive threat existing on the board now
        List<Threat> maxThreats = new ArrayList<Threat>();
        int minMovesToWin = 100;
        for (Threat t : threats[3 - player]) {
            if (t.movesToWin == 0) continue;
            else if (t.movesToWin < minMovesToWin) {
                minMovesToWin = t.movesToWin;
            }
        }
        for (Threat t : threats[3 - player]) {
            if (t.movesToWin == minMovesToWin) {
                maxThreats.add(t);
            }
        }
        minMovesToWin += 2;
        // end experimental

        Rank moves[] = new Rank[362];
        // look at threats of current player
        for (Iterator it = threats[3 - player].iterator(); it.hasNext();) {
            Threat t = (Threat) it.next();
            if (t.movesToWin == 0) continue; //no way to win w/o caps
            if (RANKS_OFFENSE[t.type] == 0) continue; //ignore 0 ranks
            for (int i = 0, j = t.next.size(); i < t.next.size(); i++, j--) {
                int m = ((Integer) t.next.get(i)).intValue();
                Rank r = moves[m];
                if (r != null) {
//                    if (log4j.isDebugEnabled()) {
//                        log4j.debug("adding " + (RANKS_OFFENSE[t.type] + j) + " to " + r);
//                    }
                    r.addOffenseRank(RANKS_OFFENSE[t.type] + j);
                    //if (GROUPS_OFFENSE[t.type] < r.group) {
                    //    r.group = GROUPS_OFFENSE[t.type];
                    //}
                    if (t.movesToWin < r.getOffenseGroup()) {
                        r.setOffenseGroup(t.movesToWin);
                    }
                }
                else {
                    r = new Rank(m);



                    r.setOffenseGroup(t.movesToWin);
                    r.addOffenseRank(RANKS_OFFENSE[t.type] + j);

                    // experimental
                    if (t.movesToWin > minMovesToWin) {
                        r.setOffenseGroup(minMovesToWin);
                        for (Threat mt : maxThreats) {
                            if (!mt.next.contains(m)) {
                                r.addOffenseRank(RANKS_OFFENSE[TYPE_PRIOR[mt.type]]);
                            }
                        }
                    }
                    // end experimental

                    moves[m] = r;
//                    if (log4j.isDebugEnabled()) {
//                        log4j.debug("creating rank " + r);
//                    }
                }
            }
        }
        //repeats above for opponents threats using RANKS_DEFENSE
        for (Iterator it = threats[player].iterator(); it.hasNext();) {
            Threat t = (Threat) it.next();
            if (t.movesToWin == 0) continue; //no way to win w/o caps
            if (RANKS_DEFENSE[t.type] == 0) continue; //ignore 0 ranks

            int movesToWin = t.movesToWin + 1; // since on defense
            if (t.type == Threat.TYPE_POTENTIAL_FOUR) {
                movesToWin++;
            }

            for (Iterator it2 = t.resps.iterator(); it2.hasNext();) {
                int m = ((Integer) it2.next()).intValue();
                if (m == 0) continue;
                Rank r = moves[m];
                if (r != null) {
//                    if (log4j.isDebugEnabled()) {
//                        log4j.debug("defense: adding " + RANKS_DEFENSE[t.type] + " to " + r);
//                    }
                    r.addDefenseRank(RANKS_DEFENSE[t.type]);
                    //if (GROUPS_DEFENSE[t.type] < r.group) {
                    //    r.group = GROUPS_DEFENSE[t.type];
                    //}
                    if (movesToWin < r.getDefenseGroup()) {
                        r.setDefenseGroup(movesToWin);
                    }
                }
                else {
                    r = new Rank(m);
                    r.setDefenseGroup(movesToWin);
                    r.addDefenseRank(RANKS_DEFENSE[t.type]);


                    // experimental
                    r.setOffenseGroup(minMovesToWin);
                    for (Threat mt : maxThreats) {
                        r.addOffenseRank(RANKS_OFFENSE[TYPE_PRIOR[mt.type]]);
                    }
                    // end experimental

                    moves[m] = r;

//                    if (log4j.isDebugEnabled()) {
//                        log4j.debug("defense: creating rank " + r);
//                    }
                }
            }
        }


        // find captures by me that can reveal hidden threats
        for (Iterator it = threats[3 - player].iterator(); it.hasNext();) {
            Threat t = (Threat) it.next();
            if (t.type == Threat.TYPE_POTENTIAL_CAPTURE) {
                int c1 = t.moves[0];
                int c2 = t.moves[1];
                int capMove = ((Integer) t.next.get(0)).intValue();
                Rank r = moves[capMove];
                for (Iterator it2 = threats[3 - player].iterator(); it2.hasNext();) {
                    Threat t2 = (Threat) it2.next();
                    if (t2.blocked == false) continue;
                    for (int i = 0; i < t2.numBlocks; i++) {
                        if (c1 == t2.blockPositions[i] ||
                            c2 == t2.blockPositions[i]) {

                            int posNewGroup = t2.blockedMovesToWin + 2; // +2 since have to capture to reveal threat
                            if (r.getOffenseGroup() > posNewGroup) {

                                //if (log4j.isDebugEnabled()) log4j.debug("updating group to " + posNewGroup + " for " + r);
                                r.setOffenseGroup(posNewGroup);
                            }
                            //if (log4j.isDebugEnabled()) log4j.debug("updating rank " + r + " by " + RANKS_OFFENSE[t2.typeSinceBlocked]);
                            r.addOffenseRank(RANKS_OFFENSE[t2.typeSinceBlocked]);
                        }
                    }
                }
            }
        }
        // find pairs to threaten revealing hidden threats

        // not really working yet because opponents threat is always
        // greater than my hidden one + 4, need to be able to reduce
        // opponents threat by 1 since we're capturing across it...
        for (Threat t : pairs[player]) {

            int c1 = t.moves[0];
            int c2 = t.moves[1];
            int attack1 = ((Integer) t.resps.get(0)).intValue();
            int attack2 = ((Integer) t.resps.get(1)).intValue();
            Rank r1 = moves[attack1];
            Rank r2 = moves[attack2];
            for (Threat t2 : threats[3 - player]) {

                if (t2.blocked == false) {
                    continue;
                }
                for (int i = 0; i < t2.numBlocks; i++) {
                    if (c1 == t2.blockPositions[i] ||
                        c2 == t2.blockPositions[i]) {

                        int posNewGroup = t2.blockedMovesToWin + 4; // +4 since have to setup cap and then make it
                        if (r1.getOffenseGroup() > posNewGroup) {
                            //if (log4j.isDebugEnabled()) log4j.debug("updating group to " + posNewGroup + " for " + r1);
                            r1.setOffenseGroup(posNewGroup);
                        }
                        //if (log4j.isDebugEnabled()) log4j.debug("updating rank " + r1 + " by " + RANKS_OFFENSE[t2.typeSinceBlocked]);
                        r1.addOffenseRank(RANKS_OFFENSE[t2.typeSinceBlocked]);

                        if (r2.getOffenseGroup() > posNewGroup) {
                            //if (log4j.isDebugEnabled()) log4j.debug("updating group to " + posNewGroup + " for " + r2);
                            r2.setOffenseGroup(posNewGroup);
                        }
                        //if (log4j.isDebugEnabled()) log4j.debug("updating rank " + r2 + " by " + RANKS_OFFENSE[t2.typeSinceBlocked]);
                        r2.addOffenseRank(RANKS_OFFENSE[t2.typeSinceBlocked]);
                    }
                }
            }
        }



        // sort by group and rank
        List<Rank> m = new ArrayList<Rank>(10);
        for (int i = 0; i < moves.length; i++) {
            if (moves[i] != null) {
                m.add(moves[i]);
            }
        }




        // now sort by offense and defense group to determine which ones
        // can be culled below
        Collections.sort(m, new Comparator<Rank>() {
            public int compare(Rank r1, Rank r2)
            {
                if (r2.getGroup() < r1.getGroup()) {
                    return 1;
                }
                else if (r2.getGroup() > r1.getGroup()) {
                    return -1;
                }
                else {
                    return r2.getRank() - r1.getRank();
                }
            }
        });

        // remove moves that are not necessary
        // determines this by the groupings.
        // if max is group 1, remove everything else
        // if max is group 2, remove everything else
        // if max is group 3
        //   if nextmax is group 4
        //     remove group 5
        // if max is group 4
        //   remove group 5
        if (!m.isEmpty()) {
            Rank curr = (Rank) m.get(0);
            int maxGroup = curr.getGroup();
            boolean threesAndFours = false;
            if (maxGroup == 3) {
                for (int i = 0; i < m.size(); i++) {
                    Rank r = (Rank) m.get(i);
                    if (r.getGroup() == 4) {
                        threesAndFours = true;
                        break;
                    }
                }
            }

            for (Iterator it = m.iterator(); it.hasNext();) {
                curr = (Rank) it.next();
                // if there is a group 1 or 2, and this one is in a group [345...], remove it
                if (maxGroup < 3 && curr.getGroup() > maxGroup) {
                    //if (log4j.isDebugEnabled()) log4j.debug("removing rank " + curr);
                    it.remove();
                }
                // group 3 and 5,6 can be combined only if there isn't a 4
                // if there are 3's and 4's, all 3's and 4's will remain
                else if (maxGroup == 3 && threesAndFours && curr.getGroup() >= 5) {
                    //if (log4j.isDebugEnabled()) log4j.debug("removing rank " + curr);
                    it.remove();
                }
                // if anything in group 4, remove 6
                else if (maxGroup == 4 && curr.getGroup() >= 5) {
                   //if (log4j.isDebugEnabled()) log4j.debug("removing rank " + curr);
                    it.remove();
                }
            }
        }

//        if (log4j.isDebugEnabled()) {
//            for (Rank r : m) {
//                log4j.debug(r);
//            }
//        }
        return m;
    }
    public int[] getNextMoves() {

        List<Rank> m = getNextMoveRanks();

        // convert List of Ranks to int[]
        //log4j.debug("remaining ranks...");
        int r[] = new int[m.size()];
        for (int i = 0; i < r.length; i++) {
            Rank rank = (Rank) m.get(i);
            //if (log4j.isDebugEnabled()) log4j.debug(rank);
            r[i] = rank.move;
        }
        return r;
    }

    /** for each potential capture that i can make, scan all other threats
     *  and see if the capture can disrupt the threat, if so add the capture
     *  move as a response
     *
     *
     */
    public void analyzeCaptures() {
        for (Iterator it = threats[3 - player].iterator(); it.hasNext();) {
            Threat t = (Threat) it.next();
            if (t.type == Threat.TYPE_POTENTIAL_CAPTURE) {
                outer: for (Iterator it2 = threats[player].iterator(); it2.hasNext();) {
                    Threat t2 = (Threat) it2.next();

                    for (int i = 0; i < t.numMoves; i++) {
                        for (int j = 0; j < t2.numMoves; j++) {
                            if (t.moves[i] == t2.moves[j]) {
                                int resp = ((Integer) t.resps.get(0)).intValue();
                                //if (log4j.isDebugEnabled()) log4j.debug("found that capture " + t +
                                //    " across " + t2 + " works, add resp "
                                //    + Utils.printMove(resp));
                                t2.addResp(resp);
                                continue outer;
                            }
                        }
                    }
                }
            }
        }
        for (Threat t : pairs[3 - player]) {

            outer: for (Threat t2 : threats[player]) {

                // opponent has a 4, so no time to capture
                if (t2.type > Threat.TYPE_TRIA) continue;
                for (int i = 0; i < t.numMoves; i++) {
                    for (int j = 0; j < t2.numMoves; j++) {
                        if (t.moves[i] == t2.moves[j]) {
                            int resp1 = ((Integer) t.resps.get(0)).intValue();
                            int resp2 = ((Integer) t.resps.get(1)).intValue();
                            //if (log4j.isDebugEnabled()) log4j.debug("found that setup capture " + t +
                            //    " across " + t2 + " works, add resps "
                            //    + Utils.printMove(resp1) + "," + Utils.printMove(resp2));
                            t2.addResp(resp1);
                            t2.addResp(resp2);
                            continue outer;
                        }
                    }
                }
            }
        }


        // look for a position that returns the maximum captures
        // if max + already captures > 5 then elevate the group to top since its a win bitch!
        int capBoard[] = new int[362];
        for (Iterator it = threats[3 - player].iterator(); it.hasNext();) {
            Threat t = (Threat) it.next();
            if (t.type == Threat.TYPE_POTENTIAL_CAPTURE) {
                capBoard[t.captureMove] += 2;
            }
        }

        for (Iterator it = threats[3 - player].iterator(); it.hasNext();) {
            Threat t = (Threat) it.next();
            if (t.type == Threat.TYPE_POTENTIAL_CAPTURE) {
                if (capBoard[t.captureMove] + caps[3 - player] > 8) {
                    capBoard[t.captureMove] = 0; //reset so only ONE capture gets its rank updated
                    t.movesToWin = 1;
                    //if (log4j.isDebugEnabled()) log4j.debug("found win by capture " + t);
                }
            }
        }

        // look for a position that returns the maximum captures on MY PIECES
        // if max + already captures > 5 then elevate the group to top since its a loser bitch!
        capBoard = new int[362];
        for (Iterator it = threats[player].iterator(); it.hasNext();) {
            Threat t = (Threat) it.next();
            if (t.type == Threat.TYPE_POTENTIAL_CAPTURE) {
                capBoard[t.captureMove] += 2;
            }
        }

        for (Iterator it = threats[player].iterator(); it.hasNext();) {
            Threat t = (Threat) it.next();
            if (t.type == Threat.TYPE_POTENTIAL_CAPTURE) {
                if (capBoard[t.captureMove] + caps[player] > 8) {
                    capBoard[t.captureMove] = 0; //reset so only ONE capture gets its rank updated
                    t.movesToWin = 1;
                    //if (log4j.isDebugEnabled()) log4j.debug("found lose by capture " + t);
                }
            }
        }

        //TODO elevate movesToWin by chasing pairs as well if that leads to a win
    }

    public String toString() {
        //if (!log4j.isDebugEnabled()) return "";

        StringBuffer buf = new StringBuffer(1024);
        for (int i = 1; i < 3; i++) {
            buf.append("Threats for player " + i + ":\n");
            for (Iterator it = threats[i].iterator(); it.hasNext();) {
                Threat threat = (Threat) it.next();
                buf.append(threat + "\n");
            }
        }
        return buf.toString();
    }
}