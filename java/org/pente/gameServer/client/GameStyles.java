package org.pente.gameServer.client;

import java.awt.*;

/**
 * Use this class to have a consistent color scheme throughout the game.
 * @author dweebo
 */
public class GameStyles {

    public static Color white[] =       { new Color(220, 220, 220), new Color(240, 240, 240) };
    public static Color black[] =       { new Color(0,   0,   0),   new Color(32,  32,  32)  };
    public static Color red[] =         { new Color(255, 0,   0),   new Color(255, 50,  50)  };
    public static Color orange[] =      { new Color(236, 122, 0),   new Color(255, 146, 29)  };
    public static Color yellow[] =      { new Color(255, 230, 0),   new Color(255, 255, 70)  };
    public static Color blue[] =        { new Color(0,   3,   126), new Color(0,   5,   160) };
    public static Color green[] =       { new Color(0,   82,  0),   new Color(0,   105, 0)   };
    public static Color purple[] =      { new Color(107, 2,   108), new Color(142, 0,   144) };
    public static Color colors[][] =    { white, black, red, orange, yellow, blue, green, purple };

    /**
     * The color of the background.
     */
    public Color boardBack;

    /**
     * The color of the background of buttons.
     */
    public Color buttonBack;

    public Color buttonDisabled;

    /**
     * The color of the foreground of buttons.
     */
    public Color buttonFore;

    /**
     * The color of player 1. Use it in TablePanel, and in Replay Panels, possibly for pieces.
     */
    public Color player1;

    /**
     * The color of player 2. Use it in TablePanel, and in Replay Panels, possibly for pieces.
     */
    public Color player2;

    public Color player1Fore;

    public Color player2Fore;

    /**
     * The color of watchers. Use it in TablePanel.
     */
    public Color foreGround;

    /**
     * Constructor.
     */
    public GameStyles(Color boardBack, Color buttonBack, Color buttonFore, Color buttonDisabled, Color player1, Color player1Fore, Color player2, Color player2Fore, Color foreGround)
    {
        this.boardBack=boardBack;
        this.buttonBack=buttonBack;
        this.buttonFore=buttonFore;
        this.buttonDisabled=buttonDisabled;
        this.player1=player1;
        this.player2=player2;
        this.player1Fore=player1Fore;
        this.player2Fore=player2Fore;
        this.foreGround=foreGround;
    }
	public GameStyles() {
		this(new Color(0, 102, 153), //board back
	         new Color(188, 188, 188), //button back
	         Color.black, //button fore
	         new Color(64, 64, 64), //new Color(0, 102, 255), //button disabled
	         Color.white, //player 1 back
	         Color.black, //player 1 fore
	         Color.black, //player 2 back
	         Color.white, //player 2 fore
	         new Color(188, 188, 188)); //foreGround
	}

    public Button createDSGButton(String label) {
    	Button b = new Button(label);

        b.setForeground(buttonFore);
        b.setBackground(buttonBack);

        return b;
    }
}
