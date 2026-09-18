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
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
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
 * The rows come from a TL-Script {@link Config#getRows() rows} expression (over
 * {@link Config#getTypes() types}), exactly as {@code <table>} binds its rows. Each row shows an
 * ordered list of {@link LineConfig lines} (the vertical counterpart of a table's columns); its group
 * label is a further expression. A row click writes the row object to the two-way
 * {@link Config#getSelection() selection} channel. Search and collapse are client-side. Unlike
 * {@code <table>} this renders a styled list (the session-rail look), not a data grid; unlike
 * {@code <object-list>} it is a selectable picker, not an in-place editor of composite items.
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

	/** Configuration for {@link DataListElement}. */
	@TagName("data-list")
	public interface Config extends UIElement.Config {

		/** Configuration property name for {@link #getTypes()}. */
		String TYPES = "types";

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

		/** Configuration property name for {@link #getCommands()}. */
		String COMMANDS = "commands";


		/** Configuration property name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		/** Configuration property name for {@link #getSearch()}. */
		String SEARCH = "search";

		/** Configuration property name for {@link #getCollapsible()}. */
		String COLLAPSIBLE = "collapsible";

		/** Configuration property name for {@link #getTitle()}. */
		String TITLE = "title";

		/** Configuration property name for {@link #getEmptyText()}. */
		String EMPTY_TEXT = "empty-text";

		/** Configuration property name for {@link #getReactElement()}. */
		String REACT_ELEMENT = "react-element";

		@Override
		@ClassDefault(DataListElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Optional qualified TL type name(s) of the row objects (for {@link LineConfig#getAttribute()
		 * attribute} resolution). Same as {@code <table>}'s {@code types}.
		 */
		@Name(TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getTypes();

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
		 * Defaults to {@code TLDataList}.
		 */
		@Name(REACT_ELEMENT)
		@Label("React element")
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

	/** Creates a {@link DataListElement}. */
	@CalledByReflection
	public DataListElement(InstantiationContext context, Config config) {
		_config = config;
		// Instantiate header commands here, where an InstantiationContext is available.
		for (PolymorphicConfiguration<? extends ViewCommand> commandConfig : config.getCommands()) {
			ViewCommand command = context.getInstance(commandConfig);
			if (command != null && commandConfig instanceof ViewCommand.Config cmdConfig) {
				_commands.add(command);
				_commandConfigs.add(cmdConfig);
			}
		}
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new DataListControl(context, _config, _commands, _commandConfigs);
	}
}
