/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.monitor;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.top_logic.base.bus.UserEvent;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.FilteredIterator;
import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.event.bus.AbstractReceiver;
import com.top_logic.event.bus.Bus;
import com.top_logic.event.bus.BusEvent;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;

/**
 * Monitor component for user activities within the application.
 * 
 * This monitor will collect all logins of users and store them as knowledge
 * objects in the default knowledge base. That objects will be represented by
 * the {@link com.top_logic.knowledge.monitor.UserSession} wrapper.
 * 
 * @author    <a href="mailto:mga@top-logic.com"></a>
 */
@ServiceDependencies(PersistencyLayer.Module.class)
public class UserMonitor extends AbstractReceiver {

	/** Number of milliseconds that {@link UserSession}s will looked up in the past. */
	public static final long THREE_DAYS = TimeUnit.DAYS.toMillis(3);

	/** Config attribute for the user names to exclude from creating login and logout events. */
    public static final String CONF_EXCLUDE_UIDS = "excludeUIDs";
    
    /** The Knowledgebase to use (usually the default KB) */
	private final KnowledgeBase kBase;
    
    
	/** User names to exclude from automatic deleting. */
	private final Set<String> excludeUIDs;

	/**
	 * Configuration for {@link UserMonitor}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends AbstractReceiver.Config {

		/**
		 * The user names to exclude from beeing logged.
		 */
		@Name(CONF_EXCLUDE_UIDS)
		@Label("Exclude user IDs")
		@Format(CommaSeparatedStringSet.class)
		Set<String> getExcludeUIDs();

		/**
		 * Namespace for the service.
		 */
		@Override
		@StringDefault(Bus.CHANGES)
		String getServiceNamespace();

