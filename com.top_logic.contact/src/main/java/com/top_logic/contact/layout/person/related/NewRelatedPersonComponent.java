/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person.related;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.col.LazyListUnmodifyable;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.layout.person.ContactPersonManager;
import com.top_logic.contact.layout.person.related.EditRelatedPersonComponent.EditRelatedPersonConfig;
import com.top_logic.knowledge.gui.layout.person.NewPersonComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;


/**
 * {@link NewPersonComponent} with additional contact field.
 *
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class NewRelatedPersonComponent extends NewPersonComponent {

    /** The related contact to the person. */
	public static final String FIELD_NAME_RELATED_CONTACT = ContactPersonManager.RELATED_PERSON_CONTACT.getName();
    
    /** If <code>true</code>, super user can change the person contact of an account. */
    private final boolean allowManualContactAssigning;


    /**
     * Creates a new {@link NewRelatedPersonComponent}.
     */
    public NewRelatedPersonComponent(InstantiationContext context, Config aAtts) throws ConfigurationException {
        super(context, aAtts);
		ApplicationConfig instance = ApplicationConfig.getInstance();
		EditRelatedPersonConfig config = instance.getConfig(EditRelatedPersonConfig.class);
		allowManualContactAssigning = config.getAllowManualContactAssigning();
    }


    @Override
	public FormContext createFormContext() {
        FormContext formContext = super.createFormContext();

        if (allowManualContactAssigning) {
            // Add related person contact field
			List<PersonContact> allContacts = new LazyListUnmodifyable<>() {
                @Override
				protected List<PersonContact> initInstance() {
					List<PersonContact> theResult = new ArrayList<>();
                    
					Iterator<?> it = ContactFactory.getInstance().getAllContacts(ContactFactory.PERSON_TYPE).iterator();
                	while(it.hasNext()){
                		PersonContact pc = (PersonContact)it.next();
                		if(pc.getPerson()==null){
                			theResult.add(pc);
                		}
                	}
                	return theResult;
                }};

            SelectField relatedContactField = FormFactory.newSelectField(FIELD_NAME_RELATED_CONTACT, allContacts, false, false);
            relatedContactField.setOptionLabelProvider(RelatedPersonContactLabelProvider.INSTANCE);
            formContext.addMember(relatedContactField);
        }

        return formContext;
    }
    
}
