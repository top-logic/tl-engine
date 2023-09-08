/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.filter.model;


/**
 * A {@link FilterModel} informs all registered {@link FilterModelListener} about 
 * changes. New {@link FilterModelListener} can be added by the method 
 * {@link #addFilterModelListener(FilterModelListener)}. Old {@link FilterModelListener}
 * can be removed by the method {@link #removeFilterModelListener(FilterModelListener)}. 
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public interface FilterModel {

	/**
	 * This method returns <code>true</code> if the listener was registered
	 * (duplicates are suppressed), <code>false</code> otherwise.
	 * 
	 * @param listener
	 *            A {@link FilterModelListener} must NOT be <code>null</code>.
	 * @return Returns <code>true</code> if the listener was registered.
	 */
	public boolean addFilterModelListener(FilterModelListener listener);
	
    /**
	 * This method returns <code>true</code> if the listener was removed or
	 * wasn't registered, <code>false</code> otherwise.
	 * 
	 * @param listener
	 *            A {@link FilterModelListener} must NOT be <code>null</code>.
	 */
	public boolean removeFilterModelListener(FilterModelListener listener);
	
}
