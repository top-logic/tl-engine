/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import static com.top_logic.layout.table.filter.SingleEmptyValueMatchCounter.*;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.col.MutableInteger;

/**
 * {@link MultiOptionMatchCounter}, that does not really count, but returning always empty values
 * for all options.
 * 
 * @see SingleEmptyValueMatchCounter#EMPTY_VALUE
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class MultiEmptyValueOptionMatchCounter implements MultiOptionMatchCounter {

	private Map<Object, MutableInteger> matchCounter;

	/**
	 * Create a new {@link MultiEmptyValueOptionMatchCounter}
	 */
	public MultiEmptyValueOptionMatchCounter() {
		matchCounter = new HashMap<>();
	}

	@Override
	public void increaseCounter(Object value) {
		// Do nothing
	}

	@Override
	public void markOption(Object value) {
		if (!matchCounter.containsKey(value)) {
			matchCounter.put(value, new MutableInteger(EMPTY_VALUE));
		}
	}

	@Override
	public Map<Object, MutableInteger> getMatchCount() {
		return matchCounter;
	}
}
