/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;

/**
 * Base of the chat's own small view elements, each of which hosts a rail section.
 *
 * <p>
 * A {@code <chat-rail/>} section holds one view element, and which one is the application's
 * decision - a list of its own objects, a document view, a live view of what the agent is doing.
 * The chat contributes the pieces only it can know: what the run has cited
 * ({@link ChatSourcesElement}). Anything the agent writes to the model - its task plan, say - is a
 * plain {@code <data-list>} instead. Each of these joins the conversation through {@code chat-id}
 * and may report a number on an optional {@code badge} channel, which is what the rail sums and
 * shows.
 * </p>
 *
 * <p>
 * Nothing stops one from being placed outside a rail; it is an ordinary element and renders its own
 * box wherever a view puts it.
 * </p>
 */
public abstract class ChatSectionElement implements UIElement {

	/** Configuration shared by the chat's rail-section elements. */
	public interface Config extends UIElement.Config {

		/**
		 * Configuration property name for {@link #getChatId()}.
		 *
		 * @see #getChatId()
		 */
		String CHAT_ID = "chat-id";

		/**
		 * Configuration property name for {@link #getBadge()}.
		 *
		 * @see #getBadge()
		 */
		String BADGE = "badge";

		/**
		 * Configuration property name for {@link #getReactElement()}.
		 *
		 * @see #getReactElement()
		 */
		String REACT_ELEMENT = "react-element";

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
		 * Shared key that lets this section and the rest of the chat show one conversation.
		 *
		 * Defaults to {@code default}, so unconfigured pieces on one page find each other.
		 */
		@Name(CHAT_ID)
		@Label("Chat id")
		@StringDefault("default")
		String getChatId();

		/**
		 * Optional channel this section writes its count to - how many of the things it shows there
		 * are right now.
		 *
		 * The rail reads the same channel to put the number beside the section's title and on its
		 * strip icon, and sums the sections to the total the conversation's rail button shows. Leave
		 * it empty for a section that has nothing to count.
		 */
		@Name(BADGE)
		@Label("Badge")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getBadge();

		/**
		 * Registered React element that renders this section.
		 *
		 * Hidden in the layout editor: the value must match a registration in {@code TypeScript}.
		 */
		@Name(REACT_ELEMENT)
		@Label("React element")
		@Hidden
		String getReactElement();
	}
}
