/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import com.top_logic.basic.col.Filter;

/**
 * {@link Filter} can implement this interface to indicate that the se
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public interface ReplaceableSearchObjectFilter extends Filter<Object> {
	
	/**
	 * This method sets the new search object.
	 * 
	 * @param newSearchObject
	 *            The new search object to set. <code>Null</code> is
	 *            permitted.
	 */
	public void setSearchObject(Object newSearchObject);
	
	/**
	 * This method returns the current search object maybe <code>null</code>.
	 */
	public Object getSearchObject();
	
}
