/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.action.ComponentAction;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Action that modifies the session by injecting a model set event that cannot
 * occur by regular operation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SetModelActionOp extends ComponentActionOp<SetModelActionOp.Config> {

	/**
	 * Configuration of {@link SetModelActionOp}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface Config extends ComponentAction {
		
		@Override
		@ClassDefault(SetModelActionOp.class)
		Class<SetModelActionOp> getImplementationClass();

		ModelName getModel();

		void setModel(ModelName modelRef);
		
	}
	
	public SetModelActionOp(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Object process(ActionContext context, LayoutComponent component, Object argument) {
		ModelName modelRef = config.getModel();
		Object model = context.resolve(modelRef);
		
		boolean ok = component.supportsModel(model);
		ApplicationAssertions.assertTrue(config,
			"Component '" + component.getName() + "' did not accept model '" + model + "'.", ok);
		
		component.setModel(model);
		return argument;
	}

}
