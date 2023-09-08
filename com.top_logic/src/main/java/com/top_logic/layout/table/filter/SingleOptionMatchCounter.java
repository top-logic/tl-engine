/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

/**
 * Counter of filter option matches of single options, determined during table filter operation.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface SingleOptionMatchCounter {

	/**
	 * Increases the counter by 1.
	 */
	void increaseCounter();

	/**
	 * the match count of the filter option, after what has been filtered for.
	 */
	int getMatchCount();
}