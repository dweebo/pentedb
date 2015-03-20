package org.pente.game;

import java.util.*;

/**
 * @author dweebo
 */
public interface GameTreeData {

    public void setID(int id);
    public int getID();

    public void setName(String name);
    public String getName();

    public void addGameSiteData(GameSiteData gameSiteData);
    public List<GameSiteData> getGameSiteData();

    public Object clone();
}
