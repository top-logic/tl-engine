/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.Set;

import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.control.table.TableViewControl.SelectionListener;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.table.Row;
import com.top_logic.table.SelectionMode;

/**
 * {@link SelectionChannelBinding} for the selection of a {@link TableViewControl}.
 *
 * <p>
 * The keys of the binding are the {@link Row#key() row keys}, and a table displays several selected
 * rows in {@link SelectionMode#MULTI} only.
 * </p>
 *
 * <p>
 * The table's selection listener fires from every selection push, including the programmatic one
 * this binding makes to display a channel value; that echo is recognized by the base class and not
 * written back.
 * </p>
 */
public class TableSelectionBinding extends SelectionChannelBinding {

	private final TableViewControl<?> _table;

	private final SelectionListener _selectionListener = this::selectionChanged;

	/**
	 * Creates a {@link TableSelectionBinding} and applies the channel's current value to the table.
	 *
	 * @param table
	 *        The table whose selection is bound.
	 * @param channel
	 *        The channel holding the selection.
	 */
	public TableSelectionBinding(TableViewControl<?> table, ViewChannel channel) {
		super(channel);
		_table = table;

		table.addSelectionListener(_selectionListener);

		attach();
	}

	/**
	 * Re-establishes the binding after the table's rows have been refreshed.
	 *
	 * <p>
	 * To be called by the owner of the table directly after {@link TableViewControl#refreshData()}:
	 * a row that appears only now (the just-created object the channel already names) is selected
	 * and scrolled into view, and a row this table displayed as selected and that is gone gives the
	 * channel up to whatever is left of the selection.
	 * </p>
	 */
	public void rowsRefreshed() {
		refreshed();
	}

	@Override
	protected Set<Object> getSelectedKeys() {
		return _table.getSelectedKeys();
	}

	@Override
	protected void displaySelection(Set<?> keys) {
		_table.selectRows(keys);
	}

	@Override
	protected boolean canDisplaySeveral() {
		return _table.getSelectionMode() == SelectionMode.MULTI;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * An editable table builds a fresh {@link TableViewControl} whenever it enters or leaves its
	 * edit mode, and binds that one instead.
	 * </p>
	 */
	@Override
	protected void detach() {
		_table.removeSelectionListener(_selectionListener);
	}

}
