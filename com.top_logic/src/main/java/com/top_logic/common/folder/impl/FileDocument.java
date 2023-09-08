/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.folder.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.top_logic.basic.io.binary.AbstractBinaryData;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dsa.util.MimeTypes;

/**
 * {@link BinaryData} based on a {@link File}.
 * 
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
public class FileDocument extends AbstractBinaryData {

	private final File file;

	/**
	 * New instance wrapping the given file. The file may not be null
	 */
	public FileDocument(File file) {
		assert file != null;
		this.file = file;
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public String getContentType() {
		return MimeTypes.getInstance().getMimeType(getName());
	}

	/**
	 * the File wrapped by this
	 */
	public File getFile() {
		return file;
	}

	@Override
	public long getSize() {
		return file.length();
	}

	@Override
	public InputStream getStream() throws IOException {
		return new FileInputStream(file);
	}

}
