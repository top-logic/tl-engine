/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.action.ComponentAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ApplicationActionOp} that checks the visibility of a given component.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CheckVisibilityOp extends ComponentActionOp<CheckVisibilityOp.Config> {
	
	public interface Config extends ComponentAction {

		@Override
		@ClassDefault(CheckVisibilityOp.class)
		Class<CheckVisibilityOp> getImplementationClass();

		boolean isVisible();
		void setVisible(boolean visible);
	}

	public CheckVisibilityOp(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void checkVisible(ActionContext context, ConfigurationItem contextConfig, LayoutComponent component) {
		// No check here. It is made explicit in process.
	}

	@Override
	protected Object process(ActionContext context, LayoutComponent component, Object argument) {
		boolean visibility = component.isVisible();
		final boolean demandedVisibility = config.isVisible();
		
		if (demandedVisibility) {
			ApplicationAssertions.assertTrue(config, "Component '" + component.getName() + "' is not visible.",
				visibility);
		} else {
			ApplicationAssertions
				.assertFalse(config, "Component '" + component.getName() + "' is visible.", visibility);
		}
		
		return argument;
	}

}
