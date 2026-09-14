/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.NonNegative;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.dnd.DropTarget;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.form.FormCommandModel;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormModel;
import com.top_logic.layout.view.form.QueryRowSetBinding;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetBinding;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.layout.view.model.TableSelectionBinding;
import com.top_logic.layout.view.table.ColumnBinding;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.layout.view.table.DeclaredFilters;
import com.top_logic.layout.view.table.DropTargetMode;
import com.top_logic.layout.view.table.FilterStateConfig;
import com.top_logic.layout.view.table.FilterStateTemplate;
import com.top_logic.layout.view.table.RowCommandColumn;
import com.top_logic.layout.view.table.TableDropBinding;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortDirection;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableId;
import com.top_logic.table.NamedFilter;
import com.top_logic.table.NamedFilterStore;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.table.impl.PersonalConfigNamedFilterStore;
import com.top_logic.table.impl.PersonalConfigViewStateStore;

/**
 * Declarative {@link UIElement} that renders a model-defined table (the {@code <table>} tag) through
 * the green-field table model ({@link com.top_logic.table.TableView}) via a {@link TableViewControl}.
 *
 * <p>
 * Input data comes from {@link ViewChannel}s, rows are computed by a TL-Script expression, and each
 * configured column reads a model attribute. Columns are sortable and (per-column) filterable.
 * </p>
 *
 * <p>
 * What the user personalizes about a table - the column order, the column widths, which columns are
 * displayed, the sort order - and the filters the user saves under a name are stored under the
 * element's personalization key. Without a configured one, that key is the table's structural
 * signature: its row types plus the attributes of its columns. That signature changes whenever a
 * column is added or removed, and everything the users of the table personalized - their saved
 * filters included - is then left behind. Setting {@code personalization-key} gives the table an
 * identity of its own that survives such an edit of the view, so set it on every table whose
 * personalization is meant to last.
 * </p>
 *
 * @implNote {@link #tableId()} derives the {@link TableId} from
 *           {@link UIElement.Config#getPersonalizationKey()} when one is configured, and from the
 *           structural signature otherwise.
 */
@InApp
public class TableElement implements UIElement {

	/**
	 * Configuration for {@link TableElement}.
	 */
	@TagName("table")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(TableElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getInputs()}. */
		String INPUTS = "inputs";

		/** Configuration name for {@link #getRows()}. */
		String ROWS = "rows";

		/** Configuration name for {@link #getTypes()}. */
		String TYPES = "types";

		/** Configuration name for {@link #getColumns()}. */
		String COLUMNS = "columns";

		/** Configuration name for {@link #getFixedColumns()}. */
		String FIXED_COLUMNS = "fixed-columns";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/** Configuration name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		/** Configuration name for {@link #getOnActivate()}. */
		String ON_ACTIVATE = "on-activate";

		/** Configuration name for {@link #getActivationButton()}. */
		String ACTIVATION_BUTTON = "activation-button";

		/** Configuration name for {@link #getGroupBy()}. */
		String GROUP_BY = "group-by";

		/** Configuration name for {@link #getRowEdit()}. */
		String ROW_EDIT = "row-edit";

		/** Configuration name for {@link #getCreateType()}. */
		String CREATE_TYPE = "create-type";

		/** Configuration name for {@link #getOnRemove()}. */
		String ON_REMOVE = "on-remove";

		/** Configuration name for {@link #getFilterBar()}. */
		String FILTER_BAR = "filter-bar";

		/** Configuration name for {@link #getPresets()}. */
		String PRESETS = "presets";

		/** Configuration name for {@link #getDrag()}. */
		String DRAG = "drag";

		/** Configuration name for {@link #getDrops()}. */
		String DROPS = "drops";

