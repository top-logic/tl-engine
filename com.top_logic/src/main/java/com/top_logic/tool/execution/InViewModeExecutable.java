/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.layout.form.component.edit.EditMode;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ExecutabilityRule} that displays a command, if its component is in view mode (or not an
 * {@link EditMode editor} at all).
 * 
 * @author <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public class InViewModeExecutable implements ExecutabilityRule {

	/**
	 * Singleton {@link InViewModeExecutable} instance.
	 */
	public static final InViewModeExecutable INSTANCE = new InViewModeExecutable();

	private InViewModeExecutable() {
		// Singleton constructor.
	}
	
	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		
		if (aComponent instanceof EditMode) {
			if (((EditMode) aComponent).isInViewMode()) {
				return ExecutableState.EXECUTABLE;
			} else {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
	    }
		
		return ExecutableState.EXECUTABLE;
	}

}
