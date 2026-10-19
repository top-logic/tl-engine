/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.base.operation.OperationMode;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ViewExecutabilityRule} for a developer command that is reserved for administrators in a
 * {@link OperationMode#PRODUCTION production} install.
 *
 * <p>
 * In {@link OperationMode#DEVELOPMENT development} and {@link OperationMode#TEST test} mode the
 * command is executable for every user, e.g. to record a scripted test with a non-administrator
 * account. In {@link OperationMode#PRODUCTION production} mode it is hidden for everyone except
 * administrators.
 * </p>
 *
 * @implNote Asks {@link com.top_logic.tool.execution.RootOnlyInProduction#isAllowed()}, so a
 *           command gated by this rule is available under exactly the conditions of the
 *           corresponding developer commands of the classic UI.
 */
public class RootOnlyInProduction implements ViewExecutabilityRule {

	/**
	 * Configuration for {@link RootOnlyInProduction}.
	 */
	@TagName("root-only-in-production")
	public interface Config extends ViewExecutabilityRule.Config {

		@Override
		@ClassDefault(RootOnlyInProduction.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();
	}

	/**
	 * Creates a new {@link RootOnlyInProduction} from configuration.
	 */
	@CalledByReflection
	public RootOnlyInProduction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		return com.top_logic.tool.execution.RootOnlyInProduction.isAllowed() ? ExecutableState.EXECUTABLE
			: ExecutableState.NOT_EXEC_HIDDEN;
	}
}
