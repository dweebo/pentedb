package org.pente.gameDatabase.swing;

import java.awt.Color;

/**
 * @author dweebo
 */
public class BoardLine {

	private int x1, x2, y1, y2;
	private Color color;

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public int getX1() {
		return x1;
	}

	public void setX1(int x1) {
		this.x1 = x1;
	}

	public int getX2() {
		return x2;
	}

	public void setX2(int x2) {
		this.x2 = x2;
	}

	public int getY1() {
		return y1;
	}

	public void setY1(int y1) {
		this.y1 = y1;
	}

	public int getY2() {
		return y2;
	}

	public void setY2(int y2) {
		this.y2 = y2;
	}

	public static final int SLOPE_HORIZ = 0;
	public static final int SLOPE_VERT = 1;
	public static final int SLOPE_DOWN = 2;
	public static final int SLOPE_UP = 3;
	public int getSlope() {
		if (x1 == x2) return SLOPE_VERT;
		else if (y1 == y2) return SLOPE_HORIZ;
		else if (x1 > x2 && y1 > y2) return SLOPE_UP;
		else if (x1 > x2 && y1 < y2) return SLOPE_DOWN;
		else if (x1 < x2 && y1 > y2) return SLOPE_DOWN;
		else if (x1 < x2 && y1 < y2) return SLOPE_UP;
		else return -1;
	}
}
