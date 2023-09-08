/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.ActionUtil;
import com.top_logic.layout.scripting.action.ComponentAction;
import com.top_logic.layout.scripting.recorder.ref.ui.LayoutComponentResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ApplicationActionOp} that does the processing in the context of a given
 * component.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ComponentActionOp<S extends ComponentAction> extends AbstractApplicationActionOp<S>
		implements LayoutComponentResolver {

	public ComponentActionOp(InstantiationContext context, S config) {
		super(context, config);
	}

	@Override
	public final Object processInternal(ActionContext context, Object argument) {
		LayoutComponent component =
			ActionUtil.getComponentByName(getConfig().getComponentName(), context.getMainLayout(), getConfig());
		checkVisible(context, getConfig(), component);
		return process(context, component, argument);
	}

	/**
	 * Executes the action in the context of the given component.
	 * 
	 * @see #process(ActionContext, Object)
	 */
	protected abstract Object process(ActionContext context, LayoutComponent component, Object argument);
	
}
