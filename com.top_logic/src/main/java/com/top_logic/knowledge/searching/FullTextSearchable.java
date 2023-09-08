/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;

/**
 * Object that can be indexed in a full-text search engine.
 * 
 * <p>
 * All persistent objects implement this interface. Custom values may implement this interface also
 * to customize their search value representation in the full-text index.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FullTextSearchable {

	/**
	 * Puts all separate values that should be indexed to the given buffer.
	 * 
	 * @param buffer
	 *        The {@link FullTextBuBuffer} to write values that should be indexed to.
	 * 
	 * @see FullTextBuBuffer#add(CharSequence)
	 */
	void generateFullText(FullTextBuBuffer buffer);

}
