/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.role;

import java.util.Collection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModule;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * ModelBuilder for scoped roles table.
 *
 * @author     <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public class ScopedRolesTableModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link ScopedRolesTableModelBuilder} instance.
	 */
	public static final ScopedRolesTableModelBuilder INSTANCE = new ScopedRolesTableModelBuilder();

	private ScopedRolesTableModelBuilder() {
		// Singleton constructor.
	}

    @Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		return CollectionUtil.modifiableList(BoundHelper.getInstance().getPossibleRoles((TLModule) businessModel));
    }

    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel instanceof TLModule;
    }

    @Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object aObject) {
        return aObject instanceof BoundedRole ? ((BoundedRole)aObject).getScope() : null;
    }

    @Override
	public boolean supportsListElement(LayoutComponent aComponent, Object aObject) {
        return aObject instanceof BoundedRole;
    }

}
