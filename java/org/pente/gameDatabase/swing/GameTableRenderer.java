package org.pente.gameDatabase.swing;

import java.awt.Component;

import javax.swing.*;

import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author dweebo
 */
public class GameTableRenderer extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int col) {

		ImageIcon i = new ImageIcon(GameReviewBoard.class.getResource("images/" +
			ViewGamePanel.getRatingsGifRatingOnly(((Integer)value).intValue())));

		setIcon(i);

		return super.getTableCellRendererComponent(
			table, value, isSelected, hasFocus, row, col);
	}
}
