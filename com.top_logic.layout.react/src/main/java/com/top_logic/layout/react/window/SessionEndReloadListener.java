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
 * A session can lose its user without the browser doing anything: the maintenance mode logs out
 * everybody who may not stay, and an administrator can terminate a session. Neither invalidates the
 * HTTP session - {@link SessionService#invalidateSession(String) Invalidation of session using the
 * session ID} only drops it from the session table - so the page keeps showing the previous user
 * and their content, and the first interaction merely establishes a fresh anonymous session behind
 * the scenes and appears to do nothing at all.
 * </p>
 *
 * <p>
 * Reloading brings the browser back as the anonymous user, showing the login and whatever the
 * application announces to it. The classic UI reaches the same end differently, by checking on
 * every request whether the session is still known to the {@link SessionService}.
 * </p>
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
		ReactWindowRegistry.reloadWindowsOfSession(event.sessionID());
	}

}
