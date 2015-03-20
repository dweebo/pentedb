package org.pente.game;

import java.util.*;

/** Interface for data structure to hold game site data.
 *  This interface provides methods to map a site name to a site id.  It also
 *  has methods to associate the events played in this site and can be used
 *  to create a tree of venue information.
 *
 *  @see GameVenueStorer
 *  @see GameEventData
 *  @author dweebo
 */
public interface GameSiteData {

    /** Useful for representing the concept of all sites in searches */
    public static final String ALL_SITES = "All Sites";


    /** Get the site id
     *  @return int The site id
     */
    public int getSiteID();

    /** Set the site id
     *  @param siteID The site id
     */
    public void setSiteID(int siteID);


    /** Get the name of the site
     *  @return String The name of the site
     */
    public String getName();

    /** Set the name of the site
     *  @param name The name of the site
     */
    public void setName(String name);


    /** Get the short name of the site
     *  @return String The short name of the site
     */
    public String getShortSite();

    /** Set the short name of the site
     *  @param short name The name of the site
     */
    public void setShortSite(String site);


    /** Get the URL of the site
     *  @return String The URL of the site
     */
    public String getURL();

    /** Set the URL of the site
     *  @param URL The name of the site
     */
    public void setURL(String URL);

    /** Add a event to this site
     *  @param GameEventData The event data
     */
    public void addGameEventData(GameEventData gameEventData);

    /** Get the list of events for this event
     *  @param Vector The list of events
     */
    public List getGameEventData();


    /** Create a copy of the data in this object in a new object
     *  @return Object A copy of this object.
     */
    public Object clone();
}