/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.base.operation.OperationMode;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.TLContext;

/**
 * {@link ExecutabilityRule} for a developer command that is reserved for administrators in a
 * {@link OperationMode#PRODUCTION production} install.
 *
 * <p>
 * In {@link OperationMode#DEVELOPMENT development} and {@link OperationMode#TEST test} mode the
 * command is executable for every user, e.g. to record a scripted test with a non-administrator
 * account. In {@link OperationMode#PRODUCTION production} mode it behaves like
 * {@link RootOnlyExecutabilityRule}.
 * </p>
 *
 * @see IDEOnly
 */
public class RootOnlyInProduction implements ExecutabilityRule {

	/**
	 * Singleton {@link RootOnlyInProduction} instance.
	 */
	public static final RootOnlyInProduction INSTANCE = new RootOnlyInProduction();

	private RootOnlyInProduction() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		return isAllowed() ? ExecutableState.EXECUTABLE : ExecutableState.NOT_EXEC_HIDDEN;
	}

	/**
	 * Whether the current user may execute a command guarded by this rule.
	 *
	 * <p>
	 * Every user outside {@link OperationMode#PRODUCTION production} mode, only administrators in
	 * production mode.
	 * </p>
	 */
	public static boolean isAllowed() {
		return IDEOnly.effectiveMode() != OperationMode.PRODUCTION || TLContext.isAdmin();
	}

}
