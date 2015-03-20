package org.pente.gameDatabase.swing;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Component to be used as tabComponent;
 * Contains a JLabel to show the text and
 * a JButton to close the tab it belongs to
 * @author dweebo
 */
public class ButtonTabComponent extends JPanel implements TabComponentEditListener {
    private final JTabbedPane pane;
    private JButton close;
    private JLabel label;
    private boolean edits = false;
    public ButtonTabComponent(final JTabbedPane pane, String name) {

        //unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        if (pane == null) {
            throw new NullPointerException("TabbedPane is null");
        }
        this.pane = pane;
        setOpaque(false);

        //make JLabel read titles from JTabbedPane
        label = new JLabel(name);

        add(label);
        //add more space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        //tab button
        //JButton button = new TabButton();
        close = new FlatButton(new ImageIcon(ButtonTabComponent.class.getResource("images/cross_small.gif")));
        add(close);

        //add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }
    public void addActionListener(ActionListener actionListener) {
        close.addActionListener(actionListener);
    }
    public void setName(String name) {
    	if (edits) {
    		name += "*";
    	}
    	label.setText(name);
    }
    public void editsMade() {
        if (!edits) {
            edits = true;
            label.setText(label.getText() + "*");
        }
    }
    public void editsSaved() {
        if (edits) {
            edits = false;
            label.setText(label.getText().substring(0, label.getText().length() - 1));
        }
    }
}