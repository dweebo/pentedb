package org.pente.gameServer.core;

/**
 * @author dweebo
 */
public interface PieceCollection {

    public void addPiece(GridPiece gridPiece);
    public void removePiece(GridPiece gridPiece);
    public void updatePiecePlayer(int x, int y, int player);
    public void clearPieces();
}