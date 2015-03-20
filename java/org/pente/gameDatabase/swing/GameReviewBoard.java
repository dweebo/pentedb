package org.pente.gameDatabase.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.JTableHeader;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Category;
import org.pente.game.Coord;
import org.pente.game.Game;
import org.pente.game.GameData;
import org.pente.game.GameDbData;
import org.pente.game.GameEventData;
import org.pente.game.GameRoundData;
import org.pente.game.GameSiteData;
import org.pente.game.GameTreeData;
import org.pente.game.GridState;
import org.pente.game.GridStateFactory;
import org.pente.game.PenteState;
import org.pente.game.SimpleGameDbData;
import org.pente.gameDatabase.GameStorerSearchRequestData;
import org.pente.gameDatabase.GameStorerSearchRequestFilterData;
import org.pente.gameDatabase.GameStorerSearchResponseData;
import org.pente.gameDatabase.GameStorerSearchResponseMoveData;
import org.pente.gameDatabase.GameStorerSearcher;
import org.pente.gameDatabase.SimpleGameStorerSearchRequestData;
import org.pente.gameDatabase.SimpleGameStorerSearchResponseData;
import org.pente.gameDatabase.swing.component.ListAutoCompleter;
import org.pente.gameDatabase.swing.importer.SGFGameFormat;
import org.pente.gameServer.client.GameOptions;
import org.pente.gameServer.client.GridBoardListener;
import org.pente.gameServer.client.SimpleGameOptions;
import org.pente.gameServer.client.awt.GameBoard;
import org.pente.gameServer.core.AlphaNumericGridCoordinates;
import org.pente.gameServer.core.GridPiece;
import org.pente.gameServer.core.SimpleGridPiece;
import org.pente.mmai.Ai;
import org.pente.mmai.AiListener;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

/**
 * @author dweebo
 */
public class GameReviewBoard extends JPanel implements TabComponent, VenueListener {

	private static final Category log4j = Category.getInstance(
		GameReviewBoard.class.getName());

    /** used to go back/forth in history and check for valid moves */
	//TODO might not be needed
    private GridState localGridState;

	// components
    // custom awt
	private GameBoard gameBoard;

	// swing
	private JTable gameTable;
	private JScrollPane gameScroll;
	private JLabel gameLabel;
	private JButton moreGamesButton;
	private JButton allGamesButton;
	private JButton gamesOptionsButton;

	private JTable resultsTable;
	private TableSorter resultsSorter;

	private GameStorerSearcher gameSearcher;
	private PlunkGameVenueStorer venueStorer;
	private PlunkDbUtil plunkDbUtil;

	// review data
	private GameTableModel gameTableModel;
	private TableSorter gameTableSorter;

	//private PlunkProp lastOpenTrees;
	//private List<Long> lastOpenTreeIds;

	private MoveTreeModel currentTreeModel;
	private JTextPane comments;
	private JPopupMenu treePop;
	private JMenuItem rename, delete, treeProperties, treeDelete;
	private JPopupMenu gamePop;
	private JMenuItem deleteGame, viewGame;
	private JMenu copyGame;
	private JPopupMenu gameOptionsPop;
	private JCheckBoxMenuItem gameOptionsSize[];
	private GameStorerSearchResponseData currentResponse;

	private SearchResultsTableModel searchResultsTableModel;
	private GridPiece highlightPiece;

	private boolean deleted = false;

	// results cache

	private List<Thread> searchThreads = new ArrayList<Thread>();

	//TODO store last used filterdata in plunk_prop
    //store it with a particular movelist
	//that takes care of setting initial game
	private GameStorerSearchRequestFilterData filterData;


	private JComboBox dbChoice;
	private JComboBox gameChoice;
	private JComboBox siteChoice;
	private JComboBox eventChoice;
	private JComboBox roundChoice;
	private JComboBox winnerChoice;

	private JTextField p1;
	private JTextField p2;
	private JComboBox p1Seat;
	private JComboBox p2Seat;

	private PlunkTree plunkTree;
	private JPanel filterPanel;

	//private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

	private Main main;
    private GameOptions gameOptions = new SimpleGameOptions(3);

	private static final NumberFormat nf = NumberFormat.getNumberInstance();

	private AiSettingsPanel aiSettings;
	private AiVisualizationPanel aiVisualization;
	private Ai ai;

	public GameReviewBoard(GameStorerSearcher gameSearcher,
		final PlunkGameVenueStorer venueStorer,
		final PlunkDbUtil plunkDbUtil,
		final PlunkTree plunkTree,
		final Main main,
		final GameStorerSearchRequestFilterData filterData,
		GameData gameData,
		String tabName, List<PlunkTree> trees) {

		this.gameSearcher = gameSearcher;
		this.venueStorer = venueStorer;
		this.plunkDbUtil = plunkDbUtil;
		this.plunkTree = plunkTree;
		this.main = main;
		this.filterData = filterData;

        gameOptions.setPlayerColor(GameOptions.WHITE, 1);
        gameOptions.setPlayerColor(GameOptions.BLACK, 2);
        gameOptions.setPlayerColor(GameOptions.GREEN, 3); // for search moves search moves
        gameOptions.setDraw3DPieces(true);
        gameOptions.setPlaySound(true);
        gameOptions.setShowLastMove(true);
        gameOptions.setDrawDepth(main.showMoveNumber());


        PlunkPenteBoardLW lw = new PlunkPenteBoardLW();
        lw.gridCoordinatesChanged(new AlphaNumericGridCoordinates(19, 19));

        /* test boardline
        BoardLine t = new BoardLine();
        t.setX1(9);
        t.setX2(15);
        t.setY1(9);
        t.setY2(9);
        t.setColor(new Color(1,0,0,.5f));
        lw.addLine(t);

        t = new BoardLine();
        t.setX1(4);
        t.setX2(1);
        t.setY1(4);
        t.setY2(1);
        t.setColor(Color.blue);
        lw.addLine(t);

        t = new BoardLine();
        t.setX1(10);
        t.setX2(10);
        t.setY1(10);
        t.setY2(15);
        t.setColor(Color.green);
        lw.addLine(t);

        t = new BoardLine();
        t.setX1(18);
        t.setX2(0);
        t.setY1(0);
        t.setY2(18);
        t.setColor(Color.magenta);
        lw.addLine(t);
        */

		gameBoard = new GameBoard(lw, gameOptions, true);

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

		gameBoard.addGridBoardListener(new GridBoardListener() {
			public void gridClicked(int x, int y, int button) {

	            if ((button & MouseEvent.BUTTON2_MASK) == MouseEvent.BUTTON2_MASK ||
	            	(button & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {

	            	if (((button & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK) ||
		            	((button & MouseEvent.SHIFT_MASK) == MouseEvent.SHIFT_MASK)) {
		            	currentTreeModel.deleteNode(currentTreeModel.getCurrentTreeNode());
		            }
		            else {
		            	currentTreeModel.prevMove();
		            }
	            	return;
	            }

	            if (gameBoard.getGridState().isGameOver()) return;

				int move = (gameBoard.getGridBoard().getGridHeight() - y - 1) *
                	gameBoard.getGridBoard().getGridWidth() + x;

				addMove(move);
                gameBoard.setCursor(Cursor.DEFAULT_CURSOR);
			}
			public void gridMoved(int x, int y) {
				requestFocus();
                if (gameBoard.getGridState().isGameOver()) {
                    gameBoard.setCursor(Cursor.DEFAULT_CURSOR);
                    return;
                }

                if (x == -1) {
                    gameBoard.setCursor(Cursor.DEFAULT_CURSOR);
                    return;
                }
				int move = (gameBoard.getGridBoard().getGridHeight() - y - 1) *
                	gameBoard.getGridBoard().getGridWidth() + x;

				if (!loadMoveByMove(move)) {
					unloadMove();
				}

                boolean validMove = localGridState.isValidMove(move,
                    localGridState.getCurrentPlayer());
				gameBoard.getGridBoard().setThinkingPieceVisible(validMove);
                if (validMove) {
                    gameBoard.setCursor(Cursor.HAND_CURSOR);
                } else {
                    gameBoard.setCursor(Cursor.DEFAULT_CURSOR);
                }
			}
		});
		gameBoard.setGame(filterData.getGame());

		gameBoard.setCursor(Cursor.HAND_CURSOR);
        localGridState = GridStateFactory.createGridState(filterData.getGame());

		if (filterData.getGame() == GridStateFactory.PENTE ||
			filterData.getGame() == GridStateFactory.POOF_PENTE ||
			filterData.getGame() == GridStateFactory.BOAT_PENTE) {
			((PenteState) gameBoard.getGridState()).setTournamentRule(false);
		}
		if (filterData.getGame() == GridStateFactory.PENTE ||
			filterData.getGame() == GridStateFactory.POOF_PENTE ||
			filterData.getGame() == GridStateFactory.BOAT_PENTE) {
			((PenteState) localGridState).setTournamentRule(false);
		}

		gameBoard.getGridBoard().setThinkingPiecePlayer(
			localGridState.getCurrentPlayer());


		gameTableModel = new GameTableModel();
		gameTableSorter = new TableSorter(gameTableModel);
		gameTable = new JTable(gameTableSorter);
		JTableHeader header = gameTable.getTableHeader();
		header.setReorderingAllowed(true);
		gameTableSorter.setTableHeader(gameTable.getTableHeader());
		gameTableSorter.setSortingStatus(6, TableSorter.ASCENDING);
		gameTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		gameTable.setDefaultRenderer(Integer.class, new GameTableRenderer());




		gamePop = new JPopupMenu();
		gamePop.setBorder(new BevelBorder(BevelBorder.RAISED));

		deleteGame = new JMenuItem(deleteGameAction);

		viewGame = new JMenuItem("Open");
		viewGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int rows[] = gameTable.getSelectedRows();
				for (int row : rows) {
					if (row != -1) {
						PlunkGameData d = gameTableModel.getGame(gameTableSorter.modelIndex(row));
					    openGame(d);
					}
				}
			}
		});

		copyGame = new JMenu("Copy to Db");

		gameTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				checkPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				checkPopup(e);
			}
			private boolean checkPopup(MouseEvent e) {
				if (!e.isPopupTrigger()) {
					return false;
				}
			    JTable target = (JTable)e.getSource();
			    int row = target.rowAtPoint(e.getPoint());
			    int rows[] = target.getSelectedRows();

			    boolean valid = rows.length > 0;
			    boolean sel = false;
			    for (int r : rows) {

			    	if (row == r) {
			    		sel = true;
			    		break;
			    	}
			    }
			    if (!sel) {
					//target.setRowSelectionAllowed(true);
					target.setRowSelectionInterval(row, row);
					valid = true;
			    }

			    if (valid) {

					gamePop.add(viewGame);
					if (filterData.getDb() != 2) {
						gamePop.add(deleteGame);
					}
					gamePop.add(copyGame);

					copyGame.removeAll();
					List<GameDbData> dbs = venueStorer.getDbTree();
					for (GameDbData dbData : dbs) {
						if (dbData.getID() == filterData.getDb()) continue;
						JMenuItem toDb = new JMenuItem(dbData.getName());
						toDb.addActionListener(copyToDbListener);
						copyGame.add(toDb);
					}

					// all this stuff to get y2 shows the popup upwards from
					// the mouse point if the popup would extend out of the
					// scrollpane, not needed though
//					int ph = gamePop.getHeight();
//					int y = e.getY() - gameScroll.getViewport().getViewRect().y;
//					int h = gameScroll.getViewport().getViewRect().height;
//					int y2 = e.getY();
//					if (y + ph > h) {
//						y2 -= ph;
//					}
			    	gamePop.show(gameTable, e.getX(), e.getY());
			    }

				return true;
			}

