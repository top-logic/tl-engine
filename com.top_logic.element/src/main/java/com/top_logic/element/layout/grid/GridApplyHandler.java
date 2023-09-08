/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.layout.form.FormContainer;

/**
 * {@link GridApplyHandler}s are used to store the changes made in a row. They can also decide whether changing of a row
 * is executable.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public interface GridApplyHandler {

	/**
	 * Stores the changes made in the given row.
	 *
	 * @param component
	 *        the GridComponent used to make the changes
	 * @param rowObject
	 *        the object which was edited
	 * @param container
	 *        the FormContainer holding the FormFields of the edited row
	 * @return <code>true</code>, if something was changed in the persistence, <code>false</code> otherwise
	 */
	public boolean storeChanges(GridComponent component, Object rowObject, FormContainer container);

	/**
	 * Checks whether the editing of the given row object is allowed (executable)
	 *
	 * @param component
	 *        the GridComponent used to edit the row object
	 * @param rowObject
	 *        the row object which shall be edited
	 * @return <code>true</code>, if editing of teh given row object is allowed, <code>false</code> otherwise
	 */
	public boolean allowEdit(GridComponent component, Object rowObject);

}
