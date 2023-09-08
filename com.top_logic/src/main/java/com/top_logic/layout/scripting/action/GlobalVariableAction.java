/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.layout.scripting.recorder.ref.value.object.GlobalVariableRef;
import com.top_logic.layout.scripting.runtime.GlobalVariableStore;
import com.top_logic.layout.scripting.runtime.action.GlobalVariableActionOp;

/**
 * The configuration of the {@link GlobalVariableActionOp}.
 * 
 * @see GlobalVariableStore
 * @see GlobalVariableRef
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public interface GlobalVariableAction extends ApplicationAction, NamedConfiguration {

	/**
	 * The name of the global variable of the scripting framework.
	 * <p>
	 * <code>null</code> is not a valid name.
	 * </p>
	 */
	@Override
	String getName();

}