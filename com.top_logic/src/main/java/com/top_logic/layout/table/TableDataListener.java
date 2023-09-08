/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.mig.html.SelectionModel;

/**
 * {@link TableDataListener} are listener to inform about changes of {@link TableData}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TableDataListener extends PropertyListener {

	/**
	 * Informs about a change of the {@link TableViewModel} of the {@link TableData}
	 * 
	 * @param source
	 *            the {@link TableData} whose {@link TableViewModel} has changed.
	 * @param oldValue
	 *            the value before the the {@link TableViewModel} had changed.
	 * @param newValue
	 *            the value after the change.
	 */
	public void notifyTableViewModelChanged(TableData source, TableViewModel oldValue, TableViewModel newValue);

	/**
	 * Informs about a change of the {@link SelectionModel} of the {@link TableData}
	 * 
	 * @param source
	 *        the {@link TableData} whose {@link SelectionModel} has changed.
	 * @param oldValue
	 *        the value before the the {@link SelectionModel} had changed.
	 * @param newValue
	 *        the value after the change.
	 */
	public void notifySelectionModelChanged(TableData source, SelectionModel oldValue, SelectionModel newValue);

}
