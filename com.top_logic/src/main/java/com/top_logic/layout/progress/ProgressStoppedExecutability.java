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
 * {@link ExecutabilityRule} for {@link AbstractProgressComponent} that allows command to be
 * executed if its {@link AbstractProgressComponent#getInfo() progress info} is not yet
 * {@link ProgressInfo#shouldStop() stopped} or almost finished.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class ProgressStoppedExecutability implements ExecutabilityRule {

	/** Singleton {@link ProgressStoppedExecutability} instance. */
	public static final ProgressStoppedExecutability INSTANCE = new ProgressStoppedExecutability();

	/**
	 * Creates a new {@link ProgressStoppedExecutability}.
	 */
	protected ProgressStoppedExecutability() {
		// singleton instance
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		ProgressInfo progressInfo = ((AbstractProgressComponent) aComponent).getInfo();

		if (progressInfo == null) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		} else if (progressInfo.shouldStop()) {
			return ExecutableState.createDisabledState(aComponent.getResPrefix().key("cancelled"));
		} else if (progressInfo.getProgress() >= 100) {
			return ExecutableState.createDisabledState(aComponent.getResPrefix().key("finishing"));
		}
		return ExecutableState.EXECUTABLE;
	}
}
