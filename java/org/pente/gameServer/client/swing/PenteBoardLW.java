package org.pente.gameServer.client.swing;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Vector;

import javax.swing.JComponent;

import org.pente.gameServer.client.GameOptions;
import org.pente.gameServer.client.GameOptionsChangeListener;
import org.pente.gameServer.client.GameStyles;
import org.pente.gameServer.client.GameTimer;
import org.pente.gameServer.client.GameTimerListener;
import org.pente.gameServer.client.GridBoardListener;
import org.pente.gameServer.client.PenteBoardComponent;
import org.pente.gameServer.client.SimpleGameTimer;
import org.pente.gameServer.core.GridCoordinates;
import org.pente.gameServer.core.GridCoordinatesChangeListener;
import org.pente.gameServer.core.GridPiece;
import org.pente.gameServer.core.SimpleGridPiece;

/**
 * @author dweebo
 */
public class PenteBoardLW extends JComponent implements PenteBoardComponent,
    GridCoordinatesChangeListener, GameOptionsChangeListener
{

    int     captureAreaWidth;
    int     captures[];

    // the number of grids on the x axis
    protected int             gridWidth;

    // number of grids on the y axis
    protected int             gridHeight;

    // if true, the pieces show up ontop of the grid lines
    // if false, the pieces show up in between the grid lines
    protected boolean         piecesOnGrid;

    // the width of the beveled edge around the whole board
    protected int             beveledEdge;

    // space after beveled edge that won't be
    // used to draw the grid lines of the board
    protected Insets          insets;

    // space used for the coordinates
    Dimension       coordinatesDimensions;

    // the left over space between the edge of the
    // insets and the beginning of the grid lines of the board
    Dimension       edgeLeftOvers;

    // the width of a piece on the board
    protected int             gridPieceSize;


    private String          gameName;
    protected Vector        gridPieces;
    protected GridPiece     highlightPiece;
    protected GridPiece     thinkingPiece;
    private GridPiece       oldThinkingPiece;
    private boolean         showThinkingPiece;
    private boolean         newMovesAvailable;
    private boolean         showNewMovesAvailable;
    private GameTimer       showNewMovesAvailableTimer;
    private boolean         drawInnerCircles;
    private boolean         drawCoordinates = true;

    protected GameOptions   gameOptions;
    GridCoordinates         gridCoordinates;

    protected boolean       boardDirty = true;
    boolean                 emptyBoardDirty = true;
    protected Object        drawLock = new Object();

    private Dimension       currentSize;

    Image                   emptyBoardImage;
    Graphics                emptyBoardGraphics;
    Image                   boardImage;
    Graphics                boardGraphics;

    private Color           backGroundColor;
    private Color           gridColor;
    private Color           gameNameColor;
    private Color           highlightColor;
	protected Color         shadowColor;

    // move listeners
    private Vector          listeners;

    private static final Font MESSAGE_FONT = new Font("Arial", Font.PLAIN, 12);
    private String          message;
	private boolean			hideMessage = false;

	private String player1Name, player2Name;

	//TODO make gridlines darker/bolder
	//TODO show highlight behind fancy pieces

    public PenteBoardLW() {

        listeners = new Vector();
        gridPieces = new Vector();
        gridPieceSize = -1;

        addMouseListener(new MoveEventGenerator());
        addMouseMotionListener(new ThinkingPieceMoveGenerator());

        // temp
        backGroundColor = new Color(255, 222, 165);
        gameNameColor = new Color(234, 196, 136);
        gridColor = Color.gray;
        highlightColor = Color.yellow;
        shadowColor = new Color(60, 60, 60);

        thinkingPiece = new SimpleGridPiece();
        thinkingPiece.setPlayer(1);
        thinkingPiece.setX(-1);
        thinkingPiece.setY(-1);
        oldThinkingPiece = new SimpleGridPiece();
        oldThinkingPiece.setPlayer(1);
        oldThinkingPiece.setX(-1);
        oldThinkingPiece.setY(-1);

        currentSize = new Dimension(0, 0);

        insets = new Insets(0, 10, 0, 10);
        coordinatesDimensions = new Dimension(0, 0);
        edgeLeftOvers = new Dimension(0, 0);
        beveledEdge = 3;
        // end temp

        insets = new Insets(0, 0, 0, 0);
        captures = new int[3];
    }

	public boolean isOpaque() {
        return true;
    }

    public int getGridWidth() {
        return gridWidth;
    }
    public void setGridWidth(int width) {
        this.gridWidth = width;
    }

    public int getGridHeight() {
        return gridHeight;
    }
    public void setGridHeight(int height) {
        this.gridHeight = height;
    }

    public boolean getOnGrid() {
        return piecesOnGrid;
    }
    public void setOnGrid(boolean onGrid) {
        this.piecesOnGrid = onGrid;
    }

    public Vector getGridPieces() {
        return (Vector) gridPieces.clone();
    }

    public void setBackgroundColor(int color) {
        this.backGroundColor = new Color(color);
        gameNameColor = backGroundColor.darker();
    }
    public void setGridColor(int color) {
        this.gridColor = new Color(color);
    }
    public void setHighlightColor(int color) {
        this.highlightColor = new Color(color);
    }
    public void setGameNameColor(int color) {
        this.gameNameColor = new Color(color);
    }

    public void setMessage(String message) {
        synchronized (drawLock) {
            this.message = message;
        }
        repaint();
    }

    public void setGameName(String gameName) {
        synchronized (drawLock) {
            this.gameName = gameName;
            emptyBoardDirty = true;
        }
        repaint();
    }

    public void setHighlightPiece(GridPiece gridPiece) {
        synchronized (drawLock) {
            this.highlightPiece = gridPiece;
            emptyBoardDirty = true;
        }

        repaint();
    }

    public void setThinkingPieceVisible(boolean visible) {
        synchronized (drawLock) {
            if (showThinkingPiece != visible) {
                showThinkingPiece = visible;
            }
        }

        repaint();
    }

    public void setThinkingPiecePlayer(int player) {
        synchronized (drawLock) {
            if (thinkingPiece.getPlayer() != player) {
                thinkingPiece.setPlayer(player);
            }
        }

        repaint();
    }

    public void setNewMovesAvailable(boolean available) {
        synchronized (drawLock) {
            if (newMovesAvailable != available) {
                newMovesAvailable = available;

                if (newMovesAvailable) {
                    if (showNewMovesAvailableTimer == null) {
                        showNewMovesAvailableTimer = new SimpleGameTimer();
                        showNewMovesAvailableTimer.setStartMinutes(1000);
                        showNewMovesAvailableTimer.reset();
                        showNewMovesAvailableTimer.addGameTimerListener(new GameTimerListener() {
                            public void timeChanged(int newSeconds, int newMinutes) {
                                synchronized (drawLock) {
                                    if (newMovesAvailable) {
                                        showNewMovesAvailable = !showNewMovesAvailable;
                                        emptyBoardDirty = true;
                                    }
                                }
                                repaint();
                            }
                        });
                    }
                    showNewMovesAvailableTimer.go();
                }
                else {
                    synchronized (drawLock) {
                        if (showNewMovesAvailableTimer != null) {
                            showNewMovesAvailableTimer.stop();
                        }
                        showNewMovesAvailable = false;
                        emptyBoardDirty = true;
                    }
                    repaint();
                }
            }
        }
    }

    public void setDrawInnerCircles(boolean drawInnerCircles) {
        this.drawInnerCircles = drawInnerCircles;
    }
    public void setDrawCoordinates(boolean drawCoordinates) {
        this.drawCoordinates = drawCoordinates;
    }

    public void setBoardInsets(int l,int t,int r,int b) {
        this.insets = new Insets(l,t,r,b);
    }
    public void setCursor(int type) {
    	setCursor(Cursor.getPredefinedCursor(type));
    }
    public void addGridBoardListener(GridBoardListener listener) {
        listeners.addElement(listener);
    }
    public void removeGridBoardListener(GridBoardListener listener) {
        listeners.removeElement(listener);
    }
    // end GridBoardComponent


    // PieceCollection
    public void addPiece(GridPiece gridPiece) {

        synchronized (drawLock) {
            gridPieces.addElement(gridPiece);
            boardDirty = true;
        }
        repaint();
    }

    public void updatePiecePlayer(int x, int y, int player) {
    	//System.out.println(gridPieces.size() + " update piece player " + x + "," + y + "," + player);
    	int count = 0;
    	synchronized (drawLock) {
    		for (int i = 0; i < gridPieces.size(); i++) {
    			GridPiece p = (GridPiece) gridPieces.elementAt(i);
    			if (p.getX() == x && p.getY() == y) {
    				p.setPlayer(player);
    				boardDirty = true;
    				count++;
    			}
    		}
    		if (boardDirty) {
    			repaint();
    		}
			//System.out.println("found " + count);
    	}
    }
    public void removePiece(GridPiece gridPiece) {
        synchronized (drawLock) {
            gridPieces.removeElement(gridPiece);
            boardDirty = true;
        }
        repaint();
    }
    public void clearPieces() {
        synchronized (drawLock) {
            gridPieces.removeAllElements();
            boardDirty = true;

            captures[1] = 0;
            captures[2] = 0;
        }
        repaint();
    }
    // end PieceCollection

    // GameOptionsChangeListener
    public void gameOptionsChanged(GameOptions gameOptions) {
        synchronized (drawLock) {
            this.gameOptions = gameOptions;
            boardDirty = true;
        }
        repaint();
    }

    // GridCoordinatesChangeListener
    public void gridCoordinatesChanged(GridCoordinates gridCoordinates) {
        synchronized (drawLock) {
            this.gridCoordinates = gridCoordinates;
            emptyBoardDirty = true;
        }
        repaint();
    }

    // Canvas

	public Dimension getMinimumSize() {
        return new Dimension(200, 200);
    }

	public Dimension getPreferredSize() {
        return new Dimension(400, 400);
    }


	public void addNotify() {
        super.addNotify();

        emptyBoardImage = createImage(1, 1);
        emptyBoardGraphics = emptyBoardImage.getGraphics();
        emptyBoardGraphics.setClip(0, 0, 1, 1);

        boardImage = createImage(1, 1);
        boardGraphics = boardImage.getGraphics();
        boardGraphics.setClip(0, 0, 1, 1);
    }
    public void destroy() {

        if (emptyBoardGraphics != null) {
            emptyBoardGraphics.dispose();
            emptyBoardGraphics = null;
        }
        if (emptyBoardImage != null) {
            emptyBoardImage.flush();
            emptyBoardImage = null;
        }

        if (boardGraphics != null) {
            boardGraphics.dispose();
            boardGraphics = null;
        }
        if (boardImage != null) {
            boardImage.flush();
            boardImage = null;
        }

        if (showNewMovesAvailableTimer != null) {
            showNewMovesAvailableTimer.destroy();
        }
    }



	public void update(Graphics g) {
        paint(g);
    }

	public void refresh() {
		synchronized (drawLock) {
			boardDirty = true;
		}
		repaint();
	}
    public void myPaint(Graphics g, int width, int height) {

        Graphics2D g2 = (Graphics2D)g;
//      Antialiasing for smooth surfaces.
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);

    	setSize(width, height);
        emptyBoardGraphics = g;
        calculateGridSize();
        drawEmptyBoard(g);
        drawBoard(g);
    }

	public void paintComponent(Graphics g) {

        if (emptyBoardGraphics != null) {
            try {
                synchronized (drawLock) {

                    Dimension size = getSize();
                    if (size.width != currentSize.width ||
                        size.height != currentSize.height) {

                        sizeChanged(size.width, size.height);
                        calculateGridSize();
                        emptyBoardDirty = true;
                        currentSize = size;
                    }

                    if (emptyBoardDirty) {
                        drawEmptyBoard(emptyBoardGraphics);
                        drawBoard(emptyBoardImage, boardGraphics);
                        g.setClip(0, 0, currentSize.width, currentSize.height);
                        g.drawImage(boardImage, 0, 0, this);
                    }
                    else if (boardDirty) {
                        drawBoard(emptyBoardImage, boardGraphics);
                        g.setClip(0, 0, currentSize.width, currentSize.height);
                        g.drawImage(boardImage, 0, 0, this);
                    }
                    else {
                        if (oldThinkingPiece.getX() >= 0 &&
                            oldThinkingPiece.getY() >= 0) {
                            int x = getStartX() + oldThinkingPiece.getX() * gridPieceSize;
                            int y = getStartY() + (gridHeight - oldThinkingPiece.getY() - 2) * gridPieceSize;

                            if (piecesOnGrid) {
                                x -= gridPieceSize / 2;
                                y += gridPieceSize / 2;
                            }
                            //g.setClip(x - 1, y - 1, x + gridPieceSize + 1, y + gridPieceSize + 1);
                            g.drawImage(boardImage, 0, 0, this);
                        }
                    }

                    g.setClip(0, 0, currentSize.width, currentSize.height);
                    // if the client wants to show thinking piece
                    // and thinking piece is on the board
                    if (showThinkingPiece &&
                        thinkingPiece.getX() >= 0 &&
                        thinkingPiece.getY() >= 0) {
                        drawPiece(g, thinkingPiece);
                    }

                    if (message != null && !hideMessage) {
                        int x = currentSize.width;
                        int y = getStartY() + 17 * gridPieceSize;

                        g.setFont(MESSAGE_FONT);
                        FontMetrics fm = g.getFontMetrics(MESSAGE_FONT);
                        int mWidth = fm.stringWidth(message);
                        int mHeight = fm.getMaxAscent() +
                                      fm.getLeading();

                        g.setColor(Color.white);
                        g.fillRect(x / 2 - mWidth / 2 - 10, y - mHeight / 2 - 10,
                            mWidth + 20, mHeight + 20);
                        g.setColor(Color.black);
                        g.drawRect(x / 2 - mWidth / 2 - 10, y - mHeight / 2 - 10,
                            mWidth + 20, mHeight + 20);
                        g.drawRect(x / 2 - mWidth / 2 - 9,  y - mHeight / 2 - 9,
                            mWidth + 18, mHeight + 18);

                        x = (x / 2) - 100 + ((200 - mWidth) / 2);
                        y = y + mHeight / 2;
                        g.drawString(message, x, y);
                    }

                }
            } catch (Throwable t) {
                t.printStackTrace(System.err);
            }

        }
    }
    private Rectangle getMessageDimensions() {

		int x = currentSize.width;
		int y = getStartY() + 17 * gridPieceSize;
		FontMetrics fm = getFontMetrics(MESSAGE_FONT);
		int mWidth = fm.stringWidth(message);
		int mHeight = fm.getMaxAscent() +
        			  fm.getLeading();

		return new Rectangle(x / 2 - mWidth / 2 - 10, y - mHeight / 2 - 10,
			mWidth + 20, mHeight + 20);
    }

    protected void sizeChanged(int x, int y) {

        if (emptyBoardImage != null &&
            x != 0 && y != 0) {

            // need a bigger image
            Rectangle rec = emptyBoardGraphics.getClipBounds();
            if (rec != null &&
                (rec.width < x ||
                 rec.height < y)) {
                if (emptyBoardGraphics != null) {
                    emptyBoardGraphics.dispose();
                    emptyBoardGraphics = null;
                }
                if (emptyBoardImage != null) {
                    emptyBoardImage.flush();
                    emptyBoardImage = null;
                }

                if (boardGraphics != null) {
                    boardGraphics.dispose();
                    boardGraphics = null;
                }
                if (boardImage != null) {
                    boardImage.flush();
                    boardImage = null;
                }

                emptyBoardImage = createImage(x, y);
                emptyBoardGraphics = emptyBoardImage.getGraphics();

                boardImage = createImage(x, y);
                boardGraphics = boardImage.getGraphics();
            }
            // use the same image but less of it
            else {
                emptyBoardGraphics.clearRect(0, 0, emptyBoardImage.getWidth(this), emptyBoardImage.getHeight(this));
                boardGraphics.clearRect(0, 0, boardImage.getWidth(this), boardImage.getHeight(this));
            }

            // set the clip to the current size
            emptyBoardGraphics.clipRect(0, 0, x, y);
            boardGraphics.clipRect(0, 0, x, y);
        }
    }


    protected void drawEmptyBoard(Graphics g) {
//System.out.println("drawEmptyBoard()");
        if (gridPieceSize < 0) {
            calculateGridSize();
        }

        drawEmptyBoardBackground(g);
        drawEmptyBoardGameName(g);
        drawEmptyBoardGrid(g);
        if (drawInnerCircles) {
            drawInnerCircles(g);
        }
        if (drawCoordinates) {
            drawEmptyBoardCoordinates(g);
        }

        emptyBoardDirty = false;
    }


    protected int getStartY() {
        return insets.top + beveledEdge + coordinatesDimensions.height + edgeLeftOvers.height;
    }


    protected void drawEmptyBoardBackground(Graphics g) {


//System.out.println("drawEmptyBoardBackground()");
        Dimension size = getSize();
//System.out.println("size="+size);
        if (size.width == 0 || size.height == 0) {
            return;
        }

        g.setColor(backGroundColor);
        g.clearRect(0, 0, size.width, size.height);

        for (int i = 0;  i < beveledEdge; i++) {
            g.fill3DRect(i, i, size.width - 2 * i, size.height - 2 * i, true);
        }
    }

    void drawEmptyBoardGameName(Graphics g) {

    	if (gameName == null) return;
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);

        int gridSizePx = gridPieceSize * (gridWidth - 1);
        int gridSizePy = gridPieceSize * (gridHeight - 1);

        int fontSize = 32;
        int width, height;
        Font f;
        FontMetrics fm;

        while (true) {

            f = new Font("Arial", Font.BOLD, fontSize);
            fm = g.getFontMetrics(f);
            width = fm.stringWidth(gameName);
            if (width > gridSizePx) {
                break;
            }
            fontSize += 4;
        }

        f = new Font("Arial", Font.BOLD, fontSize - 4);
        fm = g.getFontMetrics(f);
        height = fm.getAscent();
        width = fm.stringWidth(gameName);

        int startX = getStartX();
        int startY = getStartY();
        int x = startX + gridSizePx / 2 - width / 2;
        int y = startY + gridSizePy / 2 + height / 3;

        g.setFont(f);
        g.setColor(gameNameColor);
        g.drawString(gameName, x, y);
    }
    void drawPlayerNames(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);



        FontMetrics fm;

	   	int fontSize = 12;
	    if (gridPieceSize > 14) {
	        fontSize += 2;
	    }
	    if (gridPieceSize > 24) {
	        fontSize += 2;
	    }
        Font f = new Font("Arial", Font.BOLD, fontSize);
        fm = g.getFontMetrics(f);
        //height = fm.get();

   		// Derive a new font using a rotatation transform
   		AffineTransform fontAT = new AffineTransform();
   		fontAT.rotate(Math.PI / 2);
   		f = f.deriveFont(fontAT);
    	g2.setFont(f);

        if (player1Name != null) {
        	g2.setColor(Color.WHITE);

       		int x = beveledEdge + insets.left + edgeLeftOvers.width;
       		int p1X = x + fm.getDescent();//3=kludge(gridPieceSize/2 - height) / 2;
       		int p1Y = getStartY() + 15 + (captures[1] * gridPieceSize/2) + 10;

        	g2.drawString(player1Name, p1X, p1Y);
        }
        if (player2Name != null) {
        	g2.setColor(Color.BLACK);

        	int x = getStartX() + gridPieceSize * (gridWidth - 1) + coordinatesDimensions.width;
       		int p2X = x + fm.getDescent();//3=kludge(gridPieceSize/2 - height) / 2;
       		int p2Y = getStartY() + 15 + (captures[2] * gridPieceSize/2) + 10;

        	g2.drawString(player2Name, p2X, p2Y);
        }

    }

    void drawEmptyBoardGrid(Graphics g) {

        Color middleColor = showNewMovesAvailable ? Color.red : Color.black;
        Color gridColor = showNewMovesAvailable ? Color.red : this.gridColor;

        int startX = getStartX();
        int startY = getStartY();
        int x = startX;
        int y = startY;

        boolean drawDifferentMiddleLine = gridWidth % 2 == 1 && gridHeight % 2 == 1;

        // draw vertical grid lines
        for (int i = 0; i < gridWidth; i++) {

            if (drawDifferentMiddleLine && i == gridWidth / 2) {
                 g.setColor(middleColor);
            }
            else {
                g.setColor(gridColor);
            }

            g.drawLine(x, y, x, startY + gridPieceSize * (gridHeight - 1));
            x += gridPieceSize;
        }

        x = startX;
        y = startY;

        // draw horizontal grid lines
        for (int i = 0; i < gridHeight; i++) {

            if (drawDifferentMiddleLine && i == gridHeight / 2) {
                 g.setColor(middleColor);
            }
            else {
                g.setColor(gridColor);
            }

            g.drawLine(x, y, startX + gridPieceSize * (gridWidth - 1), y);
            y += gridPieceSize;
        }
    }

    void drawInnerCircles(Graphics g) {

        Color gridColor = showNewMovesAvailable ? Color.red : this.gridColor;
        g.setColor(gridColor);

        int distanceFromCenter = 3;
        int halfGridPieceSize = gridPieceSize / 2;
        int offsetFromX = (getGridWidth() / 2 - distanceFromCenter) * gridPieceSize - gridPieceSize / 4;
        int offsetFromY = (getGridHeight() / 2 - distanceFromCenter) * gridPieceSize - gridPieceSize / 4;

        int x = getStartX() + offsetFromX;
        int y = getStartY() + offsetFromY;

        g.drawOval(x, y, halfGridPieceSize, halfGridPieceSize);
        x += distanceFromCenter * 2 * gridPieceSize;
        g.drawOval(x, y, halfGridPieceSize, halfGridPieceSize);
        y += distanceFromCenter * 2 * gridPieceSize;
        g.drawOval(x, y, halfGridPieceSize, halfGridPieceSize);
        x -= distanceFromCenter * 2 * gridPieceSize;
        g.drawOval(x, y, halfGridPieceSize, halfGridPieceSize);
        x += distanceFromCenter * gridPieceSize;
        y -= distanceFromCenter * gridPieceSize;
        g.drawOval(x, y, halfGridPieceSize, halfGridPieceSize);
    }

    void drawEmptyBoardCoordinates(Graphics g) {

        int fontSize = 8;
        if (gridPieceSize > 14) {
            fontSize += 2;
        }
        if (gridPieceSize > 24) {
            fontSize += 2;
        }

        Font f = new Font("Helvetica", Font.PLAIN, fontSize);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        int height = fm.getAscent();

        g.setColor(Color.gray);

        int startX = getStartX();
        int startY = getStartY();
        int x = startX;
        int y = startY - 1;

        String coordsX[] = gridCoordinates.getXCoordinates();
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < coordsX.length; i++) {

                String h = coordsX[i];
                int x2 = x + gridPieceSize * i;

                if (piecesOnGrid) {
                    if (i == gridWidth - 1) {
                        x2 -= fm.stringWidth(h);
                    }
                    else if (i != 0) {
                        x2 -= fm.stringWidth(h) / 2;
                    }
                }
                else {
                    x2 += gridPieceSize / 2 - fm.stringWidth(h) / 2;
                }
                g.drawString(h, x2, y);
            }

            y = startY + gridPieceSize * (gridHeight - 1) + height;
        }

        x = startX;
        y = startY;

        String coordsY[] = gridCoordinates.getYCoordinates();
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < coordsY.length; i++) {

                String v = coordsY[i];
                int y2 = y + gridPieceSize * (coordsY.length - i - 1);

                if (piecesOnGrid) {
                    if (i == gridHeight - 1) {
                        y2 += height;
                    }
                    else if (i != 0) {
                        y2 += height / 2;
                    }
                }
                else {
                    y2 += height / 2 + gridPieceSize / 2;
                }

                int x2 = x;
                if (j == 0) {
                    x2 -= fm.stringWidth(v) + 1;
                }
                else {
                    x2 += 1;
                }

                g.drawString(v, x2, y2);
            }

            x = startX + gridPieceSize * (gridWidth - 1);
        }
    }


    void drawBoard(Image emptyBoardImage, Graphics boardGraphics) {
//System.out.println("drawBoard()");
        boardGraphics.drawImage(emptyBoardImage, 0, 0, this);

        drawBoard(boardGraphics);

        drawPlayerNames(boardGraphics);

        boardDirty = false;
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
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);

        if (gameOptions.getDraw3DPieces()) {
            //draw3DPiece(g, new Point(x, y), c, highlightColor, gridPieceSize);


            if (highlightColor != null) {
            	int width = gridPieceSize < 30 ? gridPieceSize + 4 : gridPieceSize + 6;
                int offset = gridPieceSize < 30 ? 1 : 2;
            	fillOval(g, x - offset, y - offset, width, highlightColor);
            }
            fillOval(g, x + 2, y + 2, gridPieceSize-1, shadowColor); // shadow

            g2.drawImage(pieces[p.getPlayer()], x, y, this);
        }
        else {
            draw2DPiece(g, new Point(x, y), c[1], highlightColor, gridPieceSize);
        }
    }

    protected BufferedImage pieces[] = new BufferedImage[3];
	   private BufferedImage drawBufferedPiece(int dimension, int player) {

//			First, we create a new image and set it to anti-aliased mode:


	      // new RGB image with transparency channel
	      BufferedImage image = new BufferedImage(dimension, dimension,
	            BufferedImage.TYPE_INT_ARGB);

	      // create new graphics and set anti-aliasing hint
	      Graphics2D graphics = (Graphics2D) image.getGraphics().create();
	      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	            RenderingHints.VALUE_ANTIALIAS_ON);


	      graphics.rotate(-Math.PI/6,dimension/2,dimension/2);

	      // nice soft blue
	      /*
	      Color topFillColor = new Color(185,221,236);
	      Color midFillColor = new Color(104,199,230);
	      Color botFillColor = new Color(40,182,226);
	      Color topShineColor = new Color(225,245,253);
	      Color bottomShineColor = new Color(225,245,253);
	      */

	      // decent black
	      /*
	      Color topFillColor = new Color(32,32,32);
	      Color midFillColor = new Color(16,16,16);
	      Color botFillColor = Color.black;
	      Color topShineColor = new Color(225,245,253);
	      Color bottomShineColor = new Color(225,245,253);
	      */

	      Color topFillColor = new Color(216,216,216);
	      Color midFillColor = new Color(200,200,200);
	      Color botFillColor = new Color(184,184,184);
	      Color topShineColor = Color.white;
	      Color bottomShineColor = Color.white;

	      if (player == 2) {

		      topFillColor = new Color(32,32,32);
		      midFillColor = new Color(16,16,16);
		      botFillColor = Color.black;
		      topShineColor = new Color(160,160,160);
		      bottomShineColor = new Color(160,160,160);
		      //topShineColor = new Color(225,245,253); //too shiny
		      //bottomShineColor = new Color(225,245,253);
	      }

	      GradientPaint back = new GradientPaint(0, 0, topFillColor,
	    		 dimension, dimension, botFillColor);
	      graphics.setPaint(back);

	      Shape s = new Ellipse2D.Double(0, 0, dimension - 1,
		           dimension - 1);
	      graphics.fill(s);
	      graphics.clip(s);




	      /** part 1 */
	      int width = dimension;
	      int height = dimension;
	      //int shineHeight = (int) (height / 1.8);
	      int shineHeight = height;
	      //int kernelSize = 12;
	      int kernelSize = (int) Math.min(12, Math.pow(Math
	            .min(width, height), 0.8) / 4);
	      if (kernelSize == 0)
	         kernelSize = 1;
	      BufferedImage ghostContour = getBlankImage(width + 2 * kernelSize,
	            height + 2 * kernelSize);
	      Graphics2D ghostGraphics = (Graphics2D) ghostContour.getGraphics()
	            .create();
	      ghostGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	            RenderingHints.VALUE_ANTIALIAS_ON);

	      ghostGraphics.setStroke(new BasicStroke(2 * kernelSize));
	      ghostGraphics.setColor(Color.black);
	      ghostGraphics.translate(kernelSize, kernelSize);
	      ghostGraphics.draw(s);


