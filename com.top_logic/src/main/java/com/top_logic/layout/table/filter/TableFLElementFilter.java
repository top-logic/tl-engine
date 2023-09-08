/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.table.filter.AbstractClassificationTableFilterProvider.HistoricPattern;
import com.top_logic.layout.table.filter.AbstractClassificationTableFilterProvider.MultiMatcher;
import com.top_logic.layout.table.filter.AbstractClassificationTableFilterProvider.SingleMatcher;
import com.top_logic.model.TLClassifier;

/**
 * The filter takes an array of FastListElements as configuration.
 * 
 * It accepts FastListElements or Lists or SelectFields as input Objects.
 * From them it extracts the contains, if they are FastListElements the accept method
 * will perform its duty.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class TableFLElementFilter extends AbstractTableFLElementFilter {

	private Set<? extends TLClassifier> elements;
    
    private boolean basedOnSingleElement;
    
	private final HistoricPattern<TLClassifier> _matcher;

	public TableFLElementFilter(TLClassifier anElement) {
		assert anElement != null : "Missing classifier.";
    	basedOnSingleElement = true;
		_matcher = new SingleMatcher(anElement);
    }
    
	public TableFLElementFilter(Set<? extends TLClassifier> anElementSet) {
        elements             = anElementSet;
        basedOnSingleElement = false;
		_matcher = new MultiMatcher(anElementSet);
    }
    
    /**
     * Only accept the configured FastListElement
     * @see com.top_logic.basic.col.Filter#accept(java.lang.Object)
     */
    @Override
	public boolean accept(Object anObject) {
		TLClassifier elementToCheck = extractFastListElement(anObject);
    	if (basedOnSingleElement) {
			if (elementToCheck == null) {
				// There is an explicit filter that accepts the null / unset value.
				return false;
			}
			return _matcher.matches(elementToCheck);
    	}
    	else {
    		if (elementToCheck == null && CollectionUtil.isEmptyOrNull(elements)) {
    			return true;
    		}
    		else if (elements != null){
				return _matcher.matches(elementToCheck);
    		}
    		else {
    			return false;
    		}
    	}
    }    
}

