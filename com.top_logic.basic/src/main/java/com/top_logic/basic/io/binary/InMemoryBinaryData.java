/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * Memory optimised class that serves as {@link OutputStream} and as {@link BinaryData}.
 * 
 * <p>
 * The class itself is a {@link ByteArrayOutputStream} whose content is used as input for the
 * {@link BinaryData#getStream() input stream} of the {@link BinaryData} aspect.
 * </p>
 * 
 * <p>
 * Changes using the {@link ByteArrayInputStream} such as {@link #reset()} or {@link #write(byte[])}
 * reflect to the {@link #getStream()} and {@link #getSize()}.
 * </p>
 * 
 * <p>
 * This class is not thread safe.
 * </p>
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InMemoryBinaryData extends ByteArrayStream implements BinaryData {

	private final String _contentType;

	private String _name;

	/**
	 * Creates a new {@link InMemoryBinaryData}.
	 * 
	 * @param contentType
	 *        See {@link #getContentType()}
	 */
	public InMemoryBinaryData(String contentType) {
		this(contentType, NO_NAME);
	}

	/**
	 * Creates a {@link InMemoryBinaryData} with content type and name.
	 *
	 * @param contentType
	 *        See {@link #getContentType()}.
	 * @param name
	 *        See {@link #getName()}.
	 */
	public InMemoryBinaryData(String contentType, String name) {
		_contentType = AbstractBinaryData.nonNullContentType(contentType);
		_name = name;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public synchronized String toString() {
		return getName();
	}

	@Override
	public long getSize() {
		return count;
	}

	@Override
	public int hashCode() {
		return AbstractBinaryData.hashCodeBinaryData(this);
	}

	@Override
	public boolean equals(Object anOther) {
		return AbstractBinaryData.equalsBinaryData(this, anOther);
	}

	@Override
	public String getContentType() {
		return _contentType;
	}

}
