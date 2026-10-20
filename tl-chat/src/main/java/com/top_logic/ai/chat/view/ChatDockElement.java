/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * Declarative {@code <chat-dock/>} element: a floating launcher that opens the chat over the page.
 *
 * <p>
 * The dock is a frame and nothing else. Closed it costs one button in the corner of the window;
 * opened it shows, in a card over the page, whatever the application nested inside it - normally
 * one {@code <chat>} holding a session list, a {@code <chat-conversation/>} and a
 * {@code <chat-rail/>}, but any view element will do. What the assistant is <em>made of</em> stays
 * the application's decision, exactly as it is on a page of its own.
 * </p>
 *
 * <p>
 * Nothing reflows when it opens: the panel is a fixed-position card over the work area, so no pane
 * appears, no table gets narrower, and closing it puts the page back untouched. The page behind
 * stays usable while the dock is open.
 * </p>
 *
 * <p>
 * <b>The dock contains the chat, so it cannot inherit from it.</b> Every other chat element takes
 * its conversation from the enclosing {@code <chat>}, but a scope flows downward only and here the
 * {@code <chat>} is the child. The dock therefore repeats the id:
 * </p>
 *
 * <pre>
 * &lt;chat-dock chat-id="assistant" icon="css:bi bi-stars"&gt;
 *   &lt;label&gt;&lt;en&gt;Assistant&lt;/en&gt;&lt;de&gt;Assistent&lt;/de&gt;&lt;/label&gt;
 *   &lt;chat chat-id="assistant" direction="row"&gt;
 *     &lt;panel fill="true"&gt;&lt;chat-conversation input="dockSession"/&gt;&lt;/panel&gt;
 *     &lt;chat-rail&gt;...&lt;/chat-rail&gt;
 *   &lt;/chat&gt;
 * &lt;/chat-dock&gt;
 * </pre>
 *
 * <p>
 * The two ids must agree, and a mismatch is reported rather than silent: {@link ChatJoinLog} warns
 * about a window whose chat ids do not line up, which is the shape a typo here produces.
 * </p>
 *
 * <p>
 * The dock needs the id for one thing only - the launcher's unseen count and its busy dot, which it
 * reads from the browser store of that conversation, the same store the transcript's rail button
 * reads. Neither gets a channel: they change many times a second while the agent works, and a
 * command round trip for a dot is not what a channel is for.
 * </p>
 *
 * <p>
 * <b>TEMPORARY FORK.</b> Nothing about a floating launcher is chat-specific except where its badge
 * comes from. <b>Upstream target:</b> a generic {@code <dock>} shell in {@code tl-layout-view} that
 * hosts any element and takes its badge from a channel, leaving {@code tl-chat} the store-bound
 * launcher only.
 * </p>
 */
public class ChatDockElement implements UIElement {

	/** Configuration for {@link ChatDockElement}. */
	@TagName("chat-dock")
	public interface Config extends UIElement.Config {

		/**
		 * Configuration property name for {@link #getChatId()}.
		 *
		 * @see #getChatId()
		 */
		String CHAT_ID = "chat-id";

		/**
		 * Configuration property name for {@link #getContent()}.
		 *
		 * @see #getContent()
		 */
		String CONTENT = "content";

		/**
		 * Configuration property name for {@link #getIcon()}.
		 *
		 * @see #getIcon()
		 */
		String ICON = "icon";

		/**
		 * Configuration property name for {@link #getLabel()}.
		 *
		 * @see #getLabel()
		 */
		String LABEL = "label";

		/**
		 * Configuration property name for {@link #getOpen()}.
		 *
		 * @see #getOpen()
		 */
		String OPEN = "open";

		/**
		 * Configuration property name for {@link #getReactElement()}.
		 *
		 * @see #getReactElement()
		 */
		String REACT_ELEMENT = "react-element";

		/**
		 * The element implementation.
		 *
		 * Hidden in the layout editor: the tag already fixes it, and changing it gives an element
		 * that no longer matches the options shown beside it.
		 */
		@Override
		@Hidden
		@ClassDefault(ChatDockElement.class)
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
		 * Which conversation the launcher reports on.
		 *
		 * <p>
		 * Unlike every other chat element this one has to be told, because it <em>contains</em> the
		 * {@code <chat>} rather than sitting inside it, and a chat scope reaches descendants only.
		 * Write the same id here that the nested {@code <chat>} carries. Left empty the dock joins
		 * {@code default}, which is right for a page whose chat names no id either.
		 * </p>
		 */
		@Name(CHAT_ID)
		@Label("Chat id")
		@Nullable
		String getChatId();

		/**
		 * What the panel shows, written as the {@code <chat-dock>}'s children.
		 *
		 * <p>
		 * Any view element; normally one {@code <chat>} holding the conversation and whatever else
		 * the application wants beside it. Write <b>one</b> element: the engine's fill rule
		 * ({@code .tlStack:has(> .tlPanel--fill)}) looks one level deep, so a second child adds a
		 * wrapping stack and the conversation loses the bounded height it scrolls inside.
		 * </p>
		 */
		@Name(CONTENT)
		@Label("Content")
		@DefaultContainer
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getContent();

		/**
		 * The glyph on the launcher button, e.g. {@code css:bi bi-stars}.
		 *
		 * The same vocabulary a {@code <nav-item>} or a rail {@code <section>} uses. While the panel
		 * is open the button shows a close cross instead, so this is the closed state's icon.
		 */
		@Name(ICON)
		@Label("Icon")
		@FormattedDefault("css:bi bi-chat-dots")
		ThemeImage getIcon();

		/**
		 * What the launcher is called: its tooltip and its accessible name.
		 *
		 * The one word a user reads about the dock, so name the assistant rather than the mechanism
		 * - "Assistant", not "Open the chat panel". Left empty the chat's own wording is used.
		 */
		@Name(LABEL)
		@Label("Label")
		@Nullable
		ResKey getLabel();

		/**
		 * Whether the panel is already open when the page is first shown.
		 *
		 * <p>
		 * The first-load state, and nothing more: the dock does not remember what the user did with
		 * it, so every page load starts here again.
		 * </p>
		 */
		@Name(OPEN)
		@Label("Open")
		@BooleanDefault(false)
		boolean getOpen();

		/**
		 * Registered React element that renders this dock.
		 *
		 * Defaults to {@code TLChatDock}. Hidden in the layout editor: the value must match a
		 * registration in {@code TypeScript}.
		 */
		@Name(REACT_ELEMENT)
		@Label("React element")
		@Hidden
		@StringDefault("TLChatDock")
		String getReactElement();
	}

	private final Config _config;

	private final List<UIElement> _content;

	/** Creates a {@link ChatDockElement}. */
	@CalledByReflection
	public ChatDockElement(InstantiationContext context, Config config) {
		_config = config;
		_content = config.getContent().stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new ChatDockControl(context, _config, _content);
	}
}