		/**
		 * Optional qualified TL type name(s) of the row objects, used to resolve column
		 * labels from the model attributes. When unset, the type is derived from the first
		 * row.
		 */
		@Name(TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getTypes();

		/**
		 * References to {@link ViewChannel}s whose values become positional arguments to
		 * {@link #getRows()}.
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();

		/**
		 * TL-Script function computing the row objects (a {@link Collection}).
		 */
		@Name(ROWS)
		@Mandatory
		@NonNullable
		Expr getRows();

		/**
		 * The columns to display, in display order.
		 */
		@Name(COLUMNS)
		ColumnsConfig getColumns();

		/**
		 * How many of the leading columns stay in place while the table is scrolled horizontally.
		 *
		 * <p>
		 * With a table wider than its viewport, the columns beyond these disappear behind them. The
		 * user can move the boundary (by dragging it, or through the column header's context menu);
		 * the value configured here is what a table starts with, until a personalization of its own
		 * exists. Zero (default) keeps no column in place.
		 * </p>
		 */
		@Name(FIXED_COLUMNS)
		int getFixedColumns();

		/**
		 * Optional {@link ViewChannel} to write the selected row object(s) to.
		 */
		@Name(SELECTION)
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();

		/**
		 * The command a row activation runs - a double-click on the row, or {@code Enter} while the
		 * row carries the keyboard cursor.
		 *
		 * <p>
		 * The activated row becomes the table's selection first, then the command runs with that row
		 * as its input. The command's own executability rules decide over the row, so a row the
		 * rules reject activates nothing. Without a command, activating a row only selects it.
		 * </p>
		 *
		 * <p>
		 * Configured as {@code <on-activate class="..." .../>} inside the {@code <table>} element.
		 * </p>
		 */
		@Name(ON_ACTIVATE)
		@Nullable
		@Options(fun = AllInAppImplementations.class)
		PolymorphicConfiguration<? extends ViewCommand> getOnActivate();

		/**
		 * Whether every row shows a button running the {@link #getOnActivate() activation command}
		 * for it.
		 *
		 * <p>
		 * The button sits in a column of its own at the right edge of the table, which stays there
		 * while the table is scrolled. It offers the row activation where the gestures running it -
		 * a double-click, {@code Enter} on the cursor row - are neither visible nor available: a
		 * touch device has neither. Each button follows the command's executability for its own
		 * row, so a row the rules reject shows a disabled button and a row they hide shows none.
		 * </p>
		 *
		 * <p>
		 * Switch it off for a table whose rows are opened another way - a link in a cell, a command
		 * of the surrounding toolbar. A table without an activation command shows no button in any
		 * case.
		 * </p>
		 */
		@Name(ACTIVATION_BUTTON)
		@BooleanDefault(true)
		boolean getActivationButton();

		/**
		 * The name of the column whose value the rows are initially grouped by: one collapsible
		 * header row per value, showing that value, how many rows it holds and what the other
		 * columns aggregate over them.
		 *
		 * <p>
		 * The user regroups the table from a column header or from the column selection, and that
		 * choice is kept under the table's personalization key, so this is the grouping a table
		 * starts with until a personalization of its own exists. Unset (default) starts ungrouped.
		 * Rows are grouped by one column at a time.
		 * </p>
		 */
		@Name(GROUP_BY)
		@Nullable
		String getGroupBy();

		/**
		 * Types whose object changes (create / update / delete) trigger a re-evaluation of the
		 * {@link #getRows() rows}, so the table refreshes automatically. Empty (default) keeps the
		 * table static.
		 */
		@Name(OBSERVED_TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getObservedTypes();

		/**
		 * Which rows are editable while the enclosing form is in edit mode.
		 *
		 * <p>
		 * When set, the table must be nested inside a form; cell edits buffer on row overlays and
		 * are committed together with the form's save. By default the table is read-only.
		 * </p>
		 */
		@Name(ROW_EDIT)
		RowEditPolicy getRowEdit();

		/**
		 * The concrete type instantiated when a new row is created in edit mode.
		 *
		 * <p>
		 * When unset, the table offers no row creation.
		 * </p>
		 */
		@Name(CREATE_TYPE)
		TLModelPartRef getCreateType();

		/**
		 * What removing a row means when the form is saved.
		 *
		 * <p>
		 * When unset, the table offers no row removal.
		 * </p>
		 */
		@Name(ON_REMOVE)
		RowSetBinding.RemoveMode getOnRemove();

		/**
		 * Whether the table shows its filter bar: the named filters it offers as chips, a search
		 * field examining the displayed columns, and the option to save the current filter under a
		 * name of the user's own.
		 *
		 * <p>
		 * A table that declares {@link #getPresets() presets} shows the bar in any case - that is
		 * where the presets are offered.
		 * </p>
		 */
		@Name(FILTER_BAR)
		boolean getFilterBar();

		/**
		 * Filter criteria this table offers under a name, displayed as chips in the filter bar.
		 *
		 * <p>
		 * A preset is applied - and dropped again - in one click, and the bar marks the preset whose
		 * criteria the table currently filters by. The user's own saved filters are offered next to
		 * them; they are kept under the table's personalization key, so a table offering presets
		 * should configure {@code personalization-key} as well.
		 * </p>
		 */
		@Name(PRESETS)
		PresetsConfig getPresets();

		/**
		 * Makes the rows of this table draggable, so they can be dropped on a display that accepts
		 * their type.
		 *
		 * <p>
		 * Dragging a selected row drags the whole selection, an unselected row drags itself. Unset
		 * (default) leaves the rows undraggable.
		 * </p>
		 */
		@Name(DRAG)
		DragConfig getDrag();

		/**
		 * What this table accepts a drop of, and what it does with the dropped objects.
		 *
		 * <p>
		 * A drop is applied by the first declared entry that accepts it, so a table can accept
		 * several kinds of object - and accept one of them on its rows and another as a whole.
		 * Empty (default) leaves the table accepting no drop.
		 * </p>
		 */
		@Name(DROPS)
		@DefaultContainer
		List<DropConfig> getDrops();
	}

	/**
	 * Configuration of the {@code <drag>} of a {@link TableElement}: that its rows may be dragged,
	 * and what they are announced as.
	 */
	public interface DragConfig extends ConfigurationItem {

		/** Configuration name for {@link #getType()}. */
		String TYPE = "type";

		/**
		 * The type the dragged rows are announced as, which a {@link DropConfig#getAccept() drop}
		 * accepts them by.
		 *
		 * <p>
		 * Defaults to the first of the table's {@link Config#getTypes() declared row types}. A table
		 * declaring neither is a configuration error: nothing would say what its rows are.
		 * </p>
		 */
		@Name(TYPE)
		TLModelPartRef getType();
	}

	/**
	 * Configuration of one {@code <drop>} of a {@link TableElement}: what it accepts, what it
	 * targets, and what it does.
	 */
	@TagName("drop")
	public interface DropConfig extends ConfigurationItem {

		/** Configuration name for {@link #getAccept()}. */
		String ACCEPT = "accept";

		/** Configuration name for {@link #getTarget()}. */
		String TARGET = "target";

		/** Configuration name for {@link #getTargetChannel()}. */
		String TARGET_CHANNEL = "target-channel";

		/** Configuration name for {@link #getActions()}. */
		String ACTIONS = "actions";

		/**
		 * The types of the objects this drop accepts.
		 *
		 * <p>
		 * A subtype of an accepted type is accepted as well. Acceptance is decided over the model
		 * on the server; the client is told the resulting set of type names, so a drag it cannot be
		 * applied to is not offered in the first place.
		 * </p>
		 */
		@Name(ACCEPT)
		@Mandatory
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getAccept();

		/**
		 * Whether the table as a whole or a single row is the target of this drop.
		 */
		@Name(TARGET)
		DropTargetMode getTarget();

		/**
		 * A {@link ViewChannel} the target row is written to before the {@link #getActions()
		 * actions} run, so they can read what was dropped on.
		 *
		 * <p>
		 * Unset (default) leaves the target unpublished, which is what a
		 * {@link DropTargetMode#TABLE} drop needs - it has no target row, and writes {@code null}
		 * where a channel is declared anyway.
		 * </p>
		 */
		@Name(TARGET_CHANNEL)
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getTargetChannel();

		/**
		 * The chain of actions applying the drop, receiving the dropped objects as the input of its
		 * first action.
		 */
		@Name(ACTIONS)
		@DefaultContainer
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends ViewAction>> getActions();
	}

	/**
	 * Container for the list of {@link PresetConfig}s of a {@link TableElement}.
	 */
	public interface PresetsConfig extends ConfigurationItem {

		/**
		 * The named filters the table offers, in the order they are displayed in.
		 */
		@DefaultContainer
		@Key(PresetConfig.NAME)
		List<PresetConfig> getPresets();
	}

	/**
	 * A filter criterion a {@link TableElement} offers under a name.
	 */
	@TagName("preset")
	public interface PresetConfig extends ConfigurationItem {

		/** Configuration name for {@link #getName()}. */
		String NAME = "name";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getCriteria()}. */
		String CRITERIA = "criteria";

		/**
		 * Technical name identifying this preset among the table's named filters.
		 *
		 * <p>
		 * It is what the user's choice of preset is remembered under, so it must not be changed
		 * once the table is in use.
		 * </p>
		 */
		@Name(NAME)
		@Mandatory
		String getName();

		/**
		 * The name displayed on this preset's chip. Without it, the technical
		 * {@link #getName() name} is displayed.
		 */
		@Name(LABEL)
		ResKey getLabel();

		/**
		 * What this preset filters by: one criterion per column.
		 *
		 * <p>
		 * A column not mentioned here is not filtered by this preset.
		 * </p>
		 */
		@Name(CRITERIA)
		@DefaultContainer
		List<CriterionConfig> getCriteria();
	}

