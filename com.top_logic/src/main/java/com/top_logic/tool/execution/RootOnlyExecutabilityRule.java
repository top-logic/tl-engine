/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.TLContext;

/**
 * Rule for allowing something only for root.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class RootOnlyExecutabilityRule implements ExecutabilityRule {

	/**
	 * Singleton {@link RootOnlyExecutabilityRule} instance.
	 */
	public static final RootOnlyExecutabilityRule INSTANCE = new RootOnlyExecutabilityRule();

	private RootOnlyExecutabilityRule() {
		// Singleton constructor.
	}

    @Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (TLContext.isAdmin()) {
			return ExecutableState.EXECUTABLE;
		} else {
			return ExecutableState.NOT_EXEC_HIDDEN;
        }
    }
}

