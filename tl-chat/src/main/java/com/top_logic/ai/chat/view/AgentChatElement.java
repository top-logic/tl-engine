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
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.tl.ai.core.provider.LLMService.AllAgentNameOptions;

/**
 * Declarative {@code <agent-chat/>} element for the React view layer.
 *
 * <p>
 * The shipped component is {@code TLAgentChat}. A host that composes the chat from the exported
 * building blocks in {@code tl-chat-seams.js} registers its own element
 * ({@code register('MyChat', MyComposition)}) and names it here as {@code react-element}. The
 * control still binds the session and the tracker; the browser component still talks to
 * {@code /chat/*}.
 * </p>
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

		/**
		 * Configuration property name for {@link #getPlaceholder()}.
		 *
		 * @see #getPlaceholder()
		 */
		String PLACEHOLDER = "placeholder";

		/**
		 * Configuration property name for {@link #getReactElement()}.
		 *
		 * @see #getReactElement()
		 */
		String REACT_ELEMENT = "react-element";

		@Override
		@ClassDefault(AgentChatElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Amount of agent execution detail shown in the chat.
		 */
		@Name(TRACE_MODE)
		@Label("Trace mode")
		@FormattedDefault("overview")
		TraceMode getTraceMode();

		/**
		 * Name of the agent that improves draft prompts. Leave empty to disable prompt improvement.
		 */
		@Name(PROMPT_IMPROVER_AGENT)
		@Label("Prompt improver agent")
		@Options(fun = AllAgentNameOptions.class)
		@Nullable
		String getPromptImproverAgent();

		/**
		 * The invitation standing in the empty prompt field, in this application's own words.
		 *
		 * <p>
		 * Written here as a text per language, or as the key of one. Empty keeps the chat's own
		 * wording, which is what every application got before this option existed.
		 * </p>
		 *
		 * <p>
		 * It is the <em>idle</em> invitation only. While the agent is working the field keeps saying
		 * "type your next message", because that sentence is about the run rather than about this
		 * application, and an application that replaced it would be telling the user to start
		 * something that is already under way.
		 * </p>
		 */
		@Name(PLACEHOLDER)
		@Label("Placeholder")
		@Nullable
		ResKey getPlaceholder();

		/**
		 * Registered React element that renders this chat.
		 *
		 * Defaults to {@code TLAgentChat}. A host that called
		 * {@code register('MyChat', MyComposition)} sets this to {@code MyChat}.
		 */
		@Name(REACT_ELEMENT)
		@Label("React element")
		@StringDefault("TLAgentChat")
		String getReactElement();
	}

	private final Config _config;

	/** Creates an {@link AgentChatElement}. */
	@CalledByReflection
	public AgentChatElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new AgentChatControl(context, _config.getTraceMode(), _config.getPromptImproverAgent(),
			_config.getReactElement(), _config.getPlaceholder());
	}
}
