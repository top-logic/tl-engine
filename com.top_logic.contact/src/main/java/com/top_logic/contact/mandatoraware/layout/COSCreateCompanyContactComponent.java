/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.layout;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.business.AddressHolder;
import com.top_logic.contact.layout.company.CreateCompanyContactComponent;
import com.top_logic.contact.mandatoraware.COSCompanyContact;
import com.top_logic.contact.mandatoraware.COSContactConstants;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.error.TopLogicException;

/**
 * Create CompanyContacts in COS adding a Mandator info
 * 
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class COSCreateCompanyContactComponent extends
		CreateCompanyContactComponent {

	public COSCreateCompanyContactComponent(InstantiationContext context, Config someAttrs)
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
    	
		AttributeFormContext formContext = (AttributeFormContext) super.createFormContext();
        TLClass theME = this.getMetaElement();
        TLStructuredTypePart theMA;
        try {
			theMA = MetaElementUtil.getMetaAttribute(theME, COSContactConstants.ATTRIBUTE_MANDATOR);
            
			if (formContext.hasMember(MetaAttributeGUIHelper.getAttributeIDCreate(theMA))) {
				FormField theMandatorField = formContext.getField(MetaAttributeGUIHelper.getAttributeIDCreate(theMA));
                if(theMandatorField instanceof SelectField) {
                    theMandatorField.setValue(CollectionUtil.intoList(theMandator));
                } else{
                    theMandatorField.setValue(theMandator);                    
                }
                theMandatorField.setVisible(false);
            }
            
			theMA = MetaElementUtil.getMetaAttribute(theME, AddressHolder.STREET);
			if (!formContext.hasMember(MetaAttributeGUIHelper.getAttributeIDCreate(theMA))) {
				AttributeUpdate theUpdate =
					AttributeUpdateFactory.createAttributeUpdateForCreate(formContext.getAttributeUpdateContainer(),
						theME, theMA);
				theUpdate.setValue("");
				formContext.addFormConstraintForUpdate(theUpdate);
            }
            
			theMA = MetaElementUtil.getMetaAttribute(theME, AddressHolder.ZIP_CODE);
			if (!formContext.hasMember(MetaAttributeGUIHelper.getAttributeIDCreate(theMA))) {
				AttributeUpdate theUpdate =
					AttributeUpdateFactory.createAttributeUpdateForCreate(formContext.getAttributeUpdateContainer(),
						theME, theMA);
				theUpdate.setValue("");
				formContext.addFormConstraintForUpdate(theUpdate);
            }
            
			theMA = MetaElementUtil.getMetaAttribute(theME, COSCompanyContact.SUPPLIER);
			if (!formContext.hasMember(MetaAttributeGUIHelper.getAttributeIDCreate(theMA))) {
				formContext.addFormConstraintForUpdate(
					AttributeUpdateFactory.createAttributeUpdateForCreate(formContext.getAttributeUpdateContainer(),
						theME, theMA));
            }
            
			theMA = MetaElementUtil.getMetaAttribute(theME, COSCompanyContact.CLIENT);
			if (!formContext.hasMember(MetaAttributeGUIHelper.getAttributeIDCreate(theMA))) {
				formContext.addFormConstraintForUpdate(
					AttributeUpdateFactory.createAttributeUpdateForCreate(formContext.getAttributeUpdateContainer(),
						theME, theMA));
            }
        }
        catch (NoSuchAttributeException ex) {
            throw new TopLogicException(this.getClass(), "cos.create.contact.company.formcontext", ex);
        }
        
//        theContext.addMember(HiddenField.newHiddenField(COSContactConstants.ATTRIBUTE_MANDATOR, theMandator));
        
        //theContext.addMember(BooleanField.newInstance(COSCompanyContact.SUPPLIER, Boolean.TRUE, false));
        //theContext.addMember(BooleanField.newInstance(COSCompanyContact.CLIENT,   Boolean.FALSE, false));

		return formContext;
    }
}
