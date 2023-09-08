/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Map;

import com.top_logic.basic.col.MutableInteger;

/**
 * Match counter of table filters, which have a multiple filter options to choose from.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface MultiOptionMatchCounter {

	/**
	 * Increases the counter by 1 for options, which are equal to the given value.
	 * 
	 */
	void increaseCounter(Object value);

	/**
	 * Add options, which are equal to the given value, to the index of existing column options.
	 */
	void markOption(Object value);

	/**
	 * the match count of the filter options.
	 */
	Map<Object, MutableInteger> getMatchCount();
}