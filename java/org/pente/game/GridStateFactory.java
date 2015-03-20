package org.pente.game;

/**
 * @author dweebo
 */
public class GridStateFactory {

    public static final int PENTE = 1;
    public static final int SPEED_PENTE = PENTE + 1;
    public static final int KERYO = 3;
    public static final int SPEED_KERYO = KERYO + 1;
    public static final int GOMOKU = 5;
    public static final int SPEED_GOMOKU = GOMOKU + 1;
    public static final int DPENTE = 7;
    public static final int SPEED_DPENTE = DPENTE + 1;
    public static final int GPENTE = 9;
    public static final int SPEED_GPENTE = GPENTE + 1;
    public static final int POOF_PENTE = 11;
    public static final int SPEED_POOF_PENTE = POOF_PENTE + 1;
    public static final int CONNECT6 = 13;
    public static final int SPEED_CONNECT6 = CONNECT6 + 1;
    public static final int BOAT_PENTE = 15;
    public static final int SPEED_BOAT_PENTE = BOAT_PENTE + 1;

	// 50 + normal game for turn-based games
	// only used for separate ratings
	private static final int TB_START = 50;
	public static final int TB_PENTE = TB_START + PENTE;
	public static final int TB_KERYO = TB_START + KERYO;
	public static final int TB_GOMOKU = TB_START + GOMOKU;
	public static final int TB_DPENTE = TB_START + DPENTE;
	public static final int TB_GPENTE = TB_START + GPENTE;
	public static final int TB_POOF_PENTE = TB_START + POOF_PENTE;
	public static final int TB_CONNECT6 = TB_START + CONNECT6;
	public static final int TB_BOAT_PENTE = TB_START + BOAT_PENTE;


    public static final Game PENTE_GAME = new Game(PENTE, "Pente", false);
    public static final Game SPEED_PENTE_GAME = new Game(SPEED_PENTE, "Speed Pente", true);
    public static final Game TB_PENTE_GAME = new Game(TB_PENTE, "Pente", false);
	public static final Game KERYO_GAME = new Game(KERYO, "Keryo-Pente", false);
    public static final Game SPEED_KERYO_GAME = new Game(SPEED_KERYO, "Speed Keryo-Pente", true);
    public static final Game TB_KERYO_GAME = new Game(TB_KERYO, "Keryo-Pente", false);
    public static final Game GOMOKU_GAME = new Game(GOMOKU, "Gomoku", false);
    public static final Game SPEED_GOMOKU_GAME = new Game(SPEED_GOMOKU, "Speed Gomoku", true);
    public static final Game TB_GOMOKU_GAME = new Game(TB_GOMOKU, "Gomoku", false);
    public static final Game DPENTE_GAME = new Game(DPENTE, "D-Pente", false);
    public static final Game SPEED_DPENTE_GAME = new Game(SPEED_DPENTE, "Speed D-Pente", true);
    public static final Game TB_DPENTE_GAME = new Game(TB_DPENTE, "D-Pente", false);
    public static final Game GPENTE_GAME = new Game(GPENTE, "G-Pente", false);
    public static final Game SPEED_GPENTE_GAME = new Game(SPEED_GPENTE, "Speed G-Pente", true);
    public static final Game TB_GPENTE_GAME = new Game(TB_GPENTE, "G-Pente", false);
    public static final Game POOF_PENTE_GAME = new Game(POOF_PENTE, "Poof-Pente", false);
    public static final Game SPEED_POOF_PENTE_GAME = new Game(SPEED_POOF_PENTE, "Speed Poof-Pente", true);
    public static final Game TB_POOF_PENTE_GAME = new Game(TB_POOF_PENTE, "Poof-Pente", false);
	public static final Game CONNECT6_GAME = new Game(CONNECT6, "Connect6", false);
	public static final Game SPEED_CONNECT6_GAME = new Game(SPEED_CONNECT6, "Speed Connect6", true);
	public static final Game TB_CONNECT6_GAME = new Game(TB_CONNECT6, "Connect6", false);
	public static final Game BOAT_PENTE_GAME = new Game(BOAT_PENTE, "Boat-Pente", false);
	public static final Game SPEED_BOAT_PENTE_GAME = new Game(SPEED_BOAT_PENTE, "Speed Boat-Pente", true);
	public static final Game TB_BOAT_PENTE_GAME = new Game(TB_BOAT_PENTE, "Boat-Pente", false);

