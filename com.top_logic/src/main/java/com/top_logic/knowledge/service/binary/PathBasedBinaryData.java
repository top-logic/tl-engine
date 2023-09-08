/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.binary;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.top_logic.basic.io.binary.AbstractBinaryData;
import com.top_logic.dsa.util.MimeTypes;

/**
 * {@link AbstractBinaryData} based on regular {@link Path}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PathBasedBinaryData extends AbstractBinaryData {

	private final Path _path;

	private final String _contentType;

	private final long _size;

	/**
	 * Use the given {@link Path} as basis for later streaming.
	 * 
	 * @param contentType
	 *        Value of {@link #getContentType()}.
	 * @param path
	 *        {@link Path} representing a regular file.
	 * @throws IOException
	 *         When determining size of path fails.
	 */
	public PathBasedBinaryData(String contentType, Path path) throws IOException {
		if (!Files.isRegularFile(path)) {
			throw new IllegalArgumentException("Path " + path + " must be a regular file.");
		}
		_contentType = nonNullContentType(contentType);
		_path = path;
		_size = Files.size(_path);
	}

	/**
	 * Use the given {@link Path} as basis for later streaming. Content type is determined by the
	 * file name.
	 * 
	 * @param path
	 *        {@link Path} representing a regular file.
	 * @throws IOException
	 *         When determining size of path fails.
	 */
	public PathBasedBinaryData(Path path) throws IOException {
		this(resolveContentType(path), path);
	}

	private static String resolveContentType(Path path) {
		if (MimeTypes.Module.INSTANCE.isActive()) {
			return contentTypeByFileName(path);
		} else {
			return CONTENT_TYPE_OCTET_STREAM;
		}
	}

	private static String contentTypeByFileName(Path path) {
		return MimeTypes.getInstance().getMimeType(fileName(path));
	}

	private static String fileName(Path path) {
		// IGNORE FindBugs(NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE): File name is not null, because
		// path is regular.
		return path.getFileName().toString();
	}

	@Override
	public long getSize() {
		return _size;
	}

	@Override
	public String getContentType() {
		return _contentType;
	}

	@Override
	public String getName() {
		return fileName(_path);
	}

	@Override
	public InputStream getStream() throws IOException {
		return Files.newInputStream(_path);
	}

}

