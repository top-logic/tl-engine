/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.login.LoginMethod;
import com.top_logic.base.accesscontrol.login.LoginMethods;
import com.top_logic.basic.Logger;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * React control rendering the login dialog body.
 *
 * <p>
 * Shows the built-in username/password form and, if any are contributed via
 * {@link LoginMethods#all()}, additional external login actions (e.g. "Login with Google"). The
 * built-in form submits the {@code login} command; external methods are plain browser redirects
 * handled on the client.
 * </p>
 *
 * <p>
 * On successful credential check, the actual session swap is deferred to the reload via
 * {@link PendingSessionAction} (see its documentation for the rationale).
 * </p>
 */
public class LoginControl extends ReactControl {

	private static final String REACT_MODULE = "TLLogin";

	private static final String STEP = "step";

	private static final String TITLE = "title";

	private static final String USERNAME_LABEL = "usernameLabel";

	private static final String PASSWORD_LABEL = "passwordLabel";

	private static final String LOGIN_LABEL = "loginLabel";

	private static final String ERROR_MESSAGE = "errorMessage";

	private static final String LOGIN_METHODS = "loginMethods";

	/** Initial step: collect username and password. */
	private static final String STEP_CREDENTIALS = "credentials";

	/**
	 * Creates a new {@link LoginControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 */
	public LoginControl(ReactContext context) {
		super(context, null, REACT_MODULE);
		Resources resources = Resources.getInstance();
		putState(STEP, STEP_CREDENTIALS);
		putState(TITLE, resources.getString(I18NConstants.LOGIN_TITLE));
		putState(USERNAME_LABEL, resources.getString(I18NConstants.LOGIN_USERNAME));
		putState(PASSWORD_LABEL, resources.getString(I18NConstants.LOGIN_PASSWORD));
		putState(LOGIN_LABEL, resources.getString(I18NConstants.LOGIN_SUBMIT));
		putState(LOGIN_METHODS, buildLoginMethods(context));
	}

	private List<Object> buildLoginMethods(ReactContext context) {
		List<LoginMethod> methods = LoginMethods.all();
		if (methods.isEmpty()) {
			return new ArrayList<>();
		}
		String returnTo = context.getContextPath() + "/view/" + context.getWindowName() + "/";
		Resources resources = Resources.getInstance();
		List<Object> result = new ArrayList<>(methods.size());
		for (LoginMethod method : methods) {
			Map<String, Object> descriptor = new LinkedHashMap<>();
			descriptor.put("id", method.getId());
			descriptor.put("label", resources.getString(method.getLabel()));
			descriptor.put("url", method.getInitiationUrl(returnTo));
			result.add(descriptor);
		}
		return result;
	}

	/**
	 * Handles a credential login attempt from the client.
	 */
	@ReactCommand("login")
	HandlerResult handleLogin(Map<String, Object> arguments) {
		String userName = stringArg(arguments, "username");
		String password = stringArg(arguments, "password");
		if (isEmpty(userName) || isEmpty(password)) {
			showError(I18NConstants.LOGIN_MISSING_CREDENTIALS);
			return HandlerResult.DEFAULT_RESULT;
		}

		DisplayContext context = DefaultDisplayContext.getDisplayContext();
		char[] decryptedPassword = password.toCharArray();
		boolean success;
		try {
			success = Login.getInstance()
				.checkUserPassword(userName, decryptedPassword, context.asRequest(), context.asResponse());
		} catch (Exception ex) {
			Logger.info("Login attempt for '" + userName + "' rejected: " + ex.getMessage(), LoginControl.class);
			showError(I18NConstants.LOGIN_FAILED);
			return HandlerResult.DEFAULT_RESULT;
		} finally {
			Arrays.fill(decryptedPassword, (char) 0);
		}

		if (!success) {
			showError(I18NConstants.LOGIN_FAILED);
			return HandlerResult.DEFAULT_RESULT;
		}

		// Defer the session swap to the reload request handled by ViewServlet.
		PendingSessionAction.requestLogin(context.asRequest().getSession(), userName);
		triggerReload();
		return HandlerResult.DEFAULT_RESULT;
	}

	private void showError(com.top_logic.basic.util.ResKey messageKey) {
		putState(ERROR_MESSAGE, Resources.getInstance().getString(messageKey));
	}

	private void triggerReload() {
		getReactContext().getSSEQueue().enqueue(JSSnipplet.create().setCode("window.location.reload();"));
	}

	private static String stringArg(Map<String, Object> arguments, String key) {
		Object value = arguments.get(key);
		return value == null ? null : value.toString();
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

}
