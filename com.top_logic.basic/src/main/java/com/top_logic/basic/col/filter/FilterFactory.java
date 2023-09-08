/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import java.util.Collection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;

/**
 * Factory facade for creating {@link Filter} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FilterFactory {

	/**
	 * The {@link Filter} accepting all objects.
	 */
	public static Filter<Object> trueFilter() {
		return TrueFilter.INSTANCE;
	}

	/**
	 * The {@link Filter} accepting no objects.
	 */
	public static Filter<Object> falseFilter() {
		return FalseFilter.INSTANCE;
	}

	/**
	 * Creates a {@link Filter} that accepts if an object is accepted by all of
	 * the given filters.
	 * 
	 * <p>
	 * If the list of filters is empty, the resulting filter accepts all
	 * objects.
	 * </p>
	 */
	public static <T> Filter<? super T> and(Collection<? extends Filter<? super T>> filters) {
		switch (filters.size()) {
			case 0:
				return FilterFactory.trueFilter();
			case 1:
				return CollectionUtil.getFirst(filters);
			default:
				Filter<? super T> result = trueFilter();
				for (Filter<? super T> current : filters) {
					// must be set type explicit as otherwise javac can not determine correct type
					result = FilterFactory.<T>and(result, current);
					if (isFalse(result)) {
						return falseFilter();
					}
				}
				return result;
		}
	}

	/**
	 * Returns a {@link Filter} which accepts an object iff all filters in the
	 * given array accepts it.
	 * 
	 * @param filters
	 *        a non <code>null</code> array of non <code>null</code>
	 *        {@link Filter}.
	 * 
	 * @return never <code>null</code>
	 */
	public static <T> Filter<? super T> and(Filter<? super T>... filters) {
		switch (filters.length) {
			case 0:
				return FilterFactory.trueFilter();
			case 1:
				return filters[0];
			case 2:
				// must be set type explicit as otherwise javac can not determine correct type
				return FilterFactory.<T>and(filters[0], filters[1]); 
			default:
				Filter<? super T> result = filters[0];
				for (int i = 1; i < filters.length; i++) {
					// must be set type explicit as otherwise javac can not determine correct type
					result = FilterFactory.<T>and(result, filters[i]);
					if (isFalse(result)) {
						return falseFilter();
					}
				}
				return result;
		}
	}

	/**
	 * Returns a {@link Filter} which accepts an object iff the first and the
	 * second filter accepts it.
	 * 
	 * @param first
	 *        a non <code>null</code> {@link Filter}.
	 * @param second
	 *        a non <code>null</code> {@link Filter}.
	 * 
	 * @return never <code>null</code>
	 */
	public static <T> Filter<? super T> and(Filter<? super T> first, Filter<? super T> second) {
		if (isFalse(first) || isFalse(second)) {
			return falseFilter();
		}
		if (isTrue(first)) {
			return second;
		}
		if (isTrue(second)) {
			return first;
		}
		return new ANDFilter<>(first, second);

	}

	/**
	 * Creates a {@link Filter} that accepts if an object is accepted by any of
	 * the given filters.
	 * 
	 * <p>
	 * If the list of filters is empty, the resulting filter accepts no objects.
	 * </p>
	 */
	public static <T> Filter<? super T> or(Collection<? extends Filter<? super T>> filters) {
		switch (filters.size()) {
			case 0:
				return FilterFactory.falseFilter();
			case 1:
				return CollectionUtil.getFirst(filters);
			default:
				Filter<? super T> result = falseFilter();
				for (Filter<? super T> current : filters) {
					// must be set type explicit as otherwise javac can not determine correct type
					result = FilterFactory.<T>or(result, current);
					if (isTrue(result)) {
						return trueFilter();
					}
				}
				return result;
		}
	}

	/**
	 * Returns a {@link Filter} which accepts an object iff some filter in the
	 * given array accepts it.
	 * 
	 * @param filters
	 *        a non <code>null</code> array of non <code>null</code>
	 *        {@link Filter}.
	 * 
	 * @return never <code>null</code>
	 */
	public static <T> Filter<? super T> or(Filter<? super T>... filters) {
		switch (filters.length) {
			case 0:
				return FilterFactory.falseFilter();
			case 1:
				return filters[0];
			case 2:
				// must be set type explicit as otherwise javac can not determine correct type
				return FilterFactory.<T>or(filters[0], filters[1]);
			default:
				Filter<? super T> result = filters[0];
				for (int i = 1; i < filters.length; i++) {
					// must be set type explicit as otherwise javac can not determine correct type
					result = FilterFactory.<T>or(result, filters[i]);
					if (isTrue(result)) {
						return trueFilter();
					}
				}
				return result;

		}
	}

	/**
	 * Returns a {@link Filter} which accepts an object iff the first or the
	 * second filter accepts it.
	 * 
	 * @param first
	 *        a non <code>null</code> {@link Filter}.
	 * @param second
	 *        a non <code>null</code> {@link Filter}.
	 * 
	 * @return never <code>null</code>
	 */
	public static <T> Filter<? super T> or(Filter<? super T> first, Filter<? super T> second) {
		if (isTrue(first) || isTrue(second)) {
			return trueFilter();
		}
		if (isFalse(first)) {
			return second;
		}
		if (isFalse(second)) {
			return first;
		}
		return new ORFilter<>(first, second);
	}

	/**
	 * Create the inverted {@link Filter}.
	 */
	public static <T> Filter<? super T> not(Filter<? super T> filter) {
		if (isTrue(filter)) {
			return FilterFactory.falseFilter();
		}
		if (isFalse(filter)) {
			return FilterFactory.trueFilter();
		}
		if (filter instanceof NOTFilter<?>) {
			return ((NOTFilter<? super T>) filter).getInnerFilter();
		}
		return new NOTFilter<>(filter);
	}

	/**
	 * Whether the given {@link Filter} is the {@link TrueFilter}.
	 */
	public static boolean isTrue(Filter<?> filter) {
		return filter == FilterFactory.trueFilter();
	}

	/**
	 * Whether the given {@link Filter} is the {@link FalseFilter}.
	 */
	public static boolean isFalse(Filter<?> filter) {
		return filter == FilterFactory.falseFilter();
	}

	/**
	 * Interprets the given value as {@link FalseFilter}, if <code>null</code>.
	 */
	public static <T> Filter<? super T> nullAsFalse(Filter<T> filterOrNull) {
		if (filterOrNull == null) {
			return FilterFactory.falseFilter();
		} else {
			return filterOrNull;
		}
	}

	/**
	 * Interprets the given value as {@link TrueFilter}, if <code>null</code>.
	 */
	public static <T> Filter<? super T> nullAsTrue(Filter<T> filterOrNull) {
		if (filterOrNull == null) {
			return FilterFactory.trueFilter();
		} else {
			return filterOrNull;
		}
	}

	/**
	 * Creates a {@link Filter} which first checks that the object is not
	 * <code>null</code>, has the correct type, and then delegates to the given
	 * <code>innerFilter</code>.
	 * 
	 * @param <T>
	 *        the type of objects the inner filter can process
	 * @param testClass
	 *        the class of the type of objects the inner filter can process
	 * @param innerFilter
	 *        the filter to delegate to
	 */
	public static <T> Filter<? super Object> createClassFilter(Class<? extends T> testClass, final Filter<? super T> innerFilter) {
		return new ClassFilter<T>(testClass, innerFilter);
	}

	/**
	 * Resolves the given class and delegates to {@link #createClassFilter(Class)}
	 * 
	 * @see #createClassFilter(Class)
	 */
	public static Filter<? super Object> createClassFilter(String className) throws ClassNotFoundException {
		return createClassFilter(Class.forName(className));
	}

	/**
	 * Creates a {@link Filter} which checks that the object is not
	 * <code>null</code> and has the correct type
	 * 
	 * @param testClass
	 *        the class of the type of objects the inner filter can process
	 */
	public static Filter<? super Object> createClassFilter(Class<?> testClass) {
		return createClassFilter(testClass, trueFilter());
	}

}
