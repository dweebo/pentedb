package org.pente.gameDatabase.swing;

import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.io.*;
import java.net.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.JTableHeader;

import org.apache.log4j.PropertyConfigurator;
import org.pente.database.DBHandler;
import org.pente.database.DerbyDBHandler;
import org.pente.game.*;
import org.pente.gameDatabase.GameStorerSearchRequestData;
import org.pente.gameDatabase.GameStorerSearchRequestFilterData;
import org.pente.gameDatabase.GameStorerSearchResponseData;
import org.pente.gameDatabase.GameStorerSearcher;
import org.pente.gameDatabase.HttpGameStorerSearcher;
import org.pente.gameDatabase.SimpleGameStorerSearchRequestData;
import org.pente.gameDatabase.SimpleGameStorerSearchRequestFilterData;
import org.pente.gameDatabase.SimpleGameStorerSearchResponseData;
import org.pente.gameDatabase.swing.component.ListAutoCompleter;
import org.pente.gameDatabase.swing.importer.BrainkingGameImporter;
import org.pente.gameDatabase.swing.importer.GameImporter;
import org.pente.gameDatabase.swing.importer.GameImporterListener;
import org.pente.gameDatabase.swing.importer.ImportDialog;
import org.pente.gameDatabase.swing.importer.PenteOrgGameImporter;
import org.pente.gameDatabase.swing.importer.SGFGameFormat;
import org.pente.gameDatabase.swing.importer.SGFGameImporter;
import org.pente.gameDatabase.swing.importer.StepanovGameImporter;
import org.pente.gameDatabase.swing.importer.VBaryKinGameImporter;
import org.pente.gameServer.client.GameOptions;
import org.pente.gameServer.client.SimpleGameOptions;

import org.pente.mmai.*;

/**
 * @author dweebo
 */
public class Main {

	public int getFilterNumGames() {
		return filterNumGames;
	}

