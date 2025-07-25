/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.io.Serializable;
import java.util.ArrayList;
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
public interface SelectionModel<T> extends NamedModel, Serializable {

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
	boolean isSelectable(T obj);
	
	/**
	 * Checks, whether the given object is contained in this selection.
	 */
	public boolean isSelected(T obj);
	
	/**
	 * Adds or removes the given object from this selection depending on 
	 * the select parameter. 
	 * 
	 * @param obj The object that should be selected or deselected.
	 * @param select If <code>true</code>, the object is selected. Otherwise, 
	 *        the Object is deselected. 
	 */
	public void setSelected(T obj, boolean select);
	
	/**
	 * Adds all of the given objects to the selection of this {@link SelectionModel}.
	 * 
	 * @param objects
	 *        The objects that should be selected.
	 */
	default void addToSelection(Collection<? extends T> objects) {
		Object update = startBulkUpdate();
		try {
			for (T obj : objects) {
				setSelected(obj, true);
			}
		} finally {
			completeBulkUpdate(update);
		}
	}

	/**
	 * Removes all of the given objects from the selection of this {@link SelectionModel}.
	 * 
	 * @param objects
	 *        The objects that should be de-selected.
	 */
	default void removeFromSelection(Collection<? extends T> objects) {
		Object update = startBulkUpdate();
		try {
			for (T obj : objects) {
				setSelected(obj, false);
			}
		} finally {
			completeBulkUpdate(update);
		}
	}

	/**
	 * Returns the set of selected objects. The result cannot be modified.
	 * 
	 * @return the set of selected objects.
	 */
	public Set<? extends T> getSelection();
	
	/**
	 * Selects the given objects (if possible) and deselects all others.
	 * 
	 * @param newSelection
	 *        The new selection. Must not be <code>null</code>.
	 */
	default void setSelection(Set<? extends T> newSelection) {
		Object update = startBulkUpdate();
		try {
			clear();
			addToSelection(newSelection);
		} finally {
			completeBulkUpdate(update);
		}
	}

	/**
	 * Clears this selection.
	 */
	default void clear() {
		removeFromSelection(new ArrayList<>(getSelection()));
	}

	/**
	 * Adds the given listener to this model.
	 * 
	 * @return <code>true</code> if the listener was not formerly added
	 */
	public boolean addSelectionListener(SelectionListener<T> listener);
	
	/**
	 * Removes the given listener from this model.
	 * 
	 * @return <code>true</code> if the listener was formerly added
	 */
	public boolean removeSelectionListener(SelectionListener<T> listener);

	/** @see SelectionModelOwner */
	public SelectionModelOwner getOwner();

	/**
	 * Recommend the model to prevent firing multiple events for a bulk update that consists of
	 * multiple selections.
	 * 
	 * <p>
	 * Use with the following pattern:
	 * </p>
	 * 
	 * <pre>
	 * <code>selectionModel.startBulkUpdate();
	 * try {
	 *     selectionModel.setSelected(foo, true);
	 *     selectionModel.setSelected(bar, false);
	 *     ...
	 * } finally {
	 *     selectionModel.completeBulkUpdate();
	 * }</code>
	 * </pre>
	 * 
	 * <p>
	 * This is an optional API. Depending on its implementation, the selection model may decide to
	 * send events for each operation anyway.
	 * </p>
	 * 
	 * @return An identifier for the update that must be passed to the corresponding call to
	 *         {@link #completeBulkUpdate(Object)}
	 */
	default Object startBulkUpdate() {
		// Optional operation.
		return null;
	}

	/**
	 * Completes a bulk-update and fires an event.
	 * 
	 * @param update
	 *        The value from the corresponding {@link #startBulkUpdate()} call.
	 * 
	 * @see #startBulkUpdate()
	 */
	default void completeBulkUpdate(Object update) {
		// Optional operation.
	}

}
