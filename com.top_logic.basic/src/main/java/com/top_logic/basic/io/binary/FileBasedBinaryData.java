/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link BinaryData} Based on {@link File} content.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class FileBasedBinaryData extends AbstractBinaryData {

	private final File _content;

	private final long _size;

	private final String _contentType;

	private final String _name;

	/**
	 * @see BinaryDataFactory#createBinaryData(File, String, String)
	 */
	protected FileBasedBinaryData(File content, String contentType, String name) {
		_contentType = nonNullContentType(contentType);
		_content = content;
		_name = name;
		_size = _content.length();
    }

	/**
	 * Whether the underlying {@link #getFile()} still exists in the file system.
	 * 
	 * @see #getSize()
	 */
	public boolean exists() {
		return _content.exists();
	}

	/**
	 * Access to the underlying {@link File}.
	 */
	public File getFile() {
		return _content;
	}

	@Override
	public String toString() {
		String path = _content.getPath();
		if (path.equals(_name)) {
			return _name;
		} else {
			return _name + " (" + path + ")";
		}
	}

   /** 
     * Creates a new FileInputStream on the internal File.
     * 
     * @see BinaryData#getStream()
     */
    @Override
	public InputStream getStream() throws IOException {
		return new FileInputStream(_content);
	}
	
	/**
	 * The size of the internal {@link File}.
	 * 
	 * <p>
	 * Note that a cached value is returned. Getting a non <code>0</code> value is no confirmation
	 * that the underlying file still exists, so {@link #getStream()} can be called successfully,
	 * see {@link #exists()}.
	 * </p>
	 * 
	 * @see BinaryData#getSize()
	 */
	@Override
	public long getSize() {
		return _size;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public String getContentType() {
		return _contentType;
	}

}
