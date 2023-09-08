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
 * Allow Execution for {@link EditMode#isInEditMode()}.
 * 
 * @author <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public class InEditModeExecutable implements ExecutabilityRule {
	
	public static final InEditModeExecutable INSTANCE = new InEditModeExecutable();

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (!(aComponent instanceof EditMode)) {
			return ExecutableState.EXECUTABLE;
		}
		if (((EditMode) aComponent).isInEditMode()) {
			return ExecutableState.EXECUTABLE;
		} else {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
	}
}