		/**
		 * Name of the service.
		 */
		@Override
		@StringDefault(Bus.USER)
		String getServiceName();
	}

	/**
	 * @param context
	 *        {@link InstantiationContext} The context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link UserMonitor}.
	 */
	public UserMonitor(InstantiationContext context, Config config) {
		super(context, config);
        kBase = PersistencyLayer.getKnowledgeBase();

		excludeUIDs = config.getExcludeUIDs();
       	subscribe();
	}

	/**
     * Dispatch the Events depending on (LOGIN/LOGUT) type.
     */
    @Override
	public void receive(BusEvent anEvent) throws RemoteException {
        String theType = anEvent.getType();

        if (UserEvent.LOGGED_IN.equals(theType)) {
            this.login((UserEvent) anEvent);
        }
        else if (UserEvent.LOGGED_OUT.equals(theType)) {
            this.logout((UserEvent) anEvent);
        }
    }

    /**
     * Find a user session with the given parameters.
     * 
     * @param    aUser      The user name of the session.
     * @param    anID       The ID of the session.
     * @param    aServer    The server the user session runs.
     * @return   The found session or <code>null</code>, if there is no
     *           such session.
     */
    public UserSession findUserSession(KnowledgeBase aKB, String aUser, String anID, String aServer) {
        UserSession result = null;
        try {
			Iterator<KnowledgeItem> iter = aKB.getObjectsByAttribute(
                UserSession.OBJECT_NAME,UserSession.SESSION_ID,anID);
            
            while (iter.hasNext()) {
				KnowledgeItem theKO = iter.next();
                if (aUser  .equals(theKO.getAttributeValue(UserSession.USER_NAME)) 
                 && aServer.equals(theKO.getAttributeValue(UserSession.SERVER))
                 && null == theKO.getAttributeValue(UserSession.LOGOUT)) {
					result = UserSession.getInstance((KnowledgeObject) theKO);
                     break;
                }
            }
        }
        catch (Exception e) {
            Logger.error("failed to findUserSession(" +
                    aUser + "," + anID + "," + aServer + ")", e, this);
        }

        return result;
    }

    /**
     * Find a user session with the given parameters.
     * 
     * @param    aUser      The user name of the session.
     * @param    anID       The ID of the session.
     * @param    aServer    The server the user session runs.
     * @return   The found session or <code>null</code>, if there is no
     *           such session.
     */
    public UserSession findUserSession(String aUser, String anID, String aServer) {
        return findUserSession(kBase, aUser, anID, aServer);
    }

    /**
     * Return the iterator containing all currently open sessions.
     * 
     * @return    The requested iterator (containing only instances of
     *            {@link com.top_logic.knowledge.monitor.UserSession}).
     */
    public Iterator<UserSession> getOpenSessionsIterated() {
        return (new OpenSessionIterator(this.getUserSessions().iterator()));
    }

    /**
     * Return the iterator containing all currently open sessions.
     * 
     * @return    The requested iterator (containing only instances of
     *            {@link com.top_logic.knowledge.monitor.UserSession}).
     */
    public Iterator<UserSession> getOpenSessionsIterated(KnowledgeBase aBase) {
        return (new OpenSessionIterator(this.getUserSessions(aBase).iterator()));
    }

    /**
     * Handle the login of a user.
     * 
     * @param    anEvent    The event holding the information about the login.
     */
    protected void login(UserEvent anEvent) {
        UserInterface theUser = anEvent.getPassiveUser();
        Date          theDate = anEvent.getDate();

		String userName = theUser.getUserName();
		if (isExcluded(userName)) {
			return;
		}
		try (Transaction tx = kBase.beginTransaction()) {
			UserSession.startSession(kBase, userName, anEvent.getSessionID(), anEvent.getMachine(), theDate);
            // This should happen in the (TL-/DB-)context of the user logging in.
			tx.commit();
        }
    }

    /**
     * Handle the logout of a user.
     * 
     * @param    anEvent    The event holding the information about the logout.
     * @return   <code>true</code>, if ending the session succeeds.
     */
    protected boolean logout(UserEvent anEvent) {
        UserInterface theUser    = anEvent.getPassiveUser();
        Date          theDate    = anEvent.getDate();

        String userName = theUser.getUserName();
		if (isExcluded(userName)) {
			return false;
		}

		UserSession   theSession = this.findSessionOnServer(userName,
                                                            anEvent.getSessionID());
		boolean theResult;
		try (Transaction tx = theSession.getKnowledgeBase().beginTransaction()) {
			theResult = theSession.endSession(theDate);

			if (theResult) {
				tx.commit();
			}
		}

        return (theResult);
    }

    /**
     * Return the open user session for the given user.
     * 
     * @param    aUser    The name of the user to find the 
     * @return   The found session.
     */
    protected UserSession findSessionOnServer(
        KnowledgeBase aBase, String aUser, String anID) {
        return findUserSession(aBase,aUser, anID, UserSession.getServerName());
    }

    /**
     * Return the open user session for the given user.
     * 
     * @param    aUser    The name of the user to find the 
     * @return   The found session.
     */
    protected UserSession findSessionOnServer(String aUser, String anID) {
        return findUserSession(kBase, aUser, anID, UserSession.getServerName());
    }
    
    /**
     * Return the current list of user sessions back to given Date.
     * 
     * The most current Logins are sorted first.
     * 
     * @param aBase The Knowledgebase to look in
     * @param aDate when null, will return alls Session from the last 24 Hours,
     *              else all Sessions newer or equal to date.
     * @param aSort Reeult will be sorted by this atttribute name, if given,
     *        else it will be sorted by Login Date.
     * 
     * @return    The list of user sessions, null on error.
     */
    protected List<UserSession> getUserSessions(KnowledgeBase aBase, Date aDate, String aSort) {
    	return this.getUserSessions(aBase, aDate, new Date(), aSort);
    }

    /**
     * Return the current list of user sessions back to given Date.
     * 
     * The most current Logins are sorted first.
     * 
     * @param aBase The Knowledgebase to look in
     * @param aSort Reeult will be sorted by this atttribute name, if given,
     *        else it will be sorted by Login Date.
     * 
     * @return    The list of user sessions, null on error.
     */
    protected List<UserSession> getUserSessions(KnowledgeBase aBase, Date aStartDate, Date anEndDate, String aSort) {
        
        if (aStartDate == null)
        	aStartDate = new Date(System.currentTimeMillis() - THREE_DAYS);
        if (anEndDate == null) {
        	anEndDate = new Date();
        }
        if (aSort == null)
            aSort = UserSession.LOGIN;
       
        // AND (TYPE("UserSession"), GREATER(LOGIN, <aDate>)) ORDER BY LOGIN
        // &(#("UserSession"),>("date",2004-05-03 11:29:08)) ORDER BY date
        // Create a Query the hard way, avoids uneeded parsing ...
        
        try {
            // Need this hack to "fix" undefined JDBC behavior :-/
            // Oracle does not like the millis ...
            // OTOH MSSQL uses even the millis
            aStartDate = new Timestamp((aStartDate.getTime() / 1000) * 1000);
            anEndDate  = new Timestamp((anEndDate.getTime()  / 1000) * 1000);
            

			RevisionQuery<UserSession> query = getSearchQuery(aStartDate, anEndDate);
			List<UserSession> theResult = aBase.search(query);
			Collections.reverse(theResult); // Reverse order so most recent are first
            
            return theResult;
        }
        catch (Exception e) {
            Logger.error("failed to getUserSessions()", e, this);
        } 
        return null;
    }

	private RevisionQuery<UserSession> getSearchQuery(Date startDate, Date endDate) {
		Expression loginAttr = attribute(UserSession.OBJECT_NAME, UserSession.LOGIN);
		Expression loginGEStartDate = ge(loginAttr, literal(startDate));
		Expression loginLEEndDate = le(loginAttr, literal(endDate));
		return queryResolved(filter(allOf(UserSession.OBJECT_NAME), and(loginLEEndDate, loginGEStartDate)),
			UserSession.class);
	}

    /**
     * Return the current list of user sessions from the last three days.
     * 
     * The most current Logins are sorted first.
     * 
     * @param aBase The Knowledgebase to look in
     * 
     * @return    The list of user sessions, null on error.
     */
    public List<UserSession> getUserSessions(KnowledgeBase aBase) {
        
        return getUserSessions(aBase,
                new Date(System.currentTimeMillis() - THREE_DAYS), 
                UserSession.LOGIN);
    }

    /**
     * Return the current list of user sessions from the last three days.
     * 
     * The most current Logins are sorted first.
     * 
     * @return    The list of user sessions, null on error.
     */
    public List<UserSession> getUserSessions() {
        return getUserSessions(kBase);
    }
    
    /** 
     * Return the list of user sessions in the given interval.
     * 
     * @param aStartDate the start date. If <code>null</code> the current date - 3 days will be taken
     * @param anEndDate	 the end date. If <code>null</code> the current date will be taken
     * @return the session list
     */
    public List<UserSession> getUserSessions(Date aStartDate, Date anEndDate) {
    	return this.getUserSessions(kBase, aStartDate, anEndDate, UserSession.LOGIN);
    }

	private boolean isExcluded(String userName) {
		return excludeUIDs.contains(userName);
	}

	/**
	 * Returns the {@link UserSession}s within the given range.
	 * 
	 * @param start
	 *        See {@link UserMonitor#getUserSessions(Date, Date)}
	 * @param end
	 *        See {@link UserMonitor#getUserSessions(Date, Date)}
	 * @return {@link UserMonitor#getUserSessions(Date, Date)} or an empty list if not active. In
	 *         such case a warning is logged.
	 */
	public static List<UserSession> userSessions(Date start, Date end) {
		if (Module.INSTANCE.isActive()) {
			return getInstance().getUserSessions(start, end);
		} else {
			Logger.warn(
				"The " + UserMonitor.class
					+ " seems to be disabled. Therefore this component can not work. Please check your configuration.",
				UserMonitor.class);
			return Collections.emptyList();
		}
	}

	/**
     * Special iterator for all currently open user sessions.
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
	public class OpenSessionIterator extends FilteredIterator<UserSession> {

        /**
         * Create a new instance of this iterator.
         * 
         * @param    someToBeWrapped    The iterator to be wrapped.
         */
		public OpenSessionIterator(Iterator<UserSession> someToBeWrapped) {
            super(someToBeWrapped);
        }

        @Override
		protected boolean test(UserSession anObject) {
			return (!anObject.isFinished());
        }
    }

    /**
     * Comparator for the tree set of UserSessions.
     * 
     * This comparator will compare the login dates of the given objects.
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
     */
	public class UserSessionComparator implements Comparator<UserSession> {

        @Override
		public int compare(UserSession arg0, UserSession arg1) {
			return arg1.getLogin().compareTo(arg0.getLogin());
        }
    }

	/**
	 * Return the UserMonitor configured
	 */
    public static UserMonitor getInstance() {
        return Module.INSTANCE.getImplementationInstance();
    }

	/**
	 * {@link Module} to configure when {@link UserMonitor} is needed.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<UserMonitor> {
		
		/**
		 * Singleton instance of this {@link Module}
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<UserMonitor> getImplementation() {
			return UserMonitor.class;
		}
	}
    
}
