/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.util;

import java.util.Comparator;

import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.knowledge.wrap.list.FastListElementComparator;
import com.top_logic.reporting.flex.chart.config.util.ToStringText.NotSetText;
import com.top_logic.util.TLCollator;

/**
 * {@link Comparator} handling {@link NotSetText} especially (sorting it to the end).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KeyCompare<T extends Comparable<T>> implements Comparator<T> {

	/**
	 * Singleton {@link KeyCompare} instance.
	 */
	@SuppressWarnings("rawtypes")
	public static final KeyCompare INSTANCE = new KeyCompare();

	private KeyCompare() {
		// Singleton constructor.
	}

	@Override
	public int compare(T o1, T o2) {
		if (o1 instanceof NotSetText) {
			if (o2 instanceof NotSetText) {
				return o1.compareTo(o2);
			} else {
				return 1;
			}
		} else {
			if (o2 instanceof NotSetText) {
				return -1;
			} else if (o1 instanceof FastListElement && o2 instanceof FastListElement) {
				return new FastListElementComparator(new TLCollator()).compare((FastListElement)o1, (FastListElement)o2);
			} else {
				// Use Comparable comparator as fallback, due to key values can be null (e.g. usage
				// of TreeAxisTimeSeriesDataSetBuilder und enabled shortening of available
				// options).
				return ComparableComparator.INSTANCE.compare(o1, o2);
			}
		}
	}

}
