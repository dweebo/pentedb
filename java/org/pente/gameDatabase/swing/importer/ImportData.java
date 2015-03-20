package org.pente.gameDatabase.swing.importer;

/**
 * @author dweebo
 */
public class ImportData {

	private String fileName;
	private String type;
	private String status;

	private String name;
	private String db = "";

	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
//		if (tree != null) {
//			return tree.getName();
//		}
//		else if (game != null) {
//			return Utilities.getGameName(game);
//		}
//		else {
//			return "Unknown";
//		}
	}
	public void setDb(String db) {
		this.db = db;
	}
	public String getDb() {
		return db;
//		if (game != null) {
//			return game.getDbName();
//		}
//		else {
//			return "";
//		}
	}
}
