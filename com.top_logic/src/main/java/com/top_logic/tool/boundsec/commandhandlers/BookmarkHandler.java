/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.util.Map;

import com.top_logic.mig.util.net.URLUtilities;

/**
 * Plugin that can be used to generate and interpret bookmark links.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface BookmarkHandler {

	/**
	 * Appends arguments to the given URL that identify the given object.
	 * 
	 * @param url
	 *        The URL being built.
	 * @param first
	 *        Whether the next URL parameter is the first one.
	 * @param targetObject
	 *        The object to identify.
	 * @return Whether the next URL parameter is still the first one. <code>true</code>, if the
	 *         <code>first</code> argument was <code>true</code> and this call did not append an URL
	 *         argument, <code>false</code> otherwise.
	 * 
	 * @see URLUtilities#appendUrlArg(StringBuilder, boolean, String, String)
	 */
	boolean appendIdentificationArguments(StringBuilder url, boolean first, Object targetObject);

	/**
	 * Computes the object the bookmark is leading to.
	 * 
	 * @param someArguments
	 *        Information of the object.
	 * @return Object of the bookmark.
	 */
	Object getBookmarkObject(Map<String, Object> someArguments);


}
