package org.pente.gameDatabase.swing;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * @author dweebo
 */
public class FlatButton extends JButton {

	public FlatButton() {
		init();
	}

	public FlatButton(Icon icon) {
		super(icon);
		init();
		Dimension d = getPreferredSize();
		setPreferredSize(new Dimension(d.height,d.height));
	}

	public FlatButton(String text) {
		super(text);
		init();
	}

	public FlatButton(Action a) {
		super(a);
		setText("");
		Dimension d = getPreferredSize();
		setPreferredSize(new Dimension(d.height,d.height));
		init();
	}

	public FlatButton(String text, Icon icon) {
		super(text, icon);
		init();
	}

	private void init() {
		off();
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				on();
			}
			@Override
			public void mouseExited(MouseEvent e) {
				off();
			}
		});
	}
	private void off() {
		setContentAreaFilled(false);
		setBorderPainted(false);
	}
	private void on() {
		setContentAreaFilled(true);
		setBorderPainted(true);
	}
}