    private static final Game allGames[] = {
        null, PENTE_GAME, SPEED_PENTE_GAME, KERYO_GAME, SPEED_KERYO_GAME,
        GOMOKU_GAME, SPEED_GOMOKU_GAME, DPENTE_GAME, SPEED_DPENTE_GAME,
        GPENTE_GAME, SPEED_GPENTE_GAME, POOF_PENTE_GAME, SPEED_POOF_PENTE_GAME,
        CONNECT6_GAME, SPEED_CONNECT6_GAME, BOAT_PENTE_GAME, SPEED_BOAT_PENTE_GAME
    };
	private static final Game displaygames[] = {
		PENTE_GAME,
		KERYO_GAME,
        GOMOKU_GAME,
        CONNECT6_GAME,
        BOAT_PENTE_GAME,
        DPENTE_GAME,
        GPENTE_GAME,
        POOF_PENTE_GAME,
		new Game(TB_PENTE, "Turn-based Pente", false),
		new Game(TB_KERYO, "Turn-based Keryo-Pente", false),
        new Game(TB_GOMOKU, "Turn-based Gomoku", false),
        new Game(TB_CONNECT6, "Turn-based Connect6", false),
        new Game(TB_BOAT_PENTE, "Turn-based Boat-Pente", false),
        new Game(TB_DPENTE, "Turn-based D-Pente", false),
        new Game(TB_GPENTE, "Turn-based G-Pente", false),
        new Game(TB_POOF_PENTE, "Turn-based Poof-Pente", false),
		SPEED_PENTE_GAME,
		SPEED_KERYO_GAME,
        SPEED_GOMOKU_GAME,
        SPEED_CONNECT6_GAME,
        SPEED_BOAT_PENTE_GAME,
        SPEED_DPENTE_GAME,
        SPEED_GPENTE_GAME,
        SPEED_POOF_PENTE_GAME
	};

    private static final Game normalGames[] = {
        PENTE_GAME, KERYO_GAME,
        GOMOKU_GAME, DPENTE_GAME,
        GPENTE_GAME, POOF_PENTE_GAME,
        CONNECT6_GAME, BOAT_PENTE_GAME
    };
    private static final Game speedGames[] = {
        SPEED_PENTE_GAME, SPEED_KERYO_GAME,
        SPEED_GOMOKU_GAME, SPEED_DPENTE_GAME,
        SPEED_GPENTE_GAME, SPEED_POOF_PENTE_GAME,
        SPEED_CONNECT6_GAME, SPEED_BOAT_PENTE_GAME
    };
    private static final Game tbGames[] = {
        TB_PENTE_GAME, TB_KERYO_GAME,
		TB_GOMOKU_GAME, TB_DPENTE_GAME,
		TB_GPENTE_GAME, TB_POOF_PENTE_GAME,
		TB_CONNECT6_GAME, TB_BOAT_PENTE_GAME
    };

    private static final GridState gridStates[] = new GridState[getNumGames() + 1];
	private static final GridState tbGridStates[] = new GridState[tbGames.length];
    static {
        for (int i = 1; i < gridStates.length; i++) {
            gridStates[i] = createGridState(i);
        }
        for (int i = 0; i < tbGridStates.length; i++) {
            tbGridStates[i] = createGridState(tbGames[i].getId());
        }
    }

    /** Prevent instantiation */
    private GridStateFactory() {
    }

