/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Comparator;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;

/**
 * Proxy {@link Comparator} that makes a base {@link Comparator} <code>null</code>-safe.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class NullSafeComparator<T> extends ComparatorProxy<T> {

	/**
	 * Configuration for a {@link NullSafeComparator}.
	 * 
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<S> extends ComparatorProxy.Config<S> {

		/**
		 * Whether <code>null</code> values appear before each other values.
		 */
		boolean getNullIsSmallest();

		/**
		 * Setter for {@link #getNullIsSmallest()}.
		 */
		void setNullIsSmallest(boolean value);
	}

	/**
	 * Comparison result of <code>null</code> and an other non-<code>null</code> value.
	 */
	private final int _nullCompareOther;

	/**
	 * Creates a new {@link NullSafeComparator} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link NullSafeComparator}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public NullSafeComparator(InstantiationContext context, Config<? super T> config) throws ConfigurationException {
		super(context, config);
		_nullCompareOther = toNullCompareResult(config.getNullIsSmallest());
	}

	/**
	 * Creates a {@link NullSafeComparator}.
	 * 
	 * @param base
	 *        The delegate {@link Comparator}.
	 * @param nullIsSmallest
	 *        Whether <code>null</code> values appear before each other values.
	 */
	public NullSafeComparator(Comparator<? super T> base, boolean nullIsSmallest) {
		super(base);
		_nullCompareOther = toNullCompareResult(nullIsSmallest);
	}

	private static int toNullCompareResult(boolean nullIsSmallest) {
		return nullIsSmallest ? -1 : 1;
	}

	private static boolean fromNullCompareResult(int nullCompareValue) {
		switch (nullCompareValue) {
			case -1:
				return true;
			case 1:
				return false;
			default:
				throw new IllegalArgumentException("Illegal null compare value:_" + nullCompareValue);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected ComparatorProxy.Config fillConfigForProgrammaticallyCreatedProxy(ComparatorProxy.Config config,
			Comparator impl) {
		((Config<?>) config).setNullIsSmallest(fromNullCompareResult(_nullCompareOther));
		return super.fillConfigForProgrammaticallyCreatedProxy(config, impl);
	}

	@Override
	public int compare(T o1, T o2) {
		if (o1 == null) {
			if (o2 == null) {
				return 0;
			} else {
				return _nullCompareOther;
			}
		} else {
			if (o2 == null) {
				return -_nullCompareOther;
			} else {
				return super.compare(o1, o2);
			}
		}
	}

}
