package org.pente.gameDatabase.swing;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;

import org.pente.game.GameDbData;
import org.pente.gameDatabase.swing.component.MyDialog;
import org.pente.gameDatabase.swing.importer.SGFGameFormat;

/**
 * @author dweebo
 */
public class ExportDialog extends MyDialog implements ProgressListener {

	//private int result = JOptionPane.NO_OPTION;

	public static final int EXPORT_TYPE_LIST = 1;
	public static final int EXPORT_TYPE_OPEN = 2;
	private int exportType;

	private JRadioButton openButton = null;
	private JRadioButton listButton = null;
	ButtonGroup group = null;

	private JTable treeTable;
	private ExportTreeTableModel treeTableModel = new ExportTreeTableModel();
	private JTable dbTable;
	private DbTableModel dbTableModel = new DbTableModel("Game Database");

	private JProgressBar progressBar;
	private JPanel progressPanel;
	private CardLayout progressLayout;

	private Main main;

	private JTextField fileNameField;
	private JButton browseButton;
	//private JButton ok;

	private volatile boolean exporting = false;
	private Thread exportThread;

	public ExportDialog(Frame owner, String tabName, int initExportType,
		List<PlunkTree> trees, List<GameDbData> dbData,
		final Main main) {
		super(owner, "Export", true);

		this.exportType = initExportType;
		this.main = main;

		/*
		if (tabName != null) {
			openButton = new JRadioButton("Export '" + tabName + "'");
			openButton.setToolTipText("Export just the currently opened game/moves.");
			openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					exportType = EXPORT_TYPE_OPEN;
					treeTable.getSelectionModel().clearSelection();
					dbTable.getSelectionModel().clearSelection();
				}
			});

			listButton = new JRadioButton("Export selected moves/games below");
			listButton.setToolTipText("Export all moves/games in selected databases");
			listButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					exportType = EXPORT_TYPE_LIST;
				}
			});

			if (initExportType == EXPORT_TYPE_OPEN) {
				openButton.setSelected(true);
			}
			else {
				listButton.setSelected(true);
			}
			group = new ButtonGroup();
			group.add(openButton);
			group.add(listButton);
		}
		else {
			exportType = EXPORT_TYPE_LIST;
		}
		*/

		treeTableModel.setData(trees);
		treeTable = new JTable(treeTableModel);
		JTableHeader treeHeader = treeTable.getTableHeader();
		treeHeader.setReorderingAllowed(false);
		treeHeader.setResizingAllowed(false);
		treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		treeTable.setShowGrid(false);
		treeTable.setIntercellSpacing(new Dimension(0, 0));
		treeTable.getSelectionModel().addListSelectionListener(listListener);
		treeTableModel.addTableModelListener(tableListener);

		treeTable.getColumnModel().getColumn(0).setMaxWidth(10);
		JScrollPane treeTableScroll = new JScrollPane(treeTable);

		Dimension d = treeHeader.getPreferredSize();
		d.setSize(d.getWidth() + 100, d.getHeight() * 5);
		treeTableScroll.getViewport().setPreferredSize(d);
		treeTableScroll.getViewport().setBackground(Color.white);

		dbTableModel.setData(dbData);
		dbTable = new JTable(dbTableModel);
		JTableHeader dbHeader = dbTable.getTableHeader();
		dbHeader.setReorderingAllowed(false);
		dbHeader.setResizingAllowed(false);
		dbTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dbTable.setShowGrid(false);
		dbTable.setIntercellSpacing(new Dimension(0, 0));
		dbTable.getSelectionModel().addListSelectionListener(listListener);
		dbTable.getColumnModel().getColumn(0).setMaxWidth(10);
		dbTableModel.addTableModelListener(tableListener);

		JScrollPane dbTableScroll = new JScrollPane(dbTable);

		d = dbHeader.getPreferredSize();
		d.setSize(d.getWidth() + 100, d.getHeight() * 5);
		dbTableScroll.getViewport().setPreferredSize(d);
		dbTableScroll.getViewport().setBackground(Color.white);

		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setVisible(false);

		progressPanel = new JPanel();
		progressLayout = new CardLayout();
		progressPanel.setLayout(progressLayout);
		progressPanel.add(new JLabel(""), "dummy");
		progressPanel.add(progressBar, "progress");

		fileNameField = new JTextField(25);
		fileNameField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent arg0) {
				updateOkButton();
			}
		});

		browseButton = new JButton("Browse...");
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();

				String fn = fileNameField.getText();
				if (fn != null && !fn.equals("")) {
					try {
						File f = new File(fn).getCanonicalFile();
						chooser.setSelectedFile(f);
					} catch (IOException ie) {}
				}
				chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
					public boolean accept(File f) {
				          return f.isDirectory() || f.getName().endsWith(".sgf");
					}
					public String getDescription() {
				          return "*.sgf (Smart Game Format)";
					}
				});
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int r = chooser.showSaveDialog(ExportDialog.this);
				if (r == JFileChooser.APPROVE_OPTION) {
					try {
						String fn2 = chooser.getSelectedFile().getCanonicalPath();

						if (!fn2.endsWith(".sgf") && !fn2.endsWith(".SGF")) {
							fn2 += ".sgf";
						}
						fileNameField.setText(fn2);
						updateOkButton();
					} catch (IOException ie) {}
				}
			}
		});

