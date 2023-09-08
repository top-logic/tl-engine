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
public class NullModelHidden implements ExecutabilityRule {

	public static final NullModelHidden INSTANCE = new NullModelHidden();
	
	/** 
	 * @see com.top_logic.tool.execution.ExecutabilityRule#isExecutable(com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
	 */
	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
		if (model == null) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		else {
			return ExecutableState.EXECUTABLE;
		}
	}

}
