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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import com.top_logic.base.bus.UserEvent;
import com.top_logic.base.context.DefaultSessionContext;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.event.bus.Bus;
import com.top_logic.event.bus.Sender;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;
import com.top_logic.util.license.LicenseTool;
import com.top_logic.util.license.TLLicense;

/**
 * {@link ManagedClass} holding active user sessions.
 */
@ServiceDependencies({ PersonManager.Module.class, ThreadContextManager.Module.class })
public final class SessionService extends ConfiguredManagedClass<SessionService.Config>
		implements HttpSessionBindingListener {
	
	/**
	 * Configuration for {@link SessionService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<SessionService> {
		/**
		 * @see #getOnlyOneSession()
		 */
		String ONLY_ONE_SESSION = "onlyOneSession";

		/**
		 * @see #getExcludeUIDs()
		 */
		String EXCLUDE_UIDS = "excludeUIDs";

		/**
		 * @see #getSecureSessionCookie()
		 */
		String SECURE_SESSION_COOKIE = "secureSessionCookie";

		/**
		 * Flag whether to allow only one session per user. If <code>true</code>, a user gets logged
		 * out if he is logging in a second time.
		 */
		@Name(ONLY_ONE_SESSION)
		boolean getOnlyOneSession();

		/**
		 * Comma separated list without spaces of login IDs, that are excluded from
		 * {@link #getOnlyOneSession()}.
		 */
		@Name(EXCLUDE_UIDS)
		@Label("Exclude user IDs")
		String[] getExcludeUIDs();

		/**
		 * Whether the session cookie is secured with the <code>HttpOnly</code> option and the
		 * <code>Secure</code> option, if HTTPS is used.
		 */
		@Name(SECURE_SESSION_COOKIE)
		@Mandatory
		boolean getSecureSessionCookie();
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
	 * Hashtable containing session_ids and SessionInfo-objects as key/value pairs. The session info is
	 * another Hastable. It's values can be retrieved by using the appropriate methods of SessionInfo.
	 */
	private final Map<String, SessionInfo> _sessionMap = new ConcurrentHashMap<>(100);

	/**
	 * Sender to send UserEvents to the ApplicationBus
	 */
	private Sender _sender;

	private final PersonManager _personManager;

	private final ThreadContextManager _threadContextManager;

	/**
	 * Thank you for looking at this code, ask msi for details.
	 */
	private static boolean __;

	/**
	 * Initializes a new Service.
	 */
	public SessionService(InstantiationContext context, Config config) {
		super(context, config);
		_personManager = PersonManager.getManager();
		_threadContextManager = ThreadContextManager.getManager();
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
        boolean debug = Logger.isDebugEnabled(this);
        
        if (debug) {
            Logger.debug("Removing the Session from internal List", this);
        }

        this.removeSession (session);

        try {
            if (debug) {            
                Logger.debug("Invalidating Session :" + session.getId (), this);
            }

            session.invalidate ();
        }
        catch (IllegalStateException ise) {
            //the session already was invalidated (maybe timed out)
            //do nothing
            if (debug) {
                Logger.debug("Session already was invalidated:", this);
            }
        }
    }

    /**
     * Removes the given session from the session map.
     * Used to remove a previous invalidated (maybe timed out)
     * Session.
     *
     * @param sessionid  The ID of the Session to be removed
     */
	public void invalidateSession(String sessionid) {
        this.removeSession (sessionid);
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
        boolean debug = Logger.isDebugEnabled(this);
        
        if (session == null) {
            if(debug) {
               Logger.debug("The session object is not valid because it is null",this); 
            }
            request.setAttribute(ERROR,Resources.getInstance().getString(I18NConstants.ERROR_SESSION_TIMED_OUT));
            return (false);
        }

        //ok, there is a session...Logging it's ID
        
        if (debug) {
            Logger.debug("Session to be checked is: "+session.getId(),this); 
        }
        
        if (!sessionIsValid (session)) {
            if(debug) {
               Logger.debug("The session object is not valid because it is timed out or was "+
                            "invalidated because of another reason.",this); 
            }
            this.removeSession (session);
            request.setAttribute(ERROR,Resources.getInstance().getString(I18NConstants.SESSION_INVALID));
            return (false);
        }

		SessionInfo sessioninfo = _sessionMap.get(session.getId());
		if (sessioninfo == null) {
            if(debug) {
               Logger.debug("The session object is not valid because it's ID is not found "+
               "in the SessionMap - so the session was not created by the session service",this); 
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
		SessionInfo sessioninfo = _sessionMap.get(sessionid);

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
		SessionInfo sessioninfo = _sessionMap.get(sessionid);

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
		SessionInfo sessioninfo = _sessionMap.get(sessionid);

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
		SessionInfo sessioninfo = _sessionMap.get(sessionid);

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
	 * This method creates a new session for the given request and binds the given user to it. If
	 * the given request already has a session, or the given user is null it will return null. This
	 * method should be called by the LoginPageServlet only.
	 * </p>
	 * <p>
	 * If the license is demo only single login is possible.
	 * </p>
	 * <p>
	 * If there are more users in the system as the license allowed, only root can login single.
	 * </p>
	 *
	 * @param request
	 *        the request to create the session from
	 * @param response
	 *        The current response.
	 * @param aUser
	 *        Owner of the new session
	 * @return a HttpSession or null
	 */
	public HttpSession loginUser(HttpServletRequest request, HttpServletResponse response, Person aUser) {
		return login(request, response, aUser);
    }

	private HttpSession login(HttpServletRequest request, HttpServletResponse response, Person aUser) {
		LicenseTool licenseTool = LicenseTool.getInstance();
		TLLicense license = licenseTool.getLicense();
		if (licenseTool.usersExceeded(license)) {
			if (!aUser.getName().equals(PersonManager.getManager().getSuperUserName())) {
				return null;
			}
			logOutExistingSession(aUser.getName());
		}
		if (getOnlyOneSession() || licenseTool.limitToOneSession(license)) {
			logOutExistingSession(aUser.getName());
    	}
        return (getNewSessionForUser (request, response, aUser));
    }    

    /**
	 * Logs the given user out, if he has already a session.
	 * The user gets logged out only if the login name doesn't appear in the exclude list.
	 *
	 * @param userName the user to log out.
	 */
	private void logOutExistingSession(String userName) {
		if (!ArrayUtil.contains(getExcludeUIDs(), userName)) {
			Collection<String> sessionIDs = getSessionIDs();
			for (String sessionID : sessionIDs) {
				Person user = getUser(sessionID);
				if (Utils.equals(userName, user.getName())) {
					Logger.info("Logging out user '" + userName + "' because of another login." , SessionService.class);
					invalidateSession(sessionID);
				}
			}
		}
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
            Logger.error ("[getNewSessionForUser] Given User is null.", this);            
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

		sendEvent(session.getId(), aUser, aUser, UserEvent.LOGGED_IN);

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
		_sessionMap.put(session.getId(), sessioninfo);
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
        
		SessionInfo sessioninfo = _sessionMap.remove(sessionid);
		if (sessioninfo == null) {
			return;
		}
		
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
				Logger.warn(theRemovedUser.getName()
                            + " was removed by "
					+ theRemovingUser.getName(), this);
            }
	
			sendEvent(sessionid, theRemovedUser, theRemovingUser, UserEvent.LOGGED_OUT);
        }
    }

	private void sendEvent(String sessionid, Person passiveUser, Person activeUser,
			final String mode) {
		final Sender sender = this.getSender();
		if (sender != null) {
			UserEvent theEvent =
				new UserEvent(sender, passiveUser, activeUser, sessionid, this.getClientIP(sessionid), mode);

			sender.send(theEvent);
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
	 * Returns the {@link Sender} to send {@link Bus} events.
	 * 
	 * If the {@link com.top_logic.event.bus.Bus.Module BUS module} is inactive it returns
	 * <code>null</code>
	 */
	private Sender getSender() {
		if (Bus.Module.INSTANCE.isActive() && _sender == null) {
			this._sender = new Sender(Bus.CHANGES, Bus.USER);
		}
		return (this._sender);
	}

    /**
	 * The singleton {@link SessionService} instance.
	 */
    public static SessionService getInstance () {
		return Module.INSTANCE.getImplementationInstance();
    }
    
	@Override
	protected void shutDown() {
		_sessionMap.clear();
		_sender = null;
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
	 * @see Config#ONLY_ONE_SESSION
	 */
	public boolean getOnlyOneSession() {
		return getConfig().getOnlyOneSession();
	}

	/**
	 * @see Config#getExcludeUIDs()
	 */
	public String[] getExcludeUIDs() {
		return getConfig().getExcludeUIDs();
	}

	/**
	 * @see Config#getSecureSessionCookie()
	 */
	public boolean getSecureSessionCookie() {
		return getConfig().getSecureSessionCookie();
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
