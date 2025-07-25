/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.Set;

import javax.swing.ListModel;

import com.top_logic.basic.col.Filter;

/**
 * A default implementation for {@link ReferencedSelectionModel}
 * 
 * @author     <a href="mailto:STS@top-logic.com">STS</a>
 */
public class DefaultReferencedSelectionModel<T> extends DefaultMultiSelectionModel<T>
		implements ReferencedSelectionModel<T> {
	
	private final ListModel<T> referencedSelection;
	
	/**
	 * Create a new DefaultReferencedSelectionModel
	 * 
	 * @param selectionFilter
	 *        See
	 *        {@link DefaultSingleSelectionModel#DefaultSingleSelectionModel(Filter, SelectionModelOwner)}.
	 * @param referencedSelection
	 *        The external selection list.
	 */
	public DefaultReferencedSelectionModel(Filter<? super T> selectionFilter, ListModel<T> referencedSelection,
			SelectionModelOwner owner) {
		super(selectionFilter, owner);
		this.referencedSelection = referencedSelection;
	}

	@Override
	public boolean isSelectable(T obj) {
		return !isReferencedSelection(obj) && super.isSelectable(obj);
	}

	@Override
	public boolean isReferencedSelection(Object obj) {
		
		boolean isPresent = false;
		
		if(referencedSelection != null) {
			
			// Iterate over all elements of the list model and check, if the current object is stored within the model
			for(int i = 0, size = referencedSelection.getSize(); (i < size) && !isPresent; i++) {
				if(referencedSelection.getElementAt(i).equals(obj))
					isPresent = true;
			}
			
		}
		
		return isPresent;
	}

	@Override
	public void propagateReferencedSelectionChange(Set<? extends T> changedSelection) {
		fireSelectionChanged(changedSelection);
	}
}
