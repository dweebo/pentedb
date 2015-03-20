package org.pente.gameDatabase.swing.importer;

import java.io.InputStream;

import org.pente.game.*;
import org.pente.gameDatabase.swing.PlunkGameData;

/**
 * @author dweebo
 */
public class VBaryKinGameImporter extends GameImporterAdapter {

	public boolean attemptImport(InputStream in, GameImporterListener l) {
		boolean imported = false;

		try {

			int numMoves = in.read();
			if (numMoves == -1) return false;
			if (in.read() == -1) return false;
			//System.out.println("nummoves="+numMoves);
			int moves[] = new int[361];
			int cnt = 0;
			while (true) {
				int x = in.read();
				if (x == -1) break;
				int y = in.read();
				if (y == -1) break;
				moves[cnt++] = (19 - y) * 19 + (x - 1);
			}
			if (cnt == numMoves) {
				imported = true;
				for (int i = 0; i < numMoves; i++) {
					if (moves[i] < 0 && moves[i] > 362) {
						imported = false;
						break;
					}
				}
				if (imported) {
					PlunkGameData gd = new PlunkGameData();
					gd.setGame(GridStateFactory.PENTE_GAME.getName());
					gd.setEditable(true);
					for (int i = 0; i < numMoves; i++) {
						gd.addMove(moves[i]);
					}
					GridState gs = GridStateFactory.createGridState(
						GridStateFactory.PENTE, gd);
					if (gs.isGameOver()) {
						gd.setWinner(gs.getWinner());
					}

					l.gameRead(gd, "VBarykin");
				}

			}

		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			if (in != null) {
				try { in.close(); } catch (Throwable t) {}
			}
		}

		return imported;
	}

}
