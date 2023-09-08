/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.normalize;

import java.io.File;
import java.nio.file.Paths;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Base class for resource file operations.
 */
public abstract class AbstractResourcesMojo extends AbstractMojo {

	/**
	 * Base directory to resolve {@link #resourcePath} against.
	 */
	@Parameter(defaultValue = "${project.basedir}", property = "baseDir")
	private File baseDir;

	/**
	 * The path name of the file or directory to normalize.
	 * 
	 * <p>
	 * The path name is resolved relative to {@link #baseDir}.
	 * </p>
	 */
	@Parameter(defaultValue = "src/main/webapp/WEB-INF/conf/resources", property = "resourcePath")
	private String resourcePath;

	File getBaseDir() {
		return baseDir;
	}

	String getResourcePath() {
		return resourcePath;
	}

	/**
	 * The configured directory, where resource bundles are located.
	 */
	protected File getResourceDir() {
		return Paths.get(getResourcePath()).isAbsolute() ? new File(getResourcePath())
			: new File(getBaseDir(), getResourcePath());
	}

}
