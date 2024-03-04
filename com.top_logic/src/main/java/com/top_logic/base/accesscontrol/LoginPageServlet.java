/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.knowledge.gui.layout.HttpSecureHeaderFilter;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.URLPathBuilder;
import com.top_logic.mig.html.UserAgent;
import com.top_logic.util.DeferredBootUtil;
import com.top_logic.util.DispatchException;
import com.top_logic.util.NoContextServlet;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * This class provides the functionality to get the LoginPage and send it
 * to the Webserver when receiving a forwarded request from the
 * DispatcherService. Checks the filled-in LoginPage returned; re-sends it
 * three times if the password provided by the user is incorrect; else
 * forwards the request to target by resolving it via the DispatcherService.
 *
 * @author    <a href="mailto:tri@top-loigc.com">Thomas Richter</a>
 */
public class LoginPageServlet extends NoContextServlet {

	/**
	 * Init-parameter switch to disable XSS protection checking.
	 */
	public static final String DISABLE_SECURE_HEADER_CHECK = "disableSecureHeaderCheck";

	public static final String MESSAGE_ATTR = "message";

	/**
	 * Request parameter that is created by the submit button, when rendered as image input.
	 * 
	 * <p>
	 * Seems to be the click position on the image button. This is created, even if the button has
	 * no name. Other unnamed inputs do not create submission arguments.
	 * </p>
	 * 
	 * @see #SUBMIT_BUTTON_X_PARAMETER
	 * @see #SUBMIT_BUTTON_Y_PARAMETER
	 */
	private static final String SUBMIT_BUTTON_X_PARAMETER = "x";

	/**
	 * Request parameter that is created by the submit button, when rendered as image input.
	 * 
	 * @see #SUBMIT_BUTTON_X_PARAMETER
	 * @see #SUBMIT_BUTTON_Y_PARAMETER
	 */
	private static final String SUBMIT_BUTTON_Y_PARAMETER = "y";

	private static final String PASSWORD1_PARAMETER = "pwd1";

	private static final String PASSWORD2_PARAMETER = "pwd2";

	private static final String CHANGE_PWD_PARAMETER = "changePwd";

	/**
     * Used for testing if Cookies are enabled
     */
	public static final String TEST_SESSION = "testSession";

    /**
     * Should we check if Cookies/Sessions are enabled.
     */
    public static final String ENABLE_SESSION_CHECK = "shouldSessionBeChecked";

	/**
	 * The parameter that declares which start page to use.
	 */
	public static final String PARAM_START_PAGE = "startPage";

	/**
	 * The parameter that declares the login retry page to use.
	 */
	public static final String PARAM_LOGIN_RETRY_PAGE = "retryPage";

	/**
	 * The parameter that declares the login error page to use.
	 */
	public static final String PARAM_LOGIN_ERROR_PAGE = "loginError";

    /**
     * @see #forwardToStartPage(HttpServletRequest, HttpServletResponse)
     */
	private static final Set<String> AUTHENTICATION_PARAMS = new HashSet<>(Arrays.asList(
	    SUBMIT_BUTTON_X_PARAMETER,
	    SUBMIT_BUTTON_Y_PARAMETER,
		Login.USER_NAME,
		Login.PASSWORD,
		PASSWORD1_PARAMETER,
		PASSWORD2_PARAMETER,
		CHANGE_PWD_PARAMETER,
		PARAM_START_PAGE,
		PARAM_LOGIN_RETRY_PAGE,
		PARAM_LOGIN_ERROR_PAGE,
		ENABLE_SESSION_CHECK));

	/**
	 * @see #getConfiguredLoginHook()
	 * @see Login.Config#getLoginHook()
	 */
	private LoginHook loginHook;

	private boolean _enableChecks;

	private ConcurrentMap<String, LoginFailure> _failures = new ConcurrentHashMap<>();

