/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.layout.component.model.SingleSelectionListener;
import com.top_logic.mig.html.SelectionModel;

/**
 * A {@link SingleSelectionModel} is a variant of {@link SelectionModel} which
 * can select exactly one object.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface SingleSelectionModel {

	/**
	 * Determines whether the given object can be selected by this
	 * {@link SingleSelectionModel}
	 * 
	 * @param obj
	 *        the object under test
	 * @return whether the given object can be selected
	 */
	boolean isSelectable(Object obj);

	/**
	 * This method sets the new single selection. If <code>touchedObject</code>
	 * is <code>null</code> the current selection will be deleted.
	 * 
	 * @param obj
	 *        the new selected object. may be <code>null</code>.
	 */
	void setSingleSelection(Object obj);

	/**
	 * Since only one object is selected, return the selected object.
	 * 
	 * @return the selected object.
	 */
	Object getSingleSelection();

	/**
	 * Adds the given listener to be informed about changes of the selection
	 * 
	 * @param listener
	 *        the listener to inform. must not be <code>null</code>
	 * @return whether attaching was successful. If <code>true</code> then the
	 *         listener will be informed about changes of
	 *         {@link #getSingleSelection()}
	 */
	boolean addSingleSelectionListener(SingleSelectionListener listener);

	/**
	 * Removes the given listener
	 * 
	 * @param listener
	 *        the listener to detach.
	 * @return whether detaching was successful.
	 */
	boolean removeSingleSelectionListener(SingleSelectionListener listener);

}
