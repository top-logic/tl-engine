/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import jakarta.servlet.http.Part;

import com.top_logic.basic.Settings;

/**
 * {@link Part} of a multipart request whose content is served from a {@link BinaryData}.
 *
 * <p>
 * The servlet API does not guarantee that the content of a {@link Part} delivered by the container
 * can be read more than once. A {@link BinaryDataPart} holds the content in a {@link BinaryData},
 * which can be read any number of times through {@link BinaryData#getStream()}, and therefore can
 * be passed on to any number of consumers.
 * </p>
 *
 * <p>
 * The part identity ({@link #getName()}, {@link #getSubmittedFileName()},
 * {@link #getContentType()} and the headers) is taken from the {@link Part} the content was read
 * from, the {@link #getSize()} from the {@link #getData()} that delivers the content.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BinaryDataPart implements Part {

	private final Part _original;

	private final BinaryData _data;

	/**
	 * Creates a {@link BinaryDataPart}.
	 *
	 * @param original
	 *        The {@link Part} delivered by the container, see {@link #getName()}. Its content must
	 *        already have been read into the given data.
	 * @param data
	 *        The content of the part, see {@link #getData()}.
	 */
	public BinaryDataPart(Part original, BinaryData data) {
		_original = original;
		_data = data;
	}

	/**
	 * The content of this part.
	 */
	public BinaryData getData() {
		return _data;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return _data.getStream();
	}

	@Override
	public String getContentType() {
		return _original.getContentType();
	}

	@Override
	public String getName() {
		return _original.getName();
	}

	@Override
	public String getSubmittedFileName() {
		return _original.getSubmittedFileName();
	}

	@Override
	public long getSize() {
		return _data.getSize();
	}

	/**
	 * Writes the content of this part to the given file.
	 *
	 * @param fileName
	 *        The file to write to. A relative name is resolved against the temporary directory of
	 *        the application, see {@link Settings#getTempDir()}.
	 */
	@Override
	public void write(String fileName) throws IOException {
		File file = new File(fileName);
		if (!file.isAbsolute()) {
			file = new File(Settings.getInstance().getTempDir(), fileName);
		}
		try (OutputStream out = new FileOutputStream(file)) {
			_data.deliverTo(out);
		}
	}

	/**
	 * Does nothing, the storage of the content is owned by {@link #getData()}.
	 */
	@Override
	public void delete() throws IOException {
		// The life-cycle of the content is controlled by the data this part delivers.
	}

	@Override
	public String getHeader(String name) {
		return _original.getHeader(name);
	}

	@Override
	public Collection<String> getHeaders(String name) {
		return _original.getHeaders(name);
	}

	@Override
	public Collection<String> getHeaderNames() {
		return _original.getHeaderNames();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name: " + getName() + ", file: " + getSubmittedFileName() + "]";
	}

}
