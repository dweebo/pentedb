package org.pente.gameServer.client.awt;

import java.awt.*;

import org.pente.game.*;
import org.pente.gameServer.client.*;
import org.pente.gameServer.core.*;

/**
 * @author dweebo
 */
public class GameBoard extends Container implements GridCoordinatesChangeListener,
	GameOptionsChangeListener {

	private GameOptions gameOptions;
	private GridCoordinates coordinates;

	private GridBoardComponent gridBoardComponent;
    private GridBoardOrderedPieceCollectionAdapter gridBoard;
    private GridState gridState;

    private GridState gridStates[];

	public GameBoard(PenteBoardComponent board,
			 GameOptions go,
			 boolean allowMovesWhileViewingHistory) {
       if (board instanceof GameOptionsChangeListener) {
           ((GameOptionsChangeListener) board).gameOptionsChanged(go);
       }
		init(go, getDefaultCoordinates(), null, board, allowMovesWhileViewingHistory);
	}

	private GameOptions getDefaultGameOptions() {

        GameOptions gameOptions = new SimpleGameOptions(3);
        gameOptions.setPlayerColor(GameOptions.WHITE, 1);
        gameOptions.setPlayerColor(GameOptions.BLACK, 2);
        gameOptions.setDraw3DPieces(true);
        gameOptions.setShowLastMove(true);
		return gameOptions;
	}
	private GridCoordinates getDefaultCoordinates() {
		return new AlphaNumericGridCoordinates(19, 19);
	}

	private void init(
		GameOptions gameOptions,
	    GridCoordinates coordinates,
		GridBoardListener gridBoardListener, //optional
		PenteBoardComponent board,
		boolean allowMovesWhileViewingHistory) { // optional

		this.gameOptions = gameOptions;
		this.coordinates = coordinates;

		gridBoardComponent = board;

	    // setup adapters
	    gridBoard = new PenteBoardOrderedPieceCollectionAdapter(
	    	(PenteBoardComponent) gridBoardComponent, allowMovesWhileViewingHistory);
		gridBoard.setGridHeight(19);
		gridBoard.setGridWidth(19);
		gridBoard.setOnGrid(true);
		gridBoard.setThinkingPiecePlayer(1);
		gridBoard.setThinkingPieceVisible(false);
		gridBoard.setDrawInnerCircles(true);
		if (gridBoardListener != null) {
			gridBoard.addGridBoardListener(gridBoardListener);
		}

	    // end setup adapters

	    // setup grid states
	    PenteStatePieceCollectionAdapter penteState = new PenteStatePieceCollectionAdapter(getGomoku());
	    penteState.addOrderedPieceCollectionListener(gridBoard);

	    PenteStatePieceCollectionAdapter keryoState = new PenteStatePieceCollectionAdapter(getGomoku());
	    keryoState.setCaptureLengths(new int[] {2, 3});
	    keryoState.setCapturesToWin(15);
	    keryoState.addOrderedPieceCollectionListener(gridBoard);

	    PenteStatePieceCollectionAdapter gpenteState = new PenteStatePieceCollectionAdapter(getGomoku());
	    gpenteState.setGPenteRules(true);
	    gpenteState.addOrderedPieceCollectionListener(gridBoard);

	    PenteStatePieceCollectionAdapter dpenteState = new PenteStatePieceCollectionAdapter(getGomoku());
	    dpenteState.setDPenteRules(true);
	    dpenteState.addOrderedPieceCollectionListener(gridBoard);

	    GridStatePieceCollectionAdapter gomokuState = new GridStatePieceCollectionAdapter(new SimpleGomokuState(19, 19));
	    gomokuState.addOrderedPieceCollectionListener(gridBoard);

	    PoofPenteStatePieceCollectionAdapter poofPenteState = new PoofPenteStatePieceCollectionAdapter(getGomoku());
	    poofPenteState.addOrderedPieceCollectionListener(gridBoard);

	    GridStatePieceCollectionAdapter connect6State = new GridStatePieceCollectionAdapter(
	    	new SimpleConnect6State(19, 19));
	    connect6State.addOrderedPieceCollectionListener(gridBoard);

	    PenteStatePieceCollectionAdapter boatPenteState = new PenteStatePieceCollectionAdapter(
	    	getGomoku());
	    boatPenteState.addOrderedPieceCollectionListener(gridBoard);

	    gridStates = new GridState[] { new SynchronizedPenteState(penteState),
	                                   new SynchronizedPenteState(keryoState),
	                                   new SynchronizedGridState(gomokuState),
	                                   new SynchronizedPenteState(dpenteState),
	                                   new SynchronizedPenteState(gpenteState),
	                                   poofPenteState,
	                                   connect6State,
	                                   boatPenteState,
	                                   };
	    gridState = gridStates[0]; // PENTE
	    gridBoard.setGameName(GridStateFactory.getGameName(GridStateFactory.PENTE));

		setLayout(new BorderLayout(0, 0));
		add("Center", ((Component) gridBoardComponent));
	}

	private SimpleGomokuState getGomoku() {
		SimpleGomokuState gs = new SimpleGomokuState(19, 19);
		gs.allowOverlines(true);
		return gs;
	}
	public static final int[] GAME_BG_COLORS = new int[] {
		new Color(253, 222, 163).getRGB(),
		new Color(186, 253, 163).getRGB(),
		new Color(163, 253, 235).getRGB(),
		new Color(163, 205, 253).getRGB(),
		new Color(174, 163, 253).getRGB(),
		new Color(237, 163, 253).getRGB(),
		new Color(237, 163, 253).getRGB(),
		new Color(37, 186, 255).getRGB()
	};

	//only used by tbapplet right now
	public void setGame(int game) {
		int num = game;
		if (game >= 51) {
			num -= 51;
		}
		setGridState(num / 2);
		gridBoard.setBackgroundColor(GAME_BG_COLORS[num / 2]);
	    gridBoard.setGameName(GridStateFactory.getGameName(game));
	}
	//num doesn't correspond to game right now
	public void setGridState(int num) {
		gridState = gridStates[num];
	}
	public void setGameById(int gameId) {
		setGridState(gameId / 2);
		gridBoard.setBackgroundColor(GAME_BG_COLORS[gameId / 2]);
		gridBoard.setGameName(GridStateFactory.getGameName(gameId));
	}

	public GridState getGridState() {
		return gridState;
	}
	public GridBoardOrderedPieceCollectionAdapter getGridBoard() {
		return gridBoard;
	}
	public GridBoardComponent getGridBoardComponent() {
		return gridBoardComponent;
	}

	//GridCoordinatesChangeListener
	public void gridCoordinatesChanged(GridCoordinates gridCoordinates) {
		((GridCoordinatesChangeListener) gridBoardComponent).gridCoordinatesChanged(gridCoordinates);
	}
	//end GridCoordinatesChangeListener

	//GameOptionsChangeListener
	public void gameOptionsChanged(GameOptions gameOptions) {
		((GameOptionsChangeListener) gridBoardComponent).gameOptionsChanged(gameOptions);
	}
	//end GameOptionsChangeListener

	public void setMessage(String message) {
		gridBoard.setMessage(message);
	}

	public void addPiece(GridPiece piece) {
		gridBoardComponent.addPiece(piece);
	}
	public void updatePiecePlayer(int x, int y, int player) {
		gridBoardComponent.updatePiecePlayer(x, y, player);
	}
	public void removePiece(GridPiece piece) {
		gridBoardComponent.removePiece(piece);
	}

	public void addGridBoardListener(GridBoardListener listener) {
		gridBoardComponent.addGridBoardListener(listener);
	}
	public void removeGridBoardListener(GridBoardListener listener) {
		gridBoardComponent.removeGridBoardListener(listener);
	}

	public void setCursor(int type) {
		gridBoardComponent.setCursor(type);
	}

	public void destroy() {
		if (gridBoardComponent != null) {
			gridBoardComponent.destroy();
		}
	}
	public GridCoordinates getCoordinates() {
		return coordinates;
	}
	public GameOptions getGameOptions() {
		return gameOptions;
	}
}
