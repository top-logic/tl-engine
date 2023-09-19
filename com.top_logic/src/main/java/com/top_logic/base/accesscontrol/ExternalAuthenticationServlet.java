/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.top_logic.base.accesscontrol.Login.InMaintenanceModeException;
import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.base.accesscontrol.Login.LoginFailedException;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.PersonDataAccessDevice;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.util.Utils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.DispatchException;
import com.top_logic.util.Resources;

/**
 * Authenticate against external Systems (NTLN, GROPS, SiteMinder, LDAP, ...)
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class ExternalAuthenticationServlet extends LoginPageServlet {

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

	private static final String XML_CONFIG_SECTION_NAME = "ExternalAuthentication";

	private static final String XML_KEY_REQUEST_HEADER = "HeaderKey";
	private static final String XML_KEY_EXTAUTH_ACTIVATE = "isEnabled";
	private static final String XML_KEY_ALLOW_NO_DOMAIN = "allowNoDomain";
	private static final String XML_KEY_REUSE_SESSION = "reuseSession";

	private String req_header_name;
	private boolean extAuthEnabled;
	private boolean allowAuthWithoutDomain;
	private boolean reuseSession;

	/**
	 * Configure this calls via the {@link #XML_CONFIG_SECTION_NAME}.
	 */
	public ExternalAuthenticationServlet() {
		ExternalAuthentication cfg = ApplicationConfig.getInstance().getConfig(ExternalAuthentication.class);

		req_header_name = cfg.getHeaderKey();
		extAuthEnabled = cfg.getIsEnabled();
		allowAuthWithoutDomain = cfg.getAllowNoDomain();
		reuseSession = cfg.getReuseSession();
	}

	@Override
	protected final synchronized void checkRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, DispatchException {
		try {
			internalCheckRequest(request, response);
		} catch (BreakCheckRequestException exception) {
			Logger.debug("Breaking 'checkRequest(...)'.", ExternalAuthenticationServlet.class);
		} catch (ForwardRequiredException exception) {
			Logger.debug("Forwarding user to: " + exception.getTarget(), ExternalAuthenticationServlet.class);
			forwardPage(exception.getTarget(), request, response);
		} catch (InMaintenanceModeException exception) {
			String userName = exception.getPerson() == null ? "null" : getLoginName(exception.getPerson());
			String message = "User " + userName + " tried to login while the system was in maintenance mode.";
			Logger.debug(message, ExternalAuthenticationServlet.class);
			request.setAttribute("errorMessage", Login.getI18NedMaintenanceMessage(userName));
			forwardToSSOLoginFailed(request, response);
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
		try (LoginCredentials credentials = retrieveLoginCredentials(request, response)) {
			checkLoginCredentials(credentials, request);
			if (reuseSession) {
				HttpSession existingSession = SessionService.getInstance().getSession(request);
				if (existingSession != null) {
					String message = "Reusing an existing session for user '" + credentials.getUsername() + "'.";
					Logger.debug(message, ExternalAuthenticationServlet.class);
					forwardToStartPage(request, response);
					return;
				} else {
					String message = "No existing session found for user '" + credentials.getUsername()
						+ "', creating a new one.";
					Logger.debug(message, ExternalAuthenticationServlet.class);
				}
			}
			loginUser(credentials.getPerson(), request, response);
		}
		forwardToStartPage(request, response);
	}

	/** @see #checkUser(String, String) */
	protected void checkUser(LoginCredentials credentials) throws LoginDeniedException {
		checkUser(credentials.getDomainName(), credentials.getUsername());
	}

	/**
	 * Checks, whether the given user is a valid user.
	 * 
	 * <p>
	 * Note: Definition for being a valid TopLogic user is:
	 * </p>
	 * 
	 * <p>
	 * The given userID has to be known by TopLogic and has to be unique. That means TopLogic has to
	 * find a user with the given ID in exactly ONE of its security devices.
	 * </p>
	 * 
	 * <p>
	 * The device containing the user has to support single sign on (part of device config)
	 * </p>
	 * 
	 * <p>
	 * The domainName configured for this device MUST BE the same as the one provided by the
	 * request, to make sure the useriD authenticated up front by the external System really belongs
	 * to the user we know in TopLogic.
	 * </p>
	 * 
	 * @param domainName
	 *        The domainName this user belongs to as provided by the request
	 * @param userName
	 *        Check, if the given user is a valid TopLogic user known in exactly one security
	 *        system, may be <code>null</code>.
	 * @throws LoginDeniedException
	 *         If the login is not valid.
	 */
	protected void checkUser(String domainName, String userName) throws LoginDeniedException {
		PersonDataAccessDevice accessDevice = getAccessDevice(domainName, userName);
		if (accessDevice == null) {
			throw new LoginDeniedException("No unique data access device for user '" + userName + "'.");
		}

		String deviceId = accessDevice.getDeviceID();
		if (!accessDevice.allowExternalAuthentication()) {
			throw new LoginDeniedException(
				"No single singn-on allowed for access device '" + deviceId + "'.");
		}

		if (!StringServices.isEmpty(domainName)) {
			String deviceDomainName = accessDevice.getDomainName();
			if (!domainName.equalsIgnoreCase(deviceDomainName)) {
				throw new LoginDeniedException(
					"Domain name missmatch, expected '" + deviceDomainName + "', got '" + domainName + "'.");
			}
		} else {
			if (!allowAuthWithoutDomain) {
				throw new LoginDeniedException("Missing domain in login credentials.");
			}
		}
	
		Person user = PersonManager.getManager().getPersonByName(userName);
		if (user == null) {
			throw new LoginDeniedException("User '" + userName + "' does not exist.");
		} else if (user.isAlive()) {
			try {
				String personDeviceId = user.getDataAccessDeviceID();
				if (!personDeviceId.equals(deviceId)) {
					throw new LoginDeniedException("Missmatch of access device IDs, expected '" + personDeviceId
						+ "', got '" + deviceId + "'.");
				}
			} catch (Exception e) {
				Logger.error("unable to get device Id from person", e, ExternalAuthenticationServlet.class);
			}
		} else {
			throw new LoginDeniedException("User '" + userName + "' is not alive.");
		}
	    
		Logger.debug(
			"Authenticated user '" + userName + "' using device '" + deviceId + "'.",
			ExternalAuthenticationServlet.class);
	}

	/**
	 * The {@link PersonDataAccessDevice} linked to the currently executing authentication method.
	 */
	protected PersonDataAccessDevice getAccessDevice(String domain, String userName) {
		if (StringServices.isEmpty(userName)) {
			Logger.debug("No user name given.", ExternalAuthenticationServlet.class);
			return null;
		}

		if (domain == null) {
			Logger.debug("No domain name given when authenticating '" + userName + "'.",
				ExternalAuthenticationServlet.class);
		}

		PersonDataAccessDevice uniqueDevice = null;
		TLSecurityDeviceManager devices = TLSecurityDeviceManager.getInstance();
		for (String id : devices.getConfiguredDataAccessDeviceIDs()) {
			PersonDataAccessDevice device = devices.getDataAccessDevice(id);
			if (!allowAuthWithoutDomain && !Utils.equals(domain, device.getDomainName())) {
				continue;
			}

			if (!device.userExists(userName)) {
				continue;
			}

			if (uniqueDevice != null) {
				Logger.warn("Ambiguous security device for user '" + userName + "': " + uniqueDevice + ", " + device,
					ExternalAuthenticationServlet.class);
				return null;
			}

			uniqueDevice = device;
		}

		if (uniqueDevice == null) {
			Logger.debug("No access device found for user '" + userName + "'.", ExternalAuthenticationServlet.class);
		}
		return uniqueDevice;
	}

	protected String getRequestHeaderName() {
		return req_header_name;
	}

	protected boolean isExtAuthEnabled() {
		return extAuthEnabled;
	}

	protected boolean isAllowAuthWithoutDomain() {
		return allowAuthWithoutDomain;
	}

	/**
	 * Forwards the configured login failed page for SSO logins
	 */
	protected void forwardToSSOLoginFailed(HttpServletRequest req, HttpServletResponse res) {
		forwardToTarget(ApplicationPages.getInstance().getLoginRetrySSOPage(), req, res);
	}

	/**
	 * Retrieves the {@link LoginCredentials} from the external authentication. The
	 * {@link LoginCredentials} can be checked in
	 * {@link #checkLoginCredentials(LoginCredentials, HttpServletRequest)}.
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
	 * {@link #retrieveLoginCredentials(HttpServletRequest, HttpServletResponse)}, they may need further checks. This is
	 * the place to check them.
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
	protected void checkLoginCredentials(LoginCredentials credentials, HttpServletRequest request)
			throws InMaintenanceModeException, ForwardRequiredException, LoginDeniedException, LoginFailedException {
		checkUser(credentials);
	}

	/**
	 * Login the given {@link Person}.
	 * @param response The current response.
	 * 
	 * @throws LoginFailedException
	 *         If an {@link Exception} is thrown, it is caught, annotated with further information
	 *         and rethrown wrapped in a {@link LoginFailedException}.
	 */
	private void loginUser(Person person, HttpServletRequest request, HttpServletResponse response) throws InMaintenanceModeException {
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
		return Person.getUser(person).getUserName();
	}

}