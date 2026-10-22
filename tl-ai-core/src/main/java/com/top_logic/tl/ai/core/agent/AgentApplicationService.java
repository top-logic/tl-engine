/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.agent.environment.EnvironmentConfig;
import com.top_logic.tl.ai.core.tools.model.ObjectIdConfig;

/**
 * What an agent knows about this application: what it is called, how the environment block is
 * worded, which identifiers its users speak, and the standing instructions every run is given - the
 * application context of {@code TL_init} among them.
 *
 * <p>
 * Development material: it belongs in the application's source and is written by the developers who
 * build the application, not changed in production. A change takes effect when this service is
 * restarted; nothing else restarts with it.
 * </p>
 *
 * <p>
 * Part of the split of the former {@code LLMService} (Gitea #691).
 * </p>
 */
@Label("Agent application knowledge")
@ServiceDependencies({})
public class AgentApplicationService extends ConfiguredManagedClass<AgentApplicationService.Config<?>> {

	/**
	 * Configuration interface for {@link AgentApplicationService}.
	 */
	@DisplayOrder({
		Config.PROMPT_CONTRIBUTORS,
		Config.ENVIRONMENT,
		Config.OBJECT_IDS,
	})
	public interface Config<I extends AgentApplicationService> extends ConfiguredManagedClass.Config<I> {

		/** Configuration property name for {@link #getPromptContributors()}. */
		String PROMPT_CONTRIBUTORS = "prompt-contributors";

		/** Configuration property name for {@link #getEnvironment()}. */
		String ENVIRONMENT = "environment";

		/** Configuration property name for {@link #getObjectIds()}. */
		String OBJECT_IDS = "object-ids";

		/**
		 * Standing instructions this application adds to the system prompt of every agent run.
		 *
		 * <p>
		 * For house rules that belong to the application rather than to one agent, so that they do
		 * not have to be written into every agent and do not have to be repeated on every turn. Each
		 * entry is asked once per run, in the order they are listed here, and what it answers is part
		 * of the cached prompt - so it must say the same thing throughout a conversation. The
		 * application context that Albert's TL_init skill writes is one of them. A change takes
		 * effect when this service is restarted.
		 * </p>
		 *
		 * @see #PROMPT_CONTRIBUTORS
		 */
		@Name(PROMPT_CONTRIBUTORS)
		@EntryTag("contributor")
		List<PolymorphicConfiguration<? extends SystemPromptContributor>> getPromptContributors();

		/**
		 * What every agent run is told about the situation it runs in: the wording of the
		 * environment block and the application name the model is given.
		 *
		 * @see #ENVIRONMENT
		 */
		@Name(ENVIRONMENT)
		EnvironmentConfig getEnvironment();

		/**
		 * What this application knows about the identifiers a model passes to a tool: the codes its
		 * own users speak, and what to tell a model that passed one instead of an object id.
		 *
		 * @see #OBJECT_IDS
		 */
		@Name(OBJECT_IDS)
		ObjectIdConfig getObjectIds();
	}

	private static volatile AgentApplicationService _instance;

	private final InstantiationContext _context;

	private List<SystemPromptContributor> _promptContributors = List.of();

	/**
	 * Creates a new {@link AgentApplicationService} from configuration.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The service configuration.
	 */
	@CalledByReflection
	public AgentApplicationService(InstantiationContext context, Config<?> config) {
		super(context, config);
		_context = context;
	}

	/**
	 * The running instance, or {@code null} while the service is not started.
	 */
	public static AgentApplicationService getInstance() {
		return _instance;
	}

	/**
	 * Whether the service is up; a run is not started while it is not, because it would miss the
	 * application's standing instructions.
	 */
	public static boolean isRunning() {
		return _instance != null;
	}

	@Override
	protected void startUp() {
		super.startUp();
		List<SystemPromptContributor> instances = new ArrayList<>();
		for (PolymorphicConfiguration<? extends SystemPromptContributor> contributor
				: getConfig().getPromptContributors()) {
			SystemPromptContributor instance = _context.getInstance(contributor);
			if (instance != null) {
				instances.add(instance);
			}
		}
		_promptContributors = List.copyOf(instances);
		_instance = this;
	}

	@Override
	protected void shutDown() {
		try {
			_instance = null;
		} finally {
			super.shutDown();
		}
	}

	/**
	 * What this application adds to the system prompt of every agent run, in configuration order.
	 */
	public List<SystemPromptContributor> getPromptContributors() {
		return _promptContributors;
	}

	/**
	 * The configured wording of the environment block, or {@code null} for the shipped one.
	 */
	public ResKey getEnvironmentTemplate() {
		EnvironmentConfig environment = getConfig().getEnvironment();
		return environment == null ? null : environment.getTemplate();
	}

	/**
	 * The configured application name, or {@code null} to use the application's own name.
	 */
	public ResKey getApplicationName() {
		EnvironmentConfig environment = getConfig().getEnvironment();
		return environment == null ? null : environment.getApplication();
	}

	/**
	 * What this application knows about the identifiers a model passes to a tool; <code>null</code>
	 * when nothing is configured.
	 */
	public ObjectIdConfig getObjectIds() {
		return getConfig().getObjectIds();
	}

	/**
	 * What the application adds to every system prompt, also answered when this service is down.
	 *
	 * <p>
	 * Empty for a minimal setup that boots no services, which is the same prompt such a setup built
	 * before the seam existed.
	 * </p>
	 */
	public static List<SystemPromptContributor> promptContributors() {
		AgentApplicationService service = _instance;
		return service == null ? List.of() : service.getPromptContributors();
	}

	/**
	 * Module for {@link AgentApplicationService}.
	 */
	public static final class Module extends TypedRuntimeModule<AgentApplicationService> {

		/**
		 * Singleton {@link AgentApplicationService.Module} instance.
		 */
		public static final AgentApplicationService.Module INSTANCE = new AgentApplicationService.Module();

		private Module() {
			// Singleton constructor
		}

		@Override
		public Class<AgentApplicationService> getImplementation() {
			return AgentApplicationService.class;
		}
	}
}