	/**
	 * What a {@link PresetConfig} selects in one column.
	 *
	 * <p>
	 * A criterion says what it selects in one of two ways: as a value the column's own filter
	 * translates, or in the form of that filter itself. Exactly one of the two is declared.
	 * </p>
	 */
	@TagName("criterion")
	public interface CriterionConfig extends ConfigurationItem {

		/** Configuration name for {@link #getColumn()}. */
		String COLUMN = "column";

		/** Configuration name for {@link #getExpr()}. */
		String EXPR = "expr";

		/** Configuration name for {@link #getInverted()}. */
		String INVERTED = "inverted";

		/** Configuration name for {@link #getStates()}. */
		String STATES = "states";

		/**
		 * The attribute of the column this criterion filters.
		 *
		 * <p>
		 * The table must have a column for that attribute, and that column must be filterable.
		 * </p>
		 */
		@Name(COLUMN)
		@Mandatory
		String getColumn();

		/**
		 * TL-Script expression computing the value the column is filtered by.
		 *
		 * <p>
		 * It is evaluated when the table is built and whenever one of its
		 * {@link Config#getInputs() inputs} changes, with the input values as its arguments - so an
		 * expression like {@code currentUser()} yields a preset that means something different to
		 * every user, and one over an input yields a preset that follows what is displayed
		 * elsewhere. Which value the expression may yield depends on the filter of the column: a
		 * text filter accepts any single value and matches its text, a selection filter accepts one
		 * of the values to select or a list of them, a boolean filter accepts {@code true} or
		 * {@code false}, and a range filter accepts a single value to match exactly or a list of two
		 * values as the inclusive bounds of a range. A value the column's filter cannot express is
		 * reported as a configuration error, and the preset is then not offered.
		 * </p>
		 *
		 * <p>
		 * Alternatively the criterion is written in the form of the column's filter, see
		 * {@link #getStates()}; a criterion declares one of the two, not both.
		 * </p>
		 */
		@Name(EXPR)
		Expr getExpr();

		/**
		 * The criterion written in the form of the column's filter, as an alternative to the value
		 * of {@link #getExpr()}: a text pattern with its matching options, a comparison, a
		 * selection, or the accepted truth values.
		 *
		 * <p>
		 * This is the form for everything a single value cannot say - a case-sensitive pattern, a
		 * one-sided comparison, several accepted truth values at once. At most one criterion form is
		 * declared, and a criterion declaring none is a configuration error, just as one declaring
		 * both a form and a value is.
		 * </p>
		 */
		@Name(STATES)
		@Label("Criterion form")
		@DefaultContainer
		List<FilterStateConfig> getStates();

		/**
		 * Whether the column accepts exactly the rows this criterion does <em>not</em> select.
		 *
		 * <p>
		 * Only a filter that offers the user to invert it can be inverted here, too; inverting one
		 * that does not is a configuration error.
		 * </p>
		 */
		@Name(INVERTED)
		boolean getInverted();
	}

	/**
	 * Container for the list of {@link ColumnConfig}s of a {@link TableElement}.
	 */
	public interface ColumnsConfig extends ConfigurationItem {

		/**
		 * The columns to display, in display order.
		 */
		@DefaultContainer
		List<ColumnConfig> getColumns();
	}

	/**
	 * Configuration for a single displayed column of a {@link TableElement}.
	 */
	@TagName("column")
	public interface ColumnConfig extends ConfigurationItem {

		/** Configuration name for {@link #getAttribute()}. */
		String ATTRIBUTE = "attribute";

		/** Configuration name for {@link #getFilter()}. */
		String FILTER = "filter";

		/** Configuration name for {@link #getReadonly()}. */
		String READONLY = "readonly";

		/** Configuration name for {@link #getSort()}. */
		String SORT = "sort";

		/** Configuration name for {@link #getWidth()}. */
		String WIDTH = "width";

		/**
		 * The name of the attribute (column) to display.
		 */
		@Name(ATTRIBUTE)
		@Mandatory
		String getAttribute();

		/**
		 * An optional application-defined filter for this column, overriding the type-derived
		 * default. The filter matches against the cell's display text.
		 */
		@Name(FILTER)
		PolymorphicConfiguration<? extends ColumnFilter<?>> getFilter();

		/**
		 * Whether this column stays read-only while the table is {@link Config#getRowEdit()
		 * edited}.
		 */
		@Name(READONLY)
		boolean getReadonly();

		/**
		 * The direction this column is sorted in when the table is first shown, or unset to leave
		 * it unsorted.
		 *
		 * <p>
		 * Sorting by several columns is expressed by setting this on more than one column; the
		 * declaration order decides which one sorts first. The default only applies until the user
		 * sorts the table themselves, from then on their own order is remembered.
		 * </p>
		 */
		@Name(SORT)
		@Nullable
		@NullDefault
		SortDirection getSort();

		/**
		 * The column's default display width in pixels.
		 *
		 * <p>
		 * This is the width the user sees until they resize the column themselves; from then on
		 * their own width is remembered. {@code 0} - the default - keeps the width the column's
		 * type derives.
		 * </p>
		 */
		@Name(WIDTH)
		@Constraint(NonNegative.class)
		int getWidth();
	}

	/**
	 * The order the table is displayed in before the user sorts it, taken from the
	 * {@link ColumnConfig#getSort() sorted} columns in declaration order.
	 */
	private SortSpec defaultSort() {
		ColumnsConfig columnsConfig = _config.getColumns();
		if (columnsConfig == null) {
			return SortSpec.NONE;
		}
		List<SortColumn> sortColumns = new ArrayList<>();
		for (ColumnConfig columnConfig : columnsConfig.getColumns()) {
			SortDirection direction = columnConfig.getSort();
			if (direction != null) {
				sortColumns.add(new SortColumn(columnConfig.getAttribute(), direction == SortDirection.ASC));
			}
		}
		return sortColumns.isEmpty() ? SortSpec.NONE : new SortSpec(sortColumns);
	}

