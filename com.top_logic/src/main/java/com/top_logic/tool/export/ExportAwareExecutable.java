/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} that hides the command when the component is <b>not</b>
 * {@link ExportAware}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExportAwareExecutable implements ExecutabilityRule {

	/** Singleton {@link ExportAwareExecutable} instance. */
	public static final ExportAwareExecutable INSTANCE = new ExportAwareExecutable();

	private ExportAwareExecutable() {
		// singleton instance
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (aComponent instanceof ExportAware) {
			return ExecutableState.EXECUTABLE;
		}
		return ExecutableState.NOT_EXEC_HIDDEN;
	}

}

