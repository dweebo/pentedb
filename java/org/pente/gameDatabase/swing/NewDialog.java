package org.pente.gameDatabase.swing;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.pente.game.*;
import org.pente.gameDatabase.swing.component.*;

/**
 * @author dweebo
 */
public class NewDialog extends MyDialog {

	public static final int TYPE_TREE = 1;
	public static final int TYPE_GAME = 2;
	private int newType;

	private JRadioButton treeButton = null;
	private JRadioButton gameButton = null;
	ButtonGroup group = null;

	private JComboBox gameChoice;
	private int game;


	public NewDialog(Frame owner, int gm, int initType) {
		super(owner, "New...", true);

		newType = initType;

		treeButton = new JRadioButton("Game Analysis");
		treeButton.setToolTipText("Enter game positions and variations for analysis, search for matching games.");
		treeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newType = TYPE_TREE;
			}
		});

		gameButton = new JRadioButton("Game Entry");
		gameButton.setToolTipText("Enter a new game into the database");
		gameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newType = TYPE_GAME;
			}
		});

		if (initType == TYPE_GAME) {
			gameButton.setSelected(true);
		}
		else {
			treeButton.setSelected(true);
		}

		group = new ButtonGroup();
		group.add(treeButton);
		group.add(gameButton);


		gameChoice = new JComboBox();
		for (Game g : GridStateFactory.getNormalGames()) {
			gameChoice.addItem(g.getName());
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
					NewDialog.this.game = GridStateFactory.getGameId((String)
						gameChoice.getSelectedItem());
				}
			}
		});

		JPanel top = new JPanel();
		top.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.NONE;

		top.add(treeButton, gbc);

		gbc.gridx = 2;
		top.add(gameButton, gbc);

		gbc.gridy = 2;
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		top.add(new JLabel("Game:"), gbc);
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

	public int getNewType() {
		return newType;
	}
	public int getGame() {
		return game;
	}
}
