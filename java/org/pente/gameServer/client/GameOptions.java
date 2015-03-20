package org.pente.gameServer.client;

/**
 * @author dweebo
 */
public interface GameOptions extends java.io.Serializable {

    public static final int WHITE = 0;
    public static final int BLACK = 1;
    public static final int RED = 2;
    public static final int ORANGE = 3;
    public static final int YELLOW = 4;
    public static final int BLUE = 5;
    public static final int GREEN = 6;
    public static final int PURPLE = 7;

    public int getPlayerColor(int playerNum);
    public void setPlayerColor(int color, int playerNum);

    public boolean getDraw3DPieces();
    public void setDraw3DPieces(boolean draw3DPieces);

    public boolean getShowLastMove();
    public void setShowLastMove(boolean showLastMove);

    public boolean getPlaySound();
    public void setPlaySound(boolean playSound);

    public void setDrawDepth(boolean drawDepth);
    public boolean getDrawDepth();

    public GameOptions newInstance();
}