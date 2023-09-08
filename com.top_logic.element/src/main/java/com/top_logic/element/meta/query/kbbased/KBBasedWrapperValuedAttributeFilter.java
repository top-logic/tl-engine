/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query.kbbased;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.Filter;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.element.meta.query.WrapperValuedAttributeFilter;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * Optimized implementation for WrapperValuedAttributeFilter
 * that uses implementation details to speed up search.
 *
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class KBBasedWrapperValuedAttributeFilter extends
        WrapperValuedAttributeFilter {

    /**
     * CTor with config map
     *
     * @param aValueMap the config map
     * @throws IllegalArgumentException if some config
     * values do not match constraints
     */
    public KBBasedWrapperValuedAttributeFilter(Map aValueMap)
            throws IllegalArgumentException {
        super(aValueMap);
    }

	/**
	 * CTor with attribute, search value list and negation flag.
	 *
	 * @param anAttribute
	 *        the attribute
	 * @param aWrapperList
	 *        the List of Wrappers (search values)
	 * @param doNegate
	 *        if true negate result
	 * @param isRelevant
	 *        the relevant flag
	 * @throws IllegalArgumentException
	 *         if some attributes do not match constraints
	 */
    public KBBasedWrapperValuedAttributeFilter(TLStructuredTypePart anAttribute,
            List aWrapperList, boolean doNegate, boolean isRelevant, String anAccessPath)
            throws IllegalArgumentException {
        super(anAttribute, aWrapperList, doNegate, isRelevant, anAccessPath);

        TLStructuredTypePart theAttr = this.getAttribute();
		if (!TLModelUtil.isReference(theAttr)) {
			throw new IllegalArgumentException("Unsupported non-reference attribute: '" + theAttr + "'");
		}
    }

	/**
	 * Works with optimized algorithm
	 *
	 * TODO TSA check if it works with access paths
	 *
	 * @see com.top_logic.element.meta.query.CollectionFilter#filter(java.util.Collection)
	 */
	@Override
	public Collection filter(Collection aList)
			throws NoSuchAttributeException, AttributeException {

    	if (!this.isRelevant()) {
    		return aList;
    	}

    	List theReturn;
    	final String theAccessPath = this.getAccessPath();
        if (StringServices.isEmpty(theAccessPath)) {
            theReturn = new ArrayList(aList);
     		Collection connectedContracts = getConnectedElements();
    		if (this.getNegate()) {
    		    theReturn.removeAll(connectedContracts);
    		}
    		else {
    		    theReturn.retainAll(connectedContracts);
    		}
    	} else {
    	    theReturn = new ArrayList();
    	    Collection theSearchWrappers = this.getSearchWrappers();
    	    for (Iterator theIt = aList.iterator(); theIt.hasNext();) {
                Wrapper theObject = (Wrapper) theIt.next();
                Wrapper theBaseObject = this.getBaseObject(theObject);
                Object theValue = theBaseObject.getValue(this.getAttribute().getName());
                if (theValue instanceof Collection) {
                    Collection theColl = (Collection) theValue;
                    if (CollectionUtil.containsAny(theColl, theSearchWrappers)) {
                        theReturn.add(theObject);
                    }
                } else {
                    if (theSearchWrappers.contains(theValue)) {
                        theReturn.add(theObject);
                    }
                }
            }
    	    if (this.getNegate()) {
    	        Collection tmp = theReturn;
    	        theReturn = new ArrayList(aList);
    	        theReturn.removeAll(tmp);
    	    } else {
    	        theReturn.retainAll(aList);
    	    }
    	}
		return theReturn;
	}

	/**
	 * Get the objects that carry at least one of the search objects as attribute value.
	 *
	 * @return a set of objects
	 */
	private Collection getConnectedElements() {
		Set           theResult   = new HashSet();
		List          theWrappers = this.getSearchWrappers();
        TLStructuredTypePart theMA       = this.getAttribute();

		if (CollectionUtil.isEmptyOrNull(theWrappers)) { // Search for empty Values
		    TLClass  theME   = AttributeOperations.getMetaElement(theMA);
		    final String theName = theMA.getName();

		    getAll(theME, theResult, new Filter() {
		        @Override
				public boolean accept(Object anObject) {
		            Object theValue = ((Wrapper) anObject).getValue(theName);
		            return theValue == null || ((theValue instanceof Collection) && ((Collection) theValue).isEmpty());
		        }
		    });
		}
		else {
			for (Iterator theIt = theWrappers.iterator(); theIt.hasNext(); ) {
    			Wrapper theWrapper = (Wrapper) theIt.next();

				if (TLModelUtil.isReference(theMA)) {
					theResult.addAll(AttributeOperations.getReferers(theWrapper, theMA));
    			}
    			else { 
					theResult.addAll(WrapperMetaAttributeUtil.getWrappersWithValue(theMA, theWrapper));
    		    }
    		}
		}
		return theResult;
	}

	private void getAll(TLClass aME, Collection aResult, Filter aFilter) {
		try (CloseableIterator<Wrapper> theObjectIterator =
			MetaElementUtil.iterateDirectInstances(aME, Wrapper.class)) {
            while (theObjectIterator.hasNext()) {
				Wrapper theWrapper = theObjectIterator.next();
                if (aFilter.accept(theWrapper)) {
                    aResult.add(theWrapper);
                }
            }
			for (Iterator<TLClass> theIt = aME.getSpecializations().iterator(); theIt.hasNext();) {
				getAll((theIt.next()), aResult, aFilter);
            }
        }
	}

	/**
     * Overridden to check for Check TypedWrapperMetaAttribute or WrapperSetMetaAttribute.
     */
    @Override
	public void setUpFromValueMap(Map aValueMap)
            throws IllegalArgumentException {
        super.setUpFromValueMap(aValueMap);

        TLStructuredTypePart theAttr = this.getAttribute();
		if (!TLModelUtil.isReference(theAttr)) {
			throw new IllegalArgumentException ("Only TypedWrapperMetaAttribute and WrapperSetMetaAttribute allowed!");
		}
    }

}
