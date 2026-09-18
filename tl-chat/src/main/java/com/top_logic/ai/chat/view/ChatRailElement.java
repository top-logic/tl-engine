/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;

/**
 * Declarative {@code <chat-rail/>} element: the right rail and its pane as one view element.
 *
 * <p>
 * Several chat elements on one page join the same conversation through {@code chat-id}. The
 * optional {@code input} channel selects the open {@code AgentSession}. An empty section list is
 * the framework order (Fortschritt, Kontext, Quellen); named host sections come from the browser
 * registry. The pane is configuration of this element, not a fifth tag.
 * </p>
 */
public class ChatRailElement implements UIElement {

	/**
	 * How wide the pane opens when a host does not name a mode.
	 */
	public enum PaneMode implements ExternallyNamed {

		/** A record beside the thread. */
		CARD("card"),

		/** Something to read; the pane takes most of the column. */
		DOCUMENT("document");

		private final String _externalName;

		PaneMode(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/** Configuration for {@link ChatRailElement}. */
	@TagName("chat-rail")
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
		 * Configuration property name for {@link #getSections()}.
		 *
		 * @see #getSections()
		 */
		String SECTIONS = "sections";

		/**
		 * Configuration property name for {@link #getStrip()}.
		 *
		 * @see #getStrip()
		 */
		String STRIP = "strip";

		/**
		 * Configuration property name for {@link #getPaneMode()}.
		 *
		 * @see #getPaneMode()
		 */
		String PANE_MODE = "pane-mode";

		/**
		 * Configuration property name for {@link #getPaneWidth()}.
		 *
		 * @see #getPaneWidth()
		 */
		String PANE_WIDTH = "pane-width";

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
		@ClassDefault(ChatRailElement.class)
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
		 * Shared key that lets this rail, a thread and a composer show one conversation.
		 *
		 * Defaults to {@code default}, so unconfigured pieces on one page find each other.
		 */
		@Name(CHAT_ID)
		@Label("Chat id")
		@StringDefault("default")
		String getChatId();

		/**
		 * Optional channel holding the open {@code AgentSession}.
		 *
		 * The rail follows a write. Leave empty when the rail should only talk to the other chat
		 * elements through {@code chat-id}.
		 */
		@Name(INPUT)
		@Label("Input")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getInput();

		/**
		 * Ordered rail sections. Empty means the framework order.
		 */
		@Name(SECTIONS)
		@Label("Sections")
		@DefaultContainer
		@EntryTag("section")
		List<SectionConfig> getSections();

		/**
		 * Whether the 56 px icon strip is shown.
		 */
		@Name(STRIP)
		@Label("Strip")
		@BooleanDefault(true)
		boolean getStrip();

		/**
		 * Default pane mode when a host opens without one.
		 */
		@Name(PANE_MODE)
		@Label("Pane mode")
		@FormattedDefault("card")
		PaneMode getPaneMode();

		/**
		 * Pane width in pixels. Zero keeps the remembered or automatic width.
		 *
		 * Hidden in the layout editor: the user drags the pane and the width is remembered per user,
		 * so a fixed number here overrides what the user chose.
		 */
		@Name(PANE_WIDTH)
		@Label("Pane width")
		@Hidden
		@IntDefault(0)
		int getPaneWidth();

		/**
		 * Registered React element that renders this rail.
		 *
		 * Defaults to {@code TLChatRail}. Hidden in the layout editor: the value must match a
		 * registration in {@code TypeScript}.
		 */
		@Name(REACT_ELEMENT)
		@Label("React element")
		@Hidden
		@StringDefault("TLChatRail")
		String getReactElement();
	}

	/**
	 * One named block in the rail, resolved against the three framework sections and the browser
	 * registry.
	 */
	@TagName("section")
	public interface SectionConfig extends ConfigurationItem {

		/**
		 * Configuration property name for {@link #getName()}.
		 *
		 * @see #getName()
		 */
		String NAME = "name";

		/**
		 * Configuration property name for {@link #getOpen()}.
		 *
		 * @see #getOpen()
		 */
		String OPEN = "open";

		/**
		 * Configuration property name for {@link #getBadge()}.
		 *
		 * @see #getBadge()
		 */
		String BADGE = "badge";

		/**
		 * Framework name ({@code fortschritt}, {@code kontext}, {@code quellen}) or a registered
		 * host name.
		 */
		@Name(NAME)
		@Label("Name")
		@Mandatory
		String getName();

		/**
		 * Whether the section starts expanded.
		 */
		@Name(OPEN)
		@Label("Open")
		@BooleanDefault(false)
		boolean getOpen();

		/**
		 * Whether this section contributes to the rail badge.
		 */
		@Name(BADGE)
		@Label("Badge")
		@BooleanDefault(true)
		boolean getBadge();
	}

	private final Config _config;

	/** Creates a {@link ChatRailElement}. */
	@CalledByReflection
	public ChatRailElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new ChatRailControl(context, _config);
	}

	/**
	 * The three framework sections, in the order an unconfigured rail shows them. Host sections
	 * slot in between Fortschritt and Kontext in the browser, the way {@code App} places
	 * {@code railSections} today.
	 */
	public static List<String> defaultSectionNames() {
		return List.of("fortschritt", "kontext", "quellen");
	}

	/**
	 * Browser section id for an XML name. Framework names are German in XML and English in the
	 * store, matching {@code App}.
	 */
	public static String internalName(String xmlName) {
		if (xmlName == null) {
			return "";
		}
		String trimmed = xmlName.trim();
		if ("fortschritt".equals(trimmed)) {
			return "progress";
		}
		if ("kontext".equals(trimmed)) {
			return "context";
		}
		if ("quellen".equals(trimmed)) {
			return "sources";
		}
		return trimmed;
	}

	/**
	 * Drops names that have no framework section and no registered host section, with a debug
	 * line each, instead of failing the view.
	 */
	public static int ignoreUnknownSections(List<String> names, List<String> registered) {
		if (names == null || names.isEmpty()) {
			return 0;
		}
		int ignored = 0;
		List<String> known = new ArrayList<>(defaultSectionNames());
		if (registered != null) {
			known.addAll(registered);
		}
		for (String name : names) {
			if (name == null || name.isBlank()) {
				continue;
			}
			String trimmed = name.trim();
			if (known.contains(trimmed) || known.contains(internalName(trimmed))) {
				continue;
			}
			Logger.debug("Ignoring unknown chat rail section '" + trimmed + "'.", ChatRailElement.class);
			ignored++;
		}
		return ignored;
	}
}
