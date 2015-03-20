package org.pente.gameDatabase.swing;

import java.text.*;
import java.util.*;

import javax.swing.table.*;

/**
 * @author dweebo
 */
public class PlunkTreeTableModel extends AbstractTableModel {

    private String[] columnNames = {
		"Name", "Version", "Creator", "Date Modified", "Date Created"};
	private Object[][] data = null;

	private List<PlunkTree> trees;

	public PlunkTreeTableModel() {
	}

	class DateData implements Comparable {
		private Date date;
		private final DateFormat DATE_FORMAT =
			new SimpleDateFormat("MM/dd/yyyy HH:mm");
		public DateData(Date date) {
			this.date = date;
		}
		public int compareTo(Object o) {
			DateData d = (DateData) o;
			return d.date.compareTo(date);
		}
		public String toString() {
			return DATE_FORMAT.format(date);
		}
	}


	public synchronized void setData(List<PlunkTree> trees) {
		this.trees = trees;

		data = new Object[trees.size()][columnNames.length + 1];
		for (int i = 0; i < trees.size(); i++) {
			PlunkTree t = trees.get(i);
			data[i][0] = t.getTreeId();
			data[i][1] = t.getName();
			data[i][2] = t.getVersion();
			data[i][3] = t.getCreator();
			data[i][4] = new DateData(t.getLastModified());
			data[i][5] = new DateData(t.getCreated());
		}
		fireTableRowsInserted(0, getRowCount());
	}


    public int getColumnCount() {
        return columnNames.length;
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
	        return data[row][col + 1];
		}
    }
    public PlunkTree getTree(int row) {
    	return trees.get(row);
    }

	public Class getColumnClass(int c) {
		Object val = getValueAt(0, c);
		if (val == null) return null;
		else return val.getClass();
    }
}
