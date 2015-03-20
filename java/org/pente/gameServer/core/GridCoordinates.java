package org.pente.gameServer.core;

import java.awt.Point;

/**
 * @author dweebo
 */
public interface GridCoordinates {
    public String[] getXCoordinates();
    public String[] getYCoordinates();
    public String getCoordinate(int move);
    public String getCoordinate(int x, int y);
    public Point getPoint(String coordinate);
}