/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.progress;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} returning a {@link ExecutableState#isDisabled() disabled} state when
 * the progress not yet finished.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ProgressFinishedRule implements ExecutabilityRule {

	/**
	 * This {@link ExecutableState} is disabled because the progress is not finished.
	 */
	public static final ExecutableState PROGRESS_NOT_FINISHED = ExecutableState
		.createDisabledState(I18NConstants.PROGRESS_NOT_FINISHED);

	/**
	 * Singleton {@link ProgressFinishedRule} instance.
	 */
	public static final ProgressFinishedRule INSTANCE = new ProgressFinishedRule();

	private ProgressFinishedRule() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (model instanceof ProgressInfo) {
			boolean progressFinished = ((ProgressInfo) model).isFinished();
			if (!progressFinished) {
				return PROGRESS_NOT_FINISHED;
			}
		}
		return ExecutableState.EXECUTABLE;
	}

}

