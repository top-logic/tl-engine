/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col.filter.typed;

import junit.framework.TestCase;

import com.top_logic.basic.col.filter.typed.FilterResult;

/**
 * Test for {@link FilterResult}
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestFilterResult extends TestCase {

	public void testFromBoolean() {
		assertSame(FilterResult.TRUE, FilterResult.valueOf(Boolean.TRUE));
		assertSame(FilterResult.FALSE, FilterResult.valueOf(Boolean.FALSE));
		assertSame(FilterResult.INAPPLICABLE, FilterResult.valueOf((Boolean) null));
	}

	public void testToBoolean() {
		assertSame(Boolean.TRUE, FilterResult.TRUE.toBoolean());
		assertSame(Boolean.FALSE, FilterResult.FALSE.toBoolean());
		assertSame(null, FilterResult.INAPPLICABLE.toBoolean());
	}

	public void testValueOf() {
		assertSame(FilterResult.TRUE, FilterResult.valueOf(true));
		assertSame(FilterResult.FALSE, FilterResult.valueOf(false));
	}

}

