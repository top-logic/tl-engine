/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.list;

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
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
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
 * Declarative {@code <data-list>} element: a selectable, groupable, searchable list of model
 * objects in the style of a contact or message list.
 *
 * <p>
 * The rows come from a TL-Script {@link Config#getRows() rows} expression, exactly as
 * {@code <table>} binds its rows; an {@link LineConfig#getAttribute() attribute} line resolves
 * against the row object's own type. Each row shows an ordered list of {@link LineConfig lines} (the
 * vertical counterpart of a table's columns); its group label is a further expression. A row click
 * writes the row object to the two-way {@link Config#getSelection() selection} channel. Search and
 * collapse are client-side. Unlike {@code <table>} this renders a list (design system:
 * {@code tl-list}), not a data grid; unlike {@code <object-list>} it is a selectable picker, not an
 * in-place editor of composite items. For very large sets of objects the table is the element of
 * choice: the list renders all of its rows.
 * </p>
 *
 * <p>
 * In front of the lines a row shows an avatar or an icon ({@link Config#getLeading() leading}),
 * after them metadata text or a badge ({@link Config#getTrailing() trailing}).
 * </p>
 *
 * <p>
 * Placed in a {@code <context-menu>} whose {@code input} is the {@link Config#getSelection()
 * selection} channel, the list offers the commands of the menu on a right click on a row: a row
 * outside the selection is selected first, a row inside it keeps the selection, so the menu acts on
 * the row clicked.
 * </p>
 *
 * <p>
 * A list that collapses to a strip - and opens as a drawer in a narrow window - stands in a
 * {@code <side-rail>} with one section.
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

		/** Configuration property name for {@link #getRowCssClass()}. */
		String ROW_CSS_CLASS = "row-css-class";

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

		/** Configuration property name for {@link #getTitle()}. */
		String TITLE = "title";

		/** Configuration property name for {@link #getEmptyText()}. */
		String EMPTY_TEXT = "empty-text";

		/** Configuration property name for {@link #getLeading()}. */
		String LEADING = "leading";

		/** Configuration property name for {@link #getTrailing()}. */
		String TRAILING = "trailing";

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
		 * What a section around the list reads to put a number beside its title. The list counts
		 * what it already has on the server, so the number is right before the first paint.
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
		 *
		 * <p>
		 * Not {@link #getCssClass() css-class}, which every view element has and which is the one
		 * class of the list's root element.
		 * </p>
		 */
		@Name(ROW_CSS_CLASS)
		@Label("Row CSS class")
		@Nullable
		Expr getRowCssClass();

		/**
		 * Header buttons, rendered above the list. Each is a standard view command - a
		 * {@code <generic-command>} composing any actions (execute-script, open-dialog, write-channel,
		 * with-transaction, ...), with the usual label, image, tooltip, executability and input. A list of
		 * documents, for example, configures one command that clears the {@link #getSelection()
		 * selection} channel to start a new document.
		 */
		@Name(COMMANDS)
		@EntryTag("command")
		@TreeProperty
		List<PolymorphicConfiguration<? extends ViewCommand>> getCommands();

		/**
		 * Commands offered on a single row, in a menu opened from the row itself.
		 *
		 * <p>
		 * Each is the same {@code <generic-command>} a header {@link #getCommands() command} is,
		 * with the same label, image, clique, tooltip, executability and action chain - a rename
		 * behind an {@code <open-dialog>}, a delete behind a {@code <confirm>}. The row it was
		 * opened on reaches the command through the {@link #getRowChannel() row channel}, which is
		 * what a command names in its {@code inputs}: the scripts of its chain and its
		 * {@code <executability>} rules see the row as a variable named after the channel, and the
		 * rules re-evaluate before the menu opens.
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
		@TreeProperty
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
		 * What stands in front of the lines of a row: an avatar or an icon.
		 */
		@Name(LEADING)
		@Label("Leading")
		LeadingConfig getLeading();

		/**
		 * What stands after the lines of a row: metadata text or a badge.
		 */
		@Name(TRAILING)
		@Label("Trailing")
		TrailingConfig getTrailing();
	}

	/** Container for the {@link LineConfig lines} of a {@link DataListElement}. */
	public interface LinesConfig extends ConfigurationItem {

		/** The lines to display, top to bottom. */
		@DefaultContainer
		List<LineConfig> getLines();
	}

	/**
	 * How the {@link TrailingConfig trailing} value of a row is shown.
	 */
	public enum TrailingDisplay implements ExternallyNamed {

		/** As metadata text, e.g. a date. */
		TEXT("text"),

		/**
		 * As a badge, a count or a short status. An empty value or {@code 0} shows no badge.
		 */
		BADGE("badge");

		private final String _externalName;

		TrailingDisplay(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * Configuration of what stands in front of the lines of a row: an avatar showing a picture or
	 * the initials of a name, or an icon.
	 *
	 * <p>
	 * Without {@link #getName() name} and {@link #getImage() image}, or where both yield nothing
	 * for a row, nothing stands there.
	 * </p>
	 */
	public interface LeadingConfig extends ConfigurationItem {

		/** Configuration property name for {@link #getName()}. */
		String NAME = "name";

		/** Configuration property name for {@link #getImage()}. */
		String IMAGE = "image";

		/**
		 * A TL-Script function of one row computing the name the avatar stands for: the source of
		 * its initials and of its color category, and its name for assistive technology. The result
		 * is shown by its label.
		 */
		@Name(NAME)
		@Nullable
		Expr getName();

		/**
		 * A TL-Script function of one row computing its picture or icon.
		 *
		 * <p>
		 * The result is an icon - e.g. the value of an attribute of type {@code tl.core:Icon} -, the
		 * text of an icon resource, e.g. {@code "css:fas fa-user"}, or image data: a value of type
		 * {@code tl.core:Binary} with an image content type ({@code image/...}) of at most 32 KB. An
		 * icon of an icon font is shown as icon; a picture is shown in the avatar. Any other result
		 * counts as none: the avatar shows the initials of the {@link #getName() name}.
		 * </p>
		 */
		@Name(IMAGE)
		@Nullable
		Expr getImage();
	}

	/**
	 * Configuration of what stands after the lines of a row: metadata text, or a badge.
	 *
	 * <p>
	 * The value is an {@link #getAttribute() attribute} of the row object or a
	 * {@link #getValue() computed value}, like the one of a {@link LineConfig line}.
	 * </p>
	 */
	public interface TrailingConfig extends ConfigurationItem {

		/** Configuration property name for {@link #getAttribute()}. */
		String ATTRIBUTE = "attribute";

		/** Configuration property name for {@link #getValue()}. */
		String VALUE = "value";

		/** Configuration property name for {@link #getDisplay()}. */
		String DISPLAY = "display";

		/**
		 * The model attribute shown. Use {@link #getValue()} instead for a computed value.
		 */
		@Name(ATTRIBUTE)
		@Nullable
		String getAttribute();

		/**
		 * A TL-Script function of one row computing the value shown - a formatted date, a count.
		 */
		@Name(VALUE)
		@Nullable
		Expr getValue();

		/**
		 * How the value is shown.
		 */
		@Name(DISPLAY)
		TrailingDisplay getDisplay();
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
		@NullDefault
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
		String cssClass = config.getCssClass();
		if (cssClass != null && cssClass.contains("->")) {
			// The css-class of an element is the class of its root; a function of a row there would put
			// the script text on the list and give no row a class.
			context.error("The css-class of a <data-list> is the class of the list itself, but '"
				+ cssClass + "' is a function of a row. Rename the property to "
				+ Config.ROW_CSS_CLASS + ".");
		}
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
