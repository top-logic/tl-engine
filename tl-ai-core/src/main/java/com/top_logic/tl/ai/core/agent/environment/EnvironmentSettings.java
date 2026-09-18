/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.provider.LLMService;

/**
 * Reads the {@code <environment>} section of the {@link LLMService} configuration.
 *
 * <p>
 * Every accessor answers without the service: a unit test, a migration or a boot step that runs
 * before the service is up gets the shipped default rather than an exception. The environment block
 * is a piece of prompt text, and no run should fail because the wording of it could not be looked
 * up.
 * </p>
 */
public class EnvironmentSettings {

	private EnvironmentSettings() {
		// Static utility.
	}

	/** The configured section, or {@code null} when the service is not running. */
	private static EnvironmentConfig config() {
		if (!LLMService.Module.INSTANCE.isActive()) {
			return null;
		}
		return LLMService.getInstance().getEnvironmentConfig();
	}

	/**
	 * The configured template, or the shipped {@link EnvironmentTemplate#DEFAULT} when none is set.
	 *
	 * @param resolver
	 *        Renders a {@link ResKey} in the language of the account the run acts for.
	 */
	public static String template(java.util.function.Function<ResKey, String> resolver) {
		EnvironmentConfig config = config();
		ResKey key = config == null ? null : config.getTemplate();
		if (key == null) {
			return EnvironmentTemplate.DEFAULT;
		}
		String text = resolver.apply(key);
		return text == null || text.isBlank() ? EnvironmentTemplate.DEFAULT : text;
	}

	/** The configured application name key, or {@code null} to use the application's own name. */
	public static ResKey applicationName() {
		EnvironmentConfig config = config();
		return config == null ? null : config.getApplication();
	}

	/** Whether the accounts' preference texts are sent at all. */
	public static boolean userPreferencesEnabled() {
		EnvironmentConfig config = config();
		return config == null || config.getUserPreferences();
	}

	/** How long an account's preference text may be, in characters. */
	public static int maxPreferencesLength() {
		EnvironmentConfig config = config();
		return config == null ? DEFAULT_MAX_PREFERENCES_LENGTH : config.getMaxPreferencesLength();
	}

	/**
	 * The cap that applies when the service is not running.
	 *
	 * @see EnvironmentConfig#getMaxPreferencesLength()
	 */
	public static final int DEFAULT_MAX_PREFERENCES_LENGTH = 1000;
}
