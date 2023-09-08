/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.IOException;
import java.io.OutputStream;

import com.top_logic.basic.Named;
import com.top_logic.basic.config.annotation.Label;

/**
 * Binary data that is actively produced to a given stream.
 * 
 * @see #deliverTo(OutputStream)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Binary data")
public interface BinaryDataSource extends Named {

	/**
	 * Content type {@value #CONTENT_TYPE_OCTET_STREAM}. This constant is the content type of a
	 * {@link BinaryData} if the actual content type can not not be specified better.
	 */
	String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";

	/**
	 * The exact size of the data in bytes, or <code>-1</code> if not known.
	 * 
	 * <p>
	 * Be aware that due to different encodings this may not be equal to the number of characters
	 * when read as text.
	 * </p>
	 */
	public long getSize();

	/**
	 * Returns the content type of the file.
	 * 
	 * @return never <code>null</code>
	 */
	String getContentType();

	/**
	 * Delivers the represented data to the given stream.
	 *
	 * @param out
	 *        The {@link OutputStream} to write data to.
	 * @throws IOException
	 *         If writing to the given stream fails.
	 */
	void deliverTo(OutputStream out) throws IOException;

	/**
	 * Upgrades this {@link BinaryDataSource} to a full {@link BinaryData}.
	 * 
	 * <p>
	 * Note: Using the resulting {@link BinaryData#getStream()} API might be much less efficient
	 * than {@link #deliverTo(OutputStream)}.
	 * </p>
	 */
	default BinaryData toData() {
		return StreamIOConverter.toData(this);
	}

}
