/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.util;

import java.io.File;
import java.io.FileFilter;

import com.top_logic.basic.col.Filter;

/**
 * A {@link Filter} and {@link FileFilter} that accepts only scripted-test scripts.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ScriptFileFilter implements Filter<File>, FileFilter {

	/**
	 * The file-ending of a scripted-test-xml.
	 */
	public static final String SCRIPT_FILE_ENDING = ".script.xml";

	/** The instance of the {@link ScriptFileFilter}. */
	public static final ScriptFileFilter INSTANCE = new ScriptFileFilter();

	@Override
	public boolean accept(File file) {
		return file.isFile() && file.getName().endsWith(SCRIPT_FILE_ENDING);
	}

}
