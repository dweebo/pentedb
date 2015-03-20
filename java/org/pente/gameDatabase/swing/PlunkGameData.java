package org.pente.gameDatabase.swing;

import org.pente.game.GameData;
import org.pente.game.DefaultGameData;

/**
 * @author dweebo
 */
public class PlunkGameData extends DefaultGameData {

	private PlunkNode root;
	private String dbName;

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public PlunkGameData() {
	}


    public boolean equals(Object obj) {

        // make sure the object to compare is an implementation
        // of GameData
        if (!(obj instanceof PlunkGameData)) {
            return false;
        }

        PlunkGameData data = (PlunkGameData) obj;

//
//        if (!nullSafeEquals(getDate(), data.getDate())) {
//            return false;
//        }

        if (!nullSafeEquals(getEvent(), data.getEvent())) {
            return false;
        }

        if (!nullSafeEquals(getGame(), data.getGame())) {

            return false;
        }

        if (getIncrementalTime() != data.getIncrementalTime()) {
            return false;
        }

        if (getInitialTime() != data.getInitialTime()) {
            return false;
        }

        if (getNumMoves() != data.getNumMoves()) {
            return false;
        }
        for (int i = 0; i < getNumMoves(); i++) {
            if (getMove(i) != data.getMove(i)) {
                return false;
            }
        }

        if (!nullSafeEquals(getPlayer1Data().getUserIDName(), data.getPlayer1Data().getUserIDName())) {
            return false;
        }

        if (!nullSafeEquals(getPlayer2Data().getUserIDName(), data.getPlayer2Data().getUserIDName())) {
            return false;
        }

        if (getRated() != data.getRated()) {
            return false;
        }

        if (!nullSafeEquals(getRound(), data.getRound())) {
            return false;
        }

        if (!nullSafeEquals(getSection(), data.getSection())) {
            return false;
        }

        if (!nullSafeEquals(getSite(), data.getSite())) {
            return false;
        }

        if (getTimed() != data.getTimed()) {
            return false;
        }

        if (getWinner() != data.getWinner()) {
            return false;
        }

//        if (!nullSafeEquals(dbName, data.getDbName())) {
//            return false;
//        }
        return true;
    }


    public PlunkGameData(GameData d) {
		setDate(d.getDate());
		setEvent(d.getEvent());
		setGame(d.getGame());
		setGameID(d.getGameID());
		setIncrementalTime(d.getIncrementalTime());
		setInitialTime(d.getInitialTime());
		setPlayer1Data(d.getPlayer1Data());
		setPlayer2Data(d.getPlayer2Data());
		setPrivateGame(d.isPrivateGame());
		setRated(d.getRated());
		setRound(d.getRound());
		setSection(d.getSection());
		setShortSite(d.getShortSite());
		setSite(d.getSite());
		setSiteURL(d.getSiteURL());
		setSwapped(d.didPlayersSwap());
		setTimed(d.getTimed());
		setWinner(d.getWinner());

//		setStored(d.isStored());
//		setEditable(d.isEditable());

		if (d.getNumMoves() > 0) {
			for (int i = 0; i < d.getNumMoves(); i++) {
				addMove(d.getMove(i));
			}
			if (root == null) {
				setRoot(Utilities.convertGame(this));
			}
		}
		else if (d instanceof PlunkGameData) {
			PlunkGameData pgd = (PlunkGameData) d;
			if (pgd.getRoot() != null) {
				PlunkNode n = pgd.getRoot();
				PlunkNode p = null;
				while (n != null) {
					PlunkNode copy = new PlunkNode(n);
					if (p == null) {
						root = copy;
						p = root;
					}
					else {
						copy.setParent(p);
						p = copy;
					}

					addMove(n.getMove());
					if (!n.hasChildren()) break;
					n = n.getChildren().get(0);
				}
			}
		}
	}
	public PlunkNode getRoot() {
		return root;
	}

	public void setRoot(PlunkNode root) {
		this.root = root;
	}

}
