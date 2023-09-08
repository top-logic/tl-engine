/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import java.io.File;

import com.top_logic.basic.Environment;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.version.Version;

/**
 * {@link Application} that represents the current runtime instance within a server.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RuntimeApplication implements Application {

	/**
	 * Singleton {@link RuntimeApplication} instance.
	 */
	public static final RuntimeApplication INSTANCE = new RuntimeApplication();

	private final boolean isDebug;

	private RuntimeApplication() {
		isDebug = !Environment.isDeployed();
	}

	private static final File CURRENT_DIR = new File(".");

	@Override
	public File getWebappDir() {
		return CURRENT_DIR;
	}

	/**
	 * Return true, if this application is running in an IDE.
	 */
	public boolean isDebug() {
		return this.isDebug;
	}

	@Override
	public File getModuleDir() {
		return CURRENT_DIR;
	}

	@Override
	public String getMetaConfResource() {
		return ModuleLayoutConstants.META_CONF_RESOURCE;
	}

	@Override
	public File getLayoutDir() {
		return new File(getWebappDir(), ModuleLayoutConstants.LAYOUT_PATH);
	}

	@Override
	public String getName() {
		return Version.getApplicationName();
	}

	@Override
	public File getTestSourceDir() {
		throw new UnsupportedOperationException();
	}

}
