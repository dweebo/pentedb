package org.pente.tree;

/**
 * @author dweebo
 */
class Rank {

    int move;
    private int offenseRank;
    private int defenseRank;
    private int offenseGroup = 100; //default bad group
    private int defenseGroup = 100; //default bad group

    Rank(int move) {
        this.move = move;
    }
    public void addOffenseRank(int rank) {
        this.offenseRank += rank;
    }
    public void addDefenseRank(int rank) {
        this.defenseRank += rank;
    }
    public int getOffenseRank() {
        return offenseRank;
    }
    public int getDefenseRank() {
        return defenseRank;
    }
    public int getRank() {
        return offenseRank + defenseRank;
    }
    public void setOffenseGroup(int group) {
        this.offenseGroup = group;
    }
    public int getOffenseGroup() {
        return offenseGroup;
    }
    public int getDefenseGroup() {
        return defenseGroup;
    }
    public void setDefenseGroup(int defenseGroup) {
        this.defenseGroup = defenseGroup;
    }
    public int getGroup() {
        return offenseGroup < defenseGroup ? offenseGroup : defenseGroup;
    }

    public boolean equals(Object o) {
        Rank r = (Rank) o;
        return move == r.move;
    }
    public int hashCode() {
        return new Integer(move).hashCode();
    }

//    public String toString() {
//        return "[offense=" + offenseGroup + ":" + offenseRank + ",defense=" +
//            + defenseGroup + ":" + defenseRank + ", " + Utils.printMove(move) + "]";
//    }
}
