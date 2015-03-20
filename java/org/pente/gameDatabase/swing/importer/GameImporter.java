package org.pente.gameDatabase.swing.importer;

import java.io.File;
import java.io.InputStream;

/**
 * @author dweebo
 */
public interface GameImporter {

	public boolean attemptImport(byte data[], GameImporterListener l);
	public boolean attemptImport(InputStream in, GameImporterListener l);
	public boolean attemptImport(File f, GameImporterListener l);
}
