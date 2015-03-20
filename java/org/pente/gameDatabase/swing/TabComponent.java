package org.pente.gameDatabase.swing;

import javax.swing.JComponent;
import java.io.IOException;

/**
 * @author dweebo
 */
public interface TabComponent {

	public boolean close();
	public void showTabPopup(JComponent component, int x, int y);
	public void hideTabPopup();

	public void addEditListener(TabComponentEditListener l);

	public int save();
	public void search();
	public void nextMoves();
	public void numberMoves();
	public void threats();

	public void export(java.io.File file) throws IOException;
}
