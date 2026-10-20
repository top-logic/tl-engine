/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.util.Resources;

/**
 * Reads what every run is told about its environment: the wording and the application name from
 * {@link AgentApplicationService}, the switches about the accounts' preference texts from
 * {@link AgentPolicyService}.
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

	/** The application service, or {@code null} when it is not running. */
	private static AgentApplicationService application() {
		if (!AgentApplicationService.Module.INSTANCE.isActive()) {
			return null;
		}
		return AgentApplicationService.getInstance();
	}

	/** The policy service holding the preference switches, or {@code null} when it is not running. */
	private static AgentPolicyService policy() {
		if (!AgentPolicyService.Module.INSTANCE.isActive()) {
			return null;
		}
		return AgentPolicyService.getInstance();
	}

	/**
	 * Resource key of the shipped template, in {@code tl-ai-core.app-messages}.
	 *
	 * <p>
	 * The shipped wording lives in a resource bundle rather than in the service configuration,
	 * because an application that overrides the service wholesale - which is what a save in the
	 * Service Editor does - would otherwise lose the shipped template and fall back to the English
	 * constant while the block's own headings stayed in the user's language. A bundle survives that.
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
		AgentApplicationService application = application();
		ResKey configured = application == null ? null : application.getEnvironmentTemplate();
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
		AgentApplicationService application = application();
		return application == null ? null : application.getApplicationName();
	}

	/** Whether the accounts' preference texts are sent at all. */
	public static boolean userPreferencesEnabled() {
		AgentPolicyService policy = policy();
		return policy == null || policy.getUserPreferences();
	}

	/** How long an account's preference text may be, in characters. */
	public static int maxPreferencesLength() {
		AgentPolicyService policy = policy();
		return policy == null ? DEFAULT_MAX_PREFERENCES_LENGTH : policy.getMaxPreferencesLength();
	}

	/**
	 * The cap that applies when the service is not running.
	 *
	 * @see AgentPolicyService.Config#getMaxPreferencesLength()
	 */
	public static final int DEFAULT_MAX_PREFERENCES_LENGTH = 1000;
}
