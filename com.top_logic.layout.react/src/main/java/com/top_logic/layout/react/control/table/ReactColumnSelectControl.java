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
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.table.ColumnOption;
import com.top_logic.util.Resources;

/**
 * The column selection of a table as a layer panel: each column with its handle, its name, a lock that freezes it and
 * an eye that shows or hides it.
 *
 * <p>
 * The locked columns form a block at the start of the list - the columns the table freezes. Locking puts a column at
 * the end of the block, releasing it directly behind; a drop into the block locks, a drop behind it releases. A hidden
 * column keeps its place and its lock, but only displayed columns are frozen.
 * </p>
 *
 * <p>
 * The control edits a working copy - a table's columns change only when the surrounding dialog applies
 * {@link #visibleColumns()} and {@link #lockedColumns()}, so cancelling discards the edits. The order, the eyes and
 * the locks all live on the server: every gesture is a command, and the resulting list is pushed back, exactly as
 * dragging a column header is handled by the {@link TableViewControl}.
 * </p>
 */
public class ReactColumnSelectControl extends ReactControl {

	/** Command reordering the list (drag and drop, Alt + arrow). */
	public static final String CMD_COLUMN_REORDER = "columnReorder";

	/** Command showing or hiding one column. */
	public static final String CMD_COLUMN_VISIBLE = "columnVisible";

	/** Command freezing or releasing one column. */
	public static final String CMD_COLUMN_LOCK = "columnLock";

	/** State key of the edited column list. */
	private static final String ENTRIES = "entries";

	/** Per-entry state key of the column name. */
	private static final String ENTRY_NAME = "name";

	/** Per-entry state key of the column's display label. */
	private static final String ENTRY_LABEL = "label";

	/** Per-entry state key of whether the column is displayed. */
	private static final String ENTRY_VISIBLE = "visible";

	/** Per-entry state key of whether the column is frozen. */
	private static final String ENTRY_LOCKED = "locked";

	/** One edited column. */
	private record Entry(String name, ResKey label, boolean visible, boolean locked) {

		Entry withVisible(boolean newVisible) {
			return new Entry(name, label, newVisible, locked);
		}

		Entry withLocked(boolean newLocked) {
			return new Entry(name, label, visible, newLocked);
		}
	}

	/** The edited columns, in the edited order, the locked ones first. */
	private final List<Entry> _entries;

	/**
	 * Creates a {@link ReactColumnSelectControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param options
	 *        The columns to offer, in initial display order (see
	 *        {@link com.top_logic.table.TableView#columnOptions()}).
	 * @param lockedColumns
	 *        The names of the columns the table freezes when the dialog opens.
	 */
	public ReactColumnSelectControl(ReactContext context, List<ColumnOption> options, Set<String> lockedColumns) {
		super(context, null, "TLColumnSelect");
		List<Entry> locked = new ArrayList<>();
		List<Entry> rest = new ArrayList<>();
		for (ColumnOption option : options) {
			boolean isLocked = lockedColumns.contains(option.name());
			(isLocked ? locked : rest).add(new Entry(option.name(), option.label(), option.visible(), isLocked));
		}
		_entries = new ArrayList<>(locked);
		_entries.addAll(rest);
		pushEntries();
	}

	/**
	 * The columns to display, in the edited order - the result to apply to the table.
	 */
	public List<String> visibleColumns() {
		List<String> result = new ArrayList<>(_entries.size());
		for (Entry entry : _entries) {
			if (entry.visible()) {
				result.add(entry.name());
			}
		}
		return result;
	}

	/**
	 * The displayed columns to freeze, in the edited order - the second half of the result.
	 */
	public List<String> lockedColumns() {
		List<String> result = new ArrayList<>();
		for (Entry entry : _entries) {
			if (entry.locked() && entry.visible()) {
				result.add(entry.name());
			}
		}
		return result;
	}

	/**
	 * All columns in the edited order, displayed or not.
	 */
	public List<String> order() {
		List<String> result = new ArrayList<>(_entries.size());
		for (Entry entry : _entries) {
			result.add(entry.name());
		}
		return result;
	}

	private void pushEntries() {
		Resources resources = Resources.getInstance();
		List<Map<String, Object>> entries = new ArrayList<>(_entries.size());
		for (Entry entry : _entries) {
			Map<String, Object> state = new LinkedHashMap<>();
			state.put(ENTRY_NAME, entry.name());
			state.put(ENTRY_LABEL, resources.getString(entry.label()));
			state.put(ENTRY_VISIBLE, Boolean.valueOf(entry.visible()));
			state.put(ENTRY_LOCKED, Boolean.valueOf(entry.locked()));
			entries.add(state);
		}
		putState(ENTRIES, entries);
	}

	/**
	 * Handles a drag and drop or an Alt + arrow move of one entry.
	 *
	 * <p>
	 * The position decides the lock: a column dropped into the block is locked, one dropped behind it released. The
	 * position directly at the line keeps what the column was, so a locked column ends the block and a released one
	 * starts the rest.
	 * </p>
	 */
	@ReactCommandHandler(CMD_COLUMN_REORDER)
	void handleReorder(ColumnReorderArguments args) {
		int from = indexOf(args.getColumn());
		if (from < 0) {
			return;
		}
		Entry entry = _entries.remove(from);
		int block = lockedCount();
		int target = Math.min(Math.max(args.getTargetIndex(), 0), _entries.size());
		boolean locked = entry.locked() ? target <= block : target < block;
		_entries.add(locked ? Math.min(target, block) : Math.max(target, block), entry.withLocked(locked));
		pushEntries();
	}

	/**
	 * Handles a click on the eye.
	 *
	 * <p>
	 * Hiding the last displayed column is refused: a table without columns shows nothing at all, and nothing in it
	 * could bring a column back.
	 * </p>
	 */
	@ReactCommandHandler(CMD_COLUMN_VISIBLE)
	void handleVisible(ColumnVisibleArguments args) {
		int index = indexOf(args.getColumn());
		if (index >= 0 && (args.isVisible() || visibleColumns().size() > 1)) {
			_entries.set(index, _entries.get(index).withVisible(args.isVisible()));
			pushEntries();
		}
	}

	/**
	 * Handles a click on the lock: locking puts the column at the end of the block, releasing it directly behind.
	 *
	 * <p>
	 * A command that does not change the lock leaves the order alone - locking a locked column or releasing a released
	 * one must not move it.
	 * </p>
	 */
	@ReactCommandHandler(CMD_COLUMN_LOCK)
	void handleLock(ColumnLockArguments args) {
		int index = indexOf(args.getColumn());
		if (index < 0) {
			return;
		}
		if (_entries.get(index).locked() == args.isLocked()) {
			return;
		}
		Entry entry = _entries.remove(index);
		_entries.add(lockedCount(), entry.withLocked(args.isLocked()));
		pushEntries();
	}

	/** The number of locked entries, the length of the block at the start. */
	private int lockedCount() {
		int result = 0;
		while (result < _entries.size() && _entries.get(result).locked()) {
			result++;
		}
		return result;
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
