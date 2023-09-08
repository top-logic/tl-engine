/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.top_logic.layout.component.model.AbstractSelectionModel;

/**
 * An implementation of {@link com.top_logic.mig.html.SelectionSetModel} that
 * stores multiple selection in a single map.
 * 
 * This implementation should be choosen, if all selections are may contain
 * multiple objects and the overall number of selected objects is small.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CompressedSelectionSetModel implements SelectionSetModel {
	/**
	 * A Map<Object, Integer> that stores for each object a bit set, which encodes 
	 * all selections that the object is a member of.
	 */
	Map selections = new HashMap();
	
	@Override
	public void setSelection(Object obj, int selectionIndex, boolean select) {
		if ((selectionIndex > 31) || (selectionIndex < 0)) 
			throw new IllegalArgumentException("Selection out of range: " + selectionIndex);
			
		Integer selectionSet = (Integer) selections.get(obj);
		if ((selectionSet == null) && (! select)) return;
		
		int selectionSetValue = (selectionSet == null) ? 0 : selectionSet.intValue();
		int mask = 1 << selectionIndex;
		
		if (select) {
			selectionSetValue |= mask;
		} else {
			selectionSetValue &= ~mask;
		}
		
		if (selectionSetValue == 0) {
			selections.remove(obj);
		} else {
			selections.put(obj, Integer.valueOf(selectionSetValue));
		}
	}

	@Override
	public int getSelections(Object obj) {
		Integer selectionSet = (Integer) selections.get(obj);
		
		return (selectionSet == null) ? 0 : selectionSet.intValue();
	}
	
	@Override
	public SelectionModel getSelectionModel(final int selectionIndex) {
		final int mask = 1 << selectionIndex;

		return new AbstractSelectionModel(SelectionModelOwner.NO_OWNER) {
			
			@Override
			public boolean isSelectable(Object obj) {
				return true;
			}

			@Override
			public boolean isSelected(Object obj) {
				return (CompressedSelectionSetModel.this.getSelections(obj) & mask) != 0;
			}

			@Override
			public void setSelected(Object obj, boolean select) {
				CompressedSelectionSetModel.this.setSelection(obj, selectionIndex, select);
			}

			@Override
			public void clear() {
				CompressedSelectionSetModel.this.clear(selectionIndex);
			}

			@Override
			public void setSelection(Set<?> newSelection) {
				clear();
				for (Object newSelected : newSelection) {
					setSelected(newSelected, true);
				}
			}

			@Override
			public Set<?> getSelection() {
				HashSet result = new HashSet();
				for (Iterator it = selections.keySet().iterator(); it.hasNext(); ) {
					Object obj = it.next();
					if (isSelected(obj)) result.add(obj);
				}
				return result;
			}
		};
	}

	@Override
	public void removeAllSelections(Object obj) {
		selections.remove(obj);
	}

	@Override
	public void clear(int selectionIndex) {
		for (Iterator it = selections.keySet().iterator(); it.hasNext(); ) {
			setSelection(it.next(), selectionIndex, false);
		}
	}

	@Override
	public void clear() {
		selections.clear();
	}
}
