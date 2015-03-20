package org.pente.gameServer.core;

import java.awt.Color;

/**
 * @author dweebo
 */
public class SimpleGridPiece implements GridPiece {

	private static int idSeq = 2;

	private int id;

    private int x;
    private int y;
    private int player;
    private Color color = null;

    private int depth;

    public SimpleGridPiece() {
    }
    public SimpleGridPiece(int x, int y, int player) {
        this.x = x;
        this.y = y;
        this.player = player;

        this.id = idSeq++;
    }

    public int getId() {
    	return id;
    }

    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }

    public Color getColor() {
    	return color;
    }
    public void setColor(Color c) {
    	this.color = c;
    }

    public int getPlayer() {
        return player;
    }
    public void setPlayer(int player) {
        this.player = player;
    }

    public int getDepth() {
    	return depth;
    }
    public void setDepth(int depth) {
    	this.depth = depth;
    }

    public boolean equals(Object o) {
        if (!(o instanceof GridPiece)) {
            return false;
        }
        GridPiece p = (GridPiece) o;

        return p.getPlayer() == getPlayer() &&
               p.getX() == getX() &&
               p.getY() == getY();
    }

    public String toString() {
        return "coords = [" + x + ", " + y + "], player = " + player;
    }
}