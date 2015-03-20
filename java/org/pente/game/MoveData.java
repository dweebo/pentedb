package org.pente.game;

import java.io.*;

/** Interface for data structures that hold move data for a game
 * @author dweebo
 */
public interface MoveData extends Serializable {

    /** Add a move for this game
     *  @param move An integer representation of a move
     */
    public void addMove(int move);

    /** Undo the last move */
    public void undoMove();

    /** Get a move for this game
     *  @param num The sequence number of the move
     */
    public int getMove(int num);

    /** Get the number of moves for this game
     *  @return The number of moves
     */
    public int getNumMoves();

    public int[] getMoves();
}