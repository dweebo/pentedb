package org.pente.gameDatabase.swing;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.JTableHeader;

import java.util.List;

import org.pente.gameDatabase.swing.component.*;

//don't show specific column selection, only row
/**
 * @author dweebo
 */
public class OpenDialog extends MyDialog {

	private PlunkTree selectedTree;
	private PlunkTreeTableModel plunkTreeTableModel;
	private JTable treeTable;
	private TableSorter treeSorter;
	public OpenDialog(Frame owner, List<PlunkTree> trees) {
		super(owner, "Open Game Analysis", true);

		plunkTreeTableModel = new PlunkTreeTableModel();
		plunkTreeTableModel.setData(trees);
		treeSorter = new TableSorter(plunkTreeTableModel);
		treeTable = new JTable(treeSorter);
		JTableHeader treeHeader = treeTable.getTableHeader();
		treeHeader.setReorderingAllowed(true);
		treeSorter.setTableHeader(treeTable.getTableHeader());
		treeSorter.setSortingStatus(1, TableSorter.ASCENDING);
		treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		treeTable.setShowGrid(false);
		treeTable.setIntercellSpacing(new Dimension(0, 0));
		if (trees.size() > 0) {
			treeTable.getSelectionModel().setSelectionInterval(0, 0);
		}
		treeTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() != 2) return;
			    JTable target = (JTable) e.getSource();
			    int i = target.getSelectedRow();
			    if (i != -1) {
			    	selectedTree = plunkTreeTableModel.getTree(treeSorter.modelIndex(i));
					result = JOptionPane.OK_OPTION;
					setVisible(false);
			    }
			}
		});
		treeTable.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int iKey = e.getKeyCode();
				if (iKey == KeyEvent.VK_ENTER) {
					JTable target = (JTable) e.getSource();
				    int i = target.getSelectedRow();
				    if (i != -1) {
				    	selectedTree = plunkTreeTableModel.getTree(treeSorter.modelIndex(i));
						result = JOptionPane.OK_OPTION;
						setVisible(false);
				    }
				}
			}
		});

		JScrollPane treeTableScroll = new JScrollPane(treeTable);

		Dimension d = treeHeader.getPreferredSize();
		d.setSize(d.getWidth() + 100, d.getHeight() * 5);
		treeTableScroll.getViewport().setPreferredSize(d);
		treeTableScroll.getViewport().setBackground(Color.white);

		JButton ok = new JButton("Ok");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    int i = treeTable.getSelectedRow();
			    if (i != -1) {
			    	selectedTree = plunkTreeTableModel.getTree(treeSorter.modelIndex(i));
			    	result = JOptionPane.OK_OPTION;
					setVisible(false);
			    }
			}
		});

		JPanel top = new JPanel();
		top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		top.setLayout(new BorderLayout());
		top.add(treeTableScroll, BorderLayout.CENTER);
		getContentPane().add(top, BorderLayout.NORTH);


		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		p.add(ok);
		p.add(cancel);

		getContentPane().add(p, BorderLayout.SOUTH);
		pack();

		centerDialog(owner);
		setVisible(true);
	}

	protected Action getOkAction() {
		return new AbstractAction("Ok") {
			public void actionPerformed(ActionEvent e) {
			    int i = treeTable.getSelectedRow();
			    if (i != -1) {
			    	selectedTree = plunkTreeTableModel.getTree(treeSorter.modelIndex(i));
			    	result = JOptionPane.OK_OPTION;
					setVisible(false);
			    }
			}
		};
	}

	public PlunkTree getSelectedTree() {
		return selectedTree;
	}
}