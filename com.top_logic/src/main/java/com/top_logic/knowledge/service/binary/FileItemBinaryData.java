/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.binary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.http.Part;

import com.top_logic.basic.Settings;
import com.top_logic.basic.io.binary.AbstractBinaryData;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * Wraps a {@link Part} as {@link BinaryData}.
 */
public class FileItemBinaryData extends AbstractBinaryData {

    /** The Object wrapped by this class */
	private final Part _part;

	private File _tempFile;

	/**
	 * Creates a {@link FileItemBinaryData}.
	 *
	 * @param part
	 *        The {@link Part} of a multi-part request to wrap.
	 */
	public FileItemBinaryData(Part part) {
		this._part = part;
    }
    
    @Override
	public long getSize() {
		return _part.getSize();
    }
    
    @Override
	public InputStream getStream() throws IOException {
		// Note: In Tomcat 11, accessing the input stream of an uploaded part seems to be possible
		// only once. Since the framework may access the data of a binary data multiple times, the
		// contents must be copied to a safe location before using it.
		if (_tempFile == null) {
			File tempDir = Settings.getInstance().getTempDir();
			_tempFile = File.createTempFile("upload", ".data", tempDir);
			_part.write(_tempFile.getAbsolutePath());
		}

		return new FileInputStream(_tempFile);
    }
    
	@Override
	public String getName() {
		String submittedFileName = _part.getSubmittedFileName();
		return submittedFileName != null ? submittedFileName : _part.getName();
	}

	@Override
	public String getContentType() {
		return nonNullContentType(_part.getContentType());
	}
    
}

