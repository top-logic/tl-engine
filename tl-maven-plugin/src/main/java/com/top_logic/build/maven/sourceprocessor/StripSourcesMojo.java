/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.sourceprocessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * {@link AbstractMojo} stripping a hierarchy of source files.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Mojo(name = StripSourcesMojo.GOAL, defaultPhase = LifecyclePhase.PREPARE_PACKAGE, threadSafe = true)
public class StripSourcesMojo extends AbstractMojo {

	/**
	 * Name fo the {@link StripSourcesMojo} goal.
	 */
	public static final String GOAL = "strip-sources";

	private static final String EXTENSION = ".java";

	private static final int EXTENSION_LENGTH = EXTENSION.length();

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	@Parameter(name = "baseDir", required = true)
	private File _baseDir;

	@Parameter(name = "targetDir", required = true)
	private File _targetDir;

	@Parameter(name = "mappingProperties", required = false)
	private File _mappingProperties;

	@Parameter(name = "skip", defaultValue = "false", property = "tl.skipStripSources")
	private boolean _skip;

	/**
	 * Whether to skip execution.
	 */
	public boolean isSkip() {
		return _skip;
	}

	/**
	 * @see #isSkip()
	 */
	public void setSkip(boolean skip) {
		_skip = skip;
	}

	/**
	 * The source folder to process.
	 */
	public File getBaseDir() {
		return _baseDir;
	}

	/**
	 * @see #getBaseDir()
	 */
	public void setBaseDir(File baseDir) {
		_baseDir = baseDir;
	}

	/**
	 * The folder where to write processed source files to.
	 */
	public File getTargetDir() {
		return _targetDir;
	}

	/**
	 * @see #getTargetDir()
	 */
	public void setTargetDir(File targetDir) {
		_targetDir = targetDir;
	}

	/**
	 * The properties file to store the line number mapping to.
	 */
	public File getMappingProperties() {
		return _mappingProperties;
	}

	/**
	 * @see #getMappingProperties()
	 */
	public void setMappingProperties(File mappingProperties) {
		_mappingProperties = mappingProperties;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			tryExecute();
		} catch (IOException ex) {
			throw new MojoFailureException("Cannot strip sources.", ex);
		}
	}

	private void tryExecute() throws FileNotFoundException, IOException {
		if (_skip) {
			getLog().info("Skipping.");
			return;
		}
		if (!_baseDir.exists()) {
			getLog().info("Skipping, does not exits: " + _baseDir);
			return;
		}
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(_baseDir);
		scanner.setIncludes(new String[] { "**/*.java" });
		scanner.scan();

		Properties mapping = new Properties();
		String[] files = scanner.getIncludedFiles();
		getLog().info("Processing " + files.length + " source files to " + _targetDir.getAbsolutePath());
		for (String name : files) {
			File resource = new File(_baseDir, name);

			StripInfo info;
			File targetFile = new File(_targetDir, name);
			targetFile.getParentFile().mkdirs();
			try (OutputStream out = new FileOutputStream(targetFile)) {
				info = SourceStripper.process(() -> new FileInputStream(resource), out);
				if (!info.getExcludeRanges().isEmpty()) {
					mapping.setProperty(convertBackslash(stripExtension(name)), info.toString());
				}
			} catch (RuntimeException ex) {
				getLog().error("Problem processing file " + resource.getPath() + ": " + ex.getMessage(), ex);
			}
		}

		if (_mappingProperties != null) {
			mapping.store(new FileOutputStream(_mappingProperties), null);
			getLog().info("Stored line mapping to " + _mappingProperties.getAbsolutePath());
		}

		// Remove original source folder to prevent the source jar plugin from adding the originals.
		String originalRoot = _baseDir.getCanonicalPath();
		for (Iterator<String> it = project.getCompileSourceRoots().iterator(); it.hasNext();) {
			String src = it.next();

			if (new File(src).getCanonicalPath().equals(originalRoot)) {
				it.remove();
			}
		}
		project.addCompileSourceRoot(_targetDir.getAbsolutePath());
	}

	private String convertBackslash(String name) {
		return name.replace('\\', '/');
	}

	private static String stripExtension(String name) {
		if (name.endsWith(EXTENSION)) {
			return name.substring(0, name.length() - EXTENSION_LENGTH);
		}
		return name;
	}
}
