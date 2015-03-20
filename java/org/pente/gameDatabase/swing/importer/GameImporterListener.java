package org.pente.gameDatabase.swing.importer;

import org.pente.gameDatabase.swing.PlunkGameData;
import org.pente.gameDatabase.swing.PlunkTree;

/**
 * @author dweebo
 */
public interface GameImporterListener {

	public void gameRead(PlunkGameData g, String importerName);
	public void analysisRead(PlunkTree t, String importerName);
}
