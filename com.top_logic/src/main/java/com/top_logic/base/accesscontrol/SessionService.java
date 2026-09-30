/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

import com.top_logic.base.bus.UserEvent;
import com.top_logic.base.context.DefaultSessionContext;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Holds and manages the currently active user sessions.
 * 
 * <p>
 * When an {@link Person account} is deleted, all its sessions are terminated. This happens
 * independently of the way the account is deleted and also for deletions made on another cluster
 * node.
 * </p>
 * 
 * @implNote The service listens for commits of the {@link PersistencyLayer#getKnowledgeBase()
 *           default knowledge base}. Sessions of deleted persons are terminated with
 *           {@link #terminateSession(String)} in a background task of the {@link SchedulerService},
 *           because ending a session announces the logout to the {@link UserEventListener}s, which
 *           may commit changes themselves; committing is not allowed while a commit is announced
 *           to {@link UpdateListener}s.
 */
@ServiceDependencies({
	ThreadContextManager.Module.class,
	/* The SessionService caches Persons which depend on the KB and listens for deleted Persons.
	 * Therefore it must be restarted, when the KB is restarted. */
	PersistencyLayer.Module.class,
	SchedulerService.Module.class
})
@Label("User sessions")
public final class SessionService extends ConfiguredManagedClass<SessionService.Config>
		implements HttpSessionBindingListener {
	
	/**
	 * Configuration for {@link SessionService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<SessionService> {

		/**
		 * @see #getSecureSessionCookie()
		 */
		String SECURE_SESSION_COOKIE = "secureSessionCookie";

		/**
		 * Whether the session cookie is secured with the <code>HttpOnly</code> option and the
		 * <code>Secure</code> option, if HTTPS is used.
		 */
		@Name(SECURE_SESSION_COOKIE)
		@Mandatory
		boolean getSecureSessionCookie();

		/**
		 * Configuration of user event listeners to react on login and logout.
		 */
		List<UserEventListenerConfig> getListeners();
	}

	/**
	 * {@link NamedConfigMandatory} holding the configuration of an {@link UserEventListener}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface UserEventListenerConfig extends NamedConfigMandatory {

		/**
		 * Configuration of the event listener.
		 */
		PolymorphicConfiguration<? extends UserEventListener> getImpl();
	}

	/**
	 * Listener for {@link UserEvent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface UserEventListener extends EventListener {

		/**
		 * Handles the given {@link UserEvent}.
		 */
		void notifyUserEvent(UserEvent event);

	}

	/** Name used to attach the {@link TLSessionContext} to a HTTPSession. */
	public static final String CONTEXT_NAME = TLSessionContext.class.getName();

	/** Key for the session info. */
	public static final String SESSION_INFO_KEY = "session_info";

	/**
	 * Key to bind the specific reason why a session is not valid to the request as attribute. This way
	 * the caller can use this reason to generate more detailed error messages.
	 */
	public static final String ERROR = "SessionValidationError";

	/**
	 * The registered sessions indexed by their session IDs.
	 */
	private final Map<String, Registration> _sessionMap = new ConcurrentHashMap<>(100);

	/**
	 * {@link UpdateListener} terminating the sessions of deleted {@link Person}s.
	 */
	private final UpdateListener _personDeletionListener = this::handleUpdate;

	/**
	 * The {@link KnowledgeBase} {@link #_personDeletionListener} is registered at.
	 */
	private KnowledgeBase _kb;

	/**
	 * Consumers to consume {@link UserEvent}.
	 */
	private final List<UserEventListener> _userEventListeners;

	private final ThreadContextManager _threadContextManager;

	/**
	 * Initializes a new Service.
	 */
	public SessionService(InstantiationContext context, Config config) {
		super(context, config);
		_threadContextManager = ThreadContextManager.getManager();
		List<? extends PolymorphicConfiguration<? extends UserEventListener>> listenerConfigs =
			config.getListeners().stream().map(UserEventListenerConfig::getImpl).toList();
		_userEventListeners = TypedConfiguration.getInstanceListReadOnly(context, listenerConfigs);
	}

    //------------------PUBLIC METHODS----------------------

    /**
     * Returns a valid (in terms of top logic) session or null.
     * That means if there is no valid session, null is returned.
     * For what means "valid", see the validateSession() - method;
     * 
     * You can acquire a Session the directly from the Request
     * as long as your code is called by the 
     * {@link com.top_logic.util.TopLogicServlet}
     *
     * @param    request    The request to get the Session from.
     * @return   A session or null.
     */
	public HttpSession getSession(HttpServletRequest request) {
        // getting Session from request
        if (this.validateSession(request)) {
            
            return request.getSession (false);    
        }else {
            return(null);
        }
    }

    /**
     * Invalidates the given session and removes it
     * from the session map
     */
	public void invalidateSession(HttpSession session) {
		boolean debug = Logger.isDebugEnabled(SessionService.class);
        
        if (debug) {
			Logger.debug("Removing the Session from internal List", SessionService.class);
        }

        this.removeSession (session);

        try {
			if (debug) {
				Logger.debug("Invalidating Session.", SessionService.class);
            }

            session.invalidate ();
        }
        catch (IllegalStateException ise) {
            //the session already was invalidated (maybe timed out)
            //do nothing
            if (debug) {
				Logger.debug("Session already was invalidated:", SessionService.class);
            }
        }
    }

	/**
	 * Removes the given session from the session map.
	 * 
	 * <p>
	 * Used to remove a previously invalidated (maybe timed out) session. The {@link HttpSession}
	 * itself is not touched. To end a session that is still active, use
	 * {@link #terminateSession(String)}.
	 * </p>
	 *
	 * @param sessionid
	 *        The ID of the Session to be removed
	 */
	public void invalidateSession(String sessionid) {
        this.removeSession (sessionid);
    }

	/**
	 * Ends the session with the given ID.
	 * 
	 * <p>
	 * The session is removed from the session map, its logout is announced to the
	 * {@link UserEventListener}s, and the {@link HttpSession} is invalidated. The next request of
	 * the client is therefore processed without a user.
	 * </p>
	 * 
	 * @param sessionId
	 *        The ID of the session to end. Nothing happens, if no such session is registered.
	 */
	public void terminateSession(String sessionId) {
		Registration registration = _sessionMap.get(sessionId);
		if (registration == null) {
			return;
		}

		// Removing first ensures that the logout is announced exactly once, independent of whether
		// the invalidation below reaches valueUnbound(), which removes the session again.
		removeSession(sessionId);

		try {
			registration.session().invalidate();
		} catch (IllegalStateException ex) {
			// The session was already invalidated (e.g. timed out).
			if (Logger.isDebugEnabled(SessionService.class)) {
				Logger.debug("Session already was invalidated.", SessionService.class);
			}
		}
	}

	/**
	 * Ends all sessions of the given user.
	 * 
	 * @param user
	 *        The user whose sessions are ended. The user may already be deleted.
	 * 
	 * @see #terminateSession(String)
	 */
	public void terminateSessions(Person user) {
		for (String sessionId : sessionIdsOf(Set.of(user.tId()))) {
			terminateSession(sessionId);
		}
	}

	/**
	 * The IDs of all registered sessions whose user has one of the given identities.
	 */
	private List<String> sessionIdsOf(Set<ObjectKey> userIds) {
		List<String> result = new ArrayList<>();
		for (Registration registration : _sessionMap.values()) {
			Person user = registration.info().getUser();
			if (user != null && userIds.contains(user.tId())) {
				result.add(registration.info().getSessionId());
			}
		}
		return result;
	}

	/**
	 * Terminates the sessions of all {@link Person}s deleted in the given commit.
	 */
	private void handleUpdate(KnowledgeBase sender, UpdateEvent event) {
		Set<ObjectKey> deletedKeys = event.getDeletedObjectKeys();
		if (deletedKeys.isEmpty() || _sessionMap.isEmpty()) {
			return;
		}

		Set<ObjectKey> deletedPersons = new HashSet<>();
		for (ObjectKey key : deletedKeys) {
			if (Person.OBJECT_NAME.equals(key.getObjectType().getName())) {
				deletedPersons.add(key);
			}
		}
		if (deletedPersons.isEmpty()) {
			return;
		}

		List<String> sessionIds = sessionIdsOf(deletedPersons);
		if (sessionIds.isEmpty()) {
			return;
		}

		SchedulerService.getInstance().execute(
			() -> ThreadContextManager.inSystemInteraction(SessionService.class, () -> {
				for (String sessionId : sessionIds) {
					try {
						terminateSession(sessionId);
					} catch (RuntimeException ex) {
						Logger.error("Failed to terminate session of deleted user.", ex, SessionService.class);
					}
				}
			}));
	}

	private SessionInfo info(String sessionId) {
		Registration registration = _sessionMap.get(sessionId);
		return registration == null ? null : registration.info();
	}

    /**
     * Checks if we have an valid session for the given request
     * A session is ok if:
     * It is not null AND it is not invalidated AND it's id is
     * found in the session map (so the user is logged in) AND
     * a user is bound to the session. If any of these constraints is
     * false, this method will return false.
     *
     * @param request The Request of the client to be checked
     *
     * @return true if all ok, false otherwise
     */
	public boolean validateSession(HttpServletRequest request) {
        
        HttpSession session = request.getSession(false);
		boolean debug = Logger.isDebugEnabled(SessionService.class);
        
        if (session == null) {
            if(debug) {
				Logger.debug("The session object is not valid because it is null", SessionService.class);
            }
            request.setAttribute(ERROR,Resources.getInstance().getString(I18NConstants.ERROR_SESSION_TIMED_OUT));
            return (false);
        }

		if (debug) {
			Logger.debug("Checking session.", SessionService.class);
        }
        
        if (!sessionIsValid (session)) {
            if(debug) {
               Logger.debug("The session object is not valid because it is timed out or was "+
					"invalidated because of another reason.", SessionService.class);
            }
            this.removeSession (session);
            request.setAttribute(ERROR,Resources.getInstance().getString(I18NConstants.SESSION_INVALID));
            return (false);
        }

		SessionInfo sessioninfo = info(session.getId());
		if (sessioninfo == null) {
            if(debug) {
               Logger.debug("The session object is not valid because it's ID is not found "+
					"in the SessionMap - so the session was not created by the session service", SessionService.class);
            }
            request.setAttribute(ERROR,Resources.getInstance().getString(I18NConstants.SESSION_NOT_FOUND));
            return (false);
        }


        // update time of last access -> prevent timeout
        sessioninfo.setLastAccessedTime (System.currentTimeMillis ());
        return(true);
    }

    /**
     * Returns the User object associated to the given session
     *
     *
     * @param  sessionid    a Session ID
     *
     * @return the user which is associated with the given session id
     */
	public Person getUser(String sessionid) {
		SessionInfo sessioninfo = info(sessionid);

        //if no session info found for the given session id

        if (sessioninfo == null) {
            return (null);
        }
        else {
            return sessioninfo.getUser ();
        }
    }

	/**
	 * Information about the client host.
	 * 
	 * @return A comma-separated list starting with {@link HttpServletRequest#getRemoteHost()} and
	 *         optionally followed by request headers identifying the client in case of a proxy
	 *         deployment.
	 */
    public String getClientIP (String sessionid) {
		SessionInfo sessioninfo = info(sessionid);

        //if no session data found for the given session id
        if (sessioninfo == null) {
            return (null);
        }
        else {
            return sessioninfo.getClientIP ();
        }
    }

	/**
	 * Returns the creation time of the clients session
	 * 
	 * @param sessionid
	 *        a Session ID
	 * 
	 * @return the creation time of the clients session as {@link Date} representation. null if the
	 *         given session id is unknown
	 */
	public Date getCreationTime(String sessionid) {
		SessionInfo sessioninfo = info(sessionid);

        //if no session data found for the given session id
        if (sessioninfo == null) {
            return (null);
        }
        else {
			return new Date(sessioninfo.getCreationTime());
        }
    }

	/**
	 * Returns the last access time of the clients session
	 * 
	 * @param sessionid
	 *        a Session ID
	 * 
	 * @return the last access time of the clients session as {@link Date} representation. null if
	 *         the given session id is unknown
	 */
	public Date getLastAccessedTime(String sessionid) {
		SessionInfo sessioninfo = info(sessionid);

		// if no session data found for the given session id
		if (sessioninfo == null) {
			return (null);
		} else {
			return new Date(sessioninfo.getLastAccessedTime());
		}
	}

    /**
     * Returns all SessionIDs which have been stored so far
     * that means all users logged in ;)
     *
     * @return    All session IDs as Enumeration of Strings
     */
	public Collection<String> getSessionIDs() {
		return new ArrayList<>(_sessionMap.keySet());
    }

    /**
	 * <p>
	 * This method creates a new session for the given request and binds the given user to it.
	 * </p>
	 *
	 * @param request
	 *        the request to create the session from
	 * @param response
	 *        The current response.
	 * @param aUser
	 *        Owner of the new session
	 */
	@FrameworkInternal
	public HttpSession loginUser(HttpServletRequest request, HttpServletResponse response, Person aUser) {
		return login(request, response, aUser);
    }

	private HttpSession login(HttpServletRequest request, HttpServletResponse response, Person aUser) {
        return (getNewSessionForUser (request, response, aUser));
    }    

	/**
     * Creates a new session for the given request an binds the given
     * User to it.If the given request already has a session, it will be invalidated
     * and a new one will be created.
     *
     * @param request The request to get the Session from
	 * @param response The current response.
     * @param aUser   The User for which the session should be created.
     * @exception     NullPointerException if the given User is null
     *
     * @return always a new session
     */
    private HttpSession getNewSessionForUser (HttpServletRequest request, 
			HttpServletResponse response, Person aUser) {
        //checking if the given user is null. If so return null.       
        if (aUser == null)  {
			Logger.error("[getNewSessionForUser] Given User is null.", SessionService.class);
            throw new NullPointerException("Given User is null.");
        }
        
		// Create a new session, if none does exist yet.
		HttpSession session = request.getSession(true);
		
		if (getSecureSessionCookie()) {
			boolean secure = "https".equalsIgnoreCase(request.getScheme());

			// Ticket #8127: Workaround for pre Servlet 3.0 applications: Ensure that the session
			// cookie cannot be stolen with injected JavaScript. Note: The HttpOnly flag seems only 
			// to be honored, if combined with the path attribute.
			StringBuilder cookieBuffer = new StringBuilder();
			cookieBuffer.append("JSESSIONID=");
			cookieBuffer.append(session.getId());
			cookieBuffer.append("; Path=");
			cookieBuffer.append(request.getContextPath());
			cookieBuffer.append("; HttpOnly");
			if (secure) {
				cookieBuffer.append("; Secure");
			}
			response.setHeader("SET-COOKIE", cookieBuffer.toString());
		}

		TLSessionContext sessionContext = installSession(aUser, session);
        
        this.putSession (session, aUser, request, sessionContext);            

		sendEvent(session.getId(), aUser, aUser, UserEvent.EventType.LOGGED_IN);

        return (session);
    }

	private TLSessionContext installSession(Person aUser, HttpSession session) {
		DefaultSessionContext context = (DefaultSessionContext) _threadContextManager.newSessionContext(session);
		Person person = aUser;
		context.setOriginalUser(person);
		session.setAttribute(CONTEXT_NAME, context);
		// install new context in current subsession and interaction.
		InteractionContext interaction = ThreadContextManager.getInteraction();
		if (interaction != null) {
			SubSessionContext subsession = interaction.getSubSessionContext();
			if (subsession != null) {
				subsession.setSessionContext(context);

				subsession.setCurrentLocale(aUser.getLocale());
			}
			interaction.installSessionContext(context);
		}
		return context;
	}

    /**
     * Puts a new Session into the session map.
     */
	private void putSession(HttpSession session, Person aUser, HttpServletRequest aRequest,
			TLSessionContext sessionContext) {
		SessionInfo sessioninfo = createSessionInfo(session, aRequest, aUser);

		sessionContext.addHttpSessionBindingListener(this);

        //storing session id and session info in session map
		_sessionMap.put(session.getId(), new Registration(sessioninfo, session));
    }

	@Override
	public void valueBound(HttpSessionBindingEvent event) {
		// Ignore.
	}

	/**
	 * Session is invalidated, go and notify the SessionService.
	 * 
	 * @param event
	 *        The thrown event.
	 */
	@Override
	public void valueUnbound(HttpSessionBindingEvent event) {
		String sessionId = event.getSession().getId();
		invalidateSession(sessionId);
	}

    /**
     * Creates a new SessionInfo Object for the given HTTP session,
     * request and User.
     *
     * @param    aRequest    ???
     * @param    session     ???
     * @param    aUser       ???
     *
     * @return a new SessionInfo
     */
	private SessionInfo createSessionInfo(HttpSession session, HttpServletRequest aRequest, Person aUser) {

        //preparing SessionInfo...this is what is stored for each session
        //in the session map, which is only used in this class
        SessionInfo sessioninfo = new SessionInfo ();

		sessioninfo.setUser(aUser);

		sessioninfo.setClientIP(clientHost(aRequest));
        sessioninfo.setCreationTime         (session.getCreationTime ());
        sessioninfo.setMaximumInactiveTime  (session.getMaxInactiveInterval ());
        sessioninfo.setSessionId            (session.getId());

        return (sessioninfo);
    }

	/**
	 * Retrieves information about the client accessing this web application.
	 */
	public static String clientHost(HttpServletRequest request) {
		String result = request.getRemoteHost();
		result = addHeader(result, request, "X-Forwarded-For");
		result = addHeader(result, request, "X-Forwarded");
		result = addHeader(result, request, "Forwarded");
		return result;
	}

	private static String addHeader(String result, HttpServletRequest request, String header) {
		Enumeration<String> forwardedFor = request.getHeaders(header);
		while (forwardedFor.hasMoreElements()) {
			result += ", " + header + ": " + forwardedFor.nextElement();
		}
		return result;
	}

    /**
     * Removes the given session from our map.
     *
     * @param    session    The session to be removed.
     */
	private void removeSession(HttpSession session) {
        this.removeSession (session.getId ());
    }

    /**
     * Removes the given session from our map.
     *
     * @param  sessionid ID of the the session to be removed.
     */
	private void removeSession(String sessionid) {
        
		Registration registration = _sessionMap.remove(sessionid);
		if (registration == null) {
			return;
		}
		SessionInfo sessioninfo = registration.info();

		Person theRemovedUser = sessioninfo.getUser();
		{
			Person theRemovingUser = null;
            TLContext     context            = TLContext.getContext();
            if (context != null) {
				theRemovingUser = context.getPerson();
            }
           
            if(theRemovingUser == null) {
                theRemovingUser = theRemovedUser; 
            }
            else if (!theRemovingUser.equals(theRemovedUser)) {
				// The removed user may already be deleted, use the name stored in the session info.
				Logger.warn(sessioninfo.getUserName()
                            + " was removed by "
					+ theRemovingUser.getName(), this);
            }
	
			sendEvent(sessionid, theRemovedUser, theRemovingUser, UserEvent.EventType.LOGGED_OUT);
        }
    }

	private void sendEvent(String sessionid, Person passiveUser, Person activeUser,
			UserEvent.EventType mode) {
		if (_userEventListeners.isEmpty()) {
			return;
		}
		UserEvent event = new UserEvent(passiveUser, activeUser, sessionid, this.getClientIP(sessionid), mode);
		for (UserEventListener consumer : _userEventListeners) {
			consumer.notifyUserEvent(event);
		}
	}

    /**
     * Checks if a session has been invalidated (e.g. in cause of timeout).
     *
     * @param    session    The session to verify.
     * @return   true if the given session is valid, false otherwise.
     */
	private boolean sessionIsValid(HttpSession session) {
        //we simply try to get the creation time of the session.
        //if the session has been invalidated in the meantime
        //this call should throw an illegalStateException

        try {
            /* long creation_time = */ session.getCreationTime ();            

            return (true);
        }
        catch (IllegalStateException ise) {
            return (false);
        }
    }

    /**
	 * The singleton {@link SessionService} instance.
	 */
    public static SessionService getInstance () {
		return Module.INSTANCE.getImplementationInstance();
    }
    
	@Override
	protected void startUp() {
		super.startUp();
		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(_personDeletionListener);
	}

	@Override
	protected void shutDown() {
		if (_kb != null) {
			_kb.removeUpdateListener(_personDeletionListener);
			_kb = null;
		}
		for (String sessionId : new ArrayList<>(_sessionMap.keySet())) {
			invalidateSession(sessionId);
		}
		_sessionMap.clear();
		super.shutDown();
	}
	
	/**
	 * Returns the <i>TopLogic</i> representation for the given {@link HttpSession session}.
	 * 
	 * @return <code>null</code> iff no {@link TLSessionContext} available for the given
	 *         {@link HttpSession}.
	 */
	public TLSessionContext getSession(HttpSession session) {
		return (TLSessionContext) session.getAttribute(CONTEXT_NAME);
	}

	/**
	 * @see Config#getSecureSessionCookie()
	 */
	public boolean getSecureSessionCookie() {
		return getConfig().getSecureSessionCookie();
	}

	/**
	 * A session registered in the session map.
	 * 
	 * @param info
	 *        Information about the session.
	 * @param session
	 *        The session itself.
	 */
	private record Registration(SessionInfo info, HttpSession session) {
		// Pure data.
	}

	/**
	 * Module for instantiation of the {@link SessionService}.
	 */
	public static class Module extends TypedRuntimeModule<SessionService> {

		/** Singleton for this module. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<SessionService> getImplementation() {
			return SessionService.class;
		}

	}
}