	/**
	 * Whether the table displays its filter bar.
	 *
	 * <p>
	 * Switched on explicitly, or implied by declaring presets - which are offered in that very bar.
	 * </p>
	 */
	/**
	 * The {@link Config#getGroupBy() configured} initial grouping, {@link GroupSpec#NONE} when the
	 * table starts ungrouped.
	 *
	 * <p>
	 * This is what the table's initial state carries, so a grouping the user chose - which is
	 * persisted under the table's identity - wins over it.
	 * </p>
	 */
	public GroupSpec initialGrouping() {
		String column = _config.getGroupBy();
		return StringServices.isEmpty(column) ? GroupSpec.NONE : new GroupSpec(List.of(column));
	}

	private boolean filterBar() {
		return _config.getFilterBar() || !_presets.isEmpty();
	}

	/**
	 * Where the filters the user saves under a name are persisted, or {@code null} for a table
	 * without a filter bar, which offers no way to save one.
	 */
	private NamedFilterStore filterStore() {
		return filterBar() ? PersonalConfigNamedFilterStore.INSTANCE : null;
	}

	/**
	 * The named filters this table declares, materialized over the given columns for the given
	 * input values.
	 *
	 * <p>
	 * The criterion expressions are evaluated here - when the table is built, and again whenever one
	 * of its inputs changes - so a preset over {@code currentUser()} means something different to
	 * every user, a preset over an input follows what is displayed elsewhere, and the criteria of a
	 * chip the user clicks are already computed.
	 * </p>
	 *
	 * @param columns
	 *        All columns of the table, whose filters translate the criteria.
	 * @param arguments
	 *        The values of the {@link Config#getInputs() input channels}, in declaration order -
	 *        the arguments of every criterion expression, as they are the arguments of
	 *        {@link Config#getRows()}.
	 */
	private List<NamedFilter> declaredFilters(List<? extends Column<?, ?>> columns, Object[] arguments) {
		if (_presets.isEmpty()) {
			return List.of();
		}
		List<DeclaredFilters.Declaration> declarations = new ArrayList<>(_presets.size());
		for (CompiledPreset preset : _presets) {
			List<DeclaredFilters.Criterion> criteria = new ArrayList<>(preset.criteria().size());
			for (CompiledCriterion criterion : preset.criteria()) {
				criteria.add(criterion.evaluate(arguments));
			}
			declarations.add(new DeclaredFilters.Declaration(preset.id(), preset.label(), criteria));
		}
		return DeclaredFilters.resolve(_log, tableId().value(), declarations, columns);
	}

	/** Command name of the contributed {@link #contributeAddRowCommand add-row command}. */
	private static final String COMMAND_ADD_ROW = "tableAddRow";

	/**
	 * Prefix distinguishing a {@link TableId} built from a configured
	 * {@link UIElement.Config#getPersonalizationKey() personalization key}.
	 */
	private static final String KEY_PREFIX = "key:";

	private final Config _config;

	private final QueryExecutor _rowsExecutor;

	/** The column-integration strategy per configured column, keyed by attribute name. */
	private final Map<String, ColumnBinding> _bindings = new HashMap<>();

	/** The compiled {@link Config#getPresets() presets}, in the order they are offered. */
	private final List<CompiledPreset> _presets;

	/**
	 * The type tag the rows are dragged under, or {@code null} while the table declares no
	 * {@link Config#getDrag() drag}.
	 */
	private final String _dragType;

	/** The declared {@link Config#getDrops() drops} with their actions, in declaration order. */
	private final List<CompiledDrop> _drops;

	/**
	 * Where a preset that cannot be applied to this table's columns is reported.
	 *
	 * @implNote The criterion values are evaluated per session (see {@link CriterionConfig#getExpr()}),
	 *           so whether a preset can be applied is only known when a session builds the table,
	 *           after the configuration has been instantiated.
	 */
	private final Log _log;

	/** The instantiated {@link Config#getOnActivate()} command, {@code null} without one. */
	private final ViewCommand _onActivate;

	/** The configuration {@link #_onActivate} was instantiated from, {@code null} without one. */
	private final ViewCommand.Config _onActivateConfig;

	/**
	 * A {@link PresetConfig} with its criterion expressions compiled.
	 *
	 * @param id
	 *        The {@link PresetConfig#getName() name} identifying the preset.
	 * @param label
	 *        The name to display.
	 * @param criteria
	 *        The compiled criteria, in declaration order.
	 */
	private record CompiledPreset(String id, ResKey label, List<CompiledCriterion> criteria) {
		// Pure data carrier.
	}

	/**
	 * A {@link CriterionConfig} with its expressions compiled.
	 *
	 * @param column
	 *        The attribute of the column to filter.
	 * @param value
	 *        Computes the value the column is filtered by, or {@code null} if the criterion is
	 *        declared as a {@link #state()}.
	 * @param state
	 *        Computes the criterion in the form of the column's filter, or {@code null} if the
	 *        criterion is declared as a {@link #value()}.
	 * @param inverted
	 *        Whether the column accepts exactly the rows the criterion does not select.
	 */
	private record CompiledCriterion(String column, QueryExecutor value, FilterStateTemplate state,
			boolean inverted) {

		/**
		 * The criterion selected by the given input values.
		 */
		DeclaredFilters.Criterion evaluate(Object[] arguments) {
			if (state != null) {
				return new DeclaredFilters.Criterion.State(column, state.evaluate(arguments), inverted);
			}
			return new DeclaredFilters.Criterion.Value(column, value.execute(arguments), inverted);
		}
	}

	/**
	 * A {@link DropConfig} with its action chain instantiated.
	 *
	 * @param config
	 *        What the drop accepts and targets.
	 * @param actions
	 *        The instantiated action chain applying it.
	 */
	private record CompiledDrop(DropConfig config, List<ViewAction> actions) {
		// Pure data carrier.
	}

