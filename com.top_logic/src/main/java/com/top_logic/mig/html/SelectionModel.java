/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;

/**
 * Represents a selection in a layout component.
 * 
 * @see com.top_logic.mig.html.SelectionSetModel combines multiple selection 
 *    models.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SelectionModel extends NamedModel, Serializable {

	/**
	 * true, if this {@link SelectionModel} can handle multiple selected elements, false
	 *         otherwise.
	 */
	default boolean isMultiSelectionSupported() {
		return true;
	}

	/**
	 * Whether the given object can be selected.
	 */
	boolean isSelectable(Object obj);
	
	/**
	 * Checks, whether the given object is contained in this selection.
	 */
	public boolean isSelected(Object obj);
	
	/**
	 * Adds or removes the given object from this selection depending on 
	 * the select parameter. 
	 * 
	 * @param obj The object that should be selected or deselected.
	 * @param select If <code>true</code>, the object is selected. Otherwise, 
	 *        the Object is deselected. 
	 */
	public void setSelected(Object obj, boolean select);
	
	/**
	 * Adds all of the given objects to the selection of this {@link SelectionModel}.
	 * 
	 * @param objects
	 *        The objects that should be selected.
	 */
	default void addToSelection(Collection<?> objects) {
		for (Object obj : objects) {
			setSelected(obj, true);
		}
	}

	/**
	 * Removes all of the given objects from the selection of this {@link SelectionModel}.
	 * 
	 * @param objects
	 *        The objects that should be de-selected.
	 */
	default void removeFromSelection(Collection<?> objects) {
		for (Object obj : objects) {
			setSelected(obj, false);
		}
	}

	/**
	 * Returns the set of selected objects. The result cannot be modified.
	 * 
	 * @return the set of selected objects.
	 */
	public Set<?> getSelection();
	
	/**
	 * Selects the given objects (if possible) and deselects all others.
	 * 
	 * @param newSelection
	 *        The new selection. Must not be <code>null</code>.
	 */
	void setSelection(Set<?> newSelection);

	/**
	 * Clears this selection.
	 */
	public void clear();

	/**
	 * Adds the given listener to this model.
	 * 
	 * @return <code>true</code> if the listener was not formerly added
	 */
	public boolean addSelectionListener(SelectionListener listener);
	
	/**
	 * Removes the given listener from this model.
	 * 
	 * @return <code>true</code> if the listener was formerly added
	 */
	public boolean removeSelectionListener(SelectionListener listener);

	/** @see SelectionModelOwner */
	public SelectionModelOwner getOwner();

}
