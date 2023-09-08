/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.file.filter;

import java.io.File;
import java.io.FileFilter;

import com.top_logic.basic.col.Filter;

/**
 * A {@link FileFilter} that accepts "normal" files as defined by {@link File#isFile()}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class NormalFileFilter implements Filter<File>, FileFilter {

	/**
	 * The instance of the {@link NormalFileFilter}.
	 */
	public static final NormalFileFilter INSTANCE = new NormalFileFilter();

	@Override
	public boolean accept(File file) {
		return file.isFile();
	}

}
