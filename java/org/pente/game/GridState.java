package org.pente.game;


/** Interface to classes which represent the state of the board in a grid based
 *  game.  It also holds the order of moves and has some utility methods to
 *  determine other information about the current state of the board.
 *
 *  @author dweebo
 */
public interface GridState extends MoveData {

    /** Determine if a move is valid in this grid state
     *  @param move An integer representation of a move
     *  @param player The player making the move
     *  @return boolean True if the move is valid
     */
    public boolean isValidMove(int move, int player);

    /** Determines if a player is allowed to request
     *  and undo with the current state of the grid
     *  @param player The player requesting an undo
     *  @return boolean True if the player can request an undo
     */
    public boolean canPlayerUndo(int player);

    /** Determine if the game is over
     *  @return boolean True if game is over
     */
    public boolean isGameOver();

    /** If the game is over calling this will return
     *  which player has one.  If game is not over will return 0
     *  @return int The player who won the game
     */
    public int getWinner();


    /** Clears the grid state */
    public void clear();

    public int getGridSizeX();
    public int getGridSizeY();


    /** Get whose color it is
     *  @return int The current color (1, 2, etc.)
     */
    public int getCurrentColor();

    /** Get whose turn it is
     *  @return int The current player (1, 2, etc.)
     */
    public int getCurrentPlayer();

    public int getColor(int moveNum);

    /** Get the whole board
     *  @return int[][] 2 dimensional array with an integer for each position
     */
    public int[][] getBoard();


    /** Get info about a position
     *  @param position The position on the board
     *  @return int The value associated with this position
     */
    public int getPosition(int position);

    /** Get info about a position
     *  @param x The horizontal position from left to right
     *  @param y The vertical position from top to bottom
     *  @return int The value associated with this position
     */
    public int getPosition(int x, int y);

    /** Set a position
     *  @param position The position on the board
     *  @param value The value to put at this position
     */
    public void setPosition(int position, int value);

    /** Set a position
     *  @param x The horizontal position from left to right
     *  @param y The vertical position from top to bottom
     *  @param value The value to put at this position
     */
    public void setPosition(int x, int y, int value);


    /** Converts coordinates to a single variable
     *  @param x The horizontal position from left to right
     *  @param y The vertical position from top to bottom
     *  @return int The move
     */
    public int convertMove(int x, int y);

    /** Converts a single variable move to its x,y coordinates
     *  @param move The move to convert
     *  @return Coord A point variable with x and y set
     */
    public Coord convertMove(int move);

    /** Determine if the pieces on the board are the same
     *  @param gridState The grid state to compare agains
     *  @return boolean True if the position is the same
     */
    public boolean positionEquals(GridState gridState);


    public GridState getInstance(MoveData moveData);

    // game hashing functions
    public long getHash();
    public long getHash(int index);
    public long[][] getHashes();
    public void updateHash(HashCalculator calc);

    public int getRotation();
    public int getRotation(int index);
    public int[] getRotations();

    public int rotateMove(int move, int newRotation);
    public int rotateMoveToLocalRotation(int move, int newRotation);

    public int[] getAllPossibleRotations(int move, int newRotation);
    // callbacks for subclasses to generate unique hash values
    //public void setHasher(GridState state);
    //public void updateHashes();
    //public int getHashForRotation(int rotation, int move, int moveNumber,
    //    int player, int currentHash);
    //public int getHashForPosition(int move, int player);
    //public int calculateFinalHash(int currentHash, int moveNumber);
    public void printBoard();
}