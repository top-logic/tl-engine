/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.io.Serializable;

/**
 * Manages a bundle of multiple kinds of selections.
 * 
 * Objects may be contained in multiple generalized selections at the same time.
 * One selection might be the set of objects highlighted in a user interface,
 * another independent set may be the set of objects currently in the clipboard.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SelectionSetModel extends Serializable {
	/**
	 * The maximum index of a selection within selection sets implementing this 
	 * interface. The number of selections are limited by the number of bits in
	 * values of type <code>int</code>.
	 */
	public static final int MAX_SELECTION_MODEL_INDEX = 30;

	/**
	 * Compute a bit mask that indicates in which selections the given object 
	 * is currently a member of.
	 * 
	 * @see #setSelection(Object, int, boolean)
	 * 
	 * @param obj the object in question
	 * @return a bit mask that has a bit set for each selection, the given object
	 *    is a member of.   
	 */
	public int getSelections(Object obj);
	
    /**
     * Return true when object is selected for given selection index.
     * 
     * @see #setSelection(Object, int, boolean)
     * @param selectionIndex the index of the selection in this selection set.
     * 
     * @param obj the object in question
     */
    // TODO BHU implement this
    // public boolean isSelected(int selectionIndex, Object obj);

    /**
	 * Adds/removes the given object to/from the selection indicated with the 
	 * index parameter.
	 * 
	 * @param obj the object in question
	 * @param selectionIndex the index of the selection in this selection set.
	 * @param select whether the object should be selected or deselected.
	 */
	public void setSelection(Object obj, int selectionIndex, boolean select);
	
	/**
	 * Return a selection model representing the selection at the given index.
	 *  
	 * @param selectionIndex the index of the selection
	 * @return a selection model representing the selection at the given 
	 *    index. 
	 */
	public SelectionModel getSelectionModel(int selectionIndex);
	
	/**
	 * Remove the given object from all selections managed by this selection 
	 * set.
	 *  
	 * @param obj the object that should be removed from all selections.
	 */
	public void removeAllSelections(Object obj);
	
	/**
	 * Removes all objects from the selection identified by the given index.
	 */
	public void clear(int selectionIndex);
	
	/**
	 * Remove all objects from all selections mangaged by this selection set.
	 */
	public void clear();
}