    public static GridState createGridState(int game) {
        return createGridState(game, 19, 19);
    }
    public static GridState createGridState(int game, int x, int y) {
        SimpleGomokuState gomoku = new SimpleGomokuState(x, y);
        gomoku.setDoHashes(false);
        switch (game) {
            case PENTE:
            case SPEED_PENTE:
            case TB_PENTE:
                gomoku.allowOverlines(true);
                PenteState penteState = new SimplePenteState(gomoku);
                penteState.setTournamentRule(true);
                return penteState;
            case KERYO:
            case SPEED_KERYO:
            case TB_KERYO:
                gomoku.allowOverlines(true);
                PenteState keryoState = new SimplePenteState(gomoku);
                keryoState.setTournamentRule(true);
                keryoState.setCaptureLengths(new int[] { 2, 3 });
                keryoState.setCapturesToWin(15);
                return keryoState;
            case GOMOKU:
            case SPEED_GOMOKU:
            case TB_GOMOKU:
                gomoku.setDoHashes(true);
                gomoku.allowOverlines(false);
                return gomoku;
            case GPENTE:
            case SPEED_GPENTE:
            case TB_GPENTE:
                gomoku.allowOverlines(true);
                PenteState gpenteState = new SimplePenteState(gomoku);
                gpenteState.setTournamentRule(true);
                gpenteState.setGPenteRules(true);
                return gpenteState;
            case POOF_PENTE:
            case SPEED_POOF_PENTE:
            case TB_POOF_PENTE:
                gomoku.allowOverlines(true);
                PenteState poofState = new SimplePoofPenteState(gomoku);
                poofState.setGPenteRules(false);
                poofState.setTournamentRule(true);
                return poofState;
            case DPENTE:
            case SPEED_DPENTE:
            case TB_DPENTE:
                gomoku.allowOverlines(true);
                PenteState dpenteState = new SimplePenteState(gomoku);
                dpenteState.setTournamentRule(false);
                dpenteState.setDPenteRules(true);
                return dpenteState;
            case CONNECT6:
            case SPEED_CONNECT6:
            case TB_CONNECT6:
            	return new SimpleConnect6State(x, y);
            case BOAT_PENTE:
            case SPEED_BOAT_PENTE:
            case TB_BOAT_PENTE:
            	gomoku.allowOverlines(true);
            	return new BoatPenteState(gomoku);

        }

        return null;
    }

    public static GridState createGridState(int game, MoveData moveData) {
		if (game > TB_START) {
			return tbGridStates[(game - TB_START - 1) / 2].getInstance(moveData);
		}
		else {
	        return gridStates[game].getInstance(moveData);
		}
    }

    public static Game getGame(int game) {
		if (game > TB_START) {
			return tbGames[(game - TB_START - 1) / 2];
		}
		else {
	        return allGames[game];
		}
    }
    public static boolean isValidGame(int game) {
        if (game < PENTE || game > SPEED_BOAT_PENTE) return false;
        return true;
    }
    public static String getGameName(int game) {
		return getGame(game).getName();
    }

    public static int getGameId(String gameName) throws IllegalArgumentException {
        for (int i = 1; i < allGames.length; i++) {
            if (allGames[i].getName().equals(gameName)) {
                return allGames[i].getId();
            }
        }
        for (int i = 0; i < tbGames.length; i++) {
            if (tbGames[i].getName().equals(gameName)) {
                return tbGames[i].getId();
            }
        }
        throw new IllegalArgumentException("Invalid game: " + gameName);
    }

    public static int getNumGames() {
        return allGames.length - 1;
    }
	public static int getMaxGameId() {
		return TB_BOAT_PENTE;
	}

    public static Game[] getAllGames() {
        return allGames;
    }
    public static String getDisplayName(int game) {
    	for (int i = 0; i < displaygames.length; i++) {
    		if (displaygames[i].getId() == game) {
    			return displaygames[i].getName();
    		}
    	}
    	return null;
    }
	public static Game[] getDisplayGames() {
		return displaygames;
	}
    public static Game[] getSpeedGames() {
        return speedGames;
    }
    public static Game[] getNormalGames() {
        return normalGames;
    }
    public static Game[] getTbGames() {
        return tbGames;
    }
    public static Game getSpeedGame(Game normalGame) {
        return allGames[normalGame.getId() + 1];
    }
    public static Game getNormalGame(Game speedGame) {
        return allGames[speedGame.getId() - 1];
    }
    public static int getNormalGameFromTurnbased(int game) {
    	return game - TB_START;
    }

    public static boolean isSpeedGame(int game) {
        return game < TB_START && (game % 2) == 0;
    }

    public static boolean isTurnbasedGame(int game) {
        return game > TB_START;
    }

    public static int getColor(int moveNum, int game) {
    	return gridStates[game].getColor(moveNum);
    }
}