	public void setFilterNumGames(int filterNumGames) {
		this.filterNumGames = filterNumGames;
		try {
			plunkDbUtil.storeProp(new PlunkProp("numgames", filterNumGames));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public PlunkGameVenueStorer getVenueStorer() {
		return venueStorer;
	}
	public List<String> getPlayerNames() {
		return playerNames;
	}
	public PlunkDbUtil getPlunkDbUtil() {
		return plunkDbUtil;
	}
	public SearchCache getSearchCache() {
		return searchCache;
	}

	public static void main(String args[]) throws Exception {

		try {

			//TODO maybe use log4j
			if (args.length > 2 && args[2].equals("log")) {
				System.setErr(new PrintStream(new FileOutputStream("err.log", true)));
				System.setOut(new PrintStream(new FileOutputStream("out.log", true)));
				System.out.println();
				System.out.println("Starting program on " + new java.util.Date());
				System.err.println();
				System.err.println("Starting program on " + new java.util.Date());
			}

	        Thread.setDefaultUncaughtExceptionHandler(
	            new DefaultExceptionHandler());

			try {
		        UIManager.setLookAndFeel(
		            UIManager.getSystemLookAndFeelClassName());
		    } catch (Exception e) { }

			PropertyConfigurator.configure(args[0]);

			final DBHandler db = new DerbyDBHandler(args[1]);
			final PlunkGameVenueStorer gvs = new PlunkGameVenueStorer(db);
			final PlunkPenteGameStorer gameStorer = new PlunkPenteGameStorer(db, gvs);
			final PlunkDbUtil plunkDbUtil = new PlunkDbUtil(db);

			final FasterGameStorerSearcher localGameSearcher = new FasterGameStorerSearcher(
				db, gvs);
			localGameSearcher.setDerby(true);


			javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	            	try {

						JFrame.setDefaultLookAndFeelDecorated(true);

						final JFrame frame = new JFrame("Pente db");
						frame.setIconImage(new ImageIcon(Main.class.getResource("images/logo.png")).getImage());

						final Main main = new Main(db, localGameSearcher, gvs, plunkDbUtil,
							gameStorer, frame);

						frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

						// save bounds for next startup
						frame.addComponentListener(new ComponentAdapter() {
							@Override
							public void componentResized(ComponentEvent e) {
								if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH) {
									main.bounds = frame.getBounds();
								}
							}
							@Override
							public void componentMoved(ComponentEvent e) {
								if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH &&
									frame.getLocation().x > 0 && frame.getLocation().y > 0) {
									main.bounds = frame.getBounds();
								}
							}
						});

						frame.addWindowListener(new WindowAdapter() {

							@Override
							// store bounds/maximized for next startup
							public void windowClosing(WindowEvent e) {
								main.exit();
							}
						});

						Container content = frame.getContentPane();
						content.setLayout(new GridBagLayout());
						GridBagConstraints gbc = new GridBagConstraints();

						gbc.anchor = GridBagConstraints.NORTHWEST;

						gbc.gridx = 1;
						gbc.gridy = 1;
						gbc.gridheight = 1;
						gbc.weighty = 1;
						gbc.weightx = 1;
						gbc.fill = GridBagConstraints.HORIZONTAL;
						content.add(main.getJToolBar(), gbc);

						gbc.gridy++;
						gbc.weighty = 99;
						gbc.fill = GridBagConstraints.BOTH;
						content.add(main.getMainComponent(), gbc);

						//frame.getContentPane().add(main.getJToolBar(), BorderLayout.NORTH);
						//frame.getContentPane().add(main.getMainComponent(), BorderLayout.SOUTH);
						frame.setJMenuBar(main.getJMenuBar());

						frame.pack();

						// load bounds / maximized from last time
						try {
							PlunkProp bounds = plunkDbUtil.loadProp("bounds");
							if (bounds != null) {
								frame.setBounds((Rectangle) bounds.getValue());
							} else {
								frame.setSize(800, 600);
							}

							PlunkProp maxProp = plunkDbUtil.loadProp("maximized");
							if (maxProp != null && ((Boolean) maxProp.getValue()).booleanValue()) {
								frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
							}

							PlunkProp filterNumGamesProp = plunkDbUtil.loadProp("numgames");
							if (filterNumGamesProp != null) {
								main.filterNumGames = ((Integer) filterNumGamesProp.getValue());
							}

						} catch (Exception ex) {
							ex.printStackTrace();

						}

				        frame.setVisible(true);
				        main.firstRun();

		            } catch (Exception e) {
		            	e.printStackTrace();
		            }
	            }
	        });
		} catch (Exception e) {
			System.out.println("1");
			e.printStackTrace();
			if (alreadyRunning(e)) {
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
		            public void run() {
		            	try {
							JOptionPane.showMessageDialog(null,
								"Pente db is already running, you can only run one instance of the program at a time", "Pente db Error", JOptionPane.ERROR_MESSAGE);
							System.exit(-1);
		            	} catch (Exception e) {}
		            }
				});
			}
		}
	}


	private static boolean alreadyRunning(Throwable t) {
		if (t.getMessage().contains("Derby may have already booted")) {
			return true;
		}
		else if (t.getCause() != null) return alreadyRunning(t.getCause());
		return false;
	}

	private DBHandler db;
	private PlunkPenteGameStorer gameStorer;
	private GameStorerSearcher gameSearcher;
	private PlunkGameVenueStorer venueStorer;
	private PlunkDbUtil plunkDbUtil;
	private JTabbedPane tabs;
	private List<PlunkTree> trees = null;
	private JCheckBoxMenuItem searchMenuItem;
	private JToggleButton searchButton;
	private JCheckBoxMenuItem nextMovesMenuItem;
	private JToggleButton nextMovesButton;
	private JCheckBoxMenuItem numberMovesMenuItem;
	private JToggleButton numberMovesButton;
	private JCheckBoxMenuItem threatsMenuItem;
	private JToggleButton threatsButton;
	private int filterNumGames = 10;
	private JFrame frame;

	private PenteOrgGameImporter gameImporter;
	private BrainkingGameImporter bkImporter;
	private SGFGameImporter sgfImporter;
	private VBaryKinGameImporter vbImporter;
	private StepanovGameImporter stepanovImporter;
	private List<GameImporter> importers = new ArrayList<GameImporter>();

	private Rectangle bounds;
    private GameOptions gameOptions = new SimpleGameOptions(3);

    // for gamereviewboards
	private SearchCache searchCache = new SearchCache();

	private List<VenueListener> venueListeners = new ArrayList<VenueListener>();

	// autocomplete name textfields will share this list of names
	// so be careful to always update the list in the swing threads
	private List<String> playerNames;

	private JMenu toolMenu;
	private JMenuItem switchMenu;

	public Main(DBHandler db,
			GameStorerSearcher gameSearcher,
			PlunkGameVenueStorer venueStorer,
			final PlunkDbUtil plunkDbUtil,
			PlunkPenteGameStorer gameStorer,
			JFrame frame) {

		this.db = db;
		this.gameSearcher = gameSearcher;
		this.venueStorer = venueStorer;
		this.plunkDbUtil = plunkDbUtil;
		this.gameStorer = gameStorer;
		this.frame = frame;

		gameImporter = new PenteOrgGameImporter();
		bkImporter = new BrainkingGameImporter();
		sgfImporter = new SGFGameImporter();
		vbImporter = new VBaryKinGameImporter();
		stepanovImporter = new StepanovGameImporter();
		importers.add(sgfImporter);
		importers.add(gameImporter);
		importers.add(bkImporter);
		importers.add(vbImporter);
		importers.add(stepanovImporter);

		try {
			trees = plunkDbUtil.loadPlunkTrees();

			playerNames = ListAutoCompleter.initList(plunkDbUtil.loadPlayers());

		} catch (Exception e) {
			e.printStackTrace();
			//TODO error dialog
		}

	    gameOptions.setPlayerColor(GameOptions.WHITE, 1);
	    gameOptions.setPlayerColor(GameOptions.BLACK, 2);
	    gameOptions.setDraw3DPieces(true);
	    gameOptions.setPlaySound(true);
	    gameOptions.setShowLastMove(true);
	}

	public PlunkPenteGameStorer getGameStorer() {
		return gameStorer;
	}

	private void destroy() {

	}

	public void exit() {
		if (canExit()) {

			try {
				if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
					plunkDbUtil.storeProp(new PlunkProp("maximized", true));
				}
				else {
					plunkDbUtil.storeProp(new PlunkProp("maximized", false));
				}
				if (bounds != null) {
					plunkDbUtil.storeProp(new PlunkProp("bounds", bounds));
				}

			} catch (Exception ex) {}

			db.destroy();
			frame.dispose();
			destroy();
		}
	}

	private boolean canExit() {

		for (int i = 0; i < tabs.getTabCount(); i++) {
			TabComponent c = (TabComponent) tabs.getComponentAt(i);
			if (!c.close()) return false;
		}

		return true;
	}

	public JMenuBar getJMenuBar() {

		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenuItem save = new JMenuItem(saveAction);
		save.setMnemonic(KeyEvent.VK_S);
		save.setAccelerator(KeyStroke.getKeyStroke('S',
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));

		JMenuItem saveAll = new JMenuItem("Save All");
		saveAll.setEnabled(false);


		JMenu newMenu = new JMenu("New");

		JMenuItem newAI = new JMenuItem(newAIAction);
		newAI.setMnemonic(KeyEvent.VK_C);
		newAI.setAccelerator(KeyStroke.getKeyStroke('C',
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() |
			KeyEvent.SHIFT_MASK, false));

		JMenuItem newAnalysis = new JMenuItem(newAnalysisAction);
		newAnalysis.setMnemonic(KeyEvent.VK_A);
		newAnalysis.setAccelerator(KeyStroke.getKeyStroke('A',
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));

		JMenuItem newEntry = new JMenuItem(newEntryAction);
		newEntry.setMnemonic(KeyEvent.VK_G);
		newEntry.setAccelerator(KeyStroke.getKeyStroke('G',
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));

		newMenu.setMnemonic(KeyEvent.VK_N);
		newMenu.add(newAI);
		newMenu.add(newAnalysis);
		newMenu.add(newEntry);

		JMenuItem open = new JMenuItem(openMoveDbAction);
		open.setMnemonic(KeyEvent.VK_O);
		open.setAccelerator(KeyStroke.getKeyStroke('O',
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));

		JMenuItem close = new JMenuItem(closeAction);
		close.setMnemonic(KeyEvent.VK_C);
		close.setAccelerator(KeyStroke.getKeyStroke('W',
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));

		JMenuItem closeAll = new JMenuItem(closeAllAction);
		closeAll.setMnemonic(KeyEvent.VK_L);
		closeAll.setAccelerator(KeyStroke.getKeyStroke('W',
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() |
			KeyEvent.SHIFT_MASK, false));

		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});

		JMenuItem importItem = new JMenuItem(importAction);
		importItem.setMnemonic(KeyEvent.VK_I);
		importItem.setAccelerator(KeyStroke.getKeyStroke('I',
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));

		JMenuItem export = new JMenuItem(exportAction);
		export.setMnemonic(KeyEvent.VK_E);
		export.setAccelerator(KeyStroke.getKeyStroke('E',
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));

		fileMenu.add(newMenu);
		fileMenu.add(open);
		fileMenu.addSeparator();
		fileMenu.add(close);
		fileMenu.add(closeAll);
		fileMenu.addSeparator();
		fileMenu.add(save);
		fileMenu.add(saveAll);
		fileMenu.addSeparator();
		fileMenu.add(importItem);
		fileMenu.add(export);
		fileMenu.addSeparator();
		fileMenu.add(exit);
		menuBar.add(fileMenu);

		JMenu edit = new JMenu("Edit");
		JMenuItem editProp = new JMenuItem("Edit");
		editProp.setEnabled(false);
		JMenuItem delete = new JMenuItem("Delete");
		delete.setEnabled(false);
		edit.add(editProp);
		edit.add(delete);
		menuBar.add(edit);

		JMenu viewMenu = new JMenu("View");
		searchMenuItem = new JCheckBoxMenuItem(searchAction);
		nextMovesMenuItem = new JCheckBoxMenuItem(nextMovesAction);
		numberMovesMenuItem = new JCheckBoxMenuItem(numberMovesAction);
		threatsMenuItem = new JCheckBoxMenuItem(threatsAction);

		try {
			PlunkProp p = plunkDbUtil.loadProp("search");
			if (p != null && ((Boolean) p.getValue()).booleanValue()) {
				searchMenuItem.setSelected(true);
			}
			p = plunkDbUtil.loadProp("nextMoves");
			if (p != null && ((Boolean) p.getValue()).booleanValue()) {
				nextMovesMenuItem.setSelected(true);
			}
			p = plunkDbUtil.loadProp("numberMoves");
			if (p != null && ((Boolean) p.getValue()).booleanValue()) {
				numberMovesMenuItem.setSelected(true);
			}
			p = plunkDbUtil.loadProp("threats");
			if (p != null && ((Boolean) p.getValue()).booleanValue()) {
				threatsMenuItem.setSelected(true);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}


		viewMenu.add(searchMenuItem);
		viewMenu.add(nextMovesMenuItem);
		viewMenu.add(numberMovesMenuItem);
		viewMenu.add(threatsMenuItem);
		menuBar.add(viewMenu);

		toolMenu = new JMenu("Tools");
		JMenuItem settings = new JMenuItem("Settings");
		settings.setEnabled(false);
		toolMenu.add(settings);
		menuBar.add(toolMenu);

		JMenu help = new JMenu("Help");
		JMenuItem helpContents = new JMenuItem("Help");
		helpContents.setEnabled(false);
		JMenuItem about = new JMenuItem("About");
		about.setEnabled(false);
		help.add(helpContents);
		help.add(about);
		menuBar.add(help);


		switchMenu = new JMenuItem(switchAction);

		return menuBar;
	}
	public JComponent getMainComponent() {
		tabs = new JTabbedPane();
		tabs.addMouseListener(tabPopupListener);

		tabs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (tabs.getTabCount() == 0) return;

				TabComponent c = (TabComponent) tabs.getSelectedComponent();
				if (c instanceof ViewGamePanel)
				{
					toolMenu.add(switchMenu);
				}
				else
				{
					toolMenu.remove(switchMenu);
				}
			}
		});
		// maybe load in all last opened tabs here

		JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JLabel l = new JLabel("");
		//l.setPreferredSize(new Dimension(500, 12));
		statusBar.add(l);
		//statusBar.setBackground(Color.blue);
		//statusBar.setPreferredSize(l.getPreferredSize());

		JPanel main = new JPanel();
		main.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		gbc.weighty = 99;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		main.add(tabs, gbc);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridheight = 1;
		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		main.add(statusBar, gbc);

		new DropTarget(main, dndImporter);

		return main;
	}
	public JToolBar getJToolBar() {
		JToolBar toolbar = new JToolBar();

		toolbar.add(saveAction);
		toolbar.add(openMoveDbAction);
		//toolbar.add(newAction);

		toolbar.addSeparator();

		searchButton = new JToggleButton(searchAction);
		searchButton.setText("");
		toolbar.add(searchButton);

		nextMovesButton = new JToggleButton(nextMovesAction);
		nextMovesButton.setText("");
		toolbar.add(nextMovesButton);

		numberMovesButton = new JToggleButton(numberMovesAction);
		numberMovesButton.setText("");
		toolbar.add(numberMovesButton);

		threatsButton = new JToggleButton(threatsAction);
		threatsButton.setText("T");
		toolbar.add(threatsButton);

		try {
			PlunkProp p = plunkDbUtil.loadProp("search");
			if (p != null && ((Boolean) p.getValue()).booleanValue()) {
				searchButton.setSelected(true);
			}
			p = plunkDbUtil.loadProp("nextMoves");
			if (p != null && ((Boolean) p.getValue()).booleanValue()) {
				nextMovesButton.setSelected(true);
			}
			p = plunkDbUtil.loadProp("numberMoves");
			if (p != null && ((Boolean) p.getValue()).booleanValue()) {
				numberMovesButton.setSelected(true);
			}
			p = plunkDbUtil.loadProp("threats");
			if (p != null && ((Boolean) p.getValue()).booleanValue()) {
				threatsButton.setSelected(true);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}


		return toolbar;
	}

	private void firstRun() throws Exception {

		try {
			File f = new File("sample.sgf");
			if (trees.isEmpty() && f.exists() && f.canRead()) {

				searchButton.setSelected(true);
				searchMenuItem.setSelected(true);
				nextMovesButton.setSelected(true);
				nextMovesMenuItem.setSelected(true);

				plunkDbUtil.storeProp(new PlunkProp("search", searchButton.isSelected()));
				plunkDbUtil.storeProp(new PlunkProp("nextMoves", nextMovesButton.isSelected()));

				attemptImport(f);
				File f2 = new File("mmai.sgf");
				if (f2.exists() && f2.canRead()) {
					attemptImport(f2);
				}
				/*
				SGFGameImporter si = new SGFGameImporter();
				si.attemptImport(new File("sample.sgf"), new GameImporterListener() {
					public void analysisRead(PlunkTree t, String importerName) {
						try {
							getPlunkDbUtil().storePlunkTree(t);
							getPlunkDbUtil().insertPlunkNodes(t.getRoot(), t.getTreeId());
							addPlunkTree(t);

							GameStorerSearchRequestFilterData filterData = new SimpleGameStorerSearchRequestFilterData();
							filterData.setDb(1);
							filterData.setGame(1);

							TabComponentEditListener l = addGameReviewTab(filterData, t, null, t.getName());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					public void gameRead(PlunkGameData g, String importerName) {
						try {
							// find db or create a new one
							List<GameDbData> dbs = getVenueStorer().getDbTree();
							GameDbData db = null;
							for (GameDbData db2 : dbs) {
								if (db2.getName().equals(g.getDbName())) {
									db = db2;
									break;
								}
							}
							if (db == null) {
								db = new SimpleGameDbData();
								db.setName(g.getDbName());
								getVenueStorer().addGameDbData(db,
									GridStateFactory.getGameId(g.getGame()));
							}
							saveGame(g, db, null);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				*/
			}
			else if (trees.size() == 1) {
				PlunkTree t = trees.get(0);
				if (t.getName().equals("Sample Analysis")) {
					GameStorerSearchRequestFilterData filterData = new SimpleGameStorerSearchRequestFilterData();
					filterData.setDb(1);
					filterData.setGame(1);
					addGameReviewTab(filterData, trees.get(0), null, trees.get(0).getName());
				}
			}

		} catch (Exception ex) {
			//ex.printStackTrace();
			throw ex;
		}
	}
	private void attemptImport(File f) {
		SGFGameImporter si = new SGFGameImporter();
		si.attemptImport(f, new GameImporterListener() {
			public void analysisRead(PlunkTree t, String importerName) {
				try {
					getPlunkDbUtil().storePlunkTree(t);
					getPlunkDbUtil().insertPlunkNodes(t.getRoot(), t.getTreeId());
					addPlunkTree(t);

					GameStorerSearchRequestFilterData filterData = new SimpleGameStorerSearchRequestFilterData();
					filterData.setDb(1);
					filterData.setGame(1);

					TabComponentEditListener l = addGameReviewTab(filterData, t, null, t.getName());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			public void gameRead(PlunkGameData g, String importerName) {
				try {
					// find db or create a new one
					List<GameDbData> dbs = getVenueStorer().getDbTree();
					GameDbData db = null;
					for (GameDbData db2 : dbs) {
						if (db2.getName().equals(g.getDbName())) {
							db = db2;
							break;
						}
					}
					if (db == null) {
						db = new SimpleGameDbData();
						db.setName(g.getDbName());
						getVenueStorer().addGameDbData(db,
							GridStateFactory.getGameId(g.getGame()));
					}
					saveGame(g, db, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public TabComponentEditListener addTab(final TabComponent tabComponent, String name) {
		//TODO If tab already exists then don't add it, just switch to it
		//make a new isTabOpen(String id) for that
		tabs.addTab(name, (JComponent) tabComponent);
		ButtonTabComponent close = new ButtonTabComponent(tabs, name);
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeTab(tabComponent);
			}
		});
        tabComponent.addEditListener(close);

		tabs.setTabComponentAt(tabs.getTabCount() - 1, close);
		tabs.setSelectedIndex(tabs.getTabCount() - 1);

		return close;
	}
	public void renameTab(JComponent tabComponent, String name) {
		int i = tabs.indexOfComponent(tabComponent);
		if (i != -1) {
			ButtonTabComponent close = (ButtonTabComponent) tabs.getTabComponentAt(i);
			close.setName(name);
		}
	}
	Action closeAction = new AbstractAction("Close") {
		public void actionPerformed(ActionEvent e) {

			if (tabs.getTabCount() == 0) return;

			TabComponent c = (TabComponent) tabs.getSelectedComponent();
			removeTab(c);
		}
	};
	Action closeAllAction = new AbstractAction("Close All") {
		public void actionPerformed(ActionEvent e) {
			for (int i = 0; i < tabs.getTabCount(); i++) {
				TabComponent c = (TabComponent) tabs.getComponentAt(i);
				removeTab(c);
			}
		}
	};


	public void removeTab(TabComponent tabComponent) {
		if (tabComponent.close()) {
			tabs.remove((JComponent) tabComponent);
			venueListeners.remove(tabComponent);
		}
	}

	public void addPlunkTree(PlunkTree tree) {
		trees.add(tree);
	}
	public void deletePlunkTree(PlunkTree tree) {
		trees.remove(tree);

		try {
			plunkDbUtil.deleteTree(tree.getTreeId());
		} catch (SQLException s) {
			System.err.println("error deleting tree " + tree.getTreeId());
			s.printStackTrace();
		}
	}

	MouseAdapter tabPopupListener = new MouseAdapter() {
		public void mouseReleased(MouseEvent e) {
			if (tabs.getTabCount() == 0) return;
			int index = tabs.indexAtLocation(e.getX(), e.getY());
			if (index == -1) return;
			TabComponent t = (TabComponent) tabs.getComponentAt(index);
			if (!e.isPopupTrigger()) {
				t.hideTabPopup();
			}
			else {
				t.showTabPopup(tabs, e.getX(), e.getY());
			}
		}
	};


	Action openMoveDbAction = new AbstractAction("Open",
		new ImageIcon(Main.class.getResource("images/open_move_db.gif"))) {
		{ putValue(SHORT_DESCRIPTION, "Open Game Analysis"); }

		public void actionPerformed(ActionEvent e) {

			OpenDialog open = new OpenDialog(frame, trees);
			int r = open.getResult();
			//TODO don't allow opening already open tree, or switch to it
			if (r != JOptionPane.CANCEL_OPTION) {
				PlunkTree t = open.getSelectedTree();
				if (t != null) {

					frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					//TODO load this from db instead
					GameStorerSearchRequestFilterData filterData = new SimpleGameStorerSearchRequestFilterData();
					filterData.setDb(1);
					filterData.setGame(GridStateFactory.PENTE);
					GameReviewBoard rb = new GameReviewBoard(gameSearcher,
						venueStorer, plunkDbUtil, t, Main.this, filterData,
						null, t.getName(), trees);
					addTab(rb, t.getName());
					venueListeners.add(rb);
					//if (isSearching()) {
					//	rb.search();
					//}
					frame.setCursor(null);
				}
			}
			open.dispose();
		}
	};

	Action saveAction = new AbstractAction("Save",
		new ImageIcon(Main.class.getResource("images/disk.png"))) {
		{ putValue(SHORT_DESCRIPTION, "Save"); }

		public void actionPerformed(ActionEvent e) {

			if (tabs.getTabCount() == 0) return;

			TabComponent c = (TabComponent) tabs.getSelectedComponent();
			c.save();
		}
	};

	public boolean isSearching() {
		return searchButton.isSelected();
	}
	public boolean showNextMoves() {
		return nextMovesButton.isSelected();
	}
	public boolean showMoveNumber() {
		return numberMovesButton.isSelected();
	}
	public boolean showThreats() {
		return threatsButton.isSelected();
	}

	Action searchAction = new AbstractAction("Search",
		new ImageIcon(Main.class.getResource("images/magnifier.png"))) {
		{
			putValue(SHORT_DESCRIPTION, "Search game database");
		}
		public void actionPerformed(ActionEvent a) {
			boolean search = false;
			if (a.getSource() instanceof JCheckBoxMenuItem) {
				search = ((JCheckBoxMenuItem) a.getSource()).isSelected();
			}
			else {
				search = ((JToggleButton) a.getSource()).isSelected();
			}

			searchButton.setSelected(search);
			searchMenuItem.setSelected(search);

			try {
				plunkDbUtil.storeProp(new PlunkProp("search", search));
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (tabs.getTabCount() == 0) return;

			TabComponent c = (TabComponent) tabs.getSelectedComponent();
			c.search();
		}
	};
	Action nextMovesAction = new AbstractAction("Next Moves",
		new ImageIcon(Main.class.getResource("images/next_moves.png"))) {
		{
			putValue(SHORT_DESCRIPTION, "Show next moves");
		}
		public void actionPerformed(ActionEvent a) {
			boolean next = false;
			if (a.getSource() instanceof JCheckBoxMenuItem) {
				next = ((JCheckBoxMenuItem) a.getSource()).isSelected();
			}
			else {
				next = ((JToggleButton) a.getSource()).isSelected();
			}
			nextMovesButton.setSelected(next);
			nextMovesMenuItem.setSelected(next);

			try {
				plunkDbUtil.storeProp(new PlunkProp("nextMoves", next));
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (tabs.getTabCount() == 0) return;

			TabComponent c = (TabComponent) tabs.getSelectedComponent();
			c.nextMoves();

		}
	};
	Action numberMovesAction = new AbstractAction("Move Numbers",
		new NumberMovesIcon(16)) {
		{
			putValue(SHORT_DESCRIPTION, "Show move numbers");
		}
		public void actionPerformed(ActionEvent a) {
			boolean number = false;
			if (a.getSource() instanceof JCheckBoxMenuItem) {
				number = ((JCheckBoxMenuItem) a.getSource()).isSelected();
			}
			else {
				number = ((JToggleButton) a.getSource()).isSelected();
			}
			numberMovesButton.setSelected(number);
			numberMovesMenuItem.setSelected(number);

			try {
				plunkDbUtil.storeProp(new PlunkProp("numberMoves", number));
			} catch (Exception e) {
				e.printStackTrace();
			}

			for (int i = 0; i < tabs.getTabCount(); i++) {
				TabComponent c = (TabComponent) tabs.getComponentAt(i);
				c.numberMoves();
			}
		}
	};
	Action threatsAction = new AbstractAction("Threats") {
		{
			putValue(SHORT_DESCRIPTION, "Show threats");
		}
		public void actionPerformed(ActionEvent a) {
			boolean threats = false;
			if (a.getSource() instanceof JCheckBoxMenuItem) {
				threats = ((JCheckBoxMenuItem) a.getSource()).isSelected();
			}
			else {
				threats = ((JToggleButton) a.getSource()).isSelected();
			}
			threatsButton.setSelected(threats);
			threatsMenuItem.setSelected(threats);

			try {
				plunkDbUtil.storeProp(new PlunkProp("threats", threats));
			} catch (Exception e) {
				e.printStackTrace();
			}

			for (int i = 0; i < tabs.getTabCount(); i++) {
				TabComponent c = (TabComponent) tabs.getComponentAt(i);
				c.threats();
			}
		}
	};

	JFileChooser chooser = null;
	public JFileChooser getChooser() {
		if (chooser == null) {
			chooser = new JFileChooser() {

				private static final long serialVersionUID = 1541813407103968847L;

				@Override
				public void updateUI() {
					setFileFilter(new javax.swing.filechooser.FileFilter() {
						@Override
						public boolean accept(File f) {
					          return !f.getName().endsWith(".zip");
						}
						@Override
						public String getDescription() {
					          return "No archive files";
						}
				    });
					//putClientProperty("FileChooser.useShellFolder", Boolean.FALSE);
					super.updateUI();
				}
			};
		}

//	    chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
//			@Override
//			public boolean accept(File f) {
//		          return !f.getName().endsWith(".zip");
//			}
//			@Override
//			public String getDescription() {
//		          return "No archive files";
//			}
//	    });


		return chooser;
	}



//	ContentHandler bkContentHandler = new ContentHandler() {
//	    public Object getContent(URLConnection urlc) throws IOException {
//	    	InputStreamReader in = new InputStreamReader(urlc.getInputStream());
//	    	StringBuffer buffer = new StringBuffer();
//	        char chars[] = new char[1024];
//	        while (true) {
//
//	            int l = in.read(chars);
//	            if (l == -1) {
//	                break;
//	            }
//	            else {
//	                buffer.append(chars,0,l);
//	            }
//	        }
//	        if (in != null) {
//	        	in.close();
//	        }
//
//	    	return buffer;
//	    }
//	 };
//
	DropTargetListener dndImporter = new DropTargetListener() {
		public void dragEnter(DropTargetDragEvent dtde) {
			//System.out.println("drag enter");
		}
		public void dragExit(DropTargetEvent dte) {
			//System.out.println("drag exit");
		}
		public void dragOver(DropTargetDragEvent dtde) {
			//System.out.println("drag over");
		}
		public void drop(DropTargetDropEvent drop) {

			try {
//				URLConnection.setContentHandlerFactory(new ContentHandlerFactory() {
//
//					public ContentHandler createContentHandler(String mimeType) {
//						if(mimeType.equals("text/dsg")) {
//							return bkContentHandler;
//						}
//						return null;
//					}
//				});
//
				Transferable tr = drop.getTransferable();
				DataFlavor[] flavors = tr.getTransferDataFlavors();
				//for (DataFlavor df : flavors) {
				//	System.out.println("flavor: " + df.getMimeType());
				//}
				for (DataFlavor df : flavors) {

					// from file browser or other file app?
					if (df.isFlavorJavaFileListType()) {
						drop.acceptDrop(DnDConstants.ACTION_COPY);

						List l = (List) tr.getTransferData(df);
						File f[] = new File[l.size()];
						for (int i = 0; i < f.length; i++) {
							f[i] = (File) l.get(i);
						}
						startImport(f);

				        drop.dropComplete(true);
						return;
					}
				}
				for (DataFlavor df : flavors) {
					// from web browser pointing to game
					if (df.getMimeType().equals("application/x-java-url; class=java.net.URL")) {

						drop.acceptDrop(DnDConstants.ACTION_COPY);

						URL url = (URL) tr.getTransferData(df);
						if (!url.getProtocol().equals("http")) {
							continue;
						}

						URLConnection con = url.openConnection();
						if (url.getHost().endsWith("pente.org")) {
							if (!login(true)) {
						        drop.dropComplete(true);
								return;
							}
							StringBuffer cookieBuffer = new StringBuffer(
								"name2=").append(name).append(", password2=").append(password);
							con.setRequestProperty("Cookie", cookieBuffer.toString());
						}
						byte b[] = Utilities.readStream(con.getInputStream());

						for (GameImporter gi : importers) {
							if (gi.attemptImport(b, new GameImporterListener() {
								public void analysisRead(PlunkTree t, String importer) {}
								public void gameRead(PlunkGameData g, String importer) {
									g.setRoot(Utilities.convertGame(g));
									newAction.actionPerformed(new ActionEvent(g, 0, ""));
								}
							})) {
								break;
							}
						}

				        drop.dropComplete(true);
						break;
					}
				}

			} catch (Exception ex) {
				ex.printStackTrace();
				drop.rejectDrop();
			}
		}
		public void dropActionChanged(DropTargetDragEvent dtde) {
		}
	};

	Action importAction = new AbstractAction("Import") {
		{
			putValue(SHORT_DESCRIPTION, "Import game(s)");
		}
		public void actionPerformed(ActionEvent e) {

			getChooser();
			chooser.setMultiSelectionEnabled(true);
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int r = chooser.showOpenDialog(frame);
			if (r == JFileChooser.APPROVE_OPTION) {
				boolean importOk = false;
				try {
					File f[] = chooser.getSelectedFiles();
					startImport(f);

				} catch (Exception ex) {
					//TODO log it
					ex.printStackTrace();
					importOk = false;
				}

				if (!importOk) {

				}
			}
		}
	};

	GameImporterListener importListener = new GameImporterListener() {
		public void analysisRead(PlunkTree t, String importer) {
			System.out.println("Analysis read " + t.getName() + " by " + importer);
		}
		public void gameRead(PlunkGameData g, String importer) {
			System.out.println("Game read " + Utilities.getGameName(g) + " by " + importer);
		}
	};
	private void startImport(File filesAndDirs[]) {

		ImportDialog d = new ImportDialog(frame,this,filesAndDirs,importers);
		d.dispose();
	}
//		ArrayList<File> files = new ArrayList<File>();
//
//		for (File f : filesAndDirs) {
//			if (f.isFile()) {
//				files.add(f);
//			}
//			else if (f.isDirectory()) {
//				for (File f2 : f.listFiles()) {
//					files.add(f2);
//				}
//			}
//		}
//
//		for (File f : files) {
//
//			boolean ok = false;
//			for (GameImporter gi : importers) {
//				if (gi.attemptImport(f, importListener)) {
//					ok = true;
//					break;
//				}
//			}
//			if (!ok) {
//				System.out.println("Failed to import " + f.getName());
//			}
//		}
//	}

	Action exportAction = new AbstractAction("Export") {
		{
			putValue(SHORT_DESCRIPTION, "Export");
		}
		public void actionPerformed(ActionEvent e) {

			String tabName = null;
			int exportType = ExportDialog.EXPORT_TYPE_LIST;
			if (tabs.getTabCount() != 0) {
				tabName = tabs.getTitleAt(tabs.getSelectedIndex());
				exportType = ExportDialog.EXPORT_TYPE_OPEN;
			}

			ExportDialog ed = new ExportDialog(frame, tabName,
				exportType, trees, venueStorer.getDbTree(), Main.this);
			ed.dispose();
//			int r = ed.getResult();
//			if (r == JOptionPane.CANCEL_OPTION) return;
		}
	};

	Action switchAction = new AbstractAction("Open in game analysis") {
		public void actionPerformed(ActionEvent e) {
			TabComponent c = (TabComponent) tabs.getSelectedComponent();
			if (c instanceof ViewGamePanel)
			{
				ViewGamePanel v = (ViewGamePanel) c;
				PlunkGameData g = v.getGameData();
				GameStorerSearchRequestFilterData filterData = new SimpleGameStorerSearchRequestFilterData();
				filterData.setDb(1);
				filterData.setGame(GridStateFactory.getGameId(g.getGame()));

				TabComponentEditListener l = addGameReviewTab(filterData, null, g,
					Utilities.getGameName(g) + " Analysis");
				l.editsMade();
			}
		}
	};

	Action newAnalysisAction = new AbstractAction("Game Analysis",
		new ImageIcon(Main.class.getResource("images/new_move_db.gif"))) {
		{
			putValue(SHORT_DESCRIPTION, "New game analysis");
		}
		public void actionPerformed(ActionEvent e) {
			run(e.getSource());
		}

		public void run(Object obj) {

			NewDialog2 newDialog = new NewDialog2(frame,
				"New Game Analysis", -1, GridStateFactory.getNormalGames());
			int r = newDialog.getResult();
			if (r == JOptionPane.CANCEL_OPTION) {
				return;
			}
			else {

				GameStorerSearchRequestFilterData filterData = new SimpleGameStorerSearchRequestFilterData();
				filterData.setDb(1);
				filterData.setGame(newDialog.getGame());

				TabComponentEditListener l = addGameReviewTab(filterData, null, null, "New Game Analysis");
				l.editsMade();
			}
		}
	};
	Action newEntryAction = new AbstractAction("Game Entry",
		new ImageIcon(Main.class.getResource("images/new_move_db.gif"))) {
		{
			putValue(SHORT_DESCRIPTION, "New game entry");
		}
		public void actionPerformed(ActionEvent e) {
			run(e.getSource());
		}

		public void run(Object obj) {

			NewDialog2 newDialog = new NewDialog2(frame, "New Game Entry", -1);
			int r = newDialog.getResult();
			if (r == JOptionPane.CANCEL_OPTION) {
				return;
			}
			else {
				final GameOptions gameOptions = new SimpleGameOptions(3);
		        gameOptions.setPlayerColor(GameOptions.WHITE, 1);
		        gameOptions.setPlayerColor(GameOptions.BLACK, 2);
		        gameOptions.setDraw3DPieces(true);
		        gameOptions.setPlaySound(true);
		        gameOptions.setShowLastMove(true);
		        gameOptions.setDrawDepth(showMoveNumber());

		        PlunkNode movesRoot = null;
		        String gameName = null;

		        PlunkGameData gameData = new PlunkGameData();
		        gameData.setGame(GridStateFactory.getDisplayName(newDialog.getGame()));
		        gameName = "New game";
	        	gameData.setEditable(true);
	        	gameData.setStored(false);

	        	ViewGamePanel p = new ViewGamePanel(Main.this, plunkDbUtil,
	        		venueStorer, gameStorer, gameData, gameOptions, 0,
	        		movesRoot, gameName, 0, false, false, null);

	    		venueListeners.add(p);

				TabComponentEditListener l = addTab(p, gameName);
				l.editsMade();
			}
		}
	};
	Action newAIAction = new AbstractAction("Game Against Computer") {
		{
			putValue(SHORT_DESCRIPTION, "New game against computer");
		}
		public void actionPerformed(ActionEvent e) {
			run(e.getSource());
		}

		public void run(Object obj) {

			NewDialog2 newDialog = new NewDialog2(
				frame, "New game against computer", -1, new String[] { "Pente", "Keryo-Pente" });
			int r = newDialog.getResult();
			if (r == JOptionPane.CANCEL_OPTION) {
				return;
			}
			else {
				final GameOptions gameOptions = new SimpleGameOptions(3);
		        gameOptions.setPlayerColor(GameOptions.WHITE, 1);
		        gameOptions.setPlayerColor(GameOptions.BLACK, 2);
		        gameOptions.setDraw3DPieces(true);
		        gameOptions.setPlaySound(true);
		        gameOptions.setShowLastMove(true);
		        gameOptions.setDrawDepth(showMoveNumber());

		        PlunkNode movesRoot = null;
		        String gameName = null;

		        PlunkGameData gameData = new PlunkGameData();
		        gameData.setGame(GridStateFactory.getDisplayName(newDialog.getGame()));
		        gameName = "New game against computer";//TODO add ai name

	        	gameData.setEditable(true);
	        	gameData.setStored(false);

	        	ViewGamePanel p = new ViewGamePanel(Main.this, plunkDbUtil, venueStorer,
	        		gameStorer, gameData, gameOptions, 0, movesRoot, gameName, 0, false, true, trees);

	    		venueListeners.add(p);

				TabComponentEditListener l = addTab(p, gameName);
				l.editsMade();
			}
		}
	};

	Action newAction = new AbstractAction("New",
		new ImageIcon(Main.class.getResource("images/new_move_db.gif"))) {
		{
			putValue(SHORT_DESCRIPTION, "New game analysis or new game entry");
		}
		public void actionPerformed(ActionEvent e) {
			run(e.getSource());
		}

		public void run(Object obj) {

			PlunkGameData gameData = null;
			int game = -1;
			if (obj != null && obj instanceof PlunkGameData) {
				gameData = (PlunkGameData) obj;
				game = GridStateFactory.getGameId(gameData.getGame());
			}
	        boolean imported = gameData != null;

			NewDialog newDialog = new NewDialog(frame, game, imported ?
				NewDialog.TYPE_GAME : NewDialog.TYPE_TREE);
			int r = newDialog.getResult();
			if (r == JOptionPane.CANCEL_OPTION) {
				return;
			}
			else {

				if (newDialog.getNewType() == NewDialog.TYPE_GAME) {
			        final GameOptions gameOptions = new SimpleGameOptions(3);
			        gameOptions.setPlayerColor(GameOptions.WHITE, 1);
			        gameOptions.setPlayerColor(GameOptions.BLACK, 2);
			        gameOptions.setDraw3DPieces(true);
			        gameOptions.setPlaySound(true);
			        gameOptions.setShowLastMove(true);
			        gameOptions.setDrawDepth(showMoveNumber());

			        PlunkNode movesRoot = null;
			        String gameName = null;
			        if (gameData == null) {
			        	gameData = new PlunkGameData();
			        	gameData.setGame(GridStateFactory.getDisplayName(newDialog.getGame()));
			        	gameName = "New game";
			        }
			        else {
				    	movesRoot = Utilities.convertGame(gameData);
					    gameName = Utilities.getGameName(gameData);
			        }

		        	gameData.setEditable(true);
		        	gameData.setStored(false);

		        	ViewGamePanel p = new ViewGamePanel(Main.this, plunkDbUtil, venueStorer,
		        		gameStorer, gameData, gameOptions, 0, movesRoot, gameName, 0, imported, false, null);

		    		venueListeners.add(p);

					TabComponentEditListener l = addTab(p, gameName);
					if (imported) l.editsMade();
				}
				else {
					GameStorerSearchRequestFilterData filterData = new SimpleGameStorerSearchRequestFilterData();
					filterData.setDb(1);
					filterData.setGame(newDialog.getGame());

					TabComponentEditListener l = addGameReviewTab(filterData, null, gameData, "New Game Analysis");
					l.editsMade();
				}
			}
		}
	};
	public void addGameViewTab(PlunkGameData gameData) {

		//TODO allow importing to use the dbid from file
    	ViewGamePanel p = new ViewGamePanel(Main.this, plunkDbUtil, venueStorer,
    		gameStorer, gameData, gameOptions, 0, gameData.getRoot(),
    		Utilities.getGameName(gameData), 0, true, false, null);
		venueListeners.add(p);

		TabComponentEditListener l = addTab(p, Utilities.getGameName(gameData));
		l.editsMade();
	}

	public TabComponentEditListener addGameReviewTab(GameStorerSearchRequestFilterData filterData,
		PlunkTree plunkTree, GameData gameData, String name) {

		GameReviewBoard rb = new GameReviewBoard(gameSearcher,
			venueStorer, plunkDbUtil, plunkTree, Main.this, filterData,
			gameData, name, trees);

		venueListeners.add(rb);

		TabComponentEditListener l = addTab(rb, name);

		//if (isSearching()) {
		//	rb.search();
		//}

		return l;
	}

	public void saveGame(final PlunkGameData g, GameDbData db, final PlunkGameData oldGame) throws Exception {

		getGameStorer().storeGame(g, db);
		// i think it is ok to run this in whatever thread, searchcache is synchronized
		searchCache.updateGame(g, oldGame);

		//can be called by non-swing threads so be careful
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				notifyVenueListeners();
				int i = Collections.binarySearch(playerNames,
					g.getPlayer1Data().getUserIDName(), ListAutoCompleter.noCaseComp);
				if (i < 0) {
					i = (i + 1) * -1;
					playerNames.add(i, g.getPlayer1Data().getUserIDName());
				}
				i = Collections.binarySearch(playerNames,
					g.getPlayer2Data().getUserIDName(), ListAutoCompleter.noCaseComp);
				if (i < 0) {
					i = (i + 1) * -1;
					playerNames.add(i, g.getPlayer2Data().getUserIDName());
				}
			}
		});
	}
	public void addVenueListener(VenueListener l) {
		venueListeners.add(l);
	}
	public void notifyVenueListeners() {
		for (VenueListener v : venueListeners) {
			v.venuesUpdated();
		}
	}

	private String getHost() {
		if (System.getProperty("host") != null) {
			return System.getProperty("host");
		} else {
			return "pente.org";
		}
	}

	private PlunkHttpLoader httpLoader = null;
	private GameStorerSearcher penteOrgGameSearcher;
	private String name;
	private String password;
	public PlunkHttpLoader getHttpLoader() {
		return httpLoader;
	}
	public GameStorerSearcher getPenteOrgGameSearcher() {
		return penteOrgGameSearcher;
	}
	public boolean login(boolean loadFromDb) {

		boolean valid = true;
		try
		{
			String localName = null;
			String localPassword = null;

			PlunkProp nameProp = null;
			PlunkProp passwordProp = null;
			if (loadFromDb) {
				nameProp = plunkDbUtil.loadProp("name");
				passwordProp = plunkDbUtil.loadProp("password");
			}

			if (loadFromDb && nameProp != null && passwordProp != null) {
				localName = (String) nameProp.getValue();
				localPassword = (String) passwordProp.getValue();
				//System.out.println("stored password=" + password);
			} else {
				String lp[] = getLoginFromUser();
				if (lp == null) {
					return false;
				}
				else {
					localName = lp[0];
					localPassword = lp[1];
				}
			}

			StringBuffer encryptedPasswordBuf = new StringBuffer();
			StringBuffer sessionIdBuf = new StringBuffer();
			httpLoader = new PlunkHttpLoader(getHost(), 80, localName, localPassword);
			int loginStatus = 500;
			try {
				loginStatus = httpLoader.remoteLogin(encryptedPasswordBuf, sessionIdBuf);
			} catch (Exception e) {
				System.err.println("Error logging in to pente.org");
				e.printStackTrace();
			}

			if (loginStatus == 404) {
				JOptionPane.showMessageDialog(frame, "Invalid Login: name or password incorrect", "Login Error", JOptionPane.ERROR_MESSAGE);
				valid = login(false); //recursion
			}
			else if (loginStatus == 200) {
				nameProp = new PlunkProp("name", localName);
				localPassword = encryptedPasswordBuf.toString();
				passwordProp = new PlunkProp("password", localPassword);
				plunkDbUtil.storeProp(nameProp);
				plunkDbUtil.storeProp(passwordProp);

				valid = true;

				name = localName;
				password = localPassword;

				penteOrgGameSearcher = new HttpGameStorerSearcher(
					getHost(), 80, new PGNGameFormat(),
					"/gameServer/controller", name, password);
				((HttpGameStorerSearcher) penteOrgGameSearcher).setCookie(sessionIdBuf.toString());
			}
			else if (loginStatus == 500) {
				JOptionPane.showMessageDialog(frame, "Error logging into pente.org, try again later", "Login Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}

		} catch (Exception e) {
			//TODO error dialog
			e.printStackTrace();
		}

		return valid;
	}

	private String[] getLoginFromUser() {
		JPanel login = new JPanel();
		login.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		gbc.gridwidth = 2;
		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		login.add(new JLabel("Login to pente.org"), gbc);

		JTextField name = new JTextField();
		gbc.gridy++;
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		login.add(new JLabel("Name:"), gbc);
		gbc.gridx = 2;
		login.add(name, gbc);

		JPasswordField password = new JPasswordField();
		gbc.gridy++;
		gbc.gridx = 1;
		login.add(new JLabel("Password:"), gbc);
		gbc.gridx = 2;
		login.add(password, gbc);
		//encrypt password here?
		//if so use different key than for pente.org site normal login
		int r = JOptionPane.showConfirmDialog(frame, login, "Login", JOptionPane.OK_CANCEL_OPTION);

		if (r == JOptionPane.CANCEL_OPTION) {
			return null;
		}
		else {
			return  new String[] { name.getText(), new String(password.getPassword()) };
		}
	}

}
