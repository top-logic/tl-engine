/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.print.AttributeException;


/**
 * {@link BinaryData} implemented with byte array contents.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MemoryBinaryData extends AbstractBinaryData {
	
	/** The actual content. */
	private byte[] _data;

	private final String _contentType;
	
	private final String _name;

	private int _offset;

	private int _length;

	/**
	 * @see BinaryDataFactory#createBinaryData(byte[], String, String)
	 */
	protected MemoryBinaryData(byte[] data, String contentType, String name) {
		this(data, 0, data.length, contentType, name);
	}

	/**
	 * @see BinaryDataFactory#createBinaryData(byte[], int, int, String, String)
	 */
	protected MemoryBinaryData(byte[] data, int offset, int length, String contentType, String name) {
		if (length < 0 || offset + length > data.length) {
			throw new IllegalArgumentException(
				"Invalid range [" + offset + ", " + (offset + length) + "), not within [0, " + data.length + ")");
		}
		_data = data;
		_offset = offset;
		_length = length;
		_contentType = nonNullContentType(contentType);
		_name = name;
    }

   	/**
	 * Creates a new ByteArrayInputStream based on {@link #_data}.
	 * 
	 * @see BinaryData#getStream()
	 */
    @Override
	public InputStream getStream() {
		return new ByteArrayInputStream(_data, _offset, _length);
	}
	
	/**
	 * the size of {@link #_data}.
	 * @throws AttributeException
	 *         is not thrown in this implementation
	 * 
	 * @see BinaryData#getSize()
	 */
	@Override
	public long getSize() {
		return _length;
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
