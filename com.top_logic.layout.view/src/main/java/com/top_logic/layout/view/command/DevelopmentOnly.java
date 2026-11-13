/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.base.operation.OperationMode;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.IDEOnly;

/**
 * {@link ViewExecutabilityRule} for a command that is not available in a
 * {@link OperationMode#PRODUCTION production} install.
 *
 * <p>
 * In {@link OperationMode#DEVELOPMENT development} and {@link OperationMode#TEST test} mode the
 * command is executable, so that scripted tests can exercise it. In
 * {@link OperationMode#PRODUCTION production} mode it is hidden for everyone, administrators
 * included. It is the gate of tooling that changes the definition of the application rather than
 * its data, such as the View Designer writing view files.
 * </p>
 *
 * @implNote Asks {@link IDEOnly#isAllowed()}, so a command gated by this rule is available under
 *           exactly the conditions of the IDE commands of the classic UI.
 */
public class DevelopmentOnly implements ViewExecutabilityRule {

	/**
	 * Configuration for {@link DevelopmentOnly}.
	 */
	@TagName("development-only")
	public interface Config extends ViewExecutabilityRule.Config {

		@Override
		@ClassDefault(DevelopmentOnly.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();
	}

	/**
	 * Shared instance for use outside a configuration, e.g. as the
	 * {@link ViewCommand#getIntrinsicRule() intrinsic rule} of a command.
	 */
	public static final DevelopmentOnly INSTANCE = new DevelopmentOnly();

	private DevelopmentOnly() {
		// Singleton instance.
	}

	/**
	 * Creates a new {@link DevelopmentOnly} from configuration.
	 */
	@CalledByReflection
	public DevelopmentOnly(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		return IDEOnly.isAllowed() ? ExecutableState.EXECUTABLE : ExecutableState.NOT_EXEC_HIDDEN;
	}
}
