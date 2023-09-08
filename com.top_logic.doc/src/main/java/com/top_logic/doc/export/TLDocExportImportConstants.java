/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.export;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import com.top_logic.doc.model.Page;

/**
 * Constants for the export and import.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface TLDocExportImportConstants {

	/**
	 * The root directory resource, where help files are loaded from.
	 */
	String ROOT_PATH = "doc";

	/**
	 * The relative root directory resource, where help files are loaded from.
	 * 
	 * <p>
	 * The resource name must start with a '/' character and is resolved relative to the web
	 * application root directory.
	 * </p>
	 */
	String ROOT_RELATIVE_PATH = "/" + ROOT_PATH;

	/**
	 * Maximum safe size for file names.
	 * <p>
	 * The names of files and folders are typically restricted to 255 bytes. But they can contain
	 * unicode characters that require up to 6 bytes, depending on the character set. This limit
	 * should prevent file names that are too long.
	 * </p>
	 */
	int FILE_NAME_LIMIT = 255 / 6;

	/** The {@link Charset character set} of documentation pages. */
	Charset CHARACTER_SET = StandardCharsets.UTF_8;

	/**
	 * The name of the file containing the {@link Page#getContent()}.
	 * <p>
	 * These files contain only HTML fragments: Inserted between <code>body</code> tags, they are
	 * valid HTML.
	 * </p>
	 */
	String CONTENT_FILE_NAME = "index.html";

	/** Name of the properties file with information about the {@link Page} */
	String PROPERTIES_FILE_NAME = "page.properties";

	/** Title property of a {@link Page} */
	String PROPERTIES_TITLE = "title";

	/** UUID property of a {@link Page} */
	String PROPERTIES_UUID = "uuid";

	/** Position property of a {@link Page} */
	String PROPERTIES_POSITION = "position";

	/** {@link Page#getImportSource() Import source} property of a {@link Page}. */
	String PROPERTIES_SOURCE_BUNDLE = "source";

	/**
	 * The characters that must not be used in a file name.
	 * <p>
	 * Contains not just those characters forbidden by Windows, but the "." too. That is necessary
	 * to prevent directory traversal.
	 * </p>
	 */
	Pattern INVALID_FILE_NAME_CHARACTERS = Pattern.compile("[.\\/:*?\"<>|]");

	/** Replacement for characters in filenames that are not allowed on every file system. */
	char FILE_NAME_REPLACEMENT = '_';

}
