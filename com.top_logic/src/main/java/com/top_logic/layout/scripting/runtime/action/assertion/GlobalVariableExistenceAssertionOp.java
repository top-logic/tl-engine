/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action.assertion;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.assertion.GlobalVariableExistenceAssertion;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.GlobalVariableStore;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.scripting.runtime.action.GlobalVariableActionOp;

/**
 * Assertion, whether a global variable of the scripting framework is set.
 * 
 * @see GlobalVariableStore#has(String) A global variable may also be set to <code>null</code>.
 * 
 * @see GlobalVariableStore
 * @see GlobalVariableExistenceAssertion
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class GlobalVariableExistenceAssertionOp extends GlobalVariableActionOp<GlobalVariableExistenceAssertion> {

	/**
	 * Creates a new {@link GlobalVariableExistenceAssertionOp}.
	 * <p>
	 * Is called by the typed configuration.
	 * </p>
	 */
	@CalledByReflection
	public GlobalVariableExistenceAssertionOp(InstantiationContext context, GlobalVariableExistenceAssertion config) {
		super(context, config);
	}

	@Override
	protected void processGlobalVariable(ActionContext context, String variableName) {
		boolean expected = getConfig().isExisting();
		boolean actual = context.getGlobalVariableStore().has(variableName);
		String message = "The global variable '" + variableName + " was expected to exist: " + expected
			+ "; But actually existed: " + actual;
		ApplicationAssertions.assertEquals(getConfig(), message, expected, actual);
	}

}
