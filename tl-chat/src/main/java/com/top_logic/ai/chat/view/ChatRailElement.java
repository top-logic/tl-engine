/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;

/**
 * Declarative {@code <chat-rail/>} element: the side rail beside a conversation, as a shell whose
 * content is configured.
 *
 * <p>
 * The rail extends the chat so it can show more beside the transcript - what the agent is doing, a
 * proposed plan, the sources, a document it wrote. <b>Which</b> of those is the application's
 * decision, so this element brings no content of its own: each {@code <section>} holds one view
 * element, and any element will do - {@code <chat-progress/>}, a {@code <data-list>} of the
 * application's own objects, or an element a host wrote. An empty {@code <chat-rail/>} renders an
 * empty rail.
 * </p>
 *
 * <p>
 * The {@code input} channel is the rail's second face: while it holds an object, the {@code <pane>}
 * child is shown in place of the sections, with that object available to it over the same channel
 * (an {@code <form input="…"/>}, say). Closing the pane writes {@code null} back.
 * </p>
 *
 * <p>
 * Several chat elements on one page join the same conversation through {@code chat-id}. Two rails
 * on one {@code chat-id} are not supported: the open state and the collapsed sections are one per
 * conversation, and both rails would fight over them.
 * </p>
 *
 * <p>
 * <b>TEMPORARY FORK.</b> Nothing here is chat-specific except the {@code chat-id} binding and the
 * remembered layout. <b>Upstream target:</b> a generic side-rail shell in {@code tl-layout-view}
 * (engine hand-off E2), leaving {@code tl-chat} only the sections it contributes.
 * </p>
 */
public class ChatRailElement implements UIElement {

	/**
	 * How wide the pane opens.
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
		 * Configuration property name for {@link #getSections()}.
		 *
		 * @see #getSections()
		 */
		String SECTIONS = "sections";

		/**
		 * Configuration property name for {@link #getPane()}.
		 *
		 * @see #getPane()
		 */
		String PANE = "pane";

		/**
		 * Configuration property name for {@link #getInput()}.
		 *
		 * @see #getInput()
		 */
		String INPUT = "input";

		/**
		 * Configuration property name for {@link #getStrip()}.
		 *
		 * @see #getStrip()
		 */
		String STRIP = "strip";

		/**
		 * Configuration property name for {@link #getOpen()}.
		 *
		 * @see #getOpen()
		 */
		String OPEN = "open";

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
		 * Shared key that lets this rail, a transcript and a session list show one conversation.
		 *
		 * Defaults to {@code default}, so unconfigured pieces on one page find each other.
		 */
		@Name(CHAT_ID)
		@Label("Chat id")
		@StringDefault("default")
		String getChatId();

		/**
		 * The rail's sections, top to bottom in the order written.
		 *
		 * Written directly as {@code <section>} children of the {@code <chat-rail>}. An empty list
		 * is an empty rail; this element ships no sections of its own.
		 */
		@Name(SECTIONS)
		@Label("Sections")
		@DefaultContainer
		@EntryTag("section")
		@TreeProperty
		List<SectionConfig> getSections();

		/**
		 * What the pane shows while the {@link #getInput() input channel} holds an object.
		 *
		 * Written as a {@code <pane>} child. The element inside binds to the same channel to learn
		 * which object it is showing, e.g. {@code <pane><form input="selected"/></pane>}. Several
		 * elements are stacked.
		 */
		@Name(PANE)
		@Label("Pane")
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getPane();

		/**
		 * Channel holding the object the pane shows, empty for the sections.
		 *
		 * The same {@code input} a {@code <form>} or a {@code <switch>} takes: while it holds an
		 * object the {@link #getPane() pane} replaces the section column, and the pane's close
		 * button writes {@code null} back. Leave it empty for a rail without a pane.
		 */
		@Name(INPUT)
		@Label("Input")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getInput();

		/**
		 * Whether the strip of section icons is shown.
		 *
		 * The strip is what says what the rail holds while it is closed, and clicking an icon opens
		 * the rail on that section. Without it the only way in is the transcript's rail button.
		 */
		@Name(STRIP)
		@Label("Strip")
		@BooleanDefault(true)
		boolean getStrip();

		/**
		 * Whether the rail starts open.
		 *
		 * The first-load state only: from then on the rail is where the user last left it.
		 */
		@Name(OPEN)
		@Label("Open")
		@BooleanDefault(true)
		boolean getOpen();

		/**
		 * How wide the pane opens: a record beside the transcript, or something to read.
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

	/** One collapsible block of the rail, holding one view element. */
	@TagName("section")
	public interface SectionConfig extends ConfigurationItem {

		/**
		 * Configuration property name for {@link #getName()}.
		 *
		 * @see #getName()
		 */
		String NAME = "name";

		/**
		 * Configuration property name for {@link #getLabel()}.
		 *
		 * @see #getLabel()
		 */
		String LABEL = "label";

		/**
		 * Configuration property name for {@link #getIcon()}.
		 *
		 * @see #getIcon()
		 */
		String ICON = "icon";

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
		 * Configuration property name for {@link #getContent()}.
		 *
		 * @see #getContent()
		 */
		String CONTENT = "content";

		/**
		 * Technical name of this section, unique within the rail.
		 *
		 * It is the key under which the section remembers whether the user collapsed it, so
		 * renaming a section forgets that.
		 */
		@Name(NAME)
		@Label("Name")
		@Mandatory
		String getName();

		/**
		 * The title shown in the section's header.
		 */
		@Name(LABEL)
		@Label("Label")
		@Nullable
		ResKey getLabel();

		/**
		 * The icon shown on the strip, e.g. {@code css:bi bi-list-check}.
		 *
		 * Sections without one share a generic glyph, which says nothing about what is inside; give
		 * every section an icon of its own where the strip is shown.
		 */
		@Name(ICON)
		@Label("Icon")
		@Nullable
		ThemeImage getIcon();

		/**
		 * Whether this section starts expanded.
		 *
		 * The first-load state only: from then on the section is where the user left it.
		 */
		@Name(OPEN)
		@Label("Open")
		@BooleanDefault(false)
		boolean getOpen();

		/**
		 * Optional channel carrying how many things this section is showing.
		 *
		 * The number appears beside the section's title and on its strip icon, and the rail sums the
		 * sections into the total the transcript's rail button shows. The content element writes it
		 * - {@code <chat-progress badge="…"/>} does - or a derived channel computes it.
		 */
		@Name(BADGE)
		@Label("Badge")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getBadge();

		/**
		 * What this section shows: one view element, written as the {@code <section>}'s child.
		 *
		 * Several elements are stacked, as in a {@code <tab>}.
		 */
		@Name(CONTENT)
		@DefaultContainer
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getContent();
	}

	/**
	 * One configured section with its content elements instantiated.
	 *
	 * @param name
	 *        {@link SectionConfig#getName()}.
	 * @param label
	 *        {@link SectionConfig#getLabel()}, unresolved: the locale is a session's, not the
	 *        element's.
	 * @param icon
	 *        {@link SectionConfig#getIcon()}, or {@code null}.
	 * @param open
	 *        {@link SectionConfig#getOpen()}.
	 * @param badge
	 *        {@link SectionConfig#getBadge()}, or {@code null}.
	 * @param content
	 *        The instantiated {@link SectionConfig#getContent()} elements.
	 */
	public record Section(String name, ResKey label, ThemeImage icon, boolean open, ChannelRef badge,
			List<UIElement> content) {
		// Sole record.
	}

	private final Config _config;

	private final List<Section> _sections;

	private final List<UIElement> _pane;

	/** Creates a {@link ChatRailElement}. */
	@CalledByReflection
	public ChatRailElement(InstantiationContext context, Config config) {
		_config = config;
		_sections = new ArrayList<>();
		for (SectionConfig section : config.getSections()) {
			List<UIElement> content = section.getContent().stream()
				.map(context::getInstance)
				.collect(Collectors.toList());
			_sections.add(new Section(section.getName(), section.getLabel(), section.getIcon(),
				section.getOpen(), section.getBadge(), content));
		}
		_pane = config.getPane().stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new ChatRailControl(context, _config, _sections, _pane);
	}
}
