/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.scripting.action.SelectObject;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ApplicationActionOp} that checks the selection in a given component.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CheckSelectionOp extends ComponentActionOp<SelectObject> {

	private final Filter<Object> matcher;

	public CheckSelectionOp(InstantiationContext context, SelectObject config) {
		super(context, config);
		
		this.matcher = context.getInstance(config.getMatcherConfig());
	}
	
	@Override
	protected Object process(ActionContext context, LayoutComponent component, Object argument) {
		Selectable selectable = (Selectable) component;
		Object selected = selectable.getSelected();
		boolean matches = matcher.accept(selected);
		
		ApplicationAssertions.assertTrue(config,
			"Selection does not match in component '" + component.getName() + "'.", matches);
		return argument;
	}
	
}
