/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.tl.ai.core.provider.LLMService.AllAgentNameOptions;

/**
 * Declarative {@code <chat-composer/>} element: the prompt field as a view element of its own.
 *
 * <p>
 * Several chat elements on one page join the same conversation through {@code chat-id}. The
 * optional {@code input} channel carries the open {@code AgentSession}. Every picker can be
 * switched off; sending still works.
 * </p>
 */
public class ChatComposerElement implements UIElement {

	/** Configuration for {@link ChatComposerElement}. */
	@TagName("chat-composer")
	public interface Config extends UIElement.Config {

		/**
		 * Configuration property name for {@link #getChatId()}.
		 *
		 * @see #getChatId()
		 */
		String CHAT_ID = "chat-id";

		/**
		 * Configuration property name for {@link #getInput()}.
		 *
		 * @see #getInput()
		 */
		String INPUT = "input";

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
		 * Configuration property name for {@link #getControls()}.
		 *
		 * @see #getControls()
		 */
		String CONTROLS = "controls";

		/**
		 * Configuration property name for {@link #getReferenceSource()}.
		 *
		 * @see #getReferenceSource()
		 */
		String REFERENCE_SOURCE = "reference-source";

		/**
		 * Configuration property name for {@link #getMenuItems()}.
		 *
		 * @see #getMenuItems()
		 */
		String MENU_ITEMS = "menu-items";

		/**
		 * Configuration property name for {@link #getReactElement()}.
		 *
		 * @see #getReactElement()
		 */
		String REACT_ELEMENT = "react-element";

		@Override
		@ClassDefault(ChatComposerElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Shared key that lets this composer, a thread and a session list show one conversation.
		 *
		 * Defaults to {@code default}, so unconfigured pieces on one page find each other.
		 */
		@Name(CHAT_ID)
		@Label("Chat id")
		@StringDefault("default")
		String getChatId();

		/**
		 * Optional channel holding the open {@code AgentSession}. The composer follows a write.
		 *
		 * Leave empty when the composer should only talk to the other chat elements through
		 * {@code chat-id}.
		 */
		@Name(INPUT)
		@Label("Input")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getInput();

		/**
		 * Name of the agent that improves draft prompts. Leave empty to disable prompt improvement.
		 */
		@Name(PROMPT_IMPROVER_AGENT)
		@Label("Prompt improver agent")
		@Options(fun = AllAgentNameOptions.class)
		@Nullable
		String getPromptImproverAgent();

		/**
		 * Optional placeholder for the draft field. Empty keeps the built-in wording.
		 */
		@Name(PLACEHOLDER)
		@Label("Placeholder")
		@Nullable
		ResKey getPlaceholder();

		/**
		 * Which pickers and footer controls the composer shows.
		 */
		@Name(CONTROLS)
		@Label("Controls")
		@ItemDefault
		Controls getControls();

		/**
		 * Registered reference-source name. Empty hides the {@code @} picker.
		 */
		@Name(REFERENCE_SOURCE)
		@Label("Reference source")
		@Nullable
		String getReferenceSource();

		/**
		 * Registered composer-menu-item names.
		 */
		@Name(MENU_ITEMS)
		@Label("Menu items")
		@Format(CommaSeparatedStrings.class)
		List<String> getMenuItems();

		/**
		 * Registered React element that renders this composer.
		 *
		 * Defaults to {@code TLChatComposer}.
		 */
		@Name(REACT_ELEMENT)
		@Label("React element")
		@StringDefault("TLChatComposer")
		String getReactElement();
	}

	/**
	 * The pickers and footer switches on the composer.
	 */
	@TagName("controls")
	public interface Controls extends ConfigurationItem {

		/**
		 * Configuration property name for {@link #getAutoApprove()}.
		 *
		 * @see #getAutoApprove()
		 */
		String AUTO_APPROVE = "auto-approve";

		/**
		 * Configuration property name for {@link #getEnterHint()}.
		 *
		 * @see #getEnterHint()
		 */
		String ENTER_HINT = "enter-hint";

		/**
		 * Configuration property name for {@link #getAgentPicker()}.
		 *
		 * @see #getAgentPicker()
		 */
		String AGENT_PICKER = "agent-picker";

		/**
		 * Configuration property name for {@link #getModelPicker()}.
		 *
		 * @see #getModelPicker()
		 */
		String MODEL_PICKER = "model-picker";

		/**
		 * Configuration property name for {@link #getThinking()}.
		 *
		 * @see #getThinking()
		 */
		String THINKING = "thinking";

		/**
		 * Configuration property name for {@link #getMcp()}.
		 *
		 * @see #getMcp()
		 */
		String MCP = "mcp";

		/**
		 * Configuration property name for {@link #getAttachments()}.
		 *
		 * @see #getAttachments()
		 */
		String ATTACHMENTS = "attachments";

		/**
		 * Configuration property name for {@link #getTracePicker()}.
		 *
		 * @see #getTracePicker()
		 */
		String TRACE_PICKER = "trace-picker";

		/**
		 * Configuration property name for {@link #getAgentSlash()}.
		 *
		 * @see #getAgentSlash()
		 */
		String AGENT_SLASH = "agent-slash";

		/**
		 * Whether the auto-approve switch is shown.
		 */
		@Name(AUTO_APPROVE)
		@Label("Auto approve")
		@BooleanDefault(true)
		boolean getAutoApprove();

		/**
		 * Whether the footer shows the Enter hint.
		 */
		@Name(ENTER_HINT)
		@Label("Enter hint")
		@BooleanDefault(true)
		boolean getEnterHint();

		/**
		 * Whether the agent picker is offered.
		 */
		@Name(AGENT_PICKER)
		@Label("Agent picker")
		@BooleanDefault(true)
		boolean getAgentPicker();

		/**
		 * Whether the model picker is offered.
		 */
		@Name(MODEL_PICKER)
		@Label("Model picker")
		@BooleanDefault(true)
		boolean getModelPicker();

		/**
		 * Whether effort levels are offered.
		 */
		@Name(THINKING)
		@Label("Thinking")
		@BooleanDefault(true)
		boolean getThinking();

		/**
		 * Whether MCP servers can be toggled.
		 */
		@Name(MCP)
		@Label("MCP")
		@BooleanDefault(true)
		boolean getMcp();

		/**
		 * Whether files can be attached.
		 */
		@Name(ATTACHMENTS)
		@Label("Attachments")
		@BooleanDefault(true)
		boolean getAttachments();

		/**
		 * Whether the trace-mode picker is offered.
		 */
		@Name(TRACE_PICKER)
		@Label("Trace picker")
		@BooleanDefault(true)
		boolean getTracePicker();

		/**
		 * Whether {@code /} on an empty draft opens the agent picker.
		 *
		 * Defaults to off: since #416 that key types a slash, the way it does in every chat
		 * product. Set true to get the old plus-menu shortcut.
		 */
		@Name(AGENT_SLASH)
		@Label("Agent slash")
		@BooleanDefault(false)
		boolean getAgentSlash();
	}

	private final Config _config;

	/** Creates a {@link ChatComposerElement}. */
	@CalledByReflection
	public ChatComposerElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new ChatComposerControl(context, _config);
	}
}
