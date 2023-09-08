/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.external.meta;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.contact.external.ExternalContact;
import com.top_logic.contact.external.ExternalContacts;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.AttributeUpdateFactory;
import com.top_logic.element.meta.query.MetaAttributeFilter;
import com.top_logic.element.meta.query.StoredQuery;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class ExternalContactAttributeFilter extends MetaAttributeFilter {

    /**
     * Comment for <code>SORT_ORDER</code>
     */
    protected static final Integer SORT_ORDER = Integer.valueOf(1);

	/** key for value Map to store wrapper ids. */
    protected static final String KEY_CONTACTIDS    = "contactids";

	private Collection<ExternalContact> contacts;
	
	public ExternalContactAttributeFilter(Map aValueMap)
			throws IllegalArgumentException {
		super(aValueMap);
		
		this.setUpFromValueMap(aValueMap);
	}
	
	/**
	 * CTor with attribute, search value list and negation flag.
	 *
	 * @param anAttribute
	 *        the attribute
	 * @param aContactList
	 *        the List of Wrappers (search values)
	 * @param doNegate
	 *        if true negate result
	 * @param isRelevant
	 *        the relevant flag
	 * @throws IllegalArgumentException
	 *         if some attributes do not match constraints
	 */
	public ExternalContactAttributeFilter(TLStructuredTypePart anAttribute, Collection<ExternalContact> aContactList, boolean doNegate, boolean isRelevant, String anAccessPath)
			throws IllegalArgumentException {
		super(anAttribute, doNegate, isRelevant, anAccessPath);
		this.contacts = aContactList;
	}

    @Override
	public AttributeUpdate getSearchValuesAsUpdate(AttributeUpdateContainer updateContainer, TLStructuredType type,
			String domain) {
		return AttributeUpdateFactory.createAttributeUpdateForSearch(
			updateContainer, type, getAttribute(), getSearchContacts(), null, domain);
    }

    /**
     * Get the Wrappers to search for.
     * May differ from the ones gives.
     *
     * @return the Wrappers
     */
    protected Collection<ExternalContact> getSearchContacts() {
    	return this.contacts;
    }

	/**
	 * @see com.top_logic.element.meta.query.BasicCollectionFilter#getSortOrder()
	 */
	@Override
	public Integer getSortOrder() {
		return SORT_ORDER;
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
	    Collection<ExternalContact> theSearchWrappers = this.getSearchContacts();
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
                theValues.retainAll(theSearchWrappers);
                if (theValues.isEmpty() == this.getNegate()) {
                    theResult.add(theSrcObj);
                }
            }
        }

	    return theResult;
	}

	/**
	 * Add ExternalContact list
	 *
     * @see com.top_logic.element.meta.query.MetaAttributeFilter#getValueMap()
     */
    @Override
	public Map<String, Object> getValueMap() {
        Map theMap = super.getValueMap();

        // Add wrapper list
        Collection<ExternalContact> theContacts = this.getSearchContacts();
        if (theContacts != null && !theContacts.isEmpty()) {
            int size = theContacts.size();
            StringBuffer hlp = new StringBuffer(size << 5); // * 32;
            boolean isFirst = true;
            
            for (ExternalContact theContact : theContacts) {
            	if (isFirst) {
            		isFirst = false;
            	}
            	else {
            		hlp.append(StoredQuery.SEPARATOR);
            	}
            	hlp.append(theContact.getUNumber());
            	hlp.append("\\");
                hlp.append(theContact.getSysId());
			}
	        theMap.put(KEY_CONTACTIDS  , hlp.toString());
        }

        return theMap;
    }
    
    /**
     * Set up ExternalContact list
     *
     * @see com.top_logic.element.meta.query.MetaAttributeFilter#setUpFromValueMap(java.util.Map)
     */
    @Override
	public void setUpFromValueMap(Map aValueMap)
            throws IllegalArgumentException {
        super.setUpFromValueMap(aValueMap);

        Object theIDs = aValueMap.get(KEY_CONTACTIDS);
        if ((theIDs != null) && !(theIDs instanceof String)) {
            throw new IllegalArgumentException ("Value for key " + KEY_CONTACTIDS + " must be a String!");
        }

        this.contacts = null;
		if (theIDs != null) {
			List<String>     theContacts = StringServices.toList((String) theIDs, StoredQuery.SEPARATOR);
            int      len         = theContacts.size();
            this.contacts = new ArrayList<>(len);
            
            for (String theID : theContacts) {
                ExternalContact theContact;
				try {
					int index = theID.indexOf('\\');
					String theName = (index < 0) ? theID : theID.substring(0, index - 1);
					String theSysID = (index < 0) ? "LH" : theID.substring(index + 1);
					theContact = ExternalContacts.getContact(theName, theSysID);
					if (theContact != null) {
						contacts.add(theContact);
					}
				} catch (SQLException e) {
					// Ignore, may happen
					// TODO KBU better use assignments to build search filter!!!
				}
			}
		}
    }

}
