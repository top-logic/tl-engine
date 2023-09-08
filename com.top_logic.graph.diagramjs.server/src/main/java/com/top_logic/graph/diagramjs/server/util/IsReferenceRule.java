/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.util;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLReference;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} that displays a command, if its model is a {@link TLReference}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class IsReferenceRule implements ExecutabilityRule {

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (model instanceof TLReference) {
			return ExecutableState.EXECUTABLE;
		}

		return ExecutableState.NOT_EXEC_HIDDEN;
	}

}
