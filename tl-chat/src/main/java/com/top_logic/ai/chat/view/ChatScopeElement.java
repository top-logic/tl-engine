/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.util.Resources;

/**
 * Declarative {@code <chat>} element: the conversation its nested chat elements show.
 *
 * <p>
 * The elements a chat is built from - a {@code <chat-conversation/>}, a {@code <chat-dock>} and the
 * chat's own elements in the sections of a {@code <side-rail>} - show one conversation when they
 * agree on a {@code chat-id}. Writing that id on every one of them makes the strongest coupling on
 * the page the least visible thing about it: a typo in one attribute opens a second, empty
 * conversation and nothing says so. Wrapping them in a {@code <chat>} writes the id once and every
 * element inside inherits it, so the grouping is declared where it can be read:
 * </p>
 *
 * <pre>
 * &lt;chat chat-id="support"&gt;
 *   &lt;split-panel orientation="horizontal" resizable="true"&gt;
 *     &lt;panes&gt;
 *       &lt;pane size="100" unit="%"&gt;
 *         &lt;panel fill="true"&gt;&lt;chat-conversation input="chatSession"/&gt;&lt;/panel&gt;
 *       &lt;/pane&gt;
 *       &lt;pane size="300" unit="px"&gt;
 *         &lt;side-rail side="right"&gt;...&lt;/side-rail&gt;
 *       &lt;/pane&gt;
 *     &lt;/panes&gt;
 *   &lt;/split-panel&gt;
 * &lt;/chat&gt;
 * </pre>
 *
 * <p>
 * An element inside may still name a {@code chat-id} of its own; that is the override, and an
 * element outside every {@code <chat>} joins {@link ChatScope#DEFAULT_CHAT_ID}.
 * </p>
 *
 * <p>
 * A {@code <chat>} is a scope and nothing else: it holds exactly one element and shows it
 * unchanged, without an element of its own on the page, so that the content fills the height of
 * the place the {@code <chat>} stands in as if it stood there itself. Several elements go into a
 * layout element - a {@code <split-panel>}, a {@code <stack>} - as its only content. Any element
 * may be that content, chat element or not: a {@code <chat>} around a whole page is as legitimate
 * as one around a single conversation.
 * </p>
 */
public class ChatScopeElement extends ContainerElement {

	/** Configuration for {@link ChatScopeElement}. */
	@TagName("chat")
	public interface Config extends ContainerElement.Config {

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
		 * Hidden in the layout editor: a {@code <chat>} stores nothing of its own, and the key of
		 * its content is derived from the view context.
		 */
		@Override
		@Hidden
		String getPersonalizationKey();

		/**
		 * CSS class of the element.
		 *
		 * Hidden in the layout editor: a {@code <chat>} has no element of its own on the page that
		 * the class could be written on; its content carries a class of its own.
		 */
		@Override
		@Hidden
		String getCssClass();

		/**
		 * The one element the conversation of this {@code <chat>} applies to.
		 *
		 * <p>
		 * Exactly one element, written directly inside the {@code <chat>}. Several elements go into
		 * a layout element, e.g. a {@code <split-panel>} or a {@code <stack>}, as its only content.
		 * </p>
		 */
		@Override
		List<UIElement.Config> getChildren();

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

		int count = getChildren().size();
		if (count != 1) {
			ResKey message = I18NConstants.ERROR_CHAT_CONTENT__COUNT_LOCATION.fill(count, config.location());
			context.error(Resources.getLogInstance().getString(message),
				new ConfigurationException(message, Config.CHILDREN, Integer.toString(count)));
		}
	}

	/**
	 * The control of the one content element, created in the scope of this {@code <chat>}.
	 *
	 * <p>
	 * No control of its own wraps it, so that the content takes the place of the {@code <chat>} in
	 * the layout of its container.
	 * </p>
	 */
	@Override
	public IReactControl createControl(ViewContext context) {
		return getChildren().get(0).createControl(context.withScope(ChatScope.class, _scope));
	}
}
