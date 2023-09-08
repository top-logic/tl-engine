/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.xml.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * A {@link TemplateSource} for the filesystem.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TemplateFilesystemSource implements TemplateSource {

	private final File _file;

	private static final Pattern ABSOLUTE_PATH_PATTERN = Pattern.compile("[/\\~]|([a-zA-Z]\\:\\\\)");

	/**
	 * Creates a new {@link TemplateFilesystemSource} for the given {@link File}.
	 * 
	 * @throws IllegalArgumentException
	 *         If the given {@link File} does not {@link File#exists() exist}.
	 */
	public TemplateFilesystemSource(File templateFile) {
		if (!templateFile.exists()) {
			throw new IllegalArgumentException("The given file does not exist: '" + templateFile.getAbsolutePath()
				+ "'");
		}
		_file = templateFile;
	}

	@Override
	public InputStream getContent() throws IOException {
		return new FileInputStream(_file);
	}

	@Override
	public ResourceTransaction update() throws IOException {
		return new FilesystemTransaction(_file);
	}

	@Override
	public void delete() throws IOException {
		boolean success = _file.delete();
		if (!success) {
			throw new IOException("Deletion of the underlying file failed.");
		}
	}

	@Override
	public TemplateSource resolve(TemplateSource context, String templateReference) {
		File templateFile;
		if (isAbsolute(templateReference)) {
			templateFile = new File(templateReference);
		} else {
			templateFile = new File(_file.getParentFile(), templateReference);
		}
		return new TemplateFilesystemSource(templateFile);
	}

	private boolean isAbsolute(String templatePath) {
		return ABSOLUTE_PATH_PATTERN.matcher(templatePath).matches();
	}

	@Override
	public String toString() {
		return TemplateFilesystemSource.class.getSimpleName() + "(" + _file.getAbsolutePath() + ")";
	}

}
