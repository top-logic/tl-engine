/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.basic.io.binary;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * {@link BinaryData} decoding a Base64 encoded byte stream.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Base64BinaryData extends AbstractBinaryData {
	private final String _name;

	private final String _data;

	private final String _contentType;

	private final int _size;

	/**
	 * Creates a new {@link Base64BinaryData}.
	 * 
	 * @param name
	 *        Name for the binary data. See {@link #getName()}.
	 * @param data
	 *        Base64 encoded binary data.
	 * @param contentType
	 *        Content type of the encoded data. See {@link #getContentType()}.
	 */
	public Base64BinaryData(String name, String data, String contentType) {
		_name = name;
		_data = data;
		_size = calculateDecodedLength(data);
		_contentType = contentType;
	}

	private static int calculateDecodedLength(String base64) {
		int len = base64.length();
		if (len == 0) {
			return 0;
		}
		if (len % 4 != 0) {
			// Actually invalid data.
			return -1;
		}
		int padding = 0;
		if (base64.charAt(len - 1) == '=') {
			padding++;
			if (base64.charAt(len - 2) == '=') {
				padding++;
			}
		}
		return (len * 3) / 4 - padding;
	}

	@Override
	public InputStream getStream() throws IOException {
		return Base64.getDecoder().wrap(new ASCIISource(_data));
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public long getSize() {
		return _size;
	}

	@Override
	public String getContentType() {
		return _contentType;
	}

	/**
	 * Creates an {@link InputStream} delivering the decoded bytes from a Base64 encoded string.
	 * 
	 * @param base64
	 *        The Base64 encoded string.
	 */
	public static InputStream decodeBase64(String base64) {
		return Base64.getDecoder().wrap(new ASCIISource(base64));
	}

	/**
	 * String buffer accepting binary data in ASCII encoding scheme.
	 */
	private static final class ASCIISource extends InputStream {
		private final String _input;

		private int _pos;

		/**
		 * Creates a {@link ASCIISource}.
		 *
		 * @param input
		 *        The ASCII value to read.
		 */
		public ASCIISource(String input) {
			_input = input;
			_pos = 0;
		}

		@Override
		public int read() throws IOException {
			if (_pos >= _input.length()) {
				return -1;
			}
			return _input.charAt(_pos++) & 0xFF;
		}
	}

}
