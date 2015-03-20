package org.pente.game;

/**
 * @author dweebo
 */
public class SimpleConnect6State extends GridStateDecorator
    implements GridState, HashCalculator {

    private int[]   surrounding;
    private boolean allowOverlines = true;
    private int winner;

    public SimpleConnect6State() {
        this(19, 19);
    }

    /** Create an empty gomoku state wrapped around the given GridState
     *  @param gridState The base GridState to use
     */
    public SimpleConnect6State(GridState gridState) {
        super(gridState);

        int boardSizeX = gridState.getGridSizeX();
        surrounding = new int[] { -1, -boardSizeX - 1, -boardSizeX, -boardSizeX + 1 };
    }

    /** Create a gomoku state with a certain board size
     *  @param boardSize The board size
     */
    public SimpleConnect6State(int boardSizeX, int boardSizeY) {
        super(boardSizeX, boardSizeY);

        surrounding = new int[] { -1, -boardSizeX - 1, -boardSizeX, -boardSizeX + 1 };
    }

    /** Create a new gomoku grid state with a specified size and add the moves
     *  contained in the passed in MoveData to the board.
     *  @param data MoveData containing a list of moves
     *  @param boardSize The size of the board
     */
    public SimpleConnect6State(MoveData data, int boardSizeX, int boardSizeY) {
        this(boardSizeX, boardSizeY);

        for (int i = 0; i < data.getNumMoves(); i++) {
            addMove(data.getMove(i));
        }
    }

    public GridState getInstance(MoveData moveData) {
        SimpleConnect6State state = new SimpleConnect6State(moveData,
            gridState.getGridSizeX(), gridState.getGridSizeY());

        return state;
    }



    public int getCurrentPlayer() {
    	return getColor(getNumMoves());
    }


    public int getColor(int moveNum) {
    	return ((moveNum + 1) / 2) % 2 + 1;
    }

    public int getCurrentColor() {
    	return getCurrentPlayer();
    }

    /** same as super but need it to call THIS classes getCurrentPlayer
     *  and to reset the board with the correct value
     */
    public void addMove(int move) {
        super.addMove(move);
        int p = getColor(getNumMoves() - 1);
        setPosition(move, p);
        updateHash(this);
    }
    public void undoMove(int move) {
    	super.undoMove();
    	updateHash(this);
    }

    public long calcHash(long cHash, int p, int move, int rot) {
        cHash ^= ZobristUtil.rand[p-1][rotateMove(move, rot)];
        return cHash;
    }
    /** same as super but need it to call THIS classes getCurrentPlayer */
    public boolean isValidMove(int move, int player) {

        // make sure move is in bounds
        try {
            checkOutOfBounds(move);
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (player != getCurrentPlayer()) {
            return false;
        }

        if (getPosition(move) != 0) {
            return false;
        }

        return true;
    }
    /** Converts a single variable move to its x,y coordinates
     *  @param move The move to convert
     *  @return Coord A point variable with x and y set
     */
    public Coord convertMove(int move) {
        return new Coord(move % gridState.getGridSizeX(), move / gridState.getGridSizeY());
    }

    public void checkOutOfBounds(int move) {
    	Coord p = convertMove(move);
        checkOutOfBounds(p.x, p.y);
    }
    public void checkOutOfBounds(int x, int y) {
        if (x < 0 || x >= gridState.getGridSizeX() ||
            y < 0 || y >= gridState.getGridSizeY()) {
            throw new IllegalArgumentException("Out of bounds: " + x + ", " + y + " max = " + (19 - 1) + ", " + (19 - 1));
        }
    }

    /** Determine if the game is over
     *  @return boolean True if game is over
     */
    public boolean isGameOver() {

        if (getNumMoves() < 11) {
            return false;
        }

        int lines[] = new int[4];
        int lastMove = getMove(getNumMoves() - 1);

        int lastMovePlayer = getColor(getNumMoves() - 1);
        int maxMove = gridState.getGridSizeX() * gridState.getGridSizeY() - 1;
        int direction = 1;

        for (int k = 0; k < 2; k++) {
            checkSurrounding: for (int i = 0; i < surrounding.length; i++) {

                int newMove = lastMove;
                for (int j = 0; j < gridState.getGridSizeX(); j++) {

                    int oldX = newMove % gridState.getGridSizeX();
                    newMove += direction * surrounding[i];
                    int newX = newMove % gridState.getGridSizeX();

                    // if passes over side
                    if ((oldX == 0 && newX == gridState.getGridSizeX() - 1) ||
                        (oldX == gridState.getGridSizeX() - 1 && newX == 0)) {
                        continue checkSurrounding;
                    }

                    if (newMove < 0 || newMove > maxMove) {
                        continue checkSurrounding;
                    }

                    if (getPosition(newMove) != lastMovePlayer) {
                        continue checkSurrounding;
                    }
                    lines[i]++;
                }
            }
            direction = -1;
        }
        for (int i = 0; i < lines.length; i++) {
            if (!allowOverlines && lines[i] == 5) {
                winner = getColor(getNumMoves() - 1);
                return true;
            }
            else if (allowOverlines && lines[i] >= 5) {
                winner = getColor(getNumMoves() - 1);
                return true;
            }
        }

        // for draw
        if (getNumMoves() == 361) {
            return true;
        }
        return false;
    }

    public int getWinner() {
        if (isGameOver() && winner == 0) {
            return 0;
        }
        else if (isGameOver()) {
            return winner;
        }
        else {
            return 0;
        }
    }

    /** Allow players to undo only if the last move was by the player (they just
     *  and the 1st move can't be undone.
     *  @param player The player requesting an undo
     *  @return boolean True if the player can request an undo
     */
    public boolean canPlayerUndo(int player) {
		return getNumMoves() > 1 &&
			getColor(getNumMoves() - 1) == player;
    }

    public void printBoard() {
        ((SimpleGridState) gridState).printBoard();
    }
}