/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.layout.VetoException;
import com.top_logic.layout.VetoListener;
import com.top_logic.layout.form.model.ValueVetoListener;

/**
 * Listener to set to some {@link TableModel} to provide it from setting new columns.
 * 
 * @see TableModel#setColumns(java.util.List)
 * @see ValueVetoListener
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ColumnChangeVetoListener extends VetoListener {

	/**
	 * This method determine whether this {@link ColumnChangeVetoListener} has a veto for changing
	 * {@link TableModel#getColumnNames() columns} of some {@link TableModel}.
	 * 
	 * @param evt
	 *        event object to resolve informations about the change
	 * 
	 * @throws VetoException
	 *         if this listener wants to stop setting the new value.
	 * 
	 * @see ColumnChangeEvt
	 */
	void checkVeto(ColumnChangeEvt evt) throws VetoException;

}
