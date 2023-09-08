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
 * A {@link FileFilter} that accepts files ending with ".java".
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class JavaFileFilter implements Filter<File>, FileFilter {

	/**
	 * The ending of Java files.
	 */
	public static final String JAVA_FILE_ENDING = ".java";

	/**
	 * The instance of the {@link JavaFileFilter}.
	 */
	public static final JavaFileFilter INSTANCE = new JavaFileFilter();

	@Override
	public boolean accept(File file) {
		return file.getName().endsWith(JAVA_FILE_ENDING);
	}

}

