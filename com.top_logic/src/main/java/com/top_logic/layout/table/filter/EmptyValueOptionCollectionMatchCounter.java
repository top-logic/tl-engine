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
 * {@link MultiOptionMatchCounter}, that does not really count, but collecting all possible options
 * and always returning empty values for all of them.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class EmptyValueOptionCollectionMatchCounter implements MultiOptionMatchCounter {

	private Map<Object, MutableInteger> matchCounter;
	
	/**
	 * Create a new {@link EmptyValueOptionCollectionMatchCounter}.
	 */
	public EmptyValueOptionCollectionMatchCounter() {
		matchCounter = new HashMap<>();
	}

	@Override
	public void increaseCounter(Object value) {
		MutableInteger matchCount = matchCounter.get(value);
		if (matchCount == null) {
			matchCounter.put(value, new MutableInteger(EMPTY_VALUE));
		}
	}

	@Override
	public void markOption(Object value) {
		// Do nothing
	}

	@Override
	public Map<Object, MutableInteger> getMatchCount() {
		return matchCounter;
	}
}
