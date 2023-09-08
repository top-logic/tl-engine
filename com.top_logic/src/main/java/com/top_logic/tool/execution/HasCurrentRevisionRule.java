/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Executable only if a current revision of the model exists.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class HasCurrentRevisionRule implements ExecutabilityRule {

	/**
	 * Single instance of {@link HasCurrentRevisionRule}.
	 */
	public static final HasCurrentRevisionRule INSTANCE = new HasCurrentRevisionRule();

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (!(model instanceof Wrapper)) {
			// Not a Wrapper
			return ExecutableState.NO_EXEC_NO_CURRENT_DATA;
		}
		if (WrapperHistoryUtils.getCurrent((Wrapper) model) == null) {
			// No current version.
			return ExecutableState.NO_EXEC_NO_CURRENT_DATA;
		}
		return ExecutableState.EXECUTABLE;
	}

}
