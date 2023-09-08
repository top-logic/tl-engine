/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.GlobalVariableAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.GlobalVariableStore;

/**
 * An {@link ApplicationActionOp} about a global variable of the scripting framework.
 * 
 * @see GlobalVariableStore
 * @see GlobalVariableAction
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class GlobalVariableActionOp<T extends GlobalVariableAction> extends AbstractApplicationActionOp<T> {

	/**
	 * Creates a new {@link GlobalVariableActionOp}.
	 * <p>
	 * Is called by the typed configuration.
	 * </p>
	 */
	@CalledByReflection
	public GlobalVariableActionOp(InstantiationContext context, T config) {
		super(context, config);
	}

	@Override
	protected final Object processInternal(ActionContext context, Object argument) {
		processGlobalVariable(context, getConfig().getName());
		return argument;
	}

	/** Where the actual work has to be done. */
	protected abstract void processGlobalVariable(ActionContext context, String variableName);

}
