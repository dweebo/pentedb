package org.pente.gameServer.core;

import java.util.*;

/**
 * @author dweebo
 */
public class OrderedPieceCollectionAdapter implements OrderedPieceCollection {

    private PieceCollection     basePieceCollection;

    protected Vector            pieceActions;
    protected int               currentTurn;
    protected int               maxTurn;
    protected boolean           viewingCurrent;

    public OrderedPieceCollectionAdapter(PieceCollection basePieceCollection) {
        this.basePieceCollection = basePieceCollection;

        pieceActions = new Vector();
        currentTurn = 0;
        maxTurn = currentTurn;
        viewingCurrent = true;
    }

    public synchronized void addPiece(GridPiece gridPiece, int turn) {

        GridPieceAction action = new GridPieceAction(gridPiece, turn, GridPieceAction.ADD);
        pieceActions.addElement(action);

        if (turn > maxTurn) {
            maxTurn = turn;
        }

        if (viewingCurrent) {
            if (turn > currentTurn) {
                currentTurn = turn;
            }
            basePieceCollection.addPiece(gridPiece);
        }
    }

    public synchronized void removePiece(GridPiece gridPiece, int turn) {

        GridPieceAction action = new GridPieceAction(gridPiece, turn, GridPieceAction.REMOVE);
        pieceActions.addElement(action);

        // update the max turn
        if (turn > maxTurn) {
            maxTurn = turn;
        }

        if (viewingCurrent) {
            if (turn > currentTurn) {
                currentTurn = turn;
            }
            basePieceCollection.removePiece(gridPiece);
        }
    }

    public synchronized void undoLastTurn() {

        if (maxTurn > 0) {
            for (int i = pieceActions.size() - 1; i >= 0; i--) {
                GridPieceAction action = (GridPieceAction) pieceActions.elementAt(i);
                if (action.getTurn() == maxTurn) {
                    pieceActions.removeElementAt(i);
                    if (viewingCurrent) {
                        if (action.getAction() == GridPieceAction.ADD) {
                            basePieceCollection.removePiece(action.getGridPiece());
                        }
                        else if (action.getAction() == GridPieceAction.REMOVE) {
                            basePieceCollection.addPiece(action.getGridPiece());
                        }
                    }
                }
            }

            maxTurn--;

            if (viewingCurrent) {
                currentTurn--;
            }

            else if (currentTurn == maxTurn) {
                viewingCurrent = true;
            }
        }
    }

    public synchronized void clearPieces() {

        basePieceCollection.clearPieces();
        pieceActions.removeAllElements();
        currentTurn = 0;
        maxTurn = currentTurn;
        viewingCurrent = true;
    }

    public synchronized void visitNextTurn() {
        if (currentTurn < maxTurn) {
            currentTurn++;

            for (int i = 0; i < pieceActions.size(); i++) {
                GridPieceAction action = (GridPieceAction) pieceActions.elementAt(i);
                if (action.getTurn() == currentTurn) {
                    if (action.getAction() == GridPieceAction.ADD) {
                        basePieceCollection.addPiece(action.getGridPiece());
                    }
                    else if (action.getAction() == GridPieceAction.REMOVE) {
                        basePieceCollection.removePiece(action.getGridPiece());
                    }
                }
            }
            if (currentTurn == maxTurn) {
                viewingCurrent = true;
            }
        }
    }

    public synchronized void visitPreviousTurn() {

        if (currentTurn > 0) {

            // loop in reverse order to account for poofs (added first then removed)
            // in reverse they will be
            for (int i = pieceActions.size(); i > 0; i--) {
                GridPieceAction action = (GridPieceAction) pieceActions.elementAt(i - 1);
                if (action.getTurn() == currentTurn) {
                    if (action.getAction() == GridPieceAction.REMOVE) {
                        basePieceCollection.addPiece(action.getGridPiece());
                    }
                    else if (action.getAction() == GridPieceAction.ADD) {
                        basePieceCollection.removePiece(action.getGridPiece());
                    }
                }
            }

            currentTurn--;
            viewingCurrent = false;
        }
    }

    // no special processing needed for poofs
    public synchronized void visitFirstTurn() {

        if (currentTurn > 0) {
            currentTurn = 0;
            basePieceCollection.clearPieces();
            viewingCurrent = false;
        }
    }

    // no special processing needed for poofs
    // poofs have constraint that always added first, then removed
    public synchronized void visitLastTurn() {

        if (currentTurn < maxTurn) {

            for (int i = 0; i < pieceActions.size(); i++) {
                GridPieceAction action = (GridPieceAction) pieceActions.elementAt(i);

                if (action.getTurn() > currentTurn) {
                    // what if order in vector is remove, then add?
                    // will remove first, then add leading to incorrect result
                    if (action.getAction() == GridPieceAction.ADD) {
                        basePieceCollection.addPiece(action.getGridPiece());
                    }
                    else if (action.getAction() == GridPieceAction.REMOVE) {
                        basePieceCollection.removePiece(action.getGridPiece());
                    }
                }
            }

            currentTurn = maxTurn;
            viewingCurrent = true;
        }
    }

    public void visitTurn(int turn) {

        if (turn < 0 || turn > maxTurn || turn == currentTurn) {
            return;
        }
        // we're going forward
        else if (currentTurn < turn) {

            for (int i = 0; i < pieceActions.size(); i++) {
                GridPieceAction action = (GridPieceAction) pieceActions.elementAt(i);

                if (action.getTurn() <= turn &&
                    action.getTurn() > currentTurn) {
                    if (action.getAction() == GridPieceAction.ADD) {
                        basePieceCollection.addPiece(action.getGridPiece());
                    }
                    else if (action.getAction() == GridPieceAction.REMOVE) {
                        basePieceCollection.removePiece(action.getGridPiece());
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
                    if (action.getAction() == GridPieceAction.ADD) {
                        basePieceCollection.removePiece(action.getGridPiece());
                    }
                    else if (action.getAction() == GridPieceAction.REMOVE) {
                        basePieceCollection.addPiece(action.getGridPiece());
                    }
                }
            }
        }

        currentTurn = turn;

        if (currentTurn == maxTurn) {
            viewingCurrent = true;
        }
        else {
            viewingCurrent = false;
        }
    }
}