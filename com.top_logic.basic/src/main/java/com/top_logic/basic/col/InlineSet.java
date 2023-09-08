/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;

/**
 * Memory-optimized set implementation.
 * 
 * <p>
 * An inline set is inlined into its reference, as long as it only contains zero or one elements.
 * </p>
 * 
 * <p>
 * {@link InlineSet} is used as follows:
 * 
 * <ul>
 * <li>Declaration of a generic reference of type {@link Object}:
 * <p>
 * <code>Object mySet = InlineSet.newInlineSet();</code>
 * </p>
 * </li>
 * <li>The inline set is accessed afterwards through the static methods of this class. The access is
 * structured according to the pattern
 * <p>
 * <code>this.mySet = InlineSet.modifyingAccessor(this.mySet, arguments);</code>
 * </p>
 * and
 * <p>
 * <code>result = InlineSet.nonmodifyingAccessor(this.mySet, arguments);</code>
 * </p>
 * <ul>
 * <li><code>this.mySet = InlineSet.add(this.mySet, newValue);</code></li>
 * <li><code>this.mySet = InlineSet.remove(this.mySet, oldValue);</code></li>
 * <li><code>contains = InlineSet.contains(this.mySet, someValue);</code></li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 * 
 * @see InlineList
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InlineSet {

	private static final Object EMPTY_INLINE_SET = new Object();

	private InlineSet() {
		// only static methods
	}

	/**
	 * Returns an object that serves as starting point for a new inline set.
	 */
	public static Object newInlineSet() {
		return EMPTY_INLINE_SET;
	}

	/**
	 * Adds the given element to the given inline set.
	 * 
	 * @param elementType
	 *        Common super type of all elements in the given inline set.
	 * @param inlineSet
	 *        The inline set to add element to.
	 * @param element
	 *        The element to add to the set.
	 * 
	 * @return A reference to the inline set containing the given element.
	 * 
	 * @see Set#add(Object)
	 */
	public static <E> Object add(Class<E> elementType, Object inlineSet, E element) {
		if (element instanceof InlineHashSet<?>) {
			throw new IllegalArgumentException("Inline sets must not be nested.");
		}

		if (inlineSet == EMPTY_INLINE_SET) {
			return element;
		} else if (inlineSet instanceof InlineHashSet) {
			InlineHashSet<E> set = (InlineHashSet<E>) inlineSet;
			set.internalAdd(element);
			return inlineSet;
		} else if (StringServices.equals(inlineSet, element)) {
			// value and inlineSet may be null
			// a set does not contain duplicates.
			return inlineSet;
		} else {
			E formerSingleContent = elementType.cast(inlineSet);
			InlineHashSet<E> set = new InlineHashSet<>();
			set.internalAdd(formerSingleContent);
			set.internalAdd(element);
			return set;
		}
	}

	/**
	 * Adds all elements in the given collection to the given inline set.
	 * 
	 * @param elementType
	 *        Common super type of all elements in the given inline set.
	 * @param inlineSet
	 *        The inline set to add element to.
	 * @param elements
	 *        The elements to add to the set.
	 * 
	 * @return A reference to the inline set containing all given elements.
	 * 
	 * @see Set#addAll(Collection)
	 */
	public static <E> Object addAll(Class<E> elementType, Object inlineSet, Collection<? extends E> elements) {
		switch (elements.size()) {
			case 0:
				return inlineSet;
			case 1:
				return InlineSet.add(elementType, inlineSet, elements.iterator().next());
			default:
				if (inlineSet == EMPTY_INLINE_SET) {
					return new InlineHashSet<E>(elements);
				} else if (inlineSet instanceof InlineHashSet) {
					InlineHashSet<E> set = (InlineHashSet<E>) inlineSet;
					set.internalAddAll(elements);
					return inlineSet;
				} else {
					E formerSingleContent = elementType.cast(inlineSet);
					InlineHashSet<E> set = new InlineHashSet<>(elements);
					set.internalAdd(formerSingleContent);
					return set;
				}
		}
	}

	/**
	 * Removes the given element from the given inline set.
	 * 
	 * @param inlineSet
	 *        The inline set to remove element from.
	 * @param element
	 *        The element to remove from the set.
	 * 
	 * @return A reference to the inline set after removing the given element.
	 * 
	 * @see Set#remove(Object)
	 */
	public static Object remove(Object inlineSet, Object element) {
		if (inlineSet == EMPTY_INLINE_SET) {
			return inlineSet;
		} else if (inlineSet instanceof InlineHashSet) {
			InlineHashSet<?> set = (InlineHashSet<?>) inlineSet;
			boolean success = set.internalRemove(element);
			if (success && set.size() == 1) {
				return set.iterator().next();
			} else {
				return inlineSet;
			}
		} else if (StringServices.equals(inlineSet, element)) {
			// value and inlineSet may be null
			return EMPTY_INLINE_SET;
		} else {
			return inlineSet;
		}
	}

	/**
	 * Checks whether the given element is contained in the given inline set.
	 * 
	 * @param inlineSet
	 *        The inline set to check for containment.
	 * @param element
	 *        The element to check.
	 * 
	 * @see Set#contains(Object)
	 */
	public static boolean contains(Object inlineSet, Object element) {
		if (inlineSet == EMPTY_INLINE_SET) {
			return false;
		} else if (inlineSet instanceof InlineHashSet) {
			InlineHashSet<?> set = (InlineHashSet<?>) inlineSet;
			return set.contains(element);
		} else {
			return StringServices.equals(inlineSet, element);
		}
	}

	/**
	 * Returns the number of elements contained in the given inline set.
	 * 
	 * @param inlineSet
	 *        The inline set to retrieve size from.
	 * 
	 * @see Set#size()
	 */
	public static int size(Object inlineSet) {
		if (inlineSet == EMPTY_INLINE_SET) {
			return 0;
		} else if (inlineSet instanceof InlineHashSet) {
			InlineHashSet<?> set = (InlineHashSet<?>) inlineSet;
			return set.size();
		} else {
			return 1;
		}
	}

	/**
	 * Checks whether the given inline set is empty.
	 * 
	 * @param inlineSet
	 *        The inline set to check.
	 * 
	 * @see Set#isEmpty()
	 */
	public static boolean isEmpty(Object inlineSet) {
		if (inlineSet == EMPTY_INLINE_SET) {
			return true;
		} else if (inlineSet instanceof InlineHashSet) {
			InlineHashSet<?> set = (InlineHashSet<?>) inlineSet;
			return set.isEmpty();
		} else {
			return false;
		}
	}

	/**
	 * Returns a {@link Set}
	 * 
	 * <p>
	 * The returned set must not be modified.
	 * </p>
	 * 
	 * @param elementType
	 *        Common super type of all elements in the given inline set.
	 * @param inlineSet
	 *        The inline set to create a {@link Set} from.
	 */
	public static <E> Set<E> toSet(Class<E> elementType, Object inlineSet) {
		if (inlineSet == EMPTY_INLINE_SET) {
			return Collections.emptySet();
		} else if (inlineSet instanceof InlineHashSet) {
			return (InlineHashSet<E>) inlineSet;
		} else {
			return Collections.singleton(CollectionUtil.dynamicCast(elementType, inlineSet));
		}
	}

	/**
	 * Removes all elements from the given inline set.
	 * 
	 * @param inlineSet
	 *        The inline set to clear.
	 * 
	 * @see Set#clear()
	 */
	public static Object clear(Object inlineSet) {
		return EMPTY_INLINE_SET;
	}

	/**
	 * Performs the given action for each element of the {@link InlineSet}
	 * 
	 * @param <T>
	 *        The expected static type.
	 * @param expectedType
	 *        The expected dynamic type.
	 * @param inlineSet
	 *        The {@link InlineSet}.
	 * @param action
	 *        The action to be performed for each element
	 * 
	 * @throws NullPointerException
	 *         if the specified action is null
	 * 
	 * @see Set#forEach(Consumer)
	 */
	public static <T> void forEach(Class<T> expectedType, Object inlineSet, Consumer<? super T> action) {
		if (inlineSet == EMPTY_INLINE_SET) {
			return;
		} else if (inlineSet instanceof InlineHashSet) {
			((InlineHashSet<T>) inlineSet).forEach(action);
		} else {
			action.accept(expectedType.cast(inlineSet));
		}
	}

	/**
	 * Returns an array containing all elements in the given inline set.
	 * 
	 * @param inlineSet
	 *        The inline set to extract values from.
	 * 
	 * @see Set#toArray()
	 */
	public static Object[] toArray(Object inlineSet) {
		if (inlineSet == EMPTY_INLINE_SET) {
			return ArrayUtil.EMPTY_ARRAY;
		} else if (inlineSet instanceof InlineHashSet) {
			return ((InlineHashSet<?>) inlineSet).toArray();
		} else {
			return new Object[] { inlineSet };
		}
	}

	/**
	 * Returns an array containing all elements in the given inline set.
	 * 
	 * @param inlineSet
	 *        The inline set to extract values from.
	 * @param a
	 *        An array to store content to. If it is to small a new array is constructed and
	 *        returned.
	 * 
	 * @see Set#toArray(Object[])
	 */
	public static <T> T[] toArray(Object inlineSet, T[] a) {
		if (inlineSet == EMPTY_INLINE_SET) {
			if (a.length > 0) {
				// Contract in java.util.Collection#toArray(Object[])
				a[0] = null;
			}
			return a;
		} else if (inlineSet instanceof InlineHashSet<?>) {
			return ((InlineHashSet<?>) inlineSet).toArray(a);
		} else {
			T[] result;
			if (a.length == 0) {
				result = (T[]) Array.newInstance(a.getClass().getComponentType(), 1);
			} else {
				result = a;
			}
			result[0] = (T) inlineSet;
			if (result.length > 1) {
				// Contract in java.util.Collection#toArray(Object[])
				result[1] = null;
			}
			return result;
		}
	}

	/**
	 * Set implementation used when inline set contains more than one element.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static class InlineHashSet<E> extends HashSet<E> {

		public InlineHashSet() {
		}

		public InlineHashSet(Collection<? extends E> c) {
			super(c.size());
			internalAddAll(c);
		}

		boolean internalRemove(Object o) {
			return super.remove(o);
		}

		boolean internalAdd(E e) {
			return super.add(e);
		}

		boolean internalAddAll(Collection<? extends E> c) {
			// must not call super.addAll() because this falls back to add() which is impossible.
			boolean modified = false;
			for (E e : c)
				if (internalAdd(e))
					modified = true;
			return modified;
		}

		@Override
		public boolean add(E e) {
			throw failUnmodifable();
		}

		@Override
		public boolean addAll(Collection<? extends E> c) {
			throw failUnmodifable();
		}

		@Override
		public boolean remove(Object o) {
			throw failUnmodifable();
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			throw failUnmodifable();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw failUnmodifable();
		}

		@Override
		public void clear() {
			throw failUnmodifable();
		}

		private static RuntimeException failUnmodifable() {
			throw new UnsupportedOperationException("Set variants of InlineSet's are not modifiable.");
		}

	}
}
