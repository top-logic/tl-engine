/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.core.workspace;

import static com.top_logic.basic.core.workspace.ModuleLayoutConstants.*;

import java.io.File;
import java.io.IOError;
import java.io.IOException;

import com.top_logic.basic.core.log.Log;

/**
 * Convention of the layout of files and directories within a <i>TopLogic</i> module.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModuleLayout {

	/** Default deploy aspects to include into the resource lookup path. */
	public static final String[] DEFAULT_DEPLOY_ASPECTS = new String[] {
		ModuleLayoutConstants.DEPLOY_LOCAL_FOLDER_NAME,
		ModuleLayoutConstants.DEPLOY_PRIVATE_FOLDER_NAME };

	private File workspaceDir;

	private File moduleDir;

	private File webappDir;

	private File webinfDir;

	private String metaConfResource;
	
	private File confDir;
	
	private File resourcesDir;

	private File layoutDir;

	private File srcTestDir;

	private File _deployDir;

	private File _localWebapp;

	private File _privateWebapp;

	/**
	 * Creates a {@link ModuleLayout}.
	 * 
	 * @param log
	 *        {@link Log} to report problems to, if the inspected module does not follow the
	 *        conventions.
	 * @param moduleDir
	 *        The directory of the module (the directory containing the <code>webapp</code> folder).
	 */
	public ModuleLayout(Log log, File moduleDir) {
		assert moduleDir != null : "Module dir must not be null";
		try {
			this.moduleDir = moduleDir.getCanonicalFile();
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Directory of the Eclipse workspace.
	 */
	public File getWorkspaceDir() {
		if (workspaceDir == null) {
			setWorkspaceDir(getModuleDir().getParentFile());
		}
		return workspaceDir;
	}

	/**
	 * @see #getWorkspaceDir()
	 */
	public void setWorkspaceDir(File workspaceDir) {
		checkDir(ModuleLayoutConstants.WORKSPACE, workspaceDir);
		this.workspaceDir = workspaceDir;
	}

	/**
	 * The top-level directory of the Eclipse module.
	 */
	public File getModuleDir() {
		return moduleDir;
	}

	/**
	 * The root directory of the files exposed through the HTTP interface.
	 */
	public File getWebappDir() {
		if (webappDir == null) {
			setWebappDir(new File(getModuleDir(), WEBAPP_DIR));
		}
		
		return webappDir;
	}

	/**
	 * @see #getWebappDir()
	 */
	public void setWebappDir(File webappDir) {
		checkDir(ModuleLayoutConstants.WEBAPP_DIR, webappDir);
		this.webappDir = webappDir;
	}

	/**
	 * The WEB-INF directory within {@link #getWebappDir()} that is excluded
	 * from direct export through the HTTP interface.
	 */
	public File getWebinfDir() {
		if (webinfDir == null) {
			setWebinfDir(new File(getWebappDir(), WEB_INF_DIR));
		}
		return webinfDir;
	}

	/**
	 * @see #getWebinfDir()
	 */
	public void setWebinfDir(File webinfDir) {
		checkDir(WEB_INF_DIR, webinfDir);
		this.webinfDir = webinfDir;
	}

	/**
	 * The <i>TopLogic</i> configuration directory within {@link #getWebinfDir()}.
	 */
	public File getConfDir() {
		if (confDir == null) {
			setConfDir(new File(getWebappDir(), CONF_PATH));
		}
		return confDir;
	}

	/**
	 * @see #getConfDir()
	 */
	public void setConfDir(File confDir) {
		checkDir(CONF_PATH, confDir);
		this.confDir = confDir;
	}

	/**
	 * The directory containing internationalization properties.
	 */
	public File getResourcesDir() {
		if (resourcesDir == null) {
			setResourcesDir(new File(getWebappDir(), RESOURCES_PATH));
		}
		return resourcesDir;
	}

	/**
	 * @see #getResourcesDir()
	 */
	public void setResourcesDir(File resourcesDir) {
		checkDir(RESOURCES_PATH, resourcesDir);
		this.resourcesDir = resourcesDir;
	}
	
	/**
	 * The directory containing the source code of the module.
	 */
	public File getTestSourceDir() {
		if (srcTestDir == null) {
			setTestSrcDir(new File(getModuleDir(), ModuleLayoutConstants.SRC_TEST_DIR));
		}
		return srcTestDir;
	}

	/**
	 * @see #getTestSourceDir()
	 */
	public void setTestSrcDir(File srcDir) {
		this.srcTestDir = srcDir;
	}

	/**
	 * The directory containing <i>TopLogic</i> component definitions.
	 */
	public File getLayoutDir() {
		if (layoutDir == null) {
			setLayoutDir(new File(getWebappDir(), ModuleLayoutConstants.LAYOUT_PATH));
		}
		return layoutDir;
	}

	/**
	 * @see #getLayoutDir()
	 */
	public void setLayoutDir(File layoutDir) {
		checkDir(LAYOUT_PATH, layoutDir);
		this.layoutDir = layoutDir;
	}
	
	/**
	 * The file that contains the list of configuration files for the application.
	 */
	public String getMetaConfResource() {
		if (metaConfResource == null) {
			setMetaConfResource(ModuleLayoutConstants.META_CONF_RESOURCE);
		}
		return metaConfResource;
	}
	
	/**
	 * @see #getMetaConfResource()
	 */
	public void setMetaConfResource(String metaConfResource) {
		this.metaConfResource = metaConfResource;
	}
	
	/**
	 * Returns the deploy folder for the module.
	 * 
	 * @return The deploy folder of the module. Not <code>null</code>, but may not exist.
	 */
	public File getDeployDir() {
		if (_deployDir == null) {
			setDeployDir(new File(getModuleDir(), DEPLOY_FOLDER_NAME));
		}
		return _deployDir;
	}

	/**
	 * Setter for {@link #getDeployDir()}
	 */
	void setDeployDir(File deployDir) {
		_deployDir = deployDir;
	}

	/**
	 * Returns the web application containing the data to start the application locally. Elements in
	 * this folder are not inherited to modules depending on this {@link ModuleLayout}.
	 * 
	 * @return The local web application folder of the module. Not <code>null</code>, but may not
	 *         exist.
	 */
	public File getLocalWebapp() {
		if (_localWebapp == null) {
			setLocalWebapp(new File(new File(getDeployDir(), DEPLOY_LOCAL_FOLDER_NAME), WEBAPP_DIR));
		}
		return _localWebapp;
	}

	void setLocalWebapp(File localWebapp) {
		_localWebapp = localWebapp;
	}

	/**
	 * Returns the web application containing the data that must not be included into a
	 * TopLogic-Studio.
	 * 
	 * <p>
	 * Such a web application typically contains data with local infrastructure dependent
	 * configurations, like host-names, user names, or passwords.
	 * </p>
	 * 
	 * @return The private web application folder of the module. Not <code>null</code>, but may not
	 *         exist.
	 * 
	 * @see ModuleLayoutConstants#DEPLOY_PRIVATE_FOLDER_NAME
	 */
	public File getPrivateWebapp() {
		if (_privateWebapp == null) {
			setPrivateWebapp(new File(new File(getDeployDir(), DEPLOY_PRIVATE_FOLDER_NAME), WEBAPP_DIR));
		}
		return _privateWebapp;
	}

	void setPrivateWebapp(File webapp) {
		_privateWebapp = webapp;
	}

	/**
	 * Checks that the given file exists and is a directory.
	 */
	protected final void checkDir(String simpleName, File dir) {
		checkFile(simpleName, dir);
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(
				"Directory " + simpleName + " is not a directory: " + dir.getAbsolutePath());
		}
	}

	/**
	 * Tests that the given file exists.
	 */
	protected void checkFile(String simpleName, File file) {
		if (!file.exists()) {
			throw new IllegalArgumentException("Path " + simpleName + " does not exist: " + file.getAbsolutePath());
		}
	}

}