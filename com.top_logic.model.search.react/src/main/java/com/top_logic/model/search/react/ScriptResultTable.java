/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.react;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.provider.MetaLabelProvider;
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
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Single-column table showing a script evaluation result, one row per result element.
 *
 * <p>
 * App-specific console widget (referenced by {@code class=}, not a reusable {@code @TagName}
 * element). The {@link Config#getInput() input channel} holds the raw evaluation result: a
 * {@link Collection} or array yields one row per element, any other non-{@code null} value yields a
 * single row, and {@code null} yields an empty table. Each row renders its value through the
 * {@link MetaLabelProvider} in the {@code result} column. A written result replaces the rows, so the
 * table updates after each evaluation.
 * </p>
 */
public class ScriptResultTable implements UIElement {

	/**
	 * One result row, wrapping a result element with its position so duplicate values keep distinct
	 * row keys.
	 *
	 * @param index
	 *        The zero-based position of the element in the result, used as the row key.
	 * @param value
	 *        The result element rendered in the {@code result} column, may be {@code null}.
	 */
	public record ScriptResultRow(int index, Object value) {
		// Marker record; components documented above.
	}

	/**
	 * Configuration for {@link ScriptResultTable}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		@Override
		@ClassDefault(ScriptResultTable.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel holding the raw evaluation result to display.
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();
	}

	private final ChannelRef _inputRef;

	/**
	 * Creates a new {@link ScriptResultTable} from configuration.
	 */
	@CalledByReflection
	public ScriptResultTable(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<Column<ScriptResultRow, ?>> columns = new ArrayList<>();
		columns.add(DefaultColumn.<ScriptResultRow, String> builder("result", row -> label(row.value()))
			.label(I18NConstants.SCRIPT_RESULT_COLUMN)
			.renderer(CellContent::text)
			.build());

		ViewChannel dataChannel = _inputRef != null ? context.resolveChannel(_inputRef) : null;

		ListRowSource<ScriptResultRow> source = new ListRowSource<>(
			rows(dataChannel == null ? null : dataChannel.get()), columns, row -> Integer.valueOf(row.index()));
		DefaultTableView<ScriptResultRow> view = DefaultTableView.create(columns, source);
		TableViewControl<ScriptResultRow> control = new TableViewControl<>(context, view, false);

		if (dataChannel != null) {
			ChannelListener listener = (sender, oldValue, newValue) -> {
				source.setElements(rows(newValue));
				control.refreshData();
			};
			dataChannel.addListener(listener);
			control.addCleanupAction(() -> dataChannel.removeListener(listener));
		}

		return control;
	}

	/**
	 * The result value split into table rows: one row per element of a {@link Collection} or array,
	 * a single row for any other non-{@code null} value, and no rows for {@code null}.
	 */
	private static List<ScriptResultRow> rows(Object value) {
		List<ScriptResultRow> rows = new ArrayList<>();
		if (value == null) {
			return rows;
		}
		if (value instanceof Collection<?> collection) {
			int index = 0;
			for (Object element : collection) {
				rows.add(new ScriptResultRow(index++, element));
			}
		} else if (value instanceof Object[] array) {
			for (int i = 0; i < array.length; i++) {
				rows.add(new ScriptResultRow(i, array[i]));
			}
		} else {
			rows.add(new ScriptResultRow(0, value));
		}
		return rows;
	}

	/**
	 * The label of the given result element, or the empty string for {@code null}.
	 */
	private static String label(Object value) {
		return value == null ? "" : MetaLabelProvider.INSTANCE.getLabel(value);
	}
}
