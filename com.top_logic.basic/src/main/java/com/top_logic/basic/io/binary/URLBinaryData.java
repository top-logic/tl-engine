/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import com.top_logic.basic.io.StreamUtilities;

/**
 * {@link BinaryData} directly based on an {@link URL}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class URLBinaryData extends AbstractBinaryData {

	private final URL _url;

	private long _size = -1;

	/**
	 * Creates a {@link URLBinaryData}.
	 * 
	 * @param url
	 *        The {@link URL} to take the contents from.
	 */
	public URLBinaryData(URL url) {
		_url = url;
	}

	@Override
	public InputStream getStream() throws IOException {
		return _url.openStream();
	}

	@Override
	public long getSize() {
		if (_size < 0) {
			long size;
			try {
				size = countSize();
			} catch (IOException ex) {
				throw new RuntimeException("Cannot compute size.", ex);
			}
			_size = size;
		}
		return _size;
	}

	private long countSize() throws IOException {
		try (InputStream stream = getStream()) {
			return StreamUtilities.size(stream);
		}
	}

	@Override
	public String getName() {
		return _url.toExternalForm();
	}

	@Override
	public String getContentType() {
		try {
			URLConnection connection = _url.openConnection();
			return nonNullContentType(connection.getContentType());
		} catch (IOException ex) {
			return CONTENT_TYPE_OCTET_STREAM;
		}
	}

}
