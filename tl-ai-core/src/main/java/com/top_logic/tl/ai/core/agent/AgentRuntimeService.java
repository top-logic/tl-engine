/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.constraint.annotation.Bound;
import com.top_logic.basic.config.constraint.annotation.Comparision;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tl.ai.core.limits.CapacityConfig;
import com.top_logic.tl.ai.core.observe.RunObserver;
import com.top_logic.tl.ai.core.observe.RunObservers;
import com.top_logic.tl.ai.core.provider.AgentModelAssignment;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.LLMProviderService.ServiceModelOptions;
import com.top_logic.tl.ai.core.provider.LLMProviderService.ServiceProviderOptions;
import com.top_logic.tl.ai.core.provider.ModelToolCallConstraint;
import com.top_logic.tl.ai.core.provider.ProviderExposesModelConstraint;
import com.top_logic.tl.ai.core.provider.ProviderFieldMode;
import com.top_logic.tl.ai.core.security.DefaultSecurityContextFactory;
import com.top_logic.tl.ai.core.security.SecurityContextFactory;
import com.top_logic.tl.ai.tool.api.AICategory;

/**
 * Which agent runs how: the model each agent runs on, its execution parameters, how many runs the
 * installation carries at once, which agent a chat starts with, and what decides which tool calls a
 * person must confirm.
 *
 * <p>
 * Set by operations and development. The models it names are those of {@link LLMProviderService},
 * which says how they are reached; this service depends on it, checks at its start that every model
 * it names is exposed by exactly one of its providers (or by the provider named for it), and is
 * restarted with it. Restarting this service does not touch the providers: no catalog is fetched,
 * and what is known about the health of every model is kept.
 * </p>
 *
 * <p>
 * Part of the split of the former {@code LLMService} (Gitea #691); what an agent is told about the
 * application is on {@link AgentApplicationService}, what a run may cost and keep about a person on
 * {@link AgentPolicyService}.
 * </p>
 */
@Label("Agent runtime")
@ServiceDependencies({
	// Every model reference is checked against its providers at start.
	LLMProviderService.Module.class,
})
@ServiceCategory.Of(AICategory.class)
public class AgentRuntimeService extends ConfiguredManagedClass<AgentRuntimeService.Config<?>> {

	/**
	 * Provides all available agent names for selection in the admin UI.
	 */
	public static class AllAgentNameOptions extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			if (!TopLogicAgentDefinitions.Module.INSTANCE.isActive()) {
				return List.of();
			}
			List<String> names = new ArrayList<>();
			for (AgentDefinition ad : AgentDefinitionQuery.getInstance().getAllAgents()) {
				names.add(ad.name());
			}
			return names;
		}
	}

	/**
	 * Per-agent override for model selection and execution parameters.
	 *
	 * <p>
	 * Each entry overrides the {@link Config#getDefaultModel() default model} for a specific agent and optionally
	 * configures execution parameters (max turns, timeouts, tool choice) and model-specific settings
	 * (temperature, reasoning effort). Parameters left {@code null} inherit defaults from the model
	 * catalog or system defaults.
	 * </p>
	 */
	@DisplayOrder({
		NamedConfigMandatory.NAME_ATTRIBUTE,
		AgentOverride.MODEL,
		AgentOverride.PROVIDER,
		AgentOverride.CONTEXT_WINDOW,
		AgentOverride.MAX_TOKENS,
		AgentOverride.MAX_TURNS,
		AgentOverride.MAX_TOOL_RESULT_CHARS,
		AgentOverride.TOOL_CHOICE,
		AgentOverride.TIMEOUT_SECONDS,
		AgentOverride.MAX_RETRIES,
		AgentOverride.TEMPERATURE,
		AgentOverride.TOP_P,
		AgentOverride.THINKING_EFFORT,
		AgentOverride.TOP_K,
		AgentOverride.REASONING_EFFORT,
		AgentOverride.REASONING_SUMMARY,
	})
	public interface AgentOverride extends NamedConfigMandatory, ConfigPart {
		/** @see #getModel() */
		String MODEL = "model";

		/** @see #getProvider() */
		String PROVIDER = "provider";

		/** @see #getMaxTokens() */
		String MAX_TOKENS = "max-tokens";

		/** @see #getContextWindow() */
		String CONTEXT_WINDOW = "context-window";

		/** @see #getMaxTurns() */
		String MAX_TURNS = "max-turns";

		/** @see #getTemperature() */
		String TEMPERATURE = "temperature";

		/** @see #getTopP() */
		String TOP_P = "top-p";

		/** @see #getThinkingEffort() */
		String THINKING_EFFORT = "thinking-effort";

		/** @see #getTopK() */
		String TOP_K = "top-k";

		/** @see #getReasoningEffort() */
		String REASONING_EFFORT = "reasoning-effort";

		/** @see #getReasoningSummary() */
		String REASONING_SUMMARY = "reasoning-summary";

		/** @see #getToolChoice() */
		String TOOL_CHOICE = "tool-choice";

		/** @see #getTimeoutSeconds() */
		String TIMEOUT_SECONDS = "timeout-seconds";

		/** @see #getMaxRetries() */
		String MAX_RETRIES = "max-retries";

		/** @see #getMaxToolResultChars() */
		String MAX_TOOL_RESULT_CHARS = "max-tool-result-chars";

		@Override
		@Options(fun = AllAgentNameOptions.class)
		String getName();

		/**
		 * The model to use for this agent.
		 *
		 * @see #MODEL
		 */
		@Name(MODEL)
		@Options(fun = ServiceModelOptions.class)
		@Constraint(value = ModelToolCallConstraint.class, args = @Ref(NAME_ATTRIBUTE))
		String getModel();

		/**
		 * The provider serving this agent's model, or empty when only one provider exposes it.
		 *
		 * <p>
		 * Name a provider when two provider entries expose the same model - separate accounts, or
		 * an official endpoint next to a gateway. Left empty for a model exposed twice, the
		 * service refuses to start. An agent inheriting the default model inherits the default
		 * provider.
		 * </p>
		 *
		 * @see #PROVIDER
		 */
		@Name(PROVIDER)
		@Nullable
		@Options(fun = ServiceProviderOptions.class)
		@Constraint(value = ProviderExposesModelConstraint.class, args = @Ref(MODEL))
		String getProvider();

		/**
		 * Maximum output tokens per LLM call, or {@code null} for catalog default.
		 *
		 * @see #MAX_TOKENS
		 */
		@Name(MAX_TOKENS)
		@Nullable
		Integer getMaxTokens();

		/**
		 * Context window size override, or {@code null} for catalog default.
		 *
		 * @see #CONTEXT_WINDOW
		 */
		@Name(CONTEXT_WINDOW)
		@Nullable
		Integer getContextWindow();

		/**
		 * Maximum agent loop turns, or {@code null} for default.
		 *
		 * @see #MAX_TURNS
		 */
		@Name(MAX_TURNS)
		@Nullable
		Integer getMaxTurns();

		/**
		 * Sampling temperature, or {@code null} for default.
		 *
		 * @see #TEMPERATURE
		 */
		@Name(TEMPERATURE)
		@Nullable
		@DynamicMode(fun = ProviderFieldMode.TemperatureFieldMode.class, args = @Ref(MODEL))
		@Bound(comparison = Comparision.GREATER_OR_EQUAL, value = 0.0)
		@Bound(comparison = Comparision.SMALLER_OR_EQUAL, value = 1.0)
		Double getTemperature();

		/**
		 * Top-p sampling parameter, or {@code null} for default.
		 *
		 * @see #TOP_P
		 */
		@Label("Top-P")
		@Name(TOP_P)
		@Nullable
		@DynamicMode(fun = ProviderFieldMode.TemperatureFieldMode.class, args = @Ref(MODEL))
		@Bound(comparison = Comparision.GREATER_OR_EQUAL, value = 0.0)
		@Bound(comparison = Comparision.SMALLER_OR_EQUAL, value = 1.0)
		Double getTopP();

		/**
		 * Anthropic thinking effort (low/medium/high/max), or {@code null}.
		 *
		 * @see #THINKING_EFFORT
		 */
		@Name(THINKING_EFFORT)
		@Nullable
		@DynamicMode(fun = ProviderFieldMode.AnthropicFieldMode.class, args = { @Ref(MODEL), @Ref(PROVIDER) })
		String getThinkingEffort();

		/**
		 * Anthropic top-k parameter, or {@code null}.
		 *
		 * @see #TOP_K
		 */
		@Label("Top-K")
		@Name(TOP_K)
		@Nullable
		@DynamicMode(fun = ProviderFieldMode.AnthropicFieldMode.class, args = { @Ref(MODEL), @Ref(PROVIDER) })
		Integer getTopK();

		/**
		 * <code>OpenAI</code>/<code>xAI</code> reasoning effort (low/medium/high), or {@code null}.
		 *
		 * @see #REASONING_EFFORT
		 */
		@Name(REASONING_EFFORT)
		@Nullable
		@DynamicMode(fun = ProviderFieldMode.OpenAIFieldMode.class, args = { @Ref(MODEL), @Ref(PROVIDER) })
		String getReasoningEffort();

		/**
		 * <code>OpenAI</code>/<code>xAI</code> reasoning summary (auto/detailed), or {@code null}.
		 *
		 * @see #REASONING_SUMMARY
		 */
		@Name(REASONING_SUMMARY)
		@Nullable
		@DynamicMode(fun = ProviderFieldMode.OpenAIFieldMode.class, args = { @Ref(MODEL), @Ref(PROVIDER) })
		String getReasoningSummary();

		/**
		 * Tool choice (AUTO/REQUIRED/NONE), or {@code null} for provider default.
		 *
		 * @see #TOOL_CHOICE
		 */
		@Name(TOOL_CHOICE)
		@Nullable
		String getToolChoice();

		/**
		 * Request timeout in seconds, or {@code null} for default.
		 *
		 * @see #TIMEOUT_SECONDS
		 */
		@Name(TIMEOUT_SECONDS)
		@Nullable
		Integer getTimeoutSeconds();

		/**
		 * How often a rate-limited or overloaded model is asked again before the run fails or
		 * moves on to its fallback model, or {@code null} for default. {@code 0} switches retrying
		 * off.
		 *
		 * <p>
		 * A rate-limited run with no fallback model left keeps asking for as long as the
		 * rate-limit wait of the failover section of {@link LLMProviderService} allows.
		 * </p>
		 *
		 * @see #MAX_RETRIES
		 */
		@Name(MAX_RETRIES)
		@Nullable
		Integer getMaxRetries();

		/**
		 * Maximum characters per tool result, or {@code null} for default.
		 *
		 * @see #MAX_TOOL_RESULT_CHARS
		 */
		@Name(MAX_TOOL_RESULT_CHARS)
		@Nullable
		Integer getMaxToolResultChars();
	}

	/**
	 * Configuration interface for {@link AgentRuntimeService}.
	 */
	@DisplayOrder({
		Config.DEFAULT_MODEL,
		Config.DEFAULT_PROVIDER,
		Config.AGENT_OVERRIDES,
		Config.MAX_TURNS,
		Config.CHAT_ENTRY_POINT,
		Config.CAPACITY,
		Config.SECURITY_CONTEXT,
		Config.RUN_OBSERVERS,
	})
	public interface Config<I extends AgentRuntimeService> extends ConfiguredManagedClass.Config<I> {

		/** Configuration property name for {@link #getDefaultModel()}. */
		String DEFAULT_MODEL = "default-model";

		/** Configuration property name for {@link #getDefaultProvider()}. */
		String DEFAULT_PROVIDER = "default-provider";

		/** Configuration property name for {@link #getAgentOverrides()}. */
		String AGENT_OVERRIDES = "agent-overrides";

		/** Configuration property name for {@link #getMaxTurns()}. */
		String MAX_TURNS = "max-turns";

		/** Configuration property name for {@link #getChatEntryPoint()}. */
		String CHAT_ENTRY_POINT = "chat-entry-point";

		/** Configuration property name for {@link #getCapacity()}. */
		String CAPACITY = "capacity";

		/** Configuration property name for {@link #getSecurityContext()}. */
		String SECURITY_CONTEXT = "security-context";

		/** Configuration property name for {@link #getRunObservers()}. */
		String RUN_OBSERVERS = "run-observers";

		/**
		 * The model every agent runs on that has no model of its own in the
		 * {@link #getAgentOverrides() agent overrides}.
		 *
		 * <p>
		 * Left empty, an agent without a model of its own cannot run, and says so when it is
		 * started. A model named here must be exposed by a provider of the LLM providers - otherwise
		 * this service does not start.
		 * </p>
		 *
		 * @see #DEFAULT_MODEL
		 */
		@Name(DEFAULT_MODEL)
		@Nullable
		@Options(fun = ServiceModelOptions.class)
		String getDefaultModel();

		/**
		 * The provider serving the default model, or empty when only one provider exposes it.
		 *
		 * <p>
		 * Applies to every agent that has no provider of its own. Left empty for a default model
		 * exposed by two providers, the service refuses to start rather than pick one by
		 * configuration order.
		 * </p>
		 *
		 * @see #DEFAULT_PROVIDER
		 */
		@Name(DEFAULT_PROVIDER)
		@Nullable
		@Options(fun = ServiceProviderOptions.class)
		@Constraint(value = ProviderExposesModelConstraint.class, args = @Ref(DEFAULT_MODEL))
		String getDefaultProvider();

		/**
		 * Per-agent overrides. Agents not listed here use the {@link #getDefaultModel() default
		 * model}. Applies to all agents regardless of source (config-defined or KB).
		 *
		 * @see #AGENT_OVERRIDES
		 */
		@Name(AGENT_OVERRIDES)
		@EntryTag("agent")
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		Map<String, AgentOverride> getAgentOverrides();

		/**
		 * How many turns an agent run may take when its override names no limit of its own.
		 *
		 * <p>
		 * One turn is one call of the model, which may hold several tool calls. A run that reaches
		 * the limit stops, so it is the bound on a model that loops - and also on a long task that
		 * does not.
		 * </p>
		 *
		 * @see #MAX_TURNS
		 */
		@Name(MAX_TURNS)
		@IntDefault(AgentParams.DEFAULT_MAX_TURNS)
		@Bound(comparison = Comparision.GREATER_OR_EQUAL, value = 1)
		int getMaxTurns();

		/**
		 * The agent that serves as the main entry point when a user opens a new chat session.
		 *
		 * <p>
		 * This agent receives the initial user message and orchestrates the conversation. It may
		 * delegate to other agents via its delegations list.
		 * </p>
		 *
		 * @see #CHAT_ENTRY_POINT
		 */
		@Name(CHAT_ENTRY_POINT)
		@StringDefault(DEFAULT_CHAT_ENTRY_POINT)
		@Options(fun = AllAgentNameOptions.class)
		String getChatEntryPoint();

		/**
		 * How many agent runs this installation carries at once.
		 *
		 * <p>
		 * The number of chat workers and the length of their queue are read once per application
		 * start; a change of those two takes effect when the application is restarted.
		 * </p>
		 *
		 * @see #CAPACITY
		 */
		@Name(CAPACITY)
		CapacityConfig getCapacity();

		/**
		 * What decides, for every agent run, which tool calls need a person to confirm them.
		 *
		 * <p>
		 * The shipped default asks the tool's mode: a read runs, everything that changes something is
		 * confirmed. An application whose answer depends on what is being changed - overwriting an
		 * empty section is not overwriting what a colleague wrote - names its own implementation
		 * here.
		 * </p>
		 *
		 * @see #SECURITY_CONTEXT
		 */
		@Name(SECURITY_CONTEXT)
		@ItemDefault
		@ImplementationClassDefault(DefaultSecurityContextFactory.class)
		PolymorphicConfiguration<SecurityContextFactory> getSecurityContext();

		/**
		 * What learns of every agent run of this application, from its start to its end.
		 *
		 * <p>
		 * None by default. An observer sees what the run sees; one that passes it on outside the
		 * application must authenticate that itself - see {@link RunObserver}.
		 * </p>
		 *
		 * @see #RUN_OBSERVERS
		 */
		@Name(RUN_OBSERVERS)
		List<PolymorphicConfiguration<RunObserver>> getRunObservers();
	}

	/**
	 * The chat entry point of an installation that names none.
	 *
	 * @see Config#getChatEntryPoint()
	 */
	public static final String DEFAULT_CHAT_ENTRY_POINT = "TL_SingleAgent";

	private static volatile AgentRuntimeService _instance;

	private final InstantiationContext _context;

	private SecurityContextFactory _securityContextFactory = DefaultSecurityContextFactory.INSTANCE;

	/**
	 * Creates a new {@link AgentRuntimeService} from configuration.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The service configuration.
	 */
	@CalledByReflection
	public AgentRuntimeService(InstantiationContext context, Config<?> config) {
		super(context, config);
		_context = context;
	}

	/**
	 * The running instance, or {@code null} while the service is not started.
	 */
	public static AgentRuntimeService getInstance() {
		return _instance;
	}

	/**
	 * Whether the service is up.
	 *
	 * <p>
	 * While it is restarted it is not, and a run started in that window would get the default
	 * security context and no capacity limits. {@link AgentFactory} therefore refuses to start one.
	 * </p>
	 */
	public static boolean isRunning() {
		return _instance != null;
	}

	@Override
	protected void startUp() {
		super.startUp();

		List<String> refusals = modelRefusals(LLMProviderService.getInstance());
		if (!refusals.isEmpty()) {
			for (String refusal : refusals) {
				_context.error(refusal);
			}
			// Thrown as well as reported: the context is tested for errors before startUp runs, so a
			// report alone would let an in-app restart of this service go on with a configuration it
			// refused.
			throw new ConfigurationError(ResKey.text("The agents' model configuration cannot be routed to the"
				+ " LLM providers, so this service is not running: " + String.join(" ", refusals)));
		}

		// Resolved once: a run must not pay for instantiating it, and an application that configured a
		// broken one must learn about it when the service starts rather than at the first tool call.
		SecurityContextFactory securityContextFactory = _context.getInstance(getConfig().getSecurityContext());
		_securityContextFactory = securityContextFactory == null
			? DefaultSecurityContextFactory.INSTANCE : securityContextFactory;

		RunObservers.install(TypedConfiguration.getInstanceList(_context, getConfig().getRunObservers()));

		_instance = this;
	}

	@Override
	protected void shutDown() {
		try {
			_instance = null;
			RunObservers.install(List.of());
		} finally {
			super.shutDown();
		}
	}

	/**
	 * Everything about the model references of this configuration that must stop the service from
	 * starting: a model no provider exposes, a provider that is not configured or does not expose the
	 * model, and an unqualified model that two providers expose.
	 *
	 * @param providers
	 *        The running LLM providers; <code>null</code> refuses every model named.
	 * @return One sentence per problem, in configuration order.
	 */
	List<String> modelRefusals(LLMProviderService providers) {
		Config<?> config = getConfig();
		String defaultModel = blankToNull(config.getDefaultModel());
		String defaultProvider = blankToNull(config.getDefaultProvider());
		List<ModelRef> refs = new ArrayList<>();
		List<String> subjects = new ArrayList<>();
		subjects.add("The default model");
		refs.add(new ModelRef(defaultModel, defaultModel == null ? null : defaultProvider));
		for (Map.Entry<String, AgentOverride> entry : config.getAgentOverrides().entrySet()) {
			subjects.add("Agent '" + entry.getKey() + "'");
			refs.add(modelRefOf(entry.getValue(), defaultModel, defaultProvider));
		}
		List<String> result = new ArrayList<>();
		for (int n = 0; n < refs.size(); n++) {
			ModelRef ref = refs.get(n);
			if (ref.model() == null) {
				// No model to route; reported by the resolution itself when the agent runs.
				continue;
			}
			// An override that inherits the default is the default's problem, reported once.
			if (n > 0 && ref.equals(refs.get(0))) {
				continue;
			}
			String refusal = providers == null
				? subjects.get(n) + ": model '" + ref.model() + "' is named, but the LLM providers are not running."
				: providers.routingRefusal(subjects.get(n), ref.model(), ref.provider());
			if (refusal != null) {
				result.add(refusal);
			}
		}
		return result;
	}

	/**
	 * The agent a new chat session starts with.
	 */
	public String getChatEntryPoint() {
		return getConfig().getChatEntryPoint();
	}

	/**
	 * The configured default model, or {@code null} if none is set.
	 */
	public String getDefaultModel() {
		return blankToNull(getConfig().getDefaultModel());
	}

	/**
	 * The provider serving the {@link #getDefaultModel() default model}, or {@code null} if none is
	 * named.
	 */
	public String getDefaultProvider() {
		return blankToNull(getConfig().getDefaultProvider());
	}

	/**
	 * The configured override of the given agent, or {@code null} when it has none.
	 */
	public AgentOverride getAgentOverride(String agentName) {
		return getConfig().getAgentOverrides().get(agentName);
	}

	/**
	 * How many turns a run may take whose agent names no limit of its own.
	 */
	public int getMaxTurns() {
		return getConfig().getMaxTurns();
	}

	/**
	 * How many agent runs this installation carries at once; <code>null</code> when nothing is
	 * configured.
	 */
	public CapacityConfig getCapacity() {
		return getConfig().getCapacity();
	}

	/**
	 * What builds the {@link SecurityContext} of an agent run; never {@code null}.
	 */
	public SecurityContextFactory getSecurityContextFactory() {
		return _securityContextFactory;
	}

	/**
	 * Resolves the model for the given agent.
	 *
	 * @throws IllegalStateException
	 *         if no model can be resolved
	 * @see #resolveModelAssignment(String)
	 */
	public String resolveModel(String agentName) {
		return resolveModelAssignment(agentName).model();
	}

	/**
	 * Resolves the full model assignment for the given agent, including all tuning parameters.
	 *
	 * <p>
	 * Resolution order for the model name: the agent's {@link Config#getAgentOverrides() override},
	 * then the {@link #getDefaultModel() default model}. Tuning parameters are taken from the
	 * override if present; otherwise all are {@code null} (meaning "use defaults").
	 * </p>
	 *
	 * @param agentName
	 *        the agent name
	 * @return the full assignment including model name and all optional tuning parameters
	 * @throws IllegalStateException
	 *         if no model can be resolved
	 *
	 * @see AgentModelAssignment
	 */
	public AgentModelAssignment resolveModelAssignment(String agentName) {
		AgentOverride perAgent = getAgentOverride(agentName);
		ModelRef ref = modelRefOf(perAgent, getDefaultModel(), getDefaultProvider());
		if (ref.model() == null) {
			throw new IllegalStateException(
				"No model assigned to agent '" + agentName + "' and no default model configured.");
		}
		if (perAgent != null) {
			return new AgentModelAssignment(ref.model(), ref.provider(),
				perAgent.getMaxTokens(), perAgent.getContextWindow(), perAgent.getMaxTurns(),
				perAgent.getTemperature(), perAgent.getTopP(),
				perAgent.getThinkingEffort(), perAgent.getTopK(),
				perAgent.getReasoningEffort(), perAgent.getReasoningSummary(),
				perAgent.getToolChoice(),
				perAgent.getTimeoutSeconds(), perAgent.getMaxRetries(),
				perAgent.getMaxToolResultChars());
		}
		return new AgentModelAssignment(ref.model(), ref.provider(),
			null, null, null, null, null, null, null, null, null, null, null, null, null);
	}

	/**
	 * A model together with the provider that serves it.
	 *
	 * @param model
	 *        The model name, or {@code null} if none is configured.
	 * @param provider
	 *        The name of the provider serving the model, or {@code null} for an unqualified
	 *        reference that the model's only provider serves.
	 */
	record ModelRef(String model, String provider) {
		// A pair, nothing more.
	}

	/**
	 * The model and provider the given override effectively uses.
	 *
	 * <p>
	 * Both fields fall back to the defaults independently, with one tie: an override that names no
	 * model of its own inherits the default provider along with the default model, since a provider
	 * for a model the override does not mention would be meaningless.
	 * </p>
	 *
	 * @param override
	 *        The agent's override, or {@code null} for an agent that has none.
	 * @param defaultModel
	 *        The configured default model, or {@code null}.
	 * @param defaultProvider
	 *        The configured default provider, or {@code null}.
	 */
	static ModelRef modelRefOf(AgentOverride override, String defaultModel, String defaultProvider) {
		if (override == null) {
			return new ModelRef(blankToNull(defaultModel), blankToNull(defaultProvider));
		}
		String model = blankToNull(override.getModel());
		String provider = blankToNull(override.getProvider());
		if (model == null) {
			return new ModelRef(blankToNull(defaultModel), provider != null ? provider : blankToNull(defaultProvider));
		}
		return new ModelRef(model, provider);
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value;
	}

	/**
	 * The running instance, or {@code null} while it is not.
	 */
	private static AgentRuntimeService runningInstance() {
		return _instance;
	}

	/**
	 * The configured override of the given agent, also answered when this service is down.
	 *
	 * @return The override, or {@code null} when the agent has none or the service is not running.
	 */
	public static AgentOverride agentOverride(String agentName) {
		AgentRuntimeService service = runningInstance();
		return service == null ? null : service.getAgentOverride(agentName);
	}

	/**
	 * The agent a new chat session starts with, or {@code null} when the service is not running.
	 */
	public static String chatEntryPoint() {
		AgentRuntimeService service = runningInstance();
		return service == null ? null : service.getChatEntryPoint();
	}

	/**
	 * How many turns a run may take whose agent names no limit of its own; the shipped default when
	 * the service is not running.
	 */
	public static int maxTurns() {
		AgentRuntimeService service = runningInstance();
		return service == null ? AgentParams.DEFAULT_MAX_TURNS : service.getMaxTurns();
	}

	/**
	 * Module for {@link AgentRuntimeService}.
	 */
	public static final class Module extends TypedRuntimeModule<AgentRuntimeService> {

		/**
		 * Singleton {@link AgentRuntimeService.Module} instance.
		 */
		public static final AgentRuntimeService.Module INSTANCE = new AgentRuntimeService.Module();

		private Module() {
			// Singleton constructor
		}

		@Override
		public Class<AgentRuntimeService> getImplementation() {
			return AgentRuntimeService.class;
		}
	}
}
