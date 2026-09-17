/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;

/**
 * Declarative {@code <chat-session-binding/>} element: the small chat-specific glue a generic session
 * list (a {@code <grouped-table>}) cannot express by channel alone.
 *
 * <p>
 * A generic table writes the selected {@code AgentSession} onto a {@code selection} channel, and
 * {@code <chat-thread>}/{@code <chat-composer>} read it to switch the live conversation - so
 * selection needs no glue. This element supplies the two things that do:
 * </p>
 * <ol>
 * <li>a "new chat" button that resets the live conversation (the store's {@code newChat()}, which a
 * channel write cannot trigger), and</li>
 * <li>a write-back of the store's own session id onto the {@code selection} channel when the store
 * creates a session on first send or restores one on reload, so the list highlights the active
 * row.</li>
 * </ol>
 *
 * <p>
 * It joins the conversation through {@code chat-id}, like the other chat elements.
 * </p>
 */
public class ChatSessionBindingElement implements UIElement {

	/** Configuration for {@link ChatSessionBindingElement}. */
	@TagName("chat-session-binding")
	public interface Config extends UIElement.Config {

		/**
		 * Configuration property name for {@link #getChatId()}.
		 *
		 * @see #getChatId()
		 */
		String CHAT_ID = "chat-id";

		/**
		 * Configuration property name for {@link #getSelection()}.
		 *
		 * @see #getSelection()
		 */
		String SELECTION = "selection";

		/**
		 * Configuration property name for {@link #getNewChatButton()}.
		 *
		 * @see #getNewChatButton()
		 */
		String NEW_CHAT_BUTTON = "new-chat-button";

		/**
		 * Configuration property name for {@link #getCreateSignal()}.
		 *
		 * @see #getCreateSignal()
		 */
		String CREATE_SIGNAL = "create-signal";

		/**
		 * Configuration property name for {@link #getReactElement()}.
		 *
		 * @see #getReactElement()
		 */
		String REACT_ELEMENT = "react-element";

		@Override
		@ClassDefault(ChatSessionBindingElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Shared key that ties this binding to the thread, composer and session list of one
		 * conversation.
		 *
		 * Defaults to {@code default}, so unconfigured pieces on one page find each other.
		 */
		@Name(CHAT_ID)
		@Label("Chat id")
		@StringDefault("default")
		String getChatId();

		/**
		 * The {@code selection} channel the session list writes and this binding keeps in step with
		 * the live conversation.
		 */
		@Name(SELECTION)
		@Label("Selection")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getSelection();

		/**
		 * Whether the binding renders its own "new chat" button. Off by default: the button normally
		 * lives in the session list, which pulses {@link #getCreateSignal()} instead.
		 */
		@Name(NEW_CHAT_BUTTON)
		@Label("New chat button")
		@BooleanDefault(false)
		boolean getNewChatButton();

		/**
		 * Optional channel a session list pulses when its "new" button is clicked; a pulse resets the
		 * live conversation ({@code newChat()}).
		 */
		@Name(CREATE_SIGNAL)
		@Label("Create signal")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getCreateSignal();

		/**
		 * Registered React element that renders this binding.
		 *
		 * Defaults to {@code TLChatSessionBinding}.
		 */
		@Name(REACT_ELEMENT)
		@Label("React element")
		@StringDefault("TLChatSessionBinding")
		String getReactElement();
	}

	private final Config _config;

	/** Creates a {@link ChatSessionBindingElement}. */
	@CalledByReflection
	public ChatSessionBindingElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new ChatSessionBindingControl(context, _config);
	}
}
