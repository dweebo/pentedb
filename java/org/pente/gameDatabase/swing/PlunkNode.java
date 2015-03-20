package org.pente.gameDatabase.swing;

import java.util.*;

/**
 * @author dweebo
 */
public class PlunkNode {

	private long treeId;
	private long hash;
	private PlunkNode parent;
	private int rotation;
	private int move;
	private int depth;
	private int type;

	private transient int rotatedMove = 0;

	public static final int NO_TYPE = 0;
	public static final int GOOD = 1;
	public static final int VERY_GOOD = 2;
	public static final int BAD = 3;
	public static final int VERY_BAD = 4;
	public static final int INTERESTING = 5;

	private String name;
	private String comments;

	private boolean stored;
	private boolean deleted;

	private List<PlunkNode> children;

	public boolean head_p;
	public Map<String, String> props;

	public PlunkNode() {
	}
	public PlunkNode(PlunkNode p) {
		setComments(p.getComments());
		setDepth(p.getDepth());
		setHash(p.getHash());
		setMove(p.getMove());
		setName(p.getName());
		setRotation(p.getRotation());
		setType(p.getType());
	}

	public int getRotatedMove() {
		return rotatedMove;
	}

	public void setRotatedMove(int rotatedMove) {
		this.rotatedMove = rotatedMove;
	}

	public PlunkNode[] getPath() {
		PlunkNode path[] = new PlunkNode[depth + 1];

		PlunkNode n = this;

		for (int i = depth; i >= 0; i--) {
			path[i] = n;
			if (!n.isRoot()) n = n.getParent();
		}
		return path;
	}

	public int[] getMoves() {
		int moves[] = new int[depth + 1];

		PlunkNode n = this;

		for (int i = depth; i >= 0; i--) {
			moves[i] = n.getMove();
			if (!n.isRoot()) n = n.getParent();
		}
		return moves;
	}

	public boolean isStored() {
		return stored;
	}

	public void setStored(boolean stored) {
		this.stored = stored;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	public void addChild(PlunkNode c) {
		if (children == null) {
			children = new ArrayList<PlunkNode>();
		}
		children.add(c);
	}

	public boolean isRoot() {
		return depth == 0;
	}
	public List<PlunkNode> getChildren() {
		return children;
	}
	public PlunkNode getBestMove() {
		if (children == null) return null;
		List<PlunkNode> c2 = new ArrayList<PlunkNode>(children);
		Collections.shuffle(c2);//shuffle then sort to sort of randomize
		// sort very best at top, then best, then interesting, then none
		//then bad, then very bad
		Collections.sort(c2, new Comparator<PlunkNode>() {
			public int compare(PlunkNode o1, PlunkNode o2) {
				if (o1.getType() == VERY_GOOD) return -1;
				else if (o1.getType() == GOOD) {
					if (o2.getType() == VERY_GOOD) return 1;
					else if (o2.getTreeId() == GOOD) return 0;
					else return -1;
				}
				else if (o1.getType() == INTERESTING) {
					if (o2.getType() == VERY_GOOD || o2.getType() == GOOD) return 1;
					else if (o2.getTreeId() == INTERESTING) return 0;
					else return -1;
				}
				else if (o1.getType() == NO_TYPE) {
					if (o2.getType() == VERY_GOOD || o2.getType() == GOOD || o2.getType() == INTERESTING) return 1;
					else if (o2.getTreeId() == NO_TYPE) return 0;
					else return -1;
				}
				else if (o1.getType() == BAD) {
					if (o2.getType() == VERY_BAD) return -1;
					else if (o2.getTreeId() == BAD) return 0;
					else return 1;
				}
				else if (o1.getType() == VERY_BAD) {
					if (o2.getType() == VERY_BAD) return 0;
					else return 1;
				}
				return 0;
			}
		});
		return c2.get(0);
	}

	public void setChildren(List<PlunkNode> children) {
		this.children = children;
	}
	public int getChildCount() {
		if (children == null) {
			return 0;
		}
		return children.size();
	}
	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}
	public void deleteChild(PlunkNode child) {
		if (children != null) {
			children.remove(child);
		}
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
	public boolean hasComments() {
		return comments != null;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public long getHash() {
		return hash;
	}

	public void setHash(long hash) {
		this.hash = hash;
	}

	public int getMove() {
		return move;
	}

	public void setMove(int move) {
		this.move = move;
		if (rotatedMove == 0) {
			rotatedMove = move;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PlunkNode getParent() {
		return parent;
	}

	public void setParent(PlunkNode parent) {
		this.parent = parent;

		if (parent != null) {
			parent.addChild(this);
		}
	}

	public int getRotation() {
		return rotation;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public long getTreeId() {
		return treeId;
	}

	public void setTreeId(long treeId) {
		this.treeId = treeId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
