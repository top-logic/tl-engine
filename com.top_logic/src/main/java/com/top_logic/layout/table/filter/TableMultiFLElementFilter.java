/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.table.filter.AbstractClassificationTableFilterProvider.HistoricPattern;
import com.top_logic.layout.table.filter.AbstractClassificationTableFilterProvider.MultiMatcher;
import com.top_logic.layout.table.filter.AbstractClassificationTableFilterProvider.SingleMatcher;
import com.top_logic.model.TLClassifier;

/**
 * @author     <a href="mailto:jco@top-logic.com">jco</a>
 */
public class TableMultiFLElementFilter extends AbstractTableFLElementFilter{

    private boolean              basedOnSingleElement;

	private TLClassifier singleValidElement;

	private Set<? extends TLClassifier> validElements;

	private final HistoricPattern<TLClassifier> _matcher;
    
	public TableMultiFLElementFilter(TLClassifier anElement) {
        basedOnSingleElement = true;
    	singleValidElement = anElement;
		_matcher = new SingleMatcher(anElement);
    }

	public TableMultiFLElementFilter(Set<? extends TLClassifier> anElementSet) {
        basedOnSingleElement = false;
        validElements = anElementSet;
		_matcher = new MultiMatcher(anElementSet);
    }
	
    @Override
	public boolean accept(Object anObject) {
		if (basedOnSingleElement) {
		    if (singleValidElement == null) {
		        if (anObject == null) {
		            return true;
		        }
				Collection<? extends TLClassifier> elementsToCheck = extractFastListElements(anObject);
                return CollectionUtil.isEmptyOrNull(elementsToCheck);
		    }
		    
			if (anObject == null) return false;
			
			if (singleValidElement != null) {
				if (anObject instanceof TLClassifier) {
					return _matcher.matches((TLClassifier) anObject);
				}
				else {
					Collection<? extends TLClassifier> elementsToCheck = extractFastListElements(anObject);
					if (elementsToCheck != null) {
						return _matcher.matchesAny(elementsToCheck);
					}
				}
			}
		}
		else {
            if (validElements == null) {
                if (anObject == null) {
                    return true;
                }
				Collection<? extends TLClassifier> elementsToCheck = extractFastListElements(anObject);
                return CollectionUtil.isEmptyOrNull(elementsToCheck);
            }
			if (anObject == null) {
				return false;
			}
		        	
			if (anObject instanceof TLClassifier) {
				_matcher.matches((TLClassifier) anObject);
			}
			else {
				Collection<? extends TLClassifier> elementsToCheck = extractFastListElements(anObject);
				
				return _matcher.matchesAny(elementsToCheck);
			}
		}
        return false;
	}


}
