/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.util.Collection;

import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class PersonModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link PersonModelBuilder} instance.
	 */
	public static final PersonModelBuilder INSTANCE = new PersonModelBuilder();

	protected PersonModelBuilder() {
		// Singleton constructor.
	}

    /**
     * @see com.top_logic.mig.html.ListModelBuilder#retrieveModelFromListElement(com.top_logic.mig.html.layout.LayoutComponent, java.lang.Object)
     */
    @Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anObject) {
		return null;
    }

    /**
     * @see com.top_logic.mig.html.ListModelBuilder#supportsListElement(com.top_logic.mig.html.layout.LayoutComponent, java.lang.Object)
     */
    @Override
	public boolean supportsListElement(LayoutComponent aComponent, Object anObject) {
        return anObject instanceof Person;
    }

    /**
     * @see com.top_logic.mig.html.ModelBuilder#getModel(Object, com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
        return PersonManager.getManager().getAllAlivePersons();
    }

    /**
     * @see com.top_logic.mig.html.ModelBuilder#supportsModel(java.lang.Object, com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        return true;
    }
}

