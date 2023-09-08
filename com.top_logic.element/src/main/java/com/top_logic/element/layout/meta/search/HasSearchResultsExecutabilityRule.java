/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Checks if an {@link AttributedSearchResultSet} contains result objects.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class HasSearchResultsExecutabilityRule implements ExecutabilityRule {

	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> values) {
		if (model != null) {
			if (!((AttributedSearchResultSet) model).getResultObjects().isEmpty()) {
				return ExecutableState.EXECUTABLE;
			} else {
				return ExecutableState.createDisabledState(I18NConstants.NOT_EXECUTABLE_NO_SEARCH_RESULT);
			}
		} else {
			return ExecutableState.createDisabledState(I18NConstants.NOT_EXECUTABLE_NO_SEARCH_EXECUTED);
		}
		
	}

}