	/**
	 * Creates a {@link TableElement} from configuration.
	 */
	@CalledByReflection
	public TableElement(InstantiationContext context, Config config) {
		_config = config;
		_log = context;
		_rowsExecutor = QueryExecutor.compile(config.getRows());
		_dragType = dragType(context, config);
		_drops = compileDrops(context, config.getDrops());
		if (config.getRowEdit() != RowEditPolicy.NONE && (_dragType != null || !_drops.isEmpty())) {
			// The editable table is a control of its own, which carries no drag-and-drop seam; a
			// declaration there would apply to nothing.
			context.error("A <table> with '" + Config.ROW_EDIT + "' offers neither <" + Config.DRAG
				+ "> nor <drop>.");
		}

		ColumnsConfig columnsConfig = config.getColumns();
		if (columnsConfig != null) {
			for (ColumnConfig columnConfig : columnsConfig.getColumns()) {
				_bindings.put(columnConfig.getAttribute(), resolveBinding(context, columnConfig));
			}
		}

		_presets = compilePresets(context, config.getPresets());

		PolymorphicConfiguration<? extends ViewCommand> onActivate = config.getOnActivate();
		_onActivateConfig = onActivate instanceof ViewCommand.Config activateConfig ? activateConfig : null;
		_onActivate = context.getInstance(onActivate);
	}

	/**
	 * The type tag the rows of a table declaring a {@link Config#getDrag() drag} are dragged under:
	 * the declared {@link DragConfig#getType() type}, or the first of the table's
	 * {@link Config#getTypes() row types}. {@code null} for a table whose rows are not draggable,
	 * and for one that says nothing about what its rows are - which is reported as a configuration
	 * error.
	 */
	private static String dragType(Log log, Config config) {
		DragConfig drag = config.getDrag();
		if (drag == null) {
			return null;
		}
		TLModelPartRef declared = drag.getType();
		if (declared != null) {
			return declared.qualifiedName();
		}
		List<TLModelPartRef> types = config.getTypes();
		if (types == null || types.isEmpty()) {
			log.error("A <table> whose rows are dragged must say what they are: either '"
				+ DragConfig.TYPE + "' on its <" + Config.DRAG + ">, or '" + Config.TYPES
				+ "' on the table itself.");
			return null;
		}
		return types.get(0).qualifiedName();
	}

	/**
	 * Instantiates the action chains of the declared drops, so that applying one only has to run
	 * them.
	 */
	private static List<CompiledDrop> compileDrops(InstantiationContext context, List<DropConfig> dropConfigs) {
		if (dropConfigs.isEmpty()) {
			return List.of();
		}
		List<CompiledDrop> result = new ArrayList<>(dropConfigs.size());
		for (DropConfig dropConfig : dropConfigs) {
			List<ViewAction> actions = dropConfig.getActions().stream()
				.<ViewAction> map(actionConfig -> context.getInstance(actionConfig))
				.filter(action -> action != null)
				.toList();
			result.add(new CompiledDrop(dropConfig, actions));
		}
		return result;
	}

	/**
	 * The drop target of this table's declared drops, resolved for the given session: the accepted
	 * types against the application model, the target channels against the view.
	 */
	private DropTarget dropBinding(ViewContext context) {
		List<TableDropBinding.Drop> drops = new ArrayList<>(_drops.size());
		for (CompiledDrop compiled : _drops) {
			DropConfig dropConfig = compiled.config();
			List<TLType> accepted = new ArrayList<>(dropConfig.getAccept().size());
			for (TLModelPartRef ref : dropConfig.getAccept()) {
				TLType type = ref.resolveType();
				if (type == null) {
					throw new RuntimeException(
						"A <table> accepts a drop of an unknown type: " + ref.qualifiedName());
				}
				accepted.add(type);
			}
			ChannelRef targetChannelRef = dropConfig.getTargetChannel();
			drops.add(new TableDropBinding.Drop(TableDropBinding.tagsOf(accepted), dropConfig.getTarget(),
				targetChannelRef == null ? null : context.resolveChannel(targetChannelRef),
				compiled.actions()));
		}
		return new TableDropBinding(context, drops);
	}

	/**
	 * Compiles the criterion expressions of the configured presets, so that building the table only
	 * has to evaluate them.
	 */
	private static List<CompiledPreset> compilePresets(Log log, PresetsConfig presetsConfig) {
		if (presetsConfig == null) {
			return List.of();
		}
		List<CompiledPreset> result = new ArrayList<>(presetsConfig.getPresets().size());
		for (PresetConfig presetConfig : presetsConfig.getPresets()) {
			List<CompiledCriterion> criteria = new ArrayList<>(presetConfig.getCriteria().size());
			boolean complete = true;
			for (CriterionConfig criterionConfig : presetConfig.getCriteria()) {
				CompiledCriterion criterion = compileCriterion(log, presetConfig, criterionConfig);
				if (criterion == null) {
					complete = false;
					break;
				}
				criteria.add(criterion);
			}
			if (!complete) {
				// A preset is offered with all of its criteria or not at all: one filtering by less
				// than it declares would show other rows than its name says.
				continue;
			}
			ResKey label = presetConfig.getLabel();
			result.add(new CompiledPreset(presetConfig.getName(),
				label != null ? label : ResKey.text(presetConfig.getName()), criteria));
		}
		return result;
	}

	/**
	 * Compiles one criterion: either the value expression, or the declared form of the column's
	 * filter - exactly one of the two, so a criterion declaring both or neither is reported and
	 * dropped.
	 */
	private static CompiledCriterion compileCriterion(Log log, PresetConfig presetConfig,
			CriterionConfig criterionConfig) {
		Expr expr = criterionConfig.getExpr();
		List<FilterStateConfig> states = criterionConfig.getStates();
		if (states.size() > 1) {
			log.error(criterion(presetConfig, criterionConfig) + " declares " + states.size()
				+ " criterion forms, but a column is filtered by one.");
			return null;
		}
		FilterStateConfig state = states.isEmpty() ? null : states.get(0);
		if ((expr == null) == (state == null)) {
			log.error(criterion(presetConfig, criterionConfig) + " must declare either a '"
				+ CriterionConfig.EXPR + "' or the form of the column's filter, but "
				+ (expr == null ? "declares neither" : "declares both") + ".");
			return null;
		}
		return new CompiledCriterion(criterionConfig.getColumn(),
			expr == null ? null : QueryExecutor.compile(expr),
			state == null ? null : FilterStateTemplate.compile(state),
			criterionConfig.getInverted());
	}

	/** How a criterion of a preset is named in a configuration error. */
	private static String criterion(PresetConfig presetConfig, CriterionConfig criterionConfig) {
		return "The criterion for the column '" + criterionConfig.getColumn() + "' of the preset '"
			+ presetConfig.getName() + "'";
	}

