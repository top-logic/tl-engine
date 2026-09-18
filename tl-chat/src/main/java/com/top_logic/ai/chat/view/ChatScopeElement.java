/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.element.StackElement;

/**
 * Declarative {@code <chat>} element: the conversation its nested chat elements show.
 *
 * <p>
 * The elements a chat is built from - a {@code <chat-conversation/>}, a {@code <chat-rail/>} and
 * the sections in it - show one conversation when they agree on a {@code chat-id}. Writing that id
 * on every one of them makes the strongest coupling on the page the least visible thing about it:
 * a typo in one attribute opens a second, empty conversation and nothing says so. Wrapping them in
 * a {@code <chat>} writes the id once and every element inside inherits it, so the grouping is
 * declared where it can be read:
 * </p>
 *
 * <pre>
 * &lt;chat chat-id="support" direction="row"&gt;
 *   &lt;panel fill="true"&gt;&lt;chat-conversation input="chatSession"/&gt;&lt;/panel&gt;
 *   &lt;chat-rail&gt;...&lt;/chat-rail&gt;
 * &lt;/chat&gt;
 * </pre>
 *
 * <p>
 * An element inside may still name a {@code chat-id} of its own; that is the override, and an
 * element outside every {@code <chat>} joins {@link ChatScope#DEFAULT_CHAT_ID}.
 * </p>
 *
 * <p>
 * Visually this is a {@code <stack>} and nothing more: it lays its children out in a flexbox with
 * the configured {@link Config#getDirection() direction}, {@link Config#getGap() gap} and
 * {@link Config#getAlign() alignment}, and draws no chrome of its own. Any element may sit inside,
 * chat element or not - a {@code <chat>} around a whole page is as legitimate as one around two
 * elements.
 * </p>
 */
public class ChatScopeElement extends StackElement {

	/** Configuration for {@link ChatScopeElement}. */
	@TagName("chat")
	public interface Config extends StackElement.Config {

		/**
		 * Configuration property name for {@link #getChatId()}.
		 *
		 * @see #getChatId()
		 */
		String CHAT_ID = "chat-id";

		/**
		 * The element implementation.
		 *
		 * Hidden in the layout editor: the tag already fixes it, and changing it gives an element
		 * that no longer matches the options shown beside it.
		 */
		@Override
		@Hidden
		@ClassDefault(ChatScopeElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Optional override for the auto-derived personalization key.
		 *
		 * Hidden in the layout editor: the key derived from the view context is the right one, and a
		 * hand-written value silently moves what a user has personalized somewhere else.
		 */
		@Override
		@Hidden
		String getPersonalizationKey();

		/**
		 * The conversation the chat elements inside this one show.
		 *
		 * Every nested element that names no {@code chat-id} of its own joins this one. Defaults to
		 * {@code default}, which is also what an element outside every {@code <chat>} joins, so a
		 * page with a single conversation needs no id at all. Give one where a page holds two
		 * chats, so that each says which is which.
		 */
		@Name(CHAT_ID)
		@Label("Chat id")
		@StringDefault(ChatScope.DEFAULT_CHAT_ID)
		String getChatId();
	}

	private final ChatScope _scope;

	/** Creates a {@link ChatScopeElement}. */
	@CalledByReflection
	public ChatScopeElement(InstantiationContext context, Config config) {
		super(context, config);
		_scope = new ChatScope(config.getChatId());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return super.createControl(context.withScope(ChatScope.class, _scope));
	}
}
