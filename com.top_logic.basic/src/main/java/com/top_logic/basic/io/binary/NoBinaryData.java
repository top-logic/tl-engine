/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@link BinaryData} that must not be accessed.
 * 
 * <p>
 * When accessing the data or content type, an exception is thrown.
 * </p>
 */
public class NoBinaryData extends AbstractBinaryData {

	private final String _name;

	/**
	 * Creates a {@link NoBinaryData}.
	 */
	public NoBinaryData(String name) {
		_name = name;
	}

	@Override
	public long getSize() {
		return 0;
	}

	@Override
	public String getContentType() {
		throw new UnsupportedOperationException("Contents of '" + getName() + "' must be be accessed.");
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public InputStream getStream() throws IOException {
		throw new IOException("Contents of '" + getName() + "' must be be accessed.");
	}

}
