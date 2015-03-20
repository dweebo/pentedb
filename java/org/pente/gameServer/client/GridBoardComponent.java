package org.pente.gameServer.client;

import org.pente.gameServer.core.GridPiece;
import org.pente.gameServer.core.PieceCollection;

/**
 * @author dweebo
 */
public interface GridBoardComponent extends PieceCollection {

    public int getGridWidth();
    public void setGridWidth(int width);

    public int getGridHeight();
    public void setGridHeight(int height);

    public boolean getOnGrid();
    public void setOnGrid(boolean onGrid);

    public void setBackgroundColor(int color);
    public void setGridColor(int color);
    public void setHighlightColor(int color);
    public void setGameNameColor(int color);

    public void setGameName(String name);

    public void setHighlightPiece(GridPiece gridPiece);

    public void setThinkingPieceVisible(boolean visible);
    public void setThinkingPiecePlayer(int player);

    public void setNewMovesAvailable(boolean available);

    public void setDrawInnerCircles(boolean drawInnerCircles);
    public void setDrawCoordinates(boolean drawCoordinates);

    public void setBoardInsets(int l, int t, int r, int b);
	public void setMessage(String message);

    public void addGridBoardListener(GridBoardListener listener);
    public void removeGridBoardListener(GridBoardListener listener);

	public void setCursor(int cursor);

	public void refresh();
	public void destroy();
}