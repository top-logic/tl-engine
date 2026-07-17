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
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.form.FormCommandModel;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormModel;
import com.top_logic.layout.view.form.Icons;
import com.top_logic.layout.view.form.QueryRowSetBinding;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetBinding;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.layout.view.table.ColumnBinding;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.TableId;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.table.impl.PersonalConfigViewStateStore;

/**
 * Declarative {@link UIElement} that renders a model-defined table (the {@code <table>} tag) through
 * the green-field table model ({@link com.top_logic.table.TableView}) via a {@link TableViewControl}.
 *
 * <p>
 * Input data comes from {@link ViewChannel}s, rows are computed by a TL-Script expression, and each
 * configured column reads a model attribute. Columns are sortable and (per-column) filterable, with
 * no dependency on the legacy {@code TableModel}.
 * </p>
 */
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

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/** Configuration name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		/** Configuration name for {@link #getRowEdit()}. */
		String ROW_EDIT = "row-edit";

		/** Configuration name for {@link #getCreateType()}. */
		String CREATE_TYPE = "create-type";

		/** Configuration name for {@link #getOnRemove()}. */
		String ON_REMOVE = "on-remove";

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
		 * Optional {@link ViewChannel} to write the selected row object(s) to.
		 */
		@Name(SELECTION)
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();

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
	}

	/** Command name of the contributed {@link #contributeAddRowCommand add-row command}. */
	private static final String COMMAND_ADD_ROW = "tableAddRow";

	private final Config _config;

	private final QueryExecutor _rowsExecutor;

	/** The column-integration strategy per configured column, keyed by attribute name. */
	private final Map<String, ColumnBinding> _bindings = new HashMap<>();

	/**
	 * Creates a {@link TableElement} from configuration.
	 */
	@CalledByReflection
	public TableElement(InstantiationContext context, Config config) {
		_config = config;
		_rowsExecutor = QueryExecutor.compile(config.getRows());

		ColumnsConfig columnsConfig = config.getColumns();
		if (columnsConfig != null) {
			for (ColumnConfig columnConfig : columnsConfig.getColumns()) {
				_bindings.put(columnConfig.getAttribute(), resolveBinding(context, columnConfig));
			}
		}
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
		Collection<?> rows = executeRowsQuery(_rowsExecutor, readChannelValues(inputChannels));

		if (_config.getRowEdit() != RowEditPolicy.NONE) {
			return createEditableControl(context, inputChannels, rows);
		}

		List<ColumnSetup> setups = columnSetups(resolveRowType(rows), context);
		List<Column<Object, ?>> columns = new ArrayList<>(setups.size());
		for (ColumnSetup setup : setups) {
			columns.add(setup.binding().createColumn(setup));
		}
		ListRowSource<Object> source = new ListRowSource<>(new ArrayList<>(rows), columns);
		DefaultTableView<Object> view =
			DefaultTableView.create(columns, source, PersonalConfigViewStateStore.INSTANCE, tableId());

		TableViewControl<Object> control = new TableViewControl<>(context, view, false);

		// Let each column contribute any per-session UI (e.g. a custom filter dialog).
		for (ColumnSetup setup : setups) {
			setup.binding().installUI(setup, control);
		}

		// Reflects the selection channel's current value as the table's selection (highlighted and
		// scrolled into view). Set when a selection channel is configured; reused after data refreshes
		// so a row that appears only on refresh (e.g. the just-created object) still gets selected.
		Runnable[] reapplySelection = {null};

		ChannelRef selectionRef = _config.getSelection();
		if (selectionRef != null) {
			ViewChannel selectionChannel = context.resolveChannel(selectionRef);
			// Two-way binding: the table writes its selection to the channel, and a value written to
			// the channel from elsewhere (e.g. a create command selecting the new object) is reflected
			// as the table's selection. The guard breaks the notification cycle between the two
			// directions.
			boolean[] applyingFromChannel = {false};
			control.setSelectionListener(selectedKeys -> {
				if (applyingFromChannel[0]) {
					return;
				}
				if (selectedKeys.size() == 1) {
					selectionChannel.set(selectedKeys.iterator().next());
				} else if (selectedKeys.isEmpty()) {
					selectionChannel.set(null);
				} else {
					selectionChannel.set(selectedKeys);
				}
			});
			reapplySelection[0] = () -> {
				Object value = selectionChannel.get();
				applyingFromChannel[0] = true;
				try {
					control.selectRow(value instanceof Collection ? null : value);
				} finally {
					applyingFromChannel[0] = false;
				}
			};
			ViewChannel.ChannelListener channelListener = (sender, oldValue, newValue) -> reapplySelection[0].run();
			selectionChannel.addListener(channelListener);
			control.addCleanupAction(() -> selectionChannel.removeListener(channelListener));
		}

		// Refresh the rows when observed objects change or an input channel changes. After a refresh,
		// re-apply the selection so a newly appeared row (e.g. the just-created object the selection
		// channel already points to) is selected and scrolled into view.
		QueryExecutor rowsExecutor = _rowsExecutor;
		Runnable refresh = () -> {
			control.refreshData();
			if (reapplySelection[0] != null) {
				reapplySelection[0].run();
			}
		};
		RowSourceObserver<Object> observer = new RowSourceObserver<>(
			source,
			args -> new ArrayList<>(executeRowsQuery(rowsExecutor, args)),
			resolveObservedTypes(),
			inputChannels,
			refresh);
		control.addBeforeWriteAction(() -> observer.attach(context.getModelScope()));
		control.addCleanupAction(observer::detach);

		return control;
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

		RowSetTableControl control =
			new RowSetTableControl(context, formControl, binding, editColumns(rowType), _config.getRowEdit());
		control.setFramed(false);
		control.setPersonalization(PersonalConfigViewStateStore.INSTANCE, tableId());
		control.setSelectionChannel(selectionChannel);
		control.setRowRefresh(args -> executeRowsQuery(rowsExecutor, args), resolveObservedTypes(), inputChannels);
		control.initTable();

		contributeAddRowCommand(context, formControl, binding, control);

		return control;
	}

	/**
	 * The data columns of the editable variant: one per configured {@code <column>} (with its
	 * read-only flag and resolved filter binding), or - when no columns are configured - one per
	 * non-hidden attribute of the row type.
	 */
	private List<RowSetTableControl.TableColumn> editColumns(TLStructuredType rowType) {
		List<RowSetTableControl.TableColumn> columns = new ArrayList<>();
		ColumnsConfig columnsConfig = _config.getColumns();
		if (columnsConfig != null && !columnsConfig.getColumns().isEmpty()) {
			for (ColumnConfig columnConfig : columnsConfig.getColumns()) {
				String attribute = columnConfig.getAttribute();
				columns.add(new RowSetTableControl.TableColumn(attribute, columnConfig.getReadonly(),
					_bindings.get(attribute)));
			}
		} else if (rowType != null) {
			for (TLStructuredTypePart part : rowType.getAllParts()) {
				if (DisplayAnnotations.isHidden(part)) {
					continue;
				}
				columns.add(
					new RowSetTableControl.TableColumn(part.getName(), false, ColumnBinding.TYPE_DERIVED));
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
		CommandScope scope = context.getCommandScope();
		if (scope == null) {
			return;
		}

		FormCommandModel addCommand = FormCommandModel.editModeCommand(COMMAND_ADD_ROW,
			com.top_logic.layout.view.I18NConstants.COMPOSITION_TABLE_ADD, Icons.COMPOSITION_TABLE_ADD,
			formControl, ctx -> control.addRow());
		scope.addCommand(addCommand);
		formControl.addBeforeWriteAction(addCommand::attach);
		formControl.addCleanupAction(() -> {
			scope.removeCommand(addCommand);
			addCommand.detach();
		});
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

	private Set<TLStructuredType> resolveObservedTypes() {
		List<TLModelPartRef> refs = _config.getObservedTypes();
		if (refs == null || refs.isEmpty()) {
			return Set.of();
		}
		Set<TLStructuredType> types = new HashSet<>();
		for (TLModelPartRef ref : refs) {
			TLStructuredType type = (TLStructuredType) ref.resolveType();
			if (type == null) {
				throw new RuntimeException("Failed to resolve observed type: " + ref.qualifiedName());
			}
			types.add(type);
		}
		return types;
	}

	/**
	 * A stable personalization key for this table, derived from its structural signature (row
	 * types plus column attributes), so the same configured table restores its personalization
	 * across sessions.
	 */
	private TableId tableId() {
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
	 * {@link #resolveBinding resolved binding}), or - when no columns are configured - one per
	 * non-hidden attribute of the row type, each type-derived.
	 */
	private List<ColumnSetup> columnSetups(TLStructuredType rowType, ViewContext context) {
		List<ColumnSetup> setups = new ArrayList<>();
		ColumnsConfig columnsConfig = _config.getColumns();
		if (columnsConfig != null && !columnsConfig.getColumns().isEmpty()) {
			for (ColumnConfig columnConfig : columnsConfig.getColumns()) {
				String attribute = columnConfig.getAttribute();
				TLStructuredTypePart part = rowType == null ? null : rowType.getPart(attribute);
				setups.add(new ColumnSetup(attribute, columnLabel(part, attribute), part, context,
					_bindings.get(attribute)));
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
					ColumnBinding.TYPE_DERIVED));
			}
		}
		if (setups.isEmpty()) {
			throw new IllegalStateException(
				"A <table> requires either explicit <column>s or a resolvable row type to derive them from.");
		}
		return setups;
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
