/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * The free text an account has written about how it wants to be served.
 *
 * <p>
 * The text is an attribute of the account itself ({@code tl.accounts:Person#aiPreferences}), so it
 * follows the user through every agent, every chat and every headless run they trigger. It is
 * rendered into the environment block below the application's own facts, and the block says which
 * of the two wins - a preference is a wish, not a statement about the situation.
 * </p>
 *
 * @see EnvironmentConfig#getUserPreferences()
 * @see EnvironmentConfig#getMaxPreferencesLength()
 */
public class UserPreferences {

	/**
	 * Name of the account attribute holding the text.
	 *
	 * <p>
	 * Declared in {@code WEB-INF/model/tl-ai-core/extension.tl.accounts.model.xml}; read by name
	 * rather than through a generated wrapper because the type belongs to the engine.
	 * </p>
	 */
	public static final String ATTRIBUTE = "aiPreferences";

	private UserPreferences() {
		// Static utility.
	}

	/**
	 * The preference text of the given account, or {@code null} when there is none to send.
	 *
	 * <p>
	 * {@code null} is the answer for an account that wrote nothing, for an installation whose
	 * administrator switched the feature off, and for a model that does not declare the attribute -
	 * an application that leaves out this module's account extension simply has no preferences.
	 * </p>
	 */
	public static String of(Person account) {
		if (account == null || !EnvironmentSettings.userPreferencesEnabled()) {
			return null;
		}
		String text = read(account);
		if (text == null) {
			return null;
		}
		String trimmed = text.strip();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static String read(Person account) {
		try {
			Object value = account.tValueByName(ATTRIBUTE);
			return value == null ? null : value.toString();
		} catch (RuntimeException ex) {
			// A model without the account extension: the feature is simply not installed here. Worth a
			// line in the log, never worth failing the run the user is waiting for.
			Logger.debug("No '" + ATTRIBUTE + "' attribute on the account model; "
				+ "the environment block carries no user preferences.", ex, UserPreferences.class);
			return null;
		}
	}
}
