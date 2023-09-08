/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.normalize;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import com.top_logic.tools.resources.ResourceFile;

/**
 * Maven goal to normalize project resources.
 */
@Mojo(name = "normalize")
public class NormalizeResources extends AbstractResourcesMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		File target = getResourceDir();
		if (!target.exists()) {
			getLog().info("No resources: " + target);
			return;
		}
		if (target.isDirectory()) {
			normalizeDir(target);
		} else {
			normalizeFile(target);
		}
	}

	private void normalizeDir(File dir) throws MojoExecutionException {
		File[] properties = dir.listFiles(f -> f.getName().endsWith(".properties"));
		if (properties != null) {
			for (File file : properties) {
				normalizeFile(file);
			}
		}
	}

	private void normalizeFile(File file) throws MojoExecutionException {
		try {
			byte[] origContents = Files.readAllBytes(file.toPath());

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			new ResourceFile(file).writeTo(buffer);

			byte[] newContents = buffer.toByteArray();

			if (!Arrays.equals(newContents, origContents)) {
				try (FileOutputStream out = new FileOutputStream(file)) {
					out.write(newContents);
				}
				getLog().info("Normalized: " + file);
			}
		} catch (IOException ex) {
			throw new MojoExecutionException("Failed to normalize: " + file, ex);
		}
	}

}
