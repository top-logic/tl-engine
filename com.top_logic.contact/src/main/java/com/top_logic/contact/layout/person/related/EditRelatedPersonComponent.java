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
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.knowledge.gui.layout.person.EditPersonComponent;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.model.FormContext;

/**
 * The EditRelatedPersonComponent contains additional a related {@link PersonContact}
 * information for an {@link Person}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class EditRelatedPersonComponent extends EditPersonComponent {

	/**
	 * {@link ConfigurationItem} for {@link EditRelatedPersonComponent}.
	 */
	public interface EditRelatedPersonConfig extends ConfigurationItem {
		/**
		 * If <code>true</code>, super user can change the person contact of an account
		 */
		@BooleanDefault(false)
		boolean getAllowManualContactAssigning();
	}

    /** If <code>true</code>, super user can change the person contact of an account. */
    private final boolean allowManualContactAssigning;

	/**
	 * Creates a new {@link EditRelatedPersonComponent}.
	 */
    public EditRelatedPersonComponent(InstantiationContext context, Config aSomeAttrs) throws ConfigurationException {
        super(context, aSomeAttrs);
		ApplicationConfig instance = ApplicationConfig.getInstance();
		EditRelatedPersonConfig config = instance.getConfig(EditRelatedPersonConfig.class);
		allowManualContactAssigning = config.getAllowManualContactAssigning();
    }


    @Override
	public FormContext createFormContext() {
        FormContext formContext = super.createFormContext();
        
        // Add related person contact field
        List allContacts = new LazyListUnmodifyable() {
            @Override
			protected List initInstance() {
                List theResult = new ArrayList();
                
            	Iterator it =  ContactFactory.getInstance().getAllContacts(ContactFactory.PERSON_TYPE).iterator();
            	while(it.hasNext()){
            		PersonContact pc = (PersonContact)it.next();
            		if(pc.getPerson()==null){
            			theResult.add(pc);
            		}
            	}
            	return theResult;
            }};

        return formContext;
    }
    
}
