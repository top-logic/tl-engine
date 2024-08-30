/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.mime;

/**
 * Constants for <a href="https://en.wikipedia.org/wiki/MIME_type">MIME types</a>.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class MimeTypesConstants {

	/**
	 * XML not easily readable by humans.
	 * <p>
	 * See <a href=
	 * "https://stackoverflow.com/questions/4832357/whats-the-difference-between-text-xml-vs-application-xml-for-webservice-respons">Stack
	 * Overflow</a> for the difference between {@value #APPLICATION_XML} and {@value #TEXT_XML}.
	 * </p>
	 */
	public static final String APPLICATION_XML = "application/xml";

	/** XML easily readable for humans. */
	public static final String TEXT_XML = "text/xml";

	/** HTML */
	public static final String HTML = "text/html";

}
