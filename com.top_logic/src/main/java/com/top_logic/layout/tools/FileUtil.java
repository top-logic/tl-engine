/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools;

import java.io.File;

/**
 * Utilities for applying a functionality to multiple files.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FileUtil {

	/**
	 * Applies a {@link FileHandler} to all files specified in the given arguments.
	 * 
	 * @param args
	 *        {@link File#pathSeparator} separated paths of files to process.
	 * @param tool
	 *        The {@link FileHandler} to apply to each file.
	 */
	public static void handleFiles(String[] args, FileHandler tool) throws Exception {
		for (String arg : args) {
			handleFile(arg, tool);
		}
	}

	/**
	 * Applies a {@link FileHandler} to the files specified in the given arguments.
	 * 
	 * @param arg
	 *        {@link File#pathSeparator} separated paths of files to process.
	 * @param tool
	 *        The {@link FileHandler} to apply to each file.
	 */
	public static void handleFile(String arg, FileHandler tool) throws Exception {
		for (String fileName : arg.split(File.pathSeparator)) {
			tool.handleFile(fileName);
		}
	}

}
