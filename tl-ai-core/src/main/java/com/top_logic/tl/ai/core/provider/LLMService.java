/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.Location;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.SystemPromptContributor;
import com.top_logic.tl.ai.core.agent.environment.UserMemoryConfig;
import com.top_logic.tl.ai.core.limits.BudgetConfig;
import com.top_logic.tl.ai.core.limits.CapacityConfig;
import com.top_logic.tl.ai.core.limits.RunLimitsConfig;
import com.top_logic.tl.ai.core.provider.health.FailoverConfig;
import com.top_logic.tl.ai.core.security.SecurityContextFactory;
import com.top_logic.tl.ai.core.tools.model.ObjectIdConfig;

/**
 * The service tl-ai had before its configuration was split by audience (Gitea #691); it does not run
 * any more.
 *
 * <p>
 * What it held is now configured on four services: {@link LLMProviderService} (the providers, the
 * model catalog, failover), {@link AgentRuntimeService} (which agent runs on which model and how many
 * runs at once), {@link AgentApplicationService} (what an agent is told about the application) and
 * {@link AgentPolicyService} (what a run may cost and what an agent may keep about a person).
 * </p>
 *
 * <p>
 * The class stays only so that an application's configuration of the old service can be read and
 * refused with a sentence that says where each of its sections now belongs: without it the engine
 * would fail the boot with "class not found" and name no new place, and it knows no alias for a
 * renamed service. {@link LLMProviderService} asks for that refusal at its start. It is not a module
 * and is never started; remove it with the next major release.
 * </p>
 *
 * @see #refuseOldConfiguration(InstantiationContext)
 */
public class LLMService extends ConfiguredManagedClass<LLMService.Config<?>> {

	/** The migration page every refusal points to. */
	public static final String MIGRATION_PAGE = "tl-ai-core/doc/llm-service-migration.md";

	/**
	 * The old agent execution section.
	 */
	public interface AgentExecutionConfig extends ConfigPart {

		/** @see #getChatEntryPoint() */
		String CHAT_ENTRY_POINT = "chat-entry-point";

		/** @see #getDefaultModel() */
		String DEFAULT_MODEL = "default-model";

		/** @see #getDefaultProvider() */
		String DEFAULT_PROVIDER = "default-provider";

		/** @see #getAgentOverrides() */
		String AGENT_OVERRIDES = "agent-overrides";

		/**
		 * Now {@link AgentRuntimeService.Config#getChatEntryPoint()}.
		 *
		 * @see #CHAT_ENTRY_POINT
		 */
		@Name(CHAT_ENTRY_POINT)
		@Nullable
		String getChatEntryPoint();

		/**
		 * Now {@link AgentRuntimeService.Config#getDefaultModel()}.
		 *
		 * @see #DEFAULT_MODEL
		 */
		@Name(DEFAULT_MODEL)
		@Nullable
		String getDefaultModel();

		/**
		 * Now {@link AgentRuntimeService.Config#getDefaultProvider()}.
		 *
		 * @see #DEFAULT_PROVIDER
		 */
		@Name(DEFAULT_PROVIDER)
		@Nullable
		String getDefaultProvider();

		/**
		 * Now {@link AgentRuntimeService.Config#getAgentOverrides()}.
		 *
		 * @see #AGENT_OVERRIDES
		 */
		@Name(AGENT_OVERRIDES)
		@EntryTag("agent")
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		Map<String, AgentRuntimeService.AgentOverride> getAgentOverrides();
	}

	/**
	 * The old environment section.
	 */
	public interface EnvironmentSection extends ConfigPart {

		/** @see #getTemplate() */
		String TEMPLATE = "template";

		/** @see #getApplication() */
		String APPLICATION = "application";

		/** @see #getUserPreferences() */
		String USER_PREFERENCES = "user-preferences";

		/** @see #getMaxPreferencesLength() */
		String MAX_PREFERENCES_LENGTH = "max-preferences-length";

		/**
		 * Now the template of {@link AgentApplicationService.Config#getEnvironment()}.
		 *
		 * @see #TEMPLATE
		 */
		@Name(TEMPLATE)
		@Nullable
		ResKey getTemplate();

		/**
		 * Now the application name of {@link AgentApplicationService.Config#getEnvironment()}.
		 *
		 * @see #APPLICATION
		 */
		@Name(APPLICATION)
		@Nullable
		ResKey getApplication();

		/**
		 * Now {@link AgentPolicyService.Config#getUserPreferences()}.
		 *
		 * @see #USER_PREFERENCES
		 */
		@Name(USER_PREFERENCES)
		@BooleanDefault(true)
		boolean getUserPreferences();

		/**
		 * Now {@link AgentPolicyService.Config#getMaxPreferencesLength()}.
		 *
		 * @see #MAX_PREFERENCES_LENGTH
		 */
		@Name(MAX_PREFERENCES_LENGTH)
		@IntDefault(1000)
		int getMaxPreferencesLength();
	}

	/**
	 * Every section the old service had, so that an old file can still be read - and refused.
	 */
	public interface Config<I extends LLMService> extends ConfiguredManagedClass.Config<I> {

