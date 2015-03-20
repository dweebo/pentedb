package org.pente.gameDatabase.swing;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.pente.mmai.Ai;
import org.pente.mmai.AiListener;

/**
 * @author dweebo
 */
public class AiSettingsPanel extends JPanel implements AiListener {

	private Ai ai;

	private JComboBox treeChoice;
	private JComboBox seatChoice;
	private JComboBox levelChoice;
	private JComboBox vctChoice;

	private JButton startButton;

	private CardLayout cardLayout;
	private JPanel top;

	private String startText, stopText;

	public AiSettingsPanel(final Ai ai, final List<PlunkTree> trees, boolean includePlayer,
		AiVisualizationPanel visualization, String startButtonText, String stopButtonText) {

		this.ai = ai;

		this.startText = startButtonText;
		this.stopText = stopButtonText;

		treeChoice = new JComboBox();
		treeChoice.addItem("No Opening Book");
		for (PlunkTree t : trees) {
			treeChoice.addItem(t.getName());
			if (t.getTreeId() == ai.getTreeId()) {
				treeChoice.setSelectedItem(t.getName());
			}
		}

		treeChoice.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String treeName = (String) treeChoice.getSelectedItem();
					if (treeName.equals("No Opening Book")) {
						ai.setTreeId(-1);
					}
					else {
						for (PlunkTree t : trees) {
							if (t.getName().equals(treeName)) {
								ai.setTreeId(-1);
								break;
							}
						}
					}
				}
			}
		});


		levelChoice = new JComboBox();
		for (int i = 1; i < 13; i++) {
			levelChoice.addItem("Level " + Integer.toString(i));
		}
		levelChoice.setSelectedIndex(ai.getLevel() - 1);
		levelChoice.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					ai.setLevel(levelChoice.getSelectedIndex() + 1);
				}
			}
		});

		if (includePlayer) {
			seatChoice = new JComboBox();
			seatChoice.addItem("Player 1");
			seatChoice.addItem("Player 2");
			seatChoice.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						ai.setSeat(seatChoice.getSelectedIndex() + 1);
					}
				}
			});
		}
		vctChoice = new JComboBox();
		vctChoice.addItem("VCT Off");
		vctChoice.addItem("VCT On");
		vctChoice.setSelectedIndex(ai.getVct());
		vctChoice.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					ai.setVct(vctChoice.getSelectedIndex());
				}
			}
		});

		startButton = new JButton(startText);

		JPanel settings = new JPanel();
		settings.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		if (includePlayer) {

			gbc.gridy++;
			gbc.gridx = 1;
			settings.add(new JLabel("AI Player:"), gbc);
			gbc.gridx = 2;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			settings.add(seatChoice, gbc);
		}

		gbc.gridy++;
		gbc.gridx = 1;
		settings.add(new JLabel("AI Level:"), gbc);
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		settings.add(levelChoice, gbc);

		gbc.gridy++;
		gbc.gridx = 1;
		settings.add(new JLabel("VCT Search:"), gbc);
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		settings.add(vctChoice, gbc);

		gbc.gridy++;
		gbc.gridx = 1;
		settings.add(new JLabel("Opening Book:"), gbc);
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		settings.add(treeChoice, gbc);

		// attempt to take up extra space
		gbc.gridy++;
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weighty = 100;
		settings.add(new JLabel(""), gbc);

		top = new JPanel();
		cardLayout = new CardLayout();
		top.setLayout(cardLayout);
		top.add(settings, "settings");
		top.add(visualization, "visualization");

		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.insets = new Insets(4, 4, 4, 4);

		gbc2.gridy = 1;
		gbc2.gridx = 1;
		gbc2.gridwidth = 1;
		gbc2.weighty = 100;
		gbc2.weightx = 1;
		gbc2.fill = GridBagConstraints.BOTH;
		gbc2.anchor = GridBagConstraints.NORTHWEST;
		add(top, gbc2);

		gbc2.gridy++;
		gbc2.weighty = 1;
		gbc2.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.SOUTHWEST;
		add(startButton, gbc2);

		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals(stopText)) {
					if (ai.isActive()) {
						ai.stopThinking();
					}
				}
			}
		});
	}

	public void addStartActionListener(ActionListener l) {
		startButton.addActionListener(l);
	}



	private void enableComponents(boolean enable) {
		if (seatChoice != null) seatChoice.setEnabled(enable);
		levelChoice.setEnabled(enable);
		vctChoice.setEnabled(enable);
		treeChoice.setEnabled(enable);
		if (enable) {
			startButton.setText(startText);
		}
		else {
			startButton.setText(stopText);
		}
	}

	public void startThinking() {
		enableComponents(false);
		cardLayout.show(top, "visualization");
	}
	public void stopThinking() {
		enableComponents(true);
		cardLayout.show(top, "settings");
	}
	public void aiEvaluateCallBack() {
	}
	public void aiVisualizationCallBack(int[] bd) {
	}
	public void moveReady(int[] moves, int newMove) {
	}
}
