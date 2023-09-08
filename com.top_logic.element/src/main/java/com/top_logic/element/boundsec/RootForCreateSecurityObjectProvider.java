/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec;

import com.top_logic.element.structured.StructuredElement;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;
import com.top_logic.tool.boundsec.securityObjectProvider.SecurityRootObjectProvider;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * The RootForCreateSecurityObjectProvider uses the root of the model of the component as
 * security object for create commands.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class RootForCreateSecurityObjectProvider implements SecurityObjectProvider {

    public static String ALIAS_NAME = "rootForCreate";

	@Override
	public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
        if (aCommandGroup == SimpleBoundCommandGroup.CREATE) {
			if (model instanceof StructuredElement) {
				return (BoundObject) ((StructuredElement) model).getRoot();
			} else {
				return SecurityRootObjectProvider.INSTANCE.getSecurityObject(aChecker, model, aCommandGroup);
            }
        }
		if (model instanceof BoundObject) {
			return (BoundObject) model;
		} else {
			return null;
		}
    }

}
