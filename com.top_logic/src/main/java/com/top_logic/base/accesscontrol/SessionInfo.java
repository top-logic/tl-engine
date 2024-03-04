/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import java.util.HashMap;

import jakarta.servlet.http.HttpServletRequest;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * This class holds the informations about an active session
 * These informations are stored in the sessionmap of the sessionservice
 * These informations are NOT bound to the session, just stored
 * in the sessionmap for later access to have informations about the
 * session without having the session itself.
 *
 * @author    <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
class SessionInfo extends HashMap<String, Object> {
    
    /**
    * Session Id of the Session
    */
    private static final String SESSION_ID = "session_id";
 
    /**
    * Key used within the sessioninfo to refer the user
    */
    private static final String SESSION_DATA_USER = "session_data_user";
 
    /**
    * Key used within the sessioninfo to refer the user name.
    * This is mainly used for lazy initialization of the user 
    * during restart. 
    */
    private static final String SESSION_DATA_USER_NAME = "session_data_user_name";
 
    /**
    * Key used within the sessioninfo to refer the users ip
    */
    private static final String SESSION_DATA_IP = "session_data_ip";
    
    /**
    * Key used within the sessioninfo to refer the creationtime as Long
    */
    private static final String CREATION_TIME = "creation_time";
    

    /**
    * Key used within the sessioninfo to refer the lastaccessedtime as Long
    */
    private static final String LAST_ACCESS_TIME = "last_access_time";
    
    /**
    * Key used within the sessioninfo to refer the maxinactivetime as Long
    */
    private static final String MAX_INACTIVE_TIME = "max_inactive_time";

    /**
    * Constructor. Initializes creationtime and
    * last accessed time with the current time.
    * Default for max_inactive_time is Long.MAX_VALUE
    */    
	SessionInfo() {
        super();
        this.setCreationTime(System.currentTimeMillis());
    }

    /**
    * Sets the Session ID in the sessioninfo
    */
    protected void setSessionId(String session_id) {
        this.put(SESSION_ID,session_id);
    }    

    /**
    * Sets a User in the sessioninfo
    */
	protected void setUser(Person aUser) {
        if (aUser != null) {
            this.put(SESSION_DATA_USER,aUser);
			this.put(SESSION_DATA_USER_NAME, aUser.getName());
        }
    }
    
    /**
     * Set the user via the user name.
     */
    protected void setUserName(String aUserName) {
        if (this.containsKey(SESSION_DATA_USER)) {
            this.remove(SESSION_DATA_USER);
        }
        this.put(SESSION_DATA_USER_NAME,aUserName);
    }
    
    /**
	 * @see #getClientIP()
	 */
    protected void setClientIP(String ip) {
        
        this.put(SESSION_DATA_IP,ip);
    }    

    /**
    * Sets a Time in the sessioninfo
    */
    protected void setCreationTime(long creationtime) {
        
		this.put(CREATION_TIME, Long.valueOf(creationtime));
        this.setLastAccessedTime(creationtime);
        this.setMaximumInactiveTime(Long.MAX_VALUE);
    }
    
    /**
    * Sets a Time in the sessioninfo
    */
    protected void setLastAccessedTime(long accesstime) {

		this.put(LAST_ACCESS_TIME, Long.valueOf(accesstime));
    
    }
    
    /**
    * Sets a Time in the sessioninfo
    */
    protected void setMaximumInactiveTime(long maxinactivetime) {
        
        //converting to millisecs
		this.put(MAX_INACTIVE_TIME, Long.valueOf(maxinactivetime * 1000));
    }
    
    protected String getSessionId() {
        
        return (String)(this.get(SESSION_ID));
    }    

    /**
     * Get the user for the session. Use lazy init to prevent problems while restart
     * of the server.
     */
	protected Person getUser() {
		Person theUser = (Person) (this.get(SESSION_DATA_USER));
        if (theUser == null) {
            String theUserName = (String) this.get(SESSION_DATA_USER_NAME);
            if (theUserName != null) {
				theUser = Person.byName(theUserName);
            }
        }
        return theUser;
    }
    
	/**
	 * Information about the client host.
	 * 
	 * @return A comma-separated list starting with {@link HttpServletRequest#getRemoteHost()} and
	 *         optionally followed by request headers identifying the client in case of a proxy
	 *         deployment.
	 */
    protected String getClientIP() {
        return (String)(this.get(SESSION_DATA_IP));
    }    

    protected long getCreationTime() {
        return ((Long)this.get(CREATION_TIME)).longValue();
    }
    
    protected long getLastAccessedTime() {
        return ((Long)this.get(LAST_ACCESS_TIME)).longValue();       
    }
    
    protected long getMaximumInactiveTime() {
        return ((Long)this.get(MAX_INACTIVE_TIME)).longValue();
    }

    protected long getInactiveTime() {
        long currenttime        = System.currentTimeMillis();
        long lastaccesstime     = this.getLastAccessedTime();
        long inactivetime       = currenttime - lastaccesstime;
        
        return(inactivetime);
    }
    
    /**
    * Checks if the inactive time exceeds the max inactive
    * Time and 
    * @return true if so, false other wise
    */
    protected boolean exceedsMaxInactiveTime() {

        long maxinacttime       = this.getMaximumInactiveTime();    
        long inactivetime       = this.getInactiveTime();

        if(Logger.isDebugEnabled(this)) {
            Logger.debug("MaxInTime :"+maxinacttime,this);
            Logger.debug("inactivetime: "+inactivetime,this);        
        }

        return (inactivetime > maxinacttime);
    }

    /**
    * Checks if the inactive time exceeds the max inactive
    * Time by at least buffer msecs and 
    * returns true if so, false other wise
    *
    * @param buffer the minimum amount of time the max inactive
    * value has to be exceeded to make this method return true
    *
    * @return true / false
    */
    protected boolean exceedsMaxInactiveTime(long buffer) {

        long maxinacttime = this.getMaximumInactiveTime()+buffer;
        long inactivetime = this.getInactiveTime();

        if(Logger.isDebugEnabled(this)) {
            Logger.debug("MaxInTime :"+maxinacttime,this);
            Logger.debug("inactivetime: "+inactivetime,this);
        }

        return (inactivetime > maxinacttime);
    }
    
}
