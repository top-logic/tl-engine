/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.table.ReactCellControlProvider;
import com.top_logic.layout.react.control.table.ReactTableControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.layout.view.model.ObservableTableModel;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Declarative {@link UIElement} that wraps a {@link ReactTableControl}.
 *
 * <p>
 * Replaces the legacy {@code ListModelBuilder} + {@code LayoutComponent} pattern. Input data is
 * provided via {@link ViewChannel}s, and rows are computed using TL-Script expressions.
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

		/** Configuration name for {@link #getSupportsElement()}. */
		String SUPPORTS_ELEMENT = "supportsElement";

		/** Configuration name for {@link #getModelForElement()}. */
		String MODEL_FOR_ELEMENT = "modelForElement";

		/** Configuration name for {@link #getTypes()}. */
		String TYPES = "types";

		/** Configuration name for {@link #getColumns()}. */
		String COLUMNS = "columns";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/** Configuration name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		/**
		 * Types to observe for object creation events.
		 *
		 * <p>
		 * When configured, the table re-evaluates its row function when objects of these types
		 * are created. This enables automatic row insertion for tables that use
		 * {@code all(...).filter(...)} style row queries.
		 * </p>
		 *
		 * <p>
		 * When empty (default), only updates and deletes of currently displayed rows are
		 * observed. This is correct for tables whose rows come directly from a channel.
		 * </p>
		 */
		@Name(OBSERVED_TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getObservedTypes();

		/**
		 * References to {@link ViewChannel}s whose current values become positional arguments to
		 * the expression properties.
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();

		/**
		 * TL-Script function computing the row objects.
		 *
		 * <p>
		 * Takes the input channel values as positional arguments and returns a
		 * {@link Collection} of row objects.
		 * </p>
		 */
		@Name(ROWS)
		@Mandatory
		@NonNullable
		Expr getRows();

		/**
		 * Optional TL-Script function for incremental update decisions.
		 *
		 * <p>
		 * Takes the input channel values followed by a candidate object as last argument.
		 * </p>
		 */
		@Name(SUPPORTS_ELEMENT)
		Expr getSupportsElement();

		/**
		 * Optional TL-Script function for reverse model lookup.
		 *
		 * <p>
		 * Takes the input channel values followed by a candidate object as last argument.
		 * </p>
		 */
		@Name(MODEL_FOR_ELEMENT)
		Expr getModelForElement();

		/**
		 * Comma-separated list of qualified TL type names for automatic column derivation.
		 *
		 * <p>
		 * When set, columns are derived from the type's attributes using
		 * {@link GenericTableConfigurationProvider}. Takes priority over {@link #getColumns()}.
		 * </p>
		 */
		@Name(TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getTypes();

		/**
		 * Explicit list of columns to display.
		 *
		 * <p>
		 * When set, only the listed columns are shown, in the given order. Column metadata (label,
		 * accessor, renderer) is still derived from {@link #getTypes()}. When empty, all columns
		 * derived from {@link #getTypes()} are shown.
		 * </p>
		 */
		@Name(COLUMNS)
		ColumnsConfig getColumns();

		/**
		 * Optional reference to a {@link ViewChannel} to write the selected row object(s) to.
		 */
		@Name(SELECTION)
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();
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

		/**
		 * The name of the attribute (column) to display.
		 */
		@Name(ATTRIBUTE)
		@Mandatory
		String getAttribute();
	}

	private final Config _config;

	private final QueryExecutor _rowsExecutor;

	/**
	 * Creates a new {@link TableElement} from configuration.
	 *
	 * <p>
	 * Expressions are compiled once here and shared across all sessions. If services like
	 * {@code PersistencyLayer} are not yet active, {@link QueryExecutor#compile(Expr)} returns a
	 * {@code DeferredQueryExecutor} that lazily compiles on first execution.
	 * </p>
	 */
	@CalledByReflection
	public TableElement(InstantiationContext context, Config config) {
		_config = config;

		_rowsExecutor = QueryExecutor.compile(config.getRows());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// 1. Resolve input channels.
		List<ChannelRef> inputRefs = _config.getInputs();
		List<ViewChannel> inputChannels = new ArrayList<>(inputRefs.size());
		for (ChannelRef ref : inputRefs) {
			inputChannels.add(context.resolveChannel(ref));
		}

		// 2. Execute initial row query.
		Object[] channelValues = readChannelValues(inputChannels);
		Collection<?> rows = executeRowsQuery(_rowsExecutor, channelValues);

		// 3. Build table configuration.
		TableConfiguration tableConfig;
		List<TLModelPartRef> typeRefs = _config.getTypes();
		if (typeRefs != null && !typeRefs.isEmpty()) {
			Set<TLClass> types = resolveTypes(typeRefs);
			tableConfig = TableConfigurationFactory.build(new GenericTableConfigurationProvider(types));
		} else {
			tableConfig = TableConfigurationFactory.table();
		}

		// 4. Build column names from configuration.
		List<String> explicitColumns = explicitColumnNames();
		List<String> columnNames = explicitColumns;
		if (columnNames == null) {
			columnNames = new ArrayList<>(tableConfig.getDefaultColumns());
			if (columnNames.isEmpty()) {
				columnNames = new ArrayList<>(tableConfig.createColumnIndex().keySet());
			}
		}

		// 5. Create ObjectTableModel.
		ObjectTableModel tableModel =
			new ObjectTableModel(columnNames, tableConfig, new ArrayList<>(rows));
		if (explicitColumns != null) {
			// The ObjectTableModel constructor makes all declared columns visible (the column name
			// list only defines their order). Restrict the displayed columns to the explicitly
			// configured ones.
			tableModel.getHeader().setVisibleColumns(explicitColumns);
		}

		// 6. Wrap in ObservableTableModel.
		Set<TLStructuredType> observedTypes = resolveObservedTypes();
		QueryExecutor rowsExec = _rowsExecutor;
		ObservableTableModel observableModel = new ObservableTableModel(
			tableModel,
			args -> executeRowsQuery(rowsExec, args),
			observedTypes,
			inputChannels
		);

		// 7. Create cell provider.
		ReactCellControlProvider cellProvider = createCellProvider(context);

		// 8. Create ReactTableControl (uses inner model directly).
		ReactTableControl tableControl = new ReactTableControl(context, tableModel, cellProvider);

		// 9. Wire selection channel.
		ChannelRef selectionRef = _config.getSelection();
		if (selectionRef != null) {
			ViewChannel selectionChannel = context.resolveChannel(selectionRef);
			tableControl.setSelectionListener(new ReactTableControl.SelectionListener() {
				@Override
				public void selectionChanged(Set<Object> selectedRows) {
					if (selectedRows.size() == 1) {
						selectionChannel.set(selectedRows.iterator().next());
					} else if (selectedRows.isEmpty()) {
						selectionChannel.set(null);
					} else {
						selectionChannel.set(selectedRows);
					}
				}
			});

			// Reflect the channel value in the table's visual selection: seed the initial selection
			// and keep it in sync when the channel changes from elsewhere (e.g. a sibling component,
			// or this table being rebuilt while the channel still holds a value - a responsive
			// master-detail flipping between split and drill presentations). setSelectedRows does not
			// fire the selection listener, so there is no write-back loop.
			ViewChannel.ChannelListener selectionToTable =
				(sender, oldValue, newValue) -> tableControl.setSelectedRows(toRowSet(newValue));
			selectionChannel.addListener(selectionToTable);
			tableControl.addCleanupAction(() -> selectionChannel.removeListener(selectionToTable));
			tableControl.setSelectedRows(toRowSet(selectionChannel.get()));
		}

		// 10. Lazy attach on render, cleanup on dispose.
		tableControl.addBeforeWriteAction(() -> {
			observableModel.attach(context.getModelScope());
		});
		tableControl.addCleanupAction(observableModel::detach);

		return tableControl;
	}

	/**
	 * Converts a selection channel value (a single object, a collection, or {@code null}) into the
	 * set of selected row objects expected by {@link ReactTableControl#setSelectedRows(Set)}.
	 */
	private static Set<Object> toRowSet(Object value) {
		if (value == null) {
			return Collections.emptySet();
		}
		if (value instanceof Collection<?>) {
			return new HashSet<>((Collection<?>) value);
		}
		return Collections.singleton(value);
	}

	private static Object[] readChannelValues(List<ViewChannel> channels) {
		Object[] values = new Object[channels.size()];
		for (int i = 0; i < channels.size(); i++) {
			values[i] = channels.get(i).get();
		}
		return values;
	}

	private static Collection<?> executeRowsQuery(QueryExecutor rowsExecutor, Object[] channelValues) {
		Object result = rowsExecutor.execute(channelValues);
		if (result instanceof Collection<?>) {
			return (Collection<?>) result;
		}
		if (result == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(result);
	}

	/**
	 * The explicitly configured column names, or {@code null} if {@link Config#getColumns()} is not
	 * set (so that the type-derived default columns are used).
	 */
	private List<String> explicitColumnNames() {
		ColumnsConfig columnsConfig = _config.getColumns();
		if (columnsConfig == null) {
			return null;
		}
		List<ColumnConfig> columns = columnsConfig.getColumns();
		if (columns.isEmpty()) {
			return null;
		}
		List<String> columnNames = new ArrayList<>(columns.size());
		for (ColumnConfig column : columns) {
			columnNames.add(column.getAttribute());
		}
		return columnNames;
	}

	private static Set<TLClass> resolveTypes(List<TLModelPartRef> typeRefs) {
		Set<TLClass> types = new HashSet<>();
		for (TLModelPartRef ref : typeRefs) {
			try {
				types.add(ref.resolveClass());
			} catch (ConfigurationException ex) {
				throw new RuntimeException("Failed to resolve type: " + ref.qualifiedName(), ex);
			}
		}
		return types;
	}

	private ReactCellControlProvider createCellProvider(ViewContext context) {
		return (ctx, rowObject, columnName, cellValue) -> {
			return new ReactTextControl(ctx, MetaLabelProvider.INSTANCE.getLabel(cellValue));
		};
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
}
