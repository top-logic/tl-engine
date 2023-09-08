/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Collection;
import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ExecutabilityRule} that displays a command, if only one row is selected in its component.
 * 
 * @author <a href=mailto:diana.pankratz@top-logic.com>dpa</a>
 */
public class MultiSelectionHidden implements ExecutabilityRule {

	/**
	 * Singleton {@link MultiSelectionHidden} instance.
	 */
	public static final MultiSelectionHidden INSTANCE = new MultiSelectionHidden();

	private MultiSelectionHidden() {
		// Singleton constructor.
	}
	
	private static final ExecutableState EXECUTABLE_NONE =
		ExecutableState.createDisabledState(I18NConstants.ERROR_EDIT_MULTI_SELECTION);

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (model instanceof Collection && ((Collection<?>) model).size() > 1) {
			return EXECUTABLE_NONE;
		} else {
			return ExecutableState.EXECUTABLE;
		}
		
	}

}
