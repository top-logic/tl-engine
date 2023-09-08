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
 * The HiddenInEditMode is used to hide something if the component is in edit mode.
 * 
 * @author    <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class HiddenInEditMode implements ExecutabilityRule {

    public static final HiddenInEditMode INSTANCE = new HiddenInEditMode();

    /**
     * @see com.top_logic.tool.execution.ExecutabilityRule#isExecutable(com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
     */
    @Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
		if (aComponent instanceof EditMode && ((EditMode) aComponent).isInEditMode()) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		return ExecutableState.EXECUTABLE;
    }

}

