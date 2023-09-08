/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.AttributeUpdateFactory;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Filter TypedWrapperMetaAttributes and WrapperSetMetaAttributes.
 *
 * The filter retains all source objects that have at least one of
 * the given objects as attribute value.
 *
 * @author    <a href="mailto:fma@top-logic.com"></a>
 */
public class WrapperValuedAttributeFilter extends MetaAttributeFilter {

	/** the values as a list of wrapper. */
	private List<? extends TLObject> _values;

    /**
     * Comment for <code>SORT_ORDER</code>
     */
    protected static final Integer SORT_ORDER = Integer.valueOf(1);

	/** key for value Map to store wrapper ids. */
    protected static final String KEY_WRAPPERIDS    = "wrapperids";

    /** key for value Map to store wrapper types */
    protected static final String KEY_WRAPPERTYPES  = "wrappertypes";

	/**
	 * CTor that gets all values from a Map
	 *
	 * @param aValueMap the Map with all values. Must not be <code>null</code>.
	 * @throws IllegalArgumentException if the map is <code>null</code>
	 * or some of its values do not match the filter's constraints.
	 */
	public WrapperValuedAttributeFilter(Map aValueMap) throws IllegalArgumentException {
		super(aValueMap);

		this.setUpFromValueMap(aValueMap);
	}

	/**
	 * CTor with attribute, search value list and negation flag.
	 *
	 * @param anAttribute
	 *        the attribute
	 * @param value
	 *        the List of Wrappers (search values)
	 * @param doNegate
	 *        if true negate result
	 * @param isRelevant
	 *        the relevant flag
	 * @throws IllegalArgumentException
	 *         if some attributes do not match constraints
	 */
	public WrapperValuedAttributeFilter(TLStructuredTypePart anAttribute, Collection<? extends TLObject> value, boolean doNegate,
			boolean isRelevant, String anAccessPath)
			throws IllegalArgumentException {
		super(anAttribute, doNegate, isRelevant, anAccessPath);
		_values = CollectionUtil.toList(value);
	}

	/**
	 * @see com.top_logic.element.meta.query.CollectionFilter#filter(java.util.Collection)
	 */
	@Override
	public Collection filter(Collection aList)
			throws NoSuchAttributeException, AttributeException {
    	if (!this.isRelevant()) {
    		return aList;
    	}

	    Iterator theSrcObjs = aList.iterator();
	    List theResult = new ArrayList();
	    List theSearchWrappers = this.getSearchWrappers();
	    while (theSrcObjs.hasNext()) {
            Wrapper theSrcObj = this.getBaseObject((Wrapper)theSrcObjs.next());
            TLStructuredTypePart attribute = this.getAttribute();
			if (!AttributeOperations.isCollectionValued(attribute)) {
                Object theValue = AttributeOperations.getAttributeValue(theSrcObj, attribute);
                if (theSearchWrappers.contains(theValue) != this.getNegate()) {
                    theResult.add(theSrcObj);
                }
            }
            else {
                Collection theValues = (Collection) AttributeOperations.getAttributeValue(theSrcObj, attribute);
				if (!theValues.isEmpty()) {
					theValues = new ArrayList(theValues);
					theValues.retainAll(theSearchWrappers);
				}
                if (theValues.isEmpty() == this.getNegate()) {
                    theResult.add(theSrcObj);
                }
            }
        }

	    return theResult;
	}


	/**
	 * @see com.top_logic.element.meta.query.BasicCollectionFilter#getSortOrder()
	 */
	@Override
	public Integer getSortOrder() {
		return SORT_ORDER;
	}

	/**
	 * Add wrapper list
	 *
     * @see com.top_logic.element.meta.query.MetaAttributeFilter#getValueMap()
     */
    @Override
	public Map<String, Object> getValueMap() {
        Map theMap = super.getValueMap();

        // Add wrapper list
        List theWrappers = this.getWrappers();
        if (theWrappers != null && !theWrappers.isEmpty()) {
			List<String> theTypes = new ArrayList<>(1); // expecting only one type.
            int size = theWrappers.size();
            StringBuffer hlp = new StringBuffer(size << 5); // * 32;
			TLObject wRaPp3R = (TLObject) theWrappers.get(0);
            theTypes.add(wRaPp3R.tTable().getName());
			hlp.append(IdentifierUtil.toExternalForm(KBUtils.getWrappedObjectName(wRaPp3R)));
            for (int i=1; i < size; i++) {
				wRaPp3R = (TLObject) theWrappers.get(i);
                hlp.append(StoredQuery.SEPARATOR)
					.append(IdentifierUtil.toExternalForm(KBUtils.getWrappedObjectName(wRaPp3R)));
				String theType = wRaPp3R.tTable().getName();
				if (!theTypes.contains(theType)) {
					theTypes.add(theType);
				}
			}
	        theMap.put(KEY_WRAPPERIDS  , hlp.toString());
            theMap.put(KEY_WRAPPERTYPES, StringServices.toString(theTypes,String.valueOf(StoredQuery.SEPARATOR)));
        }

        return theMap;
    }

    /**
     * Set up wrapper list
     *
     * @see com.top_logic.element.meta.query.MetaAttributeFilter#setUpFromValueMap(java.util.Map)
     */
    @Override
	public void setUpFromValueMap(Map aValueMap)
            throws IllegalArgumentException {
        super.setUpFromValueMap(aValueMap);

		_values = null;
        Object theIDs = aValueMap.get(KEY_WRAPPERIDS);
		if (theIDs == null) {
			return;
		}
		if (!(theIDs instanceof String)) {
            throw new IllegalArgumentException ("Value for key " + KEY_WRAPPERIDS + " must be a String!");
        }
        Object   theOTypes = aValueMap.get(KEY_WRAPPERTYPES);
		if (StringServices.isEmpty(theOTypes)) {
			throw new IllegalArgumentException("Value for key " + KEY_WRAPPERTYPES + " must nether null nor empty!");
        }
		if (!(theOTypes instanceof String)) {
			throw new IllegalArgumentException("Value for key " + KEY_WRAPPERTYPES + " must be a String!");
		}
		String[] theTypes = StringServices.toArray((String) theOTypes, StoredQuery.SEPARATOR);
        KnowledgeBase  theKB      = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
		List<String> theWrappers = StringServices.toList((String) theIDs, StoredQuery.SEPARATOR);
		int len = theWrappers.size();

		List<TLObject> newValues = new ArrayList<>(len);
		for (int i = 0; i < len; i++) {
			TLID theId = IdentifierUtil.fromExternalForm(theWrappers.get(i));
			TLObject theWrapper = WrapperFactory.getWrapper(theId, theTypes, theKB);
			newValues.add(theWrapper);
		}
		updateValues(newValues);
    }

    @Override
	public AttributeUpdate getSearchValuesAsUpdate(AttributeUpdateContainer updateContainer, TLStructuredType type,
			String domain) {
		return AttributeUpdateFactory.createAttributeUpdateForSearch(
			updateContainer, type, getAttribute(), getWrappers(), null, domain);
    }

    /**
     * Get the Wrappers to search for.
     * May differ from the ones gives.
     *
     * @return the Wrappers
     */
    protected List getSearchWrappers() {
    	return this.getWrappers();
    }

    /**
     * Get the internal wrapper List. Might be different from the search Wrappers
     *
     * @return the internal wrappers
     */
	public List<? extends TLObject> getWrappers() {
		return _values;
	}

	protected void updateValues(List<? extends TLObject> newValues) {
		_values = newValues;
    }

}
