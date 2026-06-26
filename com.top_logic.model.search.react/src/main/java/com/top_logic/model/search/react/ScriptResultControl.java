/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.react;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.resources.TLTypePartResourceProvider;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Renders a script evaluation result as a table whose columns are derived from the result and
 * rebuilt on every evaluation.
 *
 * <p>
 * The result is split into rows (one per element of a collection/array, a single row for a scalar,
 * none for {@code null}). The columns depend on the row values:
 * </p>
 * <ul>
 * <li>For {@link TLObject} rows, one column per attribute - the union of all attributes across the
 * (possibly different) object types present, each cell showing that object's value for the
 * attribute (empty when the object's type lacks it).</li>
 * <li>A leading {@code result} column showing the element label is added whenever a non-object
 * (primitive) value is present, or when there are no object attributes at all (the all-primitive
 * case).</li>
 * </ul>
 *
 * <p>
 * A {@link DefaultTableView}'s columns are fixed at construction, so a changed result rebuilds the
 * whole {@link TableViewControl} and swaps it in. This control is the wrapper that performs the
 * swap: it reuses the {@code TLStack} client container (no bespoke client component) and replaces
 * its single child - the result table - on each evaluation.
 * </p>
 *
 * @implNote The replaced table is disposed synchronously: its cell controls do not listen to the
 *           result channel that triggers the rebuild, so there is no re-entrant teardown.
 */
public class ScriptResultControl extends ReactControl {

	/** {@code TLStack} state key holding the child controls (here a single result table). */
	private static final String CHILDREN = "children";

	/** {@code TLStack} state key letting the first (here only) child fill the stack. */
	private static final String GROW_FIRST = "growFirst";

	/** Column id of the label column shown for primitive (non-object) results. */
	private static final String RESULT_COLUMN = "result";

	/**
	 * One result row, wrapping a result element with its position so duplicate values keep distinct
	 * row keys.
	 *
	 * @param index
	 *        The zero-based position of the element in the result, used as the row key.
	 * @param value
	 *        The result element, may be {@code null}.
	 */
	public record ScriptResultRow(int index, Object value) {
		// Marker record; components documented above.
	}

	private final ViewContext _context;

	private ReactControl _currentTable;

	/**
	 * Creates a new {@link ScriptResultControl}.
	 *
	 * @param context
	 *        The view context used to build the result table.
	 * @param dataChannel
	 *        Channel holding the raw evaluation result; a written result rebuilds the table. May be
	 *        {@code null} for a static empty table.
	 */
	public ScriptResultControl(ViewContext context, ViewChannel dataChannel) {
		super(context, null, "TLStack");
		_context = context;
		putState(GROW_FIRST, Boolean.TRUE);

		_currentTable = buildTable(dataChannel == null ? null : dataChannel.get());
		putState(CHILDREN, List.of(_currentTable));

		if (dataChannel != null) {
			ChannelListener listener = (sender, oldValue, newValue) -> rebuild(newValue);
			dataChannel.addListener(listener);
			addCleanupAction(() -> dataChannel.removeListener(listener));
		}
	}

	private void rebuild(Object value) {
		ReactControl old = _currentTable;
		ReactControl built = buildTable(value);
		_currentTable = built;

		Object token = beginUpdate();
		putState(CHILDREN, List.of(built));
		commitUpdate(token);

		if (old != null && old != built) {
			old.cleanupTree();
		}
	}

	private ReactControl buildTable(Object value) {
		List<ScriptResultRow> rows = rows(value);
		List<Column<ScriptResultRow, ?>> columns = columns(rows);
		ListRowSource<ScriptResultRow> source =
			new ListRowSource<>(rows, columns, row -> Integer.valueOf(row.index()));
		DefaultTableView<ScriptResultRow> view = DefaultTableView.create(columns, source);
		return new TableViewControl<>(_context, view, false);
	}

	/**
	 * The columns for the given rows: one per object attribute (union across all object types
	 * present), plus a leading label column when a non-object value is present or no object
	 * attributes exist.
	 */
	private static List<Column<ScriptResultRow, ?>> columns(List<ScriptResultRow> rows) {
		Map<String, TLStructuredTypePart> parts = new LinkedHashMap<>();
		boolean hasNonObject = false;
		for (ScriptResultRow row : rows) {
			Object value = row.value();
			if (value instanceof TLObject obj && obj.tType() != null) {
				for (TLStructuredTypePart part : obj.tType().getAllParts()) {
					parts.putIfAbsent(part.getName(), part);
				}
			} else {
				hasNonObject = true;
			}
		}

		List<Column<ScriptResultRow, ?>> columns = new ArrayList<>();
		if (hasNonObject || parts.isEmpty()) {
			columns.add(DefaultColumn.<ScriptResultRow, String> builder(RESULT_COLUMN, row -> label(row.value()))
				.label(I18NConstants.SCRIPT_RESULT_COLUMN)
				.renderer(CellContent::text)
				.build());
		}
		for (TLStructuredTypePart part : parts.values()) {
			String name = part.getName();
			columns.add(DefaultColumn.<ScriptResultRow, String> builder(name, row -> attribute(row.value(), name))
				.label(TLTypePartResourceProvider.labelKey(part))
				.renderer(CellContent::text)
				.build());
		}
		return columns;
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
	 * The label of the value's attribute {@code partName}, or the empty string when the value is not
	 * a {@link TLObject} or its type lacks that attribute.
	 */
	private static String attribute(Object value, String partName) {
		if (value instanceof TLObject obj) {
			TLStructuredType type = obj.tType();
			if (type != null && type.getPart(partName) != null) {
				return label(obj.tValueByName(partName));
			}
		}
		return "";
	}

	/**
	 * The label of the given value, or the empty string for {@code null}.
	 */
	private static String label(Object value) {
		return value == null ? "" : MetaLabelProvider.INSTANCE.getLabel(value);
	}

	@Override
	protected void cleanupChildren() {
		if (_currentTable != null) {
			_currentTable.cleanupTree();
			_currentTable = null;
		}
	}
}
