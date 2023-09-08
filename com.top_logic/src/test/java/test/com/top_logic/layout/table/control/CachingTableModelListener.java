/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.control;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.table.model.TableModelListener;

/**
 * {@link TableModelListener} that caches all received events
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CachingTableModelListener implements TableModelListener {

	/** List of events received by this {@link TableModelListener} */
	public List<? super TableModelEvent> receivedEvents;

	/**
	 * Creates a new {@link CachingTableModelListener} with an empty {@link ArrayList}.
	 */
	public CachingTableModelListener() {
		this(new ArrayList<>());
	}

	/**
	 * Creates a {@link CachingTableModelListener} starting with the given
	 * {@link List}.
	 * 
	 * @param events
	 *        the list used to append events to.
	 */
	public CachingTableModelListener(List<? super TableModelEvent> events) {
		receivedEvents = events;
	}

	@Override
	public void handleTableModelEvent(TableModelEvent event) {
		receivedEvents.add(event);
	}
}
