/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ref.value.object.GlobalVariableRef;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.GlobalVariableStore;

/**
 * Clears the {@link GlobalVariableStore}.
 * 
 * @see GlobalVariableRef
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class ClearGlobalVariablesOp extends AbstractApplicationActionOp<ApplicationAction> {

	/**
	 * Creates a {@link ClearGlobalVariablesOp} from a configuration.
	 * 
	 * @param context
	 *        The {@link InstantiationContext} for instantiation of dependent configured objects.
	 * @param config
	 *        The configuration of this {@link AbstractApplicationActionOp}.
	 */
	public ClearGlobalVariablesOp(InstantiationContext context, ApplicationAction config) {
		super(context, config);
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) {
		context.getGlobalVariableStore().clear();
		return argument;
	}

}
