/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action.assertion;

import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.action.GlobalVariableAction;
import com.top_logic.layout.scripting.recorder.ref.value.object.GlobalVariableRef;
import com.top_logic.layout.scripting.runtime.GlobalVariableStore;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.assertion.GlobalVariableExistenceAssertionOp;

/**
 * The configuration for the {@link GlobalVariableExistenceAssertionOp}.
 * 
 * @see GlobalVariableStore
 * @see GlobalVariableRef
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public interface GlobalVariableExistenceAssertion extends GlobalVariableAction {

	@Override
	@ClassDefault(GlobalVariableExistenceAssertionOp.class)
	Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

	/**
	 * Has this global variable to exist?
	 * <p>
	 * <code>false</code> means, it must not exist.
	 * </p>
	 */
	boolean isExisting();

	/** @see #isExisting() */
	void setExisting(boolean existing);

}