//		ok = new JButton("Ok");
//		ok.setEnabled(false);
//		ok.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//		    	result = JOptionPane.OK_OPTION;
//
//		    	ok.setEnabled(false);
//		    	export();
//		    	ok.setEnabled(true);
//			}
//		});
//		JButton cancel = new JButton("Cancel");
//		cancel.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				result = JOptionPane.CANCEL_OPTION;
//
//				if (exporting) {
//					exporting = false;
//					if (exportThread != null) {
//						exportThread.interrupt();
//						exportThread = null;
//					}
//				}
//				setVisible(false);
//			}
//		});
//		addWindowListener(new WindowAdapter() {
//			@Override
//			public void windowClosed(WindowEvent e) {
//				result = JOptionPane.CANCEL_OPTION;
//				setVisible(false);
//			}
//		});

		JPanel top = new JPanel();
		top.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;

		int y = 1;
		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.gridy = y++;
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		top.add(new JLabel("Select the resources to export:"), gbc);

//		if (tabName != null) {
//			gbc.gridx = 1;
//			gbc.gridy = y++;
//			gbc.gridheight = 1;
//			gbc.gridwidth = 2;
//			gbc.weighty = 1;
//			gbc.weightx = 1;
//
//			top.add(openButton, gbc);
//
//			gbc.gridy = y++;
//			top.add(listButton, gbc);
//		}

//		gbc.weighty = 1;
//		gbc.weightx = 1;
//		gbc.gridy = y++;
//		gbc.gridx = 1;
//		gbc.gridwidth = 1;
//		top.add(new JLabel("Game Databases:"), gbc);
//		gbc.gridx = 2;
//		top.add(new JLabel("Move Databases:"), gbc);

		gbc.gridy = y++;
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.BOTH;
		top.add(dbTableScroll, gbc);
		gbc.gridx = 2;
		gbc.weightx = 1;
		top.add(treeTableScroll, gbc);


		JPanel filePanel = new JPanel();
		filePanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.insets = new Insets(2, 2, 2, 2);
		gbc2.gridx = 1;
		gbc2.gridy = 1;
		gbc2.weightx = 1;
		filePanel.add(new JLabel("File:"), gbc2);

		gbc2.gridx++;
		gbc2.weightx = 99;
		gbc2.fill = GridBagConstraints.HORIZONTAL;
		filePanel.add(fileNameField, gbc2);

		gbc2.gridx++;
		gbc2.weightx = 1;
		gbc2.fill = GridBagConstraints.NONE;
		filePanel.add(browseButton, gbc2);


		gbc.gridy++;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		top.add(new JLabel(" "), gbc);
		gbc.gridy++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		top.add(new JLabel("Select the export destination:"), gbc);

		gbc.gridy++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		top.add(filePanel, gbc);


		gbc.gridy++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		top.add(progressPanel, gbc);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		buttonPanel.add(ok);
		buttonPanel.add(cancel);
		gbc.gridy++;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		top.add(buttonPanel, gbc);

		top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		getContentPane().add(top, BorderLayout.CENTER);

		ok.setEnabled(false);

		pack();

		centerDialog(owner);
		setVisible(true);
	}

	protected Action getOkAction() {
		return new AbstractAction("Ok") {
			public void actionPerformed(ActionEvent e) {
		    	result = JOptionPane.OK_OPTION;

		    	ok.setEnabled(false);
		    	export();
		    	ok.setEnabled(true);
			}
		};
	}
	protected Action getCancelAction() {
		return new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent e) {
				result = JOptionPane.CANCEL_OPTION;

				if (exporting) {
					exporting = false;
					if (exportThread != null) {
						exportThread.interrupt();
						exportThread = null;
					}
				}
				setVisible(false);
			}
		};
	}
