package org.pente.gameDatabase.swing;

import java.lang.Thread.UncaughtExceptionHandler;

import java.awt.Frame;
import javax.swing.*;

/**
 * @author dweebo
 */
public class DefaultExceptionHandler implements UncaughtExceptionHandler
{
    public void uncaughtException(Thread t, Throwable e) {
        // Here you should have a more robust, permanent record of problems
        JOptionPane.showMessageDialog(findActiveFrame(),
            e.toString(), "Exception Occurred", JOptionPane.OK_OPTION);
        System.err.println("Uncaught exception");
        e.printStackTrace();
    }
    private Frame findActiveFrame() {
        Frame[] frames = Frame.getFrames();
        for (int i = 0; i < frames.length; i++) {
            if (frames[i].isVisible()) {
                return frames[i];
            }
        }
        return null;
    }
}
