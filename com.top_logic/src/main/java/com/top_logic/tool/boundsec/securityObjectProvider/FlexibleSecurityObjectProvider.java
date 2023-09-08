/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.securityObjectProvider;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;

/**
 * The FlexibleSecurityObjectProvider uses the model of the component as security object, if
 * the component has a master which provides models, otherwise the security root is used as
 * security object.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class FlexibleSecurityObjectProvider implements SecurityObjectProvider {

    public static final FlexibleSecurityObjectProvider INSTANCE = new FlexibleSecurityObjectProvider();

    @Override
	public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
        // If the component has a master which provides models for the component, then the model is
        // used as security object, otherwise the security root is used as security object.
        if (aChecker instanceof LayoutComponent && ((LayoutComponent)aChecker).getMaster() != null) {
			return model instanceof BoundObject ? (BoundObject) model : null;
        }
        else {
			return SecurityRootObjectProvider.INSTANCE.getSecurityObject(aChecker, model, aCommandGroup);
        }
    }

}
