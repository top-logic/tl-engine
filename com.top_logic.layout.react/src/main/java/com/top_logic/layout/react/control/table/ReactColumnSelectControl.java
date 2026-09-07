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
 * each with a checkbox deciding whether it is displayed.
 *
 * <p>
 * The control edits a working copy - a table's columns change only when the surrounding dialog
 * applies {@link #visibleColumns()}, so cancelling discards the edits. Both the order and the
 * checkboxes live on the server: every gesture is a command, and the resulting list is pushed back,
 * exactly as dragging a column header is handled by the {@link TableViewControl}.
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

	/** Command reordering the list (drag and drop). */
	private static final String CMD_COLUMN_REORDER = "columnReorder";

	/** Command showing or hiding one column. */
	private static final String CMD_COLUMN_VISIBLE = "columnVisible";

	/** The edited columns, in the edited order. */
	private final List<ColumnOption> _entries;

	/**
	 * Creates a {@link ReactColumnSelectControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param options
	 *        The columns to offer, in initial display order (see
	 *        {@link com.top_logic.table.TableView#columnOptions()}).
	 */
	public ReactColumnSelectControl(ReactContext context, List<ColumnOption> options) {
		super(context, null, "TLColumnSelect");
		_entries = new ArrayList<>(options);
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

	private void pushEntries() {
		Resources resources = Resources.getInstance();
		List<Map<String, Object>> entries = new ArrayList<>(_entries.size());
		for (ColumnOption entry : _entries) {
			Map<String, Object> state = new LinkedHashMap<>();
			state.put(ENTRY_NAME, entry.name());
			state.put(ENTRY_LABEL, resources.getString(entry.label()));
			state.put(ENTRY_VISIBLE, Boolean.valueOf(entry.visible()));
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

	private int indexOf(String column) {
		for (int n = 0; n < _entries.size(); n++) {
			if (_entries.get(n).name().equals(column)) {
				return n;
			}
		}
		return -1;
	}

}
