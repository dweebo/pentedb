package org.pente.game;

/**
 * @author dweebo
 */
public class Game {

    private int id;
    private String name;
    private boolean speed;

    public Game(int id, String name, boolean speed) {
        this.id = id;
        this.name = name;
        this.speed = speed;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public boolean isSpeed() {
        return speed;
    }

    //  < 5 1/2 minutes with/without incremental time
    public static boolean isSpeedGame(int initial, int incremental) {
        return (initial * 60 + incremental * 15) < 331;
    }
    public String toString() {
    	return name;
    }
}