			public void mouseClicked(MouseEvent e) {
				if (checkPopup(e)) {
					return;
				}
				else if (e.getClickCount() == 2) {
				    JTable target = (JTable)e.getSource();
				    int row = target.getSelectedRow();
				    PlunkGameData d = gameTableModel.getGame(gameTableSorter.modelIndex(row));
				    openGame(d);
				}
			}
		});

		gameTable.getColumnModel().getColumn(0).setPreferredWidth(60);
		gameTable.getColumnModel().getColumn(1).setPreferredWidth(50);
		gameTable.getColumnModel().getColumn(2).setPreferredWidth(60);
		gameTable.getColumnModel().getColumn(3).setPreferredWidth(50);
		gameTable.getColumnModel().getColumn(4).setPreferredWidth(100);
		gameTable.getColumnModel().getColumn(5).setPreferredWidth(120);
		gameTable.getColumnModel().getColumn(6).setPreferredWidth(100);
		gameTable.getColumnModel().getColumn(7).setPreferredWidth(50);
		gameTable.getColumnModel().getColumn(8).setPreferredWidth(50);
		//gameTable.setPreferredSize(new Dimension(515, 500));
		gameTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		gameScroll = new JScrollPane(gameTable);

		moreGamesButton = new FlatButton(new ImageIcon(GameReviewBoard.class.getResource("images/down.png")));
		moreGamesButton.setToolTipText("Show more games");
		moreGamesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadGames(gameTableModel.getRowCount());
			}
		});
		allGamesButton = new FlatButton(new ImageIcon(GameReviewBoard.class.getResource("images/down2.png")));
		allGamesButton.setToolTipText("Show all games");
		allGamesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (currentResponse != null) {
					int total = currentResponse.getGameStorerSearchRequestData().getGameStorerSearchRequestFilterData().getTotalGameNum();
					loadGames(gameTableModel.getRowCount(), total);
				}
			}
		});


		gameOptionsPop = new JPopupMenu();
		gameOptionsPop.setBorder(new BevelBorder(BevelBorder.RAISED));
		gameOptionsSize = new JCheckBoxMenuItem[5];
		gameOptionsSize[0] = new JCheckBoxMenuItem(new FilterSizeAction("10 games", 10));
		gameOptionsSize[1] = new JCheckBoxMenuItem(new FilterSizeAction("25 games", 25));
		gameOptionsSize[2] = new JCheckBoxMenuItem(new FilterSizeAction("50 games", 50));
		gameOptionsSize[3] = new JCheckBoxMenuItem(new FilterSizeAction("100 games", 100));
		gameOptionsSize[4] = new JCheckBoxMenuItem(new FilterSizeAction("250 games", 250));
		for (JCheckBoxMenuItem i : gameOptionsSize) {
			gameOptionsPop.add(i);
			FilterSizeAction f = (FilterSizeAction) i.getAction();
			if (f.num == main.getFilterNumGames()) {
				i.setSelected(true);
			}
		}

		gamesOptionsButton = new FlatButton(new ImageIcon(GameReviewBoard.class.getResource("images/options.png")));
		gamesOptionsButton.setToolTipText("Options");
		gamesOptionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Point p = gamesOptionsButton.getLocation();
				gameOptionsPop.show(gamesOptionsButton, gamesOptionsButton.getWidth(), (int) p.getY());
			}
		});

		gameScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		gameScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		gameScroll.getViewport().setBackground(Color.white);


		gameLabel = new JLabel("");

		JPanel topGamePanel = new JPanel();
		topGamePanel.setLayout(new BorderLayout(0,0));
		topGamePanel.add(gameLabel, BorderLayout.WEST);
		JPanel topButtonPanel = new JPanel();
		topButtonPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
		topButtonPanel.add(moreGamesButton);
		topButtonPanel.add(allGamesButton);
		topButtonPanel.add(gamesOptionsButton);
		topGamePanel.add(topButtonPanel, BorderLayout.EAST);

		JPanel gamePanel = new JPanel();
		gamePanel.setLayout(new BorderLayout(0,0));
		gamePanel.add(topGamePanel, BorderLayout.NORTH);
		gamePanel.add(gameScroll, BorderLayout.CENTER);

		searchResultsTableModel = new SearchResultsTableModel();
		resultsSorter = new TableSorter(searchResultsTableModel);
		resultsTable = new JTable(resultsSorter);
		resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultsSorter.setTableHeader(resultsTable.getTableHeader());
		resultsSorter.setSortingStatus(1, TableSorter.ASCENDING);

		resultsTable.addMouseMotionListener(new MouseMotionAdapter() {
		    public void mouseMoved(MouseEvent e) {
		        Point origin = e.getPoint();
		        int row = resultsTable.rowAtPoint(origin);
		        if (row == -1) {
		          	return;
		        }
		        else {
					loadMoveByRow(row);
		        }
		    }
		});

		resultsTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
			    JTable target = (JTable)e.getSource();
			    int row = target.getSelectedRow();
				int move = searchResultsTableModel.getMove(
					resultsSorter.modelIndex(row));
				if (move == -1) return; // total row

				addMove(move);
			}
			public void mouseExited(MouseEvent e) {
				unloadMove();
			}
		});

		ListSelectionModel resultsLSM = resultsTable.getSelectionModel();
		resultsLSM.addListSelectionListener(new ListSelectionListener() {
		    public void valueChanged(ListSelectionEvent e) {
		        //Ignore extra messages.
		        if (e.getValueIsAdjusting()) return;

		        ListSelectionModel lsm =
		            (ListSelectionModel)e.getSource();
		        if (lsm.isSelectionEmpty()) {
		            //no rows are selected
		        } else {
		            int row = lsm.getMinSelectionIndex();
					loadMoveByRow(row);
		        }
		    }
		});

		treePop = new JPopupMenu();

		rename = new JMenuItem("Edit");
		rename.setIcon(new ImageIcon(GameReviewBoard.class.getResource("images/pencil.png")));
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
		treePop.add(rename);


		delete = new JMenuItem("Delete");
		delete.setIcon(new ImageIcon(GameReviewBoard.class.getResource("images/cross.png")));
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode d = (DefaultMutableTreeNode) currentTreeModel.getTempSelectedPath().getLastPathComponent();
				currentTreeModel.deleteNode(d);
			}
		});

		treeProperties = new JMenuItem("Edit");
		treeProperties.setIcon(new ImageIcon(GameReviewBoard.class.getResource("images/pencil.png")));
		treeProperties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int r = showTreeProperties("Game Analysis Properties", currentTreeModel.getPlunkTree());
				if (r == JOptionPane.OK_OPTION && currentTreeModel.getPlunkTree().canEditProps()) {
					main.renameTab(GameReviewBoard.this, currentTreeModel.getPlunkTree().getName());
				}
			}
		});

		treeDelete = new JMenuItem(deleteAnalysisAction);


		treePop.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent arg0) {
				//System.out.println("canceled");
				currentTreeModel.tempUnselectNode();
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				//System.out.println("will be become invisible");
				currentTreeModel.tempUnselectNode();
			};
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				//System.out.println("will be become visible");
			}
		});
		treePop.setBorder(new BevelBorder(BevelBorder.RAISED));

		if (filterData.getGame() == GridStateFactory.PENTE ||
			filterData.getGame() == GridStateFactory.KERYO) {
			ai = new Ai();
			ai.setGame(filterData.getGame());
			aiVisualization = new AiVisualizationPanel(ai, filterData.getGame(), gameOptions);
			aiSettings = new AiSettingsPanel(ai, trees, false, aiVisualization, "Run AI", "Stop AI");

			ai.addAiListener(aiVisualization);
			ai.addAiListener(aiSettings);
			ai.addAiListener(new AiListener() {
				public void startThinking() {
				}
				public void stopThinking() {
					aiVisualization.reset();
				}
				public void moveReady(int[] moves, int newMove) {

					addMove(newMove);

					try {
						InputStream in = new FileInputStream("yourturn.au");
						AudioStream as = new AudioStream(in);
						AudioPlayer.player.start(as);
					} catch (Exception e) { e.printStackTrace(); }

				}
				public void aiEvaluateCallBack() {
					if (aiVisualization.getAiCount() % 2000 == 0) Thread.yield();//allow other parts of program to work (or other programs)
				}
				public void aiVisualizationCallBack(int[] bd) {
				}
			});

			aiSettings.addStartActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getActionCommand().equals("Run AI")) {
						ai.setSeat(GameReviewBoard.this.localGridState.getCurrentPlayer());
						ai.setActive(true);
						aiMove();
					}
				}
			});
		}


		//TOOD redo this
//		what features does it need?
        //start with simple text
        comments = new JTextPane();
        comments.setMinimumSize(new Dimension(150, 30));
        comments.setPreferredSize(new Dimension(150, 40));
        JScrollPane commentsScroll = new JScrollPane(comments);
        commentsScroll.setMinimumSize(new Dimension(150, 30));
        commentsScroll.setPreferredSize(new Dimension(150, 40));


		JScrollPane resultsScroll = new JScrollPane(resultsTable);
		resultsScroll.setMinimumSize(new Dimension(150, 50));
		resultsScroll.setPreferredSize(new Dimension(150, 250));
		resultsScroll.getViewport().setBackground(Color.white);

		createFilterPanel();
		JScrollPane filterScroll = new JScrollPane(filterPanel);
		filterScroll.setMinimumSize(new Dimension(150, 50));
		filterScroll.setPreferredSize(new Dimension(150, 250));

		final MoveTreeModel tm = createNewTreeModel();

		currentTreeModel = tm;

		final JScrollPane treeScroll = new JScrollPane(tm.getJTree());
		treeScroll.getViewport().setMinimumSize(new Dimension(150, 50));
		treeScroll.getViewport().setPreferredSize(new Dimension(150, 250));



		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		int y = 1;



		gbc.gridx = 1;
		gbc.gridy = y++;
		gbc.gridheight = 1;
		gbc.weighty = 60;
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
		gbc.weighty = 30;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		leftPanel.add(commentsScroll, gbc);


		JTabbedPane tabbed = new JTabbedPane();
		tabbed.addTab("Results", resultsScroll);
		tabbed.addTab("Games", gamePanel);
		tabbed.addTab("Filter", filterScroll);
		if (ai != null) {
			tabbed.addTab("AI", aiSettings);
		}
		tabbed.setPreferredSize(resultsScroll.getPreferredSize());
		gbc.gridx = 1;
		gbc.gridy = y++;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.weighty = 80;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTH;
		leftPanel.add(tabbed, gbc);





		JSplitPane topPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
			leftPanel, gameBoard);
		topPane.setContinuousLayout(true);
		//splitPane.setOneTouchExpandable(true);
		topPane.setResizeWeight(0.1);




		//TODO move up
        //TODO have status bar show K10,L9 coordinate when moused over

		//TODO can we somehow show the interface before searching?

		// import game and want to load it into game review board
		if (gameData != null) {
			this.plunkTree = new PlunkTree();
			this.plunkTree.setName(tabName);
			this.plunkTree.setCanEditProps(true);
			tm.clear();
			tm.setPlunkTree(this.plunkTree);
			PlunkNode r = Utilities.convertGame(gameData);
			tm.addMoves(r, true);
			tm.visitNode(r.getHash());
		}
		// new game search
		else if (plunkTree == null) {
			tm.newPlunkTree();
			this.plunkTree = tm.getPlunkTree();
		}
		// import game search
		else if (plunkTree != null && !plunkTree.isStored()) {
			tm.clear();
			tm.setPlunkTree(this.plunkTree);
			tm.addMoves(plunkTree.getRoot(), true);
			tm.visitNode(plunkTree.getRoot().getHash());
		}
		// load existing game search from db
		else {
			loadMoveDb(plunkTree.getTreeId(), tm);
		}
