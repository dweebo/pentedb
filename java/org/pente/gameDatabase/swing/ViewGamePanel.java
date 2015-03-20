package org.pente.gameDatabase.swing;

import java.awt.*;

import  sun.audio.*;

import javax.swing.border.*;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.event.*;
import javax.swing.tree.*;

import java.text.*;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;

import org.jdesktop.swingx.JXDatePicker;
import org.pente.game.*;
import org.pente.gameDatabase.swing.importer.SGFGameFormat;
import org.pente.gameDatabase.swing.component.*;
import org.pente.gameServer.client.*;
import org.pente.gameServer.client.awt.*;
import org.pente.gameServer.core.*;
import org.pente.mmai.*;

/**
 * @author dweebo
 */
public class ViewGamePanel extends JPanel implements TabComponent, VenueListener {

	private Main main;

	// components
    // custom awt
	private PlunkPenteBoardLW board;
	private GameBoard gameBoard;

	private GridState gridState;

	// swing
	private PlunkDbUtil plunkDbUtil;
	private PlunkGameVenueStorer gameVenueStorer;

	// review data
	private MoveTreeModel currentTreeModel;
	private JTextPane comments;
	private JPopupMenu treePop;
	private JMenuItem rename, delete;

	private DateFormat dateFormat;
    private DateFormat dateFormat2 = new SimpleDateFormat("MM/dd/yyyy");
	private PlunkGameData gameData, oldGameData;
	private JLabel p1Label, p2Label, p1RatingLabel, p2RatingLabel;

	//private JTextField dbText;
	private String dbInitialText = "Type new or select from list";
	private JTextField siteText;
	private ListAutoCompleter siteAuto;
	private JTextField eventText;
	private ListAutoCompleter eventAuto;
	private JTextField roundText;
	private ListAutoCompleter roundAuto;
	private JComboBox dbChoice;
	private JSpinner p1RatingSpinner, p2RatingSpinner, initialTimeSpinner, incrementalTimeSpinner;
	private JComboBox timedChoice;
	private JComboBox ratedChoice;
	private JComboBox swapChoice;
	private JComboBox winnerChoice;
	private JTextField p1, p2;
	private JXDatePicker datePicker;
	private List<TabComponentEditListener> editListeners = new ArrayList<TabComponentEditListener>();
	private String name;
	private int db;
	private GameDbData gameDbData;
	private GameOptions gameOptions;

	private boolean dirty;

	private PlunkNode moveRoot;

	private Ai ai;

	private AiVisualizationPanel aiPanel;
	private AiSettingsPanel aiSettings;

	private JTabbedPane tabbed = new JTabbedPane();


