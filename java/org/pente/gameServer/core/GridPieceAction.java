package org.pente.gameServer.core;

/**
 * @author dweebo
 */
public class GridPieceAction {

    public static final int ADD = 1;
    public static final int REMOVE = 2;
    public static final int POOF = 3;

    private GridPiece   gridPiece;
    private int         turn;
    private int         action;

    public GridPieceAction(GridPiece gridPiece, int turn, int action) {
        this.gridPiece = gridPiece;
        this.turn = turn;
        this.action = action;
    }

    public GridPiece getGridPiece() {
        return gridPiece;
    }
    public int getTurn() {
        return turn;
    }
    public int getAction() {
        return action;
    }

    public String toString() {
        return "Turn: " + turn + ", action: " + action + ", gridpiece: " + gridPiece;
    }
}