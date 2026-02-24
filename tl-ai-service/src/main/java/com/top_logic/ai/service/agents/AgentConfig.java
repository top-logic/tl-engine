/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.top_logic.ai.service.AIService;
import com.top_logic.ai.service.ChatModelFactory;
import com.top_logic.ai.service.tools.registry.ToolRegistry;
import com.top_logic.ai.service.providers.AnthropicChatModelFactory;
import com.top_logic.ai.service.providers.MistralChatModelFactory;
import com.top_logic.ai.service.providers.ModelCapability;
import com.top_logic.ai.service.providers.OpenAIChatModelFactory;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.Function2;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Configuration for a single agent in the {@link AgentRegistry}.
 *
 * <p>
 * Each agent is identified by a unique name (provided by {@link NamedConfigMandatory}) and defines
 * the LLM model, optional thinking parameters, system prompt, and available tools and skills.
 * </p>
 *
 * <p>
 * Thinking parameters ({@link #getThinkingType()}, {@link #getThinkingBudgetTokens()},
 * {@link #getReturnThinking()}, {@link #getSendThinking()}) are only visible when the selected
 * model supports extended thinking (currently Anthropic models). When {@link ThinkingType#ENABLED}
 * is selected, {@link #getThinkingBudgetTokens()} and {@link #getMaxTokens()} become mandatory.
 * </p>
 */
@DisplayOrder({
	AgentConfig.NAME_ATTRIBUTE,
	AgentConfig.ENTRY_POINT,
	AgentConfig.DESCRIPTION,
	AgentConfig.MODEL,
	AgentConfig.SYSTEM_PROMPT,
	AgentConfig.THINKING_TYPE,
	AgentConfig.THINKING_BUDGET_TOKENS,
	AgentConfig.RETURN_THINKING,
	AgentConfig.SEND_THINKING,
	AgentConfig.REASONING_EFFORT,
	AgentConfig.MAX_TOKENS,
	AgentConfig.TEMPERATURE,
	AgentConfig.TOP_P,
	AgentConfig.STOP_SEQUENCES,
	AgentConfig.RESPONSE_FORMAT,
	AgentConfig.TOP_K,
	AgentConfig.DISABLE_PARALLEL_TOOL_USE,
	AgentConfig.PRESENCE_PENALTY,
	AgentConfig.FREQUENCY_PENALTY,
	AgentConfig.SEED,
	AgentConfig.TIMEOUT_SECONDS,
	AgentConfig.MAX_RETRIES,
	AgentConfig.TOOLS,
	AgentConfig.DELEGATIONS,
	AgentConfig.SKILLS,
})
public interface AgentConfig extends NamedConfigMandatory {

	/** @see #getDescription() */
	String DESCRIPTION = "description";

	/** @see #getModel() */
	String MODEL = "model";

	/** @see #getThinkingType() */
	String THINKING_TYPE = "thinking-type";

	/** @see #getThinkingBudgetTokens() */
	String THINKING_BUDGET_TOKENS = "thinking-budget-tokens";

	/** @see #getMaxTokens() */
	String MAX_TOKENS = "max-tokens";

	/** @see #getTemperature() */
	String TEMPERATURE = "temperature";

	/** @see #getTopP() */
	String TOP_P = "top-p";

	/** @see #getStopSequences() */
	String STOP_SEQUENCES = "stop-sequences";

	/** @see #getTimeoutSeconds() */
	String TIMEOUT_SECONDS = "timeout-seconds";

	/** @see #getMaxRetries() */
	String MAX_RETRIES = "max-retries";

	/** @see #getResponseFormat() */
	String RESPONSE_FORMAT = "response-format";

	/** @see #getTopK() */
	String TOP_K = "top-k";

	/** @see #getDisableParallelToolUse() */
	String DISABLE_PARALLEL_TOOL_USE = "disable-parallel-tool-use";

	/** @see #getPresencePenalty() */
	String PRESENCE_PENALTY = "presence-penalty";

	/** @see #getFrequencyPenalty() */
	String FREQUENCY_PENALTY = "frequency-penalty";

	/** @see #getSeed() */
	String SEED = "seed";

	/** @see #getReturnThinking() */
	String RETURN_THINKING = "return-thinking";

	/** @see #getSendThinking() */
	String SEND_THINKING = "send-thinking";

	/** @see #getSystemPrompt() */
	String SYSTEM_PROMPT = "system-prompt";

	/** @see #getTools() */
	String TOOLS = "tools";

	/** @see #getDelegations() */
	String DELEGATIONS = "delegations";

	/** @see #getSkills() */
	String SKILLS = "skills";

	/** @see #isEntryPoint() */
	String ENTRY_POINT = "entry-point";

	/** @see #isThinkingCapable() */
	String IS_THINKING_CAPABLE = "is-thinking-capable";

	/** @see #isOpenAIProvider() */
	String IS_OPENAI_PROVIDER = "is-openai-provider";

	/** @see #isAnthropicProvider() */
	String IS_ANTHROPIC_PROVIDER = "is-anthropic-provider";

	/** @see #isMistralProvider() */
	String IS_MISTRAL_PROVIDER = "is-mistral-provider";

	/** @see #isAdaptiveThinkingCapable() */
	String IS_ADAPTIVE_THINKING_CAPABLE = "is-adaptive-thinking-capable";

	/** @see #isReasoningEffortCapable() */
	String IS_REASONING_EFFORT_CAPABLE = "is-reasoning-effort-capable";

	/** @see #getReasoningEffort() */
	String REASONING_EFFORT = "reasoning-effort";

	/**
	 * Short description of this agent's purpose.
	 *
	 * <p>
	 * Used as the tool description when other agents delegate to this agent. For example:
	 * {@code "Read-only model inspection: modules, types, parts, annotations, search"}.
	 * </p>
	 */
	@Name(DESCRIPTION)
	String getDescription();

	/**
	 * The LLM model name to use for this agent (e.g. {@code "claude-sonnet-4-20250514"}).
	 *
	 * <p>
	 * Options are populated from the models registered in {@link AIService}.
	 * </p>
	 */
	@Name(MODEL)
	@Mandatory
	@Options(fun = AllModelNames.class)
	String getModel();

	/**
	 * Whether the currently selected {@link #getModel() model} supports extended thinking.
	 *
	 * <p>
	 * This derived property is {@code true} when the model supports
	 * {@link ModelCapability#THINKING_ENABLED} or {@link ModelCapability#THINKING_ADAPTIVE}. It
	 * controls the visibility of thinking-related fields.
	 * </p>
	 */
	@Name(IS_THINKING_CAPABLE)
	@Derived(fun = IsThinkingCapable.class, args = @Ref(MODEL))
	@Hidden
	boolean isThinkingCapable();

	/**
	 * Whether the selected model belongs to the OpenAI provider.
	 */
	@Name(IS_OPENAI_PROVIDER)
	@Derived(fun = IsOpenAIProvider.class, args = @Ref(MODEL))
	@Hidden
	boolean isOpenAIProvider();

	/**
	 * Whether the selected model belongs to the Anthropic provider.
	 */
	@Name(IS_ANTHROPIC_PROVIDER)
	@Derived(fun = IsAnthropicProvider.class, args = @Ref(MODEL))
	@Hidden
	boolean isAnthropicProvider();

	/**
	 * Whether the selected model belongs to the Mistral provider.
	 */
	@Name(IS_MISTRAL_PROVIDER)
	@Derived(fun = IsMistralProvider.class, args = @Ref(MODEL))
	@Hidden
	boolean isMistralProvider();

	/**
	 * Whether the selected model supports adaptive thinking.
	 */
	@Name(IS_ADAPTIVE_THINKING_CAPABLE)
	@Derived(fun = IsAdaptiveThinkingCapable.class, args = @Ref(MODEL))
	@Hidden
	boolean isAdaptiveThinkingCapable();

	/**
	 * Whether the selected model supports reasoning effort.
	 */
	@Name(IS_REASONING_EFFORT_CAPABLE)
	@Derived(fun = IsReasoningEffortCapable.class, args = @Ref(MODEL))
	@Hidden
	boolean isReasoningEffortCapable();

	/**
	 * The thinking type for extended thinking support.
	 *
	 * <p>
	 * Only visible when the selected model supports thinking (Anthropic models).
	 * {@link ThinkingType#ADAPTIVE} lets the model decide when to think.
	 * {@link ThinkingType#ENABLED} forces thinking on every request and requires
	 * {@link #getThinkingBudgetTokens()} to be set.
	 * </p>
	 *
	 * @return The thinking type, or {@code null} if extended thinking is not used.
	 */
	@Name(THINKING_TYPE)
	@Nullable
	@DynamicMode(fun = ActiveIfThinkingCapable.class, args = @Ref(IS_THINKING_CAPABLE))
	@Options(fun = AvailableThinkingTypes.class, args = @Ref(MODEL))
	ThinkingType getThinkingType();

	/**
	 * The maximum number of tokens the model may use for its internal thinking.
	 *
	 * <p>
	 * Required when {@link #getThinkingType()} is {@link ThinkingType#ENABLED}. Only visible when
	 * the model supports thinking and a thinking type is selected.
	 * </p>
	 *
	 * @return The thinking budget in tokens, or {@code null} if not set.
	 */
	@Name(THINKING_BUDGET_TOKENS)
	@Nullable
	@DynamicMode(fun = ActiveIfThinkingConfigured.class, args = { @Ref(IS_THINKING_CAPABLE), @Ref(THINKING_TYPE) })
	@Constraint(value = ThinkingBudgetRequiredConstraint.class, args = @Ref(THINKING_TYPE))
	Integer getThinkingBudgetTokens();

	/**
	 * The maximum number of output tokens for the model response.
	 *
	 * <p>
	 * Required when {@link #getThinkingType()} is {@link ThinkingType#ENABLED}.
	 * </p>
	 *
	 * @return The max output tokens, or {@code null} to use the model default.
	 */
	@Name(MAX_TOKENS)
	@Nullable
	@Constraint(value = MaxTokensRequiredConstraint.class, args = @Ref(THINKING_TYPE))
	@Constraint(value = MaxTokensGreaterThanBudgetConstraint.class, args = @Ref(THINKING_BUDGET_TOKENS))
	Integer getMaxTokens();

	/**
	 * The sampling temperature for the model (0.0 to 1.0).
	 *
	 * <p>
	 * Lower values make the output more deterministic, higher values make it more creative.
	 * </p>
	 *
	 * @return The temperature, or {@code null} to use the model default.
	 */
	@Name(TEMPERATURE)
	@Nullable
	Double getTemperature();

	/**
	 * Top-P (nucleus sampling) limits token selection to the smallest set whose cumulative
	 * probability exceeds this threshold (0.0 to 1.0).
	 *
	 * <p>
	 * Lower values (e.g. 0.1) make the output more focused by considering only the most likely
	 * tokens. Higher values (e.g. 0.9) allow more diversity. Usually only one of
	 * {@link #getTemperature()} or Top-P should be changed at a time.
	 * </p>
	 *
	 * @return The Top-P value, or {@code null} to use the model default.
	 */
	@Name(TOP_P)
	@Nullable
	Double getTopP();

	/**
	 * Optional stop sequences that terminate generation when encountered.
	 *
	 * @return The configured stop sequences, or an empty list when not set.
	 */
	@Name(STOP_SEQUENCES)
	@Format(CommaSeparatedStrings.class)
	List<String> getStopSequences();

	/**
	 * Request timeout in seconds.
	 *
	 * @return Timeout in seconds, or {@code null} to use provider defaults.
	 */
	@Name(TIMEOUT_SECONDS)
	@Nullable
	Integer getTimeoutSeconds();

	/**
	 * Maximum retries for transient provider/API failures.
	 *
	 * @return Max retries, or {@code null} to use provider defaults.
	 */
	@Name(MAX_RETRIES)
	@Nullable
	Integer getMaxRetries();

	/**
	 * Response format preference.
	 *
	 * <p>
	 * Only visible for providers that support response-format control.
	 * </p>
	 */
	@Name(RESPONSE_FORMAT)
	@Nullable
	@DynamicMode(fun = ActiveIfOpenAiOrMistralProvider.class, args = { @Ref(IS_OPENAI_PROVIDER), @Ref(IS_MISTRAL_PROVIDER) })
	ResponseFormatOption getResponseFormat();

	/**
	 * Top-K sampling limits token selection to the K most likely next tokens.
	 *
	 * <p>
	 * Lower values (e.g. 10) make the output more focused, higher values (e.g. 100) allow more
	 * variety. Only visible for Anthropic models.
	 * </p>
	 */
	@Name(TOP_K)
	@Nullable
	@DynamicMode(fun = ActiveIfAnthropicProvider.class, args = @Ref(IS_ANTHROPIC_PROVIDER))
	Integer getTopK();

	/**
	 * Whether Anthropic may execute tool calls in parallel.
	 *
	 * <p>
	 * Only visible for Anthropic models.
	 * </p>
	 */
	@Name(DISABLE_PARALLEL_TOOL_USE)
	@BooleanDefault(false)
	@DynamicMode(fun = ActiveIfAnthropicProvider.class, args = @Ref(IS_ANTHROPIC_PROVIDER))
	boolean getDisableParallelToolUse();

	/**
	 * Presence penalty penalizes tokens that have already appeared in the text, encouraging the
	 * model to talk about new topics (-2.0 to 2.0).
	 *
	 * <p>
	 * Positive values increase the likelihood of exploring new subjects. Only visible for OpenAI
	 * models.
	 * </p>
	 */
	@Name(PRESENCE_PENALTY)
	@Nullable
	@DynamicMode(fun = ActiveIfOpenAIProvider.class, args = @Ref(IS_OPENAI_PROVIDER))
	Double getPresencePenalty();

	/**
	 * Frequency penalty penalizes tokens proportionally to how often they have already appeared,
	 * reducing repetition (-2.0 to 2.0).
	 *
	 * <p>
	 * Positive values decrease the likelihood of the model repeating the same phrases verbatim.
	 * Only visible for OpenAI models.
	 * </p>
	 */
	@Name(FREQUENCY_PENALTY)
	@Nullable
	@DynamicMode(fun = ActiveIfOpenAIProvider.class, args = @Ref(IS_OPENAI_PROVIDER))
	Double getFrequencyPenalty();

	/**
	 * Seed for deterministic output. When set, repeated requests with the same seed and parameters
	 * should return similar results.
	 *
	 * <p>
	 * Useful for testing and debugging. Only visible for OpenAI models.
	 * </p>
	 */
	@Name(SEED)
	@Nullable
	@DynamicMode(fun = ActiveIfOpenAIProvider.class, args = @Ref(IS_OPENAI_PROVIDER))
	Integer getSeed();

	/**
	 * Whether to include the model's thinking in the response.
	 *
	 * <p>
	 * Only visible when the model supports thinking and a thinking type is selected. When
	 * {@code true}, the thinking content is included alongside the response text.
	 * </p>
	 */
	@Name(RETURN_THINKING)
	@BooleanDefault(false)
	@DynamicMode(fun = ActiveIfThinkingConfigured.class, args = { @Ref(IS_THINKING_CAPABLE), @Ref(THINKING_TYPE) })
	boolean getReturnThinking();

	/**
	 * Whether to send previous thinking blocks in follow-up messages.
	 *
	 * <p>
	 * Only visible when the model supports thinking and a thinking type is selected. When
	 * {@code true}, the thinking content from previous turns is included in the conversation
	 * history sent to the model.
	 * </p>
	 */
	@Name(SEND_THINKING)
	@BooleanDefault(true)
	@DynamicMode(fun = ActiveIfThinkingConfigured.class, args = { @Ref(IS_THINKING_CAPABLE), @Ref(THINKING_TYPE) })
	boolean getSendThinking();

	/**
	 * The reasoning effort level for OpenAI reasoning models.
	 *
	 * <p>
	 * Only visible for models that support reasoning effort (OpenAI o-series, GPT-5).
	 * </p>
	 *
	 * @return The reasoning effort, or {@code null} to use the provider default.
	 */
	@Name(REASONING_EFFORT)
	@Nullable
	@DynamicMode(fun = ActiveIfReasoningEffortCapable.class, args = @Ref(IS_REASONING_EFFORT_CAPABLE))
	ReasoningEffort getReasoningEffort();

	/**
	 * The system prompt that defines this agent's behavior and instructions.
	 */
	@Name(SYSTEM_PROMPT)
	@Mandatory
	@ControlProvider(MultiLineText.class)
	String getSystemPrompt();

	/**
	 * The standard tools available to this agent, selected by name.
	 *
	 * <p>
	 * Each name maps to a well-known tool class (e.g. {@code "model-browser"},
	 * {@code "tlscript"}). Tools are instantiated at runtime by the {@link AgentRegistry}.
	 * </p>
	 */
	@Name(TOOLS)
	@Format(CommaSeparatedStrings.class)
	@Options(fun = AllToolNames.class)
	List<String> getTools();

	/**
	 * Other agents this agent can delegate to, selected by name.
	 *
	 * <p>
	 * For each target agent name listed here, a dedicated {@code delegate_to_<name>} tool with a
	 * self-describing {@link dev.langchain4j.agent.tool.ToolSpecification} is created at runtime by
	 * the {@link AgentRegistry}.
	 * </p>
	 */
	@Name(DELEGATIONS)
	@Format(CommaSeparatedStrings.class)
	@Options(fun = AllAgentNames.class)
	List<String> getDelegations();

	/**
	 * Comma-separated list of skill IDs available to this agent.
	 *
	 * <p>
	 * Skills are referenced by their registered name in the {@link AgentRegistry} configuration
	 * (e.g. {@code "shared/prompting-ModelExplorer"}).
	 * </p>
	 */
	@Name(SKILLS)
	@Format(CommaSeparatedStrings.class)
	@Options(fun = AllSkillNames.class)
	List<String> getSkills();

	/**
	 * Whether this agent is the chat entry point.
	 *
	 * <p>
	 * Exactly one agent in the {@link AgentRegistry} must be marked as the entry point. The chat
	 * system uses this agent to handle incoming user messages.
	 * </p>
	 */
	@Name(ENTRY_POINT)
	@BooleanDefault(false)
	boolean isEntryPoint();

	// --- Option provider functions ---

	/**
	 * Provides only the {@link ThinkingType} values supported by the currently selected model.
	 *
	 * <p>
	 * For models with {@link ModelCapability#THINKING_ADAPTIVE}, both values are shown. For models
	 * with only {@link ModelCapability#THINKING_ENABLED}, only {@link ThinkingType#ENABLED} is
	 * shown.
	 * </p>
	 */
	class AvailableThinkingTypes extends Function1<List<ThinkingType>, String> {

		@Override
		public List<ThinkingType> apply(String modelName) {
			Set<ModelCapability> caps = getCapabilities(modelName);
			List<ThinkingType> result = new ArrayList<>();
			if (caps.contains(ModelCapability.THINKING_ADAPTIVE)) {
				result.add(ThinkingType.ADAPTIVE);
			}
			if (caps.contains(ModelCapability.THINKING_ENABLED)) {
				result.add(ThinkingType.ENABLED);
			}
			return result;
		}
	}

	/**
	 * Provides all model names registered in {@link AIService} as selection options.
	 */
	class AllModelNames extends Function0<List<String>> {

		@Override
		public List<String> apply() {
			AIService service = AIService.getInstance();
			if (service == null) {
				return Collections.emptyList();
			}
			return new ArrayList<>(service.getModelNames());
		}
	}

	/**
	 * Provides all skill names from the {@link AgentRegistry}'s configuration as selection options.
	 */
	class AllSkillNames extends Function0<List<String>> {

		@Override
		public List<String> apply() {
			AgentRegistry registry = AgentRegistry.getInstance();
			if (registry == null) {
				return Collections.emptyList();
			}
			return new ArrayList<>(registry.getConfig().getSkills().keySet());
		}
	}

	/**
	 * Provides the well-known tool names as selection options.
	 */
	class AllToolNames extends Function0<List<String>> {

		@Override
		public List<String> apply() {
			if (!ToolRegistry.Module.INSTANCE.isActive()) {
				return Collections.emptyList();
			}
			return new ArrayList<>(ToolRegistry.getInstance().getToolNames());
		}
	}

	/**
	 * Provides all agent names from the {@link AgentRegistry} as delegation targets.
	 */
	class AllAgentNames extends Function0<List<String>> {

		@Override
		public List<String> apply() {
			AgentRegistry registry = AgentRegistry.getInstance();
			if (registry == null) {
				return Collections.emptyList();
			}
			return new ArrayList<>(registry.getConfig().getAgents().keySet());
		}
	}

	// --- Derived property functions ---

	/**
	 * Checks if the selected model supports any form of extended thinking.
	 */
	class IsThinkingCapable extends Function1<Boolean, String> {

		@Override
		public Boolean apply(String modelName) {
			Set<ModelCapability> caps = getCapabilities(modelName);
			return Boolean.valueOf(
				caps.contains(ModelCapability.THINKING_ENABLED) || caps.contains(ModelCapability.THINKING_ADAPTIVE));
		}
	}

	/**
	 * Generic provider capability check for the selected model.
	 */
	abstract class IsProviderCapable extends Function1<Boolean, String> {

		protected abstract Class<? extends ChatModelFactory> providerType();

		@Override
		public Boolean apply(String modelName) {
			ChatModelFactory factory = getFactory(modelName);
			return Boolean.valueOf(factory != null && providerType().isInstance(factory));
		}
	}

	/**
	 * Checks whether the selected model is served by {@link OpenAIChatModelFactory}.
	 */
	class IsOpenAIProvider extends IsProviderCapable {

		@Override
		protected Class<? extends ChatModelFactory> providerType() {
			return OpenAIChatModelFactory.class;
		}
	}

	/**
	 * Checks whether the selected model is served by {@link AnthropicChatModelFactory}.
	 */
	class IsAnthropicProvider extends IsProviderCapable {

		@Override
		protected Class<? extends ChatModelFactory> providerType() {
			return AnthropicChatModelFactory.class;
		}
	}

	/**
	 * Checks whether the selected model is served by {@link MistralChatModelFactory}.
	 */
	class IsMistralProvider extends IsProviderCapable {

		@Override
		protected Class<? extends ChatModelFactory> providerType() {
			return MistralChatModelFactory.class;
		}
	}

	/**
	 * Checks if the selected model supports adaptive thinking.
	 */
	class IsAdaptiveThinkingCapable extends Function1<Boolean, String> {

		@Override
		public Boolean apply(String modelName) {
			return Boolean.valueOf(getCapabilities(modelName).contains(ModelCapability.THINKING_ADAPTIVE));
		}
	}

	/**
	 * Checks if the selected model supports reasoning effort.
	 */
	class IsReasoningEffortCapable extends Function1<Boolean, String> {

		@Override
		public Boolean apply(String modelName) {
			return Boolean.valueOf(getCapabilities(modelName).contains(ModelCapability.REASONING_EFFORT));
		}
	}

	// --- DynamicMode functions ---

	/**
	 * Shows the field as {@link FieldMode#ACTIVE} when the model supports thinking,
	 * {@link FieldMode#INVISIBLE} otherwise.
	 */
	class ActiveIfThinkingCapable extends Function1<FieldMode, Boolean> {

		@Override
		public FieldMode apply(Boolean thinkingCapable) {
			return Boolean.TRUE.equals(thinkingCapable) ? FieldMode.ACTIVE : FieldMode.INVISIBLE;
		}
	}

	/**
	 * Shows the field as {@link FieldMode#ACTIVE} when the model supports thinking AND a thinking
	 * type is selected, {@link FieldMode#INVISIBLE} otherwise.
	 */
	class ActiveIfThinkingConfigured extends Function2<FieldMode, Boolean, ThinkingType> {

		@Override
		public FieldMode apply(Boolean thinkingCapable, ThinkingType thinkingType) {
			if (Boolean.TRUE.equals(thinkingCapable) && thinkingType != null) {
				return FieldMode.ACTIVE;
			}
			return FieldMode.INVISIBLE;
		}
	}

	/**
	 * Shows the field as {@link FieldMode#ACTIVE} when the model is OpenAI, invisible otherwise.
	 */
	class ActiveIfOpenAIProvider extends Function1<FieldMode, Boolean> {

		@Override
		public FieldMode apply(Boolean openAICapable) {
			return Boolean.TRUE.equals(openAICapable) ? FieldMode.ACTIVE : FieldMode.INVISIBLE;
		}
	}

	/**
	 * Shows the field as {@link FieldMode#ACTIVE} when the model is Anthropic, invisible otherwise.
	 */
	class ActiveIfAnthropicProvider extends Function1<FieldMode, Boolean> {

		@Override
		public FieldMode apply(Boolean anthropicCapable) {
			return Boolean.TRUE.equals(anthropicCapable) ? FieldMode.ACTIVE : FieldMode.INVISIBLE;
		}
	}

	/**
	 * Shows the field as {@link FieldMode#ACTIVE} when model is OpenAI or Mistral.
	 */
	class ActiveIfOpenAiOrMistralProvider extends Function2<FieldMode, Boolean, Boolean> {

		@Override
		public FieldMode apply(Boolean openAICapable, Boolean mistralCapable) {
			return (Boolean.TRUE.equals(openAICapable) || Boolean.TRUE.equals(mistralCapable))
				? FieldMode.ACTIVE
				: FieldMode.INVISIBLE;
		}
	}

	/**
	 * Shows the field when the model supports reasoning effort.
	 */
	class ActiveIfReasoningEffortCapable extends Function1<FieldMode, Boolean> {

		@Override
		public FieldMode apply(Boolean capable) {
			return Boolean.TRUE.equals(capable) ? FieldMode.ACTIVE : FieldMode.INVISIBLE;
		}
	}

	/**
	 * Response format options supported by OpenAI/Mistral builders.
	 */
	enum ResponseFormatOption {
		DEFAULT,
		TEXT,
		JSON
	}

	private static ChatModelFactory getFactory(String modelName) {
		if (modelName == null || modelName.isEmpty()) {
			return null;
		}
		AIService service = AIService.getInstance();
		if (service == null) {
			return null;
		}
		return service.getFactory(modelName);
	}

	private static Set<ModelCapability> getCapabilities(String modelName) {
		ChatModelFactory factory = getFactory(modelName);
		if (factory == null) {
			return EnumSet.noneOf(ModelCapability.class);
		}
		return factory.getModelCapabilities(modelName);
	}

	// --- Constraint algorithms ---

	/**
	 * Validates that {@link AgentConfig#getThinkingBudgetTokens()} is set when
	 * {@link AgentConfig#getThinkingType()} is {@link ThinkingType#ENABLED}.
	 */
	class ThinkingBudgetRequiredConstraint extends GenericValueDependency<Integer, ThinkingType> {

		/**
		 * Creates a new {@link ThinkingBudgetRequiredConstraint}.
		 */
		public ThinkingBudgetRequiredConstraint() {
			super(Integer.class, ThinkingType.class);
		}

		@Override
		protected void checkValue(PropertyModel<Integer> budget, PropertyModel<ThinkingType> type) {
			if (type.getValue() == ThinkingType.ENABLED && budget.getValue() == null) {
				budget.setProblemDescription(I18NConstants.THINKING_BUDGET_REQUIRED_WHEN_ENABLED);
			}
		}
	}

	/**
	 * Validates that {@link AgentConfig#getMaxTokens()} is set when
	 * {@link AgentConfig#getThinkingType()} is {@link ThinkingType#ENABLED}.
	 */
	class MaxTokensRequiredConstraint extends GenericValueDependency<Integer, ThinkingType> {

		/**
		 * Creates a new {@link MaxTokensRequiredConstraint}.
		 */
		public MaxTokensRequiredConstraint() {
			super(Integer.class, ThinkingType.class);
		}

		@Override
		protected void checkValue(PropertyModel<Integer> maxTokens, PropertyModel<ThinkingType> type) {
			if (type.getValue() == ThinkingType.ENABLED && maxTokens.getValue() == null) {
				maxTokens.setProblemDescription(I18NConstants.MAX_TOKENS_REQUIRED_WHEN_THINKING_ENABLED);
			}
		}
	}

	/**
	 * Validates that {@link AgentConfig#getMaxTokens()} is greater than
	 * {@link AgentConfig#getThinkingBudgetTokens()} when both are set.
	 */
	class MaxTokensGreaterThanBudgetConstraint extends GenericValueDependency<Integer, Integer> {

		/**
		 * Creates a new {@link MaxTokensGreaterThanBudgetConstraint}.
		 */
		public MaxTokensGreaterThanBudgetConstraint() {
			super(Integer.class, Integer.class);
		}

		@Override
		protected void checkValue(PropertyModel<Integer> maxTokens, PropertyModel<Integer> budget) {
			Integer maxVal = maxTokens.getValue();
			Integer budgetVal = budget.getValue();
			if (maxVal != null && budgetVal != null && maxVal <= budgetVal) {
				maxTokens.setProblemDescription(I18NConstants.MAX_TOKENS_MUST_EXCEED_THINKING_BUDGET);
			}
		}
	}

	/**
	 * Validates that {@link ThinkingType#ADAPTIVE} is only selected when the model supports it.
	 *
	 * <p>
	 * Note: This constraint is <b>not</b> attached to the property annotation because the
	 * framework defaults nullable enum properties to their first constant
	 * ({@link ThinkingType#ADAPTIVE}) even when the user did not set a value. Validation happens at
	 * runtime in {@link AgentRegistry} instead.
	 * </p>
	 */
	class ThinkingTypeSupportsAdaptiveConstraint extends GenericValueDependency<ThinkingType, Boolean> {

		/**
		 * Creates a new {@link ThinkingTypeSupportsAdaptiveConstraint}.
		 */
		public ThinkingTypeSupportsAdaptiveConstraint() {
			super(ThinkingType.class, Boolean.class);
		}

		@Override
		protected void checkValue(PropertyModel<ThinkingType> thinkingType, PropertyModel<Boolean> adaptiveCapable) {
			if (thinkingType.getValue() == ThinkingType.ADAPTIVE
				&& !Boolean.TRUE.equals(adaptiveCapable.getValue())) {
				thinkingType.setProblemDescription(I18NConstants.ADAPTIVE_THINKING_NOT_SUPPORTED);
			}
		}
	}
}
