/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public class AlwaysHidden implements ExecutabilityRule {

	public static final ExecutabilityRule INSTANCE = new AlwaysHidden();
	
	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
		return ExecutableState.NOT_EXEC_HIDDEN;
	}

}
