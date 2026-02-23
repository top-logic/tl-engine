/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.ai.service.AIService;
import com.top_logic.ai.service.skills.SkillRegistry;
import com.top_logic.ai.service.tools.help.TLScriptReferenceTool;
import com.top_logic.ai.service.tools.model.AnnotationDefinitionExplorerTool;
import com.top_logic.ai.service.tools.model.InstanceBrowserTool;
import com.top_logic.ai.service.tools.model.ModelAnnotationBrowserTool;
import com.top_logic.ai.service.tools.model.ModelBrowserTool;
import com.top_logic.ai.service.tools.model.ModelEditorTool;
import com.top_logic.ai.service.tools.model.ModelSearchTool;
import com.top_logic.ai.service.tools.model.TypeAnalysisTool;
import com.top_logic.ai.service.tools.script.TLScriptTool;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.func.Function0;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Configuration for a single agent in the {@link AgentRegistry}.
 *
 * <p>
 * Each agent is identified by a unique name (provided by {@link NamedConfigMandatory}) and
 * defines the LLM model, optional thinking parameters, system prompt, and available tools
 * and skills.
 * </p>
 */
public interface AgentConfig extends NamedConfigMandatory {

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

	/** @see #getReturnThinking() */
	String RETURN_THINKING = "return-thinking";

	/** @see #getSendThinking() */
	String SEND_THINKING = "send-thinking";

	/** @see #getSystemPrompt() */
	String SYSTEM_PROMPT = "system-prompt";

	/** @see #getTools() */
	String TOOLS = "tools";

	/** @see #getSkills() */
	String SKILLS = "skills";

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
	 * The thinking type for extended thinking support.
	 *
	 * <p>
	 * {@link ThinkingType#ADAPTIVE} lets the model decide when to think.
	 * {@link ThinkingType#ENABLED} forces thinking on every request and requires
	 * {@link #getThinkingBudgetTokens()} to be set.
	 * </p>
	 *
	 * @return The thinking type, or {@code null} if extended thinking is not used.
	 */
	@Name(THINKING_TYPE)
	@Nullable
	ThinkingType getThinkingType();

	/**
	 * The maximum number of tokens the model may use for its internal thinking.
	 *
	 * <p>
	 * Required when {@link #getThinkingType()} is {@link ThinkingType#ENABLED}.
	 * </p>
	 *
	 * @return The thinking budget in tokens, or {@code null} if not set.
	 */
	@Name(THINKING_BUDGET_TOKENS)
	@Nullable
	Integer getThinkingBudgetTokens();

	/**
	 * The maximum number of output tokens for the model response.
	 *
	 * @return The max output tokens, or {@code null} to use the model default.
	 */
	@Name(MAX_TOKENS)
	@Nullable
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
	 * Whether to include the model's thinking in the response.
	 *
	 * <p>
	 * When {@code true}, the thinking content is included alongside the response text.
	 * </p>
	 */
	@Name(RETURN_THINKING)
	@BooleanDefault(false)
	boolean getReturnThinking();

	/**
	 * Whether to send previous thinking blocks in follow-up messages.
	 *
	 * <p>
	 * When {@code true}, the thinking content from previous turns is included in the
	 * conversation history sent to the model.
	 * </p>
	 */
	@Name(SEND_THINKING)
	@BooleanDefault(true)
	boolean getSendThinking();

	/**
	 * The system prompt that defines this agent's behavior and instructions.
	 */
	@Name(SYSTEM_PROMPT)
	@Mandatory
	@ControlProvider(MultiLineText.class)
	String getSystemPrompt();

	/**
	 * The tool configurations available to this agent.
	 *
	 * <p>
	 * Each entry is a polymorphic configuration of a tool class to be instantiated and made
	 * available to the agent during conversation.
	 * </p>
	 */
	@Name(TOOLS)
	@Subtypes({
		@Subtype(tag = "model-browser", type = ModelBrowserTool.Config.class),
		@Subtype(tag = "instance-browser", type = InstanceBrowserTool.Config.class),
		@Subtype(tag = "type-analysis", type = TypeAnalysisTool.Config.class),
		@Subtype(tag = "model-search", type = ModelSearchTool.Config.class),
		@Subtype(tag = "annotation-browser", type = ModelAnnotationBrowserTool.Config.class),
		@Subtype(tag = "annotation-explorer", type = AnnotationDefinitionExplorerTool.Config.class),
		@Subtype(tag = "model-editor", type = ModelEditorTool.Config.class),
		@Subtype(tag = "tlscript-reference", type = TLScriptReferenceTool.Config.class),
		@Subtype(tag = "tlscript", type = TLScriptTool.Config.class),
		@Subtype(tag = "exploration", type = AgentDelegationTools.Exploration.Config.class),
		@Subtype(tag = "creation", type = AgentDelegationTools.Creation.Config.class),
		@Subtype(tag = "tlscript-delegation", type = AgentDelegationTools.TLScript.Config.class),
	})
	List<PolymorphicConfiguration<?>> getTools();

	/**
	 * Comma-separated list of skill IDs available to this agent.
	 *
	 * <p>
	 * Skills are referenced by their registered name in the {@link SkillRegistry}
	 * (e.g. {@code "shared/prompting-model-explorer"}).
	 * </p>
	 */
	@Name(SKILLS)
	@Format(CommaSeparatedStrings.class)
	@Options(fun = AllSkillNames.class)
	List<String> getSkills();

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
	 * Provides all skill names from the {@link AgentRegistry}'s {@link SkillRegistry} as
	 * selection options.
	 */
	class AllSkillNames extends Function0<List<String>> {

		@Override
		public List<String> apply() {
			AgentRegistry registry = AgentRegistry.getInstance();
			if (registry == null || registry.getSkillRegistry() == null) {
				return Collections.emptyList();
			}
			return registry.getSkillRegistry().allSkillNames();
		}
	}
}
