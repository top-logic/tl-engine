/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule}, that return {@link ExecutableState#EXECUTABLE} if
 * {@link ChartConfigComponent#getBaseConfig()} is not <code>null</code>.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class RequiresBaseConfigRule implements ExecutabilityRule {

	/**
	 * Singleton {@link RequiresBaseConfigRule} instance.
	 */
	public static final RequiresBaseConfigRule INSTANCE = new RequiresBaseConfigRule();

	private RequiresBaseConfigRule() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (((ChartConfigComponent) aComponent).getBaseConfig() != null) {
			return ExecutableState.EXECUTABLE;
		} else {
			return ExecutableState.NO_EXEC_NO_MODEL;
		}
	}

}
