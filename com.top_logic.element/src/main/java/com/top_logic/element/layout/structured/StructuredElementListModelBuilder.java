/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.structured;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.element.structured.StructuredElement;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Simple list model builder returning the direct children of a {@link StructuredElement} as list.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class StructuredElementListModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link StructuredElementListModelBuilder} instance.
	 */
	public static final StructuredElementListModelBuilder INSTANCE = new StructuredElementListModelBuilder();

	private StructuredElementListModelBuilder() {
		// Singleton constructor.
	}

    @Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		if (businessModel == null) {
    		return Collections.emptyList();
		}
		return ((StructuredElement) businessModel).getChildren();
    }

    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        return aModel instanceof StructuredElement;
    }

    @Override
	public boolean supportsListElement(LayoutComponent aComponent, Object element) {
		if (!(element instanceof StructuredElement)) {
			return false;
		}
		return ((StructuredElement) element).getParent() == aComponent.getModel();
    }

    @Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anObject) {
        return ((StructuredElement) anObject).getParent();
    }

}

