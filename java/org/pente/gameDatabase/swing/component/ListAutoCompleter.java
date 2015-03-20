package org.pente.gameDatabase.swing.component;

import java.util.*;
import javax.swing.text.JTextComponent;

/**
 * @author dweebo
 */
public class ListAutoCompleter extends AutoCompleter {

	public static Comparator<String> noCaseComp = new Comparator<String>() {
    	public int compare(String s1, String s2) {
    		return s1.toLowerCase().compareTo(s2.toLowerCase());
    	}
	};

	private List<String> all;
    public ListAutoCompleter(JTextComponent comp, List list, boolean listReady){
        super(comp);
        if (listReady) {
        	all = list;
        }
        else {
        	all = initList(list);
        }
    }

    public static List<String> initList(List list) {
    	List<String> list2 = new ArrayList<String>(list.size());
        for (Object o : list) {
        	if (list2.contains(o.toString())) continue;//no dups
        	list2.add(o.toString());
        }

        Collections.sort(list2, noCaseComp);
        return list2;
    }
    public List<String> getList() {
    	return all;
    }
    public void setList(List<String> l) {
    	this.all = l;
    }
    public void updateList(List list) {
    	all = initList(list);
    }

    @Override
	protected void acceptedListItem(String selected) {
		textComp.setText(selected);
	}

	@Override
	protected boolean updateListData() {
        String value = textComp.getText();
        if (value == null || value.equals("")) {
        	list.setListData(all.toArray());
        	list.setSelectedIndex(0);
        	return true;
        }
        value = value.toLowerCase();

        int i = findStart(value, 0, all.size() - 1);
        if (i == -1) {
        	list.setListData(new String[0]);
        }
        else {
	        List<String> l = new ArrayList<String>();
	        for (; i < all.size(); i++) {
	        	String s = all.get(i);
	        	String slow = s.toLowerCase();
	        	if (slow.equals(value)) continue;//don't include actual match
	        	if (!slow.startsWith(value)) break;
	        	l.add(s);
	        }
			list.setListData(l.toArray());
        }
		return true;
	}

	/**
	 * binary search to find the index in the arraylist where
	 * the first match occurs
	 * @param firstChar
	 * @param low
	 * @param high
	 * @return
	 */
	private int findStart(String find, int low, int high) {
		 if (high < low) {
	           return -1;
		 }
         int mid = (low + high) / 2;
 		 String s = all.get(mid).toLowerCase();
	     if (s.startsWith(find)) {
	    	 if (mid == 0) return 0;
	    	 else {
	    		 String prevS = all.get(mid - 1).toLowerCase();
	    		 if (!prevS.startsWith(find)) return mid;
	    		 else return findStart(find, low, mid - 1);
	    	 }
	     }
	     else if (s.compareTo(find) > 0) {
	    	 return findStart(find, low, mid-1);
	     }
	     else if (s.compareTo(find) < 0) {
             return findStart(find, mid + 1, high);
	     }
	     else {
	    	 return -1;
	     }
	}
}
