/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.File;
import java.io.FileFilter;

import com.top_logic.basic.col.Filter;

/**
 * {@link Filter} and {@link FileFilter} that only accepts directories.
 * 
 * Stateless singleton.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DirectoriesOnlyFilter implements FileFilter, Filter<File> {
	
	/**
	 * Singleton {@link DirectoriesOnlyFilter} instance.
	 */
	public static final DirectoriesOnlyFilter INSTANCE = new DirectoriesOnlyFilter();

	private DirectoriesOnlyFilter() {
		// Singleton constructor.
	}
	
	@Override
	public boolean accept(File pathname) {
		return pathname.isDirectory();
	}
	
}
