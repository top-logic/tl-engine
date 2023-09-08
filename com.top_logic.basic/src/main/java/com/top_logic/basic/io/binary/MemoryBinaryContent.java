/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.io.BinaryContent;

/**
 * A {@link BinaryContent} for byte arrays or Strings as data sources.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class MemoryBinaryContent implements BinaryContent {

	private final byte[] _data;

	private final String _name;

	/**
	 * Creates a {@link MemoryBinaryContent} from the given byte array.
	 * 
	 * @param data
	 *        Is allowed to be <code>null</code>, which will result in: <code>byte[0]</code>
	 * @param name
	 *        Name of the content.
	 */
	public MemoryBinaryContent(byte[] data, String name) {
		_name = name;
		_data = ArrayUtil.nonNull(data);
	}

	@Override
	public InputStream getStream() throws IOException {
		return new ByteArrayInputStream(_data);
	}

	@Override
	public String toString() {
		return _name;
	}

}
