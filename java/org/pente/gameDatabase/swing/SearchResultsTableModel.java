package org.pente.gameDatabase.swing;

import java.text.*;
import java.util.*;

import javax.swing.table.*;

import org.pente.game.GameData;
import org.pente.gameDatabase.*;

import org.pente.gameServer.core.AlphaNumericGridCoordinates;
import org.pente.gameServer.core.GridCoordinates;

/**
 * @author dweebo
 */
public class SearchResultsTableModel extends AbstractTableModel {

	private String[] columnNames = {
		"Moves", "Games", "Wins" };
	private Object[][] data = null;


    private static final GridCoordinates coordinates =
		new AlphaNumericGridCoordinates(19, 19);

	class CoordinateData implements Comparable<CoordinateData> {
		private int move;
		public CoordinateData(int move) {
			this.move = move;
		}
		public int getMove() {
			return move;
		}
		public String toString() {
			if (move == -1) return "Total";
			else if (move < 0 || move > 361) return "Bad";
			else return coordinates.getCoordinate(move);
		}
		public int compareTo(CoordinateData d) {
			return d.toString().compareTo(toString());
		}
	}
	class IntegerData implements Comparable<IntegerData> {
		private int data;
		private final NumberFormat NF =
			NumberFormat.getInstance();
		public IntegerData(int data) {
			this.data = data;
		}
		public int compareTo(IntegerData d) {
			return d.data - data;
		}
		public String toString() {
			return NF.format(data);
		}
	}
	class PercentData implements Comparable<PercentData> {
		private double data;
		private final NumberFormat NF =
			NumberFormat.getPercentInstance();
		public PercentData(double data) {
	        NF.setMaximumFractionDigits(1);
			this.data = data;
		}
		public int compareTo(PercentData d) {
			return new Double(d.data).compareTo(new Double(data));
		}
		public String toString() {
			return NF.format(data);
		}
	}

	public SearchResultsTableModel() {
	}

	public synchronized void clearResults() {
		if (data == null) return;
		int rows = data.length;
		data = null;
		fireTableRowsDeleted(0, rows);
	}
	public synchronized void setResults(Vector results, int moves[]) {

		double totalGames = 0;
		double totalWins = 0;

		data = new Object[results.size() + 1][columnNames.length];
		for (int i = 0; i < results.size(); i++) {
			GameStorerSearchResponseMoveData d = (GameStorerSearchResponseMoveData)
				results.get(i);

			data[i][0] = new CoordinateData(moves[i]);
			data[i][1] = new IntegerData(d.getGames());
			data[i][2] = new PercentData(d.getPercentage());

			totalGames += d.getGames();
			totalWins += d.getWins();
		}

		data[results.size()][0] = new CoordinateData(-1);
		data[results.size()][1] = new IntegerData((int)totalGames);
		double totalP = totalGames == 0 ? 0 : totalWins / totalGames;
		data[results.size()][2] = new PercentData(totalP);

		fireTableRowsInserted(0, getRowCount());
	}

	public int getMove(int index) {
		if (data == null) {
			return -1;
		}
		CoordinateData d = (CoordinateData) data[index][0];
		return d.getMove();
	}
	public int getRow(int move) {
		if (data == null) {
			return -1;
		}
		for (int i = 0; i < data.length - 1; i++) {
			CoordinateData d = (CoordinateData) data[i][0];
			if (d.getMove() == move) return i;
		}
		return -1;
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
	        return data[row][col];
		}
    }
	public Class getColumnClass(int c) {
		Object val = getValueAt(0, c);
		if (val == null) return null;
		else return val.getClass();
    }
}
