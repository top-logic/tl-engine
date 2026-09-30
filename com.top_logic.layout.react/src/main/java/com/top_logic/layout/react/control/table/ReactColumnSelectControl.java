/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.table.ColumnOption;
import com.top_logic.util.Resources;

/**
 * The editor of a table's column selection: the columns as a list that can be reordered by dragging,
 * each with a checkbox deciding whether it is displayed and an icon choosing the one the rows are
 * grouped by.
 *
 * <p>
 * The control edits a working copy - a table's columns and grouping change only when the surrounding
 * dialog applies {@link #visibleColumns()} and {@link #groupedColumn()}, so cancelling discards the
 * edits. The order, the checkboxes and the grouping all live on the server: every gesture is a
 * command, and the resulting list is pushed back, exactly as dragging a column header is handled by
 * the {@link TableViewControl}.
 * </p>
 */
public class ReactColumnSelectControl extends ReactControl {

	/** State key of the edited column list. */
	private static final String ENTRIES = "entries";

	/** Per-entry state key of the {@link ColumnOption#name() column name}. */
	private static final String ENTRY_NAME = "name";

	/** Per-entry state key of the column's display label. */
	private static final String ENTRY_LABEL = "label";

	/** Per-entry state key of whether the column is displayed. */
	private static final String ENTRY_VISIBLE = "visible";

	/** Per-entry state key of whether the rows are grouped by the column. */
	private static final String ENTRY_GROUPED = "grouped";

	/** Command reordering the list (drag and drop). */
	private static final String CMD_COLUMN_REORDER = "columnReorder";

	/** Command showing or hiding one column. */
	private static final String CMD_COLUMN_VISIBLE = "columnVisible";

	/** Command choosing the column to group the rows by. */
	private static final String CMD_GROUP_BY = "groupBy";

	/** The edited columns, in the edited order. */
	private final List<ColumnOption> _entries;

	/** The edited grouping: the column to group the rows by, {@code null} for none. */
	private String _groupedColumn;

	/**
	 * Creates a {@link ReactColumnSelectControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param options
	 *        The columns to offer, in initial display order (see
	 *        {@link com.top_logic.table.TableView#columnOptions()}).
	 * @param groupedColumn
	 *        The column the rows are grouped by when the dialog opens, {@code null} for none.
	 */
	public ReactColumnSelectControl(ReactContext context, List<ColumnOption> options, String groupedColumn) {
		super(context, null, "TLColumnSelect");
		_entries = new ArrayList<>(options);
		_groupedColumn = groupedColumn;
		pushEntries();
	}

	/**
	 * The columns to display, in the edited order - the result to apply to the table.
	 */
	public List<String> visibleColumns() {
		List<String> result = new ArrayList<>(_entries.size());
		for (ColumnOption entry : _entries) {
			if (entry.visible()) {
				result.add(entry.name());
			}
		}
		return result;
	}

	/**
	 * The column to group the rows by, {@code null} for none - the second half of the result to
	 * apply to the table.
	 */
	public String groupedColumn() {
		return _groupedColumn;
	}

	private void pushEntries() {
		Resources resources = Resources.getInstance();
		List<Map<String, Object>> entries = new ArrayList<>(_entries.size());
		for (ColumnOption entry : _entries) {
			Map<String, Object> state = new LinkedHashMap<>();
			state.put(ENTRY_NAME, entry.name());
			state.put(ENTRY_LABEL, resources.getString(entry.label()));
			state.put(ENTRY_VISIBLE, Boolean.valueOf(entry.visible()));
			state.put(ENTRY_GROUPED, Boolean.valueOf(entry.name().equals(_groupedColumn)));
			entries.add(state);
		}
		putState(ENTRIES, entries);
	}

	/**
	 * Handles a drag and drop of one list row.
	 */
	@ReactCommandHandler(CMD_COLUMN_REORDER)
	void handleReorder(ColumnReorderArguments args) {
		int from = indexOf(args.getColumn());
		if (from >= 0) {
			ColumnOption entry = _entries.remove(from);
			_entries.add(Math.min(Math.max(args.getTargetIndex(), 0), _entries.size()), entry);
			pushEntries();
		}
	}

	/**
	 * Handles a checkbox toggle.
	 *
	 * <p>
	 * Hiding the last displayed column is refused: a table without columns shows nothing at all,
	 * and nothing in it could bring a column back.
	 * </p>
	 */
	@ReactCommandHandler(CMD_COLUMN_VISIBLE)
	void handleVisible(ColumnVisibleArguments args) {
		int index = indexOf(args.getColumn());
		if (index >= 0 && (args.isVisible() || visibleColumns().size() > 1)) {
			ColumnOption entry = _entries.get(index);
			_entries.set(index, new ColumnOption(entry.name(), entry.label(), args.isVisible()));
			pushEntries();
		}
	}

	/**
	 * Handles the choice of the grouping column.
	 *
	 * <p>
	 * The rows are grouped by one column at a time, so choosing a column moves the grouping there;
	 * choosing the column that already groups them removes the grouping.
	 * </p>
	 */
	@ReactCommandHandler(CMD_GROUP_BY)
	void handleGroupBy(GroupByArguments args) {
		String column = args.getColumn();
		_groupedColumn = column != null && column.equals(_groupedColumn) ? null : column;
		pushEntries();
	}

	private int indexOf(String column) {
		for (int n = 0; n < _entries.size(); n++) {
			if (_entries.get(n).name().equals(column)) {
				return n;
			}
		}
		return -1;
	}

}
