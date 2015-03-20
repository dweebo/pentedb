package org.pente.gameDatabase.swing;

import java.util.*;

import javax.swing.table.*;

/**
 * @author dweebo
 */
public class ExportTreeTableModel extends AbstractTableModel {

    private String[] columnNames = { "", "Game Analysis", "ID" };
	private Object[][] data = null;

	private List<PlunkTree> trees;

	public ExportTreeTableModel() {
	}

	public synchronized void setData(List<PlunkTree> trees) {
		this.trees = trees;
		Collections.sort(trees, new Comparator<PlunkTree>() {
			public int compare(PlunkTree t1, PlunkTree t2) {
				return t1.getName().toLowerCase().compareTo(t2.getName().toLowerCase());
			}
		});

		data = new Object[trees.size()][columnNames.length];
		for (int i = 0; i < trees.size(); i++) {
			PlunkTree t = trees.get(i);
			data[i][0] = false;
			data[i][1] = t.getName();
			data[i][2] = t.getTreeId();
		}
		fireTableRowsInserted(0, getRowCount());
	}

    public boolean isCellEditable(int row, int col) {
        if (col == 0) {
            return true;
        } else {
            return false;
        }
    }

    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    public int getColumnCount() {
        return columnNames.length - 1;
    }

    public synchronized int getRowCount() {
		if (data == null) return 0;
		else return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public synchronized Object getValueAt(int row, int col) {
		if (data == null) {
			return null;
		}
		else {
	        return data[row][col];
		}
    }
    public List<PlunkTree> getSelectedTrees() {
    	List<PlunkTree> t = new ArrayList<PlunkTree>();
    	for (int i = 0; i < data.length; i++) {
    		if ((Boolean) data[i][0]) {
    			t.add(trees.get(i));
    		}
    	}
    	return t;
    }

	public Class getColumnClass(int c) {
		Object val = getValueAt(0, c);
		if (val == null) return null;
		else return val.getClass();
    }

}
