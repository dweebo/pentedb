package org.pente.game;

import java.awt.*;

/** Implements the Adapter/Decorator patterns.  Overrides all methods and passes
 *  the control to the base grid state.  This class is meant to be overridden
 *  by subclasses wishing to only override a few methods, the subclass can
 *  then not worry about implementing the rest of the GridState methods.
 *
 *  @author dweebo
 */
public abstract class GridStateDecorator implements GridState {

    /** The base grid state that does the real work */
    protected GridState   gridState;

    /** Wrap this grid state around another grid state
     *  @param gridState The base grid state to call all methods on
     */
    public GridStateDecorator(GridState gridState) {
        this.gridState = gridState;
    }

    /** Create a simple grid state as the base grid state
     *  @param boardSize The size of the board
     */
    public GridStateDecorator(int boardSizeX, int boardSizeY) {
        gridState = new SimpleGridState(boardSizeX, boardSizeY);
    }

    /** Determine if a move is valid in this grid state
     *  @param move An integer representation of a move
     *  @param player The player making the move
     */
    public boolean isValidMove(int move, int player) {
        return gridState.isValidMove(move, player);
    }

    /** Determines if a player is allowed to request
     *  and undo with the current state of the grid
     *  @param player The player requesting an undo
     *  @return boolean True if the player can request an undo
     */
    public boolean canPlayerUndo(int player) {
        return gridState.canPlayerUndo(player);
    }


    public boolean isGameOver() {
        return gridState.isGameOver();
    }
    public int getWinner() {
        return gridState.getWinner();
    }

    /** Clears the grid state */
    public void clear() {
        gridState.clear();
    }

    public int getGridSizeX() {
        return gridState.getGridSizeX();
    }
    public int getGridSizeY() {
        return gridState.getGridSizeY();
    }

    /** Add a move for this board
     *  @param move An integer representation of a move
     */
    public void addMove(int move) {
        gridState.addMove(move);
    }

    /** Undo the last move */
    public void undoMove() {
        gridState.undoMove();
    }

    /** Get the number of moves for this board
     *  @return The number of moves
     */
    public int getNumMoves() {
        return gridState.getNumMoves();
    }

    /** Get a move for this board
     *  @param num The sequence number of the move
     */
    public int getMove(int moveNum) {
        return gridState.getMove(moveNum);
    }

    public int[] getMoves() {
        return gridState.getMoves();
    }

    /** Get which color it is
     *  @return int The current player (1, 2, etc.)
     */
    public int getCurrentColor() {
        return gridState.getCurrentColor();
    }

    public int getColor(int moveNum) {
    	return gridState.getColor(moveNum);
    }
    /** Get whose turn it is
     *  @return int The current player (1, 2, etc.)
     */
    public int getCurrentPlayer() {
        return gridState.getCurrentPlayer();
    }

    /** Get info about a position
     *  @param position The position on the board
     *  @return int The value associated with this position
     */
    public int getPosition(int position) {
        return gridState.getPosition(position);
    }

    /** Get info about a position
     *  @param x The horizontal position from left to right
     *  @param y The vertical position from top to bottom
     *  @return int The value associated with this position
     */
    public int getPosition(int x, int y) {
        return gridState.getPosition(x, y);
    }

    /** Get the whole board
     *  @return int[][] 2 dimensional array with an integer for each position
     */
    public int[][] getBoard() {
        return gridState.getBoard();
    }

    /** Set a position
     *  @param position The position on the board
     *  @param value The value to put at this position
     */
    public void setPosition(int position, int value) {
        gridState.setPosition(position, value);
    }

    /** Set a position
     *  @param x The horizontal position from left to right
     *  @param y The vertical position from top to bottom
     *  @param value The value to put at this position
     */
    public void setPosition(int x, int y, int value) {
        gridState.setPosition(x, y, value);
    }

    /** Converts coordinates to a single variable
     *  @param x The horizontal position from left to right
     *  @param y The vertical position from top to bottom
     *  @return int The move
     */
    public int convertMove(int x, int y) {
        return gridState.convertMove(x, y);
    }

    /** Converts a single variable move to its x,y coordinates
     *  @param move The move to convert
     *  @return Coord A point variable with x and y set
     */
    public Coord convertMove(int move) {
        return gridState.convertMove(move);
    }

    /** Determine if the pieces on the board are the same
     *  @param gridState The grid state to compare agains
     *  @return boolean True if the position is the same
     */
    public boolean positionEquals(GridState gridState) {
        return this.gridState.positionEquals(gridState);
    }


    public GridState getInstance(MoveData moveData) {
        return gridState.getInstance(moveData);
    }

    public long getHash() {
        return gridState.getHash();
    }
    public long getHash(int index) {
        return gridState.getHash(index);
    }
    public long[][] getHashes() {
    	return gridState.getHashes();
    }
    public void updateHash(HashCalculator calc) {
        gridState.updateHash(calc);
    }

    public int getRotation() {
        return gridState.getRotation();
    }
    public int getRotation(int index) {
        return gridState.getRotation(index);
    }
    public int[] getRotations() {
    	return gridState.getRotations();
    }

    public int rotateMove(int move, int newRotation) {
        return gridState.rotateMove(move, newRotation);
    }
    public int rotateMoveToLocalRotation(int move, int newRotation) {
        return gridState.rotateMoveToLocalRotation(move, newRotation);
    }
    public int[] getAllPossibleRotations(int move, int newRotation) {
    	return gridState.getAllPossibleRotations(move, newRotation);
    }
}