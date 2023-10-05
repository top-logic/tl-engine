/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.security.password.hashing.PasswordHashingService;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.knowledge.monitor.FailedLogin;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.QualifiedComponentNameConstraint;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;
import com.top_logic.util.Resources;


/**
 * Central point of login for all top-logic activities..
 *
 * @author    <a href="mailto:mer@top-logic.com">Michael Eriksson</a>
 */
@ServiceDependencies(CommandGroupRegistry.Module.class)
public class Login extends ConfiguredManagedClass<Login.Config> {

	/**
	 * Configuration options for {@link Login}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<Login> {

		/**
		 * Whether to log failed attempts to log in.
		 */
		@Name("log-failed-logins")
		boolean getLogFailedLogins();

		/**
		 * The {@link PasswordHashingService} to use.
		 */
		@ItemDefault
		@ImplementationClassDefault(PasswordHashingService.class)
		@Name("password-hashing")
		PolymorphicConfiguration<PasswordHashingService> getPasswordHashing();

		/**
		 * Component name that allows switching off maintenance mode.
		 * 
		 * <p>
		 * Users that are privileged on this component can login even if maintenance mode is
		 * enabled.
		 * </p>
		 * 
		 * @see #getCommandGroupLeavingMaintenanceMode()
		 */
		@Name("component-leaving-maintenance-mode")
		@Nullable
		@Constraint(QualifiedComponentNameConstraint.class)
		ComponentName getComponentLeavingMaintenanceMode();

		/**
		 * {@link SimpleBoundCommandGroup Name of command group} that is used to
		 * {@link #getComponentLeavingMaintenanceMode() switch off maintenance mode}.
		 */
		@Name("command-group-leaving-maintenance-mode")
		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		CommandGroupReference getCommandGroupLeavingMaintenanceMode();

