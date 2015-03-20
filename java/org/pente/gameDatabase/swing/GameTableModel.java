package org.pente.gameDatabase.swing;

import java.text.*;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.ArrayList;

import javax.swing.table.*;

import org.pente.game.*;

/**
 * @author dweebo
 */
public class GameTableModel extends AbstractTableModel {
//TODO add round/section now that isn't a size problem
    private String[] columnNames = {
		"Player 1", "Rating", "Player 2", "Rating", "Site", "Event", "Date", "Rated", "Timer"};
	private ArrayList<ArrayList<Object>> data = null;
	private List<PlunkGameData> games;


	public GameTableModel() {
	}

	private String getShortSite(String site) {
		if (site.startsWith("Pen")) return "Pente.org";
		else if (site.startsWith("Bra")) return "BrainKing";
		else if (site.startsWith("It's")) return "IYT";
		else if (site.startsWith("Rich")) return "PBeM";
		else return site;
	}

	class TimerData implements Comparable {
		private boolean timed;
		private int init;
		private int incre;
		public TimerData(boolean timed, int init, int incre) {
			this.timed = timed;
			this.init = init;
			this.incre = incre;
		}
		public int compareTo(Object o) {
			TimerData t = (TimerData) o;
			if (t.timed && !timed) return -1;
			else if (!t.timed && timed) return 1;
			else if (t.init == init) return t.incre - incre;
			else return t.init - init;
		}
		public String toString() {
			return timed ? init + "+" + incre : "No";
		}
	}
	class PlayerData implements Comparable {
		private String player;
		private boolean winner;
		public PlayerData(String player, boolean winner) {
			this.player = player;
			this.winner = winner;
		}
		public int compareTo(Object o) {
			PlayerData p = (PlayerData) o;
			return p.player.compareTo(player);
		}
		public String toString() {
			return winner ?  "<html><font color=red>" + player + "</font></html>" :
				player;
		}
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

	public synchronized void clearGames() {
		if (games == null) return;
		int len = games.size();
		games = null;
		data = null;
		fireTableRowsDeleted(0, len);
	}
	public synchronized void setGames(Vector g) {
		this.games = new ArrayList<PlunkGameData>(g.size());
		for (int i = 0; i < g.size(); i++) {
			Object o = g.get(i);
			if (o instanceof PlunkGameData) {
				this.games.add((PlunkGameData)o);
			} else {
				PlunkGameData p = new PlunkGameData((GameData)o);
				p.setStored(false);
				p.setEditable(false);
				this.games.add(p);
			}
			//this.games.add((PlunkGameData) g.get(i));
		}

		data = new ArrayList<ArrayList<Object>>();//[games.size()][columnNames.length];
		for (int i = 0; i < games.size(); i++) {
			GameData game = (GameData) games.get(i);
			cacheData(game);
		}
		fireTableRowsInserted(0, getRowCount());
	}
	public synchronized void deleteGame(GameData g) {

		for (int i = 0; i < games.size(); i++) {
			GameData g2 = (GameData) games.get(i);
			if (g.getGameID() == g2.getGameID()) {
				data.remove(i);
				games.remove(i);
				fireTableRowsDeleted(i, i);
				break;
			}
		}
	}


	private void cacheData(GameData game) {
		ArrayList<Object> objs = new ArrayList<Object>();

		objs.add(new PlayerData(
			game.getPlayer1Data().getUserIDName(), game.getWinner() == 1));
		objs.add(new Integer(game.getPlayer1Data().getRating()));
		objs.add(new PlayerData(
			game.getPlayer2Data().getUserIDName(), game.getWinner() == 2));
		objs.add(new Integer(game.getPlayer2Data().getRating()));
		objs.add(getShortSite(game.getSite()));
		objs.add(game.getEvent());
		objs.add(new DateData(game.getDate()));
		objs.add(game.getRated()? "Yes" : "No");
		objs.add(new TimerData(game.getTimed(),
						game.getInitialTime(),
						game.getIncrementalTime()));

		data.add(objs);
	}
	public PlunkGameData getGame(int index) {
		if (games == null) return null;
		return (PlunkGameData) games.get(index);
	}

    public int getColumnCount() {
        return columnNames.length;
    }

    public synchronized int getRowCount() {
		if (data == null) return 0;
		else return data.size();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public synchronized Object getValueAt(int row, int col) {
		if (data == null) {
			return null;
		}
		else {
	        return data.get(row).get(col);
		}
    }
	public Class getColumnClass(int c) {
		Object val = getValueAt(0, c);
		if (val == null) return null;
		else return val.getClass();
    }

	public void updateGame(GameData d) {
		for (int i = 0; i < games.size(); i++) {
			GameData g = games.get(i);
			if (g.getGameID() == d.getGameID()) {
				cacheData(g);
				break;
			}
		}
	}
}
