/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.person;

import java.util.Collection;
import java.util.List;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class PersonTableModelBuilder implements ListModelBuilder{

	/**
	 * Singleton {@link PersonTableModelBuilder} instance.
	 */
	public static final PersonTableModelBuilder INSTANCE = new PersonTableModelBuilder();

	private PersonTableModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent component, Object anObject) {
	    return component.getModel();
    }

    /**
     * This is a Table of Person Wrappers.
     */
	@Override
	public boolean supportsListElement(LayoutComponent component, Object anObject) {
		 return (anObject instanceof Person);
    }

    /**
	 * Request all known persons from the knowledgebase and place them in the
	 * table.
	 */
	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent component) {
		return PersonManager.getManager().getAllAlivePersons();
	} 
    
	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		return (model instanceof List);
    }
}
