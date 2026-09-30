/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.resource;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;

/**
 * Emits the page-head references (stylesheet links, import map, module scripts) for all registered
 * client resources.
 *
 * <p>
 * Script and stylesheet emission are separate so that the caller can place the import map (which
 * must precede every module script on the page) and the stylesheets (which must follow the
 * design-token block) at the correct positions in the head.
 * </p>
 */
public interface ClientResourceProvider {

	/**
	 * Writes the import map and the module script references.
	 *
	 * @param out
	 *        The writer of the HTML {@code <head>}.
	 * @param contextPath
	 *        The web application context path prefixed to every resource URL.
	 * @throws IOException
	 *         If writing fails.
	 */
	void writeScriptRefs(TagWriter out, String contextPath) throws IOException;

	/**
	 * Writes the stylesheet references.
	 *
	 * @param out
	 *        The writer of the HTML {@code <head>}.
	 * @param contextPath
	 *        The web application context path prefixed to every resource URL.
	 * @throws IOException
	 *         If writing fails.
	 */
	void writeStyleRefs(TagWriter out, String contextPath) throws IOException;

}
