/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.tooling;

import java.io.File;

import com.top_logic.basic.FileManager;

/**
 * Abstraction of an Eclipse workspace.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Workspace extends com.top_logic.basic.core.workspace.Workspace {

	/**
	 * Creates a {@link Workspace}.
	 */
	protected Workspace(File root) {
		super(root);
	}

	/**
	 * Returns the webapp directory from the top level project, the
	 * {@link FileManager#getTopLevelWebapp() web application folder of the application module}.
	 * 
	 * @see #topLevelProjectDirectory()
	 */
	public static File topLevelWebapp() {
		return FileManager.getInstance().getTopLevelWebapp().toFile();
	}

	/**
	 * Returns the top level project directory, the project of the
	 * {@link FileManager#getTopLevelWebapp() web application folder of the application module}.
	 */
	public static File topLevelProjectDirectory() {
		File webapp = FileManager.getInstance().getTopLevelWebapp().toFile();

		return projectDir(webapp);
	}

}

