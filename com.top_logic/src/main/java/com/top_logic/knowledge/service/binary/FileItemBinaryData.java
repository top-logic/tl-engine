/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.binary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.servlet.http.Part;

import com.top_logic.basic.Settings;
import com.top_logic.basic.io.StreamUtilities;
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

			// Note: When using this API, Tomcat 11 seems to delete the file directly after the
			// request is completed. But typically, the framework needs the uploaded file after the
			// upload request is done to store it to the database in a second request e.g.
			// triggering the save button.
			//
			// _part.write(_tempFile.getAbsolutePath());

			try (InputStream in = _part.getInputStream()) {
				try (OutputStream out = new FileOutputStream(_tempFile)) {
					StreamUtilities.copyStreamContents(in, out);
				}
			}
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

