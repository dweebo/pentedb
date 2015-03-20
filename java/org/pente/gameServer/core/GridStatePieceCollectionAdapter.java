package org.pente.gameServer.core;

import java.util.*;

import org.pente.game.*;

/**
 * @author dweebo
 */
public class GridStatePieceCollectionAdapter extends GridStateDecorator {

    protected Vector  listeners;
    protected Vector  gridPieces;

    public GridStatePieceCollectionAdapter(GridState gridState) {
        super(gridState);

        listeners = new Vector();
        gridPieces = new Vector();
    }
    public GridStatePieceCollectionAdapter(int boardSizeX, int boardSizeY) {
        super(boardSizeX, boardSizeY);

        listeners = new Vector();
        gridPieces = new Vector();
    }

    public void addOrderedPieceCollectionListener(OrderedPieceCollection pieceCollection) {
        listeners.addElement(pieceCollection);
    }
    public void removePieceCollectionListener(OrderedPieceCollection pieceCollection) {
        listeners.removeElement(pieceCollection);
    }

    /** Clears the grid state */
    public void clear() {
        gridState.clear();
        gridPieces.removeAllElements();
        for (int i = 0; i < listeners.size(); i++) {
            OrderedPieceCollection o = (OrderedPieceCollection) listeners.elementAt(i);
            o.clearPieces();
        }
    }

    /** Add a move for this board
     *  @param move An integer representation of a move
     */
    public void addMove(int move) {

        // add grid piece
        GridPiece p = new SimpleGridPiece();
        p.setPlayer(gridState.getCurrentColor());
        p.setDepth(gridState.getNumMoves() + 1);
        int x = move % gridState.getGridSizeX();
        int y = gridState.getGridSizeY() - move / gridState.getGridSizeX() - 1;
        p.setX(x);
        p.setY(y);

        gridState.addMove(move);

        GridPieceAction a = new GridPieceAction(p, gridState.getNumMoves(), GridPieceAction.ADD);
        gridPieces.addElement(a);

        for (int i = 0; i < listeners.size(); i++) {
            OrderedPieceCollection o = (OrderedPieceCollection) listeners.elementAt(i);
            o.addPiece(p, gridState.getNumMoves());
        }
    }

    /** Undo the last move */
    public void undoMove() {

        for (int i = 0; i < gridPieces.size(); i++) {
            GridPieceAction a = (GridPieceAction) gridPieces.elementAt(i);
            if (a.getTurn() == gridState.getNumMoves()) {
                gridPieces.removeElementAt(i);
            }
        }

        gridState.undoMove();

        for (int i = 0; i < listeners.size(); i++) {
            OrderedPieceCollection o = (OrderedPieceCollection) listeners.elementAt(i);
            o.undoLastTurn();
        }
    }
    public void printBoard() {
    	gridState.printBoard();
    }
}