/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} that hides the command if the checked component is not in comparison
 * mode.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InCompareModeExecutability implements ExecutabilityRule {

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (!(aComponent instanceof CompareAlgorithmHolder)) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		CompareAlgorithm algorithm = ((CompareAlgorithmHolder) aComponent).getCompareAlgorithm();
		if (algorithm == null) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		return ExecutableState.EXECUTABLE;
	}

}

