package org.pente.game;

/**
 * @author dweebo
 */
public class SynchronizedGridState implements GridState {

    /** The base grid state that does the real work */
    protected GridState   gridState;

    /** Wrap this grid state around another grid state
     *  @param gridState The base grid state to call all methods on
     */
    public SynchronizedGridState(GridState gridState) {
        this.gridState = gridState;
    }

    /** Determine if a move is valid in this grid state
     *  @param move An integer representation of a move
     *  @param player The player making the move
     */
    public synchronized boolean isValidMove(int move, int player) {
        return gridState.isValidMove(move, player);
    }

    /** Determines if a player is allowed to request
     *  and undo with the current state of the grid
     *  @param player The player requesting an undo
     *  @return boolean True if the player can request an undo
     */
    public synchronized boolean canPlayerUndo(int player) {
        return gridState.canPlayerUndo(player);
    }

    public synchronized boolean isGameOver() {
        return gridState.isGameOver();
    }
    public synchronized int getWinner() {
        return gridState.getWinner();
    }

    /** Clears the grid state */
    public synchronized void clear() {
        gridState.clear();
    }

    public synchronized int getGridSizeX() {
        return gridState.getGridSizeX();
    }
    public synchronized int getGridSizeY() {
        return gridState.getGridSizeY();
    }

    /** Add a move for this board
     *  @param move An integer representation of a move
     */
    public synchronized void addMove(int move) {
        gridState.addMove(move);
    }

    /** Undo the last move */
    public synchronized void undoMove() {
        gridState.undoMove();
    }

    /** Get the number of moves for this board
     *  @return The number of moves
     */
    public synchronized int getNumMoves() {
        return gridState.getNumMoves();
    }

    /** Get a move for this board
     *  @param num The sequence number of the move
     */
    public synchronized int getMove(int moveNum) {
        return gridState.getMove(moveNum);
    }

    public synchronized int[] getMoves() {
        return gridState.getMoves();
    }

    /** Get which color it is
     *  @return int The current player (1, 2, etc.)
     */
    public synchronized int getCurrentColor() {
        return gridState.getCurrentColor();
    }

    /** Get whose turn it is
     *  @return int The current player (1, 2, etc.)
     */
    public synchronized int getCurrentPlayer() {
        return gridState.getCurrentPlayer();
    }

    public int getColor(int moveNum) {
    	return gridState.getColor(moveNum);
    }
    /** Get info about a position
     *  @param position The position on the board
     *  @return int The value associated with this position
     */
    public synchronized int getPosition(int position) {
        return gridState.getPosition(position);
    }

    /** Get info about a position
     *  @param x The horizontal position from left to right
     *  @param y The vertical position from top to bottom
     *  @return int The value associated with this position
     */
    public synchronized int getPosition(int x, int y) {
        return gridState.getPosition(x, y);
    }

    /** Get the whole board
     *  @return int[][] 2 dimensional array with an integer for each position
     */
    public synchronized int[][] getBoard() {
        return gridState.getBoard();
    }

    /** Set a position
     *  @param position The position on the board
     *  @param value The value to put at this position
     */
    public synchronized void setPosition(int position, int value) {
        gridState.setPosition(position, value);
    }

    /** Set a position
     *  @param x The horizontal position from left to right
     *  @param y The vertical position from top to bottom
     *  @param value The value to put at this position
     */
    public synchronized void setPosition(int x, int y, int value) {
        gridState.setPosition(x, y, value);
    }

    /** Converts coordinates to a single variable
     *  @param x The horizontal position from left to right
     *  @param y The vertical position from top to bottom
     *  @return int The move
     */
    public synchronized int convertMove(int x, int y) {
        return gridState.convertMove(x, y);
    }

    /** Converts a single variable move to its x,y coordinates
     *  @param move The move to convert
     *  @return Coord A point variable with x and y set
     */
    public synchronized Coord convertMove(int move) {
        return gridState.convertMove(move);
    }

    /** Determine if the pieces on the board are the same
     *  @param gridState The grid state to compare agains
     *  @return boolean True if the position is the same
     */
    public boolean positionEquals(GridState gridState) {
        return gridState.positionEquals(gridState);
    }

    public synchronized GridState getInstance(MoveData moveData) {
        return gridState.getInstance(moveData);
    }


    public synchronized long getHash() {
        return gridState.getHash();
    }
    public synchronized long getHash(int index) {
        return gridState.getHash(index);
    }
    public synchronized long[][] getHashes() {
    	return gridState.getHashes();
    }
    public void updateHash(HashCalculator calc) {
        gridState.updateHash(calc);
    }

    public synchronized int getRotation() {
        return gridState.getRotation();
    }
    public synchronized int getRotation(int index) {
        return gridState.getRotation(index);
    }
    public synchronized int[] getRotations() {
    	return gridState.getRotations();
    }


    public synchronized int rotateMove(int move, int newRotation) {
        return gridState.rotateMove(move, newRotation);
    }
    public synchronized int rotateMoveToLocalRotation(int move, int newRotation) {
        return gridState.rotateMoveToLocalRotation(move, newRotation);
    }
    public synchronized int[] getAllPossibleRotations(int move, int newRotation) {
    	return gridState.getAllPossibleRotations(move, newRotation);
    }
    public void printBoard() {
    	gridState.printBoard();
    }
}