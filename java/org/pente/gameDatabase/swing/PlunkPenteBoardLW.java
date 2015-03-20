package org.pente.gameDatabase.swing;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.io.File;


import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.pente.gameServer.client.GameStyles;
import org.pente.gameServer.client.swing.PenteBoardLW;
import org.pente.gameServer.core.GridPiece;

/**
 * @author dweebo
 */
public class PlunkPenteBoardLW extends PenteBoardLW {

	private List<BoardLine> lines = new ArrayList<BoardLine>();

	public void addLine(BoardLine line) {
		lines.add(line);
	}
	public void removeLine(BoardLine line) {
		lines.remove(line);
	}
	public void setLines(List<BoardLine> lines) {
		this.lines = lines;
	}
	public void clearLines() {
		lines.clear();
	}

	public void replacePieces(List<GridPiece> pieces) {

        synchronized (drawLock) {
            gridPieces.removeAllElements();
            gridPieces.addAll(pieces);
            boardDirty = true;
        }
        repaint();
	}

    protected void drawBoard(Graphics boardGraphics) {

    	Graphics2D g = (Graphics2D) boardGraphics;
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);

    	int pad = 6;


	    BufferedImage image = getBlankImage(getWidth(), getHeight());
	    Graphics2D lineGraphics = (Graphics2D) image.getGraphics();

