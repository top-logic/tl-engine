/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Comparator;
import java.util.List;

/**
 * The class {@link ComparatorChain} is a factory to create comparator chains. A
 * comparator chain is a chain of comparators, i.e. if one comparator says that
 * two objects are equal the next comparator in the chain is consulted.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ComparatorChain {

	/**
	 * Comparator chain containing exactly two comparators
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	static final class ComparatorPair<T> implements Comparator<T> {

		private final Comparator<? super T> comp1;
		private final Comparator<? super T> comp2;

		/**
		 * @param comp1
		 *        must not be <code>null</code>
		 * @param comp2
		 *        must not be <code>null</code>
		 */
		public ComparatorPair(Comparator<? super T> comp1, Comparator<? super T> comp2) {
			this.comp1 = comp1;
			this.comp2 = comp2;
		}

		@Override
		public int compare(T o1, T o2) {
			final int result = comp1.compare(o1, o2);
			if (result == 0) {
				return comp2.compare(o1, o2);
			} else {
				return result;
			}
		}

	}

	/**
	 * Comparator that consults a list of comparators in the canonical order
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	static final class ComparatorSequence<T> implements Comparator<T> {

		/**
		 * sequence of comparators to use. <b>Must not be modified</b>
		 * 
		 *  @see ComparatorChain#newComparatorChain(List)
		 */
		private final Comparator<? super T>[] comparators;

		/**
		 * @param comparators
		 *        a non <code>null</code> list of non <code>null</code>
		 *        comparators
		 */
		public ComparatorSequence(Comparator<? super T>[] comparators) {
			this.comparators = comparators;
			assert checkArgument(comparators);
		}

		private static final boolean checkArgument(Comparator<?>[] comparators) {
			if (comparators == null) {
				throw new IllegalArgumentException("comparators must not be null");
			}
			for (int index = 0, size = comparators.length; index < size; index++) {
				Object comp = comparators[index];
				if (comp == null) {
					throw new IllegalArgumentException("comparator on position " + index + " is null");
				}
			}
			return true;
		}

		@Override
		public int compare(T o1, T o2) {
			int result;
			for (Comparator<? super T> comp : comparators) {
				result = comp.compare(o1, o2);
				if (result != 0) {
					return result;
				}
			}
			return 0;
		}

	}

	/**
	 * Creates a comparator chain consulting first <code>comp1</code> and then
	 * <code>comp2</code>
	 * 
	 * @param comp1
	 *        must not be <code>null</code>
	 * @param comp2
	 *        must not be <code>null</code>
	 */
	public static final <T> Comparator<T> newComparatorChain(Comparator<? super T> comp1, Comparator<? super T> comp2) {
		return new ComparatorPair<>(comp1, comp2);
	}

	/**
	 * Creates a comparator chain consulting the given comparators in the
	 * canonical order. If no comparator is given, a comparator is returned for
	 * which all objects are equal.
	 * 
	 * <p>
	 * This method uses unchecked cast to avoid &lt;? super T&gt; in the result
	 * type. As the comparator has type parameters only in its method arguments
	 * it is ensured that no {@link ClassCastException} can occur.
	 * </p>
	 * 
	 * @param comparators
	 *        a sequence of {@link Comparator}. Each of the comparator in the
	 *        chain must not be <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	public static final <T> Comparator<T> newComparatorChain(Comparator<? super T>... comparators) {
		switch (comparators.length) {
			case 0:
				return Equality.getInstance();
			case 1:
				/*
				 * There is no type parameter in any method return value so no
				 * class cast in return value. Moreover as the comparator
				 * compares each above T it especially compares T.
				 */
				return (Comparator<T>) comparators[0];
			case 2: {
				/*
				 * Java 1.5.0_21 doesn't find method without explicit declaration
				 * of parameter
				 */
				return ComparatorChain.<T>newComparatorChain(comparators[0], comparators[1]);
			}
			default:
				return new ComparatorSequence<>(comparators);
		}
	}

	/**
	 * Creates a comparator chain consulting the given comparators in the
	 * canonical order. If no comparator is given, a comparator is returned for
	 * which all objects are equal.
	 * 
	 * <p>
	 * This method uses unchecked cast to avoid &lt;? super T&gt; in the result
	 * type. As the comparator has type parameters only in its method arguments
	 * it is ensured that no {@link ClassCastException} can occur.
	 * </p>
	 * 
	 * @param comparators
	 *        a non <code>null</code> sequence of {@link Comparator}. Each of
	 *        the comparator in the chain must not be <code>null</code>. The
	 *        given list of comparators must not be changed after.
	 */
	@SuppressWarnings("unchecked")
	public static final <T> Comparator<T> newComparatorChain(List<? extends Comparator<? super T>> comparators) {
		/*
		 * The array is filled with comparators in the given list, so ok. As it
		 * will not be changed in ComparatorList the cast is not problematic.
		 */
		final Comparator<? super T>[] resultArray = (Comparator<? super T>[]) new Comparator<?>[comparators.size()];

		// Note: 'javac' requires a variable for the comparators array to accept the types.
		Comparator<? super T>[] comparatorsArray = comparators.toArray(resultArray);

		return new ComparatorSequence<>(comparatorsArray);
	}
}
