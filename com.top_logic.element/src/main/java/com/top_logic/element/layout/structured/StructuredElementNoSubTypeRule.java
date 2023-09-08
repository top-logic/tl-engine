/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.structured;

import java.util.Map;

import com.top_logic.element.structured.StructuredElement;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.InViewModeExecutable;

/**
 * The StructuredElementNoSubTypeRule works for {@link LayoutComponent}s which
 * have a {@link StructuredElement} as model.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StructuredElementNoSubTypeRule implements ExecutabilityRule {

	/**
	 * Singleton {@link StructuredElementNoSubTypeRule} instance.
	 */
	public static final StructuredElementNoSubTypeRule INSTANCE = new StructuredElementNoSubTypeRule();

	private StructuredElementNoSubTypeRule() {
		// Singleton constructor.
	}

	/**
	 * The method first checks is {@link InViewModeExecutable} allows execution.
	 * If it is executable by {@link InViewModeExecutable}, the methods forbids
	 * the execution if the {@link StructuredElement} allows no sub types.
	 * 
	 * @see com.top_logic.tool.execution.InViewModeExecutable#isExecutable(com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
	 */
	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		ExecutableState theState = InViewModeExecutable.INSTANCE.isExecutable(aComponent, model, someValues);
		if (theState.isExecutable()) {
			StructuredElement theElement = (StructuredElement) model;
			if (theElement == null || theElement.getChildrenTypes().size() == 0) {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
		}
		return theState;
	}

}
