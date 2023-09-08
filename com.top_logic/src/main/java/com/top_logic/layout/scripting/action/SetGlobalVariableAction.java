/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.value.object.GlobalVariableRef;
import com.top_logic.layout.scripting.runtime.GlobalVariableStore;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.SetGlobalVariableOp;

/**
 * The configuration for the {@link SetGlobalVariableOp}.
 * 
 * @see GlobalVariableStore
 * @see GlobalVariableRef
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public interface SetGlobalVariableAction extends GlobalVariableAction {

	@Override
	@ClassDefault(SetGlobalVariableOp.class)
	Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

	/**
	 * The new value of the global variable of the scripting framework.
	 * <p>
	 * <code>null</code> is a valid value.
	 * </p>
	 */
	ModelName getValue();

	/** @see #getValue() */
	void setValue(ModelName newValue);

}
