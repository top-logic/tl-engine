/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Comparator;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;

/**
 * {@link Comparator} inverting the result of an inner comparator.
 * 
 * @author <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public final class InverseComparator<T> extends ComparatorProxy<T> {

	/**
	 * Creates a {@link InverseComparator} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public InverseComparator(InstantiationContext context, Config<? super T> config) {
		super(context, config);
	}

	/**
	 * Creates a {@link InverseComparator} with the given {@link Comparator} as
	 * inner {@link Comparator}.
	 * 
	 * @param impl
	 *        A inner {@link Comparator}. Must NOT be <code>null</code>.
	 */
	public InverseComparator(Comparator<? super T> impl) {
		super(impl);
	}

	@Override
	public final int compare(T o1, T o2) {
		return super.compare(o2, o1);
	}

}
