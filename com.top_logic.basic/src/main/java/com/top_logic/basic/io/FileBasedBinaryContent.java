/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.top_logic.basic.io.binary.LazyNamedBinaryContent;

/**
 * Class which delivers files as streams.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FileBasedBinaryContent extends LazyNamedBinaryContent {

	private final File f;

	private FileBasedBinaryContent(File f) {
		if (f == null) {
			throw new NullPointerException("Can not create InputStream from null-file");
		}
		this.f = f;
	}

	/**
	 * Whether the underlying {@link File} still exists in the file system.
	 */
	public boolean exists() {
		return this.f.exists();
	}

	@Override
	public InputStream getStream() throws IOException {
		return new FileInputStream(f);
	}
	
	/** 
	 * Returns the file this {@link BinaryContent} based on.
	 *  
	 * @since 5.7.3
	 */
	public final File baseFile() {
		return f;
	}
	
	/**
	 * creates a {@link BinaryContent} which delivers the content of the given
	 * file as stream.
	 * 
	 * @param f
	 *        must not be <code>null</code>.
	 */
	public static BinaryContent createBinaryContent(File f) {
		return new FileBasedBinaryContent(f);
	}

	/**
	 * creates a {@link BinaryContent} which delivers the content of the file
	 * with the given name as stream.
	 * 
	 * @param fileName
	 *        name of the file on which the binary content should based.
	 */
	public static BinaryContent createBinaryContent(String fileName) {
		return new FileBasedBinaryContent(new File(fileName));
	}

	@Override
	protected String buildName() {
		return "file:" + FileUtilities.getSafeDetailedPath(f);
	}

}
