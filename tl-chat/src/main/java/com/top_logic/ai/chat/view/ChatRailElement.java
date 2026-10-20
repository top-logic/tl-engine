/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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
 * element, and any element will do - a {@code <data-list>} of the agent's tasks or of the
 * application's own objects, or an element a host wrote. An empty {@code <chat-rail/>} renders an
 * empty rail.
 * </p>
 *
 * <p>
 * The {@code input} channel is the rail's second face: while it holds something, the pane is shown
 * in place of the sections - one object as its form, several as a table, and what the agent's
 * {@code present} tool put there as the table, chart or form it asked for. The pane is the same for
 * every type and every application ({@link PresentationPane}); nothing is configured for it but the
 * channel. It keeps a history: a row clicked in a table opens that object's form, and "back"
 * returns to the table as it was. Closing the pane writes {@code null} back.
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
		 * Configuration property name for {@link #getDrawerBelow()}.
		 *
		 * @see #getDrawerBelow()
		 */
		String DRAWER_BELOW = "drawer-below";

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
		 * Which conversation this rail belongs to, when it must not be the one the enclosing
		 * {@code <chat>} names.
		 *
		 * Leave it empty: a rail inside a {@code <chat>} belongs to that chat, and a rail outside
		 * every {@code <chat>} belongs to {@code default}, so the pieces of one page find each other
		 * without being told. Set it only to break out of the enclosing chat.
		 */
		@Name(CHAT_ID)
		@Label("Chat id")
		@Nullable
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
		 * Channel holding what the pane shows, empty for the sections.
		 *
		 * <p>
		 * While it holds something the pane replaces the section column: one object as its form,
		 * several as a table with the columns of their type, and what the agent presented as the
		 * table, chart or form it asked for - for any type, with nothing to configure. The pane's
		 * close button writes {@code null} back. Name the same channel as the
		 * {@code <chat-conversation present="…">} so the agent can show things here. Leave it empty
		 * for a rail without a pane.
		 * </p>
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
		 * <p>
		 * The state for a user who has never opened or closed this rail. Once the user has, their
		 * own choice is what every later page load and every new tab shows, and this flag no longer
		 * applies to them.
		 * </p>
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
		 * Pane width in pixels. Zero keeps the automatic width.
		 *
		 * <p>
		 * The width for a user who has never dragged the splitter of this rail; a user who has keeps
		 * their own. Hidden in the layout editor, because a number here is a choice about somebody
		 * else's window that the user can make better themselves.
		 * </p>
		 */
		@Name(PANE_WIDTH)
		@Label("Pane width")
		@Hidden
		@IntDefault(0)
		int getPaneWidth();

		/**
		 * Width of the surrounding container, in rem, below which the rail opens as a drawer.
		 *
		 * <p>
		 * Below it the rail keeps only its strip beside the conversation, and a section or the pane
		 * opens over the conversation instead of taking a column of its own. The default fits a rail
		 * beside a conversation and a chat list. Set it to what the rail's open column and its
		 * neighbours need side by side; 0 keeps a column at every width.
		 * </p>
		 */
		@Name(DRAWER_BELOW)
		@Label("Drawer below")
		@IntDefault(70)
		int getDrawerBelow();

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
		 * <p>
		 * The state for a user who has never collapsed or expanded this section. Once the user has,
		 * their own choice is what every later page load and every new tab shows. The choice is
		 * remembered per section {@link #getName() name}, so a section added to the rail later
		 * starts as configured even for a user who arranged the others long ago.
		 * </p>
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
		 * - a {@code <data-list count="…"/>} does - or a derived channel computes it.
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
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new ChatRailControl(context, _config, _sections);
	}
}
