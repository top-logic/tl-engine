/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.IOException;
import java.io.InputStream;

import com.top_logic.basic.io.EmptyInputStream;

/**
 * {@link BinaryData} with zero bytes content.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EmptyBinaryData extends AbstractBinaryData {

	/**
	 * Singleton {@link EmptyBinaryData} instance.
	 */
	public static final EmptyBinaryData INSTANCE = new EmptyBinaryData();

	private EmptyBinaryData() {
		// Singleton constructor.
	}

	@Override
	public long getSize() {
		return 0;
	}

	@Override
	public InputStream getStream() throws IOException {
		return EmptyInputStream.INSTANCE;
	}

	@Override
	public String getName() {
		return getClass().getName();
	}

	@Override
	public String getContentType() {
		return CONTENT_TYPE_OCTET_STREAM;
	}
}
