/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.AbstractCreateComponent;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;


/**
 * CreateComponent to set up new Person Contacts.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class CreatePersonContactComponent extends AbstractCreateComponent {

	/**
	 * Configuration for the {@link CreatePersonContactComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCreateComponent.Config {

		@StringDefault(PersonContactCreateHandler.COMMAND_ID)
		@Override
		String getCreateHandler();

	}

    public CreatePersonContactComponent(InstantiationContext context, Config someAttrs)
            throws ConfigurationException {
        super(context, someAttrs);
    }

    @Override
	protected boolean supportsInternalModel(Object anObject) {
         return anObject == null;
    }
    
    /**
     * @see com.top_logic.layout.form.component.FormComponent#createFormContext()
     */
    @Override
	public FormContext createFormContext() {
        
        FormContext theContext = new FormContext("default", this.getResPrefix(), 
              new FormField[] {
   		 		FormFactory.newStringField(PersonContact.NAME_ATTRIBUTE, "", true,  false, new StringLengthConstraint(1, 30)),
   		 		FormFactory.newStringField(PersonContact.ATT_FIRSTNAME,  "", true,  false, new StringLengthConstraint(1, 30))
              });
        return theContext;
    }
}
