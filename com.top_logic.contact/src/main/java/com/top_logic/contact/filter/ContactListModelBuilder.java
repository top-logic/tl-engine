/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.filter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.mandatoraware.COSCompanyContact;
import com.top_logic.contact.mandatoraware.COSPersonContact;
import com.top_logic.element.layout.meta.search.AbstractGlobalSearchModelBuilder;
import com.top_logic.element.layout.meta.search.AttributedSearchComponent;
import com.top_logic.element.layout.meta.search.ExtendedSearchModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;

/**
 * A model builder for finding attributed of the meta element type contact.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ContactListModelBuilder extends AbstractGlobalSearchModelBuilder implements ExtendedSearchModelBuilder {

	/**
	 * Singleton {@link ContactListModelBuilder} instance.
	 */
	public static final ContactListModelBuilder INSTANCE = new ContactListModelBuilder();

	private ContactListModelBuilder() {
		// Singleton constructor.
	}

    /**
     * @see com.top_logic.mig.html.ModelBuilder#getModel(Object, com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
        TLClass theME   = ((AttributedSearchComponent) aComponent).getSearchMetaElement();
        String      theType = theME.getName();

        if (theType.equals(CompanyContact.META_ELEMENT)) {
            theType = ContactFactory.COMPANY_TYPE;
        }
        else if (theType.equals(PersonContact.META_ELEMENT)) {
            theType =  ContactFactory.PERSON_TYPE;
        }

        return ContactFactory.getInstance().getAllContacts(theType);
    }

    /**
     * @see com.top_logic.mig.html.ModelBuilder#supportsModel(java.lang.Object, LayoutComponent)
     */
    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        return aModel instanceof TLClass;
    }
    
	private static final String[] PERSON_CONTACT_COLUMNS = new String[] { COSPersonContact.NAME_ATTRIBUTE,
		COSPersonContact.ATT_PHONE_OFFICE, COSPersonContact.ATT_MAIL, COSPersonContact.ATTRIBUTE_MANDATOR };

	private static final String[] COMPANY_CONTACT_COLUMNS = new String[] { COSCompanyContact.NAME_ATTRIBUTE,
		COSCompanyContact.ATTRIBUTE_SAP_NR, COSCompanyContact.ATTRIBUTE_FKEY, COSCompanyContact.ATTRIBUTE_MANDATOR };
    private static final String[] PERSON_CONTACT_EXCLUDE  = new String[] {"validityState"};
    private static final String[] COMPANY_CONTACT_EXCLUDE = new String[] {"validityState"};
    
    /**
     * @see com.top_logic.element.layout.meta.search.ExtendedSearchModelBuilder#getResultColumnsFor(com.top_logic.model.TLClass)
     */
    @Override
	public List getResultColumnsFor(TLClass aMetaElement) {
        if (this.isPersonContact(aMetaElement)) {
            return Arrays.asList(PERSON_CONTACT_COLUMNS);
        }
        else {
            return Arrays.asList(COMPANY_CONTACT_COLUMNS);
        }
    }

    @Override
	public Set getExcludedAttributesForSearch(TLClass aMetaElement) {
        if (this.isPersonContact(aMetaElement)) {
            return new HashSet(Arrays.asList(PERSON_CONTACT_EXCLUDE));
        }
        else {
            return new HashSet(Arrays.asList(COMPANY_CONTACT_EXCLUDE));
        }
    }
    
        @Override
		public Set getExcludedAttributesForColumns(TLClass aMetaElement) {
    	if (this.isPersonContact(aMetaElement)) {
            return new HashSet(Arrays.asList(PERSON_CONTACT_EXCLUDE));
        }
        else {
            return new HashSet(Arrays.asList(COMPANY_CONTACT_EXCLUDE));
        }
	}
    
    @Override
	public Set getExcludedAttributesForReporting(TLClass aMetaElement) {
		return getExcludedAttributesForSearch(aMetaElement);
	}
    
    /**
     * @see com.top_logic.element.layout.meta.search.ExtendedSearchModelBuilder#getJspFor(com.top_logic.model.TLClass)
     */
    @Override
	public String getJspFor(TLClass aMetaElement) {
        return null;
    }
    /**
     * @see com.top_logic.element.layout.meta.search.AbstractGlobalSearchModelBuilder#getMetaElement(java.lang.String)
     */
    @Override
	public TLClass getMetaElement(String aME) throws IllegalArgumentException {
        return super.getMetaElement(aME);
    }
    /**
     * @see com.top_logic.contact.filter.ContactListModelBuilder#getModel(Object, com.top_logic.mig.html.layout.LayoutComponent)
     */
    private boolean isPersonContact(TLClass aME) {
        return PersonContact.META_ELEMENT.equals(aME.getName());
    }

}

