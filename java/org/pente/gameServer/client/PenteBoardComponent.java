package org.pente.gameServer.client;

/**
 * @author dweebo
 */
public interface PenteBoardComponent extends GridBoardComponent {
    public void incrementCaptures(int player);
    public void decrementCaptures(int player);
}