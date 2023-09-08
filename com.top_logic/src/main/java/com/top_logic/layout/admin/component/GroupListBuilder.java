/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.util.Collection;

import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * The GroupListBuilder builds a list of all groups available in the system
 *
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class GroupListBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link GroupListBuilder} instance.
	 */
	public static final GroupListBuilder INSTANCE = new GroupListBuilder();

	private GroupListBuilder() {
		// Singleton constructor.
	}

    @Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		return Group.getAll();
    }

    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        return true;
    }

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return listElement instanceof Group;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return contextComponent.getModel();
	}

}
