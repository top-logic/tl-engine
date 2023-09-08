/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.contact.business.AbstractContact;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.layout.AbstractEditContactComponent;
import com.top_logic.contact.layout.PersonContactLabelProvider;
import com.top_logic.contact.layout.person.EditPersonContactComponent;
import com.top_logic.contact.mandatoraware.COSContactConstants;
import com.top_logic.contact.mandatoraware.COSPersonContact;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.form.component.SwitchEditCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Disables deletion for contacts still bound to a contract.
 *
 * Sets up a Security Cache.
 *
 * TODO KBU/MGA 1. There are too many events as it seems
 *              2. Initial goto (i.e. without opening company contact view) does not work
 *                 It works for person contacts (this is because persons are the first tabber (tested!)...)
 *
 * @author    <a href=mailto:tri@top-logic.com>Thomas Richter</a>
 */
public class COSEditPersonContactComponent extends EditPersonContactComponent {

	public static final String PARAM_LEAD_BUYER = "leadBuyer";

	/**
	 * Configuration for {@link COSEditPersonContactComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends EditPersonContactComponent.Config {

		@Override
		@StringDefault(ContactSwitchEditCommandHandler.COMMAND_ID)
		String getEditCommand();

	}

    /**
	 * Generate a COSEditPersonContactComponent from XML.
	 */
	public COSEditPersonContactComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
		super(context, someAttrs);
	}

    /**
     * @see com.top_logic.contact.layout.person.EditPersonContactComponent#createFormContext()
     */
    @Override
	public synchronized FormContext createFormContext() {
        FormContext theContext = super.createFormContext();
        try{
            PersonContact theContact = (PersonContact) this.getModel();

            if (theContact != null && theContact.tValid()) {
                List        theList           = this.getLeadBuyer(theContact);
                SelectField theLeadBuyerField = FormFactory.newSelectField(PARAM_LEAD_BUYER, theList, true, true);

                theLeadBuyerField.setOptionLabelProvider(PersonContactLabelProvider.INSTANCE);
                theLeadBuyerField.setValue(theList);

                theContext.addMember(theLeadBuyerField);
            }
        }
        catch(Exception e) {
            Logger.error("Unable to disable fields for attributes which values were retrieved from the respective person",e, this);
        }

        return theContext;
    }

    /**
     * Do not edit Mandator
     *
     * @see com.top_logic.element.meta.form.component.EditAttributedComponent#getExcludeList()
     */
    @Override
	public List<String> getExcludeList() {
    	return Collections.emptyList();
    }

    /**
     * Return the list of company contacts the given person contact is lead buyer for.
     *
     * @param    aModel    The person contact to get the company contacts for, may be <code>null</code>.
     * @return   The list of contacts the given one is lead buyer for, never <code>null</code>.
     */
    public List getLeadBuyer(Object aModel) {
        List theResult = new ArrayList();

        if (aModel instanceof COSPersonContact) {
            try {
                theResult = ((COSPersonContact) aModel).getCompaniesOfLeadBuyer();
                Collections.sort(theResult, LabelComparator.newCachingInstance(10));
            }
            catch (Exception e) {
                Logger.error("Unable to retrieve personcontacts for companycontact",e,this);
            }
        }

        return theResult;
    }

	public static class ContactSwitchEditCommandHandler extends SwitchEditCommandHandler {

        public static final String COMMAND_ID = "ContactSwitchEditCommand";

		public ContactSwitchEditCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		@Deprecated
    	public ExecutabilityRule createExecutabilityRule() {
			return CombinedExecutabilityRule.combine(super.createExecutabilityRule(), EditDefaultExecutable.INSTANCE);
    	}
	}

	private static class EditDefaultExecutable implements ExecutabilityRule {

		public static final ExecutabilityRule INSTANCE = new EditDefaultExecutable();

		@Override
		public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
			AbstractEditContactComponent theComp    = (AbstractEditContactComponent) component;
			AbstractContact theContact = (AbstractContact) model;
			if (theContact != null && theContact.getName().startsWith(COSContactConstants.DEFAULT_CONTACT_NAME)) {
				return ExecutableState.createDisabledState(I18NConstants.ERROR_SYSTEM_CONTACT_NOT_EDITABLE);
			}
			return ExecutableState.EXECUTABLE;
		}
	}

}
