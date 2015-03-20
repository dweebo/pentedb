package org.pente.gameDatabase.swing.importer;

import java.io.InputStream;
import java.io.File;
import java.awt.event.ActionEvent;
import javax.swing.Action;

import org.pente.game.*;
import org.pente.gameDatabase.swing.PlunkGameData;

/**
 * @author dweebo
 */
public class BrainkingGameImporter extends GameImporterAdapter {

	public boolean attemptImport(InputStream in, GameImporterListener l) {
		boolean imported = false;

		PGNGameFormat gf = new PGNGameFormat("\n", "yyyy.MM.dd");

		try {
			PlunkGameData gd = gf.parse(in);
			gd.setSite("BrainKing");
			if (gd.getRound() != null && gd.getRound().equals("?")) {
				gd.setRound("");
			}
			imported = true;
			l.gameRead(gd, "BK");

		} catch (Throwable t) {
			//t.printStackTrace();
		}

		return imported;
	}

}
