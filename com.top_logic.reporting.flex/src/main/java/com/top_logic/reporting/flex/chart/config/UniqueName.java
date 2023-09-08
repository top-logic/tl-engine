/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.DefaultLabelProvider;
import com.top_logic.reporting.flex.chart.config.aggregation.AggregationFunction;

/**
 * Util-class to uncouple a internal key and a translation.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class UniqueName implements Comparable<UniqueName> {

	private static final LabelProvider STANDARD_PROVIDER = new LabelProvider() {

		@Override
		public String getLabel(Object object) {
			if (object instanceof AggregationFunction) {
				return ((AggregationFunction) object).getLabel();
			}
			return DefaultLabelProvider.INSTANCE.getLabel(object);
		}
	};

	private final Comparable<? extends Object> _key;

	private LabelProvider _provider;

	/**
	 * Creates a new {@link UniqueName}
	 * 
	 * @param key
	 *        the key to use for comparison.
	 */
	public UniqueName(Comparable<?> key) {
		_key = key;
		_provider = STANDARD_PROVIDER;
	}

	/**
	 * the unique key
	 */
	public Comparable<?> getKey() {
		return _key;
	}

	/**
	 * getter for the {@link LabelProvider} that is used to translate the internal key in
	 *         {@link #toString()}
	 */
	public LabelProvider getProvider() {
		return _provider;
	}

	/**
	 * see {@link #getProvider()}
	 */
	public void setProvider(LabelProvider labelProvider) {
		_provider = labelProvider;
	}

	@Override
	public String toString() {
		return _provider.getLabel(_key);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int compareTo(UniqueName o) {
		Comparable key1 = _key;
		Comparable key2 = o._key;
		Class<? extends Comparable> c1 = key1.getClass();
		Class<? extends Comparable> c2 = key2.getClass();
		if (c1 == c2) {
			return key1.compareTo(key2);
		} else {
			return c1.getName().compareTo(c2.getName());
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof UniqueName) {
			return _key.equals(((UniqueName) obj)._key);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() + _key.hashCode();
	}

}