	public ViewGamePanel(final Main main,
			final PlunkDbUtil plunkDbUtil,
			PlunkGameVenueStorer gameVenueStorer,
			PlunkPenteGameStorer gameStorer,
			final PlunkGameData gameData, GameOptions go, int turn,
			PlunkNode moveRoot, String name, int db, boolean imported,
			boolean aiEnabled, List<PlunkTree> trees) {

		this.main = main;
		this.plunkDbUtil = plunkDbUtil;
		this.gameVenueStorer = gameVenueStorer;
		this.gameData = gameData;
		this.name = name;
		this.db = db;
		this.moveRoot = moveRoot;
		this.gameOptions = go;

		oldGameData = new PlunkGameData(gameData);

		dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm z");

        board = new PlunkPenteBoardLW();
        board.gridCoordinatesChanged(new AlphaNumericGridCoordinates(19, 19));
		gameBoard = new GameBoard(board, go, true);
		gameBoard.setGame(GridStateFactory.getGameId(gameData.getGame()));
		gridState = GridStateFactory.createGridState(GridStateFactory.getGameId(gameData.getGame()));
		if (gameData.getGame().equals(GridStateFactory.PENTE_GAME.getName()) ||
	    	gameData.getGame().equals(GridStateFactory.KERYO_GAME.getName()) ||
	    	gameData.getGame().equals(GridStateFactory.BOAT_PENTE_GAME.getName())) {
            ((PenteState) gameBoard.getGridState()).setTournamentRule(false);
            ((PenteState) gridState).setTournamentRule(false);
        }

		if (aiEnabled) {
			ai = new Ai();
			aiPanel = new AiVisualizationPanel(ai, GridStateFactory.getGameId(gameData.getGame()), go);
			aiSettings = new AiSettingsPanel(ai, trees, true, aiPanel,
				"Start Game", "Stop AI");
			aiSettings.addStartActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getActionCommand().equals("Start Game")) {
						ai.setGame(GridStateFactory.getGameId(gameData.getGame()));
						ai.setActive(true);
						aiMove();

						updateMessage();

						//TODO move this somewhere else since user might not click start again
						if (!gridState.isGameOver()) {
							String me = "me";
							try {
								PlunkProp nameProp = plunkDbUtil.loadProp("name");
								if (nameProp != null) {
									me = nameProp.getValue().toString();
								}
							} catch (Exception ex) {}

					        PlayerData p1Data = new DefaultPlayerData();
				        	p1Data.setUserIDName(ai.getSeat() == 1 ? "mmai" + ai.getLevel() : me);
				        	gameData.setPlayer1Data(p1Data);

					        PlayerData p2Data = new DefaultPlayerData();
				        	p2Data.setUserIDName(ai.getSeat() == 2 ? "mmai" + ai.getLevel() : me);
				        	gameData.setPlayer2Data(p2Data);

				        	p1.setText(p1Data.getUserIDName());
				        	p2.setText(p2Data.getUserIDName());
						}
					}
				}
			});


			ai.addAiListener(aiListener);
			ai.addAiListener(aiPanel);
			ai.addAiListener(aiSettings);
		}

		gameBoard.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getWheelRotation() < 0) {
					for (int i = 0; i < -e.getWheelRotation(); i++) {
						currentTreeModel.prevMove();
					}
				}
				else {
					for (int i = 0; i < e.getWheelRotation(); i++) {
						currentTreeModel.nextMove();
					}
				}
			}
		});
		board.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {

				int button = e.getModifiers();
				if ((button & MouseEvent.BUTTON2_MASK) == MouseEvent.BUTTON2_MASK ||
		            (button & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {

	            	if (gameData.isEditable() &&
	            		((button & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK) ||
	            		 ((button & MouseEvent.SHIFT_MASK) == MouseEvent.SHIFT_MASK)) {
	            		currentTreeModel.deleteNode(currentTreeModel.getCurrentTreeNode());
	            	}
	            	else {
	            		currentTreeModel.prevMove();
	            	}
	            	return;
	            }
			}
		});

		if (gameData.isEditable()) {

			gameBoard.addGridBoardListener(new GridBoardListener() {
				public void gridClicked(int x, int y, int button) {

					if ((button & MouseEvent.BUTTON2_MASK) == MouseEvent.BUTTON2_MASK ||
				        (button & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {
						return;
					}

					if (currentTreeModel.getCurrentNode() != null &&
						currentTreeModel.getCurrentNode().getChildCount() != 0) {
						return;
					}

					if (gridState.isGameOver()) return;

					// if computer's turn, don't allow moves
					if (ai != null && ai.isActive() && ai.getSeat() == gridState.getCurrentPlayer()) return;

					int move = (gameBoard.getGridBoard().getGridHeight() - y - 1) *
	                	gameBoard.getGridBoard().getGridWidth() + x;

	                makeMove(move);
	                if (!gameBoard.getGridState().isGameOver()) {
	                	aiMove();
	                }
				}
				public void gridMoved(int x, int y) {
	                if (gridState.isGameOver() ||
	                	(currentTreeModel.getCurrentNode() != null &&
						currentTreeModel.getCurrentNode().getChildCount() != 0) ||
						(ai != null && ai.isActive() && ai.getSeat() == gridState.getCurrentPlayer())) {

	                	gameBoard.setCursor(Cursor.DEFAULT_CURSOR);
	                    gameBoard.getGridBoard().setThinkingPieceVisible(false);
	                    return;
	                }

					int move = (gameBoard.getGridBoard().getGridHeight() - y - 1) *
	                	gameBoard.getGridBoard().getGridWidth() + x;

	                boolean validMove = gridState.isValidMove(move,
	                	gridState.getCurrentPlayer());
					gameBoard.getGridBoard().setThinkingPieceVisible(validMove);
	                if (validMove) {
	                    gameBoard.setCursor(Cursor.HAND_CURSOR);
	                } else {
	                    gameBoard.setCursor(Cursor.DEFAULT_CURSOR);
	                }
				}
			});
		}

		treePop = new JPopupMenu();

		rename = new JMenuItem("Edit");
		rename.setIcon(new ImageIcon(ViewGamePanel.class.getResource("images/pencil.png")));
		rename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TreePath p = currentTreeModel.getTempSelectedPath();

				// temp select again because startEditingAtPath
				// selects the path to be edited 1st and we don't need to
				// do that
				currentTreeModel.tempSelectNode(p);
				currentTreeModel.getJTree().startEditingAtPath(p);
			}
		});

		if (gameData.isEditable()) {
			delete = new JMenuItem("Delete");
			delete.setIcon(new ImageIcon(ViewGamePanel.class.getResource("images/cross.png")));
			delete.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					DefaultMutableTreeNode d = (DefaultMutableTreeNode) currentTreeModel.getTempSelectedPath().getLastPathComponent();
					currentTreeModel.deleteNode(d);
					if (ai != null && ai.isActive()) {
						ai.stopThinking();
					}
					if (ai != null && ai.isActive() && gridState.getCurrentPlayer() == ai.getSeat()) {
						aiMove();
					}
					//updateMessage();
				}
			});
		}

		treePop.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent arg0) {
				currentTreeModel.tempUnselectNode();
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				currentTreeModel.tempUnselectNode();
			};
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
			}
		});
		treePop.setBorder(new BevelBorder(BevelBorder.RAISED));


        comments = new JTextPane();

        JScrollPane commentsScroll = new JScrollPane(comments);
        commentsScroll.setMinimumSize(new Dimension(100, 50));
        commentsScroll.setPreferredSize(new Dimension(100, 50));

        currentTreeModel = createNewTreeModel();

		final JScrollPane treeScroll = new JScrollPane(currentTreeModel.getJTree());
		treeScroll.getViewport().setMinimumSize(new Dimension(100, 200));
		treeScroll.getViewport().setPreferredSize(new Dimension(100, 200));


		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		int y = 1;

		gbc.gridx = 1;
		gbc.gridy = y++;
		gbc.gridheight = 1;
		gbc.weighty = 50;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		leftPanel.add(treeScroll, gbc);

		gbc.gridx = 1;
		gbc.gridy = y++;
		gbc.gridheight = 1;
		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.NONE;
		leftPanel.add(currentTreeModel.getNavPanel(), gbc);

		gbc.gridx = 1;
		gbc.gridy = y++;
		gbc.gridheight = 1;
		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		leftPanel.add(new JLabel("Comments:"), gbc);

		gbc.gridx = 1;
		gbc.gridy = y++;
		gbc.gridheight = 1;
		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		leftPanel.add(commentsScroll, gbc);


		gbc.gridx = 1;
		gbc.gridy = y++;
		gbc.gridheight = 1;
		gbc.weighty = 50;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;

		JScrollPane infoScroll = new JScrollPane(getInfoPanel());
		infoScroll.setMinimumSize(new Dimension(320, 100));
		infoScroll.setPreferredSize(new Dimension(320, 250));

		if (aiEnabled) {

			tabbed = new JTabbedPane();
			tabbed.addTab("AI Settings", aiSettings);
			tabbed.addTab("Game Info", infoScroll);

			gbc.gridx = 1;
			gbc.gridy = y++;
			gbc.gridheight = 1;
			gbc.weightx = 1;
			gbc.weighty = 80;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.anchor = GridBagConstraints.NORTH;
			leftPanel.add(tabbed, gbc);
		}
		else {
			leftPanel.add(infoScroll, gbc);
		}


		JSplitPane topPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
			leftPanel, gameBoard);
		topPane.setContinuousLayout(true);
		//splitPane.setOneTouchExpandable(true);
		topPane.setResizeWeight(0.01);



		currentTreeModel.addListener(new MoveChangeListener() {
			public void changeMoves(int[] moves, PlunkNode current) {

				gameBoard.getGridState().clear();
				gridState.clear();
				for (int m : moves) {
					gameBoard.getGridState().addMove(m);
					gridState.addMove(m);
				}
				if (current != null && current.hasChildren()) {
					for (PlunkNode c : current.getChildren()) {
						if (c.getType() != PlunkNode.NO_TYPE ||
							(c.getName() != null && !c.getName().equals(""))) {
							gameBoard.addPiece(new NoteGridPiece(c));
						}
					}
				}
				if (gameData.isEditable()) {
					gameBoard.getGridBoard().setThinkingPieceVisible(false);
					gameBoard.getGridBoard().setThinkingPiecePlayer(
						gridState.getCurrentColor());
				}

		        analyze();

				updateMessage();
			}
			public void nodeChanged() {
				gameBoard.getGridBoardComponent().refresh();
			}
		});

		addEditListener(
			new TabComponentEditListener() {
				public void editsMade() {
					dirty = true;
				}
				public void editsSaved() {
					dirty = false;
				}
			}
		);

		if (moveRoot != null) {
			currentTreeModel.addMoves(moveRoot, imported);

			GridState localGridState = GridStateFactory.createGridState(
				GridStateFactory.getGameId(gameData.getGame()));
			PlunkNode n = moveRoot;
			while (true) {
				localGridState.addMove(n.getMove());
				if (!n.hasChildren()) break;
				else n = n.getChildren().get(0);
			}


			if (imported) {
				if (gameData.getWinner() != 0) {
		            winnerChoice.setSelectedIndex(gameData.getWinner());
				}
				else if (localGridState.isGameOver()) {
					winnerChoice.setSelectedIndex(gridState.getWinner());
				}
			}

			// if opening a game that matched on the last move of the game
			// then can't move to the next move, so move to the final move
			if (localGridState.getNumMoves() == turn) {
				turn--;
			}

			long hash = localGridState.getHash(turn);
			currentTreeModel.visitNode(hash);
		}
		else if (gameData.isEditable()) {
			currentTreeModel.addMove(180);
		}

		if (ai != null && ai.isActive()) {
            aiMove();
		}


		setLayout(new BorderLayout());
		add("Center", topPane);

		updateMessage();
	}
	private void analyze() {

		((PlunkPenteBoardLW) gameBoard.getGridBoardComponent()).clearLines();
        if (gridState.isGameOver()) return;
        if (GridStateFactory.getGameId(gameData.getGame()) != GridStateFactory.PENTE) return;
        if (!main.showThreats()) return;

		Utilities.analyzePosition(gridState, (PlunkPenteBoardLW) gameBoard.getGridBoardComponent());
	}

	private void updateMessage() {

		if (!gridState.isGameOver() &&
			 gridState.getNumMoves() == 1 &&
			 ai != null && !ai.isActive()) {

			gameBoard.setMessage("You can setup the board using any position you want, then click Start to begin the game");
		}
		else if (gridState.isGameOver() && !gameData.isStored()) {
			gameBoard.setMessage("Game over!");
		}
		else {
			gameBoard.setMessage(null);
		}
	}

	private void makeMove(int move) {
		if (!gridState.isValidMove(move, gridState.getCurrentPlayer())) {
        	return;
        }

		currentTreeModel.addMove(move);

        gameBoard.setCursor(Cursor.DEFAULT_CURSOR);

        gameBoard.getGridBoard().setThinkingPieceVisible(false);
        gameBoard.getGridBoard().setThinkingPiecePlayer(gridState.getCurrentColor());

        if (gridState.isGameOver()) {
        	winnerChoice.setSelectedIndex(gridState.getWinner());
        	//gameBoard.setMessage("Game over");
        }


	}
	private void aiMove() {

        if (!gridState.isGameOver() &&
        	ai != null &&
        	ai.isActive() &&
        	ai.getSeat() == gridState.getCurrentPlayer()) {

        	// this is opening book here
        	// would be better to have this as part of the ai....
        	if (ai.getTreeId() != -1) {
	        	try {
		        	PlunkNode r = plunkDbUtil.loadSubTree(ai.getTreeId(),
		        			gridState.getHash());
		        	if (r != null && r.getChildCount() == 0) {
		        		System.err.println("bad ai opening book position " + ai.getTreeId() + ","+gameBoard.getGridState().getHash());
		        	}
		        	else if (r != null) {
		        		int m = r.getBestMove().getMove();
		        		int rm = gridState.rotateMoveToLocalRotation(m, r.getRotation());
		        		aiPanel.setText("Status: Move from opening book");
		        		makeMove(rm);
		        		return;
		        	}

	        	} catch (SQLException s) {
	        		System.err.println("Error loading opening book moves for " + gridState.getHash());
	        		s.printStackTrace();
	        	}
        	}

			List<PlunkNode> nodes = Utilities.getAllNodes(currentTreeModel.getPlunkRoot());
			int moves[] = new int[nodes.size()];
			for (int i = 0; i < moves.length; i++) moves[i] = nodes.get(i).getMove();

			ai.getMove(moves);
        }
	}


	private AiListener aiListener = new AiListener() {

		public void startThinking() {

		}
		public void stopThinking() {

		}
		public void moveReady(int[] moves, int newMove) {
			currentTreeModel.lastMove();//if user clicked back in history, bring it back to current position
			makeMove(newMove);

			try {
				InputStream in = new FileInputStream("yourturn.au");
				AudioStream as = new AudioStream(in);
				AudioPlayer.player.start(as);
			} catch (Exception e) { e.printStackTrace(); }

		}
		public void aiEvaluateCallBack() {
			if (aiPanel.getAiCount() % 2000 == 0) Thread.yield();//allow other parts of program to work (or other programs)
		}
		public void aiVisualizationCallBack(int[] bd) {
		}
	};

    public void addEditListener(TabComponentEditListener l) {
        currentTreeModel.addEditListener(l);
        editListeners.add(l);

        // hack. if already dirty before adding listener, then notify them
        if (dirty) {
        	notifyEditListeners();
        }
    }
    public void notifyEditListeners() {
    	for (TabComponentEditListener t : editListeners) {
    		t.editsMade();
    	}
    }

	public int save() {
		try {

			currentTreeModel.saveComments();
			if (gameData.isEditable()) {

				if (updateFromUserInput() == JOptionPane.CANCEL_OPTION)
					return JOptionPane.CANCEL_OPTION;

				//TODO provide fields for section maybe leave off for
				//pente.org games that we know are not in tournaments


				gameData.setRoot(currentTreeModel.getPlunkRoot());

				main.saveGame(gameData, gameDbData, oldGameData);
				oldGameData = new PlunkGameData(gameData);

				if (db == 0) {
					db = gameDbData.getID();
					dbChoice.setEnabled(false);
					//dbText.setEnabled(false);
				}

				name = gameData.getPlayer1Data().getUserIDName() + " vs. " + gameData.getPlayer2Data().getUserIDName() + " " +
            		dateFormat2.format(gameData.getDate());
				main.renameTab(this, name);

				//TODO newly added game won't show up in cached
				//do this in 2.0 or beta
				//search results, should i fix that?
				//could just flush all results that match any hash of the moves
				//in the new game

				//same goes for editing a game, might no longer belong
				//and might also be showing old data in game table view

				//also might need to update move db filter panels
				//with new databases,sites,events....

				//editable is not stored currently
				//maybe it is just a property of whether or not the game is
				//stored in the local db?
				//what if stored in both local and pente.org db?
				//seems better to not allow games played at pente.org,bk,iyt,bk
				//to be edited at all, only games w/ other sites

				currentTreeModel.clearDirtyNodes();
			}
			// game is from pente.org so can only add comments
			else {

				// store any comments/node types/name
				if (gameData.isStored()) {
					List<PlunkNode> nodes = currentTreeModel.getDirtyNodes();
					plunkDbUtil.updateMoves(nodes, gameData.getGameID(), db);
				}
				else {

					List<PlunkNode> nodes = new ArrayList<PlunkNode>();
					PlunkNode p = moveRoot;
					do {
						nodes.add(p);
						if (!p.hasChildren()) break;
						p = p.getChildren().get(0);
					} while (true);

					plunkDbUtil.insertMoves(nodes, gameData, db);
					gameData.setStored(true);
				}
				currentTreeModel.clearDirtyNodes();
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(ViewGamePanel.this,
				"Error saving", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

		return JOptionPane.NO_OPTION;
	}

	public PlunkGameData getGameData() {
		List<PlunkNode> nodes = Utilities.getAllNodes(currentTreeModel.getPlunkRoot());
		PlunkGameData d = new PlunkGameData();
		for (PlunkNode n : nodes) {
			d.addMove(n.getMove());
		}
		d.setPlayer1Data(gameData.getPlayer1Data());
		d.setPlayer2Data(gameData.getPlayer2Data());
		d.setDate(gameData.getDate());
		d.setGame(gameData.getGame());
		return d;
	}

	private int updateFromUserInput() throws Exception {
		StringBuffer errors = new StringBuffer();

		gameDbData = null;

		if (!gameData.isEditable() || gameData.isStored()) {
			List<GameDbData> dbData = gameVenueStorer.getDbTree();
			for (GameDbData gdd2 : dbData) {
				if (gdd2.getID() == db) {
					gameDbData = gdd2;
					break;
				}
			}
		}
		else {
			//String d = dbText.getText();
			String d = (String) dbChoice.getEditor().getItem();
			if (d == null || d.equals("") || d.equals(dbInitialText)) {
				errors.append("A database is required.\n");
			}
			else {
				for (GameDbData db : gameVenueStorer.getDbTree()) {
					if (db.getName().equals(d)) {
						gameDbData = db;
						break;
					}
				}
				if (gameDbData == null) {
					gameDbData = new SimpleGameDbData();
					gameDbData.setName(d);
					gameVenueStorer.addGameDbData(gameDbData,
						GridStateFactory.getGameId(gameData.getGame()));
				}
			}
		}

		if (!gameData.isEditable()) return JOptionPane.OK_OPTION;
		//hmm, if game stored in multiple db's, how do know
		//which to put it in here?
		//easiest solution is to pass in db in constructor, known when
		//the game is created and just use that.  downside is changes won't
		//be seen if there is another copy (depends on how update is done)
		//maybe user not allowed to switch it to a different database
		//from this screen, instead there would be "copy to database" button



		// update gamedata with data from fields
		String s = siteText.getText();
		//String s = (String) siteChoice.getEditor().getItem();
		if (s == null || s.equals("")) {
			errors.append("A site is required.\n");
		}
		else {
			gameData.setSite(s);
		}

		String e = eventText.getText();
		//String e = (String) eventChoice.getEditor().getItem();
		if (e == null || e.equals("")) {
			errors.append("An event is required.\n");
		}
		else {
			gameData.setEvent(e);
		}
		String r = roundText.getText();
		//String r = (String) roundChoice.getEditor().getItem();
		if (r != null && !r.equals("")) {
			gameData.setRound(r);
		}

		if (p1.getText() == null || p1.getText().equals("")) {
			errors.append("Player 1 is required.\n");
		}
		else {
			PlayerData p1Data = gameData.getPlayer1Data();
			p1Data.setUserIDName(p1.getText());
			p1Data.setRating((Integer) p1RatingSpinner.getValue());
		}

		if (p2.getText() == null || p2.getText().equals("")) {
			errors.append("Player 2 is required.");
		}
		else {
			PlayerData p2Data = gameData.getPlayer2Data();
			p2Data.setUserIDName(p2.getText());
			p2Data.setRating((Integer) p2RatingSpinner.getValue());
		}

		if (errors.length() != 0) {
			JOptionPane.showMessageDialog(this,
				errors.toString(), "Error Saving",
				JOptionPane.ERROR_MESSAGE);
			return JOptionPane.CANCEL_OPTION;
		}

		gameData.setTimed(((String) timedChoice.getSelectedItem()).equals("Yes"));
		if (gameData.getTimed()) {
			gameData.setInitialTime((Integer) initialTimeSpinner.getValue());
			gameData.setIncrementalTime((Integer) incrementalTimeSpinner.getValue());
		} else {
			gameData.setInitialTime(0);
			gameData.setIncrementalTime(0);
		}

		gameData.setRated(ratedChoice.getSelectedIndex() == 0);
		if (gameData.getGame().equals(GridStateFactory.DPENTE_GAME.getName())) {
			gameData.setSwapped(swapChoice.getSelectedIndex() == 0);
		}

		// update gamedata with moves from currentTreeModel
		while (gameData.getNumMoves() > 0) {
			gameData.undoMove();
		}
		PlunkNode p = currentTreeModel.getPlunkRoot();
		while (true) {
			gameData.addMove(p.getMove());
			if (!p.hasChildren()) break;
			p = p.getChildren().get(0);
		}

		gameData.setDate(datePicker.getDate());

		gameData.setWinner(winnerChoice.getSelectedIndex());
		return JOptionPane.OK_OPTION;
	}

	public void search() {

	}
	public void nextMoves() {

	}
	public void threats() {
		currentTreeModel.refreshBoard();
	}

	public void numberMoves() {
		gameOptions.setDrawDepth(main.showMoveNumber());
		gameBoard.gameOptionsChanged(gameOptions);
	}


	public void showTabPopup(JComponent component, int x, int y) {

		//treePop.removeAll();
		//treePop.add(treeProperties);
		//treePop.add(treeDelete);

		//treePop.show(component, x, y);
	}
	public void hideTabPopup() {
		if (treePop.isVisible()) {
			treePop.setVisible(false);
			currentTreeModel.tempUnselectNode();
		}
	}

	MouseAdapter movePopupListener = new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
			checkPopup(e);
		}
		public void mouseClicked(MouseEvent e) {
			checkPopup(e);
		}
		public void mouseReleased(MouseEvent e) {
			checkPopup(e);
		}
		private void checkPopup(MouseEvent e) {
			if (!e.isPopupTrigger()) {
				if (treePop.isVisible()) {
					treePop.setVisible(false);
					currentTreeModel.tempUnselectNode();
				}
				return;
			}

			TreePath p = currentTreeModel.getJTree().getPathForLocation(e.getX(), e.getY());
			if (p == null) return;
			DefaultMutableTreeNode d = (DefaultMutableTreeNode) p.getLastPathComponent();
			PlunkNode n = (PlunkNode) d.getUserObject();

			treePop.removeAll();
			treePop.add(rename);
			if (n.getDepth() > 0 && gameData.isEditable()) {
				treePop.add(delete);
			}
			currentTreeModel.tempSelectNode(p);

			treePop.show(currentTreeModel.getJTree(), e.getX(), e.getY());
		}
	};

	public void destroy() {
		if (gameBoard != null) {
			gameBoard.destroy();
		}
		if (ai != null) {
			ai.destroy();
		}
	}


	private MoveTreeModel createNewTreeModel() {

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");

		final MoveTreeModel tm = new MoveTreeModel(root);
		//tm.setPlunkTree(plunkTree);
		tm.setComments(comments);
		tm.setGame(GridStateFactory.getGameId(gameData.getGame()));

		//TODO start with focus on K10
		JTree tree = new JTree(tm);
		tm.setJTree(tree);

		tree.addTreeSelectionListener(tm);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setScrollsOnExpand(true);
		tree.addMouseListener(movePopupListener);

		MoveIconRenderer mir = new MoveIconRenderer(tm);
		mir.setGame(GridStateFactory.getGameId(gameData.getGame()));
		tm.setRenderer(mir);

        tree.setCellRenderer(mir);
        tree.setEditable(true);
		MoveEditor moveEditor = new MoveEditor(tree, mir);

		// in case editing node that isn't selected and user
		// then selects other component, this will allow user to later
		// add move
		moveEditor.addCellEditorListener(new CellEditorListener() {
			public void editingCanceled(ChangeEvent e) {
				tm.tempUnselectNode();
			}
			public void editingStopped(ChangeEvent e) {
			}
		});

		tree.setCellEditor(moveEditor);
		tree.getRowHeight();

		return tm;
	}




	public boolean close() {
		if (confirmSave(currentTreeModel) != JOptionPane.CANCEL_OPTION) {
			destroy();
			return true;
		}
		else {
			return false;
		}
	}
	private int confirmSave(MoveTreeModel tm) {

		if (dirty) {
			int c = JOptionPane.showConfirmDialog(
				this, "Game '" + name + "' has been modified. Save changes?",
				"Save Changes", JOptionPane.YES_NO_CANCEL_OPTION);
			if (c == JOptionPane.CANCEL_OPTION) {
				return c;
			}
			else if (c == JOptionPane.YES_OPTION) {
				c = save();
				return c;
			}

		}

		return JOptionPane.NO_OPTION;
	}
	ItemListener editItemWatcher = new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
			notifyEditListeners();
		}
	};
	KeyAdapter editKeyWatcher = new KeyAdapter() {
		public void keyTyped(KeyEvent e) {
			notifyEditListeners();
		}
	};
	ChangeListener editSpinnerWatcher = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			notifyEditListeners();
		}
	};

	private JLabel label(String text) {
		JLabel l = new JLabel(text);
		//l.setBackground(Color.red);
		return l;
	}
	private JPanel getInfoPanel() {
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new GridBagLayout());
		//infoPanel.setBackground(Color.white);
		//Border b = new LineBorder(Color.black, 2);
		//infoPanel.setBorder(b);

		int width = gameData.isEditable() ? 3 : 2;

		GridBagConstraints gbc3 = new GridBagConstraints();
		gbc3.insets = new Insets(1, 1, 1, 1);
		gbc3.fill = GridBagConstraints.HORIZONTAL;
		gbc3.gridx = 1;
		gbc3.gridy = 1;
		gbc3.weightx = 1;
		gbc3.weighty = 1;


		if (gameData.isEditable()) {

			infoPanel.add(label("Database:"), gbc3);
			gbc3.gridx++;
			gbc3.gridwidth = width;
			List<GameDbData> dbData = gameVenueStorer.getDbTree();

			// if already stored can't change the db
			if (gameData.isStored()) {
				for (GameDbData gdd : dbData) {
					if (gdd.getID() == db) {
						infoPanel.add(label(gdd.getName()), gbc3);
						break;
					}
				}
			}
			else {
				dbChoice = new JComboBox();
				dbChoice.setEditable(true);

				boolean found = populateDbCombo();

				final JTextComponent t = (JTextComponent) dbChoice.getEditor().getEditorComponent();

				if (!found) {
					t.setText(dbInitialText);
				}
				t.addFocusListener(new FocusAdapter() {
					@Override
					public void focusGained(FocusEvent e) {
						if (t.getText().equals(dbInitialText)) {
							t.setText("");
						}
					}
					public void focusLost(FocusEvent e) {
						if (t.getText().trim().equals("")) {
							t.setText(dbInitialText);
						}
					}
				});


				infoPanel.add(dbChoice, gbc3);
			}

			gbc3.gridx = 1;
			gbc3.gridy++;
		}

		infoPanel.add(label("Site:"), gbc3);
		gbc3.gridx++;
		gbc3.gridwidth = width;
		if (gameData.isEditable()) {
			siteText = new JTextField();
			siteText.addKeyListener(editKeyWatcher);

			siteAuto = new ListAutoCompleter(siteText, gameVenueStorer.getSites(), false);
			if (gameData.getSite() != null) {
				siteText.setText(gameData.getSite());
			}
			infoPanel.add(siteText, gbc3);
			//siteChoice = new JComboBox();
			//siteChoice.setEditable(true);
			//populateSiteCombo(gameData.getSite());

			//siteChoice.addItemListener(editItemWatcher);
			//siteChoice.getEditor().getEditorComponent().addKeyListener(editKeyWatcher);

			//site = new JTextField(gameData.getSite());
			//infoPanel.add(siteChoice, gbc3);
		}
		else {
			infoPanel.add(label(gameData.getSite()), gbc3);
		}

		gbc3.gridy++;
		gbc3.gridx = 1;
		infoPanel.add(label("Event:"), gbc3);
		gbc3.gridx++;
		gbc3.gridwidth = width;
		if (gameData.isEditable()) {
			eventText = new JTextField();
			eventText.addKeyListener(editKeyWatcher);
			//List<String> events = new ArrayList<String>(gameVenueStorer.getEvents().size());
			//for (GameEventData e : gameVenueStorer.getEvents()) {
			//	events.add(e.getName());
			//}
			//new ListAutoCompleter(eventText, events);
			populateEventCombo();
			if (gameData.getEvent() != null) {
				eventText.setText(gameData.getEvent());
			}
			infoPanel.add(eventText, gbc3);
			//eventChoice = new JComboBox();
			//eventChoice.setEditable(true);
			//eventChoice.addItemListener(editItemWatcher);
			//eventChoice.getEditor().getEditorComponent().addKeyListener(editKeyWatcher);

			//event = new JTextField(gameData.getEvent());
			//infoPanel.add(eventChoice, gbc3);
		}
		else {
			infoPanel.add(label(gameData.getEvent()), gbc3);
		}

		if (gameData.isEditable() || (gameData.getRound() != null && !gameData.getRound().equals(""))) {
			gbc3.gridy++;
			gbc3.gridx = 1;
			infoPanel.add(label("Round:"), gbc3);
			gbc3.gridx++;
			gbc3.gridwidth = width;
			if (gameData.isEditable()) {
				//roundChoice = new JComboBox();
				//roundChoice.setEditable(true);
				roundText = new JTextField();
				roundText.addKeyListener(editKeyWatcher);
				populateRoundCombo();
				if (gameData.getRound() != null) {
					roundText.setText(gameData.getRound());
				}
				//roundChoice.addItemListener(editItemWatcher);
				//roundChoice.getEditor().getEditorComponent().addKeyListener(editKeyWatcher);

				//event = new JTextField(gameData.getEvent());
				infoPanel.add(roundText, gbc3);
			}
			else {
				infoPanel.add(label(gameData.getRound()), gbc3);
			}
		}

		gbc3.gridx = 1;
		gbc3.gridy++;
		gbc3.gridwidth = 1;
		infoPanel.add(label("Player 1:"), gbc3);
		gbc3.gridx++;

		if (gameData.isEditable()) {
			p1 = new JTextField(gameData.getPlayer1Data().getUserIDName(), 10);
			new ListAutoCompleter(p1, main.getPlayerNames(), true);
			p1.addKeyListener(editKeyWatcher);
			p1.getDocument().addDocumentListener(new DocumentListener() {
			    public void insertUpdate(DocumentEvent e) {
			        updateWinner();
			        board.setPlayer1Name(p1.getText());
			    }
			    public void removeUpdate(DocumentEvent e) {
			        updateWinner();
			        board.setPlayer1Name(p1.getText());
			    }
			    public void changedUpdate(DocumentEvent e) {
			        updateWinner();
			        board.setPlayer1Name(p1.getText());
			    }
			    private void updateWinner() {
					int i = winnerChoice.getSelectedIndex();
					winnerChoice.removeItemAt(1);
					winnerChoice.insertItemAt(p1.getText(), 1);
					winnerChoice.setSelectedIndex(i);
				}
			});
			infoPanel.add(p1, gbc3);
		}
		else {
			p1Label = new JLabel(gameData.getPlayer1Data().getUserIDName());
			infoPanel.add(p1Label, gbc3);
		}

		gbc3.gridx++;


		if (gameData.isEditable()) {
			p1RatingLabel = label("Rating:");
			int initRating = gameData.getPlayer1Data().getRating();
			if (initRating == 0) initRating = 1600;

			p1RatingLabel.setIcon(new ImageIcon(ViewGamePanel.class.getResource("images/" +
				getRatingsGifRatingOnly(initRating))));
			p1RatingLabel.setHorizontalTextPosition(SwingConstants.LEFT);

			gbc3.anchor = GridBagConstraints.EAST;
			gbc3.fill = GridBagConstraints.NONE;
			infoPanel.add(p1RatingLabel, gbc3);
			gbc3.gridx++;

			p1RatingSpinner = new JSpinner(new SpinnerNumberModel(initRating,
				0, 3000, 1));
			JSpinner.NumberEditor ne = new JSpinner.NumberEditor(p1RatingSpinner,"####");
			p1RatingSpinner.setEditor(ne);
			p1RatingSpinner.addChangeListener(editSpinnerWatcher);

			infoPanel.add(p1RatingSpinner, gbc3);
			gbc3.fill = GridBagConstraints.HORIZONTAL;

		}
		else {
			p1RatingLabel = label(""+gameData.getPlayer1Data().getRating());
			p1RatingLabel.setIcon(new ImageIcon(ViewGamePanel.class.getResource("images/" +
					getRatingsGifRatingOnly(gameData.getPlayer1Data().getRating()))));

			infoPanel.add(p1RatingLabel, gbc3);
		}

		gbc3.gridx = 1;
		gbc3.gridy++;
		infoPanel.add(label("Player 2:"), gbc3);
		gbc3.gridx++;
		if (gameData.isEditable()) {
			p2 = new JTextField(gameData.getPlayer2Data().getUserIDName(), 10);
			new ListAutoCompleter(p2, main.getPlayerNames(), true);
			p2.addKeyListener(editKeyWatcher);
			p2.getDocument().addDocumentListener(new DocumentListener() {
			    public void insertUpdate(DocumentEvent e) {
			        updateWinner();
			        board.setPlayer2Name(p2.getText());
			    }
			    public void removeUpdate(DocumentEvent e) {
			        updateWinner();
			        board.setPlayer2Name(p2.getText());
			    }
			    public void changedUpdate(DocumentEvent e) {
			        updateWinner();
			        board.setPlayer2Name(p2.getText());
			    }
			    private void updateWinner() {
					int i = winnerChoice.getSelectedIndex();
					winnerChoice.removeItemAt(2);
					winnerChoice.insertItemAt(p2.getText(), 2);
					winnerChoice.setSelectedIndex(i);
				}
			});
			infoPanel.add(p2, gbc3);
		}
		else {
			p2Label = new JLabel(gameData.getPlayer2Data().getUserIDName());
			infoPanel.add(p2Label, gbc3);
		}

		gbc3.gridx++;

		if (gameData.isEditable()) {
			int initRating = gameData.getPlayer2Data().getRating();
			if (initRating == 0) initRating = 1600;
			p2RatingLabel = label("Rating:");
			p2RatingLabel.setIcon(new ImageIcon(ViewGamePanel.class.getResource("images/" +
				getRatingsGifRatingOnly(initRating))));
			p2RatingLabel.setHorizontalTextPosition(SwingConstants.LEFT);

			gbc3.anchor = GridBagConstraints.EAST;
			gbc3.fill = GridBagConstraints.NONE;
			infoPanel.add(p2RatingLabel, gbc3);
			gbc3.gridx++;

			p2RatingSpinner = new JSpinner(new SpinnerNumberModel(initRating,
				0, 3000, 1));
			JSpinner.NumberEditor ne = new JSpinner.NumberEditor(p2RatingSpinner,"####");
			p2RatingSpinner.setEditor(ne);
			//TODO add listener to change rating icon

			p2RatingSpinner.addChangeListener(editSpinnerWatcher);
			infoPanel.add(p2RatingSpinner, gbc3);
			gbc3.fill = GridBagConstraints.HORIZONTAL;
		}
		else {
			p2RatingLabel = label(""+gameData.getPlayer2Data().getRating());
			p2RatingLabel.setIcon(new ImageIcon(ViewGamePanel.class.getResource("images/" +
				getRatingsGifRatingOnly(gameData.getPlayer2Data().getRating()))));

			infoPanel.add(p2RatingLabel, gbc3);
		}
		gbc3.gridx = 1;
		gbc3.gridy++;
		infoPanel.add(label("Timer: "), gbc3);
		gbc3.gridx++;
		if (gameData.isEditable()) {
			timedChoice = new JComboBox();
			timedChoice.addItem("Yes");
			timedChoice.addItem("No");
			timedChoice.setSelectedIndex(gameData.getTimed() ? 0 : 1);
			timedChoice.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.DESELECTED) return;
					notifyEditListeners();
					boolean enable = ((String) timedChoice.getSelectedItem()).equals("Yes");
					initialTimeSpinner.setEnabled(enable);
					incrementalTimeSpinner.setEnabled(enable);
				}
			});
			infoPanel.add(timedChoice, gbc3);

			gbc3.gridx++;
			gbc3.anchor = GridBagConstraints.EAST;
			gbc3.fill = GridBagConstraints.NONE;
			infoPanel.add(label("Initial:"), gbc3);

			gbc3.gridx++;
			int init = gameData.getInitialTime();
			if (init == 0) init = 20;
			initialTimeSpinner = new JSpinner(new SpinnerNumberModel(init,
				0, 999, 1));
			initialTimeSpinner.setEnabled(gameData.getTimed());
			initialTimeSpinner.addChangeListener(editSpinnerWatcher);
			JSpinner.NumberEditor ne = new JSpinner.NumberEditor(initialTimeSpinner,"###");
			initialTimeSpinner.setEditor(ne);
			infoPanel.add(initialTimeSpinner, gbc3);

			gbc3.gridy++;
			gbc3.gridx = 3;
			infoPanel.add(label("Incremental:"), gbc3);

			gbc3.gridx++;
			int inc = gameData.getIncrementalTime();
			incrementalTimeSpinner = new JSpinner(new SpinnerNumberModel(inc,
				0, 999, 1));
			incrementalTimeSpinner.setEnabled(gameData.getTimed());
			incrementalTimeSpinner.addChangeListener(editSpinnerWatcher);
			incrementalTimeSpinner.setEditor(new JSpinner.NumberEditor(incrementalTimeSpinner,"###"));
			infoPanel.add(incrementalTimeSpinner, gbc3);
			gbc3.fill = GridBagConstraints.HORIZONTAL;
		}
		else {
			String timer = "";
			if (gameData.getEvent() != null && gameData.getEvent().equals("Turn-based Game")) {
			    timer = gameData.getInitialTime() + " days/move";
			}
			else if (gameData.getTimed()) {
				timer = gameData.getInitialTime() + "/" + gameData.getIncrementalTime();
			}
			else {
				timer = "No";
			}

			gbc3.gridwidth = width;
			infoPanel.add(label(timer), gbc3);
		}

		gbc3.gridx = 1;
		gbc3.gridy++;
		gbc3.gridwidth = 1;
		infoPanel.add(label("Rated:"), gbc3);
		gbc3.gridx++;
		gbc3.gridwidth = width;

		if (gameData.isEditable()) {
			ratedChoice = new JComboBox();
			ratedChoice.addItem("Yes");
			ratedChoice.addItem("No");
			ratedChoice.setSelectedIndex(gameData.getRated() ? 0 : 1);
			ratedChoice.addItemListener(editItemWatcher);
//			ratedChoice.addItemListener(new ItemListener() {
//				public void itemStateChanged(ItemEvent e) {
//
//					if (gameData.getGame().equals(GridStateFactory.PENTE_GAME.getName()) ||
//						gameData.getGame().equals(GridStateFactory.KERYO_GAME.getName()) ||
//						gameData.getGame().equals(GridStateFactory.BOAT_PENTE_GAME.getName())) {
//
//				        ((PenteState) gameBoard.getGridState()).setTournamentRule(
//				        	ratedChoice.getSelectedIndex() == 0);
//					}
//				}
//			});
			infoPanel.add(ratedChoice, gbc3);
		}
		else {
			String ratedStr = gameData.getRated() ? "Yes" : "No";
			infoPanel.add(label(ratedStr), gbc3);
		}

		if (gameData.getGame().equals(GridStateFactory.DPENTE_GAME.getName())) {
			gbc3.gridx = 1;
			gbc3.gridy++;
			gbc3.gridwidth = 1;
			infoPanel.add(label("Swapped:"), gbc3);
			gbc3.gridx++;
			gbc3.gridwidth = width;


			if (gameData.isEditable()) {
				swapChoice = new JComboBox();
				swapChoice.addItem("Yes");
				swapChoice.addItem("No");
				swapChoice.setSelectedIndex(gameData.didPlayersSwap() ? 0 : 1);
				swapChoice.addItemListener(editItemWatcher);
			}
			else {
				infoPanel.add(label(gameData.didPlayersSwap() ? "Yes" : "No"), gbc3);
			}
		}


		gbc3.gridx = 1;
		gbc3.gridy++;
		gbc3.gridwidth = 1;
		infoPanel.add(label("Game Winner:"), gbc3);
		gbc3.gridx++;
		gbc3.gridwidth = width;

		if (gameData.isEditable()) {

			winnerChoice = new JComboBox();
			String p1 = gameData.getPlayer1Data().getUserIDName();
			if (p1 == null) p1 = "";
			String p2 = gameData.getPlayer2Data().getUserIDName();
			if (p2 == null) p2 = "";
			winnerChoice.addItem("");
			winnerChoice.addItem(p1);
			winnerChoice.addItem(p2);
			if (gameData.getWinner() > 0) {
				winnerChoice.setSelectedIndex(gameData.getWinner());
			}
			infoPanel.add(winnerChoice, gbc3);
		}
		else {
			JLabel winnerLabel = (gameData.getWinner() == 1 ?
				label(p1Label.getText()) : label(p2Label.getText()));

			infoPanel.add(winnerLabel, gbc3);
		}

		gbc3.gridx = 1;
		gbc3.gridy++;
		gbc3.gridwidth = 1;
		infoPanel.add(label("Completion Date:"), gbc3);
		gbc3.gridx++;
		gbc3.gridwidth = width;
		if (gameData.isEditable()) {
			final long time = gameData.getDate() != null ?
				gameData.getDate().getTime() :
				System.currentTimeMillis();

			datePicker = new JXDatePicker(time);
			datePicker.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (datePicker.getDate().getTime() != time) {
						notifyEditListeners();
					}
				}
			});
			datePicker.getEditor().addKeyListener(editKeyWatcher);

			infoPanel.add(datePicker, gbc3);
		}
		else {
			infoPanel.add(label(dateFormat.format(gameData.getDate())), gbc3);
		}

		return infoPanel;
	}
	private boolean populateDbCombo() {

		dbChoice.removeItemListener(editItemWatcher);
		dbChoice.getEditor().getEditorComponent().removeKeyListener(editKeyWatcher);
		dbChoice.removeAllItems();

		boolean found = false;
		for (GameDbData gdd : gameVenueStorer.getDbTree()) {
			dbChoice.addItem(gdd.getName());
			if (db == gdd.getID()) {
				dbChoice.setSelectedItem(gdd.getName());
				found = true;
			}
		}

		dbChoice.addItemListener(editItemWatcher);
		dbChoice.getEditor().getEditorComponent().addKeyListener(editKeyWatcher);

		return found;
	}

	//private void populateSiteCombo(String initSite) {

		//siteChoice.removeAllItems();

		//siteChoice.addItem("");
		//siteChoice.setSelectedIndex(0);

		//boolean found = false;
		//for (GameSiteData sd : gameVenueStorer.getSites()) {
			//if (sd.getSiteID() > 4) {

				//siteChoice.addItem(sd.getName());
				//if (sd.getName().equals(initSite)) {
				//	siteChoice.setSelectedIndex(siteChoice.getItemCount() - 1);
				//	found = true;
				//}
			//}
		//}
		// this is for imports
		//if (!found) {
		//	siteChoice.addItem(initSite);
		//	siteChoice.setSelectedItem(initSite);
		//}
	//}
	private void populateEventCombo() {

		List<String> events = new ArrayList<String>();
		for (GameEventData ed : gameVenueStorer.getEvents()) {
			if (ed.getSiteData() != null && ed.getSiteData().getSiteID() > 4) {
				events.add(ed.getName());
			}
		}
		if (eventAuto == null) {
			eventAuto = new ListAutoCompleter(eventText, events, false);
		}
		else {
			eventAuto.updateList(events);
		}
	}

	private void populateRoundCombo() {

		List<GameRoundData> rounds = new ArrayList<GameRoundData>();
		for (GameEventData ed : gameVenueStorer.getEvents()) {
			if (ed.getSiteData() != null && ed.getSiteData().getSiteID() > 4) {
				rounds.addAll(ed.getGameRoundData());
			}
		}
		if (roundAuto == null) {
			roundAuto = new ListAutoCompleter(roundText, rounds, false);
		}
		else {
			roundAuto.updateList(rounds);
		}
	}

	public void export(File f) {

		StringBuffer buf = new StringBuffer();
		SGFGameFormat gf = new SGFGameFormat("\r\n");
		currentTreeModel.saveComments();
		try {
			if (updateFromUserInput() == JOptionPane.OK_OPTION) {

				PlunkGameData pgd = new PlunkGameData(gameData);
				pgd.setDbName(gameDbData.getName());
				pgd.setRoot(currentTreeModel.getPlunkRoot());
				//gf.format(pgd, gameDbData.getName(), buf);

				Utilities.writeFile(f, buf.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void venuesUpdated() {
		if (!gameData.isEditable()) return;

		populateDbCombo();
		siteAuto.updateList(gameVenueStorer.getSites());
		populateEventCombo();
		populateRoundCombo();
	}
	public static String getRatingsGifRatingOnly(int r) {
  	    String gif = "ratings_";
	    if (r >= 1900) {
	        gif += "red.gif";
	    }
	    else if (r >= 1700) {
	        gif += "yellow.gif";
	    }
	    else if (r >= 1400) {
	        gif += "blue.gif";
	    }
	    else if (r >= 1000) {
	        gif += "green.gif";
	    }
	    else {
	        gif += "gray.gif";
	    }
        return gif;
    }
}
