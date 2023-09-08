/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.col.MutableInteger;

/**
 * A dummy implementation of {@link MultiOptionMatchCounter}, which actually does nothing but
 * returning an empty map of option match counts.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DummyMultiOptionMatchCounter implements MultiOptionMatchCounter {

	/** Singleton instance of {@link DummyMultiOptionMatchCounter} */
	public static final DummyMultiOptionMatchCounter INSTANCE = new DummyMultiOptionMatchCounter();

	@Override
	public void increaseCounter(Object value) {
		// Do nothing
	}

	@Override
	public void markOption(Object value) {
		// Do nothing
	}

	@Override
	public Map<Object, MutableInteger> getMatchCount() {
		return Collections.emptyMap();
	}
}
