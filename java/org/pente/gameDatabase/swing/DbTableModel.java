package org.pente.gameDatabase.swing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.pente.game.GameDbData;

/**
 * @author dweebo
 */
public class DbTableModel extends AbstractTableModel {

    private String[] columnNames = null;
	private Object[][] data = null;

	private List<GameDbData> dbData;

	public DbTableModel(String headerName) {
		columnNames = new String[] { "", headerName, "ID" };
	}

	public synchronized void setData(List<GameDbData> dbData) {
		this.dbData = dbData;

		for (int i = 0; i < dbData.size(); i++) {
			GameDbData d = dbData.get(i);
			if (d.getID() == 2) {
				dbData.remove(i);
				break;
			}
		}
		Collections.sort(dbData, new Comparator<GameDbData>() {
			public int compare(GameDbData d1, GameDbData d2) {
				return d1.getName().toLowerCase().compareTo(d2.getName().toLowerCase());
			}
		});

		data = new Object[dbData.size()][columnNames.length];
		for (int i = 0; i < dbData.size(); i++) {
			GameDbData d = dbData.get(i);
			data[i][0] = false;
			data[i][1] = d.getName();
			data[i][2] = d.getID();
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
    public List<GameDbData> getSelectedDbs() {
    	List<GameDbData> d = new ArrayList<GameDbData>();
    	for (int i = 0; i < data.length; i++) {
    		if ((Boolean) data[i][0]) {
    			d.add(dbData.get(i));
    		}
    	}
    	return d;
    }

	public Class getColumnClass(int c) {
		Object val = getValueAt(0, c);
		if (val == null) return null;
		else return val.getClass();
    }
}