		/**
		 * Configuration of a {@link LoginHook} which is instantiated to perform additional
		 *         checks during the login process. May be <code>null</code>.
		 */
		PolymorphicConfiguration<LoginHook> getLoginHook();

	}

	/**
	 * A {@link RuntimeException} indicating that the access was denied. This class is used if no
	 * error occurred. It should therefore not be treated as an error.
	 * 
	 * @see LoginFailedException If the access was not simply denied but the check failed with an
	 *      exception.
	 */
	public static class LoginDeniedException extends RuntimeException {

		/**
		 * @param message
		 *        Must not be <code>null</code>.
		 */
		public LoginDeniedException(String message) {
			super(message);
			assert message != null;
		}

		/**
		 * @param cause
		 *        Must not be <code>null</code> and must have a {@link Throwable#getMessage()
		 *        message} that is not <code>null</code>.
		 */
		public LoginDeniedException(Throwable cause) {
			super(causeMessage(cause), cause);
		}

		private static String causeMessage(Throwable cause) {
			assert cause != null;
			String result = cause.getMessage();
			assert result != null;
			return result;
		}

		/**
		 * @param message
		 *        Must not be <code>null</code>.
		 * @param cause
		 *        Must not be <code>null</code>.
		 */
		public LoginDeniedException(String message, Throwable cause) {
			super(message, cause);
			assert message != null;
			assert cause != null;
		}

	}

	/**
	 * A {@link RuntimeException} indicating that the login has failed because of an error. Use this
	 * if the access was not simply denied, but the check failed unexpected.
	 * 
	 * @see LoginDeniedException If the access was simply denied.
	 */
	public static class LoginFailedException extends RuntimeException {

		/**
		 * @param message
		 *        Must not be <code>null</code>.
		 */
		public LoginFailedException(String message) {
			super(message);
			assert message != null;
		}

		/**
		 * @param cause
		 *        Must not be <code>null</code> and must have a {@link Throwable#getMessage()
		 *        message} that is not <code>null</code>.
		 */
		public LoginFailedException(Throwable cause) {
			super(causeMessage(cause), cause);
		}

		private static String causeMessage(Throwable cause) {
			assert cause != null;
			String result = cause.getMessage();
			assert result != null;
			return result;
		}

		/**
		 * @param message
		 *        Must not be <code>null</code>.
		 * @param cause
		 *        Must not be <code>null</code>.
		 */
		public LoginFailedException(String message, Throwable cause) {
			super(message, cause);
			assert message != null;
			assert cause != null;
		}

	}

	/** Maximum number of Input accepted for login / password */
    public final static int MAXINPUT_LEN  = 256;

    /** Name of Request Attribute for username */
    public final static String USER_NAME = "username";

    /** Name of Request Attribute for password */
    public final static String PASSWORD  = "password";

    /**
	 * @see #getAllowedGroups()
	 */
	private Set<Group> allowedGroups = null;

	/** Flag indicating to log failed logins. */
	private boolean logFailedLogins = false;

	private final PasswordHashingService _passwordHashing;

	private final ComponentName _componentLeavingMaintenanceMode;

	private final BoundCommandGroup _commandGroupLeavingMaintenanceMode;

	/**
	 * Creates a {@link Login} from configuration.
	 */
	public Login(InstantiationContext context, Config config) {
		super(context, config);
		logFailedLogins = config.getLogFailedLogins();
		_passwordHashing = context.getInstance(config.getPasswordHashing());

		_componentLeavingMaintenanceMode = config.getComponentLeavingMaintenanceMode();
		CommandGroupReference ref = config.getCommandGroupLeavingMaintenanceMode();
		BoundCommandGroup leavingCommandGoup;
		if (ref == null) {
			leavingCommandGoup = null;
		} else {
			leavingCommandGoup = ref.resolve();
			if (leavingCommandGoup == null) {
				context.error("Unknown referenced command group: " + ref.id());
			}
		}
		_commandGroupLeavingMaintenanceMode = leavingCommandGoup;
	}

    /**
     * Delivers the description of the instance of this class.
     *
     * @return    The description for debugging.
     */
    @Override
	public String toString () {
        return this.getClass ().getName () + " [" +
        //                "name: " + this.name +
                        "]";
    }

    public boolean login(String userName, HttpServletRequest aRequest, HttpServletResponse response)
			throws InMaintenanceModeException, MaxUsersExceededException {
		char[] thePassword = StringServices.nonNull(aRequest.getParameter(PASSWORD)).toCharArray();

		if (StringServices.isEmpty(userName)) {
			// don't authenticate for empty UserName
			return noLogin(userName, aRequest, FailedLogin.REASON_NO_PERSON);
		}
		if (thePassword.length == 0) {
			// don't authenticate for empty Password
			return noLogin(userName, aRequest, FailedLogin.REASON_NO_PASSWORD);
		}
		if (userName.length() > MAXINPUT_LEN || thePassword.length > MAXINPUT_LEN) {
			String reason = null;
			if (userName.length() > MAXINPUT_LEN) {
				Logger.warn("User name too long (" + userName.length() + ") ignored", Login.class);
				reason = FailedLogin.REASON_PERSON_TOO_LONG;
			}
			if (thePassword.length > MAXINPUT_LEN) {
				Logger.warn("Password too long (" + thePassword.length + ") ignored", Login.class);
				reason = reason == null ? FailedLogin.REASON_PWD_TOO_LONG : FailedLogin.REASON_BOTH_TOO_LONG;
			}
			try {
				Thread.sleep(4000); // Sleep to defer the attacker
			} catch (InterruptedException ignored) { /* ignored */
			}
			return noLogin(userName, aRequest, reason);
		}
		Person thePerson = PersonManager.getManager().getPersonByName(userName);
		if (thePerson == null || !thePerson.isAlive()) {
			// no such person known to the system or person not longer alive
			return this.noLogin(userName, aRequest, FailedLogin.REASON_UNKNOWN_PERSON);
		}
		try (LoginCredentials login = LoginCredentials.fromUserAndPassword(thePerson, thePassword)) {
			return this.login(aRequest, response, login);
		}
	}

	/**
     * Attempt to login the specified user **after** successful NTLM Authentication.
     *
     * That means no further authentication is done here. This method just assumes
     * authentication was okay before it was called and now just creates a user object and a
     * new session for the given userid
     *
     * @param aRequest
     *        the request of the user; must not be null
	 * @param response
	 *        the current response
     * @param aUser
     *        the user logging in
     * @throws Exception
     *         if the system is in maintenance mode and the user is not member of one of the
     *         specified groups
     *
     * #author Thomas Richter
     */
	public void loginFromExternalAuth(HttpServletRequest aRequest, HttpServletResponse response, Person aUser)
			throws Exception {
        boolean debug = Logger.isDebugEnabled(this);
		checkAllowedGroups(aUser);
        if (debug) {
			Logger.debug("Get new Session for user " + aUser.getName(), this);
        }
        // always get a new session for the User...
        SessionService.getInstance().loginUser(aRequest, response, aUser);
    }

	/**
	 * Checks if the user can login. The login name and password are given in the
	 * {@link LoginCredentials}.
	 * 
	 * @throws InMaintenanceModeException
	 *         If the user is not allowed to login, because the system is in maintenance mode.
	 * @throws LoginDeniedException
	 *         If the user is not allowed to login because the given password is wrong.
	 * @throws LoginFailedException
	 *         If checking the password fails with an (unexpected) error.
	 */
	public void checkLoginByPassword(LoginCredentials credentials)
			throws InMaintenanceModeException, LoginDeniedException, LoginFailedException {
		boolean authenticated = getAuthenticationDevice(credentials).authentify(credentials);
		if (!authenticated) {
			throw new LoginDeniedException(FailedLogin.REASON_PWD_VALIDATION_FAILED);
		}
		checkAllowedGroups(credentials.getPerson());
	}

	private AuthenticationDevice getAuthenticationDevice(LoginCredentials credentials)
			throws LoginDeniedException, LoginFailedException {
		String theAuthenticationDeviceId = getAuthenticationDeviceId(credentials);
		if (StringServices.isEmpty(theAuthenticationDeviceId)) {
			// Person has no device to authenticate against
			throw new LoginDeniedException(FailedLogin.REASON_NO_AUTH_DEVICE);
		}
		AuthenticationDevice theDevice =
			TLSecurityDeviceManager.getInstance().getAuthenticationDevice(theAuthenticationDeviceId);
		if (theDevice == null) {
			String message = "Authentication device '" + theAuthenticationDeviceId + "' cannot be found.";
			throw new LoginFailedException(message);
		}
		return theDevice;
	}

	private String getAuthenticationDeviceId(LoginCredentials credentials) {
		return credentials.getPerson().getAuthenticationDeviceID();
	}

    /**
	 * Attempt to login the specified user.
	 *
	 * @param aRequest
	 *        the request of the user; must not be null
	 * @param response
	 *        the current response
	 * @param login
	 *        The user name and password information.
	 *
	 * @return true if successful, else false
	 * @throws InMaintenanceModeException
	 *         to indicate that login failed because of maintenance mode
	 *
	 *         #author Michael Eriksson #author Thomas Richter
	 */
	public boolean login(HttpServletRequest aRequest, HttpServletResponse response, LoginCredentials login)
			throws InMaintenanceModeException, MaxUsersExceededException {
		Person person = login.getPerson();
		String theAuthDevice = person.getAuthenticationDeviceID();
		if (StringServices.isEmpty(theAuthDevice)) {
			// Person has no device to authenticate against
			return noLogin(person, aRequest, FailedLogin.REASON_NO_AUTH_DEVICE);
		}
		try {
			AuthenticationDevice theDevice =
				TLSecurityDeviceManager.getInstance().getAuthenticationDevice(theAuthDevice);

			if (theDevice == null) {
				Logger.error("Authentication device '" + theAuthDevice + "' cannot be found.", this);
				return noLogin(person, aRequest, FailedLogin.REASON_AUTH_DEVICE_NOT_FOUND);
			}
			boolean authenticated = theDevice.authentify(login);
			if (authenticated) {
				checkAllowedGroups(person);
				HttpSession loginUser = SessionService.getInstance().loginUser(aRequest, response, person);
				if (loginUser == null) {
					noLogin(person, aRequest, FailedLogin.REASON_MAX_USERS_EXCEEDED);
					throw new MaxUsersExceededException(person);
				}
				return true;
			} else {
				return noLogin(person, aRequest, FailedLogin.REASON_PWD_VALIDATION_FAILED);
			}
		} catch (InMaintenanceModeException e) {
			noLogin(person, aRequest, FailedLogin.REASON_MAINTENANCE_MODE);
			throw e;
		} catch (MaxUsersExceededException e) {
			noLogin(person, aRequest, FailedLogin.REASON_MAX_USERS_EXCEEDED);
			throw e;
		} catch (Exception e) {
			Logger.error("Unable to authenticate person " + person.getName(), e, this);
			return noLogin(person, aRequest, FailedLogin.REASON_USER_NOT_VALID);
		}
	}

	private boolean noLogin(Person person, HttpServletRequest request, String reason) {
		return noLogin(person.getName(), request, reason);
	}

	private boolean noLogin(String userName, HttpServletRequest request, String reason) {
		if (logFailedLogins) {
			FailedLogin.storeNewFailedLogin(userName, SessionService.clientHost(request), reason);
		}
		return false;
	}

    /**
	 * If maintenance window is active, only users in specified groups are allowed to login. This
	 * method checks the groups of the person to login.
	 * 
	 * @param aPerson
	 *        the person to authenticate
	 * @throws InMaintenanceModeException
	 *         if the check failed
	 */
	public synchronized void checkAllowedGroups(Person aPerson) throws InMaintenanceModeException {
		{
			if (allowedGroups == null) {
				return;
			}
			if (Person.isAdmin(aPerson)) {
				// Administrator may always log in.
				return;
			}
			if (Group.isMemberOfAnyGroup(aPerson, allowedGroups)) {
				// User in dedicated maintenance mode group may log in.
				return;
			}
			if (canExitMaintenanceMode(aPerson)) {
				// Users that are allowed to disable maintenance mode must be allowed to log in to
				// do that.
				return;
			}

			// Throw exception instead of returning false to indicate reason of failure;
			throw new InMaintenanceModeException(aPerson);
		}
    }

	private boolean canExitMaintenanceMode(Person aPerson) {
		if (_componentLeavingMaintenanceMode == null) {
			return false;
		}
		
		PersBoundComp securityComponent = SecurityComponentCache.getSecurityComponent(_componentLeavingMaintenanceMode);
		if (securityComponent == null) {
			Logger.error("Invalid configuration: Security component for leaving maintenance mode not found: "
				+ _componentLeavingMaintenanceMode, Login.class);
			return false;
		}
		
		if (_commandGroupLeavingMaintenanceMode == null) {
			return false;
		}

		AccessManager accessManager = AccessManager.getInstance();
		BoundObject securityRoot = BoundHelper.getInstance().getDefaultObject();
		Set<BoundedRole> roles = securityComponent.rolesForCommandGroup(_commandGroupLeavingMaintenanceMode);
		return accessManager.hasRole(aPerson, securityRoot, roles);
	}


    /**
     * Get an instance to work with.
     *
     * @return the instance
     *
     * #author Michael Eriksson
     */
    public static Login getInstance () {
		return Module.INSTANCE.getImplementationInstance();
    }

	/**
	 * Returns the configuration of the {@link Login}.
	 */
	public Config getConfiguration() {
		return getConfig();
	}

	/**
	 * Returns the {@link PasswordHashingService} of the application.
	 */
	public PasswordHashingService getPasswordHashingService() {
		return _passwordHashing;
	}

    /**
	 * Only persons who are member of a group within this set are allowed to log in.
	 * 
	 * <p>
	 * May be <code>null</code> to indicate that all persons are allowed to log in, may be empty to
	 * indicate that no one are allowed to log in.
	 * </p>
	 * 
	 * @return a set of {@link Group}s, whose members are allowed to login only, or
	 *         <code>null</code> to allow each user to log-in.
	 */
    public synchronized Set getAllowedGroups() {
        return allowedGroups;
    }

    /**
	 * Sets the groups, whose members are allowed to login only
	 *
	 * @param allowedGroups
	 *        the Set, whose members are allowed to login only
	 * @see #allowedGroups
	 */
	public synchronized void setAllowedGroups(Set<Group> allowedGroups) {
        this.allowedGroups = allowedGroups;
    }


    /**
     * Gets a I18Ned error message as reason for the failed login.
     */
    public static String getI18NedMaintenanceMessage(String userName) {
        int currentState = MaintenanceWindowManager.getInstance().getMaintenanceModeState();
        if (currentState == MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE) {
			return Resources.getInstance().getString(I18NConstants.ERROR_AUTHENTICATE_MAINTENANCE_MODE_SOON.fill(userName));
        }
        else {
			return Resources.getInstance().getString(I18NConstants.ERROR_AUTHENTICATE_MAINTENANCE_MODE.fill(userName));
        }
    }


    /**
     * Exception to indicate that login failed because system is in maintenance mode.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    public static class InMaintenanceModeException extends Exception {

		/** The person that tried to login. If the person is not known, <code>null</code>. */
		private final Person person;

        /**
		 * Creates a new InMaintenanceModeException without a message.
		 * 
		 * @param person
		 *        The person that tried to login. If the person is not known, <code>null</code>.
		 */
		public InMaintenanceModeException(Person person) {
            super();
			this.person = person;
        }

        /**
         * Creates a new InMaintenanceModeException with the given message.
         *
         * @param aMessage
         *        the message of the Exception
         */
		public InMaintenanceModeException(Person person, String aMessage) {
            super(aMessage);
			this.person = person;
		}

		/** The person that tried to login. If the person is not known, <code>null</code>. */
		public Person getPerson() {
			return person;
        }

    }

	/**
	 * Exception to indicate that login failed because there are more users in system than the
	 * license allows.
	 *
	 * @author <a href=mailto:msi@top-logic.com>msi</a>
	 */
	public static class MaxUsersExceededException extends Exception {

		/** The person that tried to login. If the person is not known, <code>null</code>. */
		private final Person person;

		/**
		 * Creates a new {@link MaxUsersExceededException} without a message.
		 * 
		 * @param person
		 *        The person that tried to login. If the person is not known, <code>null</code>.
		 */
		public MaxUsersExceededException(Person person) {
            super();
			this.person = person;
        }

		/**
		 * Creates a new {@link MaxUsersExceededException} with the given message.
		 *
		 * @param aMessage
		 *        the message of the Exception
		 */
		public MaxUsersExceededException(Person person, String aMessage) {
            super(aMessage);
			this.person = person;
		}

		/**
		 * The person that tried to login. If the person is not known, <code>null</code>.
		 */
		public Person getPerson() {
			return person;
		}

	}

	/**
	 * Singleton reference for {@link Login} service.
	 */
	public static final class Module extends TypedRuntimeModule<Login> {

		/**
		 * Singleton {@link Login.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<Login> getImplementation() {
			return Login.class;
		}

	}

}
