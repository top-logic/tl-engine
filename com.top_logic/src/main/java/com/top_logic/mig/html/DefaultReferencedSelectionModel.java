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
public class DefaultReferencedSelectionModel extends DefaultMultiSelectionModel implements ReferencedSelectionModel {
	
	private final ListModel referencedSelection;
	
	/**
	 * Create a new DefaultReferencedSelectionModel
	 * 
	 * @param selectionFilter
	 *        See
	 *        {@link DefaultSingleSelectionModel#DefaultSingleSelectionModel(Filter, SelectionModelOwner)}.
	 * @param referencedSelection
	 *        The external selection list.
	 */
	public DefaultReferencedSelectionModel(Filter selectionFilter, ListModel referencedSelection,
			SelectionModelOwner owner) {
		super(selectionFilter, owner);
		this.referencedSelection = referencedSelection;
	}

	@Override
	public boolean isSelectable(Object obj) {
		return !isReferencedSelection(obj) && super.isSelectable(obj);
	}

	/** 
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propagateReferencedSelectionChange(Set changedSelection) {
		fireSelectionChanged(changedSelection);
	}
}
