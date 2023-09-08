/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.layout;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.layout.person.CreatePersonContactComponent;
import com.top_logic.contact.mandatoraware.COSContactConstants;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Create PersonContacts in COS adding a Mandator info.
 * 
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class COSCreatePersonContactComponent extends
		CreatePersonContactComponent {

	public COSCreatePersonContactComponent(InstantiationContext context, Config someAttrs)
			throws ConfigurationException {
		super(context, someAttrs);
	}

    /** 
     * Add Mandator
     * 
     * @see com.top_logic.layout.form.component.FormComponent#createFormContext()
     */
    @Override
	public FormContext createFormContext() {
    	Mandator theMandator = null;
		Selectable theSelMaster = ((LayoutComponent) this.getDialogParent().getSelectableMaster()).getSelectableMaster();
		if (theSelMaster != null) {
			Object theSel = theSelMaster.getSelected();
			if (theSel instanceof Mandator) {
				theMandator = (Mandator) theSel;
			}
		}
		if (theMandator == null) {
			theMandator = Mandator.getRootMandator();
		}
    	
        FormContext theContext = super.createFormContext(); 
        theContext.addMember(FormFactory.newHiddenField(COSContactConstants.ATTRIBUTE_MANDATOR, theMandator));
        
       return theContext;
    }
}
