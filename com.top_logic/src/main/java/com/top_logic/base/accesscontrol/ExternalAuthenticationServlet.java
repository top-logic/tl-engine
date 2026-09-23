/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import java.io.IOException;
import java.util.Objects;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.base.accesscontrol.IdentityVerifications.Outcome;
import com.top_logic.base.accesscontrol.Login.InMaintenanceModeException;
import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.base.accesscontrol.Login.LoginFailedException;
import com.top_logic.base.accesscontrol.Login.UnknownAccountException;
import com.top_logic.base.accesscontrol.loginmethod.LoginMethod;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Logger;
import com.top_logic.basic.SessionContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.URLPathBuilder;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.DeferredBootUtil;
import com.top_logic.util.NoContextServlet;
import com.top_logic.util.Resources;

/**
 * Authenticate against external Systems (NTLN, GROPS, SiteMinder, LDAP, ...)
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class ExternalAuthenticationServlet extends NoContextServlet {

	/**
	 * This exception can be thrown in certain methods (subclass hooks), to indicate that the user
	 * has to be forwarded to the page specified by {@link ForwardRequiredException#getTarget()}.
	 * For example, to a Non Disclosure Agreement that any user of the system has to sign before the
	 * login is allowed.
	 */
	public static class ForwardRequiredException extends RuntimeException {

		private final String target;

		/**
		 * @param target
		 *        Must not be <code>null</code>.
		 */
		public ForwardRequiredException(String target) {
			if (target == null) {
				String message = "The target must not be null. It does not make sense to request a forward to 'null'!";
				throw new NullPointerException(message);
			}
			this.target = target;
		}

		/**
		 * Where the user has to be forwarded to.
		 */
		public String getTarget() {
			return target;
		}

	}

	/**
	 * This exception can be thrown in certain methods (subclass hooks), to indicate that the
	 * {@link ExternalAuthenticationServlet#checkRequest(HttpServletRequest, HttpServletResponse)}
	 * method should exit without any further processing.
	 */
	public static class BreakCheckRequestException extends RuntimeException {
		/* Nothing needed */
	}

	/**
	 * Name of the request parameter naming the {@link PendingIdentityVerification} that a request
	 * completes.
	 *
	 * <p>
	 * A request carrying it is not a login: it brings back the proof that the user of the running
	 * session has authenticated again, and the session it arrives in is the one it confirms. The
	 * token is issued by {@link IdentityVerifications#register(Person, Runnable)} and travels to the
	 * external authentication and back.
	 * </p>
	 *
	 * @see LoginMethod#getReauthenticationUrl(String)
	 */
	public static final String VERIFICATION_PARAM = "verification";

	/**
	 * Name of the query parameter under which the
	 * {@link ApplicationPages.Config#getUnknownAccountPage() unknown account page} receives the
	 * name that the external authentication system has authenticated.
	 *
	 * @see #redirectToUnknownAccountPage(String, HttpServletRequest, HttpServletResponse)
	 */
	public static final String LOGIN_NAME_PARAM = "login";

	private static final TypedAnnotatable.Property<UserTokens> TOKENS =
		TypedAnnotatable.property(UserTokens.class, "userTokens");

	private static final String XML_CONFIG_SECTION_NAME = "ExternalAuthentication";

	private static final String XML_KEY_REQUEST_HEADER = "HeaderKey";
	private static final String XML_KEY_EXTAUTH_ACTIVATE = "isEnabled";
	private static final String XML_KEY_REUSE_SESSION = "reuseSession";

	private boolean extAuthEnabled;
	private boolean reuseSession;

	/**
	 * Configure this calls via the {@link #XML_CONFIG_SECTION_NAME}.
	 */
	public ExternalAuthenticationServlet() {
		ExternalAuthentication cfg = ApplicationConfig.getInstance().getConfig(ExternalAuthentication.class);

		extAuthEnabled = cfg.getIsEnabled();
		reuseSession = cfg.getReuseSession();
	}

	/**
	 * {@link #doGet(HttpServletRequest, HttpServletResponse)} is just forwarded to
	 * {@link #doPost(HttpServletRequest, HttpServletResponse)}.
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

		if (DeferredBootUtil.redirectOnPendingBoot(request, response)) {
			return;
		}

		StopWatch watch = StopWatch.createStartedWatch();

		checkRequest(request, response);

		DebugHelper.logTiming(request, "Login user", watch, 100, ExternalAuthenticationServlet.class);
	}

	private final synchronized void checkRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			internalCheckRequest(request, response);
		} catch (BreakCheckRequestException exception) {
			Logger.debug("Breaking 'checkRequest(...)'.", ExternalAuthenticationServlet.class);
		} catch (ForwardRequiredException exception) {
			Logger.debug("Forwarding user to: " + exception.getTarget(), ExternalAuthenticationServlet.class);
			forwardToPage(exception.getTarget(), request, response);
		} catch (InMaintenanceModeException exception) {
			String userName = exception.getPerson() == null ? "null" : getLoginName(exception.getPerson());
			String message = "User " + userName + " tried to login while the system was in maintenance mode.";
			Logger.debug(message, ExternalAuthenticationServlet.class);
			request.setAttribute("errorMessage", Login.getI18NedMaintenanceMessage(userName));
			forwardToSSOLoginFailed(request, response);
		} catch (UnknownAccountException exception) {
			Logger.info("No account for externally authenticated user: " + exception.getMessage(),
				ExternalAuthenticationServlet.class);
			redirectToUnknownAccountPage(exception.getLoginName(), request, response);
		} catch (LoginDeniedException exception) {
			Logger.debug("Access denied: " + exception.getMessage(), exception, ExternalAuthenticationServlet.class);
			request.setAttribute("errorMessage", Resources.getInstance().getString(I18NConstants.ERROR_NTLM_AUTHEMTICATION_FAILED));
			forwardToSSOLoginFailed(request, response);
		} catch (LoginFailedException exception) {
			Logger.error("Login failed: " + exception.getMessage(), exception, ExternalAuthenticationServlet.class);
			request.setAttribute("errorMessage", Resources.getInstance().getString(I18NConstants.ERROR_NTLM_AUTHEMTICATION_FAILED));
			forwardToSSOLoginFailed(request, response);
		} catch (Throwable exception) {
			Logger.error("Problem during login: " + exception.getMessage(), exception,
				ExternalAuthenticationServlet.class);
			request.setAttribute("errorMessage", Resources.getInstance().getString(I18NConstants.ERROR_NTLM_AUTHEMTICATION_FAILED));
			forwardToSSOLoginFailed(request, response);
		}
	}

	private synchronized void internalCheckRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, InMaintenanceModeException,
			ForwardRequiredException, LoginDeniedException, LoginFailedException {
		if (!isExtAuthEnabled()) {
			String message =
				"Attempted to access via external authentication, but that is disabled in config. Authentication denied.";
			Logger.warn(message, ExternalAuthenticationServlet.class);
			throw new LoginDeniedException(message);
		}
		LoginCredentials credentials = retrieveLoginCredentials(request, response);
		String verificationToken = getVerificationToken(request);
		if (verificationToken != null) {
			completeIdentityVerification(verificationToken, credentials, request, response);
			return;
		}
		checkLoginCredentials(credentials, request, response);
		if (reuseSession) {
			HttpSession existingSession = SessionService.getInstance().getSession(request);
			if (existingSession != null && isAuthenticatedSession(existingSession)) {
				String message = "Reusing an existing session for user '" + credentials.getUsername() + "'.";
				Logger.debug(message, ExternalAuthenticationServlet.class);
				redirectToStartPage(request, response);
				return;
			} else {
				// Either no session at all, or only an anonymous one (e.g. the session the React view
				// layer boots for every browser). An anonymous session is not a real login, so it must
				// not suppress the external login - otherwise the user would stay anonymous.
				String message = "No authenticated session to reuse for user '" + credentials.getUsername()
						+ "', logging in.";
				Logger.debug(message, ExternalAuthenticationServlet.class);
			}
		}
		loginUser(credentials.getPerson(), request, response);
		redirectToStartPage(request, response);
	}

	/**
	 * The token of the {@link PendingIdentityVerification} that the given request completes, or
	 * <code>null</code> if the request is an ordinary login.
	 *
	 * <p>
	 * The single place that decides where the token is read from, so that an authentication
	 * mechanism which cannot carry it in {@link #VERIFICATION_PARAM} can put it elsewhere and say so
	 * here.
	 * </p>
	 */
	protected String getVerificationToken(HttpServletRequest request) {
		return StringServices.nonEmpty(request.getParameter(VERIFICATION_PARAM));
	}

	/**
	 * Completes the identity verification the given request brings the answer for and reports the
	 * result to the browser window it arrived in.
	 *
	 * <p>
	 * Deliberately none of the login steps: the session is already established and stays as it is.
	 * Logging in here would replace the HTTP session, which is right for a login and wrong for a
	 * confirmation - every window of the session would lose its ground.
	 * </p>
	 *
	 * @param token
	 *        The token from {@link #getVerificationToken(HttpServletRequest)}.
	 * @param credentials
	 *        What the external authentication produced, naming the account that just authenticated.
	 *
	 * @throws LoginDeniedException
	 *         If there is no authenticated session the confirmation could belong to.
	 */
	protected void completeIdentityVerification(String token, LoginCredentials credentials,
			HttpServletRequest request, HttpServletResponse response) throws IOException, LoginDeniedException {
		HttpSession existingSession = SessionService.getInstance().getSession(request);
		if (existingSession == null || !isAuthenticatedSession(existingSession)) {
			throw new LoginDeniedException(
				"Identity confirmation for user '" + credentials.getUsername() + "' without an authenticated session.");
		}
		Outcome outcome = IdentityVerifications.forCurrentSession().complete(token, credentials.getPerson());
		Logger.debug("Identity confirmation for user '" + credentials.getUsername() + "': " + outcome,
			ExternalAuthenticationServlet.class);
		writeVerificationResult(response, outcome);
	}

	/**
	 * Writes the page the window that carried the confirmation ends on.
	 *
	 * <p>
	 * A page of its own, with nothing to click and nowhere to go: the work the confirmation unblocks
	 * continues in the window that asked for it. The page tries to close itself, and reads as a
	 * complete answer where the browser refuses to close a window the user opened.
	 * </p>
	 */
	protected void writeVerificationResult(HttpServletResponse response, Outcome outcome) throws IOException {
		response.setCharacterEncoding(StringServices.UTF8);
		response.setContentType(HTMLConstants.CONTENT_TYPE_TEXT_HTML);

		Resources resources = Resources.getInstance();
		String title = resources.getString(I18NConstants.IDENTITY_VERIFICATION_TITLE);
		String message = resources.getString(verificationMessage(outcome));

		TagWriter out = new TagWriter(response.getWriter());
		out.writeContent(HTMLConstants.DOCTYPE_HTML);
		out.beginTag(HTMLConstants.HTML);
		{
			out.beginTag(HTMLConstants.HEAD);
			{
				out.beginBeginTag(HTMLConstants.META);
				out.writeAttribute(HTMLConstants.CHARSET_ATTR, StringServices.UTF8);
				out.endEmptyTag();

				out.beginTag(HTMLConstants.TITLE);
				out.writeText(title);
				out.endTag(HTMLConstants.TITLE);
			}
			out.endTag(HTMLConstants.HEAD);

			out.beginTag(HTMLConstants.BODY);
			{
				out.beginTag(HTMLConstants.H1);
				out.writeText(title);
				out.endTag(HTMLConstants.H1);

				out.beginTag(HTMLConstants.PARAGRAPH);
				out.writeText(message);
				out.endTag(HTMLConstants.PARAGRAPH);

				if (outcome == Outcome.VERIFIED) {
					out.beginScript();
					out.append("window.close();");
					out.endScript();
				}
			}
			out.endTag(HTMLConstants.BODY);
		}
		out.endTag(HTMLConstants.HTML);
		out.flush();
	}

	/**
	 * What the page written by
	 * {@link #writeVerificationResult(HttpServletResponse, Outcome) the result page} says about the
	 * given outcome.
	 */
	private static ResKey verificationMessage(Outcome outcome) {
		switch (outcome) {
			case VERIFIED:
				return I18NConstants.IDENTITY_VERIFIED;
			case MISMATCH:
				return I18NConstants.ERROR_IDENTITY_MISMATCH;
			case UNKNOWN:
				return I18NConstants.ERROR_IDENTITY_VERIFICATION_UNKNOWN;
		}
		throw new UnreachableAssertion("No such outcome: " + outcome);
	}

	/**
	 * Whether the given session belongs to an authenticated (non-anonymous) user.
	 */
	private static boolean isAuthenticatedSession(HttpSession session) {
		TLSessionContext context = SessionService.getInstance().getSession(session);
		if (context == null) {
			return false;
		}
		Person user = context.getOriginalUser();
		return user != null && !PersonManager.getManager().isAnonymous(user);
	}

	protected boolean isExtAuthEnabled() {
		return extAuthEnabled;
	}

	/**
	 * Forwards the configured login failed page for SSO logins
	 */
	protected void forwardToSSOLoginFailed(HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException {
		forwardToPage(ApplicationPages.getInstance().getLoginRetrySSOPage(), req, res);
	}

	/**
	 * Sends the user to the {@link ApplicationPages.Config#getUnknownAccountPage() page} that
	 * explains that this application has no account for them.
	 * 
	 * <p>
	 * A redirect, not a forward: the address the browser then shows is the page itself, not the URL
	 * through which the external authentication has delivered its answer, and the page can be
	 * reloaded without replaying the authentication.
	 * </p>
	 * 
	 * @param loginName
	 *        The name the external authentication system has authenticated. It is handed to the
	 *        page in the {@link #LOGIN_NAME_PARAM} parameter.
	 */
	protected void redirectToUnknownAccountPage(String loginName, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		URLPathBuilder url = URLPathBuilder.newEmptyBuilder();
		url.appendRaw(request.getContextPath());
		url.appendRaw(ApplicationPages.getInstance().getUnknownAccountPage());
		url.appendParameter(LOGIN_NAME_PARAM, StringServices.nonNull(loginName));

		response.sendRedirect(url.getURL());
	}

	/**
	 * Retrieves the {@link LoginCredentials} from the external authentication. The
	 * {@link LoginCredentials} can be checked in
	 * {@link #checkLoginCredentials(LoginCredentials, HttpServletRequest, HttpServletResponse)}.
	 * 
	 * @return Not <code>null</code>. Instead a {@link LoginFailedException} must be thrown.
	 * 
	 * @throws ForwardRequiredException
	 *         If retrieving the {@link LoginCredentials} reveals that the user has to be forwarded
	 *         to somewhere else, this method can throw an {@link ForwardRequiredException} and it
	 *         is taken care of the forwarding.
	 * @throws LoginDeniedException
	 *         If the login is denied.
	 * @throws LoginFailedException
	 *         If a login check fails with an error.
	 */
	protected abstract LoginCredentials retrieveLoginCredentials(HttpServletRequest request, HttpServletResponse response)
			throws ForwardRequiredException, LoginDeniedException, LoginFailedException;

	/**
	 * Hook for subclasses: After the {@link LoginCredentials} have been retrieved in
	 * {@link #retrieveLoginCredentials(HttpServletRequest, HttpServletResponse)}, they may need
	 * further checks. This is the place to check them.
	 * 
	 * @param credentials
	 *        {@link LoginCredentials} containing the {@link Person} to login.
	 * @param request
	 *        The incoming request.
	 * @param response
	 *        The outgoing response.
	 * 
	 * @throws InMaintenanceModeException
	 *         If the application is in maintenance mode and the user is not allowed to login
	 *         because of that.
	 * @throws ForwardRequiredException
	 *         If the checks reveal that the user has to be forwarded to somewhere else, this method
	 *         can throw an {@link ForwardRequiredException} and it is taken care of the forwarding.
	 * @throws LoginDeniedException
	 *         If the login is denied.
	 * @throws LoginFailedException
	 *         If a login check fails with an error.
	 */
	protected void checkLoginCredentials(LoginCredentials credentials, HttpServletRequest request,
			HttpServletResponse response)
			throws InMaintenanceModeException, ForwardRequiredException, LoginDeniedException, LoginFailedException {
		Login.getInstance().checkAllowedGroups(credentials.getPerson());
	}

	/**
	 * Login the given {@link Person}.
	 * @param response The current response.
	 * 
	 * @throws LoginFailedException
	 *         If an {@link Exception} is thrown, it is caught, annotated with further information
	 *         and rethrown wrapped in a {@link LoginFailedException}.
	 */
	protected void loginUser(Person person, HttpServletRequest request, HttpServletResponse response)
			throws InMaintenanceModeException {
		try {
			Login.getInstance().loginFromExternalAuth(request, response, person);
			String message = "The user with the loginName '" + getLoginName(person) + "' logged in successfully.";
			Logger.debug(message, ExternalAuthenticationServlet.class);
		} catch (InMaintenanceModeException exception) {
			throw exception;
		} catch (LoginDeniedException exception) {
			throw exception;
		} catch (LoginFailedException exception) {
			throw exception;
		} catch (Exception exception) {
			String message = "The user with the loginName '" + getLoginName(person)
				+ "' had passed all checks and was about to be logged in. But an error occurred!";
			throw new LoginFailedException(message, exception);
		}
	}

	/**
	 * Calls {@link Person#getFullName()}.
	 */
	protected static String getLoginName(Person person) {
		return person.getName();
	}

	/**
	 * Retrieves the {@link UserTokens} for the current session if the user has logged in via OIDC.
	 * 
	 * @return May be <code>null</code>, if the user has not logged in via OIDC.
	 */
	public static UserTokens userTokens() {
		SessionContext session = ThreadContextManager.getSession();
		if (session == null) {
			return null;
		}
		return session.get(TOKENS);
	}

	/**
	 * Installs the {@link UserTokens} for the current session.
	 */
	public void installUserTokens(UserTokens tokens) {
		SessionContext session = ThreadContextManager.getSession();
		if (session == null) {
			throw new IllegalStateException("No session available.");
		}
		session.set(TOKENS, Objects.requireNonNull(tokens));

	}

}