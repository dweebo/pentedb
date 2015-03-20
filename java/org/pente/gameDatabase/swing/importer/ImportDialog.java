package org.pente.gameDatabase.swing.importer;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

import javax.swing.*;
//import javax.swing.event.ListSelectionEvent;
//import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;

import org.pente.gameDatabase.swing.PlunkGameData;
import org.pente.gameDatabase.swing.PlunkTree;
import org.pente.gameDatabase.swing.TableSorter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.pente.game.*;
import org.pente.gameDatabase.swing.*;
import org.pente.gameServer.core.AlphaNumericGridCoordinates;
import org.pente.gameServer.core.GridCoordinates;
import org.pente.gameDatabase.swing.component.*;

/**
 * @author dweebo
 */
public class ImportDialog extends MyDialog {

    private static final GridCoordinates coordinates =
		new AlphaNumericGridCoordinates(19, 19);
	//private int result = JOptionPane.NO_OPTION;

	private ImportTableModel importModel;
	private JTable importTable;

	private JProgressBar filesProgressBar;
	private JLabel filesStatusLabel;

	private JProgressBar progressBar;
	private JLabel statusLabel;
	private int filesToLoad = 0;
	private int loadedFiles = 0;
	private int failedFiles = 0;
	private int loadedObjects = 0;

	private Thread importThread;
	private volatile boolean importing = false;

	private Main main;

	public ImportDialog(Frame owner, Main main, File filesAndDirs[], List<GameImporter> gameImporters) {
		super(owner, "Import", true);

		this.main = main;
		importModel = new ImportTableModel();
		final TableSorter importSorter = new TableSorter(importModel);
		importTable = new JTable(importSorter);
		JTableHeader importHeader = importTable.getTableHeader();
		importHeader.setReorderingAllowed(true);
		importSorter.setTableHeader(importTable.getTableHeader());
		importSorter.setSortingStatus(1, TableSorter.ASCENDING);
		importTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		importTable.setShowGrid(true);
		//treeTable.setIntercellSpacing(new Dimension(0, 0));
		//treeTable.getSelectionModel().addListSelectionListener(listListener);

		JScrollPane importTableScroll = new JScrollPane(importTable);

		Dimension d = importHeader.getPreferredSize();
		d.setSize(d.getWidth() + 100, d.getHeight() * 5);
		importTableScroll.getViewport().setPreferredSize(d);
		importTableScroll.getViewport().setBackground(Color.white);

		ArrayList<File> files = new ArrayList<File>();

		for (File f : filesAndDirs) {
			if (f.isFile()) {
				files.add(f);
			}
			else if (f.isDirectory()) {
				for (File f2 : f.listFiles()) {
					files.add(f2);
				}
			}
		}
		int num = 0;
		for (File f : files) {
			if (f.exists() && f.canRead()) {
				num++;
			}
		}
		if (num == 0) {
			JOptionPane.showMessageDialog(this, "Invalid file(s).", "Import Failed", JOptionPane.ERROR_MESSAGE);
			return;
		}
		filesToLoad = files.size();
		if (files.size() > 1) {
			filesProgressBar = new JProgressBar(0, num);
			filesProgressBar.setValue(0);
			filesProgressBar.setStringPainted(true);
			filesStatusLabel = new JLabel("Loading File: 1 of " + files.size());
		}


		progressBar = new JProgressBar(0, 1);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		statusLabel = new JLabel("");

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;

		int y = 1;

		if (filesToLoad > 1) {
			gbc.weighty = 1;
			gbc.weightx = 1;
			gbc.gridy = y++;
			gbc.gridx = 1;
			gbc.gridwidth = 1;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.NONE;
			panel.add(filesStatusLabel, gbc);

			gbc.weightx = 99;
			gbc.gridx = 2;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			panel.add(filesProgressBar, gbc);
		}

		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.gridy = y++;
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(statusLabel, gbc);

		gbc.weightx = 99;
		gbc.gridx = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(progressBar, gbc);

		gbc.weighty = 99;
		gbc.weightx = 100;
		gbc.gridy = y++;
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(importTableScroll, gbc);

		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		p.add(ok);
		p.add(cancel);

		gbc.weighty = 1;
		gbc.weightx = 100;
		gbc.gridy = y++;
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		panel.add(p, gbc);

		getContentPane().add(panel, BorderLayout.CENTER);

		pack();

		centerDialog(owner);

		startImport(files, gameImporters);

		setVisible(true);
	}


