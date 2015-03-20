package org.pente.gameDatabase.swing;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.pente.database.*;
import org.pente.game.DefaultPlayerData;
import org.pente.game.GameData;
import org.pente.game.GameEventData;
import org.pente.game.GameSiteData;
import org.pente.game.GridStateFactory;
import org.pente.game.PlayerData;

/**
 * @author dweebo
 */
public class PlunkDbUtil {

	private DBHandler dbHandler;
	public PlunkDbUtil(DBHandler dbHandler) {
		this.dbHandler = dbHandler;
	}

	public PlunkProp loadProp(String name) throws ClassNotFoundException,
		IOException, SQLException {

	    PlunkProp p = null;

	    Connection con = null;
	    PreparedStatement stmt = null;
	    ResultSet result = null;

	    try {

	        con = dbHandler.getConnection();

	        stmt = con.prepareStatement(
	            "select value " +
	            "from plunk_prop where name = ?");
	        stmt.setString(1, name);
	        result = stmt.executeQuery();
	        while (result.next()) {
	            Blob blob = result.getBlob(1);
	            ObjectInputStream in = new ObjectInputStream(
	                blob.getBinaryStream());
	            Object value = in.readObject();
	            in.close();

	            p = new PlunkProp(name, value);
	        }

	    } finally {
	        if (result != null) {
	            result.close();
	        }
	        if (stmt != null) {
	            stmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }


	    return p;
	}

	public void storeProp(PlunkProp prop)
		throws ClassNotFoundException, IOException, SQLException  {

        if (isPropStored(prop)) {
            updateProp(prop);
        }
        else {
            insertProp(prop);
        }
	}

	private boolean isPropStored(PlunkProp prop)
	    throws SQLException {

	    Connection con = null;
	    PreparedStatement stmt = null;
	    ResultSet result = null;

	    try {

	        con = dbHandler.getConnection();

	        stmt = con.prepareStatement(
	            "select 1 " +
	            "from plunk_prop " +
	            "where name = ?");
	        stmt.setString(1, prop.getName());
	        result = stmt.executeQuery();

	        return (result.next());

	    } finally {
	        if (result != null) {
	            result.close();
	        }
	        if (stmt != null) {
	            stmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }
	}
	private void insertProp(PlunkProp prop)
	    throws SQLException, IOException {

	    Connection con = null;
	    PreparedStatement stmt = null;
	    ResultSet result = null;

	    try {

	        con = dbHandler.getConnection();

	        stmt = con.prepareStatement(
	            "insert into plunk_prop (name, value) values(?, ?)");
	        stmt.setString(1, prop.getName());

	        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	        ObjectOutputStream out = new ObjectOutputStream(byteOut);
	        out.writeObject(prop.getValue());
	        byte bytes[] = byteOut.toByteArray();
	        ByteArrayInputStream in = new ByteArrayInputStream(bytes);

	        stmt.setBinaryStream(2, in, bytes.length);

	        stmt.executeUpdate();
	        out.close();

	    } finally {
	        if (result != null) {
	            result.close();
	        }
	        if (stmt != null) {
	            stmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }
	}
	private void updateProp(PlunkProp prop) throws ClassNotFoundException,
		IOException, SQLException  {

	    Connection con = null;
	    PreparedStatement stmt = null;
	    ResultSet result = null;

	    try {

	        con = dbHandler.getConnection();

	        stmt = con.prepareStatement(
	            "select name, value " +
	            "from plunk_prop " +
	            "where name = ? " +
	            "for update",
	            ResultSet.TYPE_FORWARD_ONLY,
	            ResultSet.CONCUR_UPDATABLE);
	        stmt.setString(1, prop.getName());

	        result = stmt.executeQuery();
	        if (result.next()) {
	            Blob blob = result.getBlob(2);
	            ObjectOutputStream out = new ObjectOutputStream(
	                blob.setBinaryStream(1));
	            out.writeObject(prop.getValue());
	            out.flush();
	            out.close();
	            result.updateBlob(2, blob);
	            result.updateRow();
	        }

	    } finally {
	        if (result != null) {
	            result.close();
	        }
	        if (stmt != null) {
	            stmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }
	}


	public List<PlunkTree> loadPlunkTrees() throws SQLException {

		List<PlunkTree> trees = new ArrayList<PlunkTree>();

	    Connection con = null;
	    PreparedStatement stmt = null;
	    ResultSet result = null;

	    try {

	        con = dbHandler.getConnection();

	        stmt = con.prepareStatement(
	            "select tree_id, name, version, creator, can_edit_props, " +
	            "last_update_dt, creation_dt " +
	            "from plunk_tree");
	        result = stmt.executeQuery();
	        while (result.next()) {
	            PlunkTree t = new PlunkTree();
	            t.setStored(true);
	            t.setTreeId(result.getLong(1));
	            t.setName(result.getString(2));
	            t.setVersion(result.getString(3));
	            t.setCreator(result.getString(4));
	            t.setCanEditProps(result.getString(5).equals("Y"));
                Timestamp dt = result.getTimestamp(6);
	            t.setLastModified(new java.util.Date(dt.getTime()));
                dt = result.getTimestamp(7);
	            t.setCreated(new java.util.Date(dt.getTime()));

	            trees.add(t);
	        }

	    } finally {
	        if (result != null) {
	            result.close();
	        }
	        if (stmt != null) {
	            stmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }

		return trees;
	}

	public void storePlunkTree(PlunkTree tree) throws SQLException {
		if (tree.isStored()) {
			updatePlunkTree(tree);
		} else {
			insertPlunkTree(tree);
			tree.setStored(true);
		}
	}
	private void updatePlunkTree(PlunkTree tree) throws SQLException {
		Connection con = null;
	    PreparedStatement stmt = null;

	    try {

	        con = dbHandler.getConnection();

	        stmt = con.prepareStatement(
	            "update plunk_tree " +
	            "set name = ?, version = ?, creator = ?, " +
	            "last_update_dt = current_timestamp " +
	            "where tree_id = ?");

	        stmt.setString(1, tree.getName());
	        stmt.setString(2, tree.getVersion());
	        stmt.setString(3, tree.getCreator());
	        stmt.setLong(4, tree.getTreeId());

	        stmt.executeUpdate();

	        tree.setLastModified(new java.util.Date());

	    } finally {

	        if (stmt != null) {
	            stmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }
	}

	private void insertPlunkTree(PlunkTree tree) throws SQLException {
		Connection con = null;
	    PreparedStatement stmt = null;
	    ResultSet result = null;

	    try {

	        con = dbHandler.getConnection();

	        stmt = con.prepareStatement(
	            "insert into plunk_tree (name, version, creator, can_edit_props, " +
	            "last_update_dt, creation_dt) " +
	            "values(?, ?, ?, ?, current_timestamp, current_timestamp)",
				Statement.RETURN_GENERATED_KEYS);

	        stmt.setString(1, tree.getName());
	        stmt.setString(2, tree.getVersion());
	        stmt.setString(3, tree.getCreator());
	        stmt.setString(4, tree.canEditProps() ? "Y"	: "N");

	        stmt.executeUpdate();
	        result = stmt.getGeneratedKeys();
	        if (result.next()) {
	        	tree.setTreeId(result.getLong(1));
	        	tree.setCreated(new java.util.Date());
	        	tree.setLastModified(new java.util.Date());
	        }

	    } finally {
	        if (result != null) {
	            result.close();
	        }
	        if (stmt != null) {
	            stmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }
	}

	public PlunkNode loadSubTree(long treeId, long hash) throws SQLException {
		PlunkNode root = null;

	    Connection con = null;
	    PreparedStatement stmt = null;
	    ResultSet result = null;

	    try {

	        con = dbHandler.getConnection();

	        // load parent
	        stmt = con.prepareStatement(
	            "select hash_key, rotation, move, depth, type, " +
	            "name, comments " +
	            "from plunk_node " +
	            "where tree_id = ? " +
	            "and hash_key = ?");
	        stmt.setLong(1, treeId);
	        stmt.setLong(2, hash);
	        result = stmt.executeQuery();
	        if (result.next()) {
	            root = new PlunkNode();
	            root.setTreeId(treeId);
	            root.setHash(result.getLong(1));
	            root.setRotation(result.getInt(2));
	            root.setMove(result.getInt(3));
	            root.setDepth(result.getInt(4));
	            root.setType(result.getInt(5));
	            root.setName(result.getString(6));
	            root.setComments(result.getString(7));
	        }

	        if (root != null) {
	        	result.close();
	        	stmt.close();

		        stmt = con.prepareStatement(
		            "select hash_key, rotation, move, depth, type, " +
		            "name, comments " +
		            "from plunk_node " +
		            "where tree_id = ? " +
		            "and parent_hash_key = ?");
		        stmt.setLong(1, treeId);
		        stmt.setLong(2, hash);
		        result = stmt.executeQuery();
		        while (result.next()) {
		            PlunkNode n = new PlunkNode();
		            n.setTreeId(treeId);
		            n.setHash(result.getLong(1));
		            n.setDepth(result.getInt(4));
		            n.setParent(root);
		            n.setRotation(result.getInt(2));
		            n.setMove(result.getInt(3));
		            n.setType(result.getInt(5));
		            n.setName(result.getString(6));
		            n.setComments(result.getString(7));
		        }
	        }

	    } finally {
	        if (result != null) {
	            result.close();
	        }
	        if (stmt != null) {
	            stmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }
	    return root;
	}

	public PlunkNode loadPlunkTree(long treeId, ProgressListener l) throws SQLException {

		PlunkNode root = null;
		Map<Long, PlunkNode> nodes = new HashMap<Long, PlunkNode>();

	    Connection con = null;
	    PreparedStatement stmt = null;
	    ResultSet result = null;

	    try {

	        con = dbHandler.getConnection();

	        stmt = con.prepareStatement(
	            "select hash_key, parent_hash_key, rotation, move, depth, type, " +
	            "name, comments " +
	            "from plunk_node " +
	            "where tree_id = ? " +
	            "order by depth");
	        stmt.setLong(1, treeId);
	        result = stmt.executeQuery();
	        while (result.next()) {
	            PlunkNode n = new PlunkNode();
	            n.setTreeId(treeId);
	            n.setStored(true);
	            n.setHash(result.getLong(1));
	            n.setDepth(result.getInt(5));
	            if (n.isRoot()) {
	            	root = n;
	            }
	            else {
	            	n.setParent(nodes.get(result.getLong(2)));
	            }
	            n.setRotation(result.getInt(3));
	            n.setMove(result.getInt(4));
	            n.setType(result.getInt(6));
	            n.setName(result.getString(7));
	            n.setComments(result.getString(8));

	            nodes.put(n.getHash(), n);
	            if (l != null) {
	            	l.updateProgress();
	            }
	        }

	    } finally {
	        if (result != null) {
	            result.close();
	        }
	        if (stmt != null) {
	            stmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }


		return root;
	}

	//TODO
	//scenario
	//add node w/ hash 1234, parent hash 2345
	//delete node w/ hash 1234
	//move to different path
	//add node w/ hash 1234, parent hash 5555

	//now have 2 entries in list of dirty nodes
	//works ok since 1st won't be saved since it is deleted and not stored
	//but if it was saved
	public void savePlunkNodes(List<PlunkNode> nodes, long treeId) throws SQLException {

		for (PlunkNode n : nodes) {
			if (n.isDeleted()) {
				if (n.isStored()) {
					//deletePlunkNodes(n, treeId);
					deletePlunkNode(n, treeId);
				}
			}
			else if (n.isStored()) {
				updateNode(n, treeId);
			} else {
				insertNode(n, treeId);
				n.setStored(true); // so later updates to same node won't try to insert
			}
		}
		//TODO update last mod date for in plunk_tree
	}

	public void insertPlunkNodes(PlunkNode root, long treeId) throws SQLException {

		insertNode(root, treeId);
		if (root.hasChildren()) {
			for (PlunkNode c : root.getChildren()) {
				insertPlunkNodes(c, treeId);
			}
		}
	}

	private void deletePlunkNodes(PlunkNode n, long treeId) throws SQLException {

		//visit all nodes under this node and delete them
		if (n.hasChildren()) {
			for (PlunkNode c : n.getChildren()) {
				deletePlunkNodes(c, treeId);
			}
		}
		deletePlunkNode(n, treeId);
	}

	private void deletePlunkNode(PlunkNode n, long treeId) throws SQLException {
	    Connection con = null;
	    PreparedStatement stmt = null;

	    try {

	        con = dbHandler.getConnection();

	        stmt = con.prepareStatement(
	            "delete from plunk_node " +
	            "where tree_id = ? and hash_key = ?");

	        stmt.setLong(1, treeId);
	        stmt.setLong(2, n.getHash());

	        stmt.executeUpdate();

	    } finally {

	        if (stmt != null) {
	            stmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }
	}

	private void updateNode(PlunkNode n, long treeId) throws SQLException {
	    Connection con = null;
	    PreparedStatement stmt = null;

	    try {

	        con = dbHandler.getConnection();

	        stmt = con.prepareStatement(
	            "update plunk_node " +
	            "set type = ?, name = ?, comments = ?" +
	            "where tree_id = ? and hash_key = ?");

	        stmt.setInt(1, n.getType());
	        stmt.setString(2, n.getName());
	        stmt.setString(3, n.getComments());
	        stmt.setLong(4, treeId);
	        stmt.setLong(5, n.getHash());

	        stmt.executeUpdate();

	    } finally {

	        if (stmt != null) {
	            stmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }
	}

	public PlunkNode loadMoves(long gid, PreparedStatement stmt) throws SQLException {

		PlunkNode root = null;

	    ResultSet result = null;
	    try {

	        stmt.setLong(1, gid);
	        result = stmt.executeQuery();
	        int depth = 0;
	        PlunkNode p = null;

	        while (result.next()) {
	        	if (root == null) {
	    	        root = new PlunkNode();
	    	        root.setMove(180);
	    	        p = root;
	        	}

	            p.setHash(result.getLong(2));
	            p.setRotation(result.getInt(3));
	            p.setComments(result.getString(4));
	            p.setName(result.getString(5));
	            p.setType(result.getInt(6));
	            p.setDepth(depth++);

	            int move = result.getInt(1);
	            if (move != 361) {
	            	PlunkNode n = new PlunkNode();
	            	n.setMove(result.getInt(1));
	            	n.setParent(p);
		            p = n;
	            }
	        }

	    } finally {
	        if (result != null) {
	            result.close();
	        }
	    }

		return root;
	}

	public PlunkNode loadMoves(long gid) throws SQLException {

		PlunkNode root = null;

	    Connection con = null;
	    PreparedStatement stmt = null;
	    ResultSet result = null;
	    try {

	        con = dbHandler.getConnection();

	        stmt = con.prepareStatement(
	            "select next_move, hash_key, rotation, comments, name, type " +
	            "from pente_move " +
	            "where gid = ? " +
	            "order by move_num");

	        stmt.setLong(1, gid);
	        result = stmt.executeQuery();
	        int depth = 0;
	        PlunkNode p = null;

	        while (result.next()) {
	        	if (root == null) {
	    	        root = new PlunkNode();
	    	        root.setMove(180);
	    	        p = root;
	        	}

	            p.setHash(result.getLong(2));
	            p.setRotation(result.getInt(3));
	            p.setComments(result.getString(4));
	            p.setName(result.getString(5));
	            p.setType(result.getInt(6));
	            p.setDepth(depth++);

	            int move = result.getInt(1);
	            if (move != 361) {
	            	PlunkNode n = new PlunkNode();
	            	n.setMove(result.getInt(1));
	            	n.setParent(p);
		            p = n;
	            }
	        }

	    } finally {
	        if (result != null) {
	            result.close();
	        }
	        if (stmt != null) {
	            stmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }

		return root;
	}

    public void insertMoves(List<PlunkNode> moves,
    	GameData data, int db) throws SQLException {

		PreparedStatement stmt = null;
	    Connection con = null;

		try {
	        con = dbHandler.getConnection();
			// store moves
	        stmt = con.prepareStatement("insert into pente_move " +
	            "(gid, move_num, next_move, hash_key, rotation, game, winner, " +
	            " play_date, dbid, type, name, comments) " +
	        	"values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
	        stmt.setLong(1, data.getGameID());
	        for (int i = 0; i < moves.size(); i++) {
	        	PlunkNode n = moves.get(i);
	        	stmt.setInt(2, i);
				// last move sets next_move to 361
				if (i == moves.size() - 1) {
					stmt.setInt(3, 361);
				}
				else {
	                stmt.setInt(3, moves.get(i + 1).getMove());
				}
	            stmt.setLong(4, n.getHash());
	            stmt.setInt(5, n.getRotation());
				stmt.setInt(6, GridStateFactory.getGameId(data.getGame()));
				stmt.setInt(7, data.getWinner());
				stmt.setTimestamp(8, new Timestamp(data.getDate().getTime()));
				stmt.setInt(9, db);
				stmt.setInt(10, n.getType());
				stmt.setString(11, n.getName());
				stmt.setString(12, n.getComments());
	            stmt.executeUpdate();
	        }
	    } finally {

	        if (stmt != null) { try { stmt.close(); } catch(SQLException ex) {} }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }
	}


	public void updateMoves(List<PlunkNode> moves, long gid, int db) throws SQLException {
	    Connection con = null;
	    PreparedStatement stmt = null;

	    try {

	        con = dbHandler.getConnection();

	        stmt = con.prepareStatement(
	            "update pente_move " +
	            "set type = ?, name = ?, comments = ?" +
	            "where gid = ? and hash_key = ? and dbid = ? " +
	            "and move_num = ?");

	        for (PlunkNode n : moves) {
		        stmt.setInt(1, n.getType());
		        stmt.setString(2, n.getName());
		        stmt.setString(3, n.getComments());
		        stmt.setLong(4, gid);
		        stmt.setLong(5, n.getHash());
		        stmt.setInt(6, db);
		        stmt.setInt(7, n.getDepth());

		        stmt.executeUpdate();
	        }

	    } finally {

	        if (stmt != null) {
	            stmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }
	}

	private void insertNode(PlunkNode n, long treeId) throws SQLException {
	    Connection con = null;
	    PreparedStatement stmt = null;

	    try {

	        con = dbHandler.getConnection();

	        stmt = con.prepareStatement(
	            "insert into plunk_node (tree_id, hash_key, parent_hash_key, " +
	            "rotation, move, depth, type, name, comments) " +
	            "values(?, ?, ?, ?, ?, ?, ?, ?, ?)");

	        stmt.setLong(1, treeId);
	        stmt.setLong(2, n.getHash());
	        if (n.isRoot()) {
	        	stmt.setLong(3, 0);
	        }
	        else {
	        	stmt.setLong(3, n.getParent().getHash());
	        }
	        stmt.setInt(4, n.getRotation());
	        stmt.setInt(5, n.getMove());
	        stmt.setInt(6, n.getDepth());
	        stmt.setInt(7, n.getType());
	        stmt.setString(8, n.getName());
	        stmt.setString(9, n.getComments());

	        stmt.executeUpdate();

	    } finally {

	        if (stmt != null) {
	            stmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }
	}

	public void deleteGame(long gid, int dbid) throws SQLException {

		Connection con = null;
		PreparedStatement stmt = null;

		try {
	        con = dbHandler.getConnection();
			stmt = con.prepareStatement(
	            "delete from pente_move where gid = ? " +
	            "and dbid = ?");

			stmt.setLong(1, gid);
			stmt.setInt(2, dbid);
			stmt.execute();
			stmt.close();

			stmt = con.prepareStatement(
	            "delete from pente_game where gid = ? " +
	            "and dbid = ?");

			stmt.setLong(1, gid);
			stmt.setInt(2, dbid);
			stmt.execute();

	    } finally {

	        if (stmt != null) {
	            stmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }
	}
	public int getNumNodesInTree(long treeId) throws SQLException {

		int num = 0;

	    Connection con = null;
	    PreparedStatement stmt = null;
	    ResultSet result = null;

	    try {

	        con = dbHandler.getConnection();

	        stmt = con.prepareStatement(
	            "select count(*) " +
	            "from plunk_node " +
	            "where tree_id = ?");
	        stmt.setLong(1, treeId);
	        result = stmt.executeQuery();
	        if (result.next()) {
	            num = result.getInt(1);
	        }

	    } finally {
	        if (result != null) {
	            result.close();
	        }
	        if (stmt != null) {
	            stmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }


		return num;
	}
	public int getNumGames(int dbid) throws SQLException {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet results = null;
		int num = 0;

		try {
			con = dbHandler.getConnection();
			stmt = con.prepareStatement(
	            "select count(*) " +
	            "from pente_game g " +
	            "where g.dbid = ?");
			stmt.setInt(1, dbid);

			results = stmt.executeQuery();
			if (results.next()) {
				num = results.getInt(1);
			}

	    } finally {

	        if (results != null) {
	            results.close();
	        }
	        if (stmt != null) {
	            stmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }
	    return num;
	}

	public void loadAllGames(PlunkGameVenueStorer gameVenueStorer, int dbid,
		LoadGameListener l) throws Exception {

		Connection con = null;
		PreparedStatement stmt = null;
		PreparedStatement movesStmt = null;
		ResultSet results = null;

		try {
	        con = dbHandler.getConnection();
			stmt = con.prepareStatement(
	            "select g.site_id, g.event_id, g.round, g.section, " +
	            "g.play_date, g.timer, g.rated, g.initial_time, " +
	            "g.incremental_time, p1.name, p2.name, " +
	            "g.player1_rating, g.player2_rating, g.winner, g.game, " +
	            "g.swapped, g.private, g.gid " +
	            "from pente_game g, player p1, player p2 " +
	            "where g.dbid = ? " +
	            "and p1.pid = g.player1_pid " +
	            "and p2.pid = g.player2_pid");

			movesStmt = con.prepareStatement(
	            "select next_move, hash_key, rotation, comments, name, type " +
	            "from pente_move " +
	            "where gid = ? " +
	            "order by move_num");

			stmt.setInt(1, dbid);

			results = stmt.executeQuery();
			while (results.next()) {
				PlunkGameData g = new PlunkGameData();

            	int siteID = results.getInt(1);
                int game = results.getInt(15);

                g.setGameID(results.getLong(18));

                GameSiteData siteData = gameVenueStorer.getGameSiteData(
                    game, siteID);
                g.setSite(siteData.getName());
                g.setShortSite(siteData.getShortSite());
                g.setSiteURL(siteData.getURL());

                // games from iyt,bk,pente.org,pbem not editable
                if (siteID > 4) {
                	g.setEditable(true);
                }

                int eventID = results.getInt(2);
                GameEventData eventData = gameVenueStorer.getGameEventData(
                    game, eventID, siteData.getName());
                g.setEvent(eventData.getName());


                g.setRound(results.getString(3));
                g.setSection(results.getString(4));
                Timestamp playDate = results.getTimestamp(5);
                g.setDate(new java.util.Date(playDate.getTime()));

                String timed = results.getString(6);
                if (timed.equals("S") || timed.equals("I")) {
                    g.setTimed(true);
                }
                else {
                    g.setTimed(false);
                }

                //g.setRated(MySQLDBHandler.getBooleanValueFromDBString(results.getString(7)));
                g.setInitialTime(results.getInt(8));
                g.setIncrementalTime(results.getInt(9));

                PlayerData player1Data = new DefaultPlayerData();
                player1Data.setUserIDName(results.getString(10));
                player1Data.setRating(results.getInt(12));
                g.setPlayer1Data(player1Data);

                PlayerData player2Data = new DefaultPlayerData();
                player2Data.setUserIDName(results.getString(11));
                player2Data.setRating(results.getInt(13));
                g.setPlayer2Data(player2Data);

                g.setWinner(results.getInt(14));
                g.setGame(GridStateFactory.getGameName(game));
                g.setSwapped(results.getString(16).equals("Y"));

                g.setPrivateGame(results.getString(17).equals("Y"));

                g.setRoot(loadMoves(g.getGameID(), movesStmt));

	            l.gameLoaded(g);
	        }

	    } finally {

	        if (results != null) {
	            results.close();
	        }
	        if (stmt != null) {
	            stmt.close();
	        }
	        if (movesStmt != null) {
	        	movesStmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }
	}

	public List<String> loadPlayers() throws SQLException {

		List<String> players = new ArrayList<String>();

	    Connection con = null;
	    PreparedStatement stmt = null;
	    ResultSet result = null;

	    try {

	        con = dbHandler.getConnection();

	        stmt = con.prepareStatement(
	            "select distinct(name) from player order by name");
	        result = stmt.executeQuery();
	        while (result.next()) {
	            players.add(result.getString(1));
	        }

	    } finally {
	        if (result != null) {
	            result.close();
	        }
	        if (stmt != null) {
	            stmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }

		return players;
	}

	public void deleteTree(long treeId) throws SQLException {

	    Connection con = null;
	    PreparedStatement stmt = null;

	    try {

	        con = dbHandler.getConnection();

	        stmt = con.prepareStatement(
	            "delete from plunk_tree where tree_id = ?");
	        stmt.setLong(1, treeId);
	        stmt.execute();
	        stmt.close();

	        stmt = con.prepareStatement(
            	"delete from plunk_node where tree_id = ?");
	        stmt.setLong(1, treeId);
	        stmt.execute();

	    } finally {

	        if (stmt != null) {
	            stmt.close();
	        }
	        if (con != null) {
	            dbHandler.freeConnection(con);
	        }
	    }
	}
}
