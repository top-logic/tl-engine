/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control;

import com.top_logic.layout.VetoException;
import com.top_logic.layout.VetoListener;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.control.TableControl.SelectionType;
import com.top_logic.mig.html.SelectionModel;

/**
 * {@link SelectionVetoListener} can be added to {@link TableData}s to give a veto if the selection
 * of the table is changed by the user.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface SelectionVetoListener extends VetoListener {

	/**
	 * Determines whether this {@link SelectionVetoListener} has a veto for the given
	 * {@link SelectionModel} to select the <code>newSelectedRow</code>.
	 * 
	 * @param selectionModel
	 *        The {@link SelectionModel} in which the new row should be selected
	 * @param newSelectedRow
	 *        The row object to select.
	 * @param selectionType
	 *        The kind of selection that was triggered.
	 * 
	 * @throws VetoException
	 *         if this listener does not allow selecting the new row.
	 */
	void checkVeto(SelectionModel selectionModel, Object newSelectedRow, SelectionType selectionType)
			throws VetoException;

}
