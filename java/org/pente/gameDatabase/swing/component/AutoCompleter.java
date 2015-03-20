package org.pente.gameDatabase.swing.component;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * @author dweebo
 */
public abstract class AutoCompleter{
    JList list = new JList();
    JPopupMenu popup = new JPopupMenu();
    JTextComponent textComp;
    JScrollPane scroll;
    private static final String AUTOCOMPLETER = "AUTOCOMPLETER"; //NOI18N

    public AutoCompleter(JTextComponent comp){
        textComp = comp;
        textComp.putClientProperty(AUTOCOMPLETER, this);
        scroll = new JScrollPane(list);
        scroll.setBorder(null);

        //allows list.getFixedCellHeight() to work
        list.setPrototypeCellValue("Index 1234567890");

        list.setFocusable( false );

        // allows mouse to highlight items
        list.addMouseMotionListener(new MouseMotionAdapter() {
        	public void mouseMoved(MouseEvent me) {
        		Point p = new Point(me.getX(),me.getY());
        		int sel = list.locationToIndex(p);
        		if (sel != -1) {
        			list.setSelectedIndex(sel);
        		}
        	}
        });
        // allows mouse click to select items
        list.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent me) {
        		Point p = new Point(me.getX(),me.getY());
        		int sel = list.locationToIndex(p);
        		if (sel != -1) {
        			list.setSelectedIndex(sel);
        			acceptAction.actionPerformed(new ActionEvent(textComp,0,null));
        		}
        	}
        });

        scroll.getVerticalScrollBar().setFocusable( false );

        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        popup.setBorder(BorderFactory.createLineBorder(Color.black));
        popup.add(scroll);

        if(textComp instanceof JTextField) {
            textComp.registerKeyboardAction(showAction, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), JComponent.WHEN_FOCUSED);
            textComp.getDocument().addDocumentListener(documentListener);
        } else {
            textComp.registerKeyboardAction(showAction, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
        }

        textComp.registerKeyboardAction(upAction, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), JComponent.WHEN_FOCUSED);
        textComp.registerKeyboardAction(hidePopupAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);

        popup.addPopupMenuListener(new PopupMenuListener(){
            public void popupMenuWillBecomeVisible(PopupMenuEvent e){
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e){
                textComp.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
            }

            public void popupMenuCanceled(PopupMenuEvent e){
            }
        });
        list.setRequestFocusEnabled(false);
    }

    static Action acceptAction = new AbstractAction(){
        public void actionPerformed(ActionEvent e){
            JComponent tf = (JComponent)e.getSource();
            AutoCompleter completer = (AutoCompleter)tf.getClientProperty(AUTOCOMPLETER);
            completer.popup.setVisible(false);
            completer.acceptedListItem((String)completer.list.getSelectedValue());
        }
    };

    DocumentListener documentListener = new DocumentListener(){
        public void insertUpdate(DocumentEvent e){
        	if (textComp.isShowing()) {
        		showPopup();
        	}
        }

        public void removeUpdate(DocumentEvent e){
        	if (textComp.isShowing()) {
        		showPopup();
        	}
        }

        public void changedUpdate(DocumentEvent e){}
    };

    private void showPopup(){
        popup.setVisible(false);
        if(textComp.isEnabled() && updateListData() && list.getModel().getSize()!=0){
            if(!(textComp instanceof JTextField))
                textComp.getDocument().addDocumentListener(documentListener);
            textComp.registerKeyboardAction(acceptAction, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);
            int size = list.getModel().getSize();
            list.setVisibleRowCount(size<5 ? size : 5);

            Dimension d = new Dimension(textComp.getSize().width,
            	list.getFixedCellHeight() * (size<5 ? size : 5));
            scroll.setPreferredSize(d);

            popup.show(textComp, 0, textComp.getHeight());
        }else
            popup.setVisible(false);
        textComp.requestFocus();
    }

    static Action showAction = new AbstractAction(){
        public void actionPerformed(ActionEvent e){
            JComponent tf = (JComponent)e.getSource();
            AutoCompleter completer = (AutoCompleter)tf.getClientProperty(AUTOCOMPLETER);
            if(tf.isEnabled()){
                if(completer.popup.isVisible())
                    completer.selectNextPossibleValue();
                else
                    completer.showPopup();
            }
        }
    };

    static Action upAction = new AbstractAction(){
        public void actionPerformed(ActionEvent e){
            JComponent tf = (JComponent)e.getSource();
            AutoCompleter completer = (AutoCompleter)tf.getClientProperty(AUTOCOMPLETER);
            if(tf.isEnabled()){
                if(completer.popup.isVisible())
                    completer.selectPreviousPossibleValue();
            }
        }
    };

    static Action hidePopupAction = new AbstractAction(){
        public void actionPerformed(ActionEvent e){
            JComponent tf = (JComponent)e.getSource();
            AutoCompleter completer = (AutoCompleter)tf.getClientProperty(AUTOCOMPLETER);
            if(tf.isEnabled())
                completer.popup.setVisible(false);
        }
    };

    /**
     * Selects the next item in the list.  It won't change the selection if the
     * currently selected item is already the last item.
     */
    protected void selectNextPossibleValue(){
        int si = list.getSelectedIndex();

        if(si < list.getModel().getSize() - 1){
            list.setSelectedIndex(si + 1);
            list.ensureIndexIsVisible(si + 1);
        }
    }

    /**
     * Selects the previous item in the list.  It won't change the selection if the
     * currently selected item is already the first item.
     */
    protected void selectPreviousPossibleValue(){
        int si = list.getSelectedIndex();

        if(si > 0){
            list.setSelectedIndex(si - 1);
            list.ensureIndexIsVisible(si - 1);
        }
    }

    // update list model depending on the data in textfield
    protected abstract boolean updateListData();

    // user has selected some item in the list. update textfield accordingly...
    protected abstract void acceptedListItem(String selected);
}
