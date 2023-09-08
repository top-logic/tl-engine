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
 * Default implementation of {@link MultiOptionMatchCounter}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DefaultMultiOptionMatchCounter implements MultiOptionMatchCounter {

	private Map<Object, MutableInteger> matchCounter;

	/**
	 * Create a new {@link DefaultMultiOptionMatchCounter}
	 */
	public DefaultMultiOptionMatchCounter() {
		matchCounter = new HashMap<>();
	}

	@Override
	public void increaseCounter(Object value) {
		MutableInteger optionCounter = matchCounter.get(value);
		if (optionCounter != null) {
			optionCounter.inc();
		}
	}

	@Override
	public void markOption(Object value) {
		if (!matchCounter.containsKey(value)) {
			matchCounter.put(value, new MutableInteger(0));
		}
	}

	@Override
	public Map<Object, MutableInteger> getMatchCount() {
		return matchCounter;
	}
}