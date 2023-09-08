/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.security;

import java.util.ArrayList;
import java.util.Collection;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class AttRoleClassMetaElementModelBuilder extends AttSecMetaElementModelBuilder {

	/**
	 * Singleton {@link AttRoleClassMetaElementModelBuilder} instance.
	 */
	@SuppressWarnings("hiding")
	public static final AttRoleClassMetaElementModelBuilder INSTANCE = new AttRoleClassMetaElementModelBuilder();

	private AttRoleClassMetaElementModelBuilder() {
		// Singleton constructor.
	}
    
    @Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		Collection<?> theSuper = super.getModel(businessModel, aComponent);
		ArrayList<Object> theResult = new ArrayList<>(theSuper.size() + 1);
		theResult.add(AttributeClassifierRolesComponent.GLOBAL_DOMAIN);
        theResult.addAll(theSuper);
        return theResult;
    }

	@Override
    public boolean supportsListElement(LayoutComponent aComponent, Object aObject) {
		return super.supportsListElement(aComponent, aObject)
			|| aObject == AttributeClassifierRolesComponent.GLOBAL_DOMAIN;
    }

}
