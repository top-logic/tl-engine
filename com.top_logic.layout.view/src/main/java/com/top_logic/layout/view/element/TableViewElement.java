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
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.table.Column;
import com.top_logic.table.TableId;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.table.impl.PersonalConfigViewStateStore;

/**
 * Declarative {@link UIElement} that renders the green-field table model
 * ({@link com.top_logic.table.TableView}) via a {@link TableViewControl}.
 *
 * <p>
 * The new React-style counterpart to {@link TableElement}: input data comes from
 * {@link ViewChannel}s, rows are computed by a TL-Script expression, and each configured
 * column reads a model attribute. Columns are sortable and text-filterable by their cell
 * label, with no dependency on the legacy {@code TableModel}.
 * </p>
 */
public class TableViewElement implements UIElement {

	/**
	 * Configuration for {@link TableViewElement}.
	 */
	@TagName("table-view")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(TableViewElement.class)
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
		TableElement.ColumnsConfig getColumns();

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
	}

	private final Config _config;

	private final QueryExecutor _rowsExecutor;

	/**
	 * Creates a {@link TableViewElement} from configuration.
	 */
	@CalledByReflection
	public TableViewElement(InstantiationContext context, Config config) {
		_config = config;
		_rowsExecutor = QueryExecutor.compile(config.getRows());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ViewChannel> inputChannels = new ArrayList<>();
		for (ChannelRef ref : _config.getInputs()) {
			inputChannels.add(context.resolveChannel(ref));
		}
		Collection<?> rows = executeRowsQuery(_rowsExecutor, readChannelValues(inputChannels));

		List<Column<Object, ?>> columns = buildColumns(resolveRowType(rows));
		ListRowSource<Object> source = new ListRowSource<>(new ArrayList<>(rows), columns);
		DefaultTableView<Object> view =
			DefaultTableView.create(columns, source, PersonalConfigViewStateStore.INSTANCE, tableId());

		TableViewControl<Object> control = new TableViewControl<>(context, view, false);

		ChannelRef selectionRef = _config.getSelection();
		if (selectionRef != null) {
			ViewChannel selectionChannel = context.resolveChannel(selectionRef);
			control.setSelectionListener(selectedKeys -> {
				if (selectedKeys.size() == 1) {
					selectionChannel.set(selectedKeys.iterator().next());
				} else if (selectedKeys.isEmpty()) {
					selectionChannel.set(null);
				} else {
					selectionChannel.set(selectedKeys);
				}
			});
		}

		// Refresh the rows when observed objects change or an input channel changes.
		QueryExecutor rowsExecutor = _rowsExecutor;
		RowSourceObserver<Object> observer = new RowSourceObserver<>(
			source,
			args -> new ArrayList<>(executeRowsQuery(rowsExecutor, args)),
			resolveObservedTypes(),
			inputChannels,
			control::refreshData);
		control.addBeforeWriteAction(() -> observer.attach(context.getModelScope()));
		control.addCleanupAction(observer::detach);

		return control;
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
		TableElement.ColumnsConfig columnsConfig = _config.getColumns();
		if (columnsConfig != null) {
			for (TableElement.ColumnConfig columnConfig : columnsConfig.getColumns()) {
				key.append(columnConfig.getAttribute()).append(',');
			}
		}
		return new TableId(key.toString());
	}

	private List<Column<Object, ?>> buildColumns(TLStructuredType rowType) {
		ColumnProviderService columns0 = ColumnProviderService.getInstance();
		TableElement.ColumnsConfig columnsConfig = _config.getColumns();
		List<Column<Object, ?>> columns = new ArrayList<>();
		if (columnsConfig != null && !columnsConfig.getColumns().isEmpty()) {
			for (TableElement.ColumnConfig columnConfig : columnsConfig.getColumns()) {
				String attribute = columnConfig.getAttribute();
				TLStructuredTypePart part = rowType == null ? null : rowType.getPart(attribute);
				columns.add(columns0.createColumn(attribute, columnLabel(part, attribute), part));
			}
		} else if (rowType != null) {
			// No explicit columns configured: derive a default set from the row type's
			// non-hidden attributes, in declaration order.
			for (TLStructuredTypePart part : rowType.getAllParts()) {
				if (DisplayAnnotations.isHidden(part)) {
					continue;
				}
				columns.add(columns0.createColumn(part.getName(), columnLabel(part, part.getName()), part));
			}
		}
		if (columns.isEmpty()) {
			throw new IllegalStateException(
				"A <table-view> requires either explicit <column>s or a resolvable row type to derive them from.");
		}
		return columns;
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
