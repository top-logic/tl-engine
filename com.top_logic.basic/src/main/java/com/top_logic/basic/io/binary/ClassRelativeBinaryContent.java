/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.top_logic.basic.io.BinaryContent;

/**
 * A {@link BinaryContent} that uses {@link Class#getResourceAsStream(String)}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class ClassRelativeBinaryContent implements BinaryContent {

	private final Class<?> _class;

	private final String _filename;


	/**
	 * Creates a {@link BinaryContent}.
	 * 
	 * @param contextClass
	 *        Is not allowed to be <code>null</code>.
	 * @param filename
	 *        Is not allowed to be <code>null</code>. Is allowed to be empty.
	 */
	public ClassRelativeBinaryContent(Class<?> contextClass, String filename) {
		checkParams(contextClass, filename);
		_class = contextClass;
		_filename = filename;
	}

	private static String buildName(Class<?> contextClass, String filename) {
		return "class:" + contextClass.getName() + "/" + filename;
	}

	private void checkParams(Class<?> contextClass, String filename) {
		if (contextClass == null) {
			throw new NullPointerException("Context class is not allowed to be null.");
		}
		if (filename == null) {
			throw new NullPointerException("filename is not allowed to be null.");
		}
	}

	@Override
	public InputStream getStream() throws IOException {
		InputStream result = _class.getResourceAsStream(_filename);
		if (result == null) {
			throw new FileNotFoundException("Resource '" + _filename + "' does not exist relative to " + _class + ".");
		}
		return result;
	}
	
	@Override
	public String toString() {
		return buildName(_class, _filename);
	}

	/**
	 * Creates a {@link ClassRelativeBinaryContent} with given context class, where the file name is
	 * the simple name of the context class followed by '.' and given suffix.
	 */
	public static ClassRelativeBinaryContent withSuffix(Class<?> contextClass, String suffix) {
		return new ClassRelativeBinaryContent(contextClass, contextClass.getSimpleName() + '.' + suffix);
	}

}