	protected Action getCancelAction() {
		return new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent e) {
				result = JOptionPane.CANCEL_OPTION;
				importing = false;
				if (importThread != null) {
					importThread.interrupt();
				}
				setVisible(false);
			}
		};
	}

	private void startImport(final ArrayList<File> files, final List<GameImporter> importers) {

		importThread = new Thread(new Runnable() {
			public void run() {
				MyProgressInputStream in = null;
				try
            	{
					for (int i = 0; i < files.size(); i++) {
						final File f = files.get(i);
						boolean ok = false;

						if (!f.canRead()) {
							ImportData id = new ImportData();
							id.setName("");
							id.setFileName(f.getName());
							id.setType("");
							id.setStatus("Failed: Can't read file");
							addImportData(id);
							failedFiles++;
							updateFilesStatus();
							continue;
						}

						for (GameImporter gi : importers) {
							if (!importing) return;
							resetProgress(f);
							in = new MyProgressInputStream(progressBar, new FileInputStream(f));
							if (gi.attemptImport(in, new GameImporterListener() {
								public void analysisRead(PlunkTree t, String importerName) {

									if (!importing) return;
									ImportData id = new ImportData();
									id.setName(t.getName());
									id.setFileName(f.getName());
									id.setType("Analysis/"+importerName);
									try {
										main.getPlunkDbUtil().storePlunkTree(t);
										main.getPlunkDbUtil().insertPlunkNodes(t.getRoot(), t.getTreeId());
										main.addPlunkTree(t);

										id.setStatus("Saved");
										loadedObjects++;

									} catch (SQLException s) {
										id.setStatus("Failed: Db error");
										s.printStackTrace();
									}
									addImportData(id);
								}

								public void gameRead(PlunkGameData g, String importerName) {
									if (!importing) return;
									ImportData id = new ImportData();
									id.setName(Utilities.getGameName(g));
									id.setDb(g.getDbName());
									id.setFileName(f.getName());
									id.setType("Game/"+importerName);
									System.out.println("game read " + id.getName());
									if (g.getNumMoves()==0) {
										id.setStatus("Failed: No moves");
									}
									else if (g.getMove(0) != 180) {
										id.setStatus("Failed: Bad 1st Move " + coordinates.getCoordinate(g.getMove(0)));
									}
									else {

							            if (g.getSite() == null || g.getSite().equals("")) {
							            	g.setSite("Unknown");
							            }

										//helps identify the game
										if (importerName.equals("VBarykin") && g.getEvent() == null) {
											g.setEvent(f.getName());
										}
										else if (g.getEvent() == null || g.getEvent().equals("")) {
							            	g.setEvent("Unknown");
							            }

										try {
											if (g.getDbName() == null || g.getDbName().equals("")) {
												g.setDbName("My " + g.getGame() + " Games");
												id.setDb(g.getDbName());
											}
								            if (g.getPlayer1Data() == null || g.getPlayer1Data().getUserIDName() == null) {
								            	PlayerData p1 = new DefaultPlayerData();
								            	p1.setUserIDName("Unknown");
								            	g.setPlayer1Data(p1);
								            }
								            if (g.getPlayer2Data() == null || g.getPlayer2Data().getUserIDName() == null) {
								            	PlayerData p2 = new DefaultPlayerData();
								            	p2.setUserIDName("Unknown");
								            	g.setPlayer2Data(p2);
								            }

											// find db or create a new one
											List<GameDbData> dbs = main.getVenueStorer().getDbTree();
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
												main.getVenueStorer().addGameDbData(db,
													GridStateFactory.getGameId(g.getGame()));
											}

											if (g.getRoot() == null) {
												g.setRoot(Utilities.convertGame(g));
											}

											if (main.getGameStorer().gameAlreadyStored(g, db.getID())) {
												id.setStatus("Skipped: already stored");
											}
											else {
												main.saveGame(g, db, null);
												id.setStatus("Saved");
											}
											loadedObjects++;

										} catch (Exception e) {
											id.setStatus("Failed: Db error");
											e.printStackTrace();
										}
									}

									addImportData(id);
								}
							})) {
								ok = true;
								loadedFiles++;
								updateFilesStatus();
								updateStatus();
								break;
							}
						}
						if (!ok) {
							failedFiles++;
							ImportData id = new ImportData();
							id.setFileName(f.getName());
							id.setStatus("Failed: Unknown import format");
							id.setType("");
							addImportData(id);
							updateFilesStatus();
							updateStatus();
						}
						in.close();
					}
				}
				catch (Throwable t) {
					t.printStackTrace();
				}
				finally {
					if (in != null) {
						try { in.close(); } catch (IOException i) {}
					}
				}
			}
		});
		importing = true;
		importThread.start();
	}

	private void updateFilesStatus() {
		if (filesToLoad < 2) return;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int total = failedFiles + loadedFiles;
				if (total < filesProgressBar.getMaximum()) {
					filesProgressBar.setValue(total);
					filesStatusLabel.setText("Loading File: " + total + " of " + filesToLoad);
				}
				else {
					filesProgressBar.setVisible(false);
					String text = "Loaded: " + loadedObjects + " objects in " +
						filesToLoad + " files.";
					if (failedFiles != 0) {
						text += " Failed files: " + failedFiles;
					}
					filesStatusLabel.setText(text);
					ok.setEnabled(true);
					progressBar.setVisible(false);
					statusLabel.setVisible(false);
				}
			}
		});
	}
	private void resetProgress(final File f) {
		//System.out.println("resetprogress");
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				System.out.println("resetprogress 2");
				progressBar.setMaximum((int)f.length());//TODO f.length is long
				progressBar.setValue(0);
				statusLabel.setText("Loading " + f.getName());
			}
		});
	}

	private void updateStatus() {
		if (filesToLoad != 1) return;
		//System.out.println("update status " + progressBar.getValue() + progressBar.getMaximum());
		if (filesToLoad == 1 && progressBar.getValue() == progressBar.getMaximum()) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					//System.out.println("update status 2");
					//	System.out.println("update status 3");
					progressBar.setVisible(false);
					String text = "Loaded: " + loadedObjects + " objects.";
					statusLabel.setText(text);
					ok.setEnabled(true);
				}
			});
		}
	}

	private void addImportData(final ImportData id) {
		//System.out.println("add import data");
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				System.out.println("add import data 2");
				importModel.addData(id);

				/*
				if (progress < progressBar.getMaximum()) {
					progressBar.setValue(progress);
					statusLabel.setText("Loading: " + progress + " of " + progressBar.getMaximum());
				}
				else {
					progressBar.setVisible(false);
					String text = "Loaded: " + loadedObjects + " objects in " +
						progressBar.getMaximum() + " file" + (progressBar.getMaximum() > 1 ? "s" : "") + ".";
					if (failedFiles != 0) {
						text += " Failed files: " + failedFiles;
					}
					statusLabel.setText(text);
					ok.setEnabled(true);
				}*/
			}
		});
	}
/*


	ListSelectionListener listListener = new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
	        //Ignore extra messages.
	        if (e.getValueIsAdjusting()) return;

	        ListSelectionModel lsm =
	            (ListSelectionModel)e.getSource();
	        if (openButton != null && openButton.isSelected() &&
	        	!lsm.isSelectionEmpty()) {

	        	openButton.setSelected(false);
	        	listButton.setSelected(true);
	        	exportType = EXPORT_TYPE_LIST;
	        }
	    }
	};
	*/
}
