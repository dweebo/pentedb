package org.pente.gameDatabase.swing.importer;

import java.io.InputStream;

import org.pente.game.*;
import org.pente.gameDatabase.swing.PlunkGameData;

/**
 * @author dweebo
 */
public class StepanovGameImporter extends GameImporterAdapter {

	public boolean attemptImport(InputStream in, GameImporterListener l) {
		boolean imported = false;

		try {
			outer: while (true) {
				int moves[] = new int[361];
				byte p1[] = new byte[15];
				for (int i = 0; i < 15; i++) {
					int b = in.read();
					if (b == -1) break outer;
					p1[i] = (byte) b;
				}
				String p1Name = new String(p1).trim();
				byte p2[] = new byte[15];
				for (int i = 0; i < 15; i++) {
					int b = in.read();
					if (b == -1) return imported;
					p2[i] = (byte) b;
				}
				String p2Name = new String(p2).trim();
				int numMoves = 0;
				boolean end = false;
				for (int i = 0; i < 112; i++) {
					int m1 = in.read();
					if (m1 == -1) return imported;
					int m2 = in.read();
					if (m2 == -1) return imported;
					int m = m1 << 8 | m2;
					if (m == 0) {
						end = true;
					}
					if (!end) {
						int y = (m-23) / 21;
						int x = (m-23) % 21;
						moves[numMoves++] = y * 19 + x;
					}
				}
				int r = in.read();
				if (r == -1) return imported;
				r = in.read();

				if (numMoves > 0) {
					PlunkGameData gd = new PlunkGameData();
					gd.setGame(GridStateFactory.PENTE_GAME.getName());
					gd.setEditable(true);
					PlayerData p1d = new DefaultPlayerData();
					p1d.setUserIDName(p1Name);
					gd.setPlayer1Data(p1d);
					PlayerData p2d = new DefaultPlayerData();
					p2d.setUserIDName(p2Name);
					gd.setPlayer2Data(p2d);
					if (r == 2) {
						gd.setWinner(1);
					} else {
						gd.setWinner(2);
					}

//					int moveDiff = 180 - moves[0];
//					boolean check1st = false;
//					for (int s : surrounding) {
//						if (s == moveDiff) {
//							check1st = true;
//							break;
//						}
//					}
//					if (!check1st) {
//						moveDiff = 0;//will be caught at higher level
//					}
//					int min = 1;
					for (int i = 0; i < numMoves; i++) {
						//make sure there are actually valid moves
						if (moves[i] < 0 || moves[i] > 362) {
							return imported;
						}
//						moves[i] += moveDiff;
//						if (moves[i] < 0 || moves[i] > 362) {
//							moves[i] = min++;
//						}
						gd.addMove(moves[i]);
					}

					l.gameRead(gd, "Stepanov");
					imported = true;
				}
			}

		} catch (Throwable t) {
			//t.printStackTrace();
		} finally {
			if (in != null) {
				try { in.close(); } catch (Throwable t) {}
			}
		}

		return imported;
	}
}
