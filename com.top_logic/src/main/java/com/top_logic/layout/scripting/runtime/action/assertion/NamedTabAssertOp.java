/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action.assertion;


import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.scripting.action.NamedTabSwitch;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.scripting.runtime.action.ComponentActionOp;
import com.top_logic.layout.tabbar.TabBarModel;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ApplicationActionOp} that switches a tab to a given card name.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class NamedTabAssertOp extends ComponentActionOp<NamedTabSwitch> {
	
	/** Nothing unexpected */
	public NamedTabAssertOp(InstantiationContext context, NamedTabSwitch config) {
		super(context, config);
	}
	
	@Override
	public Object process(ActionContext context, LayoutComponent component, Object argument) {
		String cardName = config.getCardName();
		
		ApplicationAssertions.assertTrue(config,
			"Switched tab '" + config.getComponentName() + "' is not visible.", component.isVisible());
		
		TabComponent tabComponent = (TabComponent) component;
		TabBarModel tabModel = tabComponent.getTabBarModel();
		Card selectedCard = (Card) tabModel.getAllCards().get(tabModel.getSelectedIndex());
		ApplicationAssertions.assertEquals(config, "Wrong tab selected!", cardName, selectedCard.getName());
		
		return argument;
	}

}
