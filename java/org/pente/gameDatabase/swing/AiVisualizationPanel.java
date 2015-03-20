package org.pente.gameDatabase.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.pente.game.*;
import org.pente.gameServer.client.GameOptions;
import org.pente.gameServer.client.awt.GameBoard;
import org.pente.gameServer.core.AlphaNumericGridCoordinates;
import org.pente.gameServer.core.GridPiece;
import org.pente.gameServer.core.SimpleGridPiece;
import org.pente.mmai.*;

/**
 * @author dweebo
 */
public class AiVisualizationPanel extends JPanel implements AiListener {

	private Ai ai;

	private GameBoard aiBoard;
	private JLabel aiStatus;
	private JLabel aiStatusTime;
	private int aiCount;
	private Timer aiTimer;
	private int aiSeconds;
	private JCheckBox aiVisualization;
	private static final NumberFormat nf = NumberFormat.getNumberInstance();

	public AiVisualizationPanel(Ai a, int game, GameOptions go) {
		this.ai = a;

		PlunkPenteBoardLW lw2 = new PlunkPenteBoardLW();
	    lw2.gridCoordinatesChanged(new AlphaNumericGridCoordinates(19, 19));
		aiBoard = new GameBoard(lw2, go, true);
		aiBoard.setGame(game);
		if (game == GridStateFactory.PENTE ||
			game == GridStateFactory.KERYO ||
			game == GridStateFactory.BOAT_PENTE) {
	        ((PenteState) aiBoard.getGridState()).setTournamentRule(false);
	    }
		aiBoard.getGridBoard().setGameName(null);
		aiBoard.getGridBoard().setBackgroundColor(new Color(222,222,222).getRGB());
		aiBoard.getGridBoard().setDrawCoordinates(false);
		aiBoard.getGridBoard().setDrawInnerCircles(false);

		aiStatus = new JLabel("Status:");
		aiTimer = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				aiSeconds++;

				int minutes = aiSeconds / 60;
				int seconds = aiSeconds % 60;
				String text = minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
				aiStatusTime.setText(text);
			}
		});

		aiStatusTime = new JLabel("0:00");
		aiVisualization = new JCheckBox("Show visualization", true);
		aiVisualization.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ai.setVisualization(aiVisualization.isSelected());
				if (aiTimer.isRunning() && !aiVisualization.isSelected()) {
					stopThinkingAiBoard();
				}
				else if (aiTimer.isRunning() && aiVisualization.isSelected()) {
					startThinkingAiBoard();
				}
			}
		});
		ai.setVisualization(true);

		aiBoard.setMinimumSize(new Dimension(100, 100));
		aiBoard.setPreferredSize(new Dimension(100, 100));

		setLayout(new GridBagLayout());
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.insets = new Insets(1, 1, 1, 1);
		gbc2.anchor = GridBagConstraints.NORTHWEST;

		gbc2.gridx = 1;
		gbc2.gridy = 1;
		gbc2.gridwidth = 2;
		gbc2.gridheight = 1;
		gbc2.weighty = 100;
		gbc2.weightx = 100;
		gbc2.fill = GridBagConstraints.BOTH;
		add(aiBoard, gbc2);

		gbc2.gridy = 2;
		gbc2.gridwidth = 1;
		gbc2.weightx = 80;
		gbc2.weighty = 1;
		gbc2.fill = GridBagConstraints.NONE;
		add(aiStatus, gbc2);

		gbc2.gridx = 2;
		gbc2.anchor = GridBagConstraints.NORTHEAST;
		add(aiStatusTime, gbc2);

		gbc2.gridy = 3;
		gbc2.gridx = 1;
		gbc2.gridwidth = 2;
		gbc2.anchor = GridBagConstraints.NORTHWEST;
		add(aiVisualization, gbc2);
	}

	public void reset() {
		aiTimer.stop();
		aiSeconds = 0;
		aiCount = 0;
	}
	public void startThinking() {
		aiTimer.start();
		startThinkingAiBoard();
	}
	public void startThinkingAiBoard() {
		if (aiVisualization.isSelected()) {
			aiBoard.getGridBoard().setGameName("Thinking");
			aiBoard.getGridBoard().setBackgroundColor(new Color(128,255,128).getRGB());
		}
	}

	public void stopThinking() {
		stopThinkingAiBoard();
		aiTimer.stop();
	}
	public void stopThinkingAiBoard() {
		aiBoard.getGridBoard().setGameName(null);
		aiBoard.getGridBoard().setBackgroundColor(new Color(222,222,222).getRGB());
		aiBoard.getGridBoard().clearPieces();
	}


	public void moveReady(int[] moves, int newMove) {
	}

	public void aiEvaluateCallBack() {
		aiCount++;
		aiStatus.setText("Status: evaluated " + nf.format(aiCount) +
			" moves ");
	}
	public void aiVisualizationCallBack(int[] bd) {
		if (bd == null) System.out.println("null vis");
		if (!aiVisualization.isSelected()) return;
		if (aiCount % 1000 != 0) return; //no need to show every position
		PlunkPenteBoardLW board = (PlunkPenteBoardLW) aiBoard.getGridBoardComponent();

		List<GridPiece> pieces = new ArrayList<GridPiece>();
		for (int i=0;i<19;i++) {
			for(int j=0;j<19;j++) {
				int x = bd[i*19+j];
				if (x != -1 && x != 0) {
					if (x == 3) x = ai.getSeat();
					pieces.add(new SimpleGridPiece(i, 18-j, x));
				}
			}
		}
		board.replacePieces(pieces);
	}

	public int getAiCount() {
		return aiCount;
	}
	public void setText(String text) {
		aiStatus.setText(text);
	}
}