//    private void centerDialog(Frame frame) {
//
//        Point location = new Point();
//        location.x = frame.getLocation().x +
//                     (frame.getSize().width + frame.getInsets().right - frame.getInsets().left) / 2 -
//                     getSize().width / 2;
//        location.y = frame.getLocation().y +
//                     (frame.getSize().height + frame.getInsets().top - frame.getInsets().bottom) / 2 -
//                     (getSize().height + getInsets().top - getInsets().bottom) / 2;
//        setLocation(location);
//    }
//
//	public int getResult() {
//		return result;
//	}
	public int getExportType() {
		return exportType;
	}

	TableModelListener tableListener = new TableModelListener() {
		public void tableChanged(TableModelEvent e) {
			updateOkButton();
		};
	};
	private void updateOkButton() {
		if (!fileNameField.getText().trim().equals("") &&
			(!dbTableModel.getSelectedDbs().isEmpty() ||
			 !treeTableModel.getSelectedTrees().isEmpty())) {

			ok.setEnabled(true);
		}
		else {
			ok.setEnabled(false);
		}
	}

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

	private void export() {

		File f = null;
		String fn = fileNameField.getText();
		if (fn != null && !fn.equals("")) {
			try {
				f = new File(fn).getCanonicalFile();
			} catch (IOException ie) {}
		}
		if (f == null || (f.exists() && !f.canWrite())) {
			JOptionPane.showMessageDialog(this, "Invalid Export Destination", "Invalid Export Destination", JOptionPane.ERROR_MESSAGE);
			return;
		}
		//TODO prompt if want to overwrite existing file
		//TODO check exporting boolean and remove any partially written files

		final File outFile = f;

		//if (exportType == ExportDialog.EXPORT_TYPE_OPEN) {
			//TODO
			//TabComponent c = (TabComponent) tabs.getSelectedComponent();
			//c.export(f);
		//}
		//else {
			final SGFGameFormat gf = new SGFGameFormat("\r\n");

			final List<PlunkTree> trees = treeTableModel.getSelectedTrees();
			final List<GameDbData> dbs = dbTableModel.getSelectedDbs();

			try {
				//get number of trees + games
				int max = trees.size();
				for (PlunkTree t : trees) {
					max += 2 * main.getPlunkDbUtil().getNumNodesInTree(t.getTreeId());
				}
				for (GameDbData db : dbs) {
					max += 2 * main.getPlunkDbUtil().getNumGames(db.getID());
				}
				progressBar.setMaximum(max);
				progressBar.setVisible(true);
				progressLayout.show(progressPanel, "progress");

			} catch (SQLException s) {
				s.printStackTrace();
				return;
			}

			exportThread = new Thread(new Runnable() {
				public void run() {

					FileWriter out = null;

					try {
						out = new FileWriter(outFile);
						for (PlunkTree t : trees) {

							if (t.getRoot() == null) { //if not loaded from db already
								t.setRoot(main.getPlunkDbUtil().loadPlunkTree(t.getTreeId(), ExportDialog.this));
							}
							gf.format(t.getRoot(), 1, //TODO add game to plunktree
								t, out);
							out.write("\n\n");

							updateProgress();
						}

						final Writer out2 = out;
						for (final GameDbData db : dbs) {
							main.getPlunkDbUtil().loadAllGames(
								main.getVenueStorer(), db.getID(),
								new LoadGameListener() {
									public void gameLoaded(PlunkGameData g) {
										updateProgress();
										System.out.println("format gm " + g.getGameID());
										try {
											gf.format(g, db.getName(), out2);
											out2.write("\n\n");
											out2.flush();
										} catch (Throwable t) {
											System.err.println("Failed to export game " + g.getGameID());
											t.printStackTrace();
										}
										updateProgress();
									}
								}
							);
						}
					}
					catch (Exception ex) {
						ex.printStackTrace();
					} finally {
						if (out != null) {
							try { out.close(); } catch (IOException ie) {}
						}

						setVisible(false);
					}
				}
			});
			exportThread.start();
		//}
	}
	public void updateProgress() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue(progressBar.getValue() + 1);
			}
		});
	}
}
