/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.context;

import java.io.File;
import java.nio.file.Path;

import com.top_logic.base.operation.OperationMode;
import com.top_logic.base.operation.OperationModeService;
import com.top_logic.basic.tooling.Workspace;

/**
 * The source of the running application, when it runs in a developer's workspace.
 *
 * <p>
 * The answer to "may a tool write into the application's source?": only when the engine reports the
 * {@link OperationMode#DEVELOPMENT development} environment - an application started from its source
 * with {@code tl_operation_mode=development} - and the web application folder of the top level
 * project exists. It is the same folder the engine's own in-app tooling writes back to, such as
 * exporting a model module into the workspace. A test install ({@link OperationMode#TEST}) and a
 * production install have no source to write; neither has an installation in a maintenance window.
 * </p>
 */
public final class DevelopmentWorkspace {

	private static volatile Path _webappForTests;

	private DevelopmentWorkspace() {
		// Only static operations.
	}

	/**
	 * The web application folder of the application's source, {@code src/main/webapp} of the top
	 * level project; <code>null</code> when the application does not run in a developer's
	 * workspace.
	 */
	public static Path webapp() {
		Path forTests = _webappForTests;
		if (forTests != null) {
			return forTests;
		}
		if (mode() != OperationMode.DEVELOPMENT) {
			return null;
		}
		File webapp;
		try {
			webapp = Workspace.topLevelWebapp();
		} catch (RuntimeException ex) {
			return null;
		}
		return webapp.isDirectory() ? webapp.toPath() : null;
	}

	/**
	 * The environment the engine reports, or <code>null</code> when its service is not started.
	 */
	public static OperationMode mode() {
		if (!OperationModeService.Module.INSTANCE.isActive()) {
			return null;
		}
		return OperationModeService.getInstance().getMode();
	}

	/**
	 * Lets {@link #webapp()} answer the given folder, as if the application ran from a source whose
	 * web application folder it is; <code>null</code> restores the real answer.
	 *
	 * <p>
	 * For a test, which runs in the test environment and whose real source must not be written to.
	 * </p>
	 */
	public static void setWebappForTests(Path webapp) {
		_webappForTests = webapp;
	}

}
