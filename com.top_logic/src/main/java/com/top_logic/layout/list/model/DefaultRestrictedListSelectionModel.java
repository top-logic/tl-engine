/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list.model;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListModel;

import com.top_logic.basic.col.Filter;

/**
 * {@link RestrictedListSelectionModel} that uses a reference to a
 * {@link ListModel} in combination with a {@link Filter} implementation to
 * decide its selectable indexes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultRestrictedListSelectionModel extends DefaultListSelectionModel implements RestrictedListSelectionModel {

	/**
	 * @see #getListModel()
	 */
	private final ListModel listModel;

	/**
	 * @see #getSelectableFilter()
	 */
	private final Filter<Object> selectableFilter;

	/**
	 * Creates a new restricted list selection model.
	 * 
	 * @param listModel
	 *        see {@link #getListModel()}
	 * @param selectableFilter
	 *        see {@link #getSelectableFilter()}
	 */
	public DefaultRestrictedListSelectionModel(ListModel listModel, Filter<Object> selectableFilter) {
		this.listModel = listModel;
		this.selectableFilter = selectableFilter;
	}

	@Override
	public boolean isSelectableIndex(int index) {
		return selectableFilter.accept(listModel.getElementAt(index));
	}

	/**
	 * Corresponding {@link ListModel} for accessing the underlying list items.
	 * 
	 * <p>
	 * To decide, whether a given index is selectable, the underlying list item
	 * is required to pass it to the {@link #selectableFilter}.
	 * </p>
	 */
	public ListModel getListModel() {
		return listModel;
	}
	
	/**
	 * {@link Filter} that marks all selectable elements by returning
	 * <code>true</code> from its {@link Filter#accept(Object)} method.
	 */
	public Filter<Object> getSelectableFilter() {
		return selectableFilter;
	}
	
	@Override
	public void addSelectionInterval(int index0, int index1) {
		internalAddSelectionInterval(index0, index1, false);
	}

	private void internalAddSelectionInterval(int index0, int index1, boolean overridePreviousSelection) {
		// The range may have reversed boundaries. Those must not be reordered,
		// because this would result in the wrong lead selection index (index0
		// instead of index1).
		//
		// Compute the direction in which the selection is performed.
		int step = index0 <= index1 ? 1 : -1;
		
		// Select all but non-selectable indices in the given range.
		int first = index0;
		boolean mustCallSet = overridePreviousSelection;
		for (int n = index0; (index1 - n) * step >= 0; n += step) {
			if (! isSelectableIndex(n)) {
				int last = n - step;
				if ((last - first) * step >= 0) {
					mustCallSet = setOrAddInterval(first, last, mustCallSet);
				}
				first = n + step;
			}
		}
		
		if ((index1 - first) * step >= 0) {
			mustCallSet = setOrAddInterval(first, index1, mustCallSet);
		}
	}

	private boolean setOrAddInterval(int index0, int index1, boolean callSet) {
		if (callSet) {
			super.setSelectionInterval(index0, index1);
		} else {
			super.addSelectionInterval(index0, index1);
		}
		return false;
	}

	@Override
	public void setSelectionInterval(int index0, int index1) {
		internalAddSelectionInterval(index0, index1, true);
	}

}
