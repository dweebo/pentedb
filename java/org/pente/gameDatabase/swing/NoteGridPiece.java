package org.pente.gameDatabase.swing;

import java.awt.*;
import java.awt.image.*;
import java.net.URL;

import java.awt.Graphics2D;

import org.pente.gameServer.core.*;

/**
 * @author dweebo
 */
public class NoteGridPiece implements GridPiece, ImageObserver {

	private PlunkNode n;
	public NoteGridPiece(PlunkNode n) {
		this.n = n;
	}
	public Color getColor() {
		return null;
	}

	public int getPlayer() {
		return 0;
	}

	public int getX() {
		return n.getRotatedMove() % 19;
	}

	public int getY() {
		return 18 - n.getRotatedMove() / 19;
	}

	public void setColor(Color c) {
	}

	public void setPlayer(int player) {
	}

	public void setX(int x) {
	}

	public void setY(int y) {
	}
    public int getDepth() {
    	return 0;
    }
    public void setDepth(int depth) {
    }

	// quick and dirty, update this
	// if no type or name, draw a small square
	public void draw(Graphics2D g, int x, int y, int gridPieceSize) {

		int x2 = x + gridPieceSize / 2, y2 = y + gridPieceSize / 2; //center

		Image i = null;

		if (n.getType() != PlunkNode.NO_TYPE) {
			URL image = null;
			switch (n.getType()) {
			case PlunkNode.GOOD:
				image = NoteGridPiece.class.getResource("images/good2.png");
				break;
			case PlunkNode.VERY_GOOD:
				image = NoteGridPiece.class.getResource("images/very_good.png");
				break;
			case PlunkNode.BAD:
				image =  NoteGridPiece.class.getResource("images/bad2.png");
				break;
			case PlunkNode.VERY_BAD:
				image = NoteGridPiece.class.getResource("images/very_bad.png");
				break;
			case PlunkNode.INTERESTING:
				image = NoteGridPiece.class.getResource("images/interesting.png");
				break;
			}

			i = Toolkit.getDefaultToolkit().getImage(image);
			x2 -= 8;
		}

		if (n.getName() != null && !n.getName().equals("")) {

	        int fontSize = 8;
	        if (gridPieceSize > 14) {
	            fontSize += 2;
	        }
	        if (gridPieceSize > 24) {
	            fontSize += 2;
	        }

			Font f = new Font("Helvetica", Font.BOLD, fontSize);
	        g.setFont(f);
	        FontMetrics fm = g.getFontMetrics();

	        x2 -= fm.charWidth(n.getName().charAt(0)) / 2 - 1;

	        g.setColor(Color.black);
	        g.drawString(n.getName().substring(0, 1),
	        	x2, y2 + fm.getAscent() / 2);

	        x2 += fm.charWidth(n.getName().charAt(0)) / 2 + 2;

		}

		if (i != null) {
			y2 -= 8;
			g.drawImage(i, x2, y2, null);
		}

		// other ideas
		// an ->
		if (i == null && (n.getName() == null || n.getName().equals(""))) {
			g.setColor(Color.black);
			g.drawRect(x2 - 3, y2 - 3, 6, 6);
		}
	}
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		return false;
	}
}
