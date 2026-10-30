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
import java.util.function.Function;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.dnd.DropArguments;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.basic.config.ConfigurationItem;
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
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.CommandModel;
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
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormModel;
import com.top_logic.layout.view.form.QueryRowSetBinding;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetBinding;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.layout.view.model.TableSelectionBinding;
import com.top_logic.layout.view.security.ModelAccessPolicy;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnDeclarations;
import com.top_logic.layout.view.table.TableDropConfig;
import com.top_logic.layout.view.table.RowClassConfig;
import com.top_logic.layout.view.table.RowClassFunction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SecurityFilterReport;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.TableId;
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
	public interface Config extends AbstractTableElement.Config, RowClassConfig {

		/** Access to the default methods of this interface for the configuration proxy. */
		Lookup LOOKUP = MethodHandles.lookup();

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

		/** Configuration name for {@link #getCreateTypeExpr()}. */
		String CREATE_TYPE_EXPR = "create-type-expr";

		/** Configuration name for {@link #getOnRemove()}. */
		String ON_REMOVE = "on-remove";

		/** Configuration name for {@link #getRowCount()}. */
		String ROW_COUNT = "row-count";

		/** Configuration name for {@link #getDrops()}. */
		String DROPS = "drops";

		/**
		 * TL-Script expression computing the row objects (a {@link Collection}).
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} as variables named after
		 * their channels, or by their {@code as} names, e.g. {@code inputs="selection as project"}
		 * with {@code rows="$project.get(`my.module:Project#tickets`)"}.
		 * </p>
		 */
		@Name(ROWS)
		@Mandatory
		@NonNullable
		@ScriptContextVariables(InputScript.Variables.class)
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
		 * The type of the rows created in edit mode.
		 *
		 * <p>
		 * A concrete type is instantiated directly. An abstract type stands for its concrete
		 * specializations: when the user may create several of them, adding a row opens a menu to
		 * choose the type from. Types the current user may not create are not offered.
		 * </p>
		 *
		 * <p>
		 * When unset, or when no type remains to be offered (an abstract type without concrete
		 * specialization), the table offers no row creation.
		 * </p>
		 *
		 * <p>
		 * Where the type is only known at runtime, {@link #getCreateTypeExpr()} computes it
		 * instead; a table declares at most one of the two.
		 * </p>
		 */
		@Name(CREATE_TYPE)
		TLModelPartRef getCreateType();

		/**
		 * TL-Script expression computing the {@link #getCreateType() create type} from the
		 * {@link #getInputs() inputs}: a model class, or nothing.
		 *
		 * <p>
		 * The computed type means exactly what the same type configured in
		 * {@link #getCreateType()} means: a concrete type is instantiated directly, an abstract one
		 * stands for its concrete specializations the user may create. A result of nothing offers
		 * no row creation, as an unset {@link #getCreateType()} does.
		 * </p>
		 *
		 * <p>
		 * The expression sees the values of the inputs as variables named after their channels, or
		 * by their {@code as} names, e.g. {@code inputs="selectedType as type"} with
		 * {@code create-type-expr="$type"}. It is evaluated once, when the table is built, like
		 * {@link #getTypesExpr()}: to follow a changing type, place the table in a
		 * {@code <switch rebuild="true">} over the input.
		 * </p>
		 *
		 * <p>
		 * A table declares at most one of {@link #getCreateType()} and this expression.
		 * </p>
		 */
		@Name(CREATE_TYPE_EXPR)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getCreateTypeExpr();

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
		 * Whether the table tells below its rows how many rows it has.
		 *
		 * <p>
		 * While a column filter, the search or a filter of the filter bar narrows the rows, the
		 * count tells how many of all rows are displayed. In a table selecting several rows, it
		 * tells how many are selected as well. A grouped table counts its rows, not its groups.
		 * </p>
		 */
		@Name(ROW_COUNT)
		@BooleanDefault(true)
		boolean getRowCount();

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
		@TreeProperty
		List<TableDropConfig> getDrops();

		/**
		 * A {@link #getDrops() drop} runs its actions with the {@link DropArguments arguments} of
		 * its signature.
		 */
		@Override
		default ActionScope actionScope(ConfigurationItem part, ActionScope outer) {
			ActionScope drop = DropArguments.scopeOf(part, getDrops(), config -> config.getTarget().signature());
			if (drop != null) {
				return drop;
			}
			return AbstractTableElement.Config.super.actionScope(part, outer);
		}
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

	private final QueryExecutor _rowsExecutor;

	/** The compiled {@link Config#getCreateTypeExpr()}, {@code null} without one. */
	private final QueryExecutor _createTypeExpr;

	/** The compiled {@link Config#getRowClass() row class function}. */
	private final RowClassFunction _rowClass;

	/** The declared {@link Config#getDrops() drops} with their actions, in declaration order. */
	private final List<DeclaredDrop> _drops;

	/**
	 * Creates a {@link TableElement} from configuration.
	 */
	@CalledByReflection
	public TableElement(InstantiationContext context, Config config) {
		super(context, config);
		_rowsExecutor = InputScript.compile(config, Config.ROWS);
		_createTypeExpr = InputScript.compile(config, Config.CREATE_TYPE_EXPR);
		if (_createTypeExpr != null && config.getCreateType() != null) {
			context.error("A <table> states its create type either as a fixed type ('" + Config.CREATE_TYPE
				+ "') or by an expression ('" + Config.CREATE_TYPE_EXPR + "'), not both.");
		}
		_rowClass = RowClassFunction.compile(config);
		_drops = compileDrops(context, config.getDrops(), dropConfig -> dropConfig.getTarget().signature());
		if (config.getRowEdit() != RowEditPolicy.NONE && (config.getDrag() != null || !_drops.isEmpty())) {
			// The editable table is a control of its own, which carries no drag-and-drop seam; a
			// declaration there would apply to nothing.
			context.error("A <table> with '" + Config.ROW_EDIT + "' offers neither <" + Config.DRAG
				+ "> nor <" + DropConfig.TAG_NAME + ">.");
		}
		if (config.getRowEdit() != RowEditPolicy.NONE && config.getEditInPlace()) {
			// A row-edit table buffers its edits in the form, an edit in place writes at once.
			context.error("A <table> with '" + Config.ROW_EDIT + "' is not edited in place ('"
				+ Config.EDIT_IN_PLACE + "').");
		}
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ViewChannel> inputChannels = ChannelInputs.resolve(context, _config.getInputs());
		Object[] inputValues = ChannelInputs.arguments(inputChannels);
		RowsResult initialRows = executeRows(_rowsExecutor, inputValues);
		Collection<?> rows = initialRows.rows();
		RowTypes types = types(inputValues);

		if (_config.getRowEdit() != RowEditPolicy.NONE) {
			return createEditableControl(context, inputChannels, inputValues, types, initialRows);
		}

		Function<Object, String> rowCssClass = _rowClass.bind(inputChannels);
		ReadOnlyTable<ListRowSource<Object>, TableSelectionBinding> table = createTable(context,
			types, resolveRowType(types, rows), inputValues, columns -> new ListRowSource<>(new ArrayList<>(rows), columns), false,
			initialGrouping(), _drops, rowCssClass, ROW_SELECTION);
		TableViewControl<Object> control = table.control();
		applyRowDiagnostics(control, initialRows.securityReport());
		control.setRowCount(_config.getRowCount());

		contributeExportCommand(context, control, table::view,
			() -> ColumnDeclarations.exportRenderers(table.setups()), inputChannels);

		// Refresh the rows when observed objects change or an input channel changes.
		QueryExecutor rowsExecutor = _rowsExecutor;
		Runnable refresh = () -> {
			refreshPresets(table, ChannelInputs.arguments(inputChannels));
			control.refreshData();
			if (table.selectionBinding() != null) {
				table.selectionBinding().rowsRefreshed();
			}
		};
		// An input change leaving the rows as they are - a change of the table's own selection
		// above all - keeps the cells where no column reads that input, and only refreshes what is
		// computed from the inputs apart from the rows.
		InputRoles roles = inputRoles(context, inputChannels);
		Runnable inputsChanged = () -> {
			if (refreshPresets(table, ChannelInputs.arguments(inputChannels))) {
				// Pushes the rows again as well, for their row class.
				control.refreshFilters();
			} else if (rowCssClass != null) {
				control.repaintRows();
			}
		};
		RowSourceObserver<Object> observer = new RowSourceObserver<>(
			table.source(),
			args -> new ArrayList<>(refreshRows(rowsExecutor, args, control)),
			ObservedTypes.resolve(_config.getObservedTypes()),
			inputChannels,
			refresh)
				.withRowIndependentInputs(roles.rowIndependent())
				.onInputsChanged(roles.cellIndependent(), inputsChanged);
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
	 *
	 * @param inputValues
	 *        The values of the input channels the table is built with.
	 * @param types
	 *        The types of the rows for this build of the table.
	 */
	private IReactControl createEditableControl(ViewContext context, List<ViewChannel> inputChannels,
			Object[] inputValues, RowTypes types, RowsResult initialRows) {
		FormModel formModel = context.getFormModel();
		if (!(formModel instanceof FormControl formControl)) {
			throw new IllegalStateException(
				"A <table> with '" + Config.ROW_EDIT + "' requires an enclosing <form>.");
		}

		TLClass createType = createType(inputValues);
		TLStructuredType rowType = resolveRowType(types, initialRows.rows());
		QueryExecutor rowsExecutor = _rowsExecutor;
		// The row function is handed to the binding before the control it reports its diagnostics to
		// exists, so the target is filled in below.
		ReactControl[] diagnosticsTarget = new ReactControl[1];
		QueryRowSetBinding binding = new QueryRowSetBinding(
			() -> tlObjectRows(refreshRows(rowsExecutor, ChannelInputs.arguments(inputChannels), diagnosticsTarget[0])),
			createType != null ? createType : (rowType instanceof TLClass rowClass ? rowClass : null),
			createType == null ? List.of() : List.copyOf(ModelAccessPolicy.concreteTypes(createType)),
			_config.getOnRemove());

		ChannelRef selectionRef = _config.getSelection();
		ViewChannel selectionChannel = selectionRef != null ? context.resolveChannel(selectionRef) : null;

		ViewCommandModel activation = activationModel(context);

		List<ColumnDeclaration> declarations = columns(types, rowType);
		List<String> declaredNames = ColumnDeclarations.declaredNames(declarations);
		TableId tableId = tableId(types);
		RowSetTableControl control =
			new RowSetTableControl(context, formControl, binding, declarations, _config.getRowEdit());
		diagnosticsTarget[0] = control;
		control.setCssClass(_config.getCssClass());
		applyRowDiagnostics(control, initialRows.securityReport());
		control.setFramed(false);
		control.setPersonalization(PersonalConfigViewStateStore.INSTANCE, tableId);
		control.setNamedFilters(
			columns -> declaredFilters(tableId, columns, declaredNames, ChannelInputs.arguments(inputChannels)),
			filterStore(), initialFilter());
		control.setFilterChannels(channel(context, _config.getActivePreset()),
			channel(context, _config.getSearchTerm()));
		control.setFilterBar(filterBar());
		control.setRowCount(_config.getRowCount());
		control.setDefaultSort(defaultSort());
		control.setGrouping(initialGrouping());
		control.setFixedColumns(_config.getFixedColumns());
		control.setSelectionChannel(selectionChannel);
		control.setSelectionMode(_config.getSelectionMode());
		InputRoles roles = inputRoles(context, inputChannels);
		control.setRowRefresh(args -> refreshRows(rowsExecutor, args, control),
			ObservedTypes.resolve(_config.getObservedTypes()), inputChannels, roles.rowIndependent(),
			roles.cellIndependent());
		control.setActivationHandler(activationHandler(context, activation));
		control.setTrailingColumns(this.<TLObject> rowCommandColumns(activation, control::activate));
		control.setRowCssClass(_rowClass.bind(inputChannels));
		control.init();

		contributeAddRowCommand(context, formControl, binding, control);
		contributeExportCommand(context, control, () -> control.getTableControl().getView(),
			control::getExportRenderers, inputChannels);

		return control;
	}

	/**
	 * Contributes the {@link RowSetTableControl#getAddRowCommand() add-row command} of the table to
	 * the enclosing command scope (next to the form's save and cancel commands). The table itself
	 * renders frameless, so it has no own toolbar to host the command.
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

		CommandModel addCommand = control.getAddRowCommand();
		scope.addCommand(addCommand);
		formControl.addCleanupAction(() -> scope.removeCommand(addCommand));
	}

	/**
	 * The type of the rows created in edit mode for a build of this table: the configured
	 * {@link Config#getCreateType() create type}, or the result of {@link Config#getCreateTypeExpr()}
	 * for the given input values.
	 *
	 * @param inputValues
	 *        The values of the {@link Config#getInputs() input channels}, in declaration order;
	 *        not consulted by a table with a fixed create type.
	 * @return The create type, {@code null} for a table offering no row creation.
	 * @throws IllegalStateException
	 *         If {@link Config#getCreateTypeExpr()} computes something else than a model class.
	 */
	public TLClass createType(Object[] inputValues) {
		if (_createTypeExpr != null) {
			Object result = _createTypeExpr.execute(inputValues);
			if (result == null || result instanceof TLClass) {
				return (TLClass) result;
			}
			throw new IllegalStateException("The '" + Config.CREATE_TYPE_EXPR
				+ "' of a <table> must compute a model class, got: " + result);
		}
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
