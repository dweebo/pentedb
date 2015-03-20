package org.pente.gameDatabase.swing.component;

import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 * @author dweebo
 */
public abstract class MyDialog extends JDialog {

	protected int result = JOptionPane.NO_OPTION;

	protected JButton cancel;
	protected JButton ok;

	public MyDialog(Frame f, String title, boolean modal) {
		super(f, title, modal);

        int menuShortcutKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        KeyStroke w = KeyStroke.getKeyStroke(KeyEvent.VK_W, menuShortcutKey);
        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(getCancelAction(), "close", w, JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(getCancelAction(), "close", esc, JComponent.WHEN_IN_FOCUSED_WINDOW);

        ok = new JButton(getOkAction());
        cancel = new JButton(getCancelAction());

		getRootPane().setDefaultButton(ok);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				getCancelAction().actionPerformed(null);
			}
		});
	}

	protected Action getOkAction() {
		return okAction;
	}
	protected Action getCancelAction() {
		return closeAction;
	}

	protected Action okAction = new AbstractAction("Ok") {
		public void actionPerformed(ActionEvent e) {
			result = JOptionPane.OK_OPTION;
			setVisible(false);
		};
	};
	protected Action closeAction = new AbstractAction("Cancel") {
		public void actionPerformed(ActionEvent e) {
			result = JOptionPane.CANCEL_OPTION;
			setVisible(false);
		};
	};

    protected void centerDialog(Frame frame) {
        Point location = new Point();
        location.x = frame.getLocation().x +
                     (frame.getSize().width + frame.getInsets().right - frame.getInsets().left) / 2 -
                     getSize().width / 2;
        location.y = frame.getLocation().y +
                     (frame.getSize().height + frame.getInsets().top - frame.getInsets().bottom) / 2 -
                     (getSize().height + getInsets().top - getInsets().bottom) / 2;
        setLocation(location);
    }

	public int getResult() {
		return result;
	}
}
