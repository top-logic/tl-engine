/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.component;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.structured.AdminElementComponent;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * {@link AdminElementComponent} to display a help page within an external window.
 */
public class ExternalWindowDocViewerComponent extends AdminElementComponent {

	/**
	 * Creates a {@link ExternalWindowDocViewerComponent}.
	 */
	public ExternalWindowDocViewerComponent(InstantiationContext context, Config someAttrs)
			throws ConfigurationException {
		super(context, someAttrs);
	}

	@Override
	public boolean allow(Person aPerson, BoundObject aModel, BoundCommandGroup aCmdGroup) {
		if (!SimpleBoundCommandGroup.isAllowedCommandGroup(aPerson, aCmdGroup)) {
			return false;
		}
		boolean isSystem = aCmdGroup.isSystemGroup();
		if (isSystem) {
			return true;
		}
		/* It is currently not possible to configure security for external windows. */
//        return AccessManager.getInstance().hasRole(aPerson, aModel, getRolesForCommandGroup(aCmdGroup));
		return true;
	}

}