    @Override
	public void init () throws ServletException {
        try {
			loginHook = getConfiguredLoginHook();
		} catch (Exception e) {
			// If an ConfigurationException is thrown, the servlet could not be
			// initialized and nobody can login. If in this case a
			// ServletException is thrown the browser just shows the login.jsp
			// again, without any informations. So an UnavailableException will
			// be thrown, to bring the browser to show a 503-error.
			Logger.error("Invalid configuration: " + e.getMessage(), e, this);
			throw new UnavailableException("Invalid configuration: " + e.getMessage());
		}
    }

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		String value = config.getInitParameter(DISABLE_SECURE_HEADER_CHECK);
		_enableChecks = !"true".equals(value);
	}

	/**
	 * The hook method being invoked during a login process.
	 * 
	 * @return The configured {@link LoginHook}.
	 * 
	 * @see Login.Config#getLoginHook() Configuration option.
	 */
	public static LoginHook getConfiguredLoginHook() {
		PolymorphicConfiguration<LoginHook> configuration = Login.getInstance().getConfiguration().getLoginHook();
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(configuration);
	}


   /**
	* doGet is just forwarded to {@link #doPost(HttpServletRequest, HttpServletResponse)} 
	*/
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    this.doPost(request, response);
	}

	/**
	 * {@link #checkRequest(HttpServletRequest, HttpServletResponse)} and care for the Environment.
	 */
	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response)
		throws IOException, ServletException {
		/* interpret content as UTF-8, independent of the default encoding of the container. The
		 * login.jsp delivers the content in UTF-8, so the content is expected to be in UTF-8. */
		request.setCharacterEncoding(StringServices.UTF8);

		if (_enableChecks) {
			String protection = response.getHeader(HttpSecureHeaderFilter.X_XSS_PROTECTION);
			if (protection == null || protection.isEmpty()) {
				response.sendRedirect(request.getContextPath() + "/jsp/display/error/NoXssProtection.jsp");
				return;
			}
		}

		if (DeferredBootUtil.redirectOnPendingBoot(request, response)) {
			return;
		}
		try {
			StopWatch watch = StopWatch.createStartedWatch();

			checkRequest(request, response);

			DebugHelper.logTiming(request, "Login user", watch, 100, LoginPageServlet.class);
		} catch (DispatchException ex) {
			Logger.fatal("DispatchException occured during checkRequest", ex, LoginPageServlet.class);
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Calls the AuthenticationService to check the login data provided with the incoming request.
	 * <p>
	 * <b>If the provided LoginData was invalid :</b><br/>
	 * Forwards the request to the LoginPage.jsp ...so the user can retry. If the max number of
	 * retries is exceeded, the request is forwarded to the LoginErrorPage, allowing no more
	 * retries.
	 * </p>
	 * <p>
	 * <b>If the provided login data was ok: </b><br/>
	 * Forwards to the page given as parameter "target" in the request. Throws an Dispatch exception
	 * if the target was given as string constant and could no be resolved to a page.
	 * </p>
	 * 
	 * @param request
	 *        The incoming request
	 * @param response
	 *        The outgoing response
	 * @throws DispatchException
	 *         when the target-parameter could not be resolved
	 * 
	 */
	protected void checkRequest(
		HttpServletRequest request,
		HttpServletResponse response)
		throws ServletException, IOException, DispatchException {

		boolean debug = Logger.isDebugEnabled(this);
		if (!checkSessionEnabled(request)) {
			this.forwardToTarget(ApplicationPages.getInstance().getNoCookiePage(), request, response);
			return;
		}

		boolean authenticated = false, deniedByMaintenanceMode = false, maxUsersExceeded = false;

		String userName = request.getParameter(Login.USER_NAME);
		if (userName == null || userName.isBlank()) {
			forwardToLoginRetry(request, response);
			return;
		}

		// Separate failed login counter per client address to prevent denial of service attacks
		// preventing a user from logging in (by iteratively trying to authenticate with wrong
		// credentials).
		String userKey = userName.toLowerCase() + '@' + request.getRemoteAddr();

		LoginFailure lastFailure = _failures.get(userKey);
		if (lastFailure != null) {
			if (!lastFailure.allowRetry()) {
				this.redirectToLoginError(request, response, lastFailure.retryTimeout());

				// Not authenticated.
				return;
			}
		}

		if (KnowledgeBaseFactory.Module.INSTANCE.isActive()) {
			try {
				// Will eventually reset the TLContext with a valid Person
				authenticated = Login.getInstance().login(userName, request, response);
			} catch (Login.InMaintenanceModeException e) {
				deniedByMaintenanceMode = true;
			} catch (Login.MaxUsersExceededException e) {
				maxUsersExceeded = true;
			}
		} else {
			authenticated = false;
			deniedByMaintenanceMode = true;
		}

        //special handling for requests submitted by the changePWD page
        boolean pwdUpdated=false;
		if (Boolean.parseBoolean(request.getParameter(CHANGE_PWD_PARAMETER))) {
            if(!authenticated){
				request.setAttribute(MESSAGE_ATTR, I18NConstants.PWD_CHANGE_MSG_OLD_PASSWORD_WRONG);
                this.forwardToChangePwdPage(request, response);
                return;
            }else{
				ResKey result = changePassword(request);
				if (result != null) {
                    request.setAttribute(MESSAGE_ATTR,result);
                    this.forwardToChangePwdPage(request, response);
                    return;
                }else{
                    pwdUpdated=true;
                }
            }
        }

		// if authentication ok so far, check if there is another (configured) reason to prevent
		// authentication
		ResKey anotherReason = null;
		if (authenticated) {
			anotherReason = this.checkRequestHook(request, response);
			if (anotherReason != null) {
				authenticated = false;
			}
		}

		// not authenticated :

		if (!authenticated) {
            if (deniedByMaintenanceMode) {
                request.setAttribute("errorMessage", Login.getI18NedMaintenanceMessage(userName));
			} else if (maxUsersExceeded) {
				request.setAttribute("errorMessage",
					Resources.getInstance().getString(I18NConstants.MAX_USERS_EXCEEDED));
			} else if (anotherReason != null) {
				request.setAttribute("errorMessage", Resources.getInstance().getString(anotherReason));
			} else {
				request.setAttribute("errorMessage",
					Resources.getInstance().getString(I18NConstants.ERROR_AUTHENTICATE) + " \"" + userName + "\"");
			}
                
			LoginFailure failure = _failures.get(userKey);
			if (failure != null && failure.timedOut()) {
				_failures.remove(userKey, failure);
				failure = null;
			}
			if (failure == null) {
				failure = new LoginFailure(userKey);
				failure = MapUtil.putIfAbsent(_failures, userKey, failure);
			}
			failure.incFailures();

			// Clean up outdated entries to prevent denial of service attacks by flooding
			// the server with nonsense authentication failures for random user names.
			for (Iterator<LoginFailure> it = _failures.values().iterator(); it.hasNext();) {
				if (it.next().timedOut()) {
					it.remove();
				}
			}

			long delay = failure.retryDelay();
			if (delay <= 0) {
				this.forwardToLoginRetry(request, response);
			} else {
				int failureCnt = failure.getFailureCnt();
				if (failureCnt > 5) {
					Logger.warn("Excessive login failures (" + failureCnt + ") for user '" + userKey
						+ "'.", LoginPageServlet.class);
				}

				// Otherwise, use another page, not allowing further retries
				this.redirectToLoginError(request, response, delay);
			}

            // Not authenticated
			return; 
		}

		if (debug) {
			Logger.debug("Authentication ok.", this);
		}

		// Clear any login failures.
		_failures.remove(userKey);

		// removing login counter from the request, because login was successful
		request.removeAttribute(SessionAttributeKeys.LOGIN_COUNT);

        // Create a new UserAgent object and store it in the session
        UserAgent.getUserAgent(request);

        //check if password okay or just updated
        if(pwdValidAndNotExpired(request) || pwdUpdated){
		// go on with the main page
		this.forwardToStartPage(request, response);
        }else { //pwd change neccessary
            this.forwardToChangePwdPage(request, response);
        }

	}

    /**
	 * Forwards the request to the LoginPageServlet; no other target possible.
	 * 
	 * @param timeout
	 *        The minimum time to wait before the next login retry is accepted.
	 */
	protected void redirectToLoginError(HttpServletRequest req, HttpServletResponse res, long timeout)
			throws IOException {
		URLPathBuilder url = createUrl(req, getLoginErrorPage(req));
		url.appendParameter("timeout", Long.toString(timeout));
		url.appendParameter("forward", createUrl(req, ApplicationPages.getInstance().getLoginRetryPage()).getURL());
		res.sendRedirect(url.getURL());
    }

	private String getLoginErrorPage(HttpServletRequest request) {
		String errorPage = request.getParameter(LoginPageServlet.PARAM_LOGIN_ERROR_PAGE);
		if (errorPage == null) {
			return ApplicationPages.getInstance().getLoginErrorPage();
		}
		return errorPage;
	}

	/**
     * Forward the request to the given page.
     *
     * @param        aRequest            The send request.
     * @param        aResponse           The send response.
     * @param        aPage               The page to be forwarded to.
     */
	protected void forwardPage(String aPage, HttpServletRequest aRequest, HttpServletResponse aResponse) {
		try {
			getServletContext().getRequestDispatcher(aPage).forward(aRequest, aResponse);
		} catch (ServletException exception) {
			throw new RuntimeException(exception);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
    }

    /**
     * Forwards the request to the LoginPageServlet; no other target possible.
     */
	protected void forwardToLogin(HttpServletRequest req, HttpServletResponse res) {
		this.forwardPage(ApplicationPages.getInstance().getLoginPage(), req, res);
    }


    /**
	 * Forwards the request to the LoginPageServlet; no other target possible.
	 */
	protected void forwardToLoginRetry(HttpServletRequest req, HttpServletResponse res) {
		this.forwardPage(getLoginRetryPage(req), req, res);
    }

	private String getLoginRetryPage(HttpServletRequest request) {
		String retryPage = request.getParameter(LoginPageServlet.PARAM_LOGIN_RETRY_PAGE);
		if (retryPage == null) {
			return ApplicationPages.getInstance().getLoginRetryPage();
		}
		return retryPage;
	}

	public void forwardToTarget(String target, HttpServletRequest req, HttpServletResponse res) {
		forwardPage(target, req, res);
    }

	/**
	 * false if a pwdChange is necessary, true otherwise
	 */
	private boolean pwdValidAndNotExpired(HttpServletRequest aRequest){
		String userName = aRequest.getParameter(Login.USER_NAME);
		try{
			Person account = Person.byName(userName);
			AuthenticationDevice device = account.getAuthenticationDevice();
			if (device == null) {
				// No password change possible, cannot request for a password update.
				return true;
			}

			if (!device.allowPwdChange()) {
				// No password change possible, cannot request for a password update.
				return true;
			}

			char[] password = aRequest.getParameter(Login.PASSWORD).toCharArray();
			return !device.isPasswordChangeRequested(account, password);
	    }catch(Exception e){
			Logger.error("Problem checking pwd validy for Person " + userName, e, this);
	    	return true; //do not spoil the login, though
	    }
	}

	/**
	 * Forwards to a page forcing the user to change his current password
	 */
	protected void forwardToChangePwdPage(HttpServletRequest request, HttpServletResponse response) {
	    try {
			this.forwardPage(ApplicationPages.getInstance().getChangePasswordPage(), request, response);
		}
        catch(Exception e) {
		    Logger.error("Unable to load changepwdpage",e,this);
		}
	}

	/**
	 * Designed to accept the request submitted by the changePwd.jsp (Change Password Page)
	 * Performs the actual pwd change
	 * 
	 * @return various error messages to be displayed
	 */
	private ResKey changePassword(HttpServletRequest aRequest) {
	    String username = aRequest.getParameter(Login.USER_NAME);
		String pwd1 = aRequest.getParameter(PASSWORD1_PARAMETER);
		String pwd2 = aRequest.getParameter(PASSWORD2_PARAMETER);
	    if(!pwd1.equals(pwd2)){
			return I18NConstants.PWD_CHANGE_MSG_PASSWORDS_DONT_MATCH;
	    }else{
			Person account = Person.byName(username);
			char[] newPassword = pwd1.toCharArray();
			AuthenticationDevice device = account.getAuthenticationDevice();

			try (Transaction tx = account.tHandle().getKnowledgeBase().beginTransaction()) {
				device.setPassword(account, newPassword);
				tx.commit();
				return null;
			} catch (TopLogicException ex) {
				return ex.getErrorKey();
			}
	    }
	}

	/**
	 * Invokes the configured additional login processing.
	 * 
	 * @see #getConfiguredLoginHook()
	 * 
	 * @return a reason To prevent login.
	 */
	protected final ResKey checkRequestHook(HttpServletRequest request, HttpServletResponse response)
			throws ServletException {
		if (loginHook != null){
			return loginHook.check(request, response);
		}
		return null;
	}

	/**
	 * The incoming request has a parameter called {@link #PARAM_START_PAGE} to indicate to where
	 * the request should be forwarded after successful login.
	 * 
	 * <p>
	 * This method forwards the request to the given target page or to the login page if no target
	 * was given.
	 * </p>
	 * 
	 * @throws ServletException
	 *         If forwarding fails.
	 */
	protected void forwardToStartPage (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String startPage  = request.getParameter(PARAM_START_PAGE);
		if (startPage == null) {
			startPage = ApplicationPages.getInstance().getStartPage();
		}

		sendRedirectToPage(startPage, request, response);
	}

	private void sendRedirectToPage(String page, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.sendRedirect(createUrl(request, page).toString());
	}

	private URLPathBuilder createUrl(HttpServletRequest request, String page) {
		// Construct URL to load after successful authentication.
		URLPathBuilder url = URLPathBuilder.newEmptyBuilder();
		url.appendRaw(request.getContextPath());
		url.appendRaw(page);

		appendCustomParameters(url, request);
		return url;
	}

	/**
	 * Appends custom parameters from the given request to the given URL buffer.
	 * 
	 * <p>
	 * Custom parameters are those that are not used in authenticating the request.
	 * </p>
	 * 
	 * @param url
	 *        The URL buffer.
	 * @param request
	 *        The request to take the parameters from.
	 */
	public static void appendCustomParameters(URLPathBuilder url, HttpServletRequest request) {
		for (Enumeration<String> it = request.getParameterNames(); it.hasMoreElements(); ) {
			String param = it.nextElement();
			if (isAuthenticationParameter(param)) {
				continue;
			}
			
			url.appendParameter(param, StringServices.nonNull(request.getParameter(param)));
		}
	}

	/**
	 * Utility to be called from the login JSP to forward all non-{@link #AUTHENTICATION_PARAMS}
	 * from a stable bookmark URL.
	 * 
	 * @param request
	 *        The {@link HttpServletRequest} to take parameters from.
	 * @param out
	 *        The {@link Writer} to write a form fragment to.
	 */
	public static void jspForwardParams(HttpServletRequest request, Writer out) throws IOException {
		// External untyped API.
		@SuppressWarnings("unchecked")
		Enumeration<String> parameterNames = request.getParameterNames();

		for (Enumeration<String> it = parameterNames; it.hasMoreElements();) {
			String param = it.nextElement();
			if (AUTHENTICATION_PARAMS.contains(param)) {
				continue;
			}

			String value = request.getParameter(param);

			TagUtil.beginBeginTag(out, INPUT);
			TagUtil.writeAttribute(out, TYPE_ATTR, HIDDEN_TYPE_VALUE);
			TagUtil.writeAttribute(out, NAME_ATTR, param);
			TagUtil.writeAttribute(out, VALUE_ATTR, value);
			TagUtil.endEmptyTag(out);
		}
	}

	/**
	 * Decide, whether the given {@link ServletRequest} parameter is internally used during
	 * authentication and should therefore not forwarded to the start page.
	 * 
	 * @param param
	 *        The parameter name to test.
	 * @return Whether the given parameter is a parameter that is used only during authentication.
	 */
	protected static boolean isAuthenticationParameter(String param) {
		return AUTHENTICATION_PARAMS.contains(param);
	}

	/**
	* If the client has cookies enabled, the
	* LoginPage of TopLogic creates an defaultcookie.
	* This method trys to read this cookie and returns true,
	* if it succeeds, false other wise
    * <p>
    * This method is only active when the request attribute
    * {@link
    * com.top_logic.base.accesscontrol.LoginPageServlet#ENABLE_SESSION_CHECK}
    * is on. When not active true is always returned.
    * </p>
	*
	* @param request of the clien to be checked
	* @return true if cookies enabled, false if not
	*/
	protected boolean checkSessionEnabled(HttpServletRequest request) {
        boolean result = true;

        boolean sessionCheckIsEnabled =
            request.getParameter (ENABLE_SESSION_CHECK) != null;

        if (sessionCheckIsEnabled) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object test = session.getAttribute(TEST_SESSION);
                result = (test != null && test instanceof Boolean);
                session.removeAttribute(TEST_SESSION);
            }
            else {
                result = false;
            }
        }

        return result;
	}

	private static class LoginFailure {

		private static final int MILLISECONDS = 1;

		private static final int SECONDS = 1000 * MILLISECONDS;

		private static final long MINUTES = 60 * SECONDS;

		/**
		 * Number of retries without delay.
		 */
		private static final int DIRECT_RETRY_CNT = 3;

		/**
		 * Initial delay after {@link #DIRECT_RETRY_CNT} failures.
		 */
		private static final double INITIAL_DELAY = 5 * SECONDS;

		/**
		 * The maximum delay.
		 */
		private static final long MAX_DELAY = 10 * MINUTES;

		private final String _key;

		private long _lastFailure;

		private int _cntFailures;

		/**
		 * Creates a {@link LoginFailure}.
		 *
		 * @param key
		 *        See {@link #getKey()}.
		 */
		public LoginFailure(String key) {
			_key = key;
		}

		/**
		 * The user name key.
		 */
		public String getKey() {
			return _key;
		}

		/**
		 * Whether this entry can be dropped since no further failed login occurred during an
		 * extended period of time.
		 */
		public boolean timedOut() {
			return now() > noRetryBefore() + MAX_DELAY;
		}

		/**
		 * The required wait time in milliseconds before the next login retry is accepted.
		 */
		public long retryDelay() {
			int failureCnt = getFailureCnt();
			if (failureCnt < DIRECT_RETRY_CNT) {
				return 0;
			}

			return Math.min(MAX_DELAY, (long) (INITIAL_DELAY * Math.pow(1.5, failureCnt - DIRECT_RETRY_CNT)));
		}

		/**
		 * Whether the required {@link #retryTimeout()} has been elapsed.
		 */
		public boolean allowRetry() {
			return now() >= noRetryBefore();
		}

		/**
		 * The timeout in milliseconds the user has to wait before the next login retry is accepted.
		 */
		public long retryTimeout() {
			return noRetryBefore() - now();
		}

		/**
		 * The absolute time in milliseconds since epoch when the next login retry can be performed.
		 */
		private synchronized long noRetryBefore() {
			return _lastFailure + retryDelay();
		}

		/**
		 * Adds another login failure.
		 */
		public synchronized void incFailures() {
			_cntFailures++;
			_lastFailure = now();
		}

		/**
		 * The current system time in milliseconds since epoch.
		 */
		private long now() {
			long now = System.currentTimeMillis();
			return now;
		}

		/**
		 * The number of failed logins without an intermediate successful login.
		 */
		public synchronized int getFailureCnt() {
			return _cntFailures;
		}
	}
}
