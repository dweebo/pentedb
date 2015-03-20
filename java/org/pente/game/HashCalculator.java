package org.pente.game;

/**
 * @author dweebo
 */
public interface HashCalculator {
    public long calcHash(long cHash, int p, int move, int rot);
}
