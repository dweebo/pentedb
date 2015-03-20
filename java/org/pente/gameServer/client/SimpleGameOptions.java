package org.pente.gameServer.client;

/**
 * @author dweebo
 */
public class SimpleGameOptions implements GameOptions {

	// old serial version uid for class before depth added
	// since thousands of game options stored in db with old class
	static final long serialVersionUID = 9071326536916945957L;
    private int         colors[];
    private boolean     draw3DPieces;
    private boolean     showLastMove;
    private boolean     playSound;
    private boolean     depth = false;

    public SimpleGameOptions(int maxPlayers) {
        colors = new int[maxPlayers + 1];
    }

    public int getPlayerColor(int playerNum) {
        return colors[playerNum];
    }
    public void setPlayerColor(int color, int playerNum) {
        colors[playerNum] = color;
    }

    public boolean getDraw3DPieces() {
        return draw3DPieces;
    }
    public void setDraw3DPieces(boolean draw3DPieces) {
        this.draw3DPieces = draw3DPieces;
    }

    public void setDrawDepth(boolean drawDepth) {
    	this.depth = drawDepth;
    }
    public boolean getDrawDepth() {
    	return depth;
    }

    public boolean getShowLastMove() {
        return showLastMove;
    }
    public void setShowLastMove(boolean showLastMove) {
        this.showLastMove = showLastMove;
    }

    public boolean getPlaySound() {
        return playSound;
    }
    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
    }

    public GameOptions newInstance() {

        GameOptions gameOptions = new SimpleGameOptions(colors.length);

        for (int i = 0; i < colors.length; i++) {
            gameOptions.setPlayerColor(colors[i], i);
        }
        gameOptions.setPlaySound(getPlaySound());
        gameOptions.setDraw3DPieces(getDraw3DPieces());
        gameOptions.setShowLastMove(getShowLastMove());

        return gameOptions;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SimpleGameOptions)) return false;
        SimpleGameOptions o = (SimpleGameOptions) obj;

        if (o.colors[1] != colors[1] ||
            o.colors[2] != colors[2] ||
            o.draw3DPieces != draw3DPieces ||
            o.playSound != playSound ||
            o.showLastMove != showLastMove) return false;

        return true;
    }
}