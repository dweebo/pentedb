package org.pente.gameDatabase.swing;

import java.util.Date;

/**
 * @author dweebo
 */
public class PlunkTree {

	private long treeId;
	private String name;
	private String version;
	private String creator;
	private Date lastModified;
	private Date created;
	private boolean canEditProps = false;

	private boolean stored = false;

	private PlunkNode root;

	public PlunkNode getRoot() {
		return root;
	}
	public void setRoot(PlunkNode root) {
		this.root = root;
	}
	public boolean isStored() {
		return stored;
	}
	public void setStored(boolean stored) {
		this.stored = stored;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public boolean canEditProps() {
		return canEditProps;
	}
	public void setCanEditProps(boolean canEditProps) {
		this.canEditProps = canEditProps;
	}
	public Date getLastModified() {
		return lastModified;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	public long getTreeId() {
		return treeId;
	}
	public void setTreeId(long treeId) {
		this.treeId = treeId;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
}
