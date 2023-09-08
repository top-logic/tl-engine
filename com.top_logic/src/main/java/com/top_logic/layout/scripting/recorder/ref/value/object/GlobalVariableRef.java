/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.object;

import com.top_logic.layout.scripting.action.SetGlobalVariableAction;
import com.top_logic.layout.scripting.recorder.ref.value.NamedValueRef;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;
import com.top_logic.layout.scripting.runtime.GlobalVariableStore;

/**
 * An {@link ValueRef} that identifies a global variable of the scripting framework.
 * 
 * @deprecated Use {@link GlobalVariableNaming} instead.
 * 
 * @see GlobalVariableStore
 * @see SetGlobalVariableAction
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
@Deprecated
public interface GlobalVariableRef extends NamedValueRef {

	/**
	 * Getter for the name (not the value!) of the global variable.
	 * <p>
	 * When this reference is resolved, the current value of the global variable is returned.
	 * </p>
	 */
	@Override
	String getName();

}
