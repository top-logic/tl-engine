/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.tooling;

import java.io.File;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;

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
	 * Returns the webapp directory from the top level project.
	 * 
	 * @see #topLevelProjectDirectory()
	 */
	public static File topLevelWebapp() {
		File project = topLevelProjectDirectory();

		return new File(project, ModuleLayoutConstants.WEBAPP_DIR);
	}

	/**
	 * Returns the top level project directory.
	 * {@link ModuleLayoutConstants#DEPLOY_LOCAL_FOLDER_NAME Local} and
	 * {@link ModuleLayoutConstants#DEPLOY_PRIVATE_FOLDER_NAME private} project directories are
	 * ignored.
	 */
	public static File topLevelProjectDirectory() {
		File webapp = FileManager.getInstance().getIDEPaths().get(0);

		return projectDir(webapp);
	}

}

