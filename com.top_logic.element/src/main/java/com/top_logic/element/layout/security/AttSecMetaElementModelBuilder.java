/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.security;

import java.util.Collection;

import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.tool.boundsec.manager.AccessManager;

/**
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class AttSecMetaElementModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link AttSecMetaElementModelBuilder} instance.
	 */
	public static final AttSecMetaElementModelBuilder INSTANCE = new AttSecMetaElementModelBuilder();

	protected AttSecMetaElementModelBuilder() {
		// Singleton constructor.
	}

    @Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
        ElementAccessManager theAM = (ElementAccessManager) AccessManager.getInstance();
        return theAM.getSupportedMetaElements();
    }

    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        return true;
    }

    @Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object aObject) {
        return null;
    }

    @Override
	public boolean supportsListElement(LayoutComponent aComponent, Object aObject) {
        return aObject instanceof TLClass;
    }

}
