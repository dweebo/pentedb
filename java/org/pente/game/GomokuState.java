package org.pente.game;

/**
 * @author dweebo
 */
public interface GomokuState extends GridState {

    public void allowOverlines(boolean allow);
    public boolean areOverlinesAllowed();
}
