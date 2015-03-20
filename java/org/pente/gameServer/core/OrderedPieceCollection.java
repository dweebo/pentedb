package org.pente.gameServer.core;

/**
 * @author dweebo
 */
public interface OrderedPieceCollection {

    public void addPiece(GridPiece gridPiece, int turn);
    public void removePiece(GridPiece gridPiece, int turn);
    public void undoLastTurn();
    public void clearPieces();

    public void visitNextTurn();
    public void visitPreviousTurn();
    public void visitFirstTurn();
    public void visitLastTurn();
    public void visitTurn(int turn);
}