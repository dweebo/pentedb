package org.pente.game;

/**
 * @author dweebo
 */
public class Time {
	private int minutes;
	private int seconds;
	public Time(int minutes, int seconds) {
		this.minutes = minutes;
		this.seconds = seconds;
	}
	public int getMinutes() {
		return minutes;
	}
	public int getSeconds() {
		return seconds;
	}
	public int getTotalSeconds() {
		return minutes * 60 + seconds;
	}
    public String toString() {
        return "Time: " + minutes + ":" + seconds;
    }
}
