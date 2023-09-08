/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui;

import java.util.Collection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * ModelBuilder for global roles table.
 *
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class RolesTableModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link RolesTableModelBuilder} instance.
	 */
	public static final RolesTableModelBuilder INSTANCE = new RolesTableModelBuilder();

	private RolesTableModelBuilder() {
		// Singleton constructor.
	}

    @Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		return CollectionUtil.modifiableList(BoundedRole.getAll());
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
	public boolean supportsListElement(LayoutComponent aComponent, Object aObject) {
        return aObject instanceof BoundedRole;
    }

}
