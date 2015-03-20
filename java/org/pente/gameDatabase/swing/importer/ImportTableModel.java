package org.pente.gameDatabase.swing.importer;

import java.util.*;

import javax.swing.table.*;

/**
 * @author dweebo
 */
public class ImportTableModel extends AbstractTableModel {

    private String[] columnNames = { "File Name", "Name", "Type", "Db", "Status" };

	private List<ImportData> importData= new ArrayList<ImportData>();

	public ImportTableModel() {
	}

	public synchronized void addData(ImportData o) {
		importData.add(o);

		fireTableRowsInserted(getRowCount(), getRowCount());
	}


    public int getColumnCount() {
        return columnNames.length;
    }

    public synchronized int getRowCount() {
		return importData.size();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public synchronized Object getValueAt(int row, int col) {
		ImportData o = importData.get(row);
		switch (col) {
		case 0: return o.getFileName();
		case 1: return o.getName();
		case 2: return o.getType();
		case 3: return o.getDb();
		case 4: return o.getStatus();
		}
		return "";
    }
    public ImportData getImportData(int row) {
    	return importData.get(row);
    }

	public Class getColumnClass(int c) {
		return String.class;
    }
}
