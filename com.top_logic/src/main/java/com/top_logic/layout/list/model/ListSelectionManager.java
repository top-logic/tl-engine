/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list.model;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * Keeps a {@link ListSelectionModel} in sync with a changing {@link ListModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ListSelectionManager implements ListDataListener {

	private ListSelectionModel selectionModel;
	
	public ListSelectionManager(ListSelectionModel selectionModel) {
		this.selectionModel = selectionModel;
	}

	@Override
	public void contentsChanged(ListDataEvent event) {
		// Clear the selection in the changed interval.
		int firstIndex = event.getIndex0();
		int lastIndex  = event.getIndex1();

		// Prevent the selection model from firing to much events.
		if (hasOverlap(firstIndex, lastIndex, selectionModel.getMinSelectionIndex(), selectionModel.getMaxSelectionIndex())) {
			selectionModel.removeSelectionInterval(firstIndex, lastIndex);
		}
	}

	/**
	 * Whether the range [r1Start, r1End] has overlap with the range [r2Start, r2End]. 
	 */
	private boolean hasOverlap(int r1Start, int r1End, int r2Start, int r2End) {
		return (r1Start >= r2Start && r1Start <= r2End) || (r1End >= r2Start && r1End <= r2End);
	}

	@Override
	public void intervalAdded(ListDataEvent event) {
		int firstIndex = event.getIndex0();
		int lastIndex  = event.getIndex1();
		ListModel listModel = (ListModel) event.getSource();
		boolean before = lastIndex != listModel.getSize() - 1;
		selectionModel.insertIndexInterval(firstIndex, lastIndex - firstIndex + 1, before);
	}

	@Override
	public void intervalRemoved(ListDataEvent event) {
		int firstIndex = event.getIndex0();
		int lastIndex = event.getIndex1();
		
		selectionModel.removeIndexInterval(firstIndex, lastIndex);
	}

}