/*
		try {
			lastOpenTrees = plunkDbUtil.loadProp("last_open_tree_ids");
			trees = plunkDbUtil.loadPlunkTrees();

		} catch (Exception e) {
			e.printStackTrace();
		}
		if (lastOpenTrees != null) {
            lastOpenTreeIds = (ArrayList<Long>) lastOpenTrees.getValue();
		}
        if (lastOpenTrees == null || lastOpenTreeIds.isEmpty()) {
            lastOpenTreeIds = new ArrayList<Long>();
        	lastOpenTrees = new PlunkProp("last_open_tree_ids", lastOpenTreeIds);
        	newMoveDbAction.newMoveDb();
        }
        else {
        	for (Long treeId : lastOpenTreeIds) {
        		addTreeModel(treeId);
        	}
        }
        // this loads move database and that triggers the game board to
        // load at 1st move and triggers results to display
        //loadMoveDb(lastOpenTreeId);
        //addTreeModel(lastOpenTreeId);

         */

		setLayout(new BorderLayout());
		add("Center", topPane);
		/*
		treeTab.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JScrollPane s = (JScrollPane) treeTab.getSelectedComponent();
				// if all trees are closed create a new one
				if (s == null) {
					newMoveDbAction.newMoveDb();
					return;
				}
				JViewport v = (JViewport) s.getComponent(0);
				JTree t = (JTree) v.getComponent(0);

				// just in case this gets done before popup closes
				currentTreeModel.tempUnselectNode();

				for (MoveTreeModel m : treeModels) {
					if (m.getJTree() == t) {
						currentTreeModel = m;
						currentTreeModel.refreshBoard();
						break;
					}
				}
			}
		});
		*/

		currentTreeModel.getJTree().requestFocusInWindow();
	}
	/*
	public void setGame(int game) {

		gameBoard.setGame(game);
        localGridState = GridStateFactory.createGridState(game);
        boolean found = false;
        for (int i = 0; i < gameChoice.getItemCount(); i++) {
        	if (gameChoice.getItemAt(i).equals(GridStateFactory.getGameName(game))) {
        		gameChoice.setSelectedIndex(i);
        		found = true;
        		break;
        	}
        }
        if (!found) {
        	gameChoice.addItem(GridStateFactory.getGameName(game));
        	gameChoice.setSelectedIndex(gameChoice.getItemCount() - 1);
        }
	}*/

	private void aiMove() {

        if (!localGridState.isGameOver() &&
        	ai != null &&
        	ai.isActive() &&
        	ai.getSeat() == localGridState.getCurrentPlayer()) {

        	// this is opening book here
        	// would be better to have this as part of the ai....
        	if (ai.getTreeId() != -1) {
	        	try {
		        	PlunkNode r = plunkDbUtil.loadSubTree(ai.getTreeId(),
		        		localGridState.getHash());
		        	if (r != null && r.getChildCount() == 0) {
		        		System.err.println("bad ai opening book position " + ai.getTreeId() + ","+gameBoard.getGridState().getHash());
		        	}
		        	else if (r != null) {
		        		int m = r.getBestMove().getMove();
		        		int rm = localGridState.rotateMoveToLocalRotation(m, r.getRotation());
		        		aiVisualization.setText("Status: Move from opening book");
		        		addMove(rm);
		        		return;
		        	}

	        	} catch (SQLException s) {
	        		System.err.println("Error loading opening book moves for " + localGridState.getHash());
	        		s.printStackTrace();
	        	}
        	}

			//List<PlunkNode> nodes = Utilities.getAllNodes(currentTreeModel.getPlunkRoot());
			//int moves[] = new int[nodes.size()];
			//for (int i = 0; i < moves.length; i++) moves[i] = nodes.get(i).getMove();

			ai.getMove(localGridState.getMoves());
        }
	}

	Action deleteGameAction = new AbstractAction("Delete",
		new ImageIcon(GameReviewBoard.class.getResource("images/cross.png"))) {
		public void actionPerformed(ActionEvent e) {
			int rows[] = gameTable.getSelectedRows();
			final List<GameData> games = new ArrayList<GameData>(rows.length);
			for (int row : rows) {
				if (row != -1) {
					GameData d = gameTableModel.getGame(gameTableSorter.modelIndex(row));
					games.add(d);
				}
			}
			final ProgressMonitor pm = new ProgressMonitor(null, "Deleting Games",
				"Initializing...", 0, games.size());

			Thread t = new Thread(new Runnable() {
				public void run() {
					for (int i = 0; i < games.size(); i++) {

						if (pm.isCanceled()) {
							pm.close();
							return;
						}

						final GameData d = games.get(i);
						pm.setNote(Utilities.getGameName(d));

						long gid = d.getGameID();
					    int dbid = filterData.getDb();
					    try {
					    	plunkDbUtil.deleteGame(gid, dbid);
					    	//TODO what if game is open in tab?

							pm.setProgress(i + 1);

							javax.swing.SwingUtilities.invokeLater(new Runnable() {
								public void run() {
							    	gameTableModel.deleteGame(d);
							    	main.getSearchCache().deleteGame(d);
									updateGameControls(currentResponse);
								}
							});

					    } catch (SQLException s) {
					    	System.err.println("problem deleting game " + gid + ":" + dbid);
					    	s.printStackTrace();
					    }
					}
					pm.close();
				};
			});
			t.start();
		}
	};

	ActionListener copyToDbListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			//System.out.println(e.getActionCommand());
			GameDbData data = null;
			for (GameDbData dbData : venueStorer.getDbTree()) {
				if (dbData.getName().equals(e.getActionCommand())) {
					data = dbData;
					break;
				}
			}
			if (data != null) {
				int gms[] = gameTable.getSelectedRows();
				final ProgressMonitor pm = new ProgressMonitor(null, "Copying Games",
					"Initializing...", 0, gms.length);

				final List<PlunkGameData> games = new ArrayList<PlunkGameData>(gms.length);
				for (int gameRow : gms) {
					games.add(gameTableModel.getGame(gameRow));
				}
				final GameDbData dbData = data;
				Thread t = new Thread(new Runnable() {
					public void run() {
						for (int i = 0; i < games.size(); i++) {

							if (pm.isCanceled()) {
								pm.close();
								return;
							}

							PlunkGameData orig = games.get(i);
							pm.setNote(Utilities.getGameName(orig));

							try {
								if (orig.getNumMoves() == 0) { // not loaded
									PlunkNode movesRoot = plunkDbUtil.loadMoves(orig.getGameID());
									orig.setRoot(movesRoot);
								}
								else if (orig.getRoot() == null) {
									orig.setRoot(Utilities.convertGame(orig));
								}

								PlunkGameData d = new PlunkGameData(orig);
								//d.setRoot(Utilities.convertGame(d));
								d.setGameID(0);
								d.setEditable(true);

								if (!main.getGameStorer().gameAlreadyStored(d, dbData.getID())) {
									main.saveGame(d, dbData, null);
								}

								pm.setProgress(i + 1);

							} catch (Exception ex) {
								System.err.println("Error copying " + orig.getGameID());
								ex.printStackTrace();
							}
						}
						pm.close();
					};
				});
				t.start();
			}
		};
	};

	class FilterSizeAction extends AbstractAction {
		int num = 0;
		public FilterSizeAction(String text, int num) {
			super(text);
			this.num = num;
		}
		public void actionPerformed(ActionEvent e) {
			main.setFilterNumGames(num);
			for (JCheckBoxMenuItem c : gameOptionsSize) {
				FilterSizeAction f = (FilterSizeAction) c.getAction();
				c.setSelected(f.num == num);
			}

//			if (currentResponse.getGames().size() < num) {
//				loadGames(0);//TODO results in + num instead of total num
//			}
		}
	}

    public void addEditListener(TabComponentEditListener l) {
        currentTreeModel.addEditListener(l);
    }

    //TODO since always using PlunkGameData now, should change code
    //to always have root populated, and therefore might not have
    //to load moves as below
    // - games from p.org might not have movesRoot yet so will have
    //   to look them up in db
    // - local games might also not yet be loaded
    // for both try looking in gamecache first
    private void openGame(PlunkGameData d) {
    	 try {
	    	// try to load moves from local db first, either this is
	    	// a local game, or it is a pente.org game that has had
	    	// comments saved
		    PlunkNode movesRoot = plunkDbUtil.loadMoves(d.getGameID());

		    // no moves found then it is a remote search game so
		    // create plunknodes from the game data
		    if (movesRoot == null) {
		    	movesRoot = Utilities.convertGame(d);
		    }
		    String gameName = Utilities.getGameName(d);
		    ViewGamePanel p = new ViewGamePanel(main, plunkDbUtil,
		    	venueStorer, main.getGameStorer(), d, gameOptions,
		    	localGridState.getNumMoves(), movesRoot, gameName,
		    	filterData.getDb(), false, false, null);

		    main.addTab(p, gameName);
    		main.addVenueListener(p);

		    } catch (SQLException s) {
		    	s.printStackTrace();
		    }
    }

	public int save() {

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		List<PlunkNode> nodes = currentTreeModel.getDirtyNodes();
		try {

			PlunkTree currentTree = currentTreeModel.getPlunkTree();

			boolean stored = currentTree.isStored();
			if (!stored) {
				//prompt for name,creator,version
				int r = showTreeProperties("New Game Analysis Properties", currentTree);
				if (r == JOptionPane.CANCEL_OPTION) return r;

				// so appears in open dialog
				main.addPlunkTree(currentTree);
			}
			else {
				// store the tree just to update the last mod date
				plunkDbUtil.storePlunkTree(currentTree);
			}
			plunkDbUtil.savePlunkNodes(nodes, currentTree.getTreeId());
			currentTreeModel.clearDirtyNodes();

			if (!stored) {
				//lastOpenTreeIds.add(currentTree.getTreeId());
				//plunkDbUtil.storeProp(lastOpenTrees);
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(GameReviewBoard.this,
				"Error saving", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} finally {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		return JOptionPane.NO_OPTION;
	}




	public void search() {

		clearResults();

		// this clears the results from board
		currentTreeModel.refreshBoard();

//		if (main.isSearching()) {
//			if (!loadCachedResults()) {
//				loadGames(0);
//			}
//		}
	}
	public void nextMoves() {
		currentTreeModel.refreshBoard();
	}
	public void numberMoves() {
		gameOptions.setDrawDepth(main.showMoveNumber());
		gameBoard.gameOptionsChanged(gameOptions);
	}
	public void threats() {
		currentTreeModel.refreshBoard();
	}


	public void showTabPopup(JComponent component, int x, int y) {

		treePop.removeAll();
		treePop.add(treeProperties);
		treePop.add(treeDelete);

		treePop.show(component, x, y);
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
//			if (d.getUserObject() instanceof String) {
//				treePop.removeAll();
//				treePop.add(treeProperties);
//				treePop.add(treeDelete);
//			}
//			else {
				treePop.removeAll();
				treePop.add(rename);
				if (n.getDepth() > 0) { // don't allow deleting K10
					treePop.add(delete);
				}
//			}

			currentTreeModel.tempSelectNode(p);

			treePop.show(currentTreeModel.getJTree(), e.getX(), e.getY());
		}
	};

	private int showTreeProperties(String title, PlunkTree tree) {
		//TODO better icon in dialog
		JPanel propPanel = new JPanel();
		propPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.NONE;

		propPanel.add(new JLabel("Game Analysis Name: "), gbc);
		gbc.gridy++;
		propPanel.add(new JLabel("Version: "), gbc);
		gbc.gridy++;
		propPanel.add(new JLabel("Creator Name: "), gbc);
		if (tree.isStored()) {
			gbc.gridy++;
			propPanel.add(new JLabel("Date Modified: "), gbc);
			gbc.gridy++;
			propPanel.add(new JLabel("Date Created: "), gbc);
		}
		gbc.gridy++;
		propPanel.add(new JLabel("Number of Moves: "), gbc);

		JTextField name = null;
		JTextField version = null;
		JTextField creator = null;

		gbc.gridy = 1;
		gbc.gridx++;
		if (tree.canEditProps()) {
			name = new JTextField(tree.getName(), 15);
			propPanel.add(name, gbc);

			String v = tree.getVersion();
			if (v == null) v = "1.0";
			version = new JTextField(v, 15);
			gbc.gridy++;
			propPanel.add(version, gbc);

			String c = tree.getCreator();
			if (c == null) c = "";
			creator = new JTextField(c, 15);
			gbc.gridy++;
			propPanel.add(creator, gbc);
		}
		else {
			propPanel.add(new JLabel(tree.getName()), gbc);
			gbc.gridy++;
			propPanel.add(new JLabel(tree.getVersion()), gbc);
			gbc.gridy++;
			propPanel.add(new JLabel(tree.getCreator()), gbc);
		}

		if (tree.isStored()) {
			DateFormat df =
				new SimpleDateFormat("MM/dd/yyyy HH:mm");

			gbc.gridy++;
			propPanel.add(new JLabel(df.format(tree.getLastModified())), gbc);
			gbc.gridy++;
			propPanel.add(new JLabel(df.format(tree.getCreated())), gbc);
		}
		gbc.gridy++;
		propPanel.add(new JLabel(Integer.toString(currentTreeModel.getNodeCount())), gbc);


		int r = JOptionPane.showConfirmDialog(this, propPanel,
			title, tree.canEditProps() ? JOptionPane.OK_CANCEL_OPTION : JOptionPane.DEFAULT_OPTION);

		if (r == JOptionPane.OK_OPTION && tree.canEditProps()) {
			tree.setName(name.getText());
			tree.setVersion(version.getText());
			tree.setCreator(creator.getText());

			try {
				plunkDbUtil.storePlunkTree(tree);
			} catch (SQLException s) {
				s.printStackTrace();
				//TODO error pane
			}
		}

		return r;
	}

	public void destroy() {
		if (gameBoard != null) {
			gameBoard.destroy();
		}
		for (Thread t : searchThreads) {
			if (t.isAlive()) {
				t.interrupt();
				t.stop();
				//TODO should i do something better here, like make sure
				//my search thread knows how to be interrupted or destroyed
			}
		}
		if (ai != null) {
			ai.destroy();
		}
	}

	private boolean readyForSearch = false;
	private void createFilterPanel() {

		dbChoice = new JComboBox();
		gameChoice = new JComboBox();
		siteChoice = new JComboBox();
		eventChoice = new JComboBox();
		roundChoice = new JComboBox();



		p1 = new JTextField("", 8);
		new ListAutoCompleter(p1, main.getPlayerNames(), true);
		p1.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				filterData.setPlayer1Name(p1.getText());
				if (currentTreeModel != null) {
					currentTreeModel.refreshBoard();
				}
			}
		});
		p1.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				int iKey = evt.getKeyCode();
				if (iKey == KeyEvent.VK_ENTER) {
					System.out.println("enter");
					filterData.setPlayer1Name(p1.getText());
					if (currentTreeModel != null) {
						currentTreeModel.refreshBoard();
					}
				}
			}
		});


		p1Seat = new JComboBox();
		p1Seat.addItem("All");
		p1Seat.addItem("P1");
		p1Seat.addItem("P2");
		p1Seat.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					return;
				}
				filterData.setPlayer1Seat(p1Seat.getSelectedIndex());
				if (currentTreeModel != null) {
					currentTreeModel.refreshBoard();
				}
			}
		});
		filterData.setPlayer1Seat(p1Seat.getSelectedIndex());

		p2 = new JTextField("", 8);
		new ListAutoCompleter(p2, main.getPlayerNames(), true);
		p2.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				filterData.setPlayer2Name(p2.getText());
				if (currentTreeModel != null) {
					currentTreeModel.refreshBoard();
				}
			}
		});
		p2.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				int iKey = evt.getKeyCode();
				if (iKey == KeyEvent.VK_ENTER) {
					filterData.setPlayer2Name(p2.getText());
					if (currentTreeModel != null) {
						currentTreeModel.refreshBoard();
					}
				}
			}
		});
		p2Seat = new JComboBox();
		p2Seat.addItem("All");
		p2Seat.addItem("P1");
		p2Seat.addItem("P2");
		p2Seat.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					return;
				}
				filterData.setPlayer2Seat(p2Seat.getSelectedIndex());
				if (currentTreeModel != null) {
					currentTreeModel.refreshBoard();
				}
			}
		});
		filterData.setPlayer2Seat(p2Seat.getSelectedIndex());

		winnerChoice = new JComboBox();
		winnerChoice.addItem("");
		winnerChoice.addItem("Player 1");
		winnerChoice.addItem("Player 2");
		winnerChoice.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					return;
				}
				filterData.setWinner(winnerChoice.getSelectedIndex());
				if (currentTreeModel != null) {
					currentTreeModel.refreshBoard();
				}
			}
		});

		//loads all venue drop-downs
		populateDbCombo();

		filterPanel = new JPanel();

		GridBagConstraints gbc = new GridBagConstraints();
		filterPanel.setLayout(new GridBagLayout());
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weighty = 1;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		filterPanel.add(new JLabel("Database: "), gbc);
		gbc.gridy++;
		filterPanel.add(new JLabel("Game: "), gbc);
		gbc.gridy++;
		filterPanel.add(new JLabel("Site:"), gbc);
		gbc.gridy++;
		filterPanel.add(new JLabel("Event:"), gbc);
		gbc.gridy++;
		filterPanel.add(new JLabel("Round:"), gbc);
		gbc.gridy++;
		filterPanel.add(new JLabel("Player:"), gbc);
		gbc.gridy++;
		filterPanel.add(new JLabel("Player:"), gbc);

		gbc.gridx = 2;
		gbc.weightx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		filterPanel.add(dbChoice, gbc);
		gbc.gridy++;
		filterPanel.add(gameChoice, gbc);
		gbc.gridy++;
		filterPanel.add(siteChoice, gbc);
		gbc.gridy++;
		filterPanel.add(eventChoice, gbc);
		gbc.gridy++;
		filterPanel.add(roundChoice, gbc);

		gbc.gridy++;
		gbc.gridwidth = 1;
		filterPanel.add(p1, gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.NONE;
		filterPanel.add(new JLabel("Seat:"), gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		filterPanel.add(p1Seat, gbc);

		gbc.gridy++;
		gbc.gridx = 2;
		filterPanel.add(p2, gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.NONE;
		filterPanel.add(new JLabel("Seat:"), gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		filterPanel.add(p2Seat, gbc);

		gbc.gridy++;
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		filterPanel.add(new JLabel("Winner: "), gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		filterPanel.add(winnerChoice, gbc);

		readyForSearch = true;
	}

	private void populateDbCombo() {

		dbChoice.removeItemListener(dbListener);
		dbChoice.removeAllItems();

		boolean foundPenteOrg = false;
		List<GameDbData> dbData = venueStorer.getDbTree();
		for (GameDbData gdd : dbData) {
			dbChoice.addItem(gdd);
			if (gdd.getID() == filterData.getDb()) {
				dbChoice.setSelectedItem(gdd);
			}
			if (gdd.getID() == 2) {
				foundPenteOrg = true;
			}
		}

		if (!foundPenteOrg) {
			//add this one manually, upon selection connect to pente.org to get
			GameDbData penteOrg = new SimpleGameDbData();
			penteOrg.setID(2);
			penteOrg.setName("Pente.org Online");
			dbChoice.insertItemAt(penteOrg, 1);
		}

		populateGameCombo();

		dbChoice.addItemListener(dbListener);
	}


	private boolean penteOrgVenuesLoaded = false;
	private GameDbData deSelected = null;
	ItemListener dbListener = new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				deSelected = (GameDbData) e.getItem();
				return;
			}

			if (e.getItem() instanceof GameDbData) {
				GameDbData gdd = (GameDbData) dbChoice.getSelectedItem();
				filterData.setDb(gdd.getID());
			}

			if (filterData.getDb() == 2 && !penteOrgVenuesLoaded) {
				dbChoice.setEnabled(false);
				gameChoice.setEnabled(false);
				siteChoice.setEnabled(false);
				eventChoice.setEnabled(false);
				roundChoice.setEnabled(false);

				loadPenteOrgVenue(GameReviewBoard.this);
				return;
			}

			populateGameCombo();
		}
	};
	ItemListener gameListener = new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) return;
			updateGame();
			populateSiteCombo();
		}
	};
	ItemListener siteListener = new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) return;
			updateSite();
			populateEventCombo();
		}
	};
	ItemListener eventListener = new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) return;

			updateEvent();
			populateRoundCombo();
		}
	};
	ItemListener roundListener = new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) return;

			updateRound();
		}
	};

	private void populateGameCombo() {

		gameChoice.removeItemListener(gameListener);

		gameChoice.removeAllItems();

		for (Game g : GridStateFactory.getNormalGames()) {
			gameChoice.addItem(g.getName());
			if (filterData.getGame() == g.getId()) {
				gameChoice.setSelectedItem(g.getName());
			}
		}

		updateGame();

		populateSiteCombo();

		gameChoice.addItemListener(gameListener);
	}

	private void updateGame() {
		int game = GridStateFactory.getGameId((String) gameChoice.getSelectedItem());

		if (game != filterData.getGame()) {
			filterData.setGame(game);

			// TODO should board be reset?  maybe not since many game variants are
			// similar and player might be just checking same position
			// but for connect6 everything is way different, prompt user maybe?
			// also ask if they want to switch move trees?
			gameBoard.setGame(game);

			if (currentTreeModel != null) {
		        currentTreeModel.setGame(game);
		        //TODO might need to call nodeschanged on all nodes
		        currentTreeModel.getJTree().repaint();
			}
			GridState newState = GridStateFactory.createGridState(game, localGridState);
			localGridState = newState;

			if (game == GridStateFactory.PENTE ||
				game == GridStateFactory.POOF_PENTE ||
				game == GridStateFactory.BOAT_PENTE) {
				((PenteState) localGridState).setTournamentRule(false);
			}

		}
	}

	private void populateSiteCombo() {

		siteChoice.removeItemListener(siteListener);

		siteChoice.removeAllItems();
		siteChoice.insertItemAt("All", 0);
		siteChoice.setSelectedIndex(0);

		List<GameDbData> dbData = venueStorer.getDbTree();
		outer: for (GameDbData gdd : dbData) {
			GameDbData gdd2 = (GameDbData) dbChoice.getSelectedItem();
			if (gdd.getID() == gdd2.getID()) {
				for (GameTreeData td : gdd.getGameTreeData()) {
					if (td.getName().equals(gameChoice.getSelectedItem())) {
						for (GameSiteData sd : td.getGameSiteData()) {
							siteChoice.addItem(sd.getName());
							if (sd.getName().equals(filterData.getSite())) {
								siteChoice.setSelectedItem(sd.getName());
							}
						}
						break outer;
					}
				}
			}
		}
		updateSite();

		populateEventCombo();

		siteChoice.addItemListener(siteListener);
	}

	private void updateSite() {
		String s = (String) siteChoice.getSelectedItem();
		if (!s.equals("All")) {
			filterData.setSite(s);
		} else {
			filterData.setSite(null);
		}
	}

	private void populateEventCombo() {

		eventChoice.removeItemListener(eventListener);

		eventChoice.removeAllItems();
		eventChoice.insertItemAt("All", 0);
		eventChoice.setSelectedIndex(0);

		boolean empty = true;
		List<GameDbData> dbData = venueStorer.getDbTree();
		outer: for (GameDbData gdd : dbData) {
			GameDbData gdd2 = (GameDbData) dbChoice.getSelectedItem();
			if (gdd.getID() == gdd2.getID()) {
				for (GameTreeData td : gdd.getGameTreeData()) {
					if (td.getName().equals(gameChoice.getSelectedItem())) {
						for (GameSiteData sd : td.getGameSiteData()) {
							if (sd.getName().equals(siteChoice.getSelectedItem())) {
								empty = false;
								for (GameEventData ed : (List<GameEventData>) sd.getGameEventData()) {
									eventChoice.addItem(ed);
									if (ed.getName().equals(filterData.getEvent())) {
										eventChoice.setSelectedItem(ed);
									}
								}
								break outer;
							}
						}
					}
				}
			}
		}

		updateEvent();

		populateRoundCombo();

		eventChoice.addItemListener(eventListener);
	}


	private void updateEvent() {

		Object o = eventChoice.getSelectedItem();
		if (o instanceof GameEventData) {
			GameEventData ed = (GameEventData) o;
			filterData.setEvent(ed.getName());
		} else {
			filterData.setEvent(null);
		}
	}

	private void populateRoundCombo() {

		roundChoice.removeItemListener(roundListener);

		roundChoice.removeAllItems();
		roundChoice.insertItemAt("All", 0);
		roundChoice.setSelectedIndex(0);

		List<GameDbData> dbData = venueStorer.getDbTree();
		outer: for (GameDbData gdd : dbData) {
			GameDbData gdd2 = (GameDbData) dbChoice.getSelectedItem();
			if (gdd.getID() == gdd2.getID()) {
				for (GameTreeData td : gdd.getGameTreeData()) {
					if (td.getName().equals(gameChoice.getSelectedItem())) {
						for (GameSiteData sd : td.getGameSiteData()) {
							if (sd.getName().equals(siteChoice.getSelectedItem())) {
								if (eventChoice.getSelectedItem() instanceof String) break outer;//All selected
								GameEventData ed2 = (GameEventData) eventChoice.getSelectedItem();
								for (GameEventData ed : (List<GameEventData>) sd.getGameEventData()) {
									if (ed.getEventID() == ed2.getEventID()) {
										for (Iterator it = ed.getGameRoundData().iterator(); it.hasNext();) {
											GameRoundData rd = (GameRoundData) it.next();

											roundChoice.addItem(rd.getName());
											if (rd.getName().equals(filterData.getRound())) {
												roundChoice.setSelectedItem(rd.getName());
											}
										}
										break outer;
									}
								}
							}
						}
					}
				}
			}
		}

		updateRound();

		roundChoice.addItemListener(roundListener);
	}

	private void updateRound() {

		Object o = roundChoice.getSelectedItem();
		if (o instanceof String) {
			String round = (String) o;
			if (!round.equals("All")) {
				filterData.setRound(round);
			}
			else {
				filterData.setRound(null);
			}
		} else {
			filterData.setRound(null);
		}
		//if (readyForSearch) {
//			clearResults(); // clear results but if not auto-search
//			// then don't load new results
//			if (main.isSearching()) {
//				if (!loadCachedResults()) {
//					loadGames(0);
//				}
//			}
		//}
		if (currentTreeModel != null && readyForSearch) {
			currentTreeModel.refreshBoard();
		}
	}

	//TODO add section filtering

	private void addMove(int move) {
		if (localGridState.isValidMove(move, localGridState.getCurrentPlayer())) {
			currentTreeModel.addMove(move);
		}
	}
	private boolean loadMoveByMove(int move) {
		int row = searchResultsTableModel.getRow(move);
		if (row == -1) return false; //not a result
		row = resultsSorter.viewIndex(row);

		loadMove(row, move, true);

		return true;
	}
	private void loadMoveByRow(int row) {

		int move = searchResultsTableModel.getMove(
			resultsSorter.modelIndex(row));
		if (move == -1) return; // total row
//System.out.println("loadMovesByRow " + row + ":" + move);
		loadMove(row, move, false);
	}
	private void loadMove(int row, int move, boolean scroll) {
		resultsTable.setRowSelectionAllowed(true);
		resultsTable.setRowSelectionInterval(row, row);

		if (scroll) {
			// TODO improvement on this would be to keep total row static
			// but don't see simple way of doing that right now
			resultsTable.scrollRectToVisible(resultsTable.getCellRect(0, 0, true));
			Rectangle bounds = resultsTable.getVisibleRect();
			Rectangle selectedBounds = resultsTable.getCellRect(row, 0, true);
			if (!bounds.contains(selectedBounds)) {
				resultsTable.scrollRectToVisible(selectedBounds);
			}
		}

		if (highlightPiece != null) {
			//System.out.println("hl " + highlightPiece.getX() + "," + highlightPiece.getY());
			gameBoard.updatePiecePlayer(highlightPiece.getX(),
				highlightPiece.getY(), 3);
		}

		Coord p = gameBoard.getGridState().convertMove(move);
    	gameBoard.updatePiecePlayer(p.x, 18 - p.y, localGridState.getCurrentPlayer());

    	highlightPiece = new SimpleGridPiece(p.x, 18 - p.y, 3);

	}
	private void unloadMove() {
		if (highlightPiece != null) {
			gameBoard.updatePiecePlayer(highlightPiece.getX(),
				highlightPiece.getY(), 3);
		}

		resultsTable.setRowSelectionAllowed(false);
	}

	private void clearResults() {

		highlightPiece = null;
		searchResultsTableModel.clearResults();
		gameTableModel.clearGames();
	}
	private boolean loadCachedResults() {

		GameStorerSearchResponseData response = main.getSearchCache().getResults(
			filterData, localGridState.getHash());
		if (response != null) {
			loadResults(response);
			return true;
		}
		return false;
	}

	//TODO stills seems not perfect, no real visual difference between
	//.85,.75,.65
	// or between 35% 25%
	   private static Color getColor(double percent) {
			//System.out.println("oldp="+percent);
			  if (percent > .6f) {
				  percent = .8f +  ((percent + .4f - 1) / 2);
			  } else if (percent > .5f) {
				  percent = .6f + ((percent + .5f - 1) * 2);
			  } else if (percent > .4f) {
				  percent = .4f + (percent + .6f - 1);
			  } else if (percent > .3f) {
				  percent = .2f + (percent + .7f - 1);
			  } else {
				  percent = 0 + percent / 2;
			  }
			  int green = 237 - (int)(196 * percent);
				//System.out.println("newp="+percent);

			return new Color(255,green,41);
	   }

	private void loadResults(GameStorerSearchResponseData response) {

		currentResponse = response;
		int moves[] = new int[response.searchResponseMoveData().size()];
		int i = 0;
		//TODO rotation not getting set properly from pente.org
		//System.out.println("rotation=" + response.getRotation());
		for (Iterator it = response.searchResponseMoveData().iterator(); it.hasNext(); i++) {
			GameStorerSearchResponseMoveData moveData =
				(GameStorerSearchResponseMoveData) it.next();
			moves[i] = gameBoard.getGridState().rotateMoveToLocalRotation(moveData.getMove(), response.getRotation());
			//moves[i] = moveData.getMove();
		}

		updateGameControls(response);

		gameTableModel.setGames(response.getGames());
		searchResultsTableModel.setResults(response.searchResponseMoveData(), moves);

		//TODO redo this algorithm it is misleading
		/*
		double totalGames = 0;
		double maxP = 0;
		double minP = 100;
		for (Iterator it = response.searchResponseMoveData().iterator(); it.hasNext();) {
			GameStorerSearchResponseMoveData moveData =
				(GameStorerSearchResponseMoveData) it.next();
			totalGames += moveData.getGames();

		}
		int cutoff = (int) (totalGames * 0.05);
		if (cutoff > 10) cutoff = 10;
		//System.out.println("total="+totalGames + ", cutoff="+cutoff);
		for (Iterator it = response.searchResponseMoveData().iterator(); it.hasNext();) {
			GameStorerSearchResponseMoveData moveData =
				(GameStorerSearchResponseMoveData) it.next();

			if (moveData.getGames() > cutoff) {
				if (moveData.getPercentage() > maxP) {
					maxP = moveData.getPercentage();
				}
				if (moveData.getPercentage() < minP) {
					minP = moveData.getPercentage();
				}
			}
		}
*/
		i = 0;
		for (Iterator it = response.searchResponseMoveData().iterator(); it.hasNext(); i++) {
			GameStorerSearchResponseMoveData moveData =
				(GameStorerSearchResponseMoveData) it.next();

			Coord p = gameBoard.getGridState().convertMove(moves[i]);

        	SimpleGridPiece gp = new SimpleGridPiece(p.x, 18 - p.y, 3);

			gp.setColor(getColor(moveData.getPercentage()));
//			if (moveData.getGames() > cutoff) {
//				double mp = moveData.getPercentage();
//				if (maxP == minP) {
//					mp = 1;
//				}
//				else {
//					double tp = mp - minP;
//					if (tp == 0) mp = 0;
//					else mp = tp / (maxP - minP);
//				}
//				//System.out.println("move="+moveData.getMove()+","+"mp="+mp);
//				int green =  237 - (int) (196 * mp);
//				gp.setColor(new Color(255,green,41));
//			}
//			else {
//				gp.setColor(new Color(255,237,41));
//			}

        	gameBoard.addPiece(gp);
		}
	}

	private void updateGameControls(GameStorerSearchResponseData response) {
		int total = response.getGameStorerSearchRequestData().getGameStorerSearchRequestFilterData().getTotalGameNum();
		if (total == 0) {
			gameLabel.setText("No matched games");
		}
		else {
			gameLabel.setText(nf.format(response.getGames().size()) + " of " + nf.format(total) + " games");
		}
		moreGamesButton.setEnabled(response.getGames().size() < total);
		int db = response.getGameStorerSearchRequestData().getGameStorerSearchRequestFilterData().getDb();
		allGamesButton.setEnabled(response.getGames().size() < total && db != 2);
	}

	public void loadGames(int startGameNum) {
		loadGames(startGameNum, startGameNum + main.getFilterNumGames());
	}
	public void loadGames(int startGameNum, int endGameNum) {

		// loading venue data, loadGames() will be called once it is done
		if (filterData.getDb() == 2 && !penteOrgVenuesLoaded) {
			return;
		}

		gameBoard.setMessage("Searching for games ...");
		gameLabel.setText("");
		moreGamesButton.setEnabled(false);

		gameBoard.setCursor(Cursor.WAIT_CURSOR);

		final GameStorerSearchRequestData request =
			new SimpleGameStorerSearchRequestData();

		for (int i = 0; i < localGridState.getNumMoves(); i++) {
			request.addMove(localGridState.getMove(i));
		}

		filterData.setStartGameNum(startGameNum);
		filterData.setEndGameNum(endGameNum);

		final GameStorerSearchRequestFilterData searchFilterData =
			(GameStorerSearchRequestFilterData) filterData.clone();
		request.setGameStorerSearchRequestFilterData(searchFilterData);

		final GameStorerSearchResponseData response =
			new SimpleGameStorerSearchResponseData();

		Thread t = new Thread(new Runnable() {
			public void run() {
				try
            	{
					if (searchFilterData.getDb() == 2) {
						main.getPenteOrgGameSearcher().search(request, response);
					}
					else {
						gameSearcher.search(request, response);
					}
					// this section is to ensure local board is same as searched
					// for, if so load results, else just cache results
					boolean resultsMatchBoard = true;
					if (request.getNumMoves() != localGridState.getNumMoves()) {
						resultsMatchBoard = false;
					}
					else {
						for (int i = 0; i < request.getNumMoves(); i++) {
							if (request.getMove(i) != localGridState.getMove(i)) {
								resultsMatchBoard = false;
								break;
							}
						}
					}
					long hash = localGridState.getHash();
					if (!resultsMatchBoard) {
						GridState s = GridStateFactory.createGridState(
							searchFilterData.getGame(), request);
						hash = s.getHash();
					}

					final GameStorerSearchResponseData existingResponse = main.getSearchCache().getResults(
						searchFilterData, hash);

					log4j.debug("response has " + response.getGames().size());
					for (int i = 0; i < response.getGames().size(); i++) {
						GameData d = (GameData) response.getGames().elementAt(i);
						GameData e = main.getSearchCache().getGame(d.getGameID());
						if (e == null) {
							log4j.debug("add game to search cache " + d.getGameID());
							main.getSearchCache().addGame(d);
						}
						else {
							log4j.debug("found game in search cache " + d.getGameID());
							response.getGames().removeElementAt(i);
							response.getGames().insertElementAt(e, i);
						}
					}
					if (existingResponse == null) {
						log4j.debug("new response, add to search cache");
						main.getSearchCache().addResults(searchFilterData, response, hash);
					}
					else {
						log4j.debug("existing reponse, add any new games to it");
						for (int i = 0; i < response.getGames().size(); i++) {
							GameData d = (GameData) response.getGames().get(i);

							if (!existingResponse.containsGame(d)) {
								existingResponse.getGames().add(d);
							}
							else {
								log4j.debug("existing response, game already there " + d.getGameID());
							}
						}
					}

					final long resultsHash = hash;
	    			javax.swing.SwingUtilities.invokeLater(new Runnable() {
	    				public void run() {
	    					updateMessage();
	    					gameBoard.setCursor(Cursor.HAND_CURSOR);

	    					// make sure again that loading results for
	    					// same position as local board
	    					if (resultsHash == localGridState.getHash()) {
		    					if (existingResponse != null) {
		    						loadResults(existingResponse);
		    					}
		    					else {
		    						loadResults(response);
		    					}
	    					}
	    				}
	    			});

	            } catch (Exception e) {
	            	e.printStackTrace();
	            	JOptionPane.showMessageDialog(GameReviewBoard.this,
	            		"Error searching", "Error", JOptionPane.ERROR_MESSAGE);

	            } finally {
	            	synchronized (searchThreads) {
	            		searchThreads.remove(this);
	            	}
	            }
	        }
		});
    	synchronized (searchThreads) {
    		searchThreads.add(t);
    	}
		t.start();
    }


	//TODO don't need to do this for each tab, move up to Main
	private void loadPenteOrgVenue(final Component c) {

		if (!main.login(true)) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					dbChoice.setEnabled(true);
					gameChoice.setEnabled(true);
					siteChoice.setEnabled(true);
					eventChoice.setEnabled(true);
					roundChoice.setEnabled(true);
				}
			});
			return;
		}

		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					long startTime = System.currentTimeMillis();
					//System.out.println("start");

					Vector v = (Vector) main.getHttpLoader().loadVenueData(c);
					System.out.println("venue size="+v.size());
					GameDbData penteOrg = new SimpleGameDbData();
					penteOrg.setID(2);
					penteOrg.setName("Pente.org Online");
					penteOrg.addGameTreeData(v);
					venueStorer.addGameTreeData(penteOrg);

					penteOrgVenuesLoaded = true;

					javax.swing.SwingUtilities.invokeLater(new Runnable() {
	    				public void run() {
	    					dbChoice.setEnabled(true);
	    					gameChoice.setEnabled(true);
	    					siteChoice.setEnabled(true);
	    					eventChoice.setEnabled(true);
	    					roundChoice.setEnabled(true);
	    					populateGameCombo();
	    				}
	    			});

					//System.out.println("finished in " + (System.currentTimeMillis() - startTime));

				} catch (Exception e) {
					JOptionPane.showMessageDialog(c, "Error loading Pente.org venue", "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();

	    			javax.swing.SwingUtilities.invokeLater(new Runnable() {
	    				public void run() {
							dbChoice.setEnabled(true);
							gameChoice.setEnabled(true);
							siteChoice.setEnabled(true);
							eventChoice.setEnabled(true);
							roundChoice.setEnabled(true);
							if (deSelected != null) {
								filterData.setDb(deSelected.getID());
								dbChoice.setSelectedItem(deSelected);
								populateGameCombo();
							}
	    				}
	    			});

				} finally {
	            	synchronized (searchThreads) {
	            		searchThreads.remove(this);
	            	}
				}
			}
		});

    	synchronized (searchThreads) {
    		searchThreads.add(t);
    	}
    	t.start();
	}


	private MoveChangeListener moveChangeListener = new MoveChangeListener() {
		public void changeMoves(int[] moves, PlunkNode current) {

			if (ai != null && ai.isActive()) ai.stopThinking();

			localGridState.clear();
			gameBoard.getGridState().clear();
			for (int m : moves) {
				localGridState.addMove(m);
				gameBoard.getGridState().addMove(m);
			}

			if (main.showNextMoves() && current != null && current.hasChildren()) {
				for (PlunkNode c : current.getChildren()) {
					gameBoard.addPiece(new NoteGridPiece(c));
				}
			}

			gameBoard.getGridBoard().setThinkingPieceVisible(false);
	        gameBoard.getGridBoard().setThinkingPiecePlayer(
	        	localGridState.getCurrentColor());

	        analyze();

			clearResults();
			if (main.isSearching() && !loadCachedResults()) {
				loadGames(0);
			}
			else {
				updateMessage();
			}
		}
		public void nodeChanged() {
			gameBoard.getGridBoardComponent().refresh();
		}
	};

	private void updateMessage() {

		if (localGridState.isGameOver()) {
			gameBoard.setMessage("Game over!");
		}
		else {
			gameBoard.setMessage(null);
		}
	}
	private void analyze() {

		((PlunkPenteBoardLW) gameBoard.getGridBoardComponent()).clearLines();
        if (localGridState.isGameOver()) return;
        if (filterData.getGame() != GridStateFactory.PENTE) return;
        if (!main.showThreats()) return;

		Utilities.analyzePosition(localGridState, (PlunkPenteBoardLW) gameBoard.getGridBoardComponent());

//        for (Iterator it = a.getThreats(localGridState.getCurrentPlayer()); it.hasNext();) {
//        	Threat t = (Threat) it.next();
//        	if (t.type == Threat.TYPE_POTENTIAL_CAPTURE) {
//        		int ends[] = t.getEnds();
//        		BoardLine l = new BoardLine();
//        		l.setColor(new Color(0, 0, 1, 0.5f));
//        		l.setX1(ends[0]%19);
//        		l.setY1(18-ends[0]/19);
//        		l.setX2(ends[1]%19);
//        		l.setY2(18-ends[1]/19);
//        		((PlunkPenteBoardLW) gameBoard.getGridBoardComponent()).addLine(l);
//        	}
//        }
	}

	private MoveTreeModel createNewTreeModel() {

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");

		final MoveTreeModel tm = new MoveTreeModel(root);
		tm.setPlunkTree(plunkTree);
		tm.setComments(comments);
		tm.setGame(filterData.getGame());

		//TODO start with focus on K10
		JTree tree = new JTree(tm);
		tm.setJTree(tree);

		tree.addTreeSelectionListener(tm);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setScrollsOnExpand(true);
		tree.addMouseListener(movePopupListener);

		MoveIconRenderer mir = new MoveIconRenderer(tm);
		mir.setGame(filterData.getGame());
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
		tm.addListener(moveChangeListener);

		return tm;
	}

	private void loadMoveDb(long treeId, MoveTreeModel tm) {
		PlunkNode r = null;

		try {
			r = plunkDbUtil.loadPlunkTree(treeId, null);

			tm.clear();
			tm.addMoves(r, false);
			tm.visitNode(r.getHash());

		} catch (Exception e) {
			e.printStackTrace();
			//TODO error dialog
		}
	}


	public boolean close() {
		if (deleted || confirmSave(currentTreeModel) != JOptionPane.CANCEL_OPTION) {
			destroy();
			return true;
		}
		else {
			return false;
		}
	}
	private int confirmSave(MoveTreeModel tm) {

		if (!tm.getDirtyNodes().isEmpty()) {
			int c = JOptionPane.showConfirmDialog(
				this, "'" + tm.getPlunkTree().getName() + "' has been modified. Save changes?",
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

	public void export(File f) throws IOException {

		StringBuffer buf = new StringBuffer();
		SGFGameFormat gf = new SGFGameFormat("\r\n");
		currentTreeModel.saveComments();

		//gf.format(currentTreeModel.getPlunkRoot(), filterData.getGame(), plunkTree, buf);

		Utilities.writeFile(f, buf.toString());
	}

	public void venuesUpdated() {
		populateDbCombo();
	}

	Action deleteAnalysisAction = new AbstractAction("Delete",
		new ImageIcon(GameReviewBoard.class.getResource("images/delete_move_db.gif"))) {
		{
			putValue(SHORT_DESCRIPTION, "Delete game analysis");
		}
		public void actionPerformed(ActionEvent e) {
			delete();
		}
		public void delete() {
			int c = JOptionPane.showConfirmDialog(
				GameReviewBoard.this, "Are you sure you want to delete '" + plunkTree.getName() + "'?",
				"Delete Analysis", JOptionPane.YES_NO_OPTION);
			if (c == JOptionPane.YES_OPTION) {
				deleted = true;
				main.deletePlunkTree(plunkTree);
				main.removeTab(GameReviewBoard.this);
			}
		}
	};

}
