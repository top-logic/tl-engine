/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link AbstractApplicationActionOp} wrapper for a {@link SimpleActionOp}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SimpleActionWrapperOp extends AbstractApplicationActionOp<SimpleActionWrapperOp.Config> {
	
	/**
	 * Configuration interface for {@link SimpleActionWrapperOp}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static interface Config extends ApplicationAction {
		Class<? extends SimpleActionOp> getActionClass();
		void setActionClass(Class<? extends SimpleActionOp> value);
	}
	
	private final SimpleActionOp action;
	
	public SimpleActionWrapperOp(InstantiationContext context, SimpleActionWrapperOp.Config config) {
		super(context, config);
		Class<? extends SimpleActionOp> actionClass = config.getActionClass();
		try {
			action = actionClass.newInstance();
		} catch (IllegalAccessException | InstantiationException ex) {
			throw ApplicationAssertions.fail(config, "Cannot instantiate action implementation '" + actionClass + "'.",
				ex);
		}
	}

	@Override
	public Object processInternal(ActionContext context, Object argument) {
		try {
			action.performTest(context);
		} catch (Exception ex) {
			ApplicationAssertions.fail(config, "Processing of action failed.", ex);
		}
		return null;
	}

}