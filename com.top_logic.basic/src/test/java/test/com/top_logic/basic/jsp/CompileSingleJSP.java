/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.jsp;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;

import test.com.top_logic.basic.util.AbstractBasicTestAll;

import com.top_logic.basic.Environment;

/**
 * Class creating a test to test compilation of a selected file.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompileSingleJSP {

	/** Name of the system variable that contains the selected file or directory to test. */
	public static final String SELECTECD_FILE = "selected_file";

	/**
	 * Runs the compilation.
	 */
	public static void main(String[] args) throws IOException {
		String fileName = Environment.getSystemPropertyOrEnvironmentVariable(SELECTECD_FILE, null);
		if (fileName == null) {
			throw new IOException(
				"No selected file given. Ensure system variable '" + SELECTECD_FILE + "' is set.");
		}
		File file = new File(fileName);
		if (!file.exists()) {
			throw new IOException(
				"Selected file '" + file + "' does not exist.");
		}
		file = file.getCanonicalFile();

		CompileJspConfig config = new CompileJspConfig(AbstractBasicTestAll.webapp());

		FileFilter filter;
		if (file.isDirectory()) {
			filter = CompileJSP.JSP_DIR_FILTER;
		} else {
			filter = CompileJSP.JSP_FILTER;
		}
		config.setJspFiles(CompileJSP.findFiles(new ArrayList<>(), file, filter));
		TestCompileJSPs.compile(config);
	}

}

