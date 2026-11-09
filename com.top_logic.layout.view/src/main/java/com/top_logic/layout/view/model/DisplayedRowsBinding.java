/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.table.Row;
import com.top_logic.table.RowKind;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewListener;

/**
 * Publishes the rows a {@link TableView} displays on a {@link ViewChannel}: the objects of its
 * data rows, in the order the table displays them.
 *
 * <p>
 * The channel holds exactly what the user sees of the table: the rows its filters and its search
 * let pass, in its sort order, and of a tree or a grouped table the rows below an expanded node or
 * group, but none below a collapsed one. A group header stands for no object and is left out. This
 * is not the set an export writes, which includes the members of collapsed nodes and groups.
 * </p>
 *
 * <p>
 * The binding is one-way: the table never reads the channel. It writes the channel when it is
 * created and after every change of the displayed rows - a filter, a search, a sort, an expansion
 * or a refresh of the rows all reach the {@link TableView} as a
 * {@link TableViewListener#rowsChanged(int, int) change of its rows}. Each value is a list of its
 * own, compared by content, so the channel notifies its listeners only when the displayed objects
 * or their order actually changed.
 * </p>
 */
public class DisplayedRowsBinding {

	private final TableView<?> _view;

	private final ViewChannel _channel;

	private final TableViewListener _viewListener = new TableViewListener() {
		@Override
		public void rowsChanged(int from, int to) {
			publish();
		}
	};

	/**
	 * Creates a {@link DisplayedRowsBinding} and publishes the rows the table displays now.
	 *
	 * @param view
	 *        The table whose displayed rows are published.
	 * @param channel
	 *        The channel receiving the objects of the displayed rows.
	 */
	public DisplayedRowsBinding(TableView<?> view, ViewChannel channel) {
		_view = view;
		_channel = channel;

		view.addListener(_viewListener);
		publish();
	}

	/**
	 * Detaches from the table, leaving the channel with the rows it displayed last.
	 *
	 * <p>
	 * To be called by the owner when the table goes away - an editable table builds a fresh
	 * {@link TableView} whenever it enters or leaves its edit mode, and binds that one instead.
	 * </p>
	 */
	public void dispose() {
		_view.removeListener(_viewListener);
	}

	/**
	 * Writes the objects of the displayed data rows to the channel.
	 */
	private void publish() {
		_channel.set(displayedObjects(_view));
	}

	/**
	 * The objects of the data rows the given table displays, in display order.
	 */
	private static List<Object> displayedObjects(TableView<?> view) {
		List<? extends Row<?>> rows = view.rows(0, view.rowCount());
		List<Object> result = new ArrayList<>(rows.size());
		for (Row<?> row : rows) {
			if (row.kind() == RowKind.DATA && row.data() != null) {
				result.add(row.data());
			}
		}
		return result;
	}

}
