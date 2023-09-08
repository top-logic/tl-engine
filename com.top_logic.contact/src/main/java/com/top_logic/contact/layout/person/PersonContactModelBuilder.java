/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person;

import java.util.Collection;

import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * @author     <a href="mailto:dna@top-logic.com">dna</a>
 */
public class PersonContactModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link PersonContactModelBuilder} instance.
	 */
	public static final PersonContactModelBuilder INSTANCE = new PersonContactModelBuilder();

	protected PersonContactModelBuilder() {
		// Singleton constructor.
	}

	/**
	 * We get all contacts of type PersonContact here.
	 * 
	 * @return The list of all known contacts of type {@link ContactFactory#PERSON_TYPE}, never
	 *         <code>null</code>.
	 */
	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent component) {
		   return ContactFactory.getInstance().getAllContacts(ContactFactory.PERSON_TYPE);
	}

    /**
	 * Event handling method.
	 * 
	 * @return <code>true</code> only if the object is of type {@link PersonContact}.
	 */
	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		  return (model == null);
	}

	
	@Override
	public Object retrieveModelFromListElement(LayoutComponent component,
			Object anObject) {
		return null;
	}

	/** 
     * this table only supports PersonContact's!
     * 
     * @return <code>true</code> only if the given object is a PersonContact
     */
	@Override
	public boolean supportsListElement(LayoutComponent component, Object anObject) {
		  return (anObject instanceof PersonContact);
	}

}
