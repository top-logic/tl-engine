/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.StringServices;

/**
 * Memory-optimized list implementation.
 * 
 * <p>
 * An inline lists is inlined into its reference, as long as it only contains zero or one elements.
 * </p>
 * 
 * <p>
 * {@link InlineList} is used as follows:
 * 
 * <ul>
 * <li>Declaration of a generic reference of type {@link Object}:
 * <p>
 * <code>Object myList = InlineList.newInlineList();</code>
 * </p>
 * </li>
 * <li>The inline list is accessed afterwards through the static methods of this class. The access
 * is structured according to the pattern
 * <p>
 * <code>this.myList = InlineList.modifyingAccessor(this.myList, arguments);</code>
 * </p>
 * and
 * <p>
 * <code>result = InlineList.nonmodifyingAccessor(this.myList, arguments);</code>
 * </p>
 * <ul>
 * <li><code>this.myList = InlineList.add(this.myList, newValue);</code></li>
 * <li><code>this.myList = InlineList.remove(this.myList, oldValue);</code></li>
 * <li><code>contains = InlineList.contains(this.myList, someValue);</code></li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class InlineList {

	private static Object EMPTY_INLINE_LIST = new NamedConstant("[]");

	private static final Object[] EMPTY_ARRAY = {};

	/** Instantiation must be done through the static facade methods of this class. */
	private InlineList() { /* static methods only */ }

	/**
	 * Returns a reference to a new {@link InlineList}.
	 */
	public static Object newInlineList() {
		return EMPTY_INLINE_LIST;
	}

	/**
	 * Adds the given value to the given {@link InlineList}.
	 * 
	 * @param <T>
	 *        The declared list type.
	 * @param contentType
	 *        The dynamic list type.
	 * @param inlineList
	 *        The reference to the {@link InlineList}.
	 * @param value
	 *        The new value to add.
	 * @return The new reference to the {@link InlineList}, that must replace the given one.
	 * 
	 * @see List#add(Object)
	 */
	public static <T> Object add(Class<T> contentType, Object inlineList, T value) {
		if (value instanceof MultipleElements<?>) {
			throw new IllegalArgumentException("Inline lists cannot be nested.");
		}
		
		if (inlineList == EMPTY_INLINE_LIST) {
			return value;
		} else if (inlineList instanceof MultipleElements<?>) {
			return ((MultipleElements<?>) inlineList).internalAdd(value);
		} else {
			return new MultipleElements<>(contentType).internalAdd(inlineList).internalAdd(value);
		}
	}

	/**
	 * Adds all elements in the given collection to the given inline set.
	 * 
	 * @param elementType
	 *        Common super type of all elements in the given inline set.
	 * @param inlineList
	 *        The inline set to add element to.
	 * @param elements
	 *        The elements to add to the set.
	 * 
	 * @return A reference to the inline set containing all given elements.
	 * 
	 * @see Set#addAll(Collection)
	 */
	public static <E> Object addAll(Class<E> elementType, Object inlineList, Collection<? extends E> elements) {
		switch (elements.size()) {
			case 0:
				return inlineList;
			case 1:
				return InlineList.add(elementType, inlineList, elements.iterator().next());
			default:
				if (inlineList == EMPTY_INLINE_LIST) {
					return new MultipleElements<>(elementType, elements);
				} else if (inlineList instanceof MultipleElements) {
					@SuppressWarnings("unchecked")
					MultipleElements<E> list = (MultipleElements<E>) inlineList;
					return list.internalAddAll(elements);
				} else {
					E formerSingleContent = elementType.cast(inlineList);
					return new MultipleElements<>(elementType).internalAdd(formerSingleContent)
						.internalAddAll(elements);
				}
		}
	}

	/**
	 * Whether the given {@link InlineList} is empty.
	 * 
	 * @param inlineList
	 *        The reference to the {@link InlineList} to check.
	 * @return Whether the given list is empty.
	 * 
	 * @see List#isEmpty()
	 */
	public static boolean isEmpty(Object inlineList) {
		if (inlineList == EMPTY_INLINE_LIST) {
			return true;
		} else if (inlineList instanceof MultipleElements<?>) {
			return ((MultipleElements<?>) inlineList).isEmpty();
		} else {
			return false;
		}
	}

	/**
	 * @param inlineList
	 *        The reference to the {@link InlineList} to check.
	 * @return The number of elements in the {@link InlineList}.
	 * 
	 * @see List#size()
	 */
	public static int size(Object inlineList) {
		if (inlineList == EMPTY_INLINE_LIST) {
			return 0;
		} else if (inlineList instanceof MultipleElements<?>) {
			return ((MultipleElements<?>) inlineList).size();
		} else {
			return 1;
		}
	}

	/**
	 * Whether the given {@link InlineList} contains the given value.
	 * 
	 * @param inlineList
	 *        The reference to the {@link InlineList} to check.
	 * @param value
	 *        The value to search in the given list.
	 * @return Whether the given list contains the given value.
	 * 
	 * @see List#contains(Object)
	 */
	public static boolean contains(Object inlineList, Object value) {
		if (inlineList == EMPTY_INLINE_LIST) {
			return false;
		} else if (inlineList instanceof MultipleElements<?>) {
			return ((MultipleElements<?>) inlineList).contains(value);
		} else {
			return StringServices.equals(inlineList, value);
		}
	}

	/**
	 * Removes the given value from the given {@link InlineList}.
	 * 
	 * @param inlineList
	 *        The reference to the {@link InlineList}.
	 * @param value
	 *        The value to remove.
	 * @return The new reference to the {@link InlineList}, that must replace the given one.
	 * 
	 * @see List#remove(Object)
	 */
	public static Object remove(Object inlineList, Object value) {
		if (inlineList == EMPTY_INLINE_LIST) {
			return EMPTY_INLINE_LIST;
		} else if (inlineList instanceof MultipleElements<?>) {
			MultipleElements<?> multipleElements = (MultipleElements<?>) inlineList;
			return multipleElements.internalRemove(value);
		} else {
			if (StringServices.equals(inlineList, value)) {
				return EMPTY_INLINE_LIST;
			} else {
				return inlineList;
			}
		}
	}

	/**
	 * Removes all values from the given {@link InlineList}.
	 * 
	 * @param inlineList
	 *        The reference to the {@link InlineList}.
	 * 
	 * @return The new reference to the {@link InlineList}, that must replace the given one.
	 * 
	 * @see List#clear()
	 */
	public static Object clear(Object inlineList) {
		return EMPTY_INLINE_LIST;
	}

	/**
	 * Converts the given {@link InlineList} to a {@link List} instance.
	 * 
	 * @param <T>
	 *        The expected static type.
	 * @param expectedType
	 *        The expected dynamic type.
	 * @param inlineList
	 *        The {@link InlineList}.
	 * @return The given {@link InlineList} as {@link List}.
	 */
	public static <T> List<T> toList(Class<T> expectedType, Object inlineList) {
		if (inlineList == EMPTY_INLINE_LIST) {
			return Collections.emptyList();
		} else if (inlineList instanceof MultipleElements<?>) {
			return ((MultipleElements<?>) inlineList).typed(expectedType);
		} else {
			return Collections.singletonList(expectedType.cast(inlineList));
		}
	}

	/**
	 * Converts the given {@link InlineList} to a {@link Iterator} instance.
	 * 
	 * @param <T>
	 *        The expected static type.
	 * @param expectedType
	 *        The expected dynamic type.
	 * @param inlineList
	 *        The {@link InlineList}.
	 * @return The given {@link InlineList} as {@link List}.
	 * 
	 * @see List#iterator()
	 */
	public static <T> Iterator<T> iterator(Class<T> expectedType, Object inlineList) {
		return toList(expectedType, inlineList).iterator();
	}
	
	/**
	 * Performs the given action for each element of the {@link InlineList}
	 * 
	 * @param <T>
	 *        The expected static type.
	 * @param expectedType
	 *        The expected dynamic type.
	 * @param inlineList
	 *        The {@link InlineList}.
	 * @param action
	 *        The action to be performed for each element
	 * 
	 * @throws NullPointerException
	 *         if the specified action is null
	 * 
	 * @see List#forEach(Consumer)
	 */
	public static <T> void forEach(Class<T> expectedType, Object inlineList, Consumer<? super T> action) {
		if (inlineList == EMPTY_INLINE_LIST) {
			return;
		} else if (inlineList instanceof MultipleElements) {
			((MultipleElements<?>) inlineList).generalize(expectedType).forEach(action);
		} else {
			action.accept(expectedType.cast(inlineList));
		}
	}

	/**
	 * Converts the given {@link InlineList} to an array.
	 * 
	 * @param inlineList
	 *        The {@link InlineList}.
	 * @return The given {@link InlineList} as array.
	 * 
	 * @see List#toArray()
	 */
	public static Object[] toArray(Object inlineList) {
		if (inlineList == EMPTY_INLINE_LIST) {
			return EMPTY_ARRAY;
		} else if (inlineList instanceof MultipleElements<?>) {
			return ((MultipleElements<?>) inlineList).toArray();
		} else {
			return new Object[] {inlineList};
		}
	}

	/**
	 * Converts the given {@link InlineList} to an array.
	 * 
	 * @param inlineList
	 *        The {@link InlineList}.
	 * @param a
	 *        An array to store content to. If it is to small a new array is constructed and
	 *        returned.
	 * 
	 * @return The given {@link InlineList} as array.
	 * 
	 * @see List#toArray(Object[])
	 */
	public static <T> T[] toArray(Object inlineList, T[] a) {
		if (inlineList == EMPTY_INLINE_LIST) {
			if (a.length > 0) {
				// Contract in java.util.List
				a[0] = null;
			}
			return a;
		} else if (inlineList instanceof MultipleElements<?>) {
			return ((MultipleElements<?>) inlineList).toArray(a);
		} else {
			T[] result;
			if (a.length == 0) {
				result = (T[]) Array.newInstance(a.getClass().getComponentType(), 1);
			} else {
				result = a;
			}
			result[0] = (T) inlineList;
			if (result.length > 1) {
				// Contract in java.util.List
				result[1] = null;
			}
			return result;
		}
	}

	/**
	 * Private wrapper type for {@link InlineList}s consisting of multiple
	 * elements.
	 * 
	 * <p>
	 * Extends {@link ArrayList} for memory optimization instead of having an
	 * internal {@link ArrayList} buffer.
	 * </p>
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private static final class MultipleElements<T> extends ArrayList<T> {
		private final Class<T> dynamicType;

		private boolean _shared = false;

		/*package protected*/ MultipleElements(Class<T> dynamicType) {
			this.dynamicType = dynamicType;
		}
		
		/* package protected */MultipleElements(Class<T> dynamicType, Collection<? extends T> elements) {
			super(elements.size());
			this.dynamicType = dynamicType;
			for (T element : elements) {
				superAdd(element);
			}
		}

		MultipleElements<T> internalAdd(Object value) {
			MultipleElements<T> result = copyIfShared();
			result.superAdd(value);
			return result;
		}
		
		MultipleElements<T> internalAddAll(Collection<?> c) {
			MultipleElements<T> result = copyIfShared();
			for (Object o : c) {
				result.superAdd(o);
			}
			return result;
		}

		private void superAdd(Object value) {
			super.add(dynamicType.cast(value));
		}

		@SuppressWarnings("unchecked") // Dynamically checked.
		<X> List<X> typed(Class<X> expectedType) {
			if (this.dynamicType != expectedType) {
				throw new ClassCastException("Expected '" + expectedType.getName() + "', actual type '" + this.dynamicType.getName() + "'.");
			}
			_shared = true;
			return (List) this;
		}

		@SuppressWarnings("unchecked") // Dynamically checked.
		<X> List<X> generalize(Class<X> expectedType) {
			if (!expectedType.isAssignableFrom(dynamicType)) {
				throw new ClassCastException("Expected '" + expectedType.getName()
					+ "' is not assignable to actual type '" + this.dynamicType.getName() + "'.");
			}
			_shared = true;
			return (List) this;
		}

		Object internalRemove(Object value) {
			// Note: Must not call super.remove(value), because this is
			// implemented through iterator().remove(), which calls the
			// deactivated public method remove() on this immutable list.
			int index = indexOf(value);
			if (index < 0) {
				return this;
			} else {
				MultipleElements<T> result = copyIfShared();
				result.superRemove(index);
				if (result.size() == 1) {
					return result.get(0);
				} else {
					return result;
				}
			}
		}

		private void superRemove(int index) {
			super.remove(index);
		}

		private MultipleElements<T> copyIfShared() {
			MultipleElements<T> result;
			if (_shared) {
				result = new MultipleElements<>(dynamicType, this);
			} else {
				result = this;
			}
			return result;
		}
		
		@Override
		public void add(int index, Object element) {
			throw new UnsupportedOperationException("Immutable list.");
		}
		
		@Override
		public boolean add(Object o) {
			throw new UnsupportedOperationException("Immutable list.");
		}
		
		@Override
		public boolean addAll(Collection<? extends T> c) {
			throw new UnsupportedOperationException("Immutable list.");
		}
		
		@Override
		public boolean addAll(int index, Collection<? extends T> c) {
			throw new UnsupportedOperationException("Immutable list.");
		}
		
		@Override
		public void clear() {
			throw new UnsupportedOperationException("Immutable list.");
		}
		
		@Override
		public T remove(int index) {
			throw new UnsupportedOperationException("Immutable list.");
		}
		
		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException("Immutable list.");
		}
		
		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException("Immutable list.");
		}
		
		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException("Immutable list.");
		}
		
		@Override
		public T set(int index, T element) {
			throw new UnsupportedOperationException("Immutable list.");
		}
	}

}
