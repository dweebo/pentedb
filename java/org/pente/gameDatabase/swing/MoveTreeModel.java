package org.pente.gameDatabase.swing;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.tree.*;

import org.pente.game.*;
import org.pente.gameServer.core.*;

/**
 * @author dweebo
 */
public class MoveTreeModel extends DefaultTreeModel implements TreeSelectionListener {

    /** represents the current selected path in the tree*/
    private GridState treeGridState;

    /** represents the current path on the board */
    private GridState boardGridState;

    /** map the hashcode of a position to a node in the tree */
    private Map<Long, DefaultMutableTreeNode> nodes =
    	new HashMap<Long, DefaultMutableTreeNode>();

    private List<PlunkNode> dirtyNodes = new ArrayList<PlunkNode>();

    private List<MoveChangeListener> listeners = new ArrayList<MoveChangeListener>();
    private List<TabComponentEditListener> editListeners = new ArrayList<TabComponentEditListener>();

    private int maxPath[];
    private DefaultMutableTreeNode currentTreeNode;
    private PlunkNode currentNode;

    private JTree tree;
    private MoveIconRenderer renderer;

    private PlunkTree currentTree;

	public MoveTreeModel(TreeNode root) {
		super(root);
	}


	public void setJTree(JTree jtree) {
		this.tree = jtree;
		tree.setRootVisible(false);

		tree.addTreeWillExpandListener(new TreeWillExpandListener() {
			public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
				TreePath p = event.getPath();
				DefaultMutableTreeNode d = (DefaultMutableTreeNode)
					p.getLastPathComponent();
				PlunkNode n = (PlunkNode) d.getUserObject();
				if (n.getDepth() == 0) { //K10
					throw new ExpandVetoException(event, "K10 not collapsable");
				}
			}
			public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
			}
		});
	}
	public JTree getJTree() {
		return tree;
	}

	public MoveIconRenderer getRenderer() {
		return renderer;
	}

	public void setRenderer(MoveIconRenderer renderer) {
		this.renderer = renderer;
	}

	public GridState getBoardGridState() {
		return boardGridState;
	}

	public PlunkTree getPlunkTree() {
		return currentTree;
	}
	public PlunkNode getCurrentNode() {
		return currentNode;
	}
	public DefaultMutableTreeNode getCurrentTreeNode() {
		return currentTreeNode;
	}

	public void setPlunkTree(PlunkTree plunkTree) {
		this.currentTree = plunkTree;
	}
	public void setGame(int game) {
		// do this if moves already entered on board, in order to keep
		// the same rotations
		if (treeGridState != null) {
			treeGridState = GridStateFactory.createGridState(game, treeGridState);
		}
		else {
			treeGridState = GridStateFactory.createGridState(game);
		}
		// do this if moves already entered on board, in order to keep
		// the same rotations
		if (boardGridState != null) {
			boardGridState = GridStateFactory.createGridState(game,boardGridState);
		}
		else {
			boardGridState = GridStateFactory.createGridState(game);
		}
	}

	public PlunkNode getPlunkRoot() {
		return (PlunkNode) ((DefaultMutableTreeNode) root.getChildAt(0)).getUserObject();
	}
	public int getNodeCount() {
		return nodes.size();
	}

	public void addListener(MoveChangeListener l) {
		listeners.add(l);
	}
	private void notifyListenersNodeChanged() {

		for (MoveChangeListener l : listeners) {
			l.nodeChanged();
		}
	}
    public void addEditListener(TabComponentEditListener l) {
        editListeners.add(l);
    }
    private void notifyEditListenersEditsMade() {
        for (TabComponentEditListener l : editListeners) {
            l.editsMade();
        }
    }
    private void notifyEditListenersEditsSaved() {
        for (TabComponentEditListener l : editListeners) {
            l.editsSaved();
        }
    }

	private void notifyListeners(PlunkNode n) {

		PlunkNode path[] = n.getPath();
		int moves[] = new int[path.length];
		if (boardGridState.getNumMoves() == 0) {
			moves = n.getMoves();
			boardGridState.addMove(moves[0]);
		}
		else {
	    	int lastDepthHashMatch = -1;
	    	for (int i = Math.min(path.length, boardGridState.getNumMoves()); i > 0; i--) {
	    		if (path[i-1].getHash() == boardGridState.getHash(i - 1)) {
	    			lastDepthHashMatch = i - 1;
	    			break;
	    		}
	    	}
	    	for (int i = 0; i <= lastDepthHashMatch; i++) {
	    		moves[i] = boardGridState.getMove(i);
	    	}
	    	for (int i = lastDepthHashMatch + 1; i < path.length; i++) {

				//if (i < boardGridState.getNumMoves() && path[i].getHash() == boardGridState.getHash(i)) {
				//	moves[i] = boardGridState.getMove(i);
				//}
				//else {

	    		//TODO move this while loop out of for loop
					while (boardGridState.getNumMoves() > i) {
						boardGridState.undoMove();
					}


	    			int possMoves[] = boardGridState.getAllPossibleRotations(path[i].getMove(), path[i].getParent().getRotation());
	    			boolean foundSame = false;
	    			for (int pm : possMoves) {
	    				if (pm == path[i].getMove()) {
	    					boardGridState.addMove(pm);
	    	    			moves[i] = pm;
	    					foundSame = true;
	    					break;
	    				}
	    			}
	    			if (!foundSame) {
	    				int move = boardGridState.rotateMoveToLocalRotation(
	    						path[i].getMove(), path[i].getParent().getRotation());
	    				boardGridState.addMove(move);
	    				moves[i] = move;
	    			}
				//}
			}

			while (boardGridState.getNumMoves() > path.length) {
				boardGridState.undoMove();
			}

			if (n.hasChildren()) {
				for (PlunkNode c : n.getChildren()) {
					int possMoves[] = boardGridState.getAllPossibleRotations(
						c.getMove(), c.getParent().getRotation());
	    			boolean foundSame = false;
	    			for (int pm : possMoves) {
	    				if (pm == c.getMove()) {
	    					c.setRotatedMove(c.getMove());
	    					foundSame = true;
	    					break;
	    				}
	    			}
	    			if (!foundSame) {
	    				int rotatedMove = boardGridState.rotateMoveToLocalRotation(
	    						c.getMove(), c.getParent().getRotation());
	    				c.setRotatedMove(rotatedMove);
	    			}
				}
			}
		}

		for (MoveChangeListener l : listeners) {
			l.changeMoves(moves, currentNode);
		}
		treeGridState.clear();
		for (int m : n.getMoves()) {
			treeGridState.addMove(m);
		}

		//these were causing auto-collapses
		//needed because coordinates might change and we want them to be
		//re-rendered, but how to do it!!!
		//reload(currentTreeNode);
		//nodeStructureChanged(currentTreeNode);

		// This works, when going back moves though the renderer switches
		// back to the default rotation, not sure that is what it should do
		// use getParent() because of possibility of increasing depth children in parent
		nodesChanged((DefaultMutableTreeNode) currentTreeNode.getParent());
	}

	// update all nodes as changed down the entire tree
	private void nodesChanged(DefaultMutableTreeNode d) {
		nodeChanged(d);
		for (int i = 0; i < d.getChildCount(); i++) {
			nodesChanged((DefaultMutableTreeNode) d.getChildAt(i));
		}
	}


	private void addDirtyNode(PlunkNode n) {
		if (!dirtyNodes.contains(n)) {
			dirtyNodes.add(n);
		}
        notifyEditListenersEditsMade();
	}
	public List<PlunkNode> getDirtyNodes() {
		saveComments(); // if user has entered comments to current node
		return dirtyNodes;
	}
	public void clearDirtyNodes() {
		dirtyNodes.clear();
        notifyEditListenersEditsSaved();
	}

	public void newPlunkTree() {
		clear();

		currentTree = new PlunkTree();
		currentTree.setName("New Game Analysis");
		currentTree.setCanEditProps(true);

		PlunkNode k10 = new PlunkNode();
		k10.setMove(180);
		boardGridState.addMove(180);
		k10.setHash(boardGridState.getHash());
		boardGridState.undoMove();

		addMoves(k10, true);
		visitNode(k10.getHash());

		nodeStructureChanged(root);
	}
	public void newPlunkTree2() {
		clear();

		currentTree = new PlunkTree();
		currentTree.setName("New Game Analysis");
		currentTree.setCanEditProps(true);

		nodeStructureChanged(root);
	}


	public void clear() {
		dirtyNodes.clear();
		currentNode = null;
		currentTreeNode = null;
		maxPath = null;
		nodes.clear();
		tempSelected = false;
		tempSelectedPath = null;
		treeGridState.clear();
		boardGridState.clear();

		((DefaultMutableTreeNode) root).removeAllChildren();
		nodeStructureChanged(root);
	}
	public void addMoves(PlunkNode plunkRoot, boolean dirty) {

		DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(plunkRoot);
		nodes.put(plunkRoot.getHash(), dmtn);
		if (dirty) addDirtyNode(plunkRoot);

		((DefaultMutableTreeNode) root).add(dmtn);

		addNode(((DefaultMutableTreeNode) root), dmtn, true, dirty);

		// because can't see the + icon to left or move in the top level
		// this will make sure it is expanded
		DefaultMutableTreeNode last = (DefaultMutableTreeNode) root.getChildAt(root.getChildCount() - 1);
		tree.expandPath(new TreePath(last.getPath()));
	}


	private void addNode(DefaultMutableTreeNode parent,
		DefaultMutableTreeNode  child,
		boolean parentIncreasingDepth, boolean dirty) {

		PlunkNode node = (PlunkNode) child.getUserObject();
		if (node.getChildCount() >= 2) {
			for (PlunkNode c : node.getChildren()) {
				DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(c);
				nodes.put(c.getHash(), dmtn);
				if (dirty) addDirtyNode(c);

				child.add(dmtn);

				addNode(child, dmtn, false, dirty);
			}
		}
		else if (node.getChildCount() == 1) {
			PlunkNode cc = node.getChildren().get(0);
			DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(cc);
			nodes.put(cc.getHash(), dmtn);
			if (dirty) addDirtyNode(cc);

			if (parentIncreasingDepth) {
				parent.add(dmtn);
				addNode(parent, dmtn, true, dirty);
			}
			else {
				child.add(dmtn);
				addNode(child, dmtn, true, dirty);
			}
		}
		else {
			return;
		}
	}

	private void deleteTreeNode(PlunkNode n) {

		n.setDeleted(true);
		addDirtyNode(n);
		nodes.remove(n.getHash());
		if (n.hasChildren()) {
			for (PlunkNode c : n.getChildren()) {
				deleteTreeNode(c);
			}
		}
	}

	public void deleteNode(DefaultMutableTreeNode d) {
		DefaultMutableTreeNode p = (DefaultMutableTreeNode) d.getParent();
		PlunkNode n = (PlunkNode) d.getUserObject();

		if (n.getParent() != null) {
			n.getParent().deleteChild(n);
		}
		deleteTreeNode(n);

		int rmi[] = new int[] { p.getIndex(d) };
		Object rm[] = new Object[] { d };
		p.remove(d);
		nodesWereRemoved(p, rmi, rm);

		if (p.getChildCount() == 1 && increasingDepth((DefaultMutableTreeNode) p.getChildAt(0))) {
			DefaultMutableTreeNode c = (DefaultMutableTreeNode) p.getChildAt(0);
			while (c.getChildCount() > 0) {
				DefaultMutableTreeNode cc = (DefaultMutableTreeNode) c.getChildAt(0);
				p.add(cc);
			}
			nodeStructureChanged(p);

			if (p != root) {
				// need to do this part recursively up parents
				DefaultMutableTreeNode pp = (DefaultMutableTreeNode) p.getParent();
				if (increasingDepth(pp)) {
					while (p.getChildCount() > 0) {
						DefaultMutableTreeNode cc = (DefaultMutableTreeNode) p.getChildAt(0);
						pp.add(cc);
					}
					nodeStructureChanged(pp);
				}
			}
		}

		else {
			while (p.getChildCount() > rmi[0]) {
				DefaultMutableTreeNode c = (DefaultMutableTreeNode) p.getChildAt(rmi[0]);
				if (((PlunkNode) c.getUserObject()).getDepth() > n.getDepth()) {

					p.remove(rmi[0]);

					n = (PlunkNode) c.getUserObject();
					deleteTreeNode(n);

				} else {
					break;
				}
			}
			nodeStructureChanged(p);
		}


		// in case deleting near the current selected node
		if (currentNode != null && currentNode.isDeleted()) {
			currentTreeNode = p;
			if (increasingDepth(p) && p.getChildCount() > 0) {
				currentTreeNode = (DefaultMutableTreeNode) p.getChildAt(rmi[0]-1);
			}
			currentNode = (PlunkNode) currentTreeNode.getUserObject();
			maxPath = currentNode.getMoves();
			selectNode(currentTreeNode);
		}

		// if parent now only has one child and parent is increasing depth then
		//   add child to parent.parent
		//   if child is increasing depth
		//     add all child.child[] to parent



		// W K10
		// B L9
		// W N10
		//   B L8 X
		//   B L7
		//     W K1
		//     B K2

		// if parent of deleted has 1 child
		// and that child is increasing depth
		// then elevate children to parent
		// W K10
		// B L9
		// W N10
		//   B L7
		//   W K1
		//   B K2

		// now if parent.parent is increasing depth
		// elevate children to parent.parent

		// W K10
		// B L9
		// W N10
		// B L7
		// W K1
		// B K2

		// if we have
		// W 1
		// W 2
		//   B C1 X
		//   B C2
		//     W C2.1
		//     B C2.2

		// then
		// W 1
		// W 2
		//   B C2
		//   W C2.1
		//   B C2.2

		// also need to handle deleting in middle of increasingDepth
		// remove all nodes below the deleted one
	}


	public void addMove(int move) {

		tempSelected = false;

		DefaultMutableTreeNode n = null;
		PlunkNode ppn = null;
		if (!nodes.isEmpty()) {
			n = nodes.get(treeGridState.getHash());
			ppn = (PlunkNode) n.getUserObject();
		}

		int rot = boardGridState.getRotation();
		boardGridState.addMove(move);



		int rotated = move;
		rotated = boardGridState.getNumMoves() == 1 ? 180 : treeGridState.rotateMoveToLocalRotation(move, rot);

		// if it is possible to keep the same move instead of rotating it
		// to a different orientation then do so
		int possMoves[] = treeGridState.getAllPossibleRotations(
			move, rot);
		for (int pm : possMoves) {
			if (pm == move) {
				rotated = move;
				break;
			}
		}

		//System.out.println("move=" + move + ", add as " + rotated);

		// local is the moves as they are in the tree
		// so when new moves added, store them rotated into the tree orientation
		// not the board orientation
		treeGridState.addMove(rotated);
		//printState(treeGridState);


		System.out.println("add move " + move);
		//treeGridState.printBoard();
		//System.out.println();
		//boardGridState.printBoard();
		//printState(boardGridState);
		//System.out.println("board rot="+rot);

		DefaultMutableTreeNode existing = nodes.get(treeGridState.getHash());
		if (existing != null) {
			selectNode(existing);
			return;
		}

		PlunkNode pn = new PlunkNode();
		pn.setMove(rotated);
		if (ppn != null) {
			pn.setParent(ppn);
		}
		pn.setHash(treeGridState.getHash());
		pn.setRotation(treeGridState.getRotation());
		pn.setDepth(treeGridState.getNumMoves() - 1);

		addDirtyNode(pn);

		if (nodes.isEmpty()) {

			n = new DefaultMutableTreeNode(pn);
			((DefaultMutableTreeNode) root).add(n);
			nodes.put(treeGridState.getHash(), n);
			selectNode(n);
			return;
		}



		// algorithm
		// node n = current move before adding
		// node p = parent of n
		// nodes n.s[] = n's sibs
		// node c = new node

		// p could have
		//   1 child - (n)
		//   2+ children w/ increasing depth - indicates the same line
		//   2+ children w/ same depth - indicates branches
		// n could have
		//   0 children
		//   1 child
		//   2+ children w/ increasing depth
		//   2+ children w/ same depth
		// if p has 2+ children, n could be
		//   at end of list
		//   not at end of list

		// if n has 0 children
		//   if p has 1 child
		//     add c to p
		//   else if p has 2+ increasing depth
		//     if n at end of list
		//       add c to p
		//     else
		//       add c to n
		//       add n.s[c.position + 1] to n
		//       add n.s[c.position + 2+] to s[c.position + 1]
		//   else p has 2+ same depth
		//     add c to n
		// else if n has 1 child or 2+ children w/ same depth
		//   add c to n
		// else if n has 2+ children w/ increasing depth
		//   add c to n
		//   for n's children n.c[]
		//     add n.c[1+] to n.c[0]

		DefaultMutableTreeNode c = new DefaultMutableTreeNode(pn);

		DefaultMutableTreeNode p = (DefaultMutableTreeNode) n.getParent();

		if (n.getChildCount() == 0) {
			if (p.getChildCount() == 1) {
				p.add(c);
				nodesWereInserted(p, new int[]{p.getChildCount() - 1});
			}
			else {

				if (increasingDepth(p)) {
					if (p.getChildAfter(n) == null) {
						p.add(c);
						nodesWereInserted(p, new int[]{p.getChildCount() - 1});
					}
					else {

						DefaultMutableTreeNode s = (DefaultMutableTreeNode) p.getChildAfter(n);
						int start = p.getIndex(s);
						int num = p.getChildCount();
						int cnt = num - start;
						Object rm[] = new Object[cnt];
						int rmi[] = new int[cnt];
						int i = 0;
						p.remove(s);
						rmi[i]=start;
						rm[i++] = s;
						n.add(s);
						nodesWereInserted(n, new int[]{n.getChildCount() - 1});
						DefaultMutableTreeNode s2 = null;
						do {
							s2 = (DefaultMutableTreeNode) p.getChildAfter(n);
							if (s2 == null) break;
							p.remove(s2);
							rmi[i]=start+i;
							rm[i++] = s2;
							s.add(s2);
							nodesWereInserted(s, new int[]{s.getChildCount() - 1});
						} while (true);

						nodesWereRemoved(p, rmi, rm);

						n.add(c);
						nodesWereInserted(n, new int[]{n.getChildCount() - 1});
					}
				}
				else {
					n.add(c);
					nodesWereInserted(n, new int[]{n.getChildCount() - 1});
				}
			}
		}
		else if (n.getChildCount() == 1 || !increasingDepth(n)) {
			n.add(c);
			nodesWereInserted(n, new int[]{n.getChildCount() - 1});
		}
		else {
			DefaultMutableTreeNode s = (DefaultMutableTreeNode) n.getFirstChild();
			int start = 1;
			int num = n.getChildCount();
			int cnt = num - start;
			Object rm[] = new Object[cnt];
			int rmi[] = new int[cnt];
			for (int i = 0; i < cnt; i++) {
				DefaultMutableTreeNode s2 = (DefaultMutableTreeNode) n.getChildAfter(s);
				n.remove(s2);
				rmi[i]= i + start;
				rm[i] = s2;
				s.add(s2);
				nodesWereInserted(s, new int[]{s.getChildCount() - 1});
			}

			nodesWereRemoved(n, rmi, rm);

			n.add(c);
			nodesWereInserted(n, new int[]{n.getChildCount() - 1});
		}

		nodes.put(treeGridState.getHash(), c);

		selectNode(c);
	}

	private void printState(GridState s) {
		for (int i = 0; i < s.getNumMoves(); i++) {
			System.out.print(s.getMove(i) + " ");
		}
		System.out.println();
	}
	public void firstMove() {

		if (treeGridState.getNumMoves() > 1) {

			treeGridState.clear();
			treeGridState.addMove(180);

			DefaultMutableTreeNode r = nodes.get(treeGridState.getHash());

			selectNode(r);
		}
	}
	public void prevMove() {

		if (treeGridState.getNumMoves() > 1) {

			treeGridState.undoMove();
			boardGridState.undoMove();
			DefaultMutableTreeNode r = nodes.get(treeGridState.getHash());

			selectNode(r);
		}
	}
	public void nextMove() {

		//keep track of furthest path into tree, when switching around swithc
		// it too, then when click next, move down the path from current selection
		//until reach end
		//if end is a node with all further siblings increasingDepth
		//continue on until reach one w/ children or end

		if (maxPath.length > boardGridState.getNumMoves()) {
			int m = maxPath[boardGridState.getNumMoves()];

			int rot = boardGridState.getRotation();
			boardGridState.addMove(m);

			int rotated = treeGridState.rotateMoveToLocalRotation(m, rot);
			treeGridState.addMove(rotated);

			DefaultMutableTreeNode r = nodes.get(treeGridState.getHash());

			selectNode(r);
		}
		else {
			TreePath selected = tree.getSelectionPath();
			DefaultMutableTreeNode n = (DefaultMutableTreeNode)
				selected.getLastPathComponent();

			if (n.getChildCount() != 0) {
				if (increasingDepth(n)) {
					selectNode((DefaultMutableTreeNode) n.getFirstChild());
				}
			}
			else {
				DefaultMutableTreeNode p = (DefaultMutableTreeNode) n.getParent();
				if (increasingDepth(p) && p.getChildAfter(n) != null) {
					selectNode((DefaultMutableTreeNode) p.getChildAfter(n));
				}
			}
		}
	}
	public void lastMove() {

		//keep track of furthest path into tree, when switching around swithc
		// it too, then when click next, move down the path from current selection
		//until reach end
		//if end is a node with all further siblings increasingDepth
		//continue on until reach one w/ children or end

		while (maxPath.length > boardGridState.getNumMoves()) {
			int rot = boardGridState.getRotation();
			int m = maxPath[treeGridState.getNumMoves()];
			boardGridState.addMove(m);
			int rotated = treeGridState.rotateMoveToLocalRotation(m, rot);
			treeGridState.addMove(rotated);
		}

		DefaultMutableTreeNode r = nodes.get(treeGridState.getHash());

		if (r.getChildCount() != 0) {
			if (increasingDepth(r)) {
				r = (DefaultMutableTreeNode) r.getLastChild();
			}

		}
		else {
			DefaultMutableTreeNode p = (DefaultMutableTreeNode) r.getParent();
			if (increasingDepth(p)) {
				r = (DefaultMutableTreeNode) p.getLastChild();
			}

		}

		selectNode(r);
	}

	public void refreshBoard() {
		if (currentNode != null) {
			notifyListeners(currentNode);
		}
	}
	public void visitNode(long hash) {
		DefaultMutableTreeNode n = nodes.get(hash);
		if (n != null) {
			selectNode(n);
		}
	}

	private void selectNode(DefaultMutableTreeNode r) {

		TreePath p = new TreePath(r.getPath());
		tree.scrollPathToVisible(p);

		tree.expandPath(p);
		tree.setSelectionPath(p);

		// setting selection path calls valueChanged below
		// which then updates the game board
	}

	private boolean tempSelected = false;
	private TreePath tempSelectedPath = null;

	public void tempSelectNode(TreePath path) {
		Object o = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
		if (o instanceof PlunkNode) {
			PlunkNode n = (PlunkNode) o;
			//System.out.println("temp select " + n.getMove());
		} else {
			//System.out.println("temp select root");
		}
		tempSelected = true;
		tempSelectedPath = path;

		tree.scrollPathToVisible(path);
		tree.setSelectionPath(path);
	}
	public TreePath getTempSelectedPath() {
		return tempSelectedPath;
	}
	public void tempUnselectNode() {
		//System.out.println("temp unselect");
		if (tempSelected) {
			tempSelected = false;
			//tempSelectedPath = null;
			if (currentTreeNode != null) {
				selectNode(currentTreeNode);
			}
		}
	}

	private boolean increasingDepth(DefaultMutableTreeNode p) {
		int depth = -1;
		if (p != root) depth = ((PlunkNode) p.getUserObject()).getDepth();
		boolean increasingDepth = true;
		for (int i = 0; i < p.getChildCount(); i++) {
			DefaultMutableTreeNode k = (DefaultMutableTreeNode) p.getChildAt(i);
			if (((PlunkNode) k.getUserObject()).getDepth() != ++depth) {
				increasingDepth = false;
				break;
			}
		}
		return increasingDepth;
	}

	/** called when selection made */
	public void valueChanged(TreeSelectionEvent e) {
		TreePath leadPath = e.getNewLeadSelectionPath();
		if (leadPath != null) {
			DefaultMutableTreeNode leadSelection = (DefaultMutableTreeNode)
				leadPath.getLastPathComponent();

			if (tempSelected || leadSelection == root) return;
			//if (leadSelection == currentTreeNode) return;

			saveComments();

			currentTreeNode = leadSelection;
			currentNode = (PlunkNode) leadSelection.getUserObject();
			notifyListeners(currentNode);

			tree.expandPath(leadPath);

			loadComments();

			// update max path used in making > moves
			int np[] = boardGridState.getMoves();
			if (maxPath == null || np.length > maxPath.length) {
				maxPath = boardGridState.getMoves();
			}
			else {
				for (int i = 0; i < np.length; i++) {
					if (np[i] != maxPath[i]) {
						maxPath = np;
						break;
					}
				}
			}
		}
	}

	public void saveComments() {
		if (currentNode != null) {
			String newComments = comments.getText();
			if (newComments == null || newComments.equals("")) {
				if (currentNode.getComments() != null) {
					currentNode.setComments(null);
					addDirtyNode(currentNode);
				}
			}
			else if (!newComments.equals(currentNode.getComments())) {
				currentNode.setComments(comments.getText());
				addDirtyNode(currentNode);
			}
		}
	}
	private void loadComments() {
		if (currentNode.hasComments()) {
			comments.setText(currentNode.getComments());
		} else {
			comments.setText("");
		}
	}

    private JTextPane comments;
    public void setComments(JTextPane comments) {
    	this.comments = comments;
    }
    Action firstMoveAction = new AbstractAction("Back to start",
    	new ImageIcon(MoveTreeModel.class.getResource("images/begin_big.gif"))) {
    	{ putValue(SHORT_DESCRIPTION, "Go to starting move"); }
    	public void actionPerformed(ActionEvent e) {
        	firstMove();
    	}
    };
    Action backMoveAction = new AbstractAction("Previous move",
    	new ImageIcon(MoveTreeModel.class.getResource("images/left_big.gif"))) {
    	{ putValue(SHORT_DESCRIPTION, "Go to previous move"); }
    	public void actionPerformed(ActionEvent e) {
        	prevMove();
    	}
    };
    Action nextMoveAction = new AbstractAction("Next move",
    	new ImageIcon(MoveTreeModel.class.getResource("images/right_big.gif"))) {
    	{ putValue(SHORT_DESCRIPTION, "Go to next move"); }
    	public void actionPerformed(ActionEvent e) {
        	nextMove();
    	}
    };
    Action lastMoveAction = new AbstractAction("Last move",
    	new ImageIcon(MoveTreeModel.class.getResource("images/end_big.gif"))) {
    	{ putValue(SHORT_DESCRIPTION, "Go to last move"); }
    	public void actionPerformed(ActionEvent e) {
        	lastMove();
    	}
    };

    private void putAction(KeyStroke k, String name, Action action) {
        tree.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, name);
        tree.getInputMap(JComponent.WHEN_FOCUSED).put(k, name);
        tree.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(k, name);
        tree.getActionMap().put(name, action);
    }

	public JPanel getNavPanel() {

        // add navigation buttons
        JButton firstButton = new FlatButton(firstMoveAction);
        putAction(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "firstMove", firstMoveAction);

        JButton backButton = new FlatButton(backMoveAction);
        putAction(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "backMove", backMoveAction);
        putAction(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "backMove", backMoveAction);

        JButton nextButton = new FlatButton(nextMoveAction);
        putAction(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "nextMove", nextMoveAction);
        putAction(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "nextMove", nextMoveAction);

        JButton lastButton = new FlatButton(lastMoveAction);
        putAction(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), "lastMove", lastMoveAction);

        JButton rotClockButton = new FlatButton(new ImageIcon(MoveTreeModel.class.getResource("images/rotate_clockwise_big.gif")));
        rotClockButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            }
        });
        rotClockButton.setToolTipText("Rotate clockwise (not coded yet)");
        rotClockButton.setEnabled(false);

        JButton rotCounterButton = new FlatButton(new ImageIcon(MoveTreeModel.class.getResource("images/rotate_counter_big.gif")));
        rotCounterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            }
        });
        rotCounterButton.setToolTipText("Rotate counter-clockwise (not coded yet)");
        rotCounterButton.setEnabled(false);

		JPanel top = new JPanel();
		top.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

		top.add(firstButton);
		top.add(backButton);
		top.add(nextButton);
		top.add(lastButton);
		top.add(rotClockButton);
		top.add(rotCounterButton);

		//TODO since these are used in 3 places, store names in one location
		FlatButton good = new FlatButton(new ImageIcon(MoveTreeModel.class.getResource("images/good2.png")));
		good.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setNodeType(PlunkNode.GOOD);
			}
		});
		good.setToolTipText("Good move");

		FlatButton verygood = new FlatButton(new ImageIcon(MoveTreeModel.class.getResource("images/very_good.png")));
		verygood.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setNodeType(PlunkNode.VERY_GOOD);
			}
		});
		verygood.setToolTipText("Very good move");

		FlatButton bad = new FlatButton(new ImageIcon(MoveTreeModel.class.getResource("images/bad2.png")));
		bad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setNodeType(PlunkNode.BAD);
			}
		});
		bad.setToolTipText("Bad move");

		FlatButton verybad = new FlatButton(new ImageIcon(MoveTreeModel.class.getResource("images/very_bad.png")));
		verybad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setNodeType(PlunkNode.VERY_BAD);
			}
		});
		verybad.setToolTipText("Very bad move");
		FlatButton interesting = new FlatButton(new ImageIcon(MoveTreeModel.class.getResource("images/interesting.png")));
		interesting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setNodeType(PlunkNode.INTERESTING);
			}
		});
		interesting.setToolTipText("Interesting move");
		FlatButton noType = new FlatButton(new ImageIcon(MoveTreeModel.class.getResource("images/no_type.png")));
		noType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setNodeType(PlunkNode.NO_TYPE);
			}
		});
		noType.setToolTipText("Blank");


		JPanel bottom = new JPanel();
		bottom.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		bottom.add(good);
		bottom.add(verygood);
		bottom.add(bad);
		bottom.add(verybad);
		bottom.add(interesting);
		bottom.add(noType);

		JPanel all = new JPanel();
		all.setLayout(new BorderLayout());
		all.add(top, BorderLayout.NORTH);
		all.add(bottom, BorderLayout.SOUTH);
		all.setMinimumSize(all.getPreferredSize());
		return all;
	}

	/** called after node name is edited */
	public void valueForPathChanged(TreePath path,
            Object newValue) {
		System.out.println("valueForPathChanged " + newValue);

		DefaultMutableTreeNode d = (DefaultMutableTreeNode) path.getLastPathComponent();
		PlunkNode n = (PlunkNode) d.getUserObject();
		n.setName((String) newValue);
		addDirtyNode(n);

		if (currentTreeNode != null) {
			tempUnselectNode();
			selectNode(currentTreeNode);
		}
		notifyListenersNodeChanged();
	}

	public void setNodeType(int type) {
		if (currentNode == null) return;

		currentNode.setType(type);
		addDirtyNode(currentNode);

		nodeChanged(currentTreeNode);
	}
}
