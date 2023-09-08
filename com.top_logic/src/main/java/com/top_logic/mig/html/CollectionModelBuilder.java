/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Simple model builder, which will take a collection as supported model.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CollectionModelBuilder implements ModelBuilder {

	/**
	 * Singleton {@link CollectionModelBuilder} instance.
	 */
	public static final CollectionModelBuilder INSTANCE = new CollectionModelBuilder();

	private CollectionModelBuilder() {
		// Singleton constructor.
	}

    /**
     * @see com.top_logic.mig.html.ModelBuilder#getModel(Object, com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		return (businessModel instanceof Collection) ? businessModel : Collections.EMPTY_LIST;
    }

    /**
     * @see com.top_logic.mig.html.ModelBuilder#supportsModel(java.lang.Object, com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        return aModel instanceof Collection;
    }
}

