/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

/**
 * Interface to make {@link AttributedSearchResultSet}s accessible.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public interface AttributedSearchResultSetAware {
	
	public AttributedSearchResultSet getSearchResult();

}
