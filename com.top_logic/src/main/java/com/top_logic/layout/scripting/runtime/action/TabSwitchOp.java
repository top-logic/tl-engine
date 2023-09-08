/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;


import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.scripting.action.TabSwitch;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ApplicationActionOp} that switches a tab to a given index.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class TabSwitchOp extends ComponentActionOp<TabSwitch> {
	
	public TabSwitchOp(InstantiationContext context, TabSwitch config) {
		super(context, config);
	}
	
	@Override
	public Object process(ActionContext context, LayoutComponent component, Object argument) {
		int index = config.getIndex();
		
		if (component instanceof TabComponent) {
			TabComponent tabComponent = (TabComponent) component;
			tabComponent.setSelectedIndex(index);
		}
		else {
			throw ApplicationAssertions.fail(config, "Not a tab component: '" + component.getName() + "'.");
		}
		
		return argument;
	}

}
