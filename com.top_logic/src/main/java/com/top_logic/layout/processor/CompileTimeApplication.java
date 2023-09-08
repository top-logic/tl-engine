/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.core.workspace.Workspace;
import com.top_logic.basic.tooling.ModuleLayout;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Information about a processed application module.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CompileTimeApplication implements Application {

	private final ModuleLayout _module;

	public static CompileTimeApplication createApplicationModule(File applicationRoot) throws IOException {
		if (!applicationRoot.exists()) {
			throw new FileNotFoundException("Application root diretory '" + applicationRoot + "' does not exist.");
		}
		if (!applicationRoot.isDirectory()) {
			throw new FileNotFoundException("Application root '" + applicationRoot + "' is not a diretory.");
		}

		return createCompileTimeApplication(Workspace.webappWorkspaceRoot(applicationRoot),
			applicationRoot);
	}

	public static CompileTimeApplication createCompileTimeApplication(File workspaceRoot, File applicationRoot)
			throws IOException {
		
		ModuleLayout module =
			new ModuleLayout(new LogProtocol(CompileTimeApplication.class),
				new File(applicationRoot, ModuleLayoutConstants.PATH_TO_MODULE_ROOT));
		module.setWorkspaceDir(workspaceRoot);
		module.setWebappDir(applicationRoot);
		
		return new CompileTimeApplication(module);
	}

	private CompileTimeApplication(ModuleLayout module) {
		_module = module;
	}

	@Override
	public String getName() {
		return getWebappDir().getName();
	}

	/**
	 * The Eclipse workspace directory, or <code>null</code>, if this is a deployed system.
	 */
	public File getWorkspaceRoot() {
		return _module.getWorkspaceDir();
	}

	@Override
	public File getModuleDir() {
		return _module.getModuleDir();
	}

	@Override
	public File getWebappDir() {
		return _module.getWebappDir();
	}

	@Override
	public String getMetaConfResource() {
		return _module.getMetaConfResource();
	}

	@Override
	public File getLayoutDir() {
		return _module.getLayoutDir();
	}

	@Override
	public File getTestSourceDir() {
		return _module.getTestSourceDir();
	}

}