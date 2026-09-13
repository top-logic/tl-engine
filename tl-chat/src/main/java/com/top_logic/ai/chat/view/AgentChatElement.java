/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.tl.ai.core.provider.LLMService.AllAgentNameOptions;

/**
 * Declarative {@code <agent-chat/>} element for the React view layer.
 */
public class AgentChatElement implements UIElement {

	/**
	 * Amount of agent execution detail shown by the chat.
	 */
	public enum TraceMode implements ExternallyNamed {

		/** Hides execution traces. */
		NONE("none"),

		/** Shows a compact execution overview. */
		OVERVIEW("overview"),

		/** Shows the detailed execution trace. */
		DETAILED("detailed");

		private final String _externalName;

		TraceMode(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/** Configuration for {@link AgentChatElement}. */
	@TagName("agent-chat")
	public interface Config extends UIElement.Config {

		/**
		 * Configuration property name for {@link #getTraceMode()}.
		 *
		 * @see #getTraceMode()
		 */
		String TRACE_MODE = "trace-mode";

		/**
		 * Configuration property name for {@link #getPromptImproverAgent()}.
		 *
		 * @see #getPromptImproverAgent()
		 */
		String PROMPT_IMPROVER_AGENT = "prompt-improver-agent";

		@Override
		@ClassDefault(AgentChatElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * @en Amount of agent execution detail shown in the chat.
		 */
		@Name(TRACE_MODE)
		@Label("Trace mode")
		@FormattedDefault("overview")
		TraceMode getTraceMode();

		/**
		 * @en Name of the agent that improves draft prompts. Leave empty to disable prompt
		 *     improvement.
		 */
		@Name(PROMPT_IMPROVER_AGENT)
		@Label("Prompt improver agent")
		@Options(fun = AllAgentNameOptions.class)
		@Nullable
		String getPromptImproverAgent();
	}

	private final Config _config;

	/** Creates an {@link AgentChatElement}. */
	@CalledByReflection
	public AgentChatElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new AgentChatControl(context, _config.getTraceMode(), _config.getPromptImproverAgent());
	}
}
