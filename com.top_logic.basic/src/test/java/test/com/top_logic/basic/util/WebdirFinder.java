/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.top_logic.basic.io.DirectoriesOnlyFilter;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.tooling.Workspace;

/**
 * Utility for finding the "web-directory" of a given (or the current) project.
 * The returned file is always the canonical file: {@link File#getCanonicalFile()}
 * <br/>
 * The "web-directory" is the directory in the "webapps" directory containing the "WEB-INF" directory.
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class WebdirFinder {
	
	/**
	 * The instance of the {@link WebdirFinder}. This is not a singleton, as (potential)
	 * subclasses can create further instances.
	 */
	public static final WebdirFinder INSTANCE = new WebdirFinder();
	
	/**
	 * Only subclasses may need to instantiate it. Everyone else should use the {@link #INSTANCE}
	 * constant directly.
	 */
	protected WebdirFinder() {
		// See JavaDoc above.
	}
	
	/**
	 * Tries to find the "web-directory" in every project next to the current one.
	 * Prints out the result for every project or the error message if an {@link Exception} is thrown.
	 * Every directory in the parent of the current working directory of the JVM
	 * is interpreted as an project. Even the ".metadata" directory.
	 * 
	 * Has been used to manually check if this method of finding the "web-directory" would work.
	 */
	public void printWebDirStateForAllProjects() {
		File[] projectDirs = new File("..").listFiles(DirectoriesOnlyFilter.INSTANCE);
		for (File projectDir : projectDirs) {
			try {
				System.out.println(findWebDir(projectDir).getCanonicalPath());
			}
			catch (Exception exception) {
				System.err.println(exception.getMessage());
			}
		}
	}
	
	/** 
	 * Finds the "web-directory" of the current project.
	 * Expects the current working directory of the JVM to be the project root directory.
	 * If no or more than one "web-directory" is found, an {@link RuntimeException} is thrown.
	 */
	public File findWebDir() {
		return findWebDir(Workspace.pwd());
	}

	/**
	 * Find the "web-directory" of the project located in the given directory.
	 * If no or more than one "web-directory" is found, an {@link RuntimeException} is thrown.
	 */
	public File findWebDir(File projectDir) {
		try {
			checkExistence(projectDir);
			checkIsDirectory(projectDir);
			File webappDir = new File(projectDir, ModuleLayoutConstants.WEBAPP_DIR);
			checkExistence(webappDir);
			checkIsDirectory(webappDir);
			return webappDir.getCanonicalFile();
		}
		catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	private void checkSingleResult(File webappsDir, List<File> webinfDirs) throws IOException {
		if (webinfDirs.isEmpty()) {
			String message = "No directory with an WEB-INF subdirecty found: " + webappsDir.getCanonicalPath();
			throw new RuntimeException(message);
		}
		if (webinfDirs.size() > 1) {
			String message = "More than one directory in webapps with an WEB-INF subdirecty found: ";
			for (File webinfDir : webinfDirs) {
				message += (webinfDir.getCanonicalPath() + "; ");
			}
			throw new RuntimeException(message);
		}
	}
	
	private void checkIsDirectory(File projectDir) throws IOException {
		if ( ! projectDir.isDirectory()) {
			throw new RuntimeException("This should be a directory but is not: " + projectDir.getCanonicalPath());
		}
	}
	
	private void checkExistence(File projectDir) throws IOException {
		if ( ! projectDir.exists()) {
			throw new RuntimeException("Missing directory: " + projectDir.getCanonicalPath());
		}
	}
	
}