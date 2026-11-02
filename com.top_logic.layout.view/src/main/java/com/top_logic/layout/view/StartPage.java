/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.basic.StringServices;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;

/**
 * The page a user is taken to when they enter the application without naming one.
 *
 * <p>
 * A page of the React UI is identified by the part of its URL after the window: the route the
 * routing participants of a view interpret, preceded by the view file of an entry point other than
 * the default view - so remembering a start page means remembering that URL. Entering the
 * application at a URL that does name a page asks for that page instead, and the start page then
 * does not apply: a link a user follows or a bookmark they open takes them where it points.
 * </p>
 *
 * <p>
 * Which {@code .view.xml} files an application offers is a different question, answered by its
 * configuration for everyone alike. A start page is one user's choice of where to begin among them.
 * </p>
 *
 * @see com.top_logic.layout.view.command.RememberStartPageAction
 */
public class StartPage {

	/**
	 * {@link PersonalConfiguration} key storing the URL of the page the user starts on.
	 */
	public static final String PERSONAL_START_PAGE_KEY = "react.startPage";

	/**
	 * The URL of the page the current user starts on, or {@code null} while they have chosen none.
	 */
	public static String get() {
		PersonalConfiguration config = PersonalConfiguration.getPersonalConfiguration();
		if (config == null) {
			return null;
		}
		Object stored = config.getJSONValue(PERSONAL_START_PAGE_KEY);
		return stored instanceof String && !StringServices.isEmpty((String) stored) ? (String) stored : null;
	}

	/**
	 * Remembers the given route as the current user's start page, or forgets the one they had when
	 * given nothing.
	 *
	 * @param route
	 *        The route to start on; {@code null} or empty forgets the stored one.
	 */
	public static void set(String route) {
		PersonalConfiguration config = PersonalConfiguration.getPersonalConfiguration();
		if (config == null) {
			return;
		}
		config.setJSONValue(PERSONAL_START_PAGE_KEY, StringServices.isEmpty(route) ? null : route);
		PersonalConfiguration.storePersonalConfiguration();
	}

}
