package org.pente.gameServer.core;

import java.awt.Color;

/**
 * @author dweebo
 */
public interface GridPiece {

    public int getX();
    public void setX(int x);

    public int getY();
    public void setY(int y);

    public int getPlayer();
    public void setPlayer(int player);

    public Color getColor();
    public void setColor(Color c);

    public int getDepth();
    public void setDepth(int depth);
}