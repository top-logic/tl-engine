/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.classprocessor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;

import com.top_logic.tool.stacktrace.internal.LineNumberEncoding;

/**
 * {@link AbstractMojo} applying a {@link LineNumberEncoding} to a set of Java class files.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Mojo(name = RewriteClassesMojo.GOAL, defaultPhase = LifecyclePhase.PREPARE_PACKAGE, threadSafe = true)
public class RewriteClassesMojo extends AbstractMojo {

	/**
	 * Goal name of {@link RewriteClassesMojo}.
	 */
	public static final String GOAL = "rewrite-classes";

	@Parameter(name = "baseDir", required = true)
	private File _baseDir;

	@Parameter(name = "targetDir", required = true)
	private File _targetDir;

	@Parameter(name = "mappingProperties", required = true)
	private File _mappingProperties;

	@Parameter(name = "skip", defaultValue = "false", property = "tl.skipRewriteClasses")
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
	 * The classes directory with classes to process.
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
	 * The output directory, where processed classes should be stored.
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
	 * The line number mapping to apply to classes.
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
			throw new MojoFailureException("Cannot process classes.", ex);
		}
	}

	private void tryExecute() throws IOException, MojoFailureException {
		if (_skip) {
			getLog().info("Skipping.");
			return;
		}
		if (!_baseDir.exists()) {
			getLog().info("Skipping, does not exits: " + _baseDir);
			return;
		}
		if (!_mappingProperties.exists()) {
			getLog().info("Skipping, does not exits: " + _mappingProperties);
			return;
		}

		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(_baseDir);
		scanner.setIncludes(new String[] { "**/*.class" });
		scanner.scan();

		ClassRewriter rewriter = new ClassRewriter();
		rewriter.loadLineNumberMapping(_mappingProperties);

		String[] files = scanner.getIncludedFiles();
		getLog().info("Processing " + files.length + " class files to " + _targetDir.getAbsolutePath());
		for (String name : files) {
			File resource = new File(_baseDir, name);

			// Read source to buffer to allow writing back to the same file.
			byte[] buffer = Files.readAllBytes(resource.toPath());

			File targetFile = new File(_targetDir, name);
			targetFile.getParentFile().mkdirs();

			try (OutputStream out = new FileOutputStream(targetFile)) {
				rewriter.rewrite(new ByteArrayInputStream(buffer), out);
			} catch (RuntimeException ex) {
				throw new MojoFailureException("Problem processing file " + resource.getPath() + ": " + ex.getMessage(),
					ex);
			}
		}
	}

}
