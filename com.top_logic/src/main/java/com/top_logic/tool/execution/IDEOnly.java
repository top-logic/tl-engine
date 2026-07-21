/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.base.operation.ApplicationModeService;
import com.top_logic.base.operation.OperationMode;
import com.top_logic.basic.core.workspace.Environment;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ExecutabilityRule} for a command that must only be shown when running the app outside of a
 * real production install, i.e. on a developer workstation or in an automated test.
 *
 * <p>
 * The command is IDE tooling (e.g. exporting a layout or extracting a model back into the source
 * tree). It is useful in {@link OperationMode#DEVELOPMENT development} and must remain available in
 * {@link OperationMode#TEST test} so that scripted tests can exercise it; it is hidden only in a
 * live {@link OperationMode#PRODUCTION production} install.
 * </p>
 *
 * @see ApplicationModeService#getMode()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IDEOnly implements ExecutabilityRule {

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		return isVisibleIn(effectiveMode()) ? ExecutableState.EXECUTABLE : ExecutableState.NOT_EXEC_HIDDEN;
	}

	/**
	 * Whether IDE tooling is available in the given {@link OperationMode}.
	 *
	 * <p>
	 * Available in {@link OperationMode#DEVELOPMENT} and {@link OperationMode#TEST}, hidden only in
	 * {@link OperationMode#PRODUCTION}. This is what keeps the export/extract commands reachable
	 * from scripted tests (which run in a {@link OperationMode#TEST test} container).
	 * </p>
	 */
	public static boolean isVisibleIn(OperationMode mode) {
		return mode != OperationMode.PRODUCTION;
	}

	/**
	 * The effective {@link OperationMode} to gate on.
	 *
	 * <p>
	 * Consults the {@link ApplicationModeService single source of truth} for the operation mode. In
	 * a minimal setup where that service is not started, the low-level {@link Environment} marker is
	 * the only available signal and is used as a fallback (it answers the narrower question
	 * "packaged / non-development", which for a bare setup maps to production vs. development).
	 * </p>
	 */
	private static OperationMode effectiveMode() {
		if (ApplicationModeService.Module.INSTANCE.isActive()) {
			return ApplicationModeService.getInstance().getMode();
		}
		return Environment.isDeployed() ? OperationMode.PRODUCTION : OperationMode.DEVELOPMENT;
	}

}
