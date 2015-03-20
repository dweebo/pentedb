package org.pente.gameDatabase.swing;

/**
 * @author dweebo
 */
public interface MoveChangeListener {

	public void changeMoves(int moves[], PlunkNode current);
	public void nodeChanged();
}
