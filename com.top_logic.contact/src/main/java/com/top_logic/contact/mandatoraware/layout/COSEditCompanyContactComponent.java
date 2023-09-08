/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.layout;

import java.text.Format;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.layout.company.EditCompanyContactComponent;
import com.top_logic.contact.mandatoraware.COSCompanyContact;
import com.top_logic.contact.mandatoraware.COSContactConstants;
import com.top_logic.contact.mandatoraware.layout.COSEditPersonContactComponent.ContactSwitchEditCommandHandler;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.element.structured.wrap.MandatorAware;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.constraints.DigitsOnlyConstraint;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.format.FillParseAndCleanFormat;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;

/**
 * This class is used by COS because it must not be possible to disable
 * either the client or supplier characteristic of a company contact as long
 * as it is still used as client or supplier by any contract.
 *
 * Further it must not be possible to delete a contact as long as it is bound
 * to any contract.
 *
 * @author    <a href=mailto:tri@top-logic.com>Thoma Richtre</a>
 *
 */
public class COSEditCompanyContactComponent extends EditCompanyContactComponent {

	/**
	 * Configuration for {@link COSEditCompanyContactComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends EditCompanyContactComponent.Config {

		@Override
		@StringDefault(ContactSwitchEditCommandHandler.COMMAND_ID)
		String getEditCommand();

	}

	public COSEditCompanyContactComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
		super(context, someAttrs);
	}

    /**
     * Create the FomContext fo the CompanyContact.
     *
     * Overriden for COS to disable client/supplierConstraint when Company is
     * used as Client or supplier somewhere.
     * Also handles SAP numbers in FKey2 if needed.
     */
    @Override
	public FormContext createFormContext() {
    	FormContext fc = super.createFormContext();
    	try{
    		//disable client / supplier checkboxes if contact is bound to any contract
    		//in these roles
    		CompanyContact theCompanyContact = (CompanyContact)this.getModel();
    		if (theCompanyContact != null) {
	    		TLClass   me                 = getMetaElement(theCompanyContact);
				TLStructuredTypePart clientAttr = MetaElementUtil.getMetaAttribute(me, COSCompanyContact.CLIENT);
				TLStructuredTypePart supplierAttr = MetaElementUtil.getMetaAttribute(me, COSCompanyContact.SUPPLIER);
				TLStructuredTypePart fkeyAttr = MetaElementUtil.getMetaAttribute(me, COSCompanyContact.FKEY2_ATTRIBUTE);
				String clientIdentifier = MetaAttributeGUIHelper.getAttributeID(clientAttr, theCompanyContact);
				String supplierIdentifier = MetaAttributeGUIHelper.getAttributeID(supplierAttr, theCompanyContact);
				String fkeyIdentifier = MetaAttributeGUIHelper.getAttributeID(fkeyAttr, theCompanyContact);

	    		setupClientSupplierConstraints(fc, theCompanyContact, clientIdentifier, supplierIdentifier, fkeyIdentifier);
    		}
    	} catch(Exception e) {
    		Logger.error("Failed to createFormContext()", e, this);
    	}
    	return fc;
    }

    /**
     * Do not edit Mandator
     *
     * @see com.top_logic.element.meta.form.component.EditAttributedComponent#getExcludeList()
     */
    @Override
	public List<String> getExcludeList() {
    	return Collections.singletonList(COSContactConstants.ATTRIBUTE_MANDATOR);
    }

    /**
     * Disable the Checkboxes for "Supplier" and "Client" when needed.
     *
     * The constraints will be disabled when the Contact is referenced by a Contract
     * as Supplier or Client.
     *
     * Also handles SAP number constraint if needed
     */
    protected void setupClientSupplierConstraints(FormContext fc, CompanyContact theCompanyContact, String clientIdentifier, String supplierIdentifier,
    				String fkeyIdentifier) {
        FormField clientConstraint    = fc.getField(clientIdentifier);
        FormField supplierConstraint  = fc.getField(supplierIdentifier);

        boolean isSAP = !StringServices.isEmpty((String) ((MandatorAware) theCompanyContact).getMandator().getValue(Mandator.SAP_SUPPLIERS));
        if (isSAP) {
        	FormField theOrig = fc.getField(fkeyIdentifier);
        	fc.removeMember(fkeyIdentifier);
        	Format    fkeyFormat = new FillParseAndCleanFormat(10, StringServices.START_POSITION_HEAD, '0');
        	FormField fkeyField  = FormFactory.newComplexField(fkeyIdentifier, fkeyFormat, true, false, false, new StringLengthConstraint(0, 10));
        	fkeyField.addConstraint(DigitsOnlyConstraint.INSTANCE);
        	fkeyField.setValue(theOrig.getValue());
        	fkeyField.setLabel(theOrig.getLabel());
        	fkeyField.setExampleValue("8979879800");
        	fc.addMember(fkeyField);
        }

        boolean        disableCient        = false;
        boolean        disableSupplier     = false;

       	clientConstraint  .setDisabled(disableCient);
       	supplierConstraint.setDisabled(disableSupplier);
    }
}
