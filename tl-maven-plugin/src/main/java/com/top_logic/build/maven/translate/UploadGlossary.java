/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.translate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Maven goal to upload a glossary.
 */
@Mojo(name = "upload-glossary", inheritByDefault = false)
public class UploadGlossary extends AbstractTranslateMojo {

	/**
	 * The glossary file to upload.
	 */
	@Parameter(property = "file", required = true)
	private File file;

	/**
	 * The character encoding of the glossary file.
	 */
	@Parameter(property = "charset", defaultValue = "utf-8")
	private String charset;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		initTranslator();

		if (!file.exists()) {
			throw new MojoExecutionException("Glossary does not exist: " + file.getAbsolutePath());
		}

		if (file.isDirectory()) {
			for (File glossary : file.listFiles(f -> f.getName().endsWith(".tsv"))) {
				upload(glossary);
			}
		} else {
			upload(file);
		}
	}

	private void upload(File glossary) throws MojoExecutionException {
		String fileName = glossary.getName();

		int separatorIndex = fileName.lastIndexOf('.');
		if (separatorIndex < 0) {
			separatorIndex = fileName.length();
		}
		int targetIndex = fileName.lastIndexOf('-', separatorIndex - 1);
		int sourceIndex = fileName.lastIndexOf('-', targetIndex - 1);

		String name = fileName.substring(0, separatorIndex);
		String source = fileName.substring(sourceIndex + 1, targetIndex);
		String target = fileName.substring(targetIndex + 1, separatorIndex);

		getLog().info("Uploading '" + name + "' (" + source + "-" + target + ") from: " + glossary.getAbsolutePath());

		StringBuilder contents = new StringBuilder();
		try (InputStream in = new FileInputStream(glossary)) {
			Reader r = new InputStreamReader(in, charset);

			int direct;
			char[] buffer = new char[4096];
			while ((direct = r.read(buffer)) >= 0) {
				contents.append(buffer, 0, direct);
			}
		} catch (IOException ex) {
			throw new MojoExecutionException("Cannot read glossary file: " + glossary.getAbsolutePath(), ex);
		}

		try {
			_translator.createGlossary(source, target, name, contents.toString());
		} catch (IOException ex) {
			throw new MojoExecutionException("Glossary upload failed.", ex);
		}
	}

}
