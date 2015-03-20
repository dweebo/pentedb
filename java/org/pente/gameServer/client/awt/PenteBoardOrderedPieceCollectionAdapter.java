package org.pente.gameServer.client.awt;

import org.pente.gameServer.client.*;
import org.pente.gameServer.core.*;

/**
 * @author dweebo
 */
public class PenteBoardOrderedPieceCollectionAdapter extends GridBoardOrderedPieceCollectionAdapter {

    PenteBoardComponent penteBoardComponent;

    public PenteBoardOrderedPieceCollectionAdapter(
        PenteBoardComponent basePieceCollection,
        boolean allowMovesWhileViewingHistory) {

        super(basePieceCollection, allowMovesWhileViewingHistory);

        this.penteBoardComponent = basePieceCollection;
    }

    public synchronized void removePiece(GridPiece gridPiece, int turn) {
        super.removePiece(gridPiece, turn);

        if (viewingCurrent) {
            penteBoardComponent.incrementCaptures(3 - gridPiece.getPlayer());
        }
    }
    public synchronized void undoLastTurn() {

        // look for pieces that were removed in the last turn
        // since we're undoing them, decrement the number of captures
        if (viewingCurrent && maxTurn > 0) {
            changeCaptures(maxTurn, false);
        }

        super.undoLastTurn();
    }

    public synchronized void visitNextTurn() {

        if (currentTurn < maxTurn) {
            changeCaptures(currentTurn + 1, true);
        }

        super.visitNextTurn();
    }
    public synchronized void visitPreviousTurn() {
        if (currentTurn > 0) {
            changeCaptures(currentTurn, false);
        }
        super.visitPreviousTurn();
    }

    public synchronized void visitLastTurn() {

        if (currentTurn < maxTurn) {
            for (int i = 0; i < pieceActions.size(); i++) {
                GridPieceAction action = (GridPieceAction) pieceActions.elementAt(i);
                if (action.getTurn() > currentTurn) {
                    if (action.getAction() == GridPieceAction.REMOVE) {
                        penteBoardComponent.incrementCaptures(3 - action.getGridPiece().getPlayer());
                    }
                }
            }
        }

        super.visitLastTurn();
    }
    public synchronized void visitTurn(int turn) {

        if (turn < 0 || turn > maxTurn || turn == currentTurn) {
            return;
        }
        // we're going forward
        else if (currentTurn < turn) {

            for (int i = 0; i < pieceActions.size(); i++) {
                GridPieceAction action = (GridPieceAction) pieceActions.elementAt(i);
                if (action.getTurn() <= turn &&
                    action.getTurn() > currentTurn) {
                    if (action.getAction() == GridPieceAction.REMOVE) {
                        penteBoardComponent.incrementCaptures(3 - action.getGridPiece().getPlayer());
                    }
                }
            }
        }
        // we're going backward
        else if (currentTurn > turn) {
            for (int i = pieceActions.size() - 1; i >= 0; i--) {
                GridPieceAction action = (GridPieceAction) pieceActions.elementAt(i);
                if (action.getTurn() > turn &&
                    action.getTurn() <= currentTurn) {
                    if (action.getAction() == GridPieceAction.REMOVE) {
                        penteBoardComponent.decrementCaptures(3 - action.getGridPiece().getPlayer());
                    }
                }
            }
        }

        super.visitTurn(turn);
    }

    void changeCaptures(int turn, boolean increment) {
        for (int i = 0; i < pieceActions.size(); i++) {
            GridPieceAction action = (GridPieceAction) pieceActions.elementAt(i);
            if (action.getTurn() == turn) {
                if (action.getAction() == GridPieceAction.REMOVE) {
                    if (increment) {
                        penteBoardComponent.incrementCaptures(3 - action.getGridPiece().getPlayer());
                    }
                    else {
                        penteBoardComponent.decrementCaptures(3 - action.getGridPiece().getPlayer());
                    }
                }
            }
        }
    }
}