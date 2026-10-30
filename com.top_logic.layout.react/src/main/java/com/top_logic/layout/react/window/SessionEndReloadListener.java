/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.window;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.accesscontrol.SessionService.UserEventListener;
import com.top_logic.base.bus.UserEvent;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;

/**
 * Reloads the browser windows of a session that has been logged out.
 *
 * <p>
 * A session that is logged out while its HTTP session lives on would leave its pages showing the
 * previous user and their content; the first interaction would then merely be answered as stale.
 * Reloading brings the browser back as the anonymous user, showing the login and whatever the
 * application announces to it.
 * </p>
 *
 * <p>
 * Ending a session through the {@link SessionService} - an administrator terminating it, the
 * maintenance mode logging out everybody who may not stay, the account being deleted - invalidates
 * the HTTP session, which reloads and tears down the windows through the {@link ReactWindowRegistry}
 * itself. This listener covers a session that is only dropped from the session table while its
 * HTTP session stays alive, which is what happens to all sessions when the {@link SessionService}
 * service is shut down.
 * </p>
 *
 * <p>
 * A logout announced for a session that is still registered with the {@link SessionService} is no
 * session end: it ends the login record of the previous user when a user logs in within one of the
 * session's windows, while the session and its other windows continue. Its windows are therefore
 * not reloaded.
 * </p>
 *
 * @implNote A session is dropped from the session table without being invalidated by
 *           {@link SessionService#invalidateSession(String)}. A session that ends is dropped from
 *           the session table before its logout is announced, which tells it from a continuing
 *           one. A login within a window
 *           ({@link SessionService#loginUserInWindow(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, com.top_logic.knowledge.wrap.person.Person)})
 *           announces the logout while the session is still registered. Reloading the other windows
 *           then would be wrong twice: they keep their user, and a reload racing the cookie with the
 *           changed session ID would lose the session. Ending a session with
 *           {@link SessionService#terminateSession(String)} invalidates it and reaches
 *           {@link ReactWindowRegistry#valueUnbound(jakarta.servlet.http.HttpSessionBindingEvent)}.
 *
 * @see ReactWindowRegistry#reloadWindowsOfSession(String)
 */
public class SessionEndReloadListener extends AbstractConfiguredInstance<SessionEndReloadListener.Config>
		implements UserEventListener {

	/**
	 * Configuration for {@link SessionEndReloadListener}.
	 */
	public interface Config extends PolymorphicConfiguration<SessionEndReloadListener> {

		@Override
		@ClassDefault(SessionEndReloadListener.class)
		Class<? extends SessionEndReloadListener> getImplementationClass();

	}

	/**
	 * Creates a new {@link SessionEndReloadListener} from configuration.
	 */
	@CalledByReflection
	public SessionEndReloadListener(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void notifyUserEvent(UserEvent event) {
		if (event.type() != UserEvent.EventType.LOGGED_OUT) {
			return;
		}
		if (SessionService.getInstance().getUser(event.sessionID()) != null) {
			// The session continues, see the class comment.
			return;
		}
		ReactWindowRegistry.reloadWindowsOfSession(event.sessionID());
	}

}
