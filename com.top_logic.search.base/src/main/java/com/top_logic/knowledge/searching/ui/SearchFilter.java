/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.dob.filt.DOAttributeFilter;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.searching.Filter;
import com.top_logic.knowledge.searching.SearchResult;
import com.top_logic.knowledge.searching.SearchResultSet;

/**
 * Filter class for filtering the search results of the Search GUI.
 * It filters the search result's knowledge objects that match certain
 * knowledge object attribute values, e.g. "Name contains ..., Name is ...".
 *
 * @author    <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public class SearchFilter implements Filter {

    /** 
     * The separator character used to separate the name and the filter
     * of the KO attribute filter to be used for filtering search results
     */  
    public static final String NAMEFILTER_SEPARATOR = ":";

    /** The result ko attribute filter */
    private ResultKOAttributeFilter filter;

    public SearchFilter(Map searchGUIFilters) {
        super();
        filter = new ResultKOAttributeFilter(searchGUIFilters);
    }

	/**
	 * @see com.top_logic.knowledge.searching.Filter#filter(com.top_logic.knowledge.searching.SearchResultSet)
	 */
	@Override
	public SearchResultSet filter(SearchResultSet aResultSet) {
        if (this.filter.size() > 0) {
            // now filter the results
            Iterator results = aResultSet.getSearchResults().iterator();
            while (results.hasNext()) {
                SearchResult result = (SearchResult) results.next();
                if (!filter.include(result)) {
                    results.remove();
                }
            }
        }
        return aResultSet; 
	}
    
}

class ResultKOAttributeFilter {

    /** The KO attribute filter "is" */
    public static final String IS_FILTER            = "is";
    
    /** The KO attribute filter "contains" */
    public static final String CONTAINS_FILTER      = "contains";

    /** The DOAttributeFilters used to filter the search results */
    private List doAttributeFilters;

    public ResultKOAttributeFilter(Map searchGUIFilters) {
        this.createDOAttributeFilters(searchGUIFilters);
    }

    public int size() {
        return this.doAttributeFilters.size();
    }
    
    public boolean include(SearchResult aResult) {
        KnowledgeObject theObject = aResult.getObject();
        Iterator theFilters = doAttributeFilters.iterator();
        while (theFilters.hasNext()) {
            DOAttributeFilter theFilter = (DOAttributeFilter) theFilters.next();
            if (!theFilter.test(theObject)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create data object attribute filters for all entries in 
     * the Search GUI filters map.
     * 
     * @param   searchGUIFilters   The Seach GUI filter map
     */ 
    private void createDOAttributeFilters(Map searchGUIFilters) {
        if (this.doAttributeFilters == null) {
            this.doAttributeFilters = new ArrayList();
        }
        Iterator guiFilters = searchGUIFilters.entrySet().iterator();
        while (guiFilters.hasNext()) {
            Map.Entry entry = (Map.Entry) guiFilters.next(); 
            String key = (String) entry.getKey();
            int index = key.indexOf(SearchFilter.NAMEFILTER_SEPARATOR);

            // get name, filter and value of ko attribute to be filtered
            String fullName = key.substring(0, index);
            String name = fullName.substring(fullName.lastIndexOf(".")+1, fullName.length());
            String fullFilter = key.substring(index+1, key.length());
            String filter = fullFilter.substring(fullFilter.lastIndexOf(".")+1, fullFilter.length());
            String value = (String) entry.getValue();
            
            // create the DOAttributeFilter
            DOAttributeFilter theFilter = null;
            if (filter.equals(IS_FILTER)) {
                theFilter = this.createEqualsFilter(name, value);
            }
            else if (filter.equals(CONTAINS_FILTER)) {
                theFilter = this.createContainsFilter(name, value); 
            }
            if (theFilter != null) {
                this.doAttributeFilters.add(theFilter);
            }            
        }
    }
    
    /** Create a Filter for an Equals Attribute condition */
    private DOAttributeFilter createEqualsFilter(String attrName, final String testString) {
        return new DOAttributeFilter(attrName) {
            @Override
			public boolean test(Object theValue)  {
                return testString.equals(theValue.toString());
            }
        };
    }
    
    /** Create a Filter for Contains Attribute condition */
    private DOAttributeFilter createContainsFilter(String attrName, final String testString) {
        return new DOAttributeFilter(attrName) {
            @Override
			public boolean test(Object theValue)  {
                if (theValue != null)
                    return theValue.toString().indexOf(testString) >= 0;
                else
                    return false;    
            }
        };
    }
}
