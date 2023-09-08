/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.util.Comparator;

/**
 * Comparator for {@link Named}.
 * 
 * <p>
 * This comparator compares {@link Named} by comparing the corresponding {@link Named#getName()
 * names}. <code>null</code> values are permitted, {@link Named} with <code>null</code> as name are
 * allowed.
 * </p>
 * 
 * @see NullLast
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NamedComparator implements Comparator<Named> {

	/** Singleton {@link NamedComparator} instance. */
	public static final NamedComparator INSTANCE = new NamedComparator();

	/**
	 * Creates a new {@link NamedComparator}.
	 */
	protected NamedComparator() {
		// Singleton instance
	}

	/**
	 * Compares the {@link Named#getName() names} of the given {@link Named}.
	 * 
	 * <p>
	 * Input elements with <code>null</code> in {@link Named#getName()} are ordered before other
	 * {@link Named}.
	 * </p>
	 * 
	 * <p>
	 * <code>null</code> as input {@link Named} is not allowed.
	 * </p>
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Named o1, Named o2) {
		String o1Name = o1.getName();
		String o2Name = o2.getName();
		if (o1Name == null) {
			return o2Name == null ? 0 : (nullFirst() ? -1 : 1);
		} else if (o2Name == null) {
			return nullFirst() ? 1 : -1;
		} else {
			return o1Name.compareTo(o2Name);
		}
	}

	/**
	 * Whether <code>null</code> must occur before non-<code>null</code> values.
	 */
	protected boolean nullFirst() {
		return true;
	}

	/**
	 * {@link NamedComparator} that orders {@link Named} with <code>null</code>
	 * {@link Named#getName() name} after non-<code>null</code> values.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class NullLast extends NamedComparator {

		/** Singleton {@link NullLast} instance. */
		@SuppressWarnings("hiding")
		public static final NullLast INSTANCE = new NullLast();

		/**
		 * Creates a new {@link NullLast}.
		 */
		protected NullLast() {
			// singleton instance
		}

		@Override
		protected boolean nullFirst() {
			return false;
		}

	}

}
