/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.tooling;

import java.io.File;
import java.io.FileFilter;


/**
 * {@link FileFilter} that tests, whether a {@link File} is the context root
 * of a web application (module).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class IsWebAppDir implements FileFilter {
	
	/**
	 * Singleton {@link IsWebAppDir} instance.
	 */
	public static final IsWebAppDir INSTANCE = new IsWebAppDir();
	
	/**
	 * Creates a {@link IsWebAppDir} {@link FileFilter}.
	 */
	protected IsWebAppDir() {
		// Singleton constructor.
	}
	
	@Override
	public boolean accept(File pathname) {
		if (! pathname.isDirectory()) {
			return false;
		}
		
		File webInf = new File(pathname, ModuleLayoutConstants.WEB_INF_DIR);
		
		return webInf.exists() && webInf.isDirectory();
	}
}