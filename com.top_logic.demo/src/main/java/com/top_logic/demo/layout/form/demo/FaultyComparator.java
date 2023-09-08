/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import java.util.Comparator;

/**
 * @author     <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public final class FaultyComparator implements Comparator<Object> {

	/** Singleton {@link FaultyComparator} instance. */
	public static final FaultyComparator INSTANCE = new FaultyComparator();

	private FaultyComparator() {
		// singleton instance
	}

	@Override
	public int compare(Object o1, Object o2) {
		throw new UnsupportedOperationException("Faulty comparator");
	}

}
