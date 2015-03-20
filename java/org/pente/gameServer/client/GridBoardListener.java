package org.pente.gameServer.client;

/**
 * @author dweebo
 */
public interface GridBoardListener {
    public void gridClicked(int x, int y, int button);
    public void gridMoved(int x, int y);
}