package org.pente.gameDatabase.swing.importer;

import java.io.InputStream;

/**
 * @author dweebo
 */
public class SGFGameImporter extends GameImporterAdapter {


	public boolean attemptImport(InputStream in, GameImporterListener l) {
		boolean imported = false;

		SGFGameFormat gf = new SGFGameFormat("\r\n", "MM/dd/yyyy");

		try {
			imported = gf.parse2(in, l);

			/*
			// if more than 5 items imported then save them
			if (objs.size() > 5 && !save && open) {
				save = true;
				open = false;
			}

			for (Object o : objs) {

				if (save) {
					if (o instanceof PlunkTree) {
						PlunkTree pt = (PlunkTree) o;
						System.out.println("found tree " + pt.getName());

						main.getPlunkDbUtil().storePlunkTree(pt);
						main.getPlunkDbUtil().insertPlunkNodes(pt.getRoot(), pt.getTreeId());
						main.addPlunkTree(pt);
					}
					else if (o instanceof PlunkGameData) {
						PlunkGameData gd = (PlunkGameData) o;

						// find dbid or create a new one
						List<GameDbData> dbs = main.getVenueStorer().getDbTree();
						GameDbData db = null;
						for (GameDbData db2 : dbs) {
							if (db2.getName().equals(gd.getDbName())) {
								db = db2;
								break;
							}
						}
						if (db == null) {
							db = new SimpleGameDbData();
							db.setName(gd.getDbName());
							main.getVenueStorer().addGameDbData(db,
								GridStateFactory.getGameId(gd.getGame()));
						}

						main.getGameStorer().storeGame(gd, db);
						List<PlunkNode> moves = Utilities.getAllNodes(gd.getRoot());
						main.getPlunkDbUtil().updateMoves(moves, gd.getGameID());
					}

					if (open) {
						if (o instanceof PlunkTree) {
							PlunkTree pt = (PlunkTree) o;
							System.out.println("found tree " + pt.getName());

							//TODO might configure below from import file?
							GameStorerSearchRequestFilterData filterData = new SimpleGameStorerSearchRequestFilterData();
							filterData.setDb(1);
							filterData.setGame(1);

							TabComponentEditListener l = main.addGameReviewTab(filterData, pt, null, pt.getName());
							if (!save) {
								l.editsMade();
							}
						}
						else if (o instanceof PlunkGameData) {
							PlunkGameData gd = (PlunkGameData) o;
							System.out.println("found game " +gd.getGameID());
							main.addGameViewTab(gd);
						}
					}
				}
			}
			*/

		} catch (Throwable t) {
			t.printStackTrace();
		}

		return imported;
	}
}