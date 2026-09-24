/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view.list;

import java.util.ArrayList;
import java.util.List;

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
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Declarative {@code <data-list>} element: a model-bound, styled, grouped, searchable, selectable
 * list - the presentation of a chat rail generalised to any model.
 *
 * <p>
 * The rows come from a TL-Script {@link Config#getRows() rows} expression, exactly as
 * {@code <table>} binds its rows; an {@link LineConfig#getAttribute() attribute} line resolves
 * against the row object's own type. Each row shows an ordered list of {@link LineConfig lines} (the
 * vertical counterpart of a table's columns); its group label is a further expression. A row click
 * writes the row object to the two-way {@link Config#getSelection() selection} channel. Search and
 * collapse are client-side. Unlike {@code <table>} this renders a styled list (the session-rail
 * look), not a data grid; unlike {@code <object-list>} it is a selectable picker, not an in-place
 * editor of composite items.
 * </p>
 *
 * <p>
 * <b>TEMPORARY FORK.</b> The engine has no data-bound list that renders like the navbar with
 * selection + grouping + search. This element fills that gap in {@code tl-ai}, rendered by the
 * {@code TLDataList} React component. <b>Upstream target:</b> a generic {@code <data-list>} in
 * {@code tl-layout-view}; then delete this package.
 * </p>
 */
public class DataListElement implements UIElement {

	/** How prominently a {@link LineConfig line} is shown. */
	public enum Emphasis implements ExternallyNamed {

		/** The prominent line (title style). */
		PRIMARY("primary"),

		/** A muted secondary line. */
		SECONDARY("secondary");

		private final String _externalName;

		Emphasis(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * When a row's action trigger is shown.
	 *
	 * <p>
	 * The same three answers - and the same touch caveat - as a {@code <panel hover-actions="...">}:
	 * a trigger on every row of a long list is visual noise, so it waits for the pointer or for
	 * keyboard focus. On a touch device there is no pointer, so {@link #HOVER} shows the trigger
	 * there anyway.
	 * </p>
	 */
	public enum RowActions implements ExternallyNamed {

		/** Revealed when the pointer is over the row or something in it has focus. */
		HOVER("hover"),

		/** Always visible. */
		ALWAYS("always"),

		/** Never rendered, whatever is configured. */
		NONE("none");

		private final String _externalName;

		RowActions(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/** Configuration for {@link DataListElement}. */
	@TagName("data-list")
	public interface Config extends UIElement.Config {

		/** Configuration property name for {@link #getRows()}. */
		String ROWS = "rows";

		/** Configuration property name for {@link #getInputs()}. */
		String INPUTS = "inputs";

		/** Configuration property name for {@link #getLines()}. */
		String LINES = "lines";

		/** Configuration property name for {@link #getGroupBy()}. */
		String GROUP_BY = "group-by";

		/** Configuration property name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/** Configuration property name for {@link #getCount()}. */
		String COUNT = "count";

		/** Configuration property name for {@link #getCssClass()}. */
		String CSS_CLASS = "css-class";

		/** Configuration property name for {@link #getCommands()}. */
		String COMMANDS = "commands";

		/** Configuration property name for {@link #getRowCommands()}. */
		String ROW_COMMANDS = "row-commands";

		/** Configuration property name for {@link #getRowChannel()}. */
		String ROW_CHANNEL = "row-channel";

		/** Configuration property name for {@link #getRowActions()}. */
		String ROW_ACTIONS = "row-actions";

		/** Configuration property name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		/** Configuration property name for {@link #getSearch()}. */
		String SEARCH = "search";

		/** Configuration property name for {@link #getCollapsible()}. */
		String COLLAPSIBLE = "collapsible";

		/** Configuration property name for {@link #getCollapsed()}. */
		String COLLAPSED = "collapsed";

		/** Configuration property name for {@link #getDrawerBelow()}. */
		String DRAWER_BELOW = "drawer-below";

		/** Configuration property name for {@link #getTitle()}. */
		String TITLE = "title";

		/** Configuration property name for {@link #getEmptyText()}. */
		String EMPTY_TEXT = "empty-text";

		/** Configuration property name for {@link #getReactElement()}. */
		String REACT_ELEMENT = "react-element";

		/**
		 * The element implementation.
		 *
		 * Hidden in the layout editor: the tag already fixes it, and changing it gives an element
		 * that no longer matches the options shown beside it.
		 */
		@Override
		@Hidden
		@ClassDefault(DataListElement.class)
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
		 * TL-Script function computing the row objects (a {@link java.util.Collection}), same as
		 * {@code <table>}'s {@code rows}.
		 */
		@Name(ROWS)
		@Mandatory
		@NonNullable
		Expr getRows();

		/**
		 * References to channels whose values become positional arguments to {@link #getRows()}.
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();

		/**
		 * The lines shown per row, top to bottom - the vertical counterpart of a table's columns.
		 */
		@Name(LINES)
		LinesConfig getLines();

		/**
		 * Optional TL-Script function of one row computing its group label. Rows sharing a label sit
		 * under one header, in first-appearance order. Empty means no grouping (a flat list).
		 */
		@Name(GROUP_BY)
		@Nullable
		Expr getGroupBy();

		/**
		 * Optional two-way channel holding the selected row object.
		 */
		@Name(SELECTION)
		@Label("Selection")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getSelection();

		/**
		 * Optional channel receiving how many rows the list shows, written whenever the rows change.
		 *
		 * <p>
		 * What a {@code <chat-rail/>} section reads to put a number beside its title. Unlike the
		 * chat's own sections, which count in the browser and report the number back, a list counts
		 * what it already has on the server - no round trip, and the number is right before the
		 * first paint.
		 * </p>
		 */
		@Name(COUNT)
		@Label("Count")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getCount();

		/**
		 * Optional TL-Script function of one row computing extra CSS classes for its row element.
		 *
		 * <p>
		 * The hook a view needs to show a row's <em>state</em> rather than only its text - a done
		 * step, a failed import, an overdue item. The result is written onto the row as it comes,
		 * so several classes are a space-separated string; what they look like is the application
		 * theme's business.
		 * </p>
		 */
		@Name(CSS_CLASS)
		@Label("CSS class")
		@Nullable
		Expr getCssClass();

		/**
		 * Header buttons, rendered above the list. Each is a standard view command - a
		 * {@code <generic-command>} composing any actions (execute-script, open-dialog, write-channel,
		 * with-transaction, ...), with the usual label, image, tooltip, executability and input. A chat
		 * session list, for example, configures one command that clears the {@link #getSelection()
		 * selection} channel ("New chat").
		 */
		@Name(COMMANDS)
		@EntryTag("command")
		List<PolymorphicConfiguration<? extends ViewCommand>> getCommands();

		/**
		 * Commands offered on a single row, in a menu opened from the row itself.
		 *
		 * <p>
		 * Each is the same {@code <generic-command>} a header {@link #getCommands() command} is,
		 * with the same label, image, clique, tooltip, executability and action chain - a rename
		 * behind an {@code <open-dialog>}, a delete behind a {@code <confirm>}. The row it was
		 * opened on reaches the command through the {@link #getRowChannel() row channel}, which is
		 * what a command names in its {@code input}, so an {@code <executability>} rule sees the row
		 * as well and re-evaluates before the menu opens.
		 * </p>
		 *
		 * <p>
		 * Rendered by the application's own context menu, so the entries look like every other menu
		 * in it and answer the same keys. A list placed where there is no menu to open - outside an
		 * {@code <app-shell>} - renders no trigger rather than a broken one.
		 * </p>
		 */
		@Name(ROW_COMMANDS)
		@EntryTag("command")
		List<PolymorphicConfiguration<? extends ViewCommand>> getRowCommands();

		/**
		 * Name of the local channel a {@link #getRowCommands() row command} reads its row from.
		 *
		 * <p>
		 * Registered by this list and visible only inside it, the way {@code <context-menu>}
		 * registers its {@code contextTarget}. Rename it only to avoid a clash with a channel of the
		 * enclosing view.
		 * </p>
		 */
		@Name(ROW_CHANNEL)
		@Label("Row channel")
		@StringDefault("row")
		String getRowChannel();

		/**
		 * When a row shows the trigger that opens its {@link #getRowCommands() commands}.
		 */
		@Name(ROW_ACTIONS)
		@Label("Row actions")
		RowActions getRowActions();


		/**
		 * Types whose object changes (create / update / delete) re-run {@link #getRows()}, so the
		 * list refreshes automatically.
		 */
		@Name(OBSERVED_TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getObservedTypes();

		/**
		 * Whether the list shows a search field (client-side filter on the row text).
		 */
		@Name(SEARCH)
		@Label("Search")
		@BooleanDefault(true)
		boolean getSearch();

		/**
		 * Whether the list can collapse to a narrow strip.
		 */
		@Name(COLLAPSIBLE)
		@Label("Collapsible")
		@BooleanDefault(true)
		boolean getCollapsible();

		/**
		 * Whether the list starts collapsed.
		 *
		 * <p>
		 * The first-load state, and nothing more: it applies to every page load, and a user who
		 * expands the list has it back to this on the next one. Needs {@link #getCollapsible()}, as
		 * a list that cannot collapse cannot start collapsed either.
		 * </p>
		 */
		@Name(COLLAPSED)
		@Label("Collapsed")
		@BooleanDefault(false)
		boolean getCollapsed();

		/**
		 * Width of the surrounding container, in rem, below which the list becomes a drawer.
		 *
		 * <p>
		 * Below it the list keeps only its narrow strip, and its expand arrow opens the list over its
		 * neighbour instead of taking a column. The default fits a list beside a conversation and a
		 * chat rail. Set it to what the list and its neighbours need side by side; 0 keeps a column at
		 * every width. Needs {@link #getCollapsible()}.
		 * </p>
		 */
		@Name(DRAWER_BELOW)
		@Label("Drawer below")
		@IntDefault(44)
		int getDrawerBelow();

		/**
		 * The header title shown above the list.
		 */
		@Name(TITLE)
		@Label("Title")
		@Nullable
		ResKey getTitle();

		/**
		 * The text shown when the list is empty.
		 */
		@Name(EMPTY_TEXT)
		@Label("Empty text")
		@Nullable
		ResKey getEmptyText();

		/**
		 * Registered React element that renders this list.
		 *
		 * Defaults to {@code TLDataList}. Hidden in the layout editor: the value must match a
		 * registration in {@code TypeScript}.
		 */
		@Name(REACT_ELEMENT)
		@Label("React element")
		@Hidden
		@StringDefault("TLDataList")
		String getReactElement();
	}

	/** Container for the {@link LineConfig lines} of a {@link DataListElement}. */
	public interface LinesConfig extends ConfigurationItem {

		/** The lines to display, top to bottom. */
		@DefaultContainer
		List<LineConfig> getLines();
	}

	/** Configuration for a single line of a row - like a table {@code <column>}, but stacked. */
	@TagName("line")
	public interface LineConfig extends ConfigurationItem {

		/** Configuration property name for {@link #getAttribute()}. */
		String ATTRIBUTE = "attribute";

		/** Configuration property name for {@link #getValue()}. */
		String VALUE = "value";

		/** Configuration property name for {@link #getEmphasis()}. */
		String EMPHASIS = "emphasis";

		/**
		 * The model attribute shown on this line (the normal case). Use {@link #getValue()} instead
		 * for a computed line.
		 */
		@Name(ATTRIBUTE)
		@Nullable
		String getAttribute();

		/**
		 * A TL-Script function of one row computing this line's text - a computed line (a formatted
		 * value, a count) that a plain {@link #getAttribute() attribute} cannot express.
		 */
		@Name(VALUE)
		@Nullable
		Expr getValue();

		/**
		 * How prominently the line is shown. Unset means the first line is {@link Emphasis#PRIMARY}
		 * and the rest {@link Emphasis#SECONDARY}.
		 */
		@Name(EMPHASIS)
		@Nullable
		Emphasis getEmphasis();
	}

	private final Config _config;

	private final List<ViewCommand> _commands = new ArrayList<>();

	private final List<ViewCommand.Config> _commandConfigs = new ArrayList<>();

	private final List<ViewCommand> _rowCommands = new ArrayList<>();

	private final List<ViewCommand.Config> _rowCommandConfigs = new ArrayList<>();

	/** Creates a {@link DataListElement}. */
	@CalledByReflection
	public DataListElement(InstantiationContext context, Config config) {
		_config = config;
		// Instantiate the commands here, where an InstantiationContext is available.
		instantiate(context, config.getCommands(), _commands, _commandConfigs);
		instantiate(context, config.getRowCommands(), _rowCommands, _rowCommandConfigs);
	}

	private static void instantiate(InstantiationContext context,
			List<PolymorphicConfiguration<? extends ViewCommand>> configs,
			List<ViewCommand> commands, List<ViewCommand.Config> commandConfigs) {
		for (PolymorphicConfiguration<? extends ViewCommand> commandConfig : configs) {
			ViewCommand command = context.getInstance(commandConfig);
			if (command != null && commandConfig instanceof ViewCommand.Config cmdConfig) {
				commands.add(command);
				commandConfigs.add(cmdConfig);
			}
		}
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new DataListControl(context, _config, _commands, _commandConfigs, _rowCommands,
			_rowCommandConfigs);
	}
}
