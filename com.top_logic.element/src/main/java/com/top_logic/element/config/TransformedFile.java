/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.top_logic.basic.io.BinaryContent;

/**
 * {@link File} based {@link BinaryContent} with an explicit name.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TransformedFile implements BinaryContent {

	private final File _file;

	private final String _name;

	/**
	 * Creates a {@link TransformedFile}.
	 * 
	 * @param file
	 *        The {@link File} to read.
	 * @param name
	 *        The explicit name of the file resource.
	 */
	public TransformedFile(File file, String name) {
		_file = file;
		_name = name;
	}

	@Override
	public InputStream getStream() throws IOException {
		return new FileInputStream(_file);
	}

	@Override
	public String toString() {
		return _name;
	}

}
