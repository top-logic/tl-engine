/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Proxy for a {@link BinaryDataSource}.
 */
public class BinaryDataSourceProxy extends AbstractBinaryData {

	private final BinaryDataSource _impl;

	/**
	 * Creates a {@link BinaryDataSourceProxy}.
	 */
	public BinaryDataSourceProxy(BinaryDataSource impl) {
		_impl = impl;
	}

	@Override
	public String getName() {
		return _impl.getName();
	}

	@Override
	public long getSize() {
		return _impl.getSize();
	}

	@Override
	public String getContentType() {
		return _impl.getContentType();
	}

	@Override
	public void deliverTo(OutputStream out) throws IOException {
		_impl.deliverTo(out);
	}

	@Override
	public InputStream getStream() throws IOException {
		// Make sure that the getStream() of the implementation is used directly, if it also
		// directly implements BinaryData.
		return _impl.toData().getStream();
	}

}
