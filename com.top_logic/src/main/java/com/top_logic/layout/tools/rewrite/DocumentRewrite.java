/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.rewrite;

import org.w3c.dom.Document;

import com.top_logic.basic.Log;

/**
 * Algorithm transforming a {@link Document}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DocumentRewrite {

	/**
	 * Initializes the rewrite engine with a {@link Log} for error reporting.
	 * 
	 * @param log
	 *        The {@link Log} to use for future {@link #rewrite(Document)} operations.
	 */
	void init(Log log);

	/**
	 * Transforms the given {@link Document}.
	 * 
	 * @param document
	 *        The {@link Document} to modify.
	 * @return Whether some changes are made.
	 */
	boolean rewrite(Document document);

}
