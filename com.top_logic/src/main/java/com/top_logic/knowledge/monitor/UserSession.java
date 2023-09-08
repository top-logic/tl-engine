/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.monitor;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * Information about the session of a user.
 * 
 * Every session of a user will be logged in the system by a knowledge object
 * wrapped by this class. This will provide the access to the data and some
 * more functions.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class UserSession extends AbstractWrapper {

    /** The name of the meta object of the held knowledge object. */
    public static final String OBJECT_NAME = "UserSession";

    /** Attribute name for the name of the user. */
    public static final String USER_NAME = "name";

    /** Attribute name for the ID of the users session. */
    public static final String SESSION_ID = "id";

    /** Attribute name for the IP address of the users machine. */
    private static final String SESSION_MACHINE = "machine";

    /** Attribute name for the login of the user. */
    public static final String LOGIN = "date";

    /** Attribute name for the login of the user. */
    public static final String LOGOUT = "logout";

    /** Attribute name for the server, the user is logged on. */
    public static final String SERVER = "server";

    /** The name of this machine. */
    private static String machine;

    /**
     * Create a new instance of this class.
     * 
     * @param    ko         The UserSession object to be wrapped.
     */
    public UserSession(KnowledgeObject ko) {
        super(ko);
    }

    @Override
	protected String toStringValues() {
		return super.toStringValues() +
			", user: " + this.getUsername() +
			", finished: " + this.isFinished() +
			", server: " + this.getServer() +
			", login: " + this.getLogin();
    }

    /**
     * Return the name of the owner of this session.
     * 
     * @return    The user name of the owner or <code>null</code> in case of
     *            failure in getting the name of the user.
     */
    public String getUsername() {
		return tGetDataString(USER_NAME);
    }

    /**
     * Return the ID of the original HTTP-Session.
     * 
     * @return    The ID of the session or <code>null</code> in case of
     *            failure in getting it.
     */
    public String getSessionId() {
		return tGetDataString(SESSION_ID);
    }

    /**
     * Return the {@link Person person object} of the owner of this session.
     * 
     * @return    The requested person or <code>null</code> in case of
     *            failure in getting the name of the user..
     */
    public Person getPerson() {
        String theUser = this.getUsername();

        if (theUser != null) {
            return (PersonManager.getManager().getPersonByName(theUser));
        }
        else {
            return (null);
        }
    }

    /**
     * Return the login time of the user in this session.
     * 
     * @return    The login time or <code>null</code> if something went wrong.
     */
    public Date getLogin() {
		return tGetDataDate(LOGIN);
    }

    /**
     * Return the logout time of the user in this session.
     * 
     * If the user session is still valid, this method will return 
     * <code>null</code> also when the read of logout time fails.
     * In case of an error, this method will drop a message to the
     * logger.
     * 
     * @return    The logout time or <code>null</code>.
     */
    public Date getLogout() {
		return tGetDataDate(LOGOUT);
    }

    /**
     * Return the machine, the user is working on in this session.
     * 
     * @return    The client machine (IP address) or <code>null</code>.
     */
    public String getMachine() {
		return tGetDataString(SESSION_MACHINE);
    }

    /**
     * Return the server, the user is working on in this session.
     * 
     * @return    The server machine or <code>null</code>.
     */
    public String getServer() {
		return tGetDataString(SERVER);
    }

    /**
     * Return the duration of this session in milliseconds.
     * 
     * If this session is finished, the duration will be the difference of
     * the login time to the logout time. If the session is still open, the
     * duration will be the difference of the  login time to now. If the 
     * login time is null, this method will return 0.
     * 
     * @return    The duration of this session in milliseconds
     */
    public long getDuration() {
        Date theLogin = this.getLogin();

        if (theLogin == null) {
            return (0);
        }
        // else 
        Date theLogout = this.getLogout();

        if (theLogout == null) {
            return System.currentTimeMillis() - theLogin.getTime();
        }
        // else
        return (theLogout.getTime() - theLogin.getTime());
    }

    /**
     * Check, if this session has ended.
     * 
     * @return    <code>true</code>, when this session has ended.
     */
    public boolean isFinished() {
        return (this.getLogout() != null);
    }

    /**
     * End the current session of this user if it's not finished..
     * 
     * @param    aDate    The date of finish of this session.
     * @return   <code>true</code>, if calling this method results in a change
     *           of the held knowledge object.
     */
    public boolean endSession(Date aDate) {
		boolean active = !isFinished();
		if (active) {
			this.tSetData(LOGOUT, aDate);
		}
		return active;
    }

    /**
     * Return the name of this server.
     * 
     * This will be needed, when running in a cluster to identify the server
     * machine, the user is working on.
     * 
     * @return    The name of this server or "localhost" if getting the real
     *            name fails.
     */
    public static String getServerName() {
        if (UserSession.machine == null) {
            try {
                UserSession.machine = InetAddress.getLocalHost().getHostName();
            }
            catch (UnknownHostException ex) {
                Logger.error("Unable to find host name of this machine", ex, UserSession.class);

                UserSession.machine = "localhost";
            }
        }

        return (UserSession.machine);
    }

    /**
     * Start a session for the given user using the given KBase.
     *
     * @param    aName     The user name.
     * @param    aLogin    The login date of the user.
     */
    public static UserSession startSession(KnowledgeBase aKBase,
        String aName, String anID, String anIP, Date aLogin) {
        UserSession   theSession = null;
		{
			KnowledgeObject theKO = aKBase.createKnowledgeObject(OBJECT_NAME);

            theKO.setAttributeValue(USER_NAME, aName);
            theKO.setAttributeValue(LOGIN, (aLogin != null) ? aLogin : new Date());
            theKO.setAttributeValue(SESSION_ID, anID);
            theKO.setAttributeValue(SESSION_MACHINE, anIP);
            theKO.setAttributeValue(SERVER, UserSession.getServerName());

            theSession = UserSession.getInstance(theKO);
        }

        return (theSession);
    }

    /**
     * Start a session for the given user using default KBase.
     *
     * @param    aName     The user name.
     * @param    aLogin    The login date of the user.
     */
    public static UserSession startSession(String aName, String anID, String anIP, Date aLogin) {
        
        return startSession(getDefaultKnowledgeBase(),
                            aName, anID, anIP, aLogin);
    }

    /**
     * Return a user session for the given knowledge object.
     * 
     * @param    aKO    The knowledge object to be wrapped.
     * @return   The wrapped object.
     */
    public static UserSession getInstance(KnowledgeObject aKO) {
        return ((UserSession) WrapperFactory.getWrapper(aKO));
    }


    /**
     * Close all session leftover from previous runs.
     * 
     * Used during initial setup to cleanup the KnowledgeBase.
     * This will only close sessions that originated on this machine.
     * This will <code>commit()</code> the given KnowledgeBase.
     * 
     * @param maxcount the maximum number of Objects to check.
     */
    public static void cleanupUserSessions(KnowledgeBase aBase, int maxcount) {

        try {
            ThreadContext.pushSuperUser();
            Date          theDate    = new Date();
            String        theServer  = UserSession.getServerName();
                
			RevisionQuery<KnowledgeObject> query = queryUnresolved(filter(
				allOf(UserSession.OBJECT_NAME),
				eqBinary(attribute(UserSession.OBJECT_NAME, UserSession.SERVER), literal(theServer))
			), order(attribute(UserSession.OBJECT_NAME, UserSession.LOGIN)));
			Collection result = aBase.search(query);
    
            int      skip = result.size() - maxcount;
            Iterator iter = result.iterator(); // no need to fix these, too
            while (skip > 0 && iter.hasNext()) {
                skip --;
                iter.next();     
            }
            
            int fixed = 0;
            try {
                while( iter.hasNext()) {
                    KnowledgeObject ko = (KnowledgeObject) iter.next();
                    if (null == ko.getAttributeValue(UserSession.LOGOUT)) {
                        fixed++;
                        ko.setAttributeValue(UserSession.LOGOUT, theDate);
                    }
                }
            }
            catch (DataObjectException dox) {
                Logger.error("Unable to cleanupUserSessions()", dox, UserSession.class);
            }
            if (fixed > 0) {
                Logger.info("Fixed " + fixed + " unterminated UserSession(s)", UserSession.class);
                aBase.commit();
            }
        }
        finally {
            ThreadContext.popSuperUser();
        }
    }

    /**
     * Close all session leftover from previous runs.
     * 
     * Used during initial setup to cleanup the KnowledgeBase.
     * This will only close sessions that originated on this machine.
     * 
     * @param maxcount the maximum number of Objects to check.
     */
    public static void cleanupUserSessions(int maxcount) {
        cleanupUserSessions(getDefaultKnowledgeBase(), maxcount);
    }
    
    /** 
     * Resolve a List of UserSessions from a Set of KOs
     */
    protected static List getSessionsFromSet(Set aSet) {
        return getWrappersFromCollection(aSet);
    }
}
