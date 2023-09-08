/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.layout.person.PersonContactModelBuilder;
import com.top_logic.contact.mandatoraware.COSContactConstants;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.element.structured.wrap.MandatorAware;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;

/**
 * The COSCompanyContactModelBuilder is mandatoraware
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class COSPersonContactModelBuilder extends PersonContactModelBuilder {

    private TLStructuredTypePart metaAttribute;

	/**
	 * Singleton {@link COSPersonContactModelBuilder} instance.
	 */
	public static final COSPersonContactModelBuilder INSTANCE = new COSPersonContactModelBuilder();

	protected COSPersonContactModelBuilder() {
		// Singleton constructor.
	}
    
    /**
     * @see com.top_logic.contact.layout.company.CompanyContactModelBuilder#retrieveModelFromListElement(com.top_logic.mig.html.layout.LayoutComponent, java.lang.Object)
     */
    @Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anObject) {
        return (((MandatorAware) anObject).getMandator());
    }
    
    protected List retrieveListFromModel(LayoutComponent aComponent, Object aModel) {
        try {
			Collection theColl = AttributeOperations.getReferers((Wrapper) aModel, this.getMandatorMetaAttribute());
            List       theResult = new ArrayList(theColl.size());

            for (Iterator theIt = theColl.iterator(); theIt.hasNext(); ) {
                Object theObject = theIt.next();

                if (this.supportsListElement(aComponent, theObject)) {
                    theResult.add(theObject);
                }
            }

            return (theResult);
        }
        catch (Exception ex) {
            Logger.error("Unable to get list for model " + aModel, ex, this);
            return (Collections.EMPTY_LIST);
        }
    }
    
    /**
     * @see com.top_logic.contact.layout.company.CompanyContactModelBuilder#getModel(Object, com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		return this.retrieveListFromModel(aComponent, businessModel);
    }
    
    /**
     * @see com.top_logic.contact.layout.company.CompanyContactModelBuilder#supportsListElement(com.top_logic.mig.html.layout.LayoutComponent, java.lang.Object)
     */
    @Override
	public boolean supportsListElement(LayoutComponent aComponent, Object anObject) {
        return (anObject instanceof PersonContact);
    }
    
    /**
     * @see com.top_logic.contact.layout.company.CompanyContactModelBuilder#supportsModel(java.lang.Object, com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        return (aModel instanceof Mandator) || aModel == null;
    }
    
    /**
	 * Return the meta attribute mandator from the company contact meta element description.
	 * 
	 * @return The requested attribute, never <code>null</code>.
	 */
    protected TLStructuredTypePart getMandatorMetaAttribute() throws NoSuchAttributeException {
        if (this.metaAttribute == null) {
            TLClass   theME = ContactFactory.getInstance().getMetaElement(PersonContact.META_ELEMENT);
			this.metaAttribute = MetaElementUtil.getMetaAttribute(theME, COSContactConstants.ATTRIBUTE_MANDATOR);
        }

        return (this.metaAttribute);
    }
}

