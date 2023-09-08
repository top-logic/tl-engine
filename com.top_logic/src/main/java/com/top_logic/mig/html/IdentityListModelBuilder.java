/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.Collection;
import java.util.List;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ListModelBuilder} expecting a {@link List} as component model.
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class IdentityListModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link IdentityListModelBuilder} instance.
	 */
	public static final IdentityListModelBuilder INSTANCE = new IdentityListModelBuilder();

	private IdentityListModelBuilder() {
		// Singleton constructor.
	}

    /**
     * @see com.top_logic.mig.html.ListModelBuilder#retrieveModelFromListElement(com.top_logic.mig.html.layout.LayoutComponent, java.lang.Object)
     */
    @Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object aAnObject) {
        return null;
    }

    /**
     * @see com.top_logic.mig.html.ListModelBuilder#supportsListElement(com.top_logic.mig.html.layout.LayoutComponent, java.lang.Object)
     */
    @Override
	public boolean supportsListElement(LayoutComponent aComponent, Object anObject) {
		Object model = aComponent.getModel();
		if (model instanceof List<?>) {
			List<?> current = (List<?>) model;
			return current.contains(anObject);
		} else {
			return false;
		}
    }

    /**
     * @see com.top_logic.mig.html.ModelBuilder#getModel(Object, com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		return (List<?>) businessModel;
    }

    /**
     * @see com.top_logic.mig.html.ModelBuilder#supportsModel(java.lang.Object, com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        return aModel == null || aModel instanceof List;
    }

}

