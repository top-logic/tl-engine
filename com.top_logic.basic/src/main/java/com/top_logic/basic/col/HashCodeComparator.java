/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Comparator;

/**
 * {@link Comparator}, that distinguish objects on the base of their {@link Object#hashCode() hash
 * code}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class HashCodeComparator implements Comparator<Object> {

	/** Static instance of {@link HashCodeComparator} */
	public static final HashCodeComparator INSTANCE = new HashCodeComparator();

	private HashCodeComparator() {
		// Singleton instance
	}

	@Override
	public int compare(Object o1, Object o2) {
		return Integer.compare(o1.hashCode(), o2.hashCode());
	}
}
