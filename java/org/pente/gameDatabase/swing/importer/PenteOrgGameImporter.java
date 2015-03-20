package org.pente.gameDatabase.swing.importer;

import java.io.InputStream;

import org.pente.game.*;
import org.pente.gameDatabase.swing.PlunkGameData;

/**
 * @author dweebo
 */
public class PenteOrgGameImporter extends GameImporterAdapter {

	public boolean attemptImport(InputStream in, GameImporterListener l) {
		boolean imported = false;

		PGNGameFormat gf = new PGNGameFormat("\r\n", "MM/dd/yyyy");

		try {
			PlunkGameData gd = gf.parse(in);
			imported = true;

			l.gameRead(gd, "PGN");

		} catch (Throwable t) {
			//t.printStackTrace();
		}

		return imported;
	}
}
