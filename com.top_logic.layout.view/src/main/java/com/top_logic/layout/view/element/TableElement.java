/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.dnd.DeclaredDrop;
import com.top_logic.layout.view.dnd.DropConfig;
import com.top_logic.layout.view.form.FormCommandModel;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormModel;
import com.top_logic.layout.view.form.QueryRowSetBinding;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetBinding;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnDeclarations;
import com.top_logic.layout.view.table.TableDropConfig;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SecurityFilterReport;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.table.impl.PersonalConfigViewStateStore;

/**
 * Declarative {@link UIElement} that renders a model-defined table (the {@code 
 * <table>
 * } tag) through the green-field table model ({@link com.top_logic.table.TableView}) via a
 * {@link TableViewControl}.
 *
 * <p>
 * Input data comes from {@link ViewChannel}s, rows are computed by a TL-Script expression, and each
 * column is declared by an entry of the {@code <columns>} - over a model attribute of the rows,
 * over a value computed from them, or over an object they point to. Columns are sortable and
 * (per-column) filterable.
 * </p>
 *
 * <p>
 * The columns, the selection, the filter bar, the activation and the dragging of rows are those of
 * every {@link AbstractTableElement}; this element computes its rows as a flat list.
 * </p>
 */
@InApp
public class TableElement extends AbstractTableElement<TableElement.Config> {

	/**
	 * Configuration for {@link TableElement}.
	 */
	@TagName("table")
	public interface Config extends AbstractTableElement.Config {

		@Override
		@ClassDefault(TableElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getRows()}. */
		String ROWS = "rows";

		/** Configuration name for {@link #getGroupBy()}. */
		String GROUP_BY = "group-by";

		/** Configuration name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		/** Configuration name for {@link #getRowEdit()}. */
		String ROW_EDIT = "row-edit";

		/** Configuration name for {@link #getCreateType()}. */
		String CREATE_TYPE = "create-type";

		/** Configuration name for {@link #getOnRemove()}. */
		String ON_REMOVE = "on-remove";

		/** Configuration name for {@link #getDrops()}. */
		String DROPS = "drops";

		/**
		 * TL-Script function computing the row objects (a {@link Collection}).
		 *
		 * <p>
		 * The values of the declared inputs come first, in declaration order.
		 * </p>
		 */
		@Name(ROWS)
		@Mandatory
		@NonNullable
		Expr getRows();

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
		List<TableDropConfig> getDrops();
	}

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

	/** Command name of the contributed {@link #contributeAddRowCommand add-row command}. */
	private static final String COMMAND_ADD_ROW = "tableAddRow";

	private final QueryExecutor _rowsExecutor;

	/** The declared {@link Config#getDrops() drops} with their actions, in declaration order. */
	private final List<DeclaredDrop> _drops;

