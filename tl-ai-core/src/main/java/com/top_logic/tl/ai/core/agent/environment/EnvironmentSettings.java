/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.provider.LLMService;
import com.top_logic.util.Resources;

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
	 * Resource key of the shipped template, in {@code tl-ai-core.app-messages}.
	 *
	 * <p>
	 * The shipped wording lives in a resource bundle rather than in the service configuration,
	 * because an application that overrides {@code LLMService} wholesale - which is what an
	 * application that names its own providers does - would otherwise lose the shipped template and
	 * fall back to the English constant while the block's own headings stayed in the user's
	 * language. A bundle survives that.
	 * </p>
	 */
	public static final String TEMPLATE_RESOURCE = "tl.ai.environment.template";

	/**
	 * The template to use, in the language of the account the run acts for.
	 *
	 * <p>
	 * Configuration first, then the shipped resource, then {@link EnvironmentTemplate#DEFAULT} - so
	 * an installation may reword it, a language this product ships is honoured, and a run still gets
	 * a block when neither is reachable.
	 * </p>
	 *
	 * @param resources
	 *        Resolves a {@link ResKey} in that account's language.
	 */
	public static String template(Resources resources) {
		EnvironmentConfig config = config();
		ResKey configured = config == null ? null : config.getTemplate();
		String text = configured == null ? null : resources.getString(configured, null);
		if (text == null || text.isBlank()) {
			text = resources.getString(ResKey.legacy(TEMPLATE_RESOURCE), null);
		}
		return text == null || text.isBlank() ? EnvironmentTemplate.DEFAULT : text;
	}

	/**
	 * Resource key of the shipped {@code {{turn_context}}} wording, in {@code tl-ai-core.app-messages}.
	 *
	 * <p>
	 * A bundle rather than the service configuration, for the reason {@link #TEMPLATE_RESOURCE} is
	 * one: an application that names its own LLM providers overrides the service wholesale, and the
	 * wording has to survive that in the user's language.
	 * </p>
	 */
	public static final String TURN_CONTEXT_RESOURCE = "tl.ai.environment.turn-context";

	/**
	 * What the environment block says about the per-turn block, in the language of the account the
	 * run acts for.
	 *
	 * <p>
	 * The shipped resource, then {@link TurnContext#NOTE} - so a language this product ships is
	 * honoured and a run still gets a sentence when the bundle is not reachable.
	 * </p>
	 *
	 * @param resources
	 *        Resolves a {@link ResKey} in that account's language.
	 */
	public static String turnContextNote(Resources resources) {
		String text = resources.getString(ResKey.legacy(TURN_CONTEXT_RESOURCE), null);
		return text == null || text.isBlank() ? TurnContext.NOTE : text;
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
