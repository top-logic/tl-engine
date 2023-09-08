/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.export;

/**
 * Partial algorithm for filling a {@link PreloadBuilder}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface PreloadContribution {

	/**
	 * Adds the some {@link PreloadOperation}s to the given {@link PreloadBuilder}.
	 * 
	 * @param preloadBuilder
	 *        The {@link PreloadBuilder} to contribute to.
	 * 
	 * @see PreloadBuilder#addPreload(PreloadOperation)
	 */
	void contribute(PreloadBuilder preloadBuilder);

}
