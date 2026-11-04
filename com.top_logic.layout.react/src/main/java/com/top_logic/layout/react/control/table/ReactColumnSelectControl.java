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
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.table.ColumnOption;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * The column selection of a table as a layer panel: each column with its handle, its name, a lock that freezes it, an
 * eye that shows or hides it and a toggle grouping the rows by it.
 *
 * <p>
 * The locked columns form a block at the start of the list - the columns the table freezes. Locking puts a column at
 * the end of the block, releasing it directly behind. A drag changes the section of the column as well: the client
 * lists the columns in three sections, frozen, movable and hidden, and dropping a column into another section is one
 * {@link #CMD_COLUMN_MOVE move} that sets the lock and the visibility of the column and its position. A hidden column
 * keeps its place when it is hidden by the eye or by a drop on the section "hidden": it is the same position in this
 * list, so showing it again by the eye puts it back where it was.
 * </p>
 *
 * <p>
 * A locked column is always displayed: hiding it releases it, and locking a hidden column by its lock displays it.
 * The table freezes a leading run of displayed columns and keeps no lock for a hidden one, so a hidden locked column
 * would be a state the list shows but the table drops.
 * </p>
 *
 * <p>
 * The rows are grouped by one column at a time, which may also be a hidden one: the header menu of the table offers
 * the displayed columns alone, so the list is where the rows are grouped by a column the table does not display. A
 * tree table offers no grouping, its rows are structured by their nodes already.
 * </p>
 *
 * <p>
 * The control holds no working copy of its own to be applied later: whoever shows it learns of every gesture that
 * changed something by {@link #setOnChange(Runnable)} and applies {@link #visibleColumns()}, {@link #lockedColumns()}
 * and {@link #groupedColumn()} then, so the table follows each click. The order, the eyes, the locks and the grouping
 * all live on the server: every gesture is a command, and the resulting list is pushed back, exactly as dragging a
 * column header is handled by the {@link TableViewControl}.
 * </p>
 *
 * <p>
 * Every command counts in the {@link #REVISION revision}, pushed with the list in one patch - also a command that was
 * refused or changed nothing, whose arguments could not be read, or whose handler failed. The client counts the
 * commands it sent, and shows a predicted list for the commands it has no answer to yet: when the revision has caught
 * up, the list pushed is the one to show.
 * </p>
 */
public class ReactColumnSelectControl extends ReactControl {

	/**
	 * Command moving one column to a position and into a section - frozen, movable or hidden - in one step (drag and
	 * drop, Alt + arrow).
	 *
	 * @see ColumnMoveArguments
	 */
	public static final String CMD_COLUMN_MOVE = "columnMove";

	/** Command showing or hiding one column. */
	public static final String CMD_COLUMN_VISIBLE = "columnVisible";

	/** Command freezing or releasing one column. */
	public static final String CMD_COLUMN_LOCK = "columnLock";

	/**
	 * Command choosing the column to group the rows by, or none, with the arguments of the table's own
	 * {@link TableViewControl#CMD_GROUP}.
	 */
	public static final String CMD_GROUP = TableViewControl.CMD_GROUP;

	/** State key of whether the rows can be grouped at all. */
	public static final String GROUPABLE = "groupable";

	/**
	 * State key of the revision: the number of commands of the column selection handled so far, refused ones included.
	 */
	public static final String REVISION = "revision";

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

	/** Per-entry state key of whether the rows are grouped by the column. */
	private static final String ENTRY_GROUPED = "grouped";

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

	/** Whether the rows can be grouped at all. */
	private final boolean _groupable;

	/** The edited grouping: the column to group the rows by, {@code null} for none. */
	private String _groupedColumn;

	/** The number of commands handled, pushed as {@link #REVISION}. */
	private int _revision;

	/** Whether the command being executed has been counted in the {@link #_revision} already. */
	private boolean _answered;

	/** Told after every command that changed the columns or the grouping, {@code null} for nobody. */
	private Runnable _onChange;

	/**
	 * Creates a {@link ReactColumnSelectControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param options
	 *        The columns to offer, in initial display order (see
	 *        {@link com.top_logic.table.TableView#columnOptions()}).
	 * @param lockedColumns
	 *        The names of the columns the table freezes when the selection opens.
	 * @param groupedColumn
	 *        The column the rows are grouped by when the selection opens, {@code null} for none.
	 * @param groupable
	 *        Whether the rows can be grouped at all. A tree table cannot: its rows are structured by their nodes
	 *        already, so no column offers grouping then.
	 */
	public ReactColumnSelectControl(ReactContext context, List<ColumnOption> options, Set<String> lockedColumns,
			String groupedColumn, boolean groupable) {
		super(context, null, "TLColumnSelect");
		_groupable = groupable;
		_groupedColumn = groupedColumn;
		putState(GROUPABLE, Boolean.valueOf(groupable));
		putState(REVISION, Integer.valueOf(_revision));
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
	 * Sets who is told that a gesture changed the columns, their order, their locks or the grouping - after the
	 * change is made and {@link #visibleColumns() readable}, once per command, and not for a command that changed
	 * nothing: one that was refused, named an unknown column or asked for what already was.
	 *
	 * @param onChange
	 *        The listener, {@code null} for none.
	 */
	public void setOnChange(Runnable onChange) {
		_onChange = onChange;
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
	 * The column to group the rows by, {@code null} for none - the third part of the result.
	 *
	 * <p>
	 * Unless the rows can be grouped, this is the grouping the selection opened with.
	 * </p>
	 */
	public String groupedColumn() {
		return _groupedColumn;
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

	/**
	 * Executes the command and answers it: every command counts in the {@link #REVISION revision} exactly once.
	 *
	 * <p>
	 * A handler that made its change answers with the changed list. A command that ends without - refused or ignored
	 * by its handler, with arguments that could not be read, with a handler that failed - is answered here, with the
	 * list as it is: the client counts on an answer to every command it sent.
	 * </p>
	 */
	@Override
	public HandlerResult executeCommand(String commandName, Map<String, Object> arguments) {
		_answered = false;
		try {
			return super.executeCommand(commandName, arguments);
		} finally {
			if (!_answered) {
				afterEdit(_entries, _groupedColumn);
			}
		}
	}

	/**
	 * Pushes the list and the revision after a command and tells the listener, if the command changed something.
	 *
	 * <p>
	 * The list is pushed in any case, together with the incremented revision in one patch: a drag whose drop changes
	 * nothing must still end with the list the server holds, or the client would keep showing the moved row, and the
	 * client counts on an answer to every command it sent.
	 * </p>
	 */
	private void afterEdit(List<Entry> before, String groupedBefore) {
		// Counted before anything that might fail, so that a failing listener does not have the command counted twice.
		_answered = true;
		_revision++;
		Object token = beginUpdate();
		pushEntries();
		putState(REVISION, Integer.valueOf(_revision));
		commitUpdate(token);
		boolean changed = !before.equals(_entries) || !Objects.equals(groupedBefore, _groupedColumn);
		if (changed && _onChange != null) {
			_onChange.run();
		}
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
			state.put(ENTRY_GROUPED, Boolean.valueOf(entry.name().equals(_groupedColumn)));
			entries.add(state);
		}
		putState(ENTRIES, entries);
	}

	/**
	 * Handles a drag and drop or an Alt + arrow move of one entry, which may also change the section of the column.
	 *
	 * <p>
	 * Hiding ({@code visible} is {@code false}) is refused for the last displayed column. Otherwise the column keeps
	 * its place, only a frozen column is released and moves directly behind the block; the target index is ignored.
	 * </p>
	 *
	 * <p>
	 * Displaying sets the lock as given and puts the column at the target index of the whole list, counted without the
	 * column. The index is taken to the nearest place of the block the column then belongs to: a frozen column ends
	 * the block at the latest, a movable one starts the rest at the earliest.
	 * </p>
	 *
	 * <p>
	 * A refused move, and one of an unknown column, is answered by {@link #executeCommand(String, Map)}.
	 * </p>
	 */
	@ReactCommandHandler(CMD_COLUMN_MOVE)
	void handleMove(ColumnMoveArguments args) {
		int from = indexOf(args.getColumn());
		if (from < 0) {
			return;
		}
		List<Entry> before = List.copyOf(_entries);
		Entry entry = _entries.get(from);
		if (!args.isVisible()) {
			if (entry.visible() && visibleColumns().size() <= 1) {
				return;
			}
			if (entry.locked()) {
				_entries.remove(from);
				_entries.add(lockedCount(), entry.withLocked(false).withVisible(false));
			} else {
				_entries.set(from, entry.withVisible(false));
			}
		} else {
			_entries.remove(from);
			int block = lockedCount();
			int target = Math.min(Math.max(args.getTargetIndex(), 0), _entries.size());
			_entries.add(args.isLocked() ? Math.min(target, block) : Math.max(target, block),
				entry.withVisible(true).withLocked(args.isLocked()));
		}
		afterEdit(before, _groupedColumn);
	}

	/**
	 * Handles a click on the eye.
	 *
	 * <p>
	 * Hiding the last displayed column is refused: a table without columns shows nothing at all, and nothing in it
	 * could bring a column back. Hiding a locked column releases it, so it moves directly behind the block.
	 * </p>
	 */
	@ReactCommandHandler(CMD_COLUMN_VISIBLE)
	void handleVisible(ColumnVisibleArguments args) {
		int index = indexOf(args.getColumn());
		if (index < 0 || (!args.isVisible() && visibleColumns().size() <= 1)) {
			return;
		}
		List<Entry> before = List.copyOf(_entries);
		Entry entry = _entries.get(index);
		if (!args.isVisible() && entry.locked()) {
			_entries.remove(index);
			_entries.add(lockedCount(), entry.withLocked(false).withVisible(false));
		} else {
			_entries.set(index, entry.withVisible(args.isVisible()));
		}
		afterEdit(before, _groupedColumn);
	}

	/**
	 * Handles a click on the lock: locking puts the column at the end of the block and displays it, releasing puts it
	 * directly behind.
	 *
	 * <p>
	 * A command that does not change the lock leaves the order alone - locking a locked column or releasing a released
	 * one must not move it.
	 * </p>
	 */
	@ReactCommandHandler(CMD_COLUMN_LOCK)
	void handleLock(ColumnLockArguments args) {
		int index = indexOf(args.getColumn());
		if (index < 0 || _entries.get(index).locked() == args.isLocked()) {
			return;
		}
		List<Entry> before = List.copyOf(_entries);
		Entry entry = _entries.remove(index).withLocked(args.isLocked());
		_entries.add(lockedCount(), args.isLocked() ? entry.withVisible(true) : entry);
		afterEdit(before, _groupedColumn);
	}

	/**
	 * Handles a click on the group toggle: the rows are grouped by the given column, or by none when the column is
	 * empty.
	 *
	 * <p>
	 * The rows are grouped by one column at a time, so the client moves the grouping by naming another column and
	 * removes it by naming none. A column the list does not offer, and any grouping of a table that cannot be grouped,
	 * is ignored.
	 * </p>
	 */
	@ReactCommandHandler(CMD_GROUP)
	void handleGroup(GroupArguments args) {
		if (!_groupable) {
			return;
		}
		String column = args.getColumn();
		String before = _groupedColumn;
		if (StringServices.isEmpty(column)) {
			_groupedColumn = null;
		} else if (indexOf(column) >= 0) {
			_groupedColumn = column;
		} else {
			return;
		}
		afterEdit(_entries, before);
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
