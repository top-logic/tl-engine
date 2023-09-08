/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person.related;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.layout.person.ContactPersonManager;
import com.top_logic.knowledge.gui.layout.person.NewPersonCommandHandler;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.TLContext;

/**
 * Relate new Persons with a {@link PersonContact} or create a new one if needed.
 *
 * This implies that<ul>
 *   <li>the ContactPersonManager is configured. This class will
 *       not complain in this case.</li>
 *   <li>This command is part of a NewRelatedPersonComponent or a compatible replacement</li>
 *   <li>The Page "NewRelatedPerson.jsp"  or a compatible replacement is used </li>
 * </ul>
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class NewRelatedPersonCommandHandler extends NewPersonCommandHandler {

	/**
	 * Creates a new {@link NewRelatedPersonCommandHandler}.
	 */
	public NewRelatedPersonCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
			Map arguments) {
		TLContext context = TLContext.getContext();
		try {
			if (formContainer.hasMember(NewRelatedPersonComponent.FIELD_NAME_RELATED_CONTACT)) {
				SelectField field = (SelectField) formContainer.getField(NewRelatedPersonComponent.FIELD_NAME_RELATED_CONTACT);
				PersonContact contact = (PersonContact) field.getSingleSelection();
				context.set(ContactPersonManager.RELATED_PERSON_CONTACT, contact);
			}
			return super.createObject(component, createContext, formContainer, arguments);
		}
		finally {
			context.reset(ContactPersonManager.RELATED_PERSON_CONTACT);
		}
	}

}