	/**
	 * The column integration for a configured column: derived from the attribute's type when no
	 * filter is configured, the filter's own integration when it provides one, or a value-text filter
	 * otherwise. This single capability check ({@link ColumnBinding}) is the only place filter kinds
	 * are distinguished.
	 */
	private static ColumnBinding resolveBinding(InstantiationContext context, ColumnConfig columnConfig) {
		PolymorphicConfiguration<? extends ColumnFilter<?>> filterConfig = columnConfig.getFilter();
		if (filterConfig == null) {
			return ColumnBinding.TYPE_DERIVED;
		}
		ColumnFilter<?> filter = context.getInstance(filterConfig);
		if (filter == null) {
			return ColumnBinding.TYPE_DERIVED;
		}
		return filter instanceof ColumnBinding binding ? binding : ColumnBinding.forValueFilter(filter);
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ViewChannel> inputChannels = new ArrayList<>();
		for (ChannelRef ref : _config.getInputs()) {
			inputChannels.add(context.resolveChannel(ref));
		}
		Object[] inputValues = readChannelValues(inputChannels);
		Collection<?> rows = executeRowsQuery(_rowsExecutor, inputValues);

		if (_config.getRowEdit() != RowEditPolicy.NONE) {
			return createEditableControl(context, inputChannels, rows);
		}

		ViewCommandModel activation = activationModel(context);

		List<ColumnSetup> setups = columnSetups(resolveRowType(rows), context);
		List<Column<Object, ?>> columns = new ArrayList<>(setups.size());
		for (ColumnSetup setup : setups) {
			columns.add(setup.buildColumn());
		}
		columns.addAll(this.<Object> rowCommandColumns(context, activation));
		ListRowSource<Object> source = new ListRowSource<>(new ArrayList<>(rows), columns);
		Set<String> hiddenByDefault = hiddenByDefault(setups.stream().map(ColumnSetup::attribute).toList());
		TableViewState initialState = DefaultTableView.initialState(columns, defaultSort(), hiddenByDefault);
		initialState.setFrozenCount(_config.getFixedColumns());
		initialState.setGrouping(initialGrouping());
		DefaultTableView<Object> view = new DefaultTableView<>(columns, source, initialState,
			PersonalConfigViewStateStore.INSTANCE, tableId(), hiddenByDefault,
			declaredFilters(columns, inputValues), filterStore());

		TableViewControl<Object> control = new TableViewControl<>(context, view, false);
		control.setFilterBar(filterBar());
		if (_dragType != null) {
			control.setDragSource(_dragType);
		}
		if (!_drops.isEmpty()) {
			control.setDropTarget(dropBinding(context));
		}

		// Let each column contribute any per-session UI (e.g. a custom filter dialog).
		for (ColumnSetup setup : setups) {
			setup.binding().installUI(setup, control);
		}

		ChannelRef selectionRef = _config.getSelection();
		TableSelectionBinding selectionBinding =
			selectionRef != null ? new TableSelectionBinding(control, context.resolveChannel(selectionRef)) : null;
		if (selectionBinding != null) {
			control.addCleanupAction(selectionBinding::dispose);
		}

		control.setActivationHandler(activationHandler(context, activation));

		// Refresh the rows when observed objects change or an input channel changes.
		QueryExecutor rowsExecutor = _rowsExecutor;
		Runnable refresh = () -> {
			if (!_presets.isEmpty()) {
				// The criteria of the presets are computed from the inputs, so a changed input means
				// other criteria: they are resolved again, and a chip the user has applied goes on
				// filtering by what it now means.
				view.setDeclaredFilters(declaredFilters(columns, readChannelValues(inputChannels)));
			}
			control.refreshData();
			if (selectionBinding != null) {
				selectionBinding.rowsRefreshed();
			}
		};
		RowSourceObserver<Object> observer = new RowSourceObserver<>(
			source,
			args -> new ArrayList<>(executeRowsQuery(rowsExecutor, args)),
			ObservedTypes.resolve(_config.getObservedTypes()),
			inputChannels,
			refresh);
		// Observe the model only while the table is displayed: a table that is not attached - the
		// inactive child of a tab bar, a page nobody looks at - must not react to model changes.
		control.addAttachListener(() -> observer.attach(context.getModelScope()));
		control.addDetachListener(observer::detach);

		return control;
	}

	/**
	 * The model of the {@link Config#getOnActivate() configured activation command}, {@code null}
	 * when the table configures none.
	 *
	 * <p>
	 * One model serves both ways of running the command - the activation gesture and the button the
	 * rows carry - so both decide by the same rules.
	 * </p>
	 */
	private ViewCommandModel activationModel(ViewContext context) {
		if (_onActivate == null || _onActivateConfig == null) {
			return null;
		}
		return ViewCommandModel.forCommand(context, _onActivate, _onActivateConfig);
	}

	/**
	 * The handler running the activation command with the activated row, {@code null} when the
	 * table configures none.
	 */
	private static <R> TableViewControl.ActivationHandler<R> activationHandler(ViewContext context,
			ViewCommandModel activation) {
		if (activation == null) {
			return null;
		}
		return row -> activation.execute(context, row);
	}

	/**
	 * The trailing columns holding a button per row, empty when the table offers no command per
	 * row.
	 *
	 * @param activation
	 *        The model of the activation command, {@code null} when the table configures none.
	 */
	private <R> List<Column<R, R>> rowCommandColumns(ViewContext context, ViewCommandModel activation) {
		if (activation == null || !_config.getActivationButton()) {
			return List.of();
		}
		return RowCommandColumn.columns(context, List.of(
			new RowCommandColumn.RowCommand(activation, Icons.TABLE_ACTIVATE_ROW,
				I18NConstants.TABLE_ACTIVATE_ROW)));
	}

