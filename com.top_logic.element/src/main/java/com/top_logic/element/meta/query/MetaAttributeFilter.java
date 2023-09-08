/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.Utils;

/**
 * Filters based on MetaAttributes.
 *
 * @author    <a href="mailto:kbu@top-logic.com"></a>
 */
public abstract class MetaAttributeFilter extends BasicCollectionFilter {

	/** key for value Map to store attribute ID. */
    protected static final String KEY_ID = "id";

	/** key for value Map to store attribute name. */
    protected static final String KEY_NAME = "maName";

    /** key for value Map to store access path. */
    protected static final String KEY_ACCESS_PATH = "accessPath";

	/**
	 * key for value Map to put in type of attribute. NOTE: ME is NOT stored! Do NOT put in ME in
	 * getValueMap!
	 */
    public static final String MAP_META_ELEMENT = "_meta_element";

    /** The attribute whose contents is used to check filter conditions. */
	private TLStructuredTypePart attribute;

	/**
	 * A path to access the object the value to be used to filter is part of.
	 *
	 * If the access path is not empty, the filter method must perform its test on the objects
	 * refered by the access path, i.e. use
	 * {@link Utils#getValueByPath(String, com.top_logic.model.TLObject)} to get the base object,
	 * then call getValue() with the meta attribue.
	 */
	private String accessPath;

	/**
	 * CTor with an attribute
	 *
	 * @param anAttribute the attribute for filtering
	 * @param doNegate the negation flag
	 * @param isRelevant the relevant flag
	 */
	public MetaAttributeFilter(TLStructuredTypePart anAttribute, boolean doNegate, boolean isRelevant, String anAccessPath) {
		super(doNegate, isRelevant);
		this.setAttribute(anAttribute);
		this.accessPath = anAccessPath;
	}

	/**
     * This method returns the accessPath.
     *
     * @return    Returns the accessPath.
     */
    public String getAccessPath() {
        return (this.accessPath);
    }

    public void setAccessPath(String anAccessPath) {
        this.accessPath = anAccessPath;
    }

	/**
	 * CTor that gets all values from a Map
	 *
	 * @param aValueMap the Map with all values. Must not be <code>null</code>.
	 * @throws IllegalArgumentException if the map is <code>null</code>
	 * or some of its values do not match the filter's constraints.
	 */
	public MetaAttributeFilter(Map aValueMap) throws IllegalArgumentException {
		super(aValueMap);

		this.setUpFromValueMap(aValueMap);
	}

	protected Wrapper getBaseObject(Wrapper anObject) {
	    if (StringServices.isEmpty(this.accessPath)) {
	        return anObject;
	    } else {
	        return (Wrapper) Utils.getValueByPath(this.accessPath, anObject);
	    }
	}

	/**
	 * Get the attribute
	 *
	 * @return the attribute
	 */
	public TLStructuredTypePart getAttribute() {
		return attribute;
	}

	/**
	 * Get the attribute that is really filtered.
	 * May be different from the atrribute
	 *
	 * @return the attribute
	 */
	public TLStructuredTypePart getFilteredAttribute() {
		return attribute;
	}

	/**
	 * Set the attribute
	 *
	 * @param anAttribute the attribute
	 */
	private void setAttribute (TLStructuredTypePart anAttribute) {
		this.attribute = anAttribute;
	}

	/**
	 * Add SimpleMetaAttribute ID
	 *
     * @see com.top_logic.element.meta.query.BasicCollectionFilter#getValueMap()
     */
    @Override
	public Map<String, Object> getValueMap() {
		Map<String, Object> theMap = super.getValueMap();

        theMap.put(KEY_ID,   KBUtils.getWrappedObjectName(this.getAttribute()));
        theMap.put(KEY_NAME, this.getAttribute().getName());
        if ( ! StringServices.isEmpty(this.accessPath)) {
            theMap.put(KEY_ACCESS_PATH, this.accessPath);
        }

        return theMap;
    }

    /**
     * Get SimpleMetaAttribute ID and set SimpleMetaAttribute
     *
     * @see com.top_logic.element.meta.query.BasicCollectionFilter#setUpFromValueMap(java.util.Map)
     */
    @Override
	public void setUpFromValueMap(Map aValueMap)
            throws IllegalArgumentException {
        super.setUpFromValueMap(aValueMap);

        this.accessPath = (String) aValueMap.get(KEY_ACCESS_PATH);

		TLID theID = (TLID) aValueMap.get(KEY_ID);

        TLStructuredTypePart theMA = null;
		if (!StringServices.isEmpty(theID)) {
			// Try to setup from ID
	        try {
				theMA = (TLStructuredTypePart) WrapperFactory.getWrapper(theID, KBBasedMetaAttribute.OBJECT_NAME);
	        }
	        catch (Exception wex) {
	            throw new IllegalArgumentException("Something wrong getting SimpleMetaAttribute Wrapper for ID " + theID + ": " + wex);
	        }
        }

        if (theMA == null) {								// Try to setup from MA name and ME
        	Object theMEO = aValueMap.get(MAP_META_ELEMENT);
        	if (theMEO instanceof TLClass) {
        		Object theMANameO = aValueMap.get(KEY_NAME);
                if (theMANameO == null || !(theMANameO instanceof String)) {	// Not null if ID was null of not found!
                	throw new IllegalArgumentException ("Value for key " + KEY_NAME + " must be a String!");
                }
        		try {
					theMA = MetaElementUtil.getMetaAttribute(((TLClass) theMEO), (String) theMANameO);
        		}
        		catch (Exception ex) {
					throw new IllegalArgumentException("Error getting attribute for name " + theMANameO
						+ " of type " + ((TLClass) theMEO).getName() + ": " + ex);
        		}
        	}
        }

        if (theMA == null) {
			throw new IllegalArgumentException("No attribute for ID " + theID);
        }

        this.setAttribute(theMA);
    }

    /**
	 * Get an AttributeUpdate with the search value(s) of this filter.
	 * 
	 * @param updateContainer
	 *        The {@link AttributeUpdateContainer} to create the {@link AttributeUpdate} in.
	 * @param domain
	 *        An identifier for the matched object.
	 *
	 * @return an AttributeUpdate with the search value(s) of this filter
	 */
	public abstract AttributeUpdate getSearchValuesAsUpdate(AttributeUpdateContainer updateContainer,
			TLStructuredType type, String domain);

    /**
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
    	try {
    		return this.getClass().getName() + " for attribute " + this.getAttribute().getName();
    	}
    	catch (Throwable t) {
    		return super.toString();
    	}
    }
}