	/**
	 * Creates a {@link TableElement} from configuration.
	 */
	@CalledByReflection
	public TableElement(InstantiationContext context, Config config) {
		super(context, config);
		_rowsExecutor = QueryExecutor.compile(config.getRows());
		_drops = compileDrops(context, config.getDrops(), dropConfig -> dropConfig.getTarget().signature());
		if (config.getRowEdit() != RowEditPolicy.NONE && (config.getDrag() != null || !_drops.isEmpty())) {
			// The editable table is a control of its own, which carries no drag-and-drop seam; a
			// declaration there would apply to nothing.
			context.error("A <table> with '" + Config.ROW_EDIT + "' offers neither <" + Config.DRAG
				+ "> nor <" + DropConfig.TAG_NAME + ">.");
		}
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ViewChannel> inputChannels = ChannelInputs.resolve(context, _config.getInputs());
		Object[] inputValues = ChannelInputs.arguments(inputChannels);
		RowsResult initialRows = executeRows(_rowsExecutor, inputValues);
		Collection<?> rows = initialRows.rows();

		if (_config.getRowEdit() != RowEditPolicy.NONE) {
			return createEditableControl(context, inputChannels, initialRows);
		}

		ReadOnlyTable<ListRowSource<Object>> table = createTable(context, resolveRowType(rows), inputValues,
			columns -> new ListRowSource<>(new ArrayList<>(rows), columns), false, initialGrouping(), _drops);
		TableViewControl<Object> control = table.control();
		applyRowDiagnostics(control, initialRows.securityReport());

		// Refresh the rows when observed objects change or an input channel changes.
		QueryExecutor rowsExecutor = _rowsExecutor;
		Runnable refresh = () -> {
			refreshPresets(table, ChannelInputs.arguments(inputChannels));
			control.refreshData();
			if (table.selectionBinding() != null) {
				table.selectionBinding().rowsRefreshed();
			}
		};
		RowSourceObserver<Object> observer = new RowSourceObserver<>(
			table.source(),
			args -> new ArrayList<>(refreshRows(rowsExecutor, args, control)),
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
	 * Creates the editable variant: rows come from the same query (frozen while an edit session
	 * runs), cells become editable according to {@link Config#getRowEdit()}, and membership
	 * changes follow {@link Config#getCreateType()} / {@link Config#getOnRemove()}.
	 */
	private IReactControl createEditableControl(ViewContext context, List<ViewChannel> inputChannels,
			RowsResult initialRows) {
		FormModel formModel = context.getFormModel();
		if (!(formModel instanceof FormControl formControl)) {
			throw new IllegalStateException(
				"A <table> with '" + Config.ROW_EDIT + "' requires an enclosing <form>.");
		}

		TLClass createType = resolveCreateType();
		TLStructuredType rowType = resolveRowType(initialRows.rows());
		QueryExecutor rowsExecutor = _rowsExecutor;
		// The row function is handed to the binding before the control it reports its diagnostics to
		// exists, so the target is filled in below.
		ReactControl[] diagnosticsTarget = new ReactControl[1];
		QueryRowSetBinding binding = new QueryRowSetBinding(
			() -> tlObjectRows(refreshRows(rowsExecutor, ChannelInputs.arguments(inputChannels), diagnosticsTarget[0])),
			createType != null ? createType : (rowType instanceof TLClass rowClass ? rowClass : null),
			createType == null ? List.of() : List.of(createType),
			_config.getOnRemove());

		ChannelRef selectionRef = _config.getSelection();
		ViewChannel selectionChannel = selectionRef != null ? context.resolveChannel(selectionRef) : null;

		ViewCommandModel activation = activationModel(context);

		List<ColumnDeclaration> declarations = columns(rowType);
		List<String> declaredNames = ColumnDeclarations.declaredNames(declarations);
		RowSetTableControl control =
			new RowSetTableControl(context, formControl, binding, declarations, _config.getRowEdit());
		diagnosticsTarget[0] = control;
		control.setCssClass(_config.getCssClass());
		applyRowDiagnostics(control, initialRows.securityReport());
		control.setFramed(false);
		control.setPersonalization(PersonalConfigViewStateStore.INSTANCE, tableId());
		control.setNamedFilters(
			columns -> declaredFilters(columns, declaredNames, ChannelInputs.arguments(inputChannels)),
			filterStore(), initialFilter());
		control.setFilterChannels(channel(context, _config.getActivePreset()),
			channel(context, _config.getSearchTerm()));
		control.setFilterBar(filterBar());
		control.setDefaultSort(defaultSort());
		control.setGrouping(initialGrouping());
		control.setFixedColumns(_config.getFixedColumns());
		control.setSelectionChannel(selectionChannel);
		control.setSelectionMode(_config.getSelectionMode());
		control.setRowRefresh(args -> refreshRows(rowsExecutor, args, control),
			ObservedTypes.resolve(_config.getObservedTypes()), inputChannels);
		control.setActivationHandler(activationHandler(context, activation));
		control.setTrailingColumns(this.<TLObject> rowCommandColumns(context, activation));
		control.init();

		contributeAddRowCommand(context, formControl, binding, control);

		return control;
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
	 * The outcome of a rows query: the rows it delivers, and what the security filter removed from
	 * them.
	 *
	 * @param rows
	 *        The rows to display.
	 * @param securityReport
	 *        The objects the current user must not read, which the query result therefore does not
	 *        contain.
	 */
	private record RowsResult(Collection<?> rows, SecurityFilterReport securityReport) {
		// Pure data.
	}

	/**
	 * Executes the rows query, observing what the current user's read rights removed from its
	 * result.
	 *
	 * @param rowsExecutor
	 *        The compiled rows expression.
	 * @param channelValues
	 *        The values of the input channels, passed as the expression arguments.
	 *
	 * @see #applyRowDiagnostics(ReactControl, SecurityFilterReport)
	 */
	private static RowsResult executeRows(QueryExecutor rowsExecutor, Object[] channelValues) {
		SecurityFilterReport securityReport = new SecurityFilterReport();
		EvalContext definitions = rowsExecutor.context();
		definitions.setSecurityReport(securityReport);
		Object result = rowsExecutor.executeWith(definitions, Args.some(channelValues));
		return new RowsResult(toRows(result), securityReport);
	}

	/**
	 * Executes the rows query and reports its {@link SecurityFilterReport} to the given control.
	 *
	 * @param control
	 *        The control displaying the rows, {@code null} while it is not built yet.
	 *
	 * @see #executeRows(QueryExecutor, Object[])
	 */
	private static Collection<?> refreshRows(QueryExecutor rowsExecutor, Object[] channelValues,
			ReactControl control) {
		RowsResult result = executeRows(rowsExecutor, channelValues);
		if (control != null) {
			applyRowDiagnostics(control, result.securityReport());
		}
		return result.rows();
	}

	private static Collection<?> toRows(Object result) {
		if (result instanceof Collection<?> collection) {
			return collection;
		}
		return result == null ? Collections.emptyList() : Collections.singletonList(result);
	}

}
