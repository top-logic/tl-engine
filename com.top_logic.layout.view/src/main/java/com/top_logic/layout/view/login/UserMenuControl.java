/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * React control showing the current user and offering login/logout.
 *
 * <p>
 * For the anonymous user it shows a "Login" action that opens the {@link LoginControl} in a modal
 * dialog; for an authenticated user it shows the user's name and a "Logout" action. The user's name
 * makes the active account observable in the view UI even where no access control hides content from
 * the anonymous user.
 * </p>
 */
public class UserMenuControl extends ReactControl {

	private static final String REACT_MODULE = "TLUserMenu";

	private static final String USER_NAME = "userName";

	private static final String LOGGED_IN = "loggedIn";

	private static final String LOGIN_LABEL = "loginLabel";

	private static final String LOGOUT_LABEL = "logoutLabel";

	/**
	 * Creates a new {@link UserMenuControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 */
	public UserMenuControl(ReactContext context) {
		super(context, null, REACT_MODULE);
		Resources resources = Resources.getInstance();
		boolean anonymous = TLContext.isAnonymous();
		putState(LOGGED_IN, Boolean.valueOf(!anonymous));
		putState(USER_NAME, currentUserName(anonymous, resources));
		putState(LOGIN_LABEL, resources.getString(I18NConstants.LOGIN_ACTION));
		putState(LOGOUT_LABEL, resources.getString(I18NConstants.LOGOUT_ACTION));
	}

	private static String currentUserName(boolean anonymous, Resources resources) {
		if (anonymous) {
			return resources.getString(I18NConstants.NOT_LOGGED_IN);
		}
		Person user = TLContext.currentUser();
		if (user == null) {
			return resources.getString(I18NConstants.NOT_LOGGED_IN);
		}
		return user.getFullName();
	}

	/**
	 * Opens the login dialog for the anonymous user.
	 */
	@ReactCommand("openLogin")
	HandlerResult handleOpenLogin() {
		ReactContext context = getReactContext();
		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager == null) {
			return HandlerResult.DEFAULT_RESULT;
		}
		LoginControl login = new LoginControl(context);
		dialogManager.openDialog(true, login, result -> {
			// Nothing to do: a successful login reloads the page.
		});
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Switches the session back to the anonymous user.
	 */
	@ReactCommand("logout")
	HandlerResult handleLogout() {
		DisplayContext context = DefaultDisplayContext.getDisplayContext();
		PendingSessionAction.requestLogout(context.asRequest().getSession());
		getReactContext().getSSEQueue().enqueue(JSSnipplet.create().setCode("window.location.reload();"));
		return HandlerResult.DEFAULT_RESULT;
	}

}
