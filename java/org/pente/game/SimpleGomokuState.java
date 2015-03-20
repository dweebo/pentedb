package org.pente.game;

/**
 * @author dweebo
 */
public class SimpleGomokuState extends GridStateDecorator
    implements GomokuState, HashCalculator {

    private int[]   surrounding;
    private boolean allowOverlines = false;
    private boolean doHashes = false;

    private int winner;

    public SimpleGomokuState() {
        this(19, 19);
    }

    /** Create an empty gomoku state wrapped around the given GridState
     *  @param gridState The base GridState to use
     */
    public SimpleGomokuState(GridState gridState) {
        super(gridState);

        int boardSizeX = gridState.getGridSizeX();
        surrounding = new int[] { -1, -boardSizeX - 1, -boardSizeX, -boardSizeX + 1 };
    }

    /** Create a gomoku state with a certain board size
     *  @param boardSize The board size
     */
    public SimpleGomokuState(int boardSizeX, int boardSizeY) {
        super(boardSizeX, boardSizeY);

        surrounding = new int[] { -1, -boardSizeX - 1, -boardSizeX, -boardSizeX + 1 };
    }

    /** Create a new gomoku grid state with a specified size and add the moves
     *  contained in the passed in MoveData to the board.
     *  @param data MoveData containing a list of moves
     *  @param boardSize The size of the board
     */
    public SimpleGomokuState(MoveData data, int boardSizeX, int boardSizeY,
        boolean doHashes) {
        this(boardSizeX, boardSizeY);

        this.doHashes = doHashes;
        for (int i = 0; i < data.getNumMoves(); i++) {
            addMove(data.getMove(i));
        }
    }

    public void setDoHashes(boolean doHashes) {
        this.doHashes = doHashes;
    }

    public GridState getInstance(MoveData moveData) {
        SimpleGomokuState state = new SimpleGomokuState(moveData,
            gridState.getGridSizeX(), gridState.getGridSizeY(), doHashes);
        state.allowOverlines(allowOverlines);

        return state;
    }

    public void allowOverlines(boolean allow) {
        allowOverlines = allow;
    }
    public boolean areOverlinesAllowed() {
        return allowOverlines;
    }

    public void addMove(int move) {
        gridState.addMove(move);
        if (doHashes) {
            gridState.updateHash(this);
        }
    }
    public void undoMove() {
    	gridState.undoMove();
        if (doHashes) {
            gridState.updateHash(this);
        }
    }

    /** Determine if the game is over
     *  @return boolean True if game is over
     */
    public boolean isGameOver() {

        int lines[] = new int[4];
        int lastMove = getMove(getNumMoves() - 1);

        int lastMovePlayer = getCurrentColor() == 1 ? 2 : 1;
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
            if (!allowOverlines && lines[i] == 4) {
                winner = 3 - getCurrentPlayer();
                return true;
            }
            else if (allowOverlines && lines[i] >= 4) {
                winner = 3 - getCurrentPlayer();
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

    /** Allow players to undo only if it is not the players turn (they just
     *  moved and it is the other players turn) and the 1st move can't be undone.
     *  @param player The player requesting an undo
     *  @return boolean True if the player can request an undo
     */
    public boolean canPlayerUndo(int player) {

        if (gridState.canPlayerUndo(player)) {
            return gridState.getCurrentPlayer() != player &&
                   gridState.getNumMoves() > 1;
        }

        return false;
    }
    public long calcHash(long cHash, int p, int move, int rot) {
        cHash ^= ZobristUtil.rand[p-1][rotateMove(move, rot)];
        return cHash;
    }
    public void printBoard() {
        ((SimpleGridState) gridState).printBoard();
    }
}