/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Set;

import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.model.ApplicationContext;
import com.top_logic.tl.ai.core.model.TlAiCoreFactory;
import com.top_logic.tl.ai.core.provider.AgentModelAssignment;
import com.top_logic.util.model.ModelService;

/**
 * Adds the application context - what the application is for, who works with it, its terms and its
 * rules - to the system prompt of every agent a user talks to.
 *
 * <p>
 * The text is the {@link ApplicationContext} singleton of {@code tl.ai.core}: written by Albert's
 * {@code TL_init} skill and saved by the person who approved it, or typed into the Agent Editor by an
 * administrator. It is in the knowledge base rather than in the service configuration so that
 * neither needs a restart, nor a save of the service editor, which writes a snapshot of the whole
 * service that shadows every later change of its shipped configuration.
 * </p>
 *
 * <p>
 * Asked by {@link AgentFactory} for every run, after the contributors the installation configures,
 * without being configured itself: applications override the {@code LLMService} configuration as a
 * whole, so an entry shipped in its contributor list would never reach them. An application that has
 * written no context gets nothing added, so there is nothing to switch off. A saved change reaches the
 * next run - in an open conversation that costs one miss of the provider's prompt cache, as a
 * restarted {@link TextPromptContributor} does.
 * </p>
 *
 * <p>
 * An {@link AgentDefinition#infrastructure() infrastructure} agent never gets it: the application runs
 * such an agent for a user, the prompt improver for one, and it has no use for the domain and would
 * pay for it on every call.
 * </p>
 */
public final class ApplicationContextContributor implements SystemPromptContributor {

	/** The one instance; it has no state. */
	public static final ApplicationContextContributor INSTANCE = new ApplicationContextContributor();

	/**
	 * The longest text the context may have, in characters.
	 *
	 * <p>
	 * The same bound as the {@code size-constraint} of {@code tl.ai.core:ApplicationContext#text}:
	 * every run of every agent that gets the text pays for it, so this is the one cost guard it has -
	 * about 2,000 tokens.
	 * </p>
	 */
	public static final int MAX_LENGTH = 8000;

	private ApplicationContextContributor() {
		// Singleton.
	}

	@Override
	public String contribute(AgentDefinition agent, AgentModelAssignment assignment) {
		if (agent.infrastructure()) {
			return null;
		}
		ApplicationContext context = current();
		if (context == null || !appliesTo(context, agent.name())) {
			return null;
		}
		return context.getText();
	}

	/**
	 * Whether the agent with the given technical name gets the text of the given context.
	 */
	static boolean appliesTo(ApplicationContext context, String agentName) {
		Set<String> agents = context.getAgents();
		return agents == null || agents.isEmpty() || agents.contains(agentName);
	}

	/**
	 * The application context of this application, or <code>null</code> when there is no model to
	 * hold it - a run without a knowledge base, as unit tests of the agent loop start one.
	 */
	public static ApplicationContext current() {
		if (!ModelService.Module.INSTANCE.isActive()) {
			return null;
		}
		TLModule module = ModelService.getApplicationModel().getModule(TlAiCoreFactory.TL_AI_CORE_STRUCTURE);
		if (module == null) {
			return null;
		}
		TLObject singleton = module.getSingleton(TlAiCoreFactory.SINGLETON_APPLICATION_CONTEXT);
		return singleton instanceof ApplicationContext context ? context : null;
	}

}
