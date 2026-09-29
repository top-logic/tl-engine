/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.monitor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.sql.LoggingDataSourceProxy.Statistics;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Read-only table of captured SQL statement statistics, one row per normalized statement with its
 * call count, total / average / maximum execution time (ms) and processed row count. The rows come
 * from an input channel fed by {@link SqlMonitorAction}, so the table updates when monitoring is
 * started, refreshed or stopped.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element).
 * </p>
 */
public class SqlStatisticsTable implements UIElement {

	private static final long NANOS_PER_MILLI = 1_000_000L;

	/**
	 * Configuration for {@link SqlStatisticsTable}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		@Override
		@ClassDefault(SqlStatisticsTable.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel holding the captured statistics ({@code List<Map.Entry<String, Statistics>>}).
		 */
		@Name(INPUT)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();
	}

	private final ChannelRef _inputRef;

	/**
	 * Creates a new {@link SqlStatisticsTable} from configuration.
	 */
	@CalledByReflection
	public SqlStatisticsTable(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<Column<Entry<String, Statistics>, ?>> columns = new ArrayList<>();
		columns.add(DefaultColumn.<Entry<String, Statistics>, String> builder("statement", Entry::getKey)
			.label(I18NConstants.SQL_COLUMN_STATEMENT)
			.renderer(CellContent::text)
			.sort(() -> Comparator.<String> naturalOrder())
			.filter(new TextColumnFilter<>(text -> text))
			.width(640)
			.build());
		columns.add(numberColumn("calls", I18NConstants.SQL_COLUMN_CALLS, e -> (long) e.getValue().getCnt(), 90));
		columns.add(numberColumn("total", I18NConstants.SQL_COLUMN_TIME_TOTAL,
			e -> e.getValue().getElapsedNanoSum() / NANOS_PER_MILLI, 120));
		columns.add(numberColumn("avg", I18NConstants.SQL_COLUMN_TIME_AVG,
			e -> e.getValue().getElapsedNanoAvg() / NANOS_PER_MILLI, 120));
		columns.add(numberColumn("max", I18NConstants.SQL_COLUMN_TIME_MAX,
			e -> e.getValue().getElapsedNanoMax() / NANOS_PER_MILLI, 120));
		columns.add(numberColumn("rows", I18NConstants.SQL_COLUMN_ROWS, e -> e.getValue().getRowsSum(), 90));

		ViewChannel channel = context.resolveChannel(_inputRef);
		ListRowSource<Entry<String, Statistics>> source = new ListRowSource<>(rows(channel.get()), columns, Entry::getKey);
		DefaultTableView<Entry<String, Statistics>> view = DefaultTableView.create(columns, source);
		TableViewControl<Entry<String, Statistics>> control = new TableViewControl<>(context, view, false);

		ChannelListener listener = (sender, oldValue, newValue) -> {
			source.setElements(rows(newValue));
			control.refreshData();
		};
		channel.addListener(listener);
		control.addCleanupAction(() -> channel.removeListener(listener));

		return control;
	}

	/**
	 * The statistics entries held by the channel value, or an empty list when nothing is captured.
	 */
	@SuppressWarnings("unchecked")
	private static List<Entry<String, Statistics>> rows(Object value) {
		if (value instanceof List<?> list) {
			return (List<Entry<String, Statistics>>) list;
		}
		return List.of();
	}

	/**
	 * A sortable, numeric column reading one {@link Long} metric of a statistics entry.
	 */
	private static Column<Entry<String, Statistics>, Long> numberColumn(String id, ResKey label,
			java.util.function.Function<? super Entry<String, Statistics>, Long> value, int width) {
		return DefaultColumn.<Entry<String, Statistics>, Long> builder(id, value)
			.label(label)
			.renderer(number -> CellContent.text(number == null ? "" : Long.toString(number)))
			.sort(() -> Comparator.<Long> naturalOrder())
			.width(width)
			.build();
	}
}
