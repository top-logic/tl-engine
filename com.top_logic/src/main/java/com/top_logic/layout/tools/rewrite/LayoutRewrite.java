/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.rewrite;

import org.w3c.dom.Document;

/**
 * {@link DocumentRewrite} to rewrite calls of typed layout definitions.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface LayoutRewrite extends DocumentRewrite {

	/**
	 * Service implementation of {@link #rewrite(Document)} that calls
	 * {@link #rewriteLayout(String, String, Document)} without layout key and template.
	 * 
	 * @see com.top_logic.layout.tools.rewrite.DocumentRewrite#rewrite(org.w3c.dom.Document)
	 */
	@Override
	default boolean rewrite(Document document) {
		return rewriteLayout(null, null, document);
	}

	/**
	 * Rewrites the call of a layout template.
	 * 
	 * @param layoutKey
	 *        key under which the layout is stored. May be <code>null</code>.
	 * @param template
	 *        Name of the called template. May be <code>null</code>.
	 * @param document
	 *        The {@link Document} to modify.
	 * @return Whether some changes are made.
	 * 
	 * @see #rewrite(Document)
	 */
	boolean rewriteLayout(String layoutKey, String template, Document document);
}

