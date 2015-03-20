package org.pente.gameDatabase.swing;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import org.pente.game.GridStateFactory;
import org.pente.gameServer.core.AlphaNumericGridCoordinates;
import org.pente.gameServer.core.GridCoordinates;

import org.pente.game.*;

import org.apache.log4j.*;

/**
 * @author dweebo
 */
public class MoveIconRenderer implements TreeCellRenderer {

	private Category log4j = Category.getInstance(MoveIconRenderer.class.getName());

    private static final GridCoordinates coordinates =
		new AlphaNumericGridCoordinates(19, 19);

    private MoveTreeModel model;
    public MoveIconRenderer(MoveTreeModel model) {
    	this.model = model;
    }

    private int game;
    private boolean rotate;
    public void setGame(int game) {
    	this.game = game;
    }
    public void setRotate(boolean rotate) {
    	this.rotate = rotate;
    }

    //TODO show end of game icon?

	public Component getTreeCellRendererComponent(JTree tree, Object value,
		boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		return getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus, false);
	}
	public Component getTreeCellRendererComponent(JTree tree, Object value,
		boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus,
		boolean forEditing) {

		if (value != null && value instanceof DefaultMutableTreeNode)
		{
			DefaultMutableTreeNode tn = (DefaultMutableTreeNode) value;
			if (tn.getUserObject() instanceof String) return new Label(""); // for root, can't seem to get rid of this


			PlunkNode n = (PlunkNode) tn.getUserObject();

			//log4j.debug("getTreeCellRenderComponent " + n.getDepth() + ":" + n.getMove());

			JLabel moveLabel = new JLabel();

			//String text = coordinates.getCoordinate(n.getMove());

			int rotMove = !rotate ? n.getMove() : getRotateMove(n);
			//text += ":" + coordinates.getCoordinate(rotMove);
			String text =
				//n.getMove() + ":" + n.getRotation() + ":" +
				coordinates.getCoordinate(rotMove);

			if (forEditing) {
				text += ": ";
			}
			else if (n.getName() != null && !n.getName().equals("")) {
				text += ": " + n.getName();
			}
			moveLabel.setText(text);

			MoveIcon mi = new MoveIcon(tree.getRowHeight(),
				GridStateFactory.getColor(n.getDepth(), game), n.getDepth() + 1);
			moveLabel.setIcon(mi);
			moveLabel.setIconTextGap(2);
			moveLabel.setVerticalTextPosition(JLabel.CENTER);

			if (selected) {
				moveLabel.setOpaque(true);
				moveLabel.setForeground(UIManager.getColor("Tree.selectionForeground"));
				moveLabel.setBackground(UIManager.getColor("Tree.selectionBackground"));
			}

			if (!forEditing && n.getType() != PlunkNode.NO_TYPE) {
				JPanel p = new JPanel();
				p.setBackground(UIManager.getColor("Tree.textBackground"));
				p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				p.add(moveLabel);
				JLabel img = null;
				switch (n.getType()) {
				case PlunkNode.GOOD:
					img = new JLabel(new ImageIcon(MoveIconRenderer.class.getResource("images/good2.png")));
					break;
				case PlunkNode.VERY_GOOD:
					img = new JLabel(new ImageIcon(MoveIconRenderer.class.getResource("images/very_good.png")));
					break;
				case PlunkNode.BAD:
					img = new JLabel(new ImageIcon(MoveIconRenderer.class.getResource("images/bad2.png")));
					break;
				case PlunkNode.VERY_BAD:
					img = new JLabel(new ImageIcon(MoveIconRenderer.class.getResource("images/very_bad.png")));
					break;
				case PlunkNode.INTERESTING:
					img = new JLabel(new ImageIcon(MoveIconRenderer.class.getResource("images/interesting.png")));
					break;
				}
				img.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
				p.add(img);
				return p;
			}
			else {
				return moveLabel;
			}
		}
		else {
			return null;
		}
	}

    private int getRotateMove(PlunkNode n) {

    	GridState g = GridStateFactory.createGridState(game);
    	GridState board = model.getBoardGridState();
    	PlunkNode path[] = n.getPath();

    	// wasn't working if beginning moves don't hash to the board
    	// but later moves do
    	//instead go backwards from board moves and find the last position
    	//with same hash and use board moves up until that point
    	int lastDepthHashMatch = -1;
    	for (int i = Math.min(path.length, board.getNumMoves()); i > 0; i--) {
    		if (path[i-1].getHash() == board.getHash(i - 1)) {
    			lastDepthHashMatch = i - 1;
    			break;
    		}
    	}
    	if (lastDepthHashMatch > -1) {
	    	for (int i = 0; i <= lastDepthHashMatch; i++) {
	    		g.addMove(board.getMove(i));
	    	}
    	}
    	//} else {
    	//	lastDepthHashMatch = 0;
    	//}
    	for (int i = lastDepthHashMatch + 1; i < path.length; i++) {
    		PlunkNode p = path[i];
    		if (i == 0) {
    			g.addMove(p.getMove());
    		}
    		//else if (p.getDepth() < board.getNumMoves() && p.getHash() == board.getHash(p.getDepth())) {
    		//	g.addMove(board.getMove(p.getDepth()));
    		//}
    		else {
    			int possMoves[] = g.getAllPossibleRotations(p.getMove(), p.getParent().getRotation());
    			boolean foundSame = false;
    			for (int pm : possMoves) {
    				if (pm == p.getMove()) {
    					g.addMove(pm);
    					foundSame = true;
    					break;
    				}
    			}
    			if (!foundSame) {
    				int move = g.rotateMoveToLocalRotation(
    						p.getMove(), p.getParent().getRotation());
    				g.addMove(move);
    			}
    		}
    	}
    	return g.getMove(g.getNumMoves()-1);
    }
}