		/** @see #getProviders() */
		String PROVIDERS = "providers";

		/** @see #getAgentExecution() */
		String AGENT_EXECUTION = "agent-execution";

		/** @see #getEnvironment() */
		String ENVIRONMENT = "environment";

		/** @see #getUserMemory() */
		String USER_MEMORY = "user-memory";

		/** @see #getRunLimits() */
		String RUN_LIMITS = "run-limits";

		/** @see #getObjectIds() */
		String OBJECT_IDS = "object-ids";

		/** @see #getSecurityContext() */
		String SECURITY_CONTEXT = "security-context";

		/** @see #getPromptContributors() */
		String PROMPT_CONTRIBUTORS = "prompt-contributors";

		/** @see #getModelCatalog() */
		String MODEL_CATALOG = "model-catalog";

		/** @see #getFailover() */
		String FAILOVER = "failover";

		/**
		 * Now {@link LLMProviderService.Config#getProviders()}.
		 *
		 * @see #PROVIDERS
		 */
		@Name(PROVIDERS)
		@EntryTag("provider")
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		Map<String, LLMProviderConfig.Config> getProviders();

		/**
		 * Now on {@link AgentRuntimeService}.
		 *
		 * @see #AGENT_EXECUTION
		 */
		@Name(AGENT_EXECUTION)
		AgentExecutionConfig getAgentExecution();

		/**
		 * Now on {@link AgentApplicationService} and {@link AgentPolicyService}.
		 *
		 * @see #ENVIRONMENT
		 */
		@Name(ENVIRONMENT)
		EnvironmentSection getEnvironment();

		/**
		 * Now {@link AgentPolicyService.Config#getUserMemory()}.
		 *
		 * @see #USER_MEMORY
		 */
		@Name(USER_MEMORY)
		UserMemoryConfig getUserMemory();

		/**
		 * Now on {@link AgentRuntimeService} (capacity) and {@link AgentPolicyService} (budget).
		 *
		 * @see #RUN_LIMITS
		 */
		@Name(RUN_LIMITS)
		RunLimitsConfig getRunLimits();

		/**
		 * Now {@link AgentApplicationService.Config#getObjectIds()}.
		 *
		 * @see #OBJECT_IDS
		 */
		@Name(OBJECT_IDS)
		ObjectIdConfig getObjectIds();

		/**
		 * Now {@link AgentRuntimeService.Config#getSecurityContext()}.
		 *
		 * @see #SECURITY_CONTEXT
		 */
		@Name(SECURITY_CONTEXT)
		@Nullable
		PolymorphicConfiguration<SecurityContextFactory> getSecurityContext();

		/**
		 * Now {@link AgentApplicationService.Config#getPromptContributors()}.
		 *
		 * @see #PROMPT_CONTRIBUTORS
		 */
		@Name(PROMPT_CONTRIBUTORS)
		@EntryTag("contributor")
		List<PolymorphicConfiguration<? extends SystemPromptContributor>> getPromptContributors();

		/**
		 * Now {@link LLMProviderService.Config#getModelCatalog()}.
		 *
		 * @see #MODEL_CATALOG
		 */
		@Name(MODEL_CATALOG)
		LLMProviderService.ModelCatalogConfig getModelCatalog();

		/**
		 * Now {@link LLMProviderService.Config#getFailover()}.
		 *
		 * @see #FAILOVER
		 */
		@Name(FAILOVER)
		FailoverConfig getFailover();
	}

	/**
	 * Created by reflection only, never started.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LLMService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * Refuses the start when the application still configures this service.
	 *
	 * @param context
	 *        Where the refusal is reported, in addition to being thrown.
	 * @throws ConfigurationError
	 *         If a configuration of this service sets anything, naming where it is and where each of
	 *         its sections now belongs.
	 */
	public static void refuseOldConfiguration(InstantiationContext context) {
		String refusal = refusal(configured());
		if (refusal == null) {
			return;
		}
		context.error(refusal);
		throw new ConfigurationError(ResKey.text(refusal));
	}

	/**
	 * The configuration of this service the application has, or <code>null</code> when it has
	 * none.
	 */
	private static Config<?> configured() {
		ApplicationConfig applicationConfig = ApplicationConfig.getInstance();
		if (applicationConfig == null) {
			return null;
		}
		ServiceConfiguration<?> config;
		try {
			// Answers a fresh default when nothing configures the service; refusal() then finds
			// nothing set.
			config = applicationConfig.getServiceConfiguration(LLMService.class);
		} catch (ConfigurationException ex) {
			return null;
		}
		return config instanceof Config<?> old ? old : null;
	}

