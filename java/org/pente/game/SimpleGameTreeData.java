package org.pente.game;

import java.util.*;

/**
 * @author dweebo
 */
public class SimpleGameTreeData implements GameTreeData, Cloneable, java.io.Serializable {

    private int id;
    private String name;
    private List sites = new ArrayList(3);

    public void setID(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
       return name;
    }


    public void addGameSiteData(GameSiteData gameSiteData) {
        sites.add(gameSiteData);
    }


    public List getGameSiteData() {
        return sites;
    }

    public Object clone()  {

        SimpleGameTreeData cloned = new SimpleGameTreeData();

        cloned.setID(getID());
        cloned.setName(getName());

        for (int i = 0; i < sites.size(); i++) {
            GameSiteData s = (GameSiteData) sites.get(i);
            cloned.addGameSiteData((GameSiteData) s.clone());
        }

        return cloned;
    }
}
