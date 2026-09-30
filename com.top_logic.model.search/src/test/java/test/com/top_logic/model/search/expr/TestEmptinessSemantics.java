/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import junit.framework.TestCase;

import com.top_logic.basic.util.WithEmptiness;
import com.top_logic.model.search.expr.IsEmpty;
import com.top_logic.model.search.expr.IsEqual;

/**
 * Test for the TL-Script emptiness semantics of {@link WithEmptiness} values.
 *
 * <p>
 * A {@link WithEmptiness} value that reports itself empty must behave like <code>null</code> and the
 * empty string, both for <code>isEmpty()</code> ({@link IsEmpty#compute(Object)}) and for equality
 * ({@link IsEqual#equals(Object, Object)}).
 * </p>
 *
 * @author <a href="mailto:bernhard.haumacher@top-logic.com">Bernhard Haumacher</a>
 */
public class TestEmptinessSemantics extends TestCase {

	/**
	 * A minimal {@link WithEmptiness} value with a fixed emptiness state.
	 */
	private static final class Value implements WithEmptiness {

		private final boolean _empty;

		Value(boolean empty) {
			_empty = empty;
		}

		@Override
		public boolean isEmpty() {
			return _empty;
		}
	}

	public void testIsEmptyHonorsWithEmptiness() {
		assertTrue(IsEmpty.compute(new Value(true)));
		assertFalse(IsEmpty.compute(new Value(false)));
	}

	public void testEmptyEqualsNull() {
		assertTrue(IsEqual.equals(new Value(true), null));
		assertTrue(IsEqual.equals(null, new Value(true)));
	}

	public void testEmptyEqualsEmptyString() {
		assertTrue(IsEqual.equals(new Value(true), ""));
		assertTrue(IsEqual.equals("", new Value(true)));
	}

	public void testEmptyEqualsEmpty() {
		assertTrue(IsEqual.equals(new Value(true), new Value(true)));
	}

	public void testNonEmptyNotEqualNull() {
		assertFalse(IsEqual.equals(new Value(false), null));
		assertFalse(IsEqual.equals(null, new Value(false)));
	}

}
