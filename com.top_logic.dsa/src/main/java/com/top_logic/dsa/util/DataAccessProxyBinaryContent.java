/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.util;

import java.io.IOException;
import java.io.InputStream;

import com.top_logic.basic.io.BinaryContent;
import com.top_logic.dsa.DataAccessProxy;

/**
 * {@link BinaryContent} based on a {@link DataAccessProxy}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DataAccessProxyBinaryContent implements BinaryContent {

	private final DataAccessProxy _dap;

	/**
	 * Creates a new {@link DataAccessProxyBinaryContent}.
	 * 
	 * @param resourceName
	 *        The resource to create {@link DataAccessProxy} from.
	 */
	public DataAccessProxyBinaryContent(String resourceName) {
		this(new DataAccessProxy(resourceName));
	}

	/**
	 * Creates a new {@link DataAccessProxyBinaryContent}.
	 * 
	 * @param dap
	 *        The {@link DataAccessProxy} to get {@link DataAccessProxy#getEntry()} from.
	 */
	public DataAccessProxyBinaryContent(DataAccessProxy dap) {
		_dap = dap;
	}

	@Override
	public InputStream getStream() throws IOException {
		return _dap.getEntry();
	}

	@Override
	public String toString() {
		return _dap.getPath();
	}

}
