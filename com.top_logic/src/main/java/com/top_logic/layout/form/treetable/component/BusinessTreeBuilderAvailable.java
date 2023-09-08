/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.treetable.component;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Checks whether the business tree model builder of the {@link StructureEditComponent} exists.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class BusinessTreeBuilderAvailable implements ExecutabilityRule {

	/**
	 * Singleton {@link BusinessTreeBuilderAvailable} instance.
	 */
	public static final BusinessTreeBuilderAvailable INSTANCE = new BusinessTreeBuilderAvailable();

	private BusinessTreeBuilderAvailable() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (aComponent instanceof StructureEditComponent<?>
			&& ((StructureEditComponent<?>) aComponent)._compareModelBuilder == null) {
			// Need a business tree builder for comparison
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		return ExecutableState.EXECUTABLE;
	}

}

