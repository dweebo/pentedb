package org.pente.gameDatabase.swing;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.*;
import javax.swing.tree.*;

/**
 * @author dweebo
 */
public class MoveEditor extends DefaultTreeCellEditor {

	private MoveIconRenderer renderer;

	public MoveEditor(JTree tree, MoveIconRenderer renderer) {
		super(tree, null);

		this.renderer = renderer;
	}


	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value,
		boolean isSelected, boolean expanded, boolean leaf, int row) {

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panel.add(renderer.getTreeCellRendererComponent(tree, value, isSelected,
			expanded, leaf, row, true, true));

		Component c = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
		JTextField f = (JTextField) editingComponent;
		DefaultMutableTreeNode r = (DefaultMutableTreeNode) lastPath.getLastPathComponent();
		//TODO allow editing tree's name?
		PlunkNode n = (PlunkNode) r.getUserObject();
		if (n.getName() != null) {
			f.setText(n.getName());
		}
		else {
			f.setText("");
		}
		panel.add(c);
		return panel;
	}
}
