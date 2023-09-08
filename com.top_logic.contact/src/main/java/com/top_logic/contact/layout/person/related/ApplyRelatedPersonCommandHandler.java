/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person.related;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.layout.person.ContactPersonManager;
import com.top_logic.knowledge.gui.layout.person.ApplyPersonCommandHandler;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;


/**
 * The {@link ApplyPersonCommandHandler} for the {@link EditRelatedPersonComponent}.
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ApplyRelatedPersonCommandHandler extends ApplyPersonCommandHandler {

    public ApplyRelatedPersonCommandHandler(InstantiationContext context, Config config) {
        super(context, config);
    }
	
    @Override
	protected void updateNonUserFields(Person aPerson, FormContext aContext) {
        super.updateNonUserFields(aPerson, aContext);
		storeStartPageAutomatism(aContext);
		if (aContext.hasMember(EditRelatedPersonComponent.FIELD_NAME_RELATED_CONTACT)) {
			PersonContact personContact =
				(PersonContact) ((SelectField) aContext.getField(EditRelatedPersonComponent.FIELD_NAME_RELATED_CONTACT))
					.getSingleSelection();
			if (personContact != null) {
				PersonManager personManager = PersonManager.getManager();
				if (personManager instanceof ContactPersonManager) {
					((ContactPersonManager) personManager).connectContactToPerson(aPerson, personContact);
				}
			}
        }

		if (aContext.hasMember(EditCurrentPersonComponent.PERSONAL_CONFIGURATION_AUTO_TRANSLATE)) {
			FormField autoTranslate =
				aContext.getField(EditCurrentPersonComponent.PERSONAL_CONFIGURATION_AUTO_TRANSLATE);
			getPersonalConfig().setAutoTranslate((boolean) autoTranslate.getValue());
		}
    }

	/**
	 * Apply the value of the {@link EditCurrentPersonComponent#FIELD_START_PAGE_AUTOMATISM} field.
	 */
	protected void storeStartPageAutomatism(FormContext context) {
		if (!hasFieldStartPageAutomatism(context)) {
			/* This field does not exist in the administrator tab, only in the settings dialog.
			 * Additionally, this option can be hidden from the user in which case the field is not
			 * built. */
			return;
		}
		BooleanField field = getFieldStartPageAutomatism(context);
		if (field.isChanged()) {
			getPersonalConfig().setStartPageAutomatism(field.getAsBoolean());
		}
	}

	private boolean hasFieldStartPageAutomatism(FormContext context) {
		return context.hasMember(EditCurrentPersonComponent.FIELD_START_PAGE_AUTOMATISM);
	}

	private BooleanField getFieldStartPageAutomatism(FormContext context) {
		return (BooleanField) context.getField(EditCurrentPersonComponent.FIELD_START_PAGE_AUTOMATISM);
	}

	private static PersonalConfiguration getPersonalConfig() {
		return PersonalConfiguration.getPersonalConfiguration();
	}

}
