/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.knowledge.analyze.SearchResult;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Convert a search result set into a list of valued.
 * 
 * Therefore this class will take all objects from {@link SearchResultSet#getSearchResults()}
 * and take the {@link SearchResult#getResult() values} as list elements.
 * 
 * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
 */
public class SearchResultBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link SearchResultBuilder} instance.
	 */
	public static final SearchResultBuilder INSTANCE = new SearchResultBuilder();

	protected SearchResultBuilder() {
		// Singleton constructor.
	}

    @Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		SearchResultSet theModel = (SearchResultSet) businessModel;

        if (theModel == null) {
            return Collections.EMPTY_LIST;
        }
        else {
			List theList = new ArrayList(theModel.getSearchResults().size());

            for (Iterator theIt = theModel.getSearchResults().iterator(); theIt.hasNext();) {
                SearchResult theSearchResult = (SearchResult) theIt.next();
                // remove invalid wrappers from the result
                Object theRowObject = theSearchResult.getResult();
                if ( (! (theRowObject instanceof Wrapper)) || ((Wrapper) theRowObject).tValid()) {
                    theList.add(theRowObject);
                }
            }

            return theList;
        }
    }

    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        return aModel instanceof SearchResultSet;
    }

	@Override
	public Object retrieveModelFromListElement(LayoutComponent component, Object anObject) {
	    return anObject;
    }

	@Override
	public boolean supportsListElement(LayoutComponent component, Object anObject) {
	    return anObject instanceof SearchResultSet;
    }

}
