/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout;

import java.util.Map;

import com.top_logic.bpe.layout.execution.DisplayDescriptionAware;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} returning a
 * {@link ExecutableState#createDisabledState(com.top_logic.basic.util.ResKey) disabled state} when
 * the {@link DisplayDescriptionAware} has no display description.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HasDescriptionRule implements ExecutabilityRule {

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (aComponent instanceof DisplayDescriptionAware) {
			if (((DisplayDescriptionAware) aComponent).getDisplayDescription() != null) {
				return ExecutableState.EXECUTABLE;
			} else {
				return ExecutableState.createDisabledState(I18NConstants.NOT_EXECUTABLE_NO_DISPLAY_DESCRIPTION);
			}
		}
		return ExecutableState.NOT_EXEC_HIDDEN;
	}

}

