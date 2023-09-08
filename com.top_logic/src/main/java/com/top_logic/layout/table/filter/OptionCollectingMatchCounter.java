/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.col.MutableInteger;

/**
 * {@link MultiOptionMatchCounter}, that not only counts options matches but also collects and
 * provides all possible options.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class OptionCollectingMatchCounter implements MultiOptionMatchCounter {
	
	private Map<Object, MutableInteger> matchCounter;
	
	/**
	 * Create a new {@link OptionCollectingMatchCounter}
	 */
	public OptionCollectingMatchCounter() {
		matchCounter = new HashMap<>();
	}

	@Override
	public void increaseCounter(Object value) {
		MutableInteger matchCount = matchCounter.get(value);
		if (matchCount != null) {
			matchCount.inc();
		} else {
			matchCounter.put(value, new MutableInteger(1));
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
