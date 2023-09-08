/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching.ui;

import java.util.Collections;

import com.top_logic.knowledge.searching.SearchResultSet;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelBuilder} implementation
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class SearchResultModelBuilder implements ModelBuilder {

	/**
	 * Singleton {@link SearchResultModelBuilder} instance.
	 */
	public static final SearchResultModelBuilder INSTANCE = new SearchResultModelBuilder();

	private SearchResultModelBuilder() {
		// Singleton constructor.
	}

    /** 
     * @see com.top_logic.mig.html.ModelBuilder#getModel(Object, com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	public Object getModel(Object businessModel, LayoutComponent component) {
		if(businessModel == null || !supportsModel(businessModel, component)) {
            return Collections.EMPTY_LIST;
        } else {
           SearchResultSet searchResult = (SearchResultSet) businessModel;
               return searchResult.getSearchResults();
        }
    }

    /** 
     * @see com.top_logic.mig.html.ModelBuilder#supportsModel(java.lang.Object, com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	public boolean supportsModel(Object model, LayoutComponent component) {
        return model == null || model instanceof SearchResultSet;
    }

}
