/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.control.table.TableViewControl.SelectionListener;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.table.Row;

/**
 * Two-way binding between the selection of a {@link TableViewControl} and a {@link ViewChannel}.
 *
 * <p>
 * The channel is the shared selection: several tables, a detail panel and a command's
 * executability can all be bound to the same one. A table is therefore only one of its writers,
 * and it writes it in exactly two situations:
 * </p>
 *
 * <ul>
 * <li>The selection changed in the table itself (the user clicked a row): one selected
 * {@link Row#key() row key} is written as that object, several as the {@link Set} of keys, none as
 * <code>null</code>.</li>
 * <li>A row this table displayed as selected is gone after a refresh of its rows - deleted,
 * filtered away by a changed input, no longer matching the row criterion. The value names
 * something nobody can see any more, so the channel is cleared.</li>
 * </ul>
 *
 * <p>
 * A value the table merely does not contain means no more than "no row selected here": the table
 * shows no selection and leaves the channel alone. Clearing it would destroy what another writer
 * put there - the row a second table over a different row set selected, or the object a create
 * command wrote before this table's rows caught up with it. A value that is a {@link Collection} is
 * applied as "no selection" as well, and is left alone for the same reason.
 * </p>
 *
 * <p>
 * The table's own echo of an applied channel value (its selection listener fires from every
 * selection push, including a programmatic one) never writes the channel back.
 * </p>
 */
public class TableSelectionBinding {

	private final TableViewControl<?> _table;

	private final ViewChannel _channel;

	private final ViewChannel.ChannelListener _channelListener;

	private final SelectionListener _selectionListener = this::handleSelectionChanged;

	/** Whether a channel value is currently being applied, so the table's echo is not written back. */
	private boolean _applyingFromChannel;

	/** The {@link Row#key() row keys} the table last displayed as selected. */
	private Set<Object> _displayedKeys = Set.of();

	/**
	 * Creates a {@link TableSelectionBinding} and applies the channel's current value to the table.
	 *
	 * @param table
	 *        The table whose selection is bound.
	 * @param channel
	 *        The channel holding the selection.
	 */
	public TableSelectionBinding(TableViewControl<?> table, ViewChannel channel) {
		_table = table;
		_channel = channel;

		table.addSelectionListener(_selectionListener);
		_channelListener = (sender, oldValue, newValue) -> applyChannelValue();
		channel.addListener(_channelListener);

		applyChannelValue();
	}

	/**
	 * Re-establishes the binding after the table's rows have been refreshed.
	 *
	 * <p>
	 * To be called by the owner of the table directly after {@link TableViewControl#refreshData()}:
	 * the channel value is applied again, so a row that appears only now (the just-created object
	 * the channel already names) is selected and scrolled into view; and a row this table displayed
	 * as selected before the refresh that is no longer among the rows clears the channel.
	 * </p>
	 */
	public void rowsRefreshed() {
		// The refresh has silently dropped vanished keys from the table's selection, so what the
		// table still holds tells which of the displayed rows survived. The refresh itself does not
		// notify the selection listener, hence the last notified selection is the one displayed
		// before it.
		boolean displayedRowGone = !_displayedKeys.isEmpty()
			&& !_table.getSelectedKeys().containsAll(_displayedKeys);

		applyChannelValue();

		if (displayedRowGone && _table.getSelectedKeys().isEmpty()) {
			_channel.set(null);
		}
	}

	/**
	 * Detaches from the table and the channel.
	 *
	 * <p>
	 * To be called by the owner when the table goes away - an editable table builds a fresh
	 * {@link TableViewControl} whenever it enters or leaves its edit mode, and binds that one
	 * instead.
	 * </p>
	 */
	public void dispose() {
		_channel.removeListener(_channelListener);
		_table.removeSelectionListener(_selectionListener);
	}

	/**
	 * Writes a selection made in the table to the channel.
	 */
	private void handleSelectionChanged(Set<Object> selectedKeys) {
		_displayedKeys = new LinkedHashSet<>(selectedKeys);
		if (_applyingFromChannel) {
			return;
		}
		if (selectedKeys.size() == 1) {
			_channel.set(selectedKeys.iterator().next());
		} else if (selectedKeys.isEmpty()) {
			_channel.set(null);
		} else {
			_channel.set(selectedKeys);
		}
	}

	/**
	 * Displays the channel's current value as the table's selection, showing nothing where the
	 * table has no such row.
	 */
	private void applyChannelValue() {
		Object value = _channel.get();
		if (isDisplayed(value)) {
			// The table already shows this selection: it is the table's own, echoed back through
			// the channel. Re-selecting would reduce a multiple selection to nothing and scroll the
			// row the user just clicked.
			return;
		}
		_applyingFromChannel = true;
		try {
			_table.selectRow(value instanceof Collection ? null : value);
		} finally {
			_applyingFromChannel = false;
		}
	}

	/**
	 * Whether the table's current selection is exactly the given channel value.
	 */
	private boolean isDisplayed(Object value) {
		Set<Object> selectedKeys = _table.getSelectedKeys();
		if (value == null) {
			return selectedKeys.isEmpty();
		}
		if (value instanceof Collection<?> keys) {
			return selectedKeys.equals(new LinkedHashSet<Object>(keys));
		}
		return selectedKeys.size() == 1 && selectedKeys.contains(value);
	}

}
