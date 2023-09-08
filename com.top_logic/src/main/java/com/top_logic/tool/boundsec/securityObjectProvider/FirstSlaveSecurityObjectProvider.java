/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.securityObjectProvider;

import java.util.Collection;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;

/**
 * This FirstSlaveSecurityObjectProvider delegates the security question to the first slave.
 *
 * @author <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class FirstSlaveSecurityObjectProvider implements SecurityObjectProvider {

    public static String ALIAS_NAME = "slave";

	@Override
	public BoundObject getSecurityObject(BoundChecker checker, Object model, BoundCommandGroup commandGroup) {
		LayoutComponent layoutComponent = (LayoutComponent) checker;
		Collection<? extends LayoutComponent> slaves = layoutComponent.getSlaves();

		if (slaves.isEmpty()) {
			throw new IllegalArgumentException("The component ('" + layoutComponent.getName() + "') must have a slave component but no slave component could be found.");
		}
		
		LayoutComponent firstSlaveComponent = slaves.iterator().next();
		
		if (firstSlaveComponent == null) {
			throw new IllegalArgumentException("The component ('" + layoutComponent.getName()
				+ "') has no slave component with the name ('" + firstSlaveComponent + "')");
		}
		
		return (firstSlaveComponent instanceof BoundChecker
			? ((BoundChecker) firstSlaveComponent).getCurrentObject(commandGroup, model)
			: null);
	}

}
