/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.StreamUtilities;

/**
 * Class encapsulating an {@link InputStream} to be used as input and output
 * value in a BinaryMetaAttribute.
 * 
 * Implementations of BinaryData are immutable unless stated otherwise.
 * 
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
@Binding(BinaryDataBinding.class)
public interface BinaryData extends BinaryDataSource, BinaryContent {

	/**
	 * The hash code of {@link BinaryData} is based on size and contents as
	 * follows:
	 * 
	 * <p>
	 * The hash code of a {@link BinaryData} is defined to be the
	 * {@link Long#hashCode()} of {@link #getSize()} combined with binary
	 * xor with the hash code of the first 256 bytes of the {@link #getStream()
	 * stream contents}.
	 * </p>
	 * 
	 * <p>
	 * The hash code of the first 256 bytes of the stream contents is built as
	 * follows:
	 * </p>
	 * 
	 * <p>
	 * The stream is read in 64 blocks of 4 bytes building <code>int</code>
	 * values using little-endian byte order. Those <code>int</code> values are
	 * combined using xor.
	 * </p>
	 * 
	 * @see AbstractBinaryData#hashCodeBinaryData(BinaryData)
	 */
	@Override
	public int hashCode();

	/**
	 * Equality of {@link BinaryData} must be based upon contents.
	 * 
	 * @param obj
	 *        The other object to compare with.
	 * @return Whether the other object is also instance of {@link BinaryData}
	 *         and has the same contents as this instance.
	 *         
	 * @see AbstractBinaryData#equalsBinaryData(BinaryData, Object)
	 */
	@Override
	public boolean equals(Object obj);

	@Override
	default void deliverTo(OutputStream out) throws IOException {
		try (InputStream in = getStream()) {
			StreamUtilities.copyStreamContents(in, out);
		}
	}

	@Override
	default BinaryData toData() {
		return this;
	}

	/**
	 * Casts the given value to a {@link BinaryData}.
	 * 
	 * <p>
	 * Instead of a direct Java cast, this method is preferred because it considers upgrading
	 * {@link BinaryDataSource} to {@link BinaryData}.
	 * </p>
	 */
	public static BinaryData cast(Object value) {
		if (value == null) {
			return null;
		}
		return ((BinaryDataSource) value).toData();
	}
}
