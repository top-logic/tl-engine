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
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.react.control.table.ReactCellControlProvider;
import com.top_logic.layout.react.control.table.ReactTableControl;
import com.top_logic.layout.react.control.table.ReactTextCellControl;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
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

		/** Configuration name for {@link #getColumns()}. */
		String COLUMNS = "columns";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

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
		 * Column configuration for the table.
		 */
		@Name(COLUMNS)
		PolymorphicConfiguration<TableConfigurationProvider> getColumns();

		/**
		 * Optional reference to a {@link ViewChannel} to write the selected row object(s) to.
		 */
		@Name(SELECTION)
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();
	}

	private final Config _config;

	private final TableConfigurationProvider _columnsProvider;

	/**
	 * Creates a new {@link TableElement} from configuration.
	 *
	 * <p>
	 * Expression compilation is deferred to {@link #createControl(ViewContext)} because services
	 * like {@code PersistencyLayer} may not be available during Phase 1 (config parsing).
	 * </p>
	 */
	@CalledByReflection
	public TableElement(InstantiationContext context, Config config) {
		_config = config;
		_columnsProvider = context.getInstance(config.getColumns());
	}

	@Override
	public ViewControl createControl(ViewContext context) {
		// Compile expressions (defers if services are not running).
		QueryExecutor rowsExecutor = QueryExecutor.compile(_config.getRows());
		QueryExecutor supportsElementExecutor = QueryExecutor.compileOptional(_config.getSupportsElement());
		QueryExecutor modelForElementExecutor = QueryExecutor.compileOptional(_config.getModelForElement());

		// 1. Resolve input channels.
		List<ChannelRef> inputRefs = _config.getInputs();
		List<ViewChannel> inputChannels = new ArrayList<>(inputRefs.size());
		for (ChannelRef ref : inputRefs) {
			inputChannels.add(context.resolveChannel(ref));
		}

		// 2. Execute initial row query.
		Object[] channelValues = readChannelValues(inputChannels);
		Collection<?> rows = executeRowsQuery(rowsExecutor, channelValues);

		// 3. Build table configuration.
		TableConfiguration tableConfig;
		if (_columnsProvider != null) {
			tableConfig = TableConfigurationFactory.build(_columnsProvider);
		} else {
			tableConfig = TableConfigurationFactory.table();
		}

		// 4. Build column names from configuration.
		List<String> columnNames = new ArrayList<>(tableConfig.createColumnIndex().keySet());

		// 5. Create ObjectTableModel.
		ObjectTableModel tableModel =
			new ObjectTableModel(columnNames, tableConfig, new ArrayList<>(rows));

		// 6. Create cell provider.
		ReactCellControlProvider cellProvider = createCellProvider();

		// 7. Create ReactTableControl.
		ReactTableControl tableControl = new ReactTableControl(tableModel, cellProvider);

		// 8. Wire selection channel.
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
		}

		// 9. Wire input channel listeners for re-query on change.
		ViewChannel.ChannelListener refreshListener = (sender, oldValue, newValue) -> {
			Object[] newValues = readChannelValues(inputChannels);
			Collection<?> newRows = executeRowsQuery(rowsExecutor, newValues);
			tableModel.setRowObjects(new ArrayList<>(newRows));
		};
		for (ViewChannel channel : inputChannels) {
			channel.addListener(refreshListener);
		}

		return tableControl;
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

	private ReactCellControlProvider createCellProvider() {
		return (rowObject, columnName, cellValue) -> {
			return new ReactTextCellControl(MetaLabelProvider.INSTANCE.getLabel(cellValue));
		};
	}
}