	/**
	 * Creates the editable variant: rows come from the same query (frozen while an edit session
	 * runs), cells become editable according to {@link Config#getRowEdit()}, and membership
	 * changes follow {@link Config#getCreateType()} / {@link Config#getOnRemove()}.
	 */
	private IReactControl createEditableControl(ViewContext context, List<ViewChannel> inputChannels,
			Collection<?> rows) {
		FormModel formModel = context.getFormModel();
		if (!(formModel instanceof FormControl formControl)) {
			throw new IllegalStateException(
				"A <table> with '" + Config.ROW_EDIT + "' requires an enclosing <form>.");
		}

		TLClass createType = resolveCreateType();
		TLStructuredType rowType = resolveRowType(rows);
		QueryExecutor rowsExecutor = _rowsExecutor;
		QueryRowSetBinding binding = new QueryRowSetBinding(
			() -> tlObjectRows(executeRowsQuery(rowsExecutor, readChannelValues(inputChannels))),
			createType != null ? createType : (rowType instanceof TLClass rowClass ? rowClass : null),
			createType == null ? List.of() : List.of(createType),
			_config.getOnRemove());

		ChannelRef selectionRef = _config.getSelection();
		ViewChannel selectionChannel = selectionRef != null ? context.resolveChannel(selectionRef) : null;

		ViewCommandModel activation = activationModel(context);

		List<RowSetTableControl.TableColumn> editColumns = editColumns(rowType);
		RowSetTableControl control =
			new RowSetTableControl(context, formControl, binding, editColumns, _config.getRowEdit());
		control.setFramed(false);
		control.setPersonalization(PersonalConfigViewStateStore.INSTANCE, tableId());
		control.setNamedFilters(columns -> declaredFilters(columns, readChannelValues(inputChannels)),
			filterStore());
		control.setFilterBar(filterBar());
		control.setHiddenByDefault(
			hiddenByDefault(editColumns.stream().map(RowSetTableControl.TableColumn::attribute).toList()));
		control.setDefaultSort(defaultSort());
		control.setGrouping(initialGrouping());
		control.setFixedColumns(_config.getFixedColumns());
		control.setSelectionChannel(selectionChannel);
		control.setRowRefresh(args -> executeRowsQuery(rowsExecutor, args), ObservedTypes.resolve(_config.getObservedTypes()), inputChannels);
		control.setActivationHandler(activationHandler(context, activation));
		control.setTrailingColumns(this.<TLObject> rowCommandColumns(context, activation));
		control.init();

		contributeAddRowCommand(context, formControl, binding, control);

		return control;
	}

	/**
	 * The data columns of the editable variant: one per configured {@code <column>} (with its
	 * read-only flag and resolved filter binding), followed by the {@link #offeredParts offered}
	 * remainder of the row type - or, when no columns are configured, one per non-hidden attribute
	 * of the row type.
	 */
	private List<RowSetTableControl.TableColumn> editColumns(TLStructuredType rowType) {
		List<RowSetTableControl.TableColumn> columns = new ArrayList<>();
		ColumnsConfig columnsConfig = _config.getColumns();
		if (columnsConfig != null && !columnsConfig.getColumns().isEmpty()) {
			for (ColumnConfig columnConfig : columnsConfig.getColumns()) {
				String attribute = columnConfig.getAttribute();
				columns.add(new RowSetTableControl.TableColumn(attribute, columnConfig.getReadonly(),
					_bindings.get(attribute), columnConfig.getWidth()));
			}
			for (TLStructuredTypePart part : offeredParts(configuredAttributes())) {
				// An offered column has no <column> to declare a read-only flag, so it is editable
				// exactly as a form field for that attribute would be.
				columns.add(new RowSetTableControl.TableColumn(part.getName(),
					!DisplayAnnotations.isEditable(part), ColumnBinding.TYPE_DERIVED, 0));
			}
		} else if (rowType != null) {
			for (TLStructuredTypePart part : rowType.getAllParts()) {
				if (DisplayAnnotations.isHidden(part)) {
					continue;
				}
				columns.add(
					new RowSetTableControl.TableColumn(part.getName(), false, ColumnBinding.TYPE_DERIVED, 0));
			}
		}
		if (columns.isEmpty()) {
			throw new IllegalStateException(
				"A <table> requires either explicit <column>s or a resolvable row type to derive them from.");
		}
		return columns;
	}

	/**
	 * Contributes the "add row" command to the enclosing command scope (next to the form's save
	 * and cancel commands), visible only while the form is in edit mode. The table itself renders
	 * frameless, so it has no own toolbar to host the command.
	 */
	private static void contributeAddRowCommand(ViewContext context, FormControl formControl,
			RowSetBinding binding, RowSetTableControl control) {
		if (binding.getCreateTypes().isEmpty()) {
			return;
		}
		CommandScope scope = context.getScope(CommandScope.class);
		if (scope == null) {
			return;
		}

		FormCommandModel addCommand = FormCommandModel.editModeCommand(COMMAND_ADD_ROW,
			com.top_logic.layout.view.I18NConstants.COMPOSITION_TABLE_ADD,
			com.top_logic.layout.view.form.Icons.COMPOSITION_TABLE_ADD,
			formControl, ctx -> control.addRow());
		scope.addCommand(addCommand);
		formControl.addAttachListener(addCommand::attach);
		formControl.addDetachListener(addCommand::detach);
		formControl.addCleanupAction(() -> scope.removeCommand(addCommand));
	}

	/**
	 * The resolved {@link Config#getCreateType() create type}, or {@code null} if none is
	 * configured.
	 */
	private TLClass resolveCreateType() {
		TLModelPartRef ref = _config.getCreateType();
		if (ref == null) {
			return null;
		}
		try {
			return ref.resolveClass();
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Failed to resolve create type: " + ref.qualifiedName(), ex);
		}
	}

	/**
	 * The {@link TLObject} rows of a query result; non-model entries are skipped.
	 */
	private static List<TLObject> tlObjectRows(Collection<?> rows) {
		List<TLObject> result = new ArrayList<>(rows.size());
		for (Object row : rows) {
			if (row instanceof TLObject object) {
				result.add(object);
			}
		}
		return result;
	}

	/**
	 * The stable identity of this table, under which its personalization and the user's saved
	 * filters are stored.
	 *
	 * <p>
	 * The configured {@link UIElement.Config#getPersonalizationKey() personalization key} when
	 * there is one. Without it, the identity is the table's structural signature - its row types
	 * plus its column attributes - which changes whenever a column is added or removed, so that a
	 * configured key is what keeps a personalization across an edit of the view.
	 * </p>
	 */
	public TableId tableId() {
		String personalizationKey = _config.getPersonalizationKey();
		if (!StringServices.isEmpty(personalizationKey)) {
			// Namespaced, so that a short key cannot collide with the structural signature of some
			// other table.
			return new TableId(KEY_PREFIX + personalizationKey);
		}
		StringBuilder key = new StringBuilder();
		List<TLModelPartRef> types = _config.getTypes();
		if (types != null) {
			for (TLModelPartRef type : types) {
				key.append(type.qualifiedName()).append(',');
			}
		}
		key.append('|');
		ColumnsConfig columnsConfig = _config.getColumns();
		if (columnsConfig != null) {
			for (ColumnConfig columnConfig : columnsConfig.getColumns()) {
				key.append(columnConfig.getAttribute()).append(',');
			}
		}
		return new TableId(key.toString());
	}

