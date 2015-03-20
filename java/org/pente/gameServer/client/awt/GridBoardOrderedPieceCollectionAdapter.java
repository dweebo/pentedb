package org.pente.gameServer.client.awt;

import java.util.Vector;

import org.pente.gameServer.client.GridBoardComponent;
import org.pente.gameServer.client.GridBoardListener;
import org.pente.gameServer.core.GridPiece;
import org.pente.gameServer.core.GridPieceAction;
import org.pente.gameServer.core.OrderedPieceCollectionAdapter;

/**
 * @author dweebo
 */
public class GridBoardOrderedPieceCollectionAdapter extends OrderedPieceCollectionAdapter
    implements GridBoardComponent, GridBoardListener {

    private GridBoardComponent  gridBoardComponent;

    private Vector              listeners;

    // this is what the client has set the thinking piece to
    // be.  However sometimes this component must set the actual
    // thinking piece to a different value, but then later revert
    // to what the client wanted it to be
    private boolean clientThinkingPieceVisible;
    // but if the client is allowed to make moves while viewing history
    // then that pretty much overrides above
    private boolean allowMovesWhileViewingHistory;

    public GridBoardOrderedPieceCollectionAdapter(
        GridBoardComponent basePieceCollection,
        boolean allowMovesWhileViewingHistory) {

        super(basePieceCollection);

        this.gridBoardComponent = basePieceCollection;
        this.allowMovesWhileViewingHistory = allowMovesWhileViewingHistory;
        clientThinkingPieceVisible = true;

        listeners = new Vector();
        basePieceCollection.addGridBoardListener(this);
    }

    // GridBoardComponent
    public int getGridWidth() {
        return gridBoardComponent.getGridWidth();
    }
    public void setGridWidth(int width) {
        gridBoardComponent.setGridWidth(width);
    }

    public int getGridHeight() {
        return gridBoardComponent.getGridHeight();
    }
    public void setGridHeight(int height) {
        gridBoardComponent.setGridHeight(height);
    }

    public boolean getOnGrid() {
        return gridBoardComponent.getOnGrid();
    }
    public void setOnGrid(boolean onGrid) {
        gridBoardComponent.setOnGrid(onGrid);
    }

    public void setBackgroundColor(int color) {
        gridBoardComponent.setBackgroundColor(color);
    }
    public void setGridColor(int color) {
        gridBoardComponent.setGridColor(color);
    }
    public void setHighlightColor(int color) {
        gridBoardComponent.setHighlightColor(color);
    }
    public void setGameNameColor(int color) {
        gridBoardComponent.setGameNameColor(color);
    }

	public void setMessage(String message) {
		gridBoardComponent.setMessage(message);
	}
    public void setGameName(String gameName) {
        gridBoardComponent.setGameName(gameName);
    }

    public void setHighlightPiece(GridPiece gridPiece) {
        gridBoardComponent.setHighlightPiece(gridPiece);
    }

    public synchronized void setThinkingPieceVisible(boolean visible) {

        clientThinkingPieceVisible = visible;
        if (viewingCurrent || allowMovesWhileViewingHistory) {
            gridBoardComponent.setThinkingPieceVisible(clientThinkingPieceVisible);
        }
    }

    public synchronized void setThinkingPiecePlayer(int player) {
        gridBoardComponent.setThinkingPiecePlayer(player);
    }

    // don't allow clients to explicitly change this
    public void setNewMovesAvailable(boolean available) {
    }

    public void setDrawInnerCircles(boolean drawInnerCircles) {
        gridBoardComponent.setDrawInnerCircles(drawInnerCircles);
    }
    public void setDrawCoordinates(boolean drawCoordinates) {
        gridBoardComponent.setDrawCoordinates(drawCoordinates);
    }

    public void setBoardInsets(int l, int t, int r, int b) {
        gridBoardComponent.setBoardInsets(l,t,r,b);
    }

    public void addGridBoardListener(GridBoardListener listener) {
        listeners.addElement(listener);
    }
    public void removeGridBoardListener(GridBoardListener listener) {
        listeners.removeElement(listener);
    }

    public void addPiece(GridPiece gridPiece) {
        //throw new UnsupportedOperationException("Unsupported: Use addPiece(GridPiece gridPiece, int turn) instead.");
    }
    public void updatePiecePlayer(int x, int y, int player) {
        //throw new UnsupportedOperationException("Unsupported: Use addPiece(GridPiece gridPiece, int turn) instead.");
    }
    public void removePiece(GridPiece gridPiece) {
        //throw new UnsupportedOperationException("Unsupported: Use removePiece(GridPiece gridPiece, int turn) instead.");
    }
    // end GridBoardComponent

    // GridBoardListener
    public void gridClicked(int x, int y, int button) {

		// moved out because of deadlock issues
		synchronized(this) {
			if (!viewingCurrent) {
				return;
			}
		}

        for (int i = 0; i < listeners.size(); i++) {
            GridBoardListener l = (GridBoardListener) listeners.elementAt(i);
            l.gridClicked(x, y, button);
        }
    }
    public void gridMoved(int x, int y) {

		synchronized (this) {
			if (!viewingCurrent) {
				return;
			}
		}

        for (int i = 0; i < listeners.size(); i++) {
            GridBoardListener l = (GridBoardListener) listeners.elementAt(i);
            l.gridMoved(x, y);
        }
    }
    // end GridBoardListener

    // OrderedPieceCollectionAdapter
    public synchronized void addPiece(GridPiece gridPiece, int turn) {
        super.addPiece(gridPiece, turn);

        if (viewingCurrent) {
            gridBoardComponent.setHighlightPiece(getHighlightPiece(currentTurn));
        }
        else {
            gridBoardComponent.setNewMovesAvailable(true);
        }
    }
    public synchronized void removePiece(GridPiece gridPiece, int turn) {
        super.removePiece(gridPiece, turn);
    }
    public synchronized void undoLastTurn() {

        super.undoLastTurn();

        if (viewingCurrent) {
            gridBoardComponent.setHighlightPiece(getHighlightPiece(currentTurn));
            gridBoardComponent.setThinkingPieceVisible(clientThinkingPieceVisible);
            gridBoardComponent.setNewMovesAvailable(false);
        }
    }

    public synchronized void visitNextTurn() {
        super.visitNextTurn();

        gridBoardComponent.setHighlightPiece(getHighlightPiece(currentTurn));

        if (viewingCurrent) {
//System.out.println("visitNextTurn, viewingCurrent");
            gridBoardComponent.setThinkingPieceVisible(clientThinkingPieceVisible);
            gridBoardComponent.setNewMovesAvailable(false);
        }
    }
    public synchronized void visitPreviousTurn() {
        super.visitPreviousTurn();

        gridBoardComponent.setHighlightPiece(getHighlightPiece(currentTurn));
        if (viewingCurrent) {
            gridBoardComponent.setThinkingPieceVisible(clientThinkingPieceVisible);
        }
        else {
            gridBoardComponent.setThinkingPieceVisible(false);
        }
    }
    public synchronized void visitFirstTurn() {
        super.visitFirstTurn();

        gridBoardComponent.setThinkingPieceVisible(false);
    }
    public synchronized void visitLastTurn() {
        super.visitLastTurn();

        gridBoardComponent.setHighlightPiece(getHighlightPiece(currentTurn));
        gridBoardComponent.setThinkingPieceVisible(clientThinkingPieceVisible);
        gridBoardComponent.setNewMovesAvailable(false);
    }
    public synchronized void visitTurn(int turn) {
        super.visitTurn(turn);

        gridBoardComponent.setHighlightPiece(getHighlightPiece(currentTurn));
        if (viewingCurrent) {
            gridBoardComponent.setThinkingPieceVisible(clientThinkingPieceVisible);
            gridBoardComponent.setNewMovesAvailable(false);
        }
        else {
            gridBoardComponent.setThinkingPieceVisible(false);
        }
    }

    private GridPiece getHighlightPiece(int turn) {

        for (int i = pieceActions.size() - 1; i >= 0; i--) {
            GridPieceAction action = (GridPieceAction) pieceActions.elementAt(i);
            if (action.getTurn() == turn) {
                //if (action.getAction() == GridPieceAction.REMOVE) {
                //    return null;
                //}
                if (action.getAction() == GridPieceAction.ADD) {
                    return action.getGridPiece();
                }
            }
        }

        return null;
    }

    public void setCursor(int cursor) {
    	gridBoardComponent.setCursor(cursor);
    }
    public void destroy() {
    	gridBoardComponent.destroy();
    }
    public void refresh() {
    	gridBoardComponent.refresh();
    }

}