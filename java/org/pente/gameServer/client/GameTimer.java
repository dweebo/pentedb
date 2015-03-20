package org.pente.gameServer.client;

/**
 * @author dweebo
 */
public interface GameTimer {

    public void addGameTimerListener(GameTimerListener listener);
    public void removeGameTimerListener(GameTimerListener listener);

    public void setStartMinutes(int minutes);
    public int getStartMinutes();

    public void setStartSeconds(int seconds);
    public int getStartSeconds();

	public void increment(int incrementSeconds);
	public void incrementMillis(int incrementMillis);
    public void adjust(int newMinutes, int newSeconds);
    public void adjust(int newMinutes, int newSeconds, int newMillis);

    public int getMinutes();
    public int getSeconds();

    public void reset();

    public void go();
    public boolean isRunning();
    public void stop();

    public void destroy();
}