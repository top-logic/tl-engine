/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * This class disables commands if the model of the component is null.
 * 
 * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public class NullModelDisabled implements ExecutabilityRule {

	/** {@link ExecutableState#isDisabled() Disabled} rule that states "There is no model". */
	public static final ExecutableState EXEC_STATE_DISABLED =
		ExecutableState.createDisabledState(I18NConstants.ERROR_NO_MODEL_2);
    
	public static final NullModelDisabled INSTANCE = new NullModelDisabled();
	
	/** 
	 * @see com.top_logic.tool.execution.ExecutabilityRule#isExecutable(com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
	 */
	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
		if (model == null) {
			return NullModelDisabled.EXEC_STATE_DISABLED;
		}
		return ExecutableState.EXECUTABLE;
	}

}
