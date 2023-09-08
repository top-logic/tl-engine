/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.touch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Maven goal to create an empty file.
 */
@Mojo(name = "touch", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class TouchFile extends AbstractMojo {

	/**
	 * The directory, {@link #path} is resolved relative to.
	 */
	@Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
	private File outputDirectory;

	/**
	 * The path to touch (relative to {@link #outputDirectory}.
	 */
	@Parameter(required = true)
	private String path;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		File file = new File(outputDirectory, path);
		if (file.exists()) {
			return;
		}
		file.getParentFile().mkdirs();
		try (OutputStream out = new FileOutputStream(file)) {
			// Empty file.
			getLog().info("Touching file: " + file);
		} catch (IOException ex) {
			throw new MojoExecutionException("File creation failed: " + ex.getMessage(), ex);
		}
	}

}
