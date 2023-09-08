/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * {@link Filter} that matches {@link Filter}s accepting a certain object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FilterFilter implements Filter {

	private final Object testObject;

	/**
	 * Creates a {@link Filter} that accepts {@link Filter}, which themselves
	 * accept the given test object.
	 * 
	 * @param testObject
	 *        the object to pass to the {@link Filter#accept(Object)} of
	 *        filtered {@link Filter}s.
	 */
	public FilterFilter(Object testObject) {
		this.testObject = testObject;
	}

	@Override
	public boolean accept(Object anObject) {
		return ((Filter) anObject).accept(testObject);
	}

}