	    lineGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);

	    AlphaComposite ac =
            AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

	    lineGraphics.setComposite(ac); // blends each line to other lines


    	int xd = (int) (0.70710f * (gridPieceSize + pad * 2 + 2) / 2);
    	for (BoardLine l : lines) {

    		// not very efficient but create a new image the size of the board
    		// for each line that is drawn.  draw it with alpha 1, then draw
    		// the line to the lineGraphics which is another temporary image
    		// of ALL the lines, use alpha on that image to blend the lines
    		// together
    	    BufferedImage image2 = getBlankImage(getWidth(), getHeight());
    	    Graphics2D g2 = (Graphics2D) image2.getGraphics();

    	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);

    	    g2.setColor(l.getColor());

    		int x1 = getStartX() + l.getX1() * gridPieceSize - gridPieceSize / 2 - pad;
            int y1 = getStartY() + (gridHeight - l.getY1() - 2) * gridPieceSize + gridPieceSize / 2 - pad;
    		int x2 = getStartX() + l.getX2() * gridPieceSize - gridPieceSize / 2 - pad;
            int y2 = getStartY() + (gridHeight - l.getY2() - 2) * gridPieceSize + gridPieceSize / 2 - pad;

            g2.fillOval(x1, y1, gridPieceSize + 2 * pad, gridPieceSize + 2 * pad);
            g2.fillOval(x2, y2, gridPieceSize + 2 * pad, gridPieceSize + 2 * pad);

            int x[], y[];
            if (l.getSlope() == BoardLine.SLOPE_HORIZ) {
	            x = new int[] { x1 + gridPieceSize / 2 + pad,
	            				x1 + gridPieceSize / 2 + pad,
	            				x2 + gridPieceSize / 2 + pad,
	            				x2 + gridPieceSize / 2 + pad};

	            y = new int[] { y1, y1 + gridPieceSize + pad * 2,
	            					  y2 + gridPieceSize + pad * 2, y2};
            }
            else if (l.getSlope() == BoardLine.SLOPE_VERT) {
	            x = new int[] { x1,
  					  x1 + gridPieceSize + pad * 2,
  					  x2 + gridPieceSize + pad * 2,
  					  x2};

	            y = new int[] { y1 + gridPieceSize / 2 + pad,
	            				y1 + gridPieceSize / 2 + pad,
	            				y2 + gridPieceSize / 2 + pad,
	            				y2 + gridPieceSize / 2 + pad  };
            }
            else if (l.getSlope() == BoardLine.SLOPE_UP) {
	            x = new int[] { x1 + pad + gridPieceSize / 2 - xd,
	  					  		x1 + pad + gridPieceSize / 2 + xd,
	  				            x2 + pad + gridPieceSize / 2 + xd,
	  		  					x2 + pad + gridPieceSize / 2 - xd };

	            y = new int[] { y1 + pad + gridPieceSize / 2 - xd,
	            				y1 + pad + gridPieceSize / 2 + xd,
	            				y2 + pad + gridPieceSize / 2 + xd,
	            				y2 + pad + gridPieceSize / 2 - xd  };
            }
            else {
	            x = new int[] { x1 + pad + gridPieceSize / 2 - xd,
					  		x1 + pad + gridPieceSize / 2 + xd,
				            x2 + pad + gridPieceSize / 2 + xd,
		  					x2 + pad + gridPieceSize / 2 - xd };

	            y = new int[] { y1 + pad + gridPieceSize / 2 + xd,
        				y1 + pad + gridPieceSize / 2 - xd,
        				y2 + pad + gridPieceSize / 2 - xd,
        				y2 + pad + gridPieceSize / 2 + xd  };
            }

            g2.fillPolygon(x, y, 4);
            lineGraphics.drawImage(image2, 0, 0, null);
            g2.dispose();
    	}

    	g.drawImage(image, 0, 0, null);
    	lineGraphics.dispose();

    	super.drawBoard(boardGraphics);
    }

    protected void drawPiece(Graphics g, GridPiece p) {

        Color c[] = GameStyles.colors[gameOptions.getPlayerColor(p.getPlayer())];

        Color highlightColor = null;
        if (gameOptions.getShowLastMove() && p == highlightPiece) {
            highlightColor = Color.yellow;
        }

        int x = getStartX() + p.getX() * gridPieceSize;
        int y = getStartY() + (gridHeight - p.getY() - 2) * gridPieceSize;

        if (piecesOnGrid) {
            x -= gridPieceSize / 2;
            y += gridPieceSize / 2;
        }

        if (p instanceof NoteGridPiece) {
        	NoteGridPiece ngp = (NoteGridPiece) p;
        	ngp.draw((Graphics2D)g, x, y, gridPieceSize);
        }
        else if (p.getColor() != null && p.getPlayer() == 3) {

        	/* shaded circle */
        	int r = gridPieceSize;
        	BufferedImage image = new BufferedImage(r, r,
    	            BufferedImage.TYPE_INT_ARGB);

    	    // create new graphics and set anti-aliasing hint
    	    Graphics2D graphics = (Graphics2D) image.getGraphics().create();
    	    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	          RenderingHints.VALUE_ANTIALIAS_ON);
    	    graphics.setColor(p.getColor());
    	    graphics.fillOval(0, 0, r - 1, r - 1);


	  	    GradientPaint spot = new GradientPaint(0, 0, new Color(255, 255, 255,
	  	          120), r, r, new Color(255, 255, 255, 0));
	  	    Graphics2D tempGraphics = (Graphics2D) graphics.create();
	  	    tempGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
    		          RenderingHints.VALUE_ANTIALIAS_ON);
	  	    tempGraphics.setPaint(spot);
	  	    tempGraphics.setClip(new Ellipse2D.Double(0, 0, r - 1,
	  	          r - 1));
	  	    tempGraphics.fillRect(0, 0, r, r);
	  	    tempGraphics.dispose();

	  	    // draw outline of the icon
		    graphics.setColor(Color.gray);
	  	    graphics.drawOval(0, 0, r - 1, r - 1);
		    graphics.dispose();

		    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			    RenderingHints.VALUE_ANTIALIAS_ON);
		    g.drawImage(image, x, y, this);


        	/* simple circle
	        Graphics2D g2 = (Graphics2D)g;
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	              RenderingHints.VALUE_ANTIALIAS_ON);

	        int r = gridPieceSize;

	        g.setColor(p.getColor());
	        g.fillOval(x + 1, y + 1, r - 2, r - 2);  // player color

  	      	g.setColor(Color.gray);
  	      	g.drawOval(x, y, r - 1, r - 1);  // black
  	      	*/

        }
        else if (gameOptions.getDraw3DPieces()) {
            //draw3DPiece(g, new Point(x, y), c, highlightColor, gridPieceSize);

            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                  RenderingHints.VALUE_ANTIALIAS_ON);

            if (highlightColor != null) {
            	int width = gridPieceSize < 30 ? gridPieceSize + 4 : gridPieceSize + 6;
                int offset = gridPieceSize < 30 ? 1 : 2;
            	fillOval(g, x - offset, y - offset, width, highlightColor);
            }
            fillOval(g, x + 2, y + 2, gridPieceSize-1, shadowColor); // shadow

            g2.drawImage(pieces[p.getPlayer()], x, y, this);
        }
        else {
            super.drawPiece(g, p);
        }

        if (gameOptions.getDrawDepth() && p != thinkingPiece &&
        	p.getDepth() > 0 &&
        	(p.getPlayer() == 1 || p.getPlayer() == 2)) {
	        int fontSize = 8;
		    if (gridPieceSize > 14) {
		        fontSize += 2;
		    }
		    if (gridPieceSize > 24) {
		        fontSize += 2;
		    }
	        Font f = new Font("Helvetica", Font.PLAIN, fontSize);
	        FontMetrics fm = g.getFontMetrics(f);
            g.setFont(f);
            if (p.getPlayer() == 1) {
            	g.setColor(Color.black);
            }
            else {
            	g.setColor(Color.white);
            }

            int x2 = x + gridPieceSize / 2, y2 = y + gridPieceSize / 2; //center
	        x2 -= fm.stringWidth(Integer.toString(p.getDepth())) / 2;

        	g.drawString(Integer.toString(p.getDepth()), x2, y2 + fm.getAscent() / 2);
        }
    }
}