	/**
	 * The resolved column descriptors: one per configured {@code <column>} (using its
	 * {@link #resolveBinding resolved binding}), followed by the {@link #offeredParts offered}
	 * remainder of the row type - or, when no columns are configured, one per non-hidden attribute
	 * of the row type, each type-derived.
	 */
	private List<ColumnSetup> columnSetups(TLStructuredType rowType, ViewContext context) {
		List<ColumnSetup> setups = new ArrayList<>();
		ColumnsConfig columnsConfig = _config.getColumns();
		if (columnsConfig != null && !columnsConfig.getColumns().isEmpty()) {
			for (ColumnConfig columnConfig : columnsConfig.getColumns()) {
				String attribute = columnConfig.getAttribute();
				TLStructuredTypePart part = rowType == null ? null : rowType.getPart(attribute);
				setups.add(new ColumnSetup(attribute, columnLabel(part, attribute), part, context,
					_bindings.get(attribute), columnConfig.getWidth()));
			}
			for (TLStructuredTypePart part : offeredParts(configuredAttributes())) {
				String attribute = part.getName();
				setups.add(new ColumnSetup(attribute, columnLabel(part, attribute), part, context,
					ColumnBinding.TYPE_DERIVED, 0));
			}
		} else if (rowType != null) {
			// No explicit columns configured: derive a default set from the row type's
			// non-hidden attributes, in declaration order.
			for (TLStructuredTypePart part : rowType.getAllParts()) {
				if (DisplayAnnotations.isHidden(part)) {
					continue;
				}
				String attribute = part.getName();
				setups.add(new ColumnSetup(attribute, columnLabel(part, attribute), part, context,
					ColumnBinding.TYPE_DERIVED, 0));
			}
		}
		if (setups.isEmpty()) {
			throw new IllegalStateException(
				"A <table> requires either explicit <column>s or a resolvable row type to derive them from.");
		}
		return setups;
	}

	/**
	 * The attributes a table with explicitly configured {@code <column>}s <em>offers</em> in
	 * addition: those of its {@link Config#getTypes() configured types} that no column covers and
	 * that a form would display, too - so a user can add any attribute of the row type to the table
	 * through the column selection, without the table having to enumerate them all.
	 *
	 * <p>
	 * Their columns start out hidden (see {@link #hiddenByDefault(Collection)}); a table configures
	 * the columns it considers worth showing, and the rest is a choice, not a default.
	 * </p>
	 *
	 * @param covered
	 *        The attributes of the configured columns, which are not offered a second time.
	 */
	private List<TLStructuredTypePart> offeredParts(Collection<String> covered) {
		// Only an explicitly configured type gives a stable set of columns; a type guessed from the
		// first row would offer different columns depending on the data at hand.
		List<TLModelPartRef> typeRefs = _config.getTypes();
		if (typeRefs == null || typeRefs.isEmpty()) {
			return List.of();
		}
		Set<String> seen = new HashSet<>(covered);
		List<TLStructuredTypePart> result = new ArrayList<>();
		for (TLModelPartRef typeRef : typeRefs) {
			TLStructuredType type;
			try {
				type = typeRef.resolveClass();
			} catch (ConfigurationException ex) {
				throw new RuntimeException("Failed to resolve type: " + typeRef.qualifiedName(), ex);
			}
			for (TLStructuredTypePart part : type.getAllParts()) {
				if (DisplayAnnotations.isHidden(part) || !seen.add(part.getName())) {
					continue;
				}
				result.add(part);
			}
		}
		return result;
	}

	/** The attributes of the configured {@code <column>}s, empty if none are configured. */
	private Set<String> configuredAttributes() {
		ColumnsConfig columnsConfig = _config.getColumns();
		if (columnsConfig == null || columnsConfig.getColumns().isEmpty()) {
			return Set.of();
		}
		Set<String> result = new LinkedHashSet<>();
		for (ColumnConfig columnConfig : columnsConfig.getColumns()) {
			result.add(columnConfig.getAttribute());
		}
		return result;
	}

	/**
	 * Which of the given columns the table does not display until the user selects them: everything
	 * beyond the configured {@code <column>}s, i.e. the {@link #offeredParts offered} attributes.
	 */
	private Set<String> hiddenByDefault(Collection<String> columns) {
		Set<String> configured = configuredAttributes();
		if (configured.isEmpty()) {
			return Set.of();
		}
		Set<String> result = new LinkedHashSet<>(columns);
		result.removeAll(configured);
		return result;
	}

	/**
	 * The display label for a column: the model attribute's label if the part can be resolved,
	 * otherwise the attribute name.
	 */
	private static ResKey columnLabel(TLStructuredTypePart part, String attribute) {
		return part != null ? TLModelNamingConvention.resourceKey(part) : ResKey.text(attribute);
	}

	/**
	 * Resolves the row type used for column-label resolution: the first configured
	 * {@link Config#getTypes() type}, or the type of the first row, or {@code null}.
	 */
	private TLStructuredType resolveRowType(Collection<?> rows) {
		List<TLModelPartRef> typeRefs = _config.getTypes();
		if (typeRefs != null && !typeRefs.isEmpty()) {
			try {
				return typeRefs.get(0).resolveClass();
			} catch (ConfigurationException ex) {
				throw new RuntimeException("Failed to resolve type: " + typeRefs.get(0).qualifiedName(), ex);
			}
		}
		for (Object row : rows) {
			if (row instanceof TLObject object) {
				return object.tType();
			}
		}
		return null;
	}

	private static Object[] readChannelValues(List<ViewChannel> channels) {
		Object[] values = new Object[channels.size()];
		for (int n = 0; n < channels.size(); n++) {
			values[n] = channels.get(n).get();
		}
		return values;
	}

	private static Collection<?> executeRowsQuery(QueryExecutor rowsExecutor, Object[] channelValues) {
		Object result = rowsExecutor.execute(channelValues);
		if (result instanceof Collection<?> collection) {
			return collection;
		}
		return result == null ? Collections.emptyList() : Collections.singletonList(result);
	}

}
