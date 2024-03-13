/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.encryption.pbe;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.DBOperation;
import com.top_logic.basic.version.Version;
import com.top_logic.util.DeferredBootUtil;
import com.top_logic.util.TLContext;

/**
 * JSP model class for the initial application setup.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ApplicationSetup {

	private enum Phase {
		KEY_INPUT, PASSWORD_INPUT, FINISHED
	}

	private final ServletContext _application;

	private final HttpServletRequest _request;

	private final HttpServletResponse _response;

	private Phase _phase;

	private String _applicationKey = "";

	private String _newPassword = "";

	private String _passwordTwice = "";

	private boolean _errorInvalidKey;

	private boolean _errorPasswordMissmatch;

	private boolean _errorInvalidPassword;

	private boolean _errorInternal;

	private boolean _isSetup;

	private Map<String, Account> _accounts;

	/**
	 * Creates a {@link ApplicationSetup}.
	 */
	@CalledFromJSP
	public ApplicationSetup(ServletContext application, HttpServletRequest request, HttpServletResponse response) {
		_application = application;
		_request = request;
		_response = response;

		try {
			process();
		} catch (InvalidPasswordException ex) {
			Logger.info("Invalid application password.", ApplicationSetup.class);
			_errorInvalidPassword = true;
		} catch (SQLException ex) {
			Logger.error("Database error: " + ex.getMessage(), ApplicationSetup.class);
			_errorInternal = true;
		} catch (ModuleException ex) {
			Logger.error("Module error: " + ex.getMessage(), ApplicationSetup.class);
			_errorInternal = true;
		} catch (Exception ex) {
			Logger.error("Internal error: " + ex.getMessage(), ApplicationSetup.class);
			_errorInternal = true;
		}
	}

	private void process() throws Exception {
		if (!DeferredBootUtil.isBootPending()) {
			redirect("");
			phase(Phase.FINISHED);
			return;
		}

		_isSetup = !ApplicationPasswordUtil.hasPassword();

		phase(Phase.KEY_INPUT);
		_applicationKey = param("key");

		if (StringServices.isEmpty(_applicationKey)) {
			return;
		}
		
		if (!ApplicationPasswordUtil.checkApplicationKey(_applicationKey)) {
			_errorInvalidKey = true;
			return;
		}

		_newPassword = param("newPassword");
		_passwordTwice = param("passwordTwice");
		phase(Phase.PASSWORD_INPUT);

		if (StringServices.isEmpty(_newPassword)) {
			return;
		}
		
		if (!_newPassword.equals(_passwordTwice)) {
			_errorPasswordMissmatch = true;
			return;
		}
		
		if (!_isSetup) {
			String oldPassword = param("oldPassword");
			ApplicationPasswordUtil.changePassword(oldPassword, _newPassword);

			redirect("/applicationPassword.jsp");
			phase(Phase.FINISHED);
			return;
		}

		TLContext.inSystemContext(ApplicationSetup.class, new DBOperation() {
			@Override
			protected void update(PooledConnection connection) throws ModuleException, InvalidPasswordException, SQLException {
				{
					installKey(connection);
				}
			}

			private void installKey(PooledConnection connection) throws ModuleException, InvalidPasswordException,
					SQLException {
				ApplicationPasswordUtil.setupPassword(connection, getNewPassword());

				// Starting the encryption service requires the encryption key to be set up.
				connection.commit();
			}
		}).reportProblem();

		redirect("/applicationPassword.jsp");
		phase(Phase.FINISHED);
	}

	private void redirect(String location) throws IOException {
		_response.sendRedirect(_request.getContextPath() + location);
	}

	private String param(String parameter) {
		return StringServices.nonNull(_request.getParameter(parameter));
	}

	/**
	 * Information about an account during initial setup.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class Account {

		private final String _id;

		private String _password = "";

		private String _passwordTwice = "";

		/**
		 * Creates a {@link Account}.
		 * 
		 * @param id
		 *        See {@link #getId()}.
		 */
		Account(String id) {
			_id = id;
		}

		/**
		 * The login ID.
		 */
		@CalledFromJSP
		public String getId() {
			return _id;
		}

		/**
		 * Whether a {@link #getPassword()} was provided.
		 */
		boolean hasPassword() {
			return !StringServices.isEmpty(_password);
		}

		/**
		 * Whether {@link #getPassword()} and {@link #getPasswordTwice()} match.
		 */
		boolean hasMatchingPasswords() {
			return StringServices.equals(_password, _passwordTwice);
		}

		/**
		 * The entered initial password.
		 */
		String getPassword() {
			return _password;
		}

		/** @see #getPassword() */
		void setPassword(String password) {
			_password = password;
		}

		/**
		 * The retyped {@link #getPassword()}.
		 */
		String getPasswordTwice() {
			return _passwordTwice;
		}

		/** @see #getPasswordTwice() */
		void setPasswordTwice(String passwordTwice) {
			_passwordTwice = passwordTwice;
		}

	}

	/**
	 * The name of the application being set up.
	 */
	@CalledFromJSP
	public String getApplicationName() {
		return Version.getApplicationName();
	}

	/**
	 * The entered application key.
	 */
	@CalledFromJSP
	public String getApplicationKey() {
		return _applicationKey;
	}

	/**
	 * The application passphrase.
	 */
	@CalledFromJSP
	public String getNewPassword() {
		return _newPassword;
	}

	/**
	 * The retyped application passphrase.
	 */
	@CalledFromJSP
	public String getPasswordTwice() {
		return _passwordTwice;
	}

	/**
	 * The {@link Account} with the given name.
	 */
	Account getAccount(String userName) {
		return _accounts.get(userName);
	}

	/**
	 * All initial accounts.
	 */
	@CalledFromJSP
	public Collection<Account> getAccounts() {
		return _accounts.values();
	}

	/**
	 * Whether the setup has finished.
	 */
	@CalledFromJSP
	public boolean phaseFinished() {
		return phase() == Phase.FINISHED;
	}

	/**
	 * Whether the setup requests the application key.
	 */
	@CalledFromJSP
	public boolean phaseKeyInput() {
		return phase() == Phase.KEY_INPUT;
	}

	/**
	 * Whether the setup requests the application passphrase.
	 */
	@CalledFromJSP
	public boolean phasePasswordInput() {
		return phase() == Phase.PASSWORD_INPUT;
	}

	/**
	 * Whether this is the initial setup. <code>false</code> for changing the application
	 * passphrase.
	 */
	@CalledFromJSP
	public boolean isSetup() {
		return _isSetup;
	}

	/**
	 * Whether an internal error has occurred.
	 */
	@CalledFromJSP
	public boolean errorInternal() {
		return _errorInternal;
	}

	/**
	 * Whether the application key is invalid.
	 */
	@CalledFromJSP
	public boolean errorInvalidKey() {
		return _errorInvalidKey;
	}

	/**
	 * Whether the application passphrase is invalid.
	 */
	@CalledFromJSP
	public boolean errorInvalidPassword() {
		return _errorInvalidPassword;
	}

	/**
	 * Whether the both application passphrases do not match.
	 */
	@CalledFromJSP
	public boolean errorPasswordMissmatch() {
		return _errorPasswordMissmatch;
	}

	private void phase(Phase phase) {
		_phase = phase;
	}

	private Phase phase() {
		return _phase;
	}

}
