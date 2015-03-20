package org.pente.gameDatabase.swing.importer;

import java.io.*;

/**
 * @author dweebo
 */
public abstract class GameImporterAdapter implements GameImporter {

	public boolean attemptImport(byte[] data, GameImporterListener l) {
		InputStream in = new ByteArrayInputStream(data);
		return attemptImport(in, l);
	}

	public abstract boolean attemptImport(InputStream in, GameImporterListener l);

	public boolean attemptImport(File f, GameImporterListener l) {
		InputStream in = null;
		try {
			in = new FileInputStream(f);
			return attemptImport(in, l);
		} catch (FileNotFoundException fe) {
			System.err.println("File not found: " + f);
			return false;
		} finally {
			if (in != null) {
				try { in.close(); } catch (IOException i) {}
			}
		}
	}

}
