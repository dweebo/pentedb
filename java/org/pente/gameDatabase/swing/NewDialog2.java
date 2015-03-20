package org.pente.gameDatabase.swing;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.pente.game.*;
import org.pente.gameDatabase.swing.component.*;

/**
 * @author dweebo
 */
public class NewDialog2 extends MyDialog {


	private JComboBox gameChoice;
	private int game;


	public NewDialog2(Frame owner, String name, int gm) {
		this(owner, name, gm, GridStateFactory.getAllGames());
	}
	public NewDialog2(Frame owner, String name, int gm, Object[] games) {
		super(owner, name, true);


		gameChoice = new JComboBox();
		for (Object game : games) {
			if (game != null) {
				gameChoice.addItem(game.toString());
			}
		}

		if (gm == -1) {
			gameChoice.setSelectedIndex(0);
			game = 1;
		}
		else {
			gameChoice.setSelectedItem(GridStateFactory.getGameName(gm));
			gameChoice.setEnabled(false);
			game = gm;
		}

		gameChoice.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					NewDialog2.this.game = GridStateFactory.getGameId((String)
						gameChoice.getSelectedItem());
				}
			}
		});

		JPanel top = new JPanel();
		top.setLayout(new GridBagLayout());
		top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		gbc.weighty = 1;
		gbc.weightx = 1;

		gbc.anchor = GridBagConstraints.NORTHEAST;
		top.add(new JLabel("Select a Game:"), gbc);
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		top.add(gameChoice, gbc);


		getContentPane().add(top, BorderLayout.NORTH);

		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		p.add(ok);
		p.add(cancel);

		getContentPane().add(p, BorderLayout.SOUTH);
		pack();

		centerDialog(owner);
		setVisible(true);
	}

	public int getGame() {
		return game;
	}
}
