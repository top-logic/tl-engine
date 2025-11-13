/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.role;

import java.util.Collection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.mig.html.ElementUpdate;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * ModelBuilder for global roles table.
 *
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class GlobalRolesTableModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link GlobalRolesTableModelBuilder} instance.
	 */
	public static final GlobalRolesTableModelBuilder INSTANCE = new GlobalRolesTableModelBuilder();

	private GlobalRolesTableModelBuilder() {
		// Singleton constructor.
	}

    @Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		return CollectionUtil.modifiableList(BoundedRole.getAllGlobalRoles());
    }

    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        return aModel == null;
    }

    @Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object aObject) {
        return null;
    }

	@Override
	public ElementUpdate supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return ElementUpdate.fromDecision(shouldDisplay(contextComponent, listElement));
	}

	private boolean shouldDisplay(LayoutComponent contextComponent, Object listElement) {
		return listElement instanceof BoundedRole role && role.isGlobal();
    }

}