	/**
	 * Why the given configuration of the old service stops the application, or <code>null</code>
	 * when it sets nothing.
	 *
	 * @param old
	 *        The configuration; may be <code>null</code>.
	 */
	static String refusal(Config<?> old) {
		if (old == null) {
			return null;
		}
		List<String> moves = new ArrayList<>();
		String providers = LLMProviderService.class.getSimpleName();
		String runtime = AgentRuntimeService.class.getSimpleName();
		String application = AgentApplicationService.class.getSimpleName();
		String policy = AgentPolicyService.class.getSimpleName();

		move(moves, old, Config.PROVIDERS, "<providers>", providers + " <providers>");
		move(moves, old, Config.MODEL_CATALOG, "<model-catalog>",
			providers + " <model-catalog> (delete <llm-configuration>: it is generated at every start)");
		move(moves, old, Config.FAILOVER, "<failover>", providers + " <failover>");

		AgentExecutionConfig execution = old.getAgentExecution();
		move(moves, execution, AgentExecutionConfig.DEFAULT_MODEL, "<agent-execution default-model>",
			runtime + " default-model");
		move(moves, execution, AgentExecutionConfig.DEFAULT_PROVIDER, "<agent-execution default-provider>",
			runtime + " default-provider");
		move(moves, execution, AgentExecutionConfig.CHAT_ENTRY_POINT, "<agent-execution chat-entry-point>",
			runtime + " chat-entry-point");
		move(moves, execution, AgentExecutionConfig.AGENT_OVERRIDES, "<agent-execution><agent-overrides>",
			runtime + " <agent-overrides>");
		move(moves, old, Config.SECURITY_CONTEXT, "<security-context>", runtime + " <security-context>");

		RunLimitsConfig limits = old.getRunLimits();
		for (String property : List.of(CapacityConfig.CHAT_WORKERS, CapacityConfig.CHAT_QUEUE,
			CapacityConfig.CONCURRENT_RUNS_PER_ACCOUNT)) {
			move(moves, limits, property, "<run-limits " + property + ">", runtime + " <capacity " + property + ">");
		}
		for (String property : List.of(BudgetConfig.TOKEN_BUDGET, BudgetConfig.BUDGET_WINDOW_HOURS)) {
			move(moves, limits, property, "<run-limits " + property + ">", policy + " <budget " + property + ">");
		}
		move(moves, limits, BudgetConfig.ROLE_BUDGETS, "<run-limits><role-budgets>",
			policy + " <budget><role-budgets>");

		EnvironmentSection environment = old.getEnvironment();
		move(moves, environment, EnvironmentSection.TEMPLATE, "<environment><template>",
			application + " <environment><template>");
		move(moves, environment, EnvironmentSection.APPLICATION, "<environment><application>",
			application + " <environment><application>");
		move(moves, environment, EnvironmentSection.USER_PREFERENCES, "<environment user-preferences>",
			policy + " user-preferences");
		move(moves, environment, EnvironmentSection.MAX_PREFERENCES_LENGTH, "<environment max-preferences-length>",
			policy + " max-preferences-length");
		move(moves, old, Config.OBJECT_IDS, "<object-ids>", application + " <object-ids>");
		move(moves, old, Config.PROMPT_CONTRIBUTORS, "<prompt-contributors>", application + " <prompt-contributors>");
		move(moves, old, Config.USER_MEMORY, "<user-memory>", policy + " <user-memory>");

		if (moves.isEmpty()) {
			return null;
		}
		return "The application still configures " + LLMService.class.getName() + where(old)
			+ ", which tl-ai no longer reads: its configuration was split in four services (Gitea #691). Move "
			+ String.join("; ", moves) + ". Then delete the configuration of " + LLMService.class.getSimpleName()
			+ ". The new services are " + LLMProviderService.class.getName() + ", "
			+ AgentRuntimeService.class.getName() + ", " + AgentApplicationService.class.getName() + " and "
			+ AgentPolicyService.class.getName() + "; see " + MIGRATION_PAGE + ".";
	}

	private static void move(List<String> moves, ConfigurationItem owner, String property, String oldPath,
			String newPlace) {
		if (isSet(owner, property)) {
			moves.add(oldPath + " to " + newPlace);
		}
	}

	private static String where(ConfigurationItem item) {
		Location location = item.location();
		if (location == null || location.getResource() == null) {
			return "";
		}
		int line = location.getLine();
		return " (" + location.getResource() + (line > 0 ? ":" + line : "") + ")";
	}

	/**
	 * Whether the given property of the given configuration holds a value of its own.
	 *
	 * <p>
	 * An empty element - which a Service Editor snapshot may write for a section it shows - holds
	 * none.
	 * </p>
	 */
	static boolean isSet(ConfigurationItem owner, String property) {
		if (owner == null) {
			return false;
		}
		PropertyDescriptor descriptor = owner.descriptor().getProperty(property);
		return descriptor != null && owner.valueSet(descriptor) && holdsValue(owner.value(descriptor));
	}

	private static boolean holdsValue(Object value) {
		if (value == null) {
			return false;
		}
		if (value instanceof Collection<?> collection) {
			return !collection.isEmpty();
		}
		if (value instanceof Map<?, ?> map) {
			return !map.isEmpty();
		}
		if (value instanceof PolymorphicConfiguration<?>) {
			// A class named is a setting, even with nothing else in the element.
			return true;
		}
		if (value instanceof ConfigurationItem item) {
			for (PropertyDescriptor property : item.descriptor().getProperties()) {
				if (property.hasContainerAnnotation() || property.isDerived()) {
					continue;
				}
				if (item.valueSet(property) && holdsValue(item.value(property))) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

}
