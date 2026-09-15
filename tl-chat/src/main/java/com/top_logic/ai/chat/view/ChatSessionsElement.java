/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;

/**
 * Declarative {@code <chat-sessions/>} element: the chat's session list as a view element of its
 * own.
 *
 * <p>
 * Several chat elements on one page join the same conversation through {@code chat-id}. The
 * optional {@code selection} channel carries the open {@code AgentSession} object, so a host table
 * or form can write and read it. Without a channel the list still drives the shared browser store,
 * and every element on the same {@code chat-id} follows.
 * </p>
 */
public class ChatSessionsElement implements UIElement {

	/**
	 * How the session list is grouped.
	 */
	public enum Grouping implements ExternallyNamed {

		/** Groups sessions by last-updated day. */
		BY_DATE("by-date"),

		/** One flat list, newest first. */
		FLAT("flat");

		private final String _externalName;

		Grouping(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/** Configuration for {@link ChatSessionsElement}. */
	@TagName("chat-sessions")
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
		 * Configuration property name for {@link #getSearch()}.
		 *
		 * @see #getSearch()
		 */
		String SEARCH = "search";

		/**
		 * Configuration property name for {@link #getNewChat()}.
		 *
		 * @see #getNewChat()
		 */
		String NEW_CHAT = "new-chat";

		/**
		 * Configuration property name for {@link #getDelete()}.
		 *
		 * @see #getDelete()
		 */
		String DELETE = "delete";

		/**
		 * Configuration property name for {@link #getGrouping()}.
		 *
		 * @see #getGrouping()
		 */
		String GROUPING = "grouping";

		/**
		 * Configuration property name for {@link #getCollapsible()}.
		 *
		 * @see #getCollapsible()
		 */
		String COLLAPSIBLE = "collapsible";

		/**
		 * Configuration property name for {@link #getReactElement()}.
		 *
		 * @see #getReactElement()
		 */
		String REACT_ELEMENT = "react-element";

		@Override
		@ClassDefault(ChatSessionsElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Shared key that lets this list, a thread and a composer show one conversation.
		 *
		 * Defaults to {@code default}, so unconfigured pieces on one page find each other.
		 */
		@Name(CHAT_ID)
		@Label("Chat id")
		@StringDefault("default")
		String getChatId();

		/**
		 * Optional two-way channel holding the open {@code AgentSession}.
		 *
		 * Leave empty when the list should only talk to the other chat elements through
		 * {@code chat-id}.
		 */
		@Name(SELECTION)
		@Label("Selection")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getSelection();

		/**
		 * Whether the list shows a search field.
		 */
		@Name(SEARCH)
		@Label("Search")
		@BooleanDefault(true)
		boolean getSearch();

		/**
		 * Whether the list offers a command that starts a new chat.
		 */
		@Name(NEW_CHAT)
		@Label("New chat")
		@BooleanDefault(true)
		boolean getNewChat();

		/**
		 * Whether a session can be deleted from the list.
		 */
		@Name(DELETE)
		@Label("Delete")
		@BooleanDefault(true)
		boolean getDelete();

		/**
		 * How rows are grouped.
		 */
		@Name(GROUPING)
		@Label("Grouping")
		@FormattedDefault("by-date")
		Grouping getGrouping();

		/**
		 * Whether the list can collapse to a narrow strip.
		 */
		@Name(COLLAPSIBLE)
		@Label("Collapsible")
		@BooleanDefault(true)
		boolean getCollapsible();

		/**
		 * Registered React element that renders this list.
		 *
		 * Defaults to {@code TLChatSessions}.
		 */
		@Name(REACT_ELEMENT)
		@Label("React element")
		@StringDefault("TLChatSessions")
		String getReactElement();
	}

	private final Config _config;

	/** Creates a {@link ChatSessionsElement}. */
	@CalledByReflection
	public ChatSessionsElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new ChatSessionsControl(context, _config);
	}
}
