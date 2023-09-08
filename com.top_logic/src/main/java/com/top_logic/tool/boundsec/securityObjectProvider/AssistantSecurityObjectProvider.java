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
import com.top_logic.tool.boundsec.assistent.AssistentComponent;

/**
 * The AssistantSecurityObjectProvider is intended to be used in assistant step components
 * to use the security object of the enclosing assistant component.
 *
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class AssistantSecurityObjectProvider implements SecurityObjectProvider {

    public static String ALIAS_NAME = "assistant";

    @Override
	public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
        AssistentComponent theAssistant = AssistentComponent.getEnclosingAssistentComponent((LayoutComponent)aChecker);
		return theAssistant instanceof BoundChecker
			? ((BoundChecker) theAssistant).getCurrentObject(aCommandGroup, model)
			: null;
    }

}
