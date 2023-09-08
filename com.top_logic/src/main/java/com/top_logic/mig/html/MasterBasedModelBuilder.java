/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * A model builder which returns the model of the master component.
 * 
 * This can be used to display a tree, which is bound by a master relation.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class MasterBasedModelBuilder implements ModelBuilder {

	/**
	 * Singleton {@link MasterBasedModelBuilder} instance.
	 */
	public static final MasterBasedModelBuilder INSTANCE = new MasterBasedModelBuilder();

	private MasterBasedModelBuilder() {
		// Singleton constructor.
	}

    /** 
     * @see com.top_logic.mig.html.ModelBuilder#getModel(Object, com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
        LayoutComponent theMaster = aComponent.getMaster();

        return (theMaster == null) ? null : theMaster.getModel();
    }

    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        return (true);
    }
}
