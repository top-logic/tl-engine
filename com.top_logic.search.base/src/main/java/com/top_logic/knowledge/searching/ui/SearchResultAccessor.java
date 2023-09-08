/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching.ui;

import java.util.Date;
import java.util.List;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.searching.SearchResult;
import com.top_logic.knowledge.searching.SearchResultAttribute;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * {@link Accessor} implementation. This method gets values from {@link SearchResult}
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class SearchResultAccessor extends ReadOnlyAccessor<Object> {

    public static final String RANKING_KEY = "ranking";
    
    public static final String MODIFIED_KEY = "modified";
    
    /**
     * Create a new SearchResultAccessor.
     */
    public SearchResultAccessor() {
    super();
    }
    
    
    /** 
     * @see com.top_logic.layout.Accessor#getValue(java.lang.Object, java.lang.String)
     */
    @Override
	public Object getValue(Object object, String property) {
        if (object == null) {
            return null;
        }
        if (object instanceof SearchResult) {
            SearchResult theResult = (SearchResult) object;
            List theAttributesList = (theResult).getAttributes();
            if (property.equals("rating")) {
                return HTMLFormatter.getInstance().formatPercent(((Number)iterOverSearchAttributes(theAttributesList, RANKING_KEY)).doubleValue());
                
            }else if(property.equals("modified")) {
                Long millis = (Long)getValueFromWrapper(property, theResult);
                if(millis == null) {
                    return null;
                }
                return new Date(millis.longValue());
                
            }else if(property.equals("location")){
                return theResult.getObject().getKnowledgeBase().getName();
                
//                use this code to get information like "repository://file.txt"
//                return getValueFromWrapper("physicalResource", theResult);
            }else {
                return getValueFromWrapper(property, theResult);
                
            }
        }
        return null;
    }

    
    /**
     * This method wrapps the {@link KnowledgeObject} from the given {@link SearchResult}
     * and returns the value for the given property.
     */
    protected Object getValueFromWrapper(String aProperty, SearchResult aResult) {
		// IGNORE FindBugs(RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT): For better portability.
		Wrapper searchedObject = WrapperFactory.getWrapper(aResult.getObject());
		return WrapperAccessor.INSTANCE.getValue(searchedObject, aProperty);
    }

    
    /**
     * This method returns the value of a {@link SearchResultAttribute} from
     * an list of SRAs for the given key.
     */
    protected Object iterOverSearchAttributes(List someAtts, String aKey) {
        for (int i = 0; i < someAtts.size(); i++) {
            SearchResultAttribute theAttribute = (SearchResultAttribute) someAtts.get(i);
            if (theAttribute.getKey().equals(aKey)) {
                return theAttribute.getValue();
            }
        }
        return null;
    }
    
}
