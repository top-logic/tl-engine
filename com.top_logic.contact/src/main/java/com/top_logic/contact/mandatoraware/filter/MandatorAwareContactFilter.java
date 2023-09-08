/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.element.core.TraversalFactory;
import com.top_logic.element.core.util.AllElementVisitor;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.AbstractAttributedValueFilter;
import com.top_logic.element.meta.kbbased.filtergen.CollectionCheckingValueFilter;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.element.structured.wrap.MandatorAware;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Checks if contact is attached to suitable mandator
 * 
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
@Deprecated
public abstract class MandatorAwareContactFilter extends AbstractAttributedValueFilter implements CollectionCheckingValueFilter {

	public static final String MANDATOR_ATTRIBUTE = "mandator";

	/**
	 * Check if the Mandator is correct in the current context
	 * 
	 * @param anAttributed
	 *        the attibuted (e.g. a Contract). May be <code>null</code> in a search (update
	 *        container must be given then)
	 * @param editContext
	 *        the update container (may be <code>null</code>)
	 * @param aContact
	 *        the contact whose mandator is to be checked
	 * @return true if the mandator is suitable or cannot be checked
	 */
	protected boolean checkMandator(TLObject anAttributed, EditContext editContext,
			Object aContact) {
		Mandator theMandator = getMandator(anAttributed, editContext);

		boolean isSearch = isSearch(anAttributed, editContext);
		
        if (theMandator != null) {
        	// Performance short cut to speed up initial search
        	if (isSearch && theMandator.equals(Mandator.getRootMandator())) {
        		return true;
        	}
        	
    		Mandator theContactMandator = ((MandatorAware) aContact).getMandator();
    		if (theContactMandator != null) {
    			boolean found = false;
    			Mandator theParent = theMandator;
    			while (!found && (theParent != null)) {
    				found = theParent.equals(theContactMandator);
    				theParent = (Mandator) theParent.getParent();
    			}
    			
    			if (found || !isSearch) {	
    				// If we are not in a search we offer only contacts
    				// attached to the mandator of the attributed or to one
    				// of its parents
    				return found;
    			}
    			else {
    				// In a search we additionally offer all contacts attached 
    				// to child mandators
        			theParent = theContactMandator;
        			while (!found && (theParent != null)) {
        				found = theParent.equals(theMandator);
        				theParent = (Mandator) theParent.getParent();
        			}

        			return found;
    			}
    		}
        }
        
        return true;
	}
	
	protected boolean checkBasics(TLObject anAttributed, EditContext editContext,
			Object anObject) {
		return true;
	}
	
	protected List checkBasics(TLObject anAttributed, EditContext editContext,
			FormContext aContext, List aList) {
    	List theRes = new ArrayList();
    	if (aList != null) {
	    	Iterator theVals = aList.iterator();
	    	while (theVals.hasNext()) {
				Object theVal = theVals.next();
				if (this.checkBasics(anAttributed, editContext, theVal)) {
					theRes.add(theVal);
				}
			}
    	}
    	
    	return theRes;
    }
	
    /**
	 * Accept the value if it is a CompanyContact that may act as a Client
	 */
    @Override
	public boolean accept(Object anObject, EditContext editContext) {
		TLObject object = editContext.getObject();
		if (!this.checkBasics(object, editContext, anObject)) {
        	return false;
        }
    	
		return this.checkMandator(object, editContext, anObject);
    }
    
    @Override
	public List filter(TLObject anAttributed, EditContext editContext, FormContext aContext,
			List aList) {
    	if (aList == null) {
    		return aList;
    	}
    	
		List theRes = this.checkBasics(anAttributed, editContext, aContext, aList);

		return this.checkMandator(anAttributed, editContext, theRes);
    }

	/**
	 * Check Mandator on a collection of MandatorAware objects and return the matching objects
	 * 
	 * @param anAttributed
	 *        the attibuted (e.g. a Contract). May be <code>null</code> in a search (update
	 *        container must be given then)
	 * @param editContext
	 *        the edit context (may be <code>null</code>)
	 * @param aContactList
	 *        the contacts whose mandator is to be checked
	 * @return the matching contacts. The initialial collection if no mandator is given or the
	 *         collection is null.
	 */
	protected List checkMandator(TLObject anAttributed, EditContext editContext,
			List aContactList) {
		Mandator theMandator = getMandator(anAttributed, editContext);

		boolean isSearch = isSearch(anAttributed, editContext);
		
        if (theMandator != null) {
        	// Performance short cut to speed up initial search
        	if (isSearch && theMandator.equals(Mandator.getRootMandator())) {
        		return aContactList;
        	}
        	
        	Set  theMandatorSet = new HashSet();
        	List theResult      = new ArrayList();
        	
			Mandator theParent = theMandator;
			while (theParent != null) {
				theMandatorSet.add(theParent);
				theParent = (Mandator) theParent.getParent();
			}
			
			if (isSearch) {	
				// In a search we additionally allow all child mandators as well
				AllElementVisitor theVis = new AllElementVisitor();
	            TraversalFactory.traverse(theMandator, theVis, TraversalFactory.DEPTH_FIRST);
				theMandatorSet.addAll(theVis.getList());
			}
    	
			if (aContactList != null) {
				Iterator theContacts = aContactList.iterator();
				while (theContacts.hasNext()) {
					MandatorAware theAware = (MandatorAware) theContacts.next();
					if (theMandatorSet.contains(theAware.getMandator())) {
						theResult.add(theAware);
					}
				}
				
				return theResult;
			}
        }

        return aContactList;
	}
        
	public static boolean isSearch(TLObject anAttributed, EditContext editContext) {
		boolean isSearch = false;

		if (editContext != null) {
			isSearch = editContext.isSearchUpdate();
		}
		return isSearch;
	}

	public static Mandator getMandator(TLObject anAttributed, EditContext editContext) {
		if (editContext != null) {
			try {
				TLStructuredType metaElement = editContext.getType();
				if (MetaElementUtil.hasMetaAttribute(metaElement, MANDATOR_ATTRIBUTE)) {
					TLStructuredTypePart theMandatorAttribute = MetaElementUtil.getMetaAttribute(metaElement, MANDATOR_ATTRIBUTE);
					AttributeUpdate theMandatorUpdate =
						editContext.getOverlay().getScope().getAttributeUpdate(theMandatorAttribute, anAttributed);
					if (theMandatorUpdate != null) {
						Object theVal = theMandatorUpdate.getCorrectValues();
						return (Mandator) CollectionUtil.getFirst(theVal);
					}
				}
			}
			catch (Exception ex) {
				// Ignore here
			}
		}
		return null;
	}
}
