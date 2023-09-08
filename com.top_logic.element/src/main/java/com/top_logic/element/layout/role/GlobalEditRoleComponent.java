/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.role;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.tool.boundsec.gui.EditRoleComponent;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Edit roles without binding object.
 * 
 * @author    <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
public class GlobalEditRoleComponent extends EditRoleComponent {

    /**
     * Construct a GlobalEditRoleComponent from XML-Attributes
     * @param atts      the XML attributes
     */
    public GlobalEditRoleComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }

    /**
     * We deal only with BoundedRoles that are unbound
     */
    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return anObject instanceof BoundedRole && ((BoundedRole) anObject).isGlobal();
    }

}
