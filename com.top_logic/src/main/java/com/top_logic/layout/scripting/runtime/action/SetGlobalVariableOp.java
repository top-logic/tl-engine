/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.SetGlobalVariableAction;
import com.top_logic.layout.scripting.recorder.ref.value.object.GlobalVariableRef;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.GlobalVariableStore;

/**
 * Sets the value of a global variable of the scripting framework.
 * 
 * @see GlobalVariableStore
 * @see SetGlobalVariableAction
 * @see GlobalVariableRef
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class SetGlobalVariableOp extends GlobalVariableActionOp<SetGlobalVariableAction> {

	/**
	 * Creates a new {@link SetGlobalVariableOp}.
	 * <p>
	 * Is called by the typed configuration.
	 * </p>
	 */
	@CalledByReflection
	public SetGlobalVariableOp(InstantiationContext context, SetGlobalVariableAction config) {
		super(context, config);
	}

	@Override
	protected void processGlobalVariable(ActionContext context, String variableName) {
		Object newVariableValue = context.resolve(getConfig().getValue());
		context.getGlobalVariableStore().set(variableName, newVariableValue);
	}

}
