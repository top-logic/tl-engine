/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.role;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModule;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.gui.EditRoleComponent;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Edit roles that have a binding object
 * 
 * @author    <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
public class ScopedEditRoleComponent extends EditRoleComponent {

    /**
     * Construct a ScopedEditRoleComponent from XML-Attributes
     * @param atts      the XML attributes
     */
    public ScopedEditRoleComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }

    /**
     * We deal only with BoundedRoles that are bound to a StructuredElement
     */
    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return anObject instanceof BoundedRole && !((BoundedRole) anObject).isGlobal();
    }

    @Override
	protected TLModule getRoleScope() {
        Object theObject = ((LayoutComponent) this.getSelectableMaster()).getModel();
        if (theObject instanceof BoundObject && theObject instanceof Wrapper) {
			return (TLModule) theObject;
        }
        return super.getRoleScope();
    }

}
