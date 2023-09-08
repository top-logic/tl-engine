/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.search;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.contact.mandatoraware.COSContactConstants;
import com.top_logic.contact.mandatoraware.COSPersonContact;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.layout.meta.search.AttributedSearchComponent;
import com.top_logic.element.layout.meta.search.SearchFieldSupport;
import com.top_logic.element.layout.meta.search.SearchFilterSupport;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.element.meta.query.MetaAttributeFilter;
import com.top_logic.element.meta.query.StoredQuery;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;

/**
 * The ContactSearchFilterSupport adds support for mandator filters in the
 * contact search. The default configuration would create multi selection fields
 * for the mandator filter. This is not wanted for purchasing. so the multi
 * selection fields have to be replaced by single selection fields and their
 * values have to be copied.
 * 
 * @author    <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class ContactSearchFilterSupport extends SearchFilterSupport {
   
    public static final ContactSearchFilterSupport INSTANCE = new ContactSearchFilterSupport();
    
	public static final Property<String> MANDATOR_CONSTANT = TypedAnnotatable.property(String.class, "mandatorAttributeName");

	@Override
	public void fillFormContext(AttributeFormContext aContext, StoredQuery aQuery) {
        super.fillFormContext(aContext, aQuery);
        
		String property = aContext.get(MANDATOR_CONSTANT);
		String theAttributeName = (property != null) ? property : COSContactConstants.ATTRIBUTE_MANDATOR;
        
        fillMandatorField(aContext, aQuery, theAttributeName);
    }

	/**
	 * Creates the single selection fields for the mandator and sets the valid values from the given
	 * {@link StoredQuery}.
	 * 
	 * @param formContext
	 *        the {@link AttributeFormContext} to use, must not be <code>null</code>.
	 * @param aQuery
	 *        the {@link StoredQuery} to use, might be <code>null</code>.
	 * @param anAttrName
	 *        the name of the mandator attribute for the search {@link TLClass} used in the
	 *        {@link AttributedSearchComponent}.
	 */
	private void fillMandatorField(AttributeFormContext formContext, StoredQuery aQuery, String anAttrName) {
		AttributedSearchComponent theComp =
			(AttributedSearchComponent) formContext.get(SearchFieldSupport.CONTROL_COMPONENT_KEY);
        boolean                   isExtended    = theComp.isExtendedSearch();
        TLClass               theSearchME   = theComp.getSearchMetaElement();
		TLStructuredTypePart             theMandatorMA = null;
        boolean                   hasMandator   = false;
        try {
			theMandatorMA = MetaElementUtil.getMetaAttribute(theSearchME, anAttrName);
            hasMandator   = true;
        }
        catch (NoSuchAttributeException e) {
            Logger.error("Could not access mandator meta attribute", e, this.getClass());
        }
		if (aQuery!= null) {
        	if (hasMandator) {
        		MetaAttributeFilter theMandatorFilter = aQuery.getFilterFor(theMandatorMA, null);

        		if (theMandatorFilter != null) {
					String theFieldName = MetaAttributeGUIHelper.getAttributeIDCreate(theMandatorMA);
					SelectField theMandatorField = (SelectField) formContext.getField(theFieldName);
					Object theVal = theMandatorFilter
						.getSearchValuesAsUpdate(formContext.getAttributeUpdateContainer(), theSearchME, null)
						.getCorrectValues();

        			if (isExtended) {
						FormField theRelevantField =
							formContext.getField(getRelevantAndNegateMemberName(theMandatorMA, null));
        				boolean     isRelevant       = theMandatorFilter.isRelevant();
        				boolean     isNegative       = theMandatorFilter.getNegate();
        				theRelevantField.setValue(isRelevant ? Boolean.valueOf(!isNegative) : null);
        			}

        			theMandatorField.setValue(theVal);
        		}
        	}
        }
        else {
			String theFieldName = MetaAttributeGUIHelper.getAttributeIDCreate(theMandatorMA);
			if (formContext.hasMember(theFieldName)) {
				SelectField theMandatorField = (SelectField) formContext.getField(theFieldName);
        		Mandator    actualMandator   = COSPersonContact.getActualMandator();

        		if (isExtended) {
					FormField theRelevantField =
						formContext.getField(getRelevantAndNegateMemberName(theMandatorMA, null));
        			theRelevantField.setValue(Boolean.TRUE);
        		}

        		theMandatorField.setAsSingleSelection(actualMandator);
        	}
        }
	}
}