//			      graphics.drawImage(ghostContour, 0, 0, width - 1, shineHeight,
//			    	         kernelSize, kernelSize, kernelSize + width - 1, kernelSize
//			    	         + shineHeight, null);


	      /** part 2 **/

	      int kernelMatrixSize = (2 * kernelSize + 1) * (2 * kernelSize + 1);
	      float[] kernelData = new float[kernelMatrixSize];
	      for (int i = 0; i < kernelMatrixSize; i++)
	         kernelData[i] = 1.0f / kernelMatrixSize;
	      Kernel kernel = new Kernel(2 * kernelSize, 2 * kernelSize,
	            kernelData);
	      ConvolveOp convolve = new ConvolveOp(kernel);
	      BufferedImage blurredGhostContour = getBlankImage(width + 2
	            * kernelSize, height + 2 * kernelSize);
	      convolve.filter(ghostContour, blurredGhostContour);


//			      graphics.drawImage(blurredGhostContour, 0, 0, width - 1, shineHeight,
//			    	         kernelSize, kernelSize, kernelSize + width - 1, kernelSize
//			    	         + shineHeight, null);


	      /** part 3 **/

	      BufferedImage reverseGhostContour = getBlankImage(width + 2
		         * kernelSize, height + 2 * kernelSize);
		   Graphics2D reverseGraphics = (Graphics2D) reverseGhostContour
		         .getGraphics();
		   Color bottomShineColorTransp = new Color(bottomShineColor.getRed(),
		         bottomShineColor.getGreen(), bottomShineColor.getBlue(), 32);
		   GradientPaint gradientShine = new GradientPaint(0, kernelSize,
		         topShineColor, 0, kernelSize + shineHeight,
		         bottomShineColorTransp);
		   reverseGraphics.setPaint(gradientShine);
		   reverseGraphics.fillRect(0, kernelSize, width + 2 * kernelSize,
		         kernelSize + shineHeight);
		   reverseGraphics.setComposite(AlphaComposite.DstOut);
		   reverseGraphics.drawImage(blurredGhostContour, 0, 0, null);



	      graphics.drawImage(reverseGhostContour, 0, 0, width - 1, shineHeight,
	    	         kernelSize, kernelSize, kernelSize + width - 1, kernelSize
	    	         + shineHeight, null);


		   /** part 4 */

		   BufferedImage overGhostContour = getBlankImage(width + 2
		         * kernelSize, height + 2 * kernelSize);
		   Graphics2D overGraphics = (Graphics2D) overGhostContour
		         .getGraphics();
		   overGraphics.setPaint(new GradientPaint(0, kernelSize,
		         topFillColor, 0, kernelSize + height / 2, midFillColor));
		   overGraphics.fillRect(kernelSize, kernelSize, kernelSize + width,
		         kernelSize + shineHeight);
		   overGraphics.setComposite(AlphaComposite.DstIn);
		   overGraphics.drawImage(blurredGhostContour, 0, 0, null);


		   graphics.drawImage(overGhostContour, 0, 0, width - 1, shineHeight,
		         kernelSize, kernelSize, kernelSize + width - 1, kernelSize
		         + shineHeight, null);



	      // dispose
      reverseGraphics.dispose();
		  overGraphics.dispose();
		  ghostGraphics.dispose();
	      graphics.dispose();


	      return image;
	   }
		public static BufferedImage getBlankImage(int width, int height) {


			BufferedImage image = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_ARGB);

			// get graphics and set hints
			Graphics2D graphics = (Graphics2D) image.getGraphics().create();

			graphics.setColor(new Color(0, 0, 0, 0));
			graphics.setComposite(AlphaComposite.Src);
			graphics.fillRect(0, 0, width, height);
			graphics.dispose();

			return image;
		}


    void draw2DPiece(Graphics g, Point p, Color c, Color highlightColor, int r) {

        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);

        int width = r < 30 ? r + 3 : r + 5;
        int offset = r < 30 ? 1 : 2;
        if (highlightColor != null) {
            fillOval(g, p.x - offset, p.y - offset, width, highlightColor);
        }

        fillOval(g, p.x, p.y, r, Color.black);  // black
        fillOval(g, p.x + 1, p.y + 1, r - 2, c);  // player color
    }

    void draw3DPiece(Graphics g, Point p, Color c[], Color highlightColor, int r) {

        Graphics2D g2 = (Graphics2D)g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);

        int width = r < 30 ? r + 4 : r + 6;
        int offset = r < 30 ? 1 : 2;

        if (highlightColor != null) {
            fillOval(g, p.x - offset, p.y - offset, width, highlightColor); // highlight
        }

        int darkWidth = r - 1;
        int lightWidth = (int) (darkWidth) * 5 / 6;
        int reflectionWidth = (darkWidth < 18) ? 4 : 6;
        int reflectionOffset = darkWidth / 4;

        fillOval(g, p.x + 2, p.y + 2, darkWidth, shadowColor); // shadow
        fillOval(g, p.x, p.y, darkWidth, c[0]);  // dark
        fillOval(g, p.x + 1, p.y + 1, lightWidth, c[1]); // light
        fillOval(g, p.x + reflectionOffset, p.y + reflectionOffset, reflectionWidth, Color.white); // reflection

    }

    protected void fillOval(Graphics g, int x, int y, int r, Color c) {
        g.setColor(c);
        g.fillOval(x, y, r, r);
    }

    Point getGridMove(int x, int y) {

        x -= getStartX();
        y -= getStartY();
        y += gridPieceSize;

        if (piecesOnGrid) {
            x += gridPieceSize / 2;
            y -= gridPieceSize / 2;
        }
        if (y < 0) {
            return null;
        }

        if (x < 0) {
        	return null;
        }

        x /= gridPieceSize;
        y /= gridPieceSize;
        y = gridHeight - 1 - y;

        int piecesOnGridOffset = (piecesOnGrid) ? 0 : 1;
        if (x >= 0 && x < gridWidth - piecesOnGridOffset &&
            y >= 0 && y < gridHeight - piecesOnGridOffset) {
            return new Point(x, y);
        }
        else {
            return null;
        }
    }

    // mouse click handling code to notify move listeners
    class MoveEventGenerator extends MouseAdapter {

        // listen for mouse presses instead of mouse clicks
        // mouse clicks are only registered when click down/up in same
        // place, doesn't always happen when moves are being made quickly

		public void mousePressed(MouseEvent e) {

            boolean gridClicked = false;
            Point gridMove = null;

            synchronized (drawLock) {

                gridMove = getGridMove(e.getX(), e.getY());
                if (gridMove != null) {
                    gridClicked = true;
                }
            }

            if (gridClicked) {
                for (int i = 0; i < listeners.size(); i++) {
                    GridBoardListener l = (GridBoardListener) listeners.elementAt(i);

                    // which button pressed changed in 1.4
                    l.gridClicked(gridMove.x, gridMove.y, e.getModifiers());
                }
            }
        }
		public void mouseExited(MouseEvent e) {
			setThinkingPieceVisible(false);
		}
    }

    class ThinkingPieceMoveGenerator extends MouseMotionAdapter {

		public void mouseMoved(MouseEvent e) {

            boolean gridMoved = false;
            Point gridMove = null;

            synchronized (drawLock) {
                gridMove = getGridMove(e.getX(), e.getY());
                if (gridMove == null) {
                    if (thinkingPiece.getX() != -1 || thinkingPiece.getY() != -1) {

                        oldThinkingPiece.setX(thinkingPiece.getX());
                        oldThinkingPiece.setY(thinkingPiece.getY());

                        thinkingPiece.setX(-1);
                        thinkingPiece.setY(-1);
                        repaint();

                        gridMoved = true;
                        gridMove = new Point(-1, -1);
                    }
                }
                else {
                    if (gridMove.x != thinkingPiece.getX() ||
                        gridMove.y != thinkingPiece.getY()) {

                        oldThinkingPiece.setX(thinkingPiece.getX());
                        oldThinkingPiece.setY(thinkingPiece.getY());

                        thinkingPiece.setX(gridMove.x);
                        thinkingPiece.setY(gridMove.y);

                        gridMoved = true;

                        repaint();
                    }
                }
            }

            if (message != null) {
            	Rectangle r = getMessageDimensions();
            	if (!hideMessage && r.contains(e.getX(), e.getY())) {
            		hideMessage = true;
                    repaint();
            	}
            	else if (hideMessage && !r.contains(e.getX(), e.getY())) {
            		hideMessage = false;
            		repaint();
            	}
            }

            // moved out of synchronized block due to deadlock
            if (gridMoved) {
                for (int i = 0; i < listeners.size(); i++) {
                    GridBoardListener l = (GridBoardListener) listeners.elementAt(i);
                    l.gridMoved(gridMove.x, gridMove.y);
                }
            }
        }

    }
    public void incrementCaptures(int player) {
        synchronized (drawLock) {
            captures[player]++;
            boardDirty = true;
        }
        repaint();
    }
    public void decrementCaptures(int player) {
        synchronized (drawLock) {
            captures[player]--;
            boardDirty = true;
        }
        repaint();
    }


    protected int getStartX() {
        return beveledEdge + insets.left + captureAreaWidth + edgeLeftOvers.width + coordinatesDimensions.width;
    }

    protected void calculateGridSize() {

        Dimension size = getSize();

        // get coordinates width/height
        if (drawCoordinates) {
	        Font f = new Font("Helvetica", Font.PLAIN, 10);
	        FontMetrics fm = emptyBoardGraphics.getFontMetrics(f);
	        coordinatesDimensions.width = fm.stringWidth("10") + 2;
	        coordinatesDimensions.height = fm.getAscent() + 2;
        }
        else {
        	coordinatesDimensions.width = 0;
        	coordinatesDimensions.height = 0;
        }
        // end coordinates

        // get gridpiecesize
        size.width -= (insets.left + insets.right + beveledEdge * 2 + coordinatesDimensions.width * 2);
        size.height -= (insets.top + insets.bottom + beveledEdge * 2 + coordinatesDimensions.height * 2);

        int gridPieceSizeWidth = size.width / (gridWidth);
        int gridPieceSizeHeight = size.height / (gridHeight - 1);

        int oldGPS = gridPieceSize;
        gridPieceSize = gridPieceSizeWidth < gridPieceSizeHeight ?
            gridPieceSizeWidth : gridPieceSizeHeight;
        // end gridpiecesize

        if (oldGPS != gridPieceSize) {
        	pieces[1] = drawBufferedPiece(gridPieceSize, 1);
        	pieces[2] = drawBufferedPiece(gridPieceSize, 2);
        }

        captureAreaWidth = gridPieceSize / 2;

        // get edges left overs
        edgeLeftOvers.width = (size.width - gridPieceSize * gridWidth) / 2;
        edgeLeftOvers.height = (size.height - gridPieceSize * (gridHeight - 1)) / 2;
    }
    protected void drawBoard(Graphics boardGraphics) {
        // draw highlight piece below other pieces
        if (gameOptions.getShowLastMove()) {
            for (int i = gridPieces.size() - 1; i >= 0; i--) {
                GridPiece piece = (GridPiece) gridPieces.elementAt(i);
                if (piece == highlightPiece) {
                    drawPiece(boardGraphics, piece);
                    break;
                }
            }
        }

        // loop through pieces and draw on board
        for (int i = gridPieces.size() - 1; i >= 0; i--) {
            GridPiece piece = (GridPiece) gridPieces.elementAt(i);

            if (gameOptions.getShowLastMove() && piece == highlightPiece) {
                continue;
            }

            drawPiece(boardGraphics, piece);
        }

        int x = beveledEdge + insets.left + edgeLeftOvers.width;
        int y = getStartY();

        Color c[] = GameStyles.colors[gameOptions.getPlayerColor(2)];
        for (int i = 0; i < captures[1]; i++) {
            //int maxj = captures[1] >= 2 * (i + 1) ? 2 : 1;
            //for (int j = 0; j < maxj; j++) {
                if (gameOptions.getDraw3DPieces()) {

                	fillOval(boardGraphics, x + 1, y + i * gridPieceSize/2 + 1, gridPieceSize/2-1, shadowColor); // shadow

                	boardGraphics.drawImage(drawBufferedPiece(gridPieceSize/2, 2), x, y + i * gridPieceSize/2, this);
                	//draw3DPiece(boardGraphics, new Point(x + j * gridPieceSize, y + i * gridPieceSize), c, null, gridPieceSize);
                }
                else {
                    draw2DPiece(boardGraphics, new Point(x, y + i * gridPieceSize), c[1], null, gridPieceSize);
                }
            //}
        }

	   	int fontSize = 8;
	    if (gridPieceSize > 14) {
	        fontSize += 2;
	    }
	    if (gridPieceSize > 24) {
	        fontSize += 2;
	    }
        Font f = new Font("Helvetica", Font.PLAIN, fontSize);
        FontMetrics fm = emptyBoardGraphics.getFontMetrics(f);
        if (captures[1] > 0) {
            boardGraphics.setFont(f);
            boardGraphics.setColor(Color.red);
            int capNumX = x + (gridPieceSize/2 - fm.stringWidth(Integer.toString(captures[1]))) / 2;
        	int capNumY = getStartY() + (captures[1] * gridPieceSize/2) + 10;
        	boardGraphics.drawString(Integer.toString(captures[1]), capNumX, capNumY);
        }

        x = getStartX() + gridPieceSize * (gridWidth - 1) + coordinatesDimensions.width;
        y = getStartY();
        c = GameStyles.colors[gameOptions.getPlayerColor(1)];
        for (int i = 0; i < captures[2]; i++) {
            //int maxj = captures[2] >= 2 * (i + 1) ? 2 : 1;
           // for (int j = 0; j < maxj; j++) {
                if (gameOptions.getDraw3DPieces()) {
                	fillOval(boardGraphics, x + 1, y + i * gridPieceSize/2 + 1, gridPieceSize/2-1, shadowColor); // shadow

                	boardGraphics.drawImage(drawBufferedPiece(gridPieceSize/2, 1), x, y + i * gridPieceSize/2, this);
                	//draw3DPiece(boardGraphics, new Point(x + j * gridPieceSize, y + i * gridPieceSize), c, null, gridPieceSize);
                }
                else {
                    draw2DPiece(boardGraphics, new Point(x, y + i * gridPieceSize/2), c[1], null, gridPieceSize/2);
                }
            //}
        }
        if (captures[2] > 0) {
            boardGraphics.setFont(f);
            boardGraphics.setColor(Color.red);
            int capNumX = x + (gridPieceSize/2 - fm.stringWidth(Integer.toString(captures[2]))) / 2;
        	int capNumY = getStartY() + (captures[2] * gridPieceSize/2) + 10;
        	boardGraphics.drawString(Integer.toString(captures[2]), capNumX, capNumY);
        }
    }

	public void setPlayer1Name(String player1Name) {
		synchronized (drawLock) {
			boardDirty = true;
			this.player1Name = player1Name;
		}
		repaint();
	}

	public void setPlayer2Name(String player2Name) {
		synchronized (drawLock) {
			boardDirty = true;
			this.player2Name = player2Name;
		}
		repaint();
	}

}
