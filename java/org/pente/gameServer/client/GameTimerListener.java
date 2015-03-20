package org.pente.gameServer.client;

/**
 * @author dweebo
 */
public interface GameTimerListener {
    public void timeChanged(int newSeconds, int newMinutes);
}