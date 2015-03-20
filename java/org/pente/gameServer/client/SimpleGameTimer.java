package org.pente.gameServer.client;

import java.util.*;

/**
 * @author dweebo
 */
public class SimpleGameTimer implements GameTimer, Runnable {

    private int startMinutes;
    private int startSeconds;

    private Object timeLock;
    private int minutes;
    private int seconds;

    private Thread thread;
    private boolean running;

    private boolean alive;

    private Vector listeners;

    public SimpleGameTimer() {
        alive = true;
        listeners = new Vector();
        timeLock = new Object();
    }

    public void addGameTimerListener(GameTimerListener listener) {
        listeners.addElement(listener);
    }
    public void removeGameTimerListener(GameTimerListener listener) {
        listeners.removeElement(listener);
    }
    private void timeChanged(int newMinutes, int newSeconds) {

        for (int i = 0; i < listeners.size(); i++) {
            GameTimerListener listener = (GameTimerListener) listeners.elementAt(i);
            listener.timeChanged(newMinutes, newSeconds);
        }
    }

    public boolean isRunning() {
        synchronized (timeLock) {
        	return running;
        }
    }
    public void setStartMinutes(int minutes) {
        this.startMinutes = minutes;
    }
    public int getStartMinutes() {
        return startMinutes;
    }

    public void setStartSeconds(int seconds) {
        this.startSeconds = seconds;
    }
    public int getStartSeconds() {
        return startSeconds;
    }

    public int getMinutes() {
        synchronized (timeLock) {
            return minutes;
        }
    }

    public int getSeconds() {
        synchronized (timeLock) {
            return seconds;
        }
    }

	public void incrementMillis(int incrementMillis) {
		increment(incrementMillis / 1000);
	}
	public void increment(int incrementSeconds) {

		synchronized (timeLock) {
			seconds += incrementSeconds;
			if (seconds > 59) {
				minutes++;
				seconds = seconds - 60;
			}

			timeChanged(minutes, seconds);
		}
	}
	public void adjust(int newMinutes, int newSeconds, int newMillis) {
		adjust(newMinutes, newSeconds);
	}
    public void adjust(int newMinutes, int newSeconds) {

        synchronized (timeLock) {
            minutes = newMinutes;
            seconds = newSeconds;

            timeChanged(minutes, seconds);
        }
    }
    public void reset() {

        synchronized (timeLock) {
            minutes = startMinutes;
            seconds = startSeconds;

            timeChanged(minutes, seconds);
        }
    }

    public void go() {

        synchronized (timeLock) {

            if (thread == null) {
                thread = new Thread(this, "SimpleGameTimer");
                thread.start();
            }

            running = true;
            timeLock.notify();
        }
    }
    public void stop() {

        synchronized (timeLock) {
            running = false;
            timeLock.notify();
        }
    }

    public void destroy() {
        synchronized (timeLock) {
            alive = false;

            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
            if (listeners != null) {
                listeners.removeAllElements();
            }
        }
    }

    public void run() {

        while (true) {

            synchronized (timeLock) {
                if (!alive) {
                    return;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }

                if (!alive) {
                    return;
                }

                while (!running) {
                    try {
                        timeLock.wait();
                    } catch (InterruptedException ex) {
                    }
                }
                if (!alive) {
                    return;
                }

                seconds--;

                if (seconds < 0) {
                    seconds = 59;
                    minutes--;
                }
            }
            timeChanged(minutes, seconds);
        }
    }
}