/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.lang.ref.WeakReference;

import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.table.model.TableModelListener;

/**
 * {@link WeakReference} to connect a {@link TableModelListener} to a {@link TableModel application
 * model} without preventing the listener from being garbage collected.
 * 
 * <p>
 * This class can be used to ensure that the {@link TableViewModel} can die if it is no longer
 * needed (e.g. the {@link TableViewModel} is built anonymously in some {@link TableControl}) and
 * the {@link TableControl} has been discarded).
 * </p>
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public final class WeakTableModelListener extends WeakReference<TableModelListener> implements TableModelListener {

	/**
	 * Creates a {@link WeakTableModelListener}.
	 * 
	 * @param delegate
	 *        The {@link TableModelListener} that should be weakly referenced.
	 */
	public WeakTableModelListener(TableModelListener delegate) {
		super(delegate);
	}

	/**
	 * Tries to forward the given event to the {@link #get() referent} of this
	 * {@link WeakReference}. If the referent is <code>null</code> it removes this listener from
	 * the source of the event.
	 *
	 * @see com.top_logic.layout.table.model.TableModelListener#handleTableModelEvent(com.top_logic.layout.table.model.TableModelEvent)
	 */
	@Override
	public void handleTableModelEvent(TableModelEvent event) {
		TableModelListener delegate = get();
		if (delegate == null) {
			((TableModel) event.getSource()).removeTableModelListener(this);
		} else {
			delegate.handleTableModelEvent(event);
		}
	}

}