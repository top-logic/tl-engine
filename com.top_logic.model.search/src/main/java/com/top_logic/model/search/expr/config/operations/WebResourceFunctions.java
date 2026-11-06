/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.net.URLConnection;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSourceProxy;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions reading the resources of the web application.
 *
 * <p>
 * A resource is a file of the web application, for example an image, a template or a
 * configuration file. Its path is relative to the root of the web application, such as
 * <code>/images/logo.png</code>. A resource provided by several modules of the application is read
 * in its effective version, the one of the module that overrides all others.
 * </p>
 *
 * <p>
 * The resources are read-only. Only resources of the web application are accessible, no other files
 * of the server.
 * </p>
 */
@ScriptPrefix("web")
public class WebResourceFunctions extends TLScriptFunctions {

	/**
	 * Separator of the segments of a resource path.
	 */
	public static final String PATH_SEPARATOR = "/";

	/**
	 * Path segment naming the parent directory, which is not allowed in a resource path.
	 */
	public static final String PARENT_SEGMENT = "..";

	/**
	 * Character not allowed in a resource path.
	 */
	public static final char BACKSLASH = '\\';

	/**
	 * Path prefix that makes the {@link FileManager} resolve a name in the file system of the server
	 * instead of the web application, which is not allowed in a resource path.
	 */
	public static final String DIRECT_FILE_PREFIX = "file://";

	/**
	 * The resource of the web application with the given path as binary value.
	 *
	 * <p>
	 * The name of the result is the file name of the resource, its content type is determined from
	 * the file name extension, for example <code>image/svg+xml</code> for an SVG image. An extension
	 * the MIME type mappings of the application do not know is looked up in the MIME types of the
	 * Java runtime; if it is unknown there too, the content type is
	 * {@link BinaryData#CONTENT_TYPE_OCTET_STREAM}.
	 * </p>
	 *
	 * <p>
	 * The path must start with {@link #PATH_SEPARATOR}; it must neither contain a segment
	 * {@link #PARENT_SEGMENT}, nor a backslash, nor start with {@link #DIRECT_FILE_PREFIX}.
	 * In a script entered interactively, only an administrator may call this function.
	 * </p>
	 *
	 * <p>
	 * Example: <code>webResource("/images/logo.png")</code>
	 * </p>
	 *
	 * @param path
	 *        The path of the resource relative to the root of the web application.
	 * @return The contents of the resource, or <code>null</code> if no path is given or the
	 *         resource does not exist.
	 */
	@Label("Resource of the web application")
	@AdminOnly
	@SideEffectFree
	public static BinaryData resource(@Mandatory String path) {
		if (path == null) {
			return null;
		}
		checkPath(path);

		BinaryData data = FileManager.getInstance().getDataOrNull(path);
		if (data == null) {
			return null;
		}

		String fileName = path.substring(path.lastIndexOf(PATH_SEPARATOR) + PATH_SEPARATOR.length());
		String contentType = contentType(data, fileName);
		return new BinaryDataSourceProxy(data) {
			@Override
			public String getName() {
				return fileName;
			}

			@Override
			public String getContentType() {
				return contentType;
			}
		};
	}

	private static String contentType(BinaryData data, String fileName) {
		String contentType = data.getContentType();
		if (StringServices.isEmpty(contentType) || BinaryData.CONTENT_TYPE_OCTET_STREAM.equals(contentType)) {
			String guessed = URLConnection.guessContentTypeFromName(fileName);
			if (guessed != null) {
				return guessed;
			}
			return BinaryData.CONTENT_TYPE_OCTET_STREAM;
		}
		return contentType;
	}

	private static void checkPath(String path) {
		if (!path.startsWith(PATH_SEPARATOR)
			|| path.startsWith(DIRECT_FILE_PREFIX)
			|| path.indexOf(BACKSLASH) >= 0
			|| hasParentSegment(path)) {
			throw new TopLogicException(I18NConstants.ERROR_INVALID_WEB_RESOURCE_PATH__PATH.fill(path));
		}
	}

	private static boolean hasParentSegment(String path) {
		for (String segment : path.split(PATH_SEPARATOR, -1)) {
			if (PARENT_SEGMENT.equals(segment)) {
				return true;
			}
		}
		return false;
	}

}
