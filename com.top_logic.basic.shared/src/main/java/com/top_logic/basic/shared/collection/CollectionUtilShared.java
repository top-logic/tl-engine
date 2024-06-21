/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.shared.collection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import com.top_logic.basic.shared.collection.factory.CollectionFactoryShared;
import com.top_logic.basic.shared.collection.iterator.IteratorUtilShared;
import com.top_logic.basic.shared.collection.map.MapUtilShared;

/**
 * This class implements extended Collection operations.
 * 
 * @see IteratorUtilShared
 *
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public abstract class CollectionUtilShared extends CollectionFactoryShared {

	/** @see IteratorUtilShared#EMPTY_ITERATOR */
	public static final Iterator<?> EMPTY_ITERATOR = IteratorUtilShared.EMPTY_ITERATOR;

	/** Holds a singleton set containing null as single element. Used by the cleanUp method. */
	public static final Set<?> SINGLE_NULL_COLLECTION = Collections.singleton(null);

	/** The default load factor of new HashSets. */
	public static final float DEFAULT_LOAD_FACTOR = 0.75f;

	/**
	 * Creates a new set which is big enough to hold the given amount of elements without need of
	 * resizing. This is required because in a set created with new HashSet(size) can only be put
	 * size * DEFAULT_LOAD_FACTOR (= size * 0.75) elements without resizing the set.
	 *
	 * @see MapUtilShared#newMap(int)
	 * @param size
	 *        the requested capacity of the new map
	 * @return the new Set with the specified capacity
	 */
	public static final <T> HashSet<T> newSet(int size) {
		return new HashSet<>((int) (size / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a new set which is big enough to hold the given amount of elements without need of
	 * resizing. This is required because in a set created with new HashSet(size) can only be put
	 * size * DEFAULT_LOAD_FACTOR (= size * 0.75) elements without resizing the set.
	 *
	 * @see MapUtilShared#newMap(int)
	 * @param size
	 *        the requested capacity of the new map
	 * @return the new {@link LinkedHashSet} with the specified capacity
	 */
	public static final <T> LinkedHashSet<T> newLinkedSet(int size) {
		return new LinkedHashSet<>((int) (size / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Null safe version of equals.
	 *
	 * @param a
	 *        the first parameter
	 * @param b
	 *        the second parameter
	 * @return <code>true</code> if both are identical (e.g. both are null), or both are equal.
	 */
	public static boolean equals(Object a, Object b) {
		if (a == b) {
			// objects identical or both null
			return true;
		}
		if (a != null) {
			// safely use equals on a
			return a.equals(b);
		}
		// a is null, but b is not null
		return false;
	}

	/**
	 * Convenience method to check isEmpty for collections which may be null.
	 *
	 * @return true if the collection is null or empty
	 */
	public static boolean isEmptyOrNull(Collection<?> aCollection) {
		return aCollection == null || aCollection.isEmpty();
	}

	/**
	 * Convenience method to check isEmpty for maps which may be null.
	 *
	 * @return true if the Map is null or empty
	 */
	public static boolean isEmptyOrNull(Map<?, ?> aMap) {
		return aMap == null || aMap.isEmpty();
	}

	/**
	 * Whether any of the given values is null.
	 * 
	 * @param entries
	 *        If it is null, false is returned, as null is converted to the empty list, which does
	 *        not contain null.
	 */
	public static boolean containsNull(Object... entries) {
		for (Object entry : entries) {
			if (entry == null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether any of the given values is null.
	 * 
	 * @param entries
	 *        If it is null, false is returned, as null is converted to the empty list, which does
	 *        not contain null.
	 */
	public static boolean containsNull(Collection<?> entries) {
		for (Object entry : nonNull(entries)) {
			if (entry == null) {
				return true;
			}
		}
		return false;
	}

	/** @see IteratorUtilShared#emptyIterator() */
	public static final <T> Iterator<T> emptyIterator() {
		return IteratorUtilShared.emptyIterator();
	}

	/**
	 * Check, if there is at least one object, which appears in both collections.
	 * 
	 * @return If collections have at least one object in common.
	 */
	public static boolean containsAny(Collection<?> c1, Collection<?> c2) {
		if (c1 == null || c1.isEmpty())
			return false;
		if (c2 == null || c2.isEmpty())
			return false;

		Collection<?> small, large;
		if (c1.size() > c2.size()) {
			small = c2;
			large = c1;
		} else {
			small = c1;
			large = c2;
		}

		for (Object t : small) {
			if (large.contains(t))
				return true;
		}
		return false;
	}

	/**
	 * This method checks whether both collections contain the exact same content. Both collections
	 * will be asked containsAll with the opposite collection.
	 *
	 * @param a
	 *        First Collection
	 * @param b
	 *        second Collection
	 * @return if both collections contain exactly the same.
	 */
	public static boolean containsSame(Collection<?> a, Collection<?> b) {
		if (a == b) {
			// objects identical or both null
			return true;
		}
		if (a == null) {
			return b == null;
		}
		if (b == null) {
			return false;
		}
		if (a.containsAll(b)) {
			return b.containsAll(a);
		}
		return false;
	}

	/**
	 * If the given object is a collection the first element is returned, otherwise the given object
	 * is returned..
	 * 
	 * If the collection is empty or null a null will be returned.
	 * 
	 * It will <code>assert</code> that the collection contains one element.
	 * 
	 * @param object
	 *        A collection with only one object or a single object.
	 */
	public static Object getSingleValueFrom(Object object) {
		if (object instanceof Collection<?>) {
			return getSingleValueFromCollection((Collection<?>) object);
		} else {
			return object;
		}
	}

	/**
	 * This method returns the first element from a collection. If the collection is empty or
	 * <code>null</code>, <code>null</code> will be returned. If not, it will <code>assert</code>
	 * that the collection contains exactly one element.
	 * 
	 * @param aColl
	 *        the {@link Collection} to get the element from
	 * @return the element in the collection
	 */
	public static <T> T getSingleValueFromCollection(Collection<T> aColl) {
		int size;
		if ((aColl == null) || 0 == (size = aColl.size())) {
			return (null);
		}
		assert size == 1 : "Collection contains more than a single object: " + size;

		if (aColl instanceof List<?>) {
			return ((List<T>) aColl).get(0);
		}
		return aColl.iterator().next();
	}

	/**
	 * This method returns the first element from a List. If the List is empty or <code>null</code>,
	 * <code>null</code> will be returned. If not, it will <code>assert</code> that the {@link List}
	 * contains at least one element.
	 * 
	 * @param aList
	 *        the {@link List} to get the element from
	 * @return the element in the list
	 */
	public static <T> T getSingleValueFromCollection(List<T> aList) {
		if ((aList == null) || aList.isEmpty()) {
			return (null);
		}

		assert (aList.size() == 1) : "List contains more than a single object: " + aList.size();

		return aList.get(0);
	}

	/**
	 * Returns an empty collection for null, the collection otherwise
	 * 
	 * @param aColl
	 *        a {@link Collection}
	 * @return the given collection or an empty collection in case of NULL, never NULL
	 */
	public static <T> Collection<T> toCollection(Collection<T> aColl) {
		return aColl == null ? new ArrayList<>(0) : aColl;
	}

	/**
	 * Converts the given iterable to a list.
	 *
	 * @param iterable
	 *        The {@link Iterable} to convert.
	 * @return The given iterable, if it does implement the {@link List} interface, or a new list
	 *         containing the collection's elements. Returns an empty list, if the argument was
	 *         <code>null</code>.
	 */
	public static <T> List<T> toList(Iterable<T> iterable) {
		if (iterable instanceof List<?>) {
			return (List<T>) iterable;
		}
		if (iterable == null) {
			return new ArrayList<>(0);
		}
		if (iterable instanceof Collection<?>) {
			return new ArrayList<>((Collection<T>) iterable);
		}
		return toListIterable(iterable);
	}

	/**
	 * Wraps the given value(s) into a list, if it is neither <code>null</code> or already a
	 * collection.
	 * 
	 * <p>
	 * The resulting list must not be modified. It may either be the argument itself or a read-only
	 * view.
	 * </p>
	 * 
	 * @param value
	 *        The value to wrap.
	 * 
	 * @return The given value, if it is already a list. A collection is converted to a list. A
	 *         singleton value is wrapped into a singleton list. For <code>null</code>, the empty
	 *         list is returned.
	 */
	public static List<?> asList(Object value) {
		if (value instanceof List<?>) {
			return (List<?>) value;
		} else if (value instanceof Collection<?>) {
			return asListCollection((Collection<?>) value);
		} else if (value instanceof Iterable<?>) {
			return asListIterable((Iterable<?>) value);
		} else if (value instanceof Iterator<?>) {
			return asListIterator((Iterator<?>) value);
		} else if (value == null) {
			return Collections.emptyList();
		} else if (value.getClass().isArray()) {
			return asListArray(value);
		} else {
			return Collections.singletonList(value);
		}
	}

	private static List<?> asListCollection(Collection<?> collection) {
		switch (collection.size()) {
			case 0:
				return Collections.emptyList();
			case 1:
				return Collections.singletonList(collection.iterator().next());
			default:
				return new ArrayList<>(collection);
		}
	}

	private static List<?> asListIterable(Iterable<?> iterable) {
		return asListIterator(iterable.iterator());
	}

	private static List<?> asListIterator(Iterator<?> it) {
		if (it.hasNext()) {
			Object first = it.next();
			if (it.hasNext()) {
				ArrayList<Object> result = new ArrayList<>();
				result.add(first);
				do {
					result.add(it.next());
				} while (it.hasNext());
				return result;
			} else {
				return Collections.singletonList(first);
			}
		} else {
			return Collections.emptyList();
		}
	}

	private static List<?> asListArray(Object value) {
		int length = Array.getLength(value);
		switch (length) {
			case 0:
				return Collections.emptyList();
			case 1:
				return Collections.singletonList(Array.get(value, 0));
			default:
				List<Object> result = new ArrayList<>();
				for (int n = 0, cnt = length; n < cnt; n++) {
					result.add(Array.get(value, n));
				}
				return result;
		}
	}

	/**
	 * Wraps the given value into a set, if it is neither <code>null</code> or already a collection.
	 * 
	 * <p>
	 * The resulting set must not be modified. It may either be the argument itself or a read-only
	 * view.
	 * </p>
	 *
	 * @param value
	 *        The value to wrap.
	 * 
	 * @return The given value, if it is already a set. A collection is converted to a set. A
	 *         singleton value is wrapped into a singleton set. For <code>null</code>, the empty set
	 *         is returned.
	 */
	public static Set<?> asSet(Object value) {
		if (value instanceof Set<?>) {
			return (Set<?>) value;
		} else if (value instanceof Collection<?>) {
			return asSetCollection((Collection<?>) value);
		} else if (value instanceof Iterable<?>) {
			return asSetIterable((Iterable<?>) value);
		} else if (value instanceof Iterator<?>) {
			return asSetIterator((Iterator<?>) value);
		} else if (value == null) {
			return Collections.emptySet();
		} else if (value.getClass().isArray()) {
			return asSetArray(value);
		} else {
			return Collections.singleton(value);
		}
	}

	private static Set<?> asSetCollection(Collection<?> collection) {
		switch (collection.size()) {
			case 0:
				return Collections.emptySet();
			case 1:
				return Collections.singleton(collection.iterator().next());
			default:
				return new HashSet<>(collection);
		}
	}

	private static Set<?> asSetIterable(Iterable<?> iterable) {
		return asSetIterator(iterable.iterator());
	}

	private static Set<?> asSetIterator(Iterator<?> it) {
		if (it.hasNext()) {
			Object first = it.next();
			if (it.hasNext()) {
				Set<Object> result = new HashSet<>();
				result.add(first);
				do {
					result.add(it.next());
				} while (it.hasNext());
				return result;
			} else {
				return Collections.singleton(first);
			}
		} else {
			return Collections.emptySet();
		}
	}

	private static Set<?> asSetArray(Object value) {
		int length = Array.getLength(value);
		switch (length) {
			case 0:
				return Collections.emptySet();
			case 1:
				return Collections.singleton(Array.get(value, 0));
			default:
				HashSet<Object> result = new HashSet<>();
				for (int n = 0, cnt = length; n < cnt; n++) {
					result.add(Array.get(value, n));
				}
				return result;
		}
	}

	/**
	 * Copies all elements of the given source.
	 * 
	 * @param source
	 *        the source to take elements from
	 * @return a {@link List} of elements fetched from the given source.
	 */
	public static <T> ArrayList<T> toListIterable(Iterable<? extends T> source) {
		return toList(source.iterator());
	}

	/**
	 * Buffers all elements of the given {@link Iterator}.
	 *
	 * @param it
	 *        the {@link Iterator} to traverse
	 * @return a {@link List} of elements fetched from the given iterator.
	 */
	public static <T> ArrayList<T> toList(Iterator<? extends T> it) {
		ArrayList<T> result = new ArrayList<>();
		while (it.hasNext()) {
			result.add(it.next());
		}
		return result;
	}

	/**
	 * Inserts elements from the given source into a mutable {@link Set}.
	 * 
	 * @param source
	 *        the {@link Iterable} source of elements.
	 * @return a {@link HashSet} of elements fetched from the given source.
	 */
	public static <T> HashSet<T> toSetIterable(Iterable<? extends T> source) {
		return toSet(source.iterator());
	}

	/**
	 * Inserts elements from the given {@link Iterator} into a mutable {@link Set}.
	 *
	 * @param it
	 *        the {@link Iterator} to traverse.
	 * @return a {@link HashSet} of elements fetched from the given iterator.
	 */
	public static <T> HashSet<T> toSet(Iterator<? extends T> it) {
		HashSet<T> result = new HashSet<>();
		while (it.hasNext()) {
			result.add(it.next());
		}
		return result;
	}

	/**
	 * Converts the given collection to a set.
	 *
	 * @param aCollection
	 *        The collection to convert
	 * @return The given collection, if it does implement the {@link Set} interface, or a new set
	 *         containing the collection's elements.
	 */
	public static <E> Set<E> toSet(Collection<E> aCollection) {
		if (aCollection instanceof Set<?>) {
			return (Set<E>) aCollection;
		}
		if (aCollection == null) {
			return newSet(0);
		}
		return new HashSet<>(aCollection);
	}

	/**
	 * Converts the given array to a mutable list.
	 * 
	 * Use {@link Arrays#asList(Object...)} when you can use an immutable list.
	 *
	 * @param aArray
	 *        The array to convert
	 * @return A new ArrayList containing the array's elements. Returns an empty list, if the
	 *         argument was <code>null</code>.
	 */
	public static <T> ArrayList<T> toList(T[] aArray) {
		if (aArray == null) {
			return new ArrayList<>(0);
		}
		ArrayList<T> theResult = new ArrayList<>(aArray.length);
		for (int i = 0; i < aArray.length; i++) {
			theResult.add(aArray[i]);
		}
		return theResult;
	}

	/**
	 * Converts the given array to a set.
	 *
	 * @param aArray
	 *        The array to convert
	 * @return A new set containing the array's elements. Returns an empty set, if the argument was
	 *         <code>null</code>.
	 */
	public static <E> Set<E> toSet(E[] aArray) {
		if (aArray == null || aArray.length == 0) {
			return new HashSet<>(0);
		}
		Set<E> theResult = newSet(aArray.length);
		for (int i = 0; i < aArray.length; i++) {
			theResult.add(aArray[i]);
		}
		return theResult;
	}

	/**
	 * Creates a new modifiable list containing the elements given in the parameter list.
	 * 
	 * @param <E>
	 *        the type of the list
	 * @param elements
	 *        the elements to put into the list
	 * @return a modifiable list containing the elements given in the parameter list
	 */
	@SafeVarargs
	public static <E> List<E> createList(E... elements) {
		return toList(elements);
	}

	/**
	 * Creates a new modifiable set containing the elements given in the parameter list.
	 * 
	 * @param <E>
	 *        the type of the set
	 * @param elements
	 *        the elements to put into the set
	 * @return a modifiable set containing the elements given in the parameter list
	 */
	@SafeVarargs
	public static <E> Set<E> createSet(E... elements) {
		return toSet(elements);
	}

	/**
	 * Converts the given collection into a list and creates a new modifiable list with the same
	 * content if necessary so that the returned list is a modifiable list.
	 * 
	 * @param aCollection
	 *        the collection to convert
	 * @return the given list, if it is a modifiable list, or a new modifiable list containing the
	 *         given collection's elements.
	 */
	public static <E> List<E> modifiableList(Collection<? extends E> aCollection) {
		if (aCollection == null)
			return new ArrayList<>(0);
		return new ArrayList<>(aCollection);
	}

	/**
	 * Converts the given collection into a set and creates a new modifiable set with the same
	 * content if necessary so that the returned set is a modifiable set.
	 * 
	 * @param aCollection
	 *        the collection to convert
	 * @return the given set, if it is a modifiable set, or a new modifiable set containing the
	 *         given collection's elements.
	 */
	public static <E> Set<E> modifiableSet(Collection<? extends E> aCollection) {
		if (aCollection == null)
			return new HashSet<>(0);
		return new HashSet<>(aCollection);
	}

	/**
	 * Removes duplicated entries from a given list.
	 * 
	 * <p>
	 * This implementation uses a temporary {@link Set}, consider using more efficient methods for
	 * lists with {@link Comparable} elements, e.g. #sort other Methods in case you have a) lots of
	 * data b) a List that is already sorted in some way.
	 * </p>
	 * 
	 * @param aList
	 *        The list that contains duplicated entries.
	 * @return A list that contains no duplicated entries.
	 * 
	 * @see #sortRemovingDuplicates(List)
	 */
	public static <E> List<E> removeDuplicates(List<? extends E> aList) {
		Set<? extends E> temp = toSet(aList);
		return new ArrayList<>(temp);
	}

	/**
	 * Removes duplicates in the given list assuming that equal values only occur next to each other
	 * (as in a sorted list).
	 * 
	 * <p>
	 * In the general case, use {@link #sortRemovingDuplicates(List)}, or
	 * {@link #removeDuplicates(List)}.
	 * </p>
	 * 
	 * @param sortedList
	 *        The sorted list to modify.
	 * 
	 * @see #sortRemovingDuplicates(List)
	 */
	public static <T> void removeDuplicatesSortedInline(List<T> sortedList) {
		removeDuplicatesSortedInline(sortedList, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				if (Objects.equals(o1, o2)) {
					return 0;
				}
				// Violates comparator asymmetry requirement, but is only used for deciding
				// equality.
				return -1;
			}
		});
	}

	/**
	 * Removes duplicates in the given list assuming that equal values only occur next to each other
	 * (as in a sorted list).
	 * 
	 * <p>
	 * In the general case, use {@link #sortRemovingDuplicates(List)}, or
	 * {@link #removeDuplicates(List)}.
	 * </p>
	 * 
	 * @param sortedList
	 *        The sorted list to modify.
	 * @param order
	 *        A strategy deciding the equality of elements (the
	 *        {@link Comparator#compare(Object, Object)} contract of antisymmetry is not required).
	 * 
	 * @see #removeDuplicatesSortedInline(List)
	 * @see #sortRemovingDuplicates(List, Comparator)
	 */
	public static <T> void removeDuplicatesSortedInline(List<T> sortedList, Comparator<? super T> order) {
		if (sortedList.size() <= 1) {
			return;
		}
		int read = 1;
		int write = 1;
		T last = sortedList.get(0);
		for (int size = sortedList.size(); read < size; read++) {
			T next = sortedList.get(read);
			if (order.compare(last, next) != 0) {
				sortedList.set(write++, next);
				last = next;
			}
		}

		// Remove unused slots.
		for (int index = sortedList.size() - 1; index >= write; index--) {
			sortedList.remove(index);
		}
	}

	/**
	 * Removes duplicates in the given list by sorting its entries and removing adjacent equal
	 * elements.
	 * 
	 * @param list
	 *        The list to modify.
	 * 
	 * @see #sortRemovingDuplicates(List, Comparator)
	 * @see #removeDuplicatesSortedInline(List)
	 */
	public static <T extends Comparable<T>> void sortRemovingDuplicates(List<T> list) {
		sortRemovingDuplicates(list, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				if (o1 == null) {
					if (o2 == null) {
						return 0;
					} else {
						return -1;
					}
				} else {
					if (o2 == null) {
						return 1;
					} else {
						return o1.compareTo(o2);
					}
				}
			}
		});
	}

	/**
	 * Removes duplicates in the given list by sorting its entries and removing adjacent equal
	 * elements.
	 * 
	 * @param list
	 *        The list to modify.
	 * @param order
	 *        The sort order to use for sorting and deciding equality.
	 * 
	 * @see #sortRemovingDuplicates(List)
	 * @see #removeDuplicatesSortedInline(List, Comparator)
	 */
	public static <T> void sortRemovingDuplicates(List<T> list, Comparator<? super T> order) {
		if (list.size() <= 1) {
			return;
		}
		Collections.sort(list, order);
		removeDuplicatesSortedInline(list, order);
	}

	/**
	 * Removes all <code>null</code> values from the given collection.
	 *
	 * @param aCol
	 *        the collection to cleanUp
	 */
	public static void cleanUp(Collection<?> aCol) {
		if (aCol != null)
			aCol.removeAll(SINGLE_NULL_COLLECTION);
	}

	/**
	 * Creates a new mutable list with the given object in it. The returned list will always have a
	 * size of 1.
	 *
	 * @param aObject
	 *        the object to put into a list
	 * @return a new list with only the given object in it
	 */
	public static <E> List<E> intoList(E aObject) {
		ArrayList<E> theList = new ArrayList<>(1);
		theList.add(aObject);
		return theList;
	}

	/**
	 * Creates a new mutable set with the given object in it. The returned set will always have a
	 * size of 1.
	 *
	 * @param aObject
	 *        the object to put into a set
	 * @return a new set with only the given object in it
	 */
	public static <E> Set<E> intoSet(E aObject) {
		HashSet<E> theSet = newSet(1);
		theSet.add(aObject);
		return theSet;
	}

	/**
	 * Creates a new mutable sorted set with the given object in it. The returned set will always
	 * have a size of 1.
	 *
	 * @param aObject
	 *        the object to put into a set
	 * @return a new set with only the given object in it
	 */
	public static <E> SortedSet<E> intoSortedSet(E aObject) {
		TreeSet<E> theSet = new TreeSet<>();
		theSet.add(aObject);
		return theSet;
	}

	/**
	 * Creates a new mutable list with the given object in it. If the given object is
	 * <code>null</code>, a empty modifiable list is returned.
	 * 
	 * @param aObject
	 *        the object to put into a list
	 * @return a new list with only the given object in it
	 */
	public static <E> List<E> intoListNotNull(E aObject) {
		return aObject == null ? new ArrayList<>(0) : intoList(aObject);
	}

	/**
	 * Creates a new mutable set with the given object in it. If the given object is
	 * <code>null</code>, a empty modifiable set is returned.
	 *
	 * @param aObject
	 *        the object to put into a set
	 * @return a new set with only the given object in it
	 */
	public static <E> Set<E> intoSetNotNull(E aObject) {
		return aObject == null ? newSet(0) : intoSet(aObject);
	}

	/**
	 * Creates a new mutable sorted set with the given object in it. If the given object is
	 * <code>null</code>, a empty modifiable set is returned.
	 *
	 * @param aObject
	 *        the object to put into a set
	 * @return a new set with only the given object in it
	 */
	public static <E> SortedSet<E> intoSortedSetNotNull(E aObject) {
		return aObject == null ? new TreeSet<>() : intoSortedSet(aObject);
	}

	/**
	 * Returns the first element of the given collection.
	 *
	 * @param aCollection
	 *        the collection to get the first element from.
	 * @return the first element of the given collection or <code>null</code>, if the collection is
	 *         empty
	 */
	public static <T> T getFirst(Collection<T> aCollection) {
		if (isEmptyOrNull(aCollection)) {
			return null;
		}
		if (aCollection instanceof RandomAccess) {
			return ((List<T>) aCollection).get(0);
		}
		if (aCollection instanceof SortedSet) {
			return ((SortedSet<T>) aCollection).first();
		}
		return aCollection.iterator().next();
	}

	/**
	 * Returns the first element assuming object is a collection.
	 *
	 * @param anObject
	 *        the collection to get the first element from.
	 * @return the first element of the given collection, <code>null</code>, if the collection is
	 *         empty anObject in case it is not a collection.
	 */
	public static Object getFirst(Object anObject) {
		if (!(anObject instanceof Collection<?>)) {
			return anObject; // implies check for null
		}
		return getFirst((Collection<?>) anObject);
	}

	/**
	 * Returns the second element of the given collection.
	 *
	 * @param aCollection
	 *        the collection to get the second element from.
	 * @return the second element of the given collection or <code>null</code>, if the collection
	 *         has not enough elements
	 */
	public static <E> E getSecond(Collection<E> aCollection) {
		if (isEmptyOrNull(aCollection) || aCollection.size() < 2) {
			return null;
		}
		if (aCollection instanceof RandomAccess) {
			return ((List<E>) aCollection).get(1);
		}
		Iterator<E> it = aCollection.iterator();
		it.next();
		return it.next();
	}

	/**
	 * Returns the last element of the given collection.
	 *
	 * @param aCollection
	 *        the collection to get the last element from.
	 * @return the last element of the given collection or <code>null</code>, if the collection is
	 *         empty
	 */
	public static <E> E getLast(Collection<E> aCollection) {
		if (isEmptyOrNull(aCollection)) {
			return null;
		}
		if (aCollection instanceof RandomAccess) {
			return ((List<E>) aCollection).get(aCollection.size() - 1);
		}
		if (aCollection instanceof List) {
			return ((List<E>) aCollection).listIterator(aCollection.size()).previous();
		}
		if (aCollection instanceof SortedSet) {
			return ((SortedSet<E>) aCollection).last();
		}
		Iterator<E> it = aCollection.iterator();
		E theLast = it.next();
		while (it.hasNext()) {
			theLast = it.next();
		}
		return theLast;
	}

	/**
	 * Returns the last element assuming object is a collection.
	 *
	 * @param anObject
	 *        the collection to get the last element from.
	 * @return the last element of the given collection, <code>null</code>, if the collection is
	 *         empty anObject in case it is not a collection.
	 */
	public static Object getLast(Object anObject) {
		if (!(anObject instanceof Collection<?>)) {
			return anObject; // implies check for null
		}
		return getLast((Collection<?>) anObject);
	}

	/**
	 * Returns an unmodifiable set containing the base collection and all values in the given second
	 * collection.
	 */
	public static <E> Set<E> unmodifiableSet(Collection<? extends E> base, Collection<? extends E> addedValues) {
		HashSet<E> result = newSet(base.size() + addedValues.size());
		result.addAll(base);
		result.addAll(addedValues);
		return Collections.unmodifiableSet(result);
	}

	/**
	 * Returns an unmodifiable set containing the base collection and all values in the given array.
	 */
	public static <E> Set<E> unmodifiableSet(Collection<? extends E> base, E[] addedValues) {
		if (base == null) {
			return unmodifiableSet(addedValues);
		}
		return unmodifiableSet(base, Arrays.asList(addedValues));
	}

	/**
	 * Returns the given array as unmodifiable set.
	 */
	public static <E> Set<E> unmodifiableSet(E[] values) {
		return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(values)));
	}

	/**
	 * Returns a copy of the given {@link Collection} as unmodifiable set.
	 */
	public static <E> Set<E> unmodifiableSet(Collection<E> original) {
		return Collections.unmodifiableSet(new HashSet<>(original));
	}

	/**
	 * Returns an unmodifiable list containing the base collection and all values in the given
	 * second collection.
	 */
	public static <E> List<E> unmodifiableList(Collection<? extends E> base, Collection<? extends E> addedValues) {
		ArrayList<E> result = new ArrayList<>(base.size() + addedValues.size());
		result.addAll(base);
		result.addAll(addedValues);
		return Collections.unmodifiableList(result);
	}

	/**
	 * Returns a copy of the given Collection as unmodifiable list.
	 */
	public static <E> List<E> unmodifiableList(Collection<E> values) {
		return Collections.unmodifiableList(new ArrayList<>(values));
	}

	/**
	 * Returns the first elements of the base list in a new list.
	 *
	 * @param base
	 *        The base collection. Must NOT be <code>null</code>.
	 * @param number
	 *        Number of elements (0..n).
	 */
	public static <E> List<E> getFirstElementsAsList(Collection<? extends E> base, int number) {
		ArrayList<E> result = new ArrayList<>(number < 0 ? 0 : number);

		int i = 0;
		for (Iterator<? extends E> iterator = base.iterator(); iterator.hasNext();) {
			if (i < number) {
				result.add(iterator.next());
				i++;
			} else {
				break;
			}
		}

		return result;
	}

	/**
	 * Converts the given <code>int</code> array to a list of {@link Integer}.
	 */
	public static List<Integer> toList(int[] anArray) {
		ArrayList<Integer> result = new ArrayList<>(anArray.length);
		for (int index = 0, cnt = anArray.length; index < cnt; index++) {
			result.add(Integer.valueOf(anArray[index]));
		}
		return result;
	}

	/**
	 * Returns a list of length <code>aNumber</code> which contains <code>null</code> at each
	 * position.
	 */
	public static <E> List<E> createEmptyList(int aNumber) {
		ArrayList<E> result = new ArrayList<>(aNumber);
		for (int index = 0; index < aNumber; index++) {
			result.add(null);
		}
		return result;
	}

	/**
	 * Optimized union of two Sets.
	 * 
	 * @param s1
	 *        may be <code>null</code>
	 * @param s2
	 *        may be <code>null</code>
	 * 
	 * @return s1, s2 or a Union of s1 and s2. So it may be <code>null</code>
	 */
	public static <T> Set<T> union2(Set<T> s1, Set<T> s2) {
		if (s1 == s2) {
			return s1;
		}

		int size1 = s1 != null ? s1.size() : 0;
		if (size1 == 0) {
			return s2;
		} // s1 != null and not empty

		int size2 = s2 != null ? s2.size() : 0;
		if (size2 == 0) {
			return s1;
		} // s2 != null and not empty

		HashSet<T> result = newSet(size1 + size2);
		result.addAll(s1);
		if (result.addAll(s2)) {
			return result;
		} else {
			// Free memory that was uselessly allocated.
			return s1;
		}
	}

	/**
	 * Computes the union of the two given collections.
	 * 
	 * @param s1
	 *        The first collection.
	 * @param s2
	 *        The second collection.
	 * @return A new set that contains all entries from both collections.
	 */
	public static <E> HashSet<E> union(Collection<? extends E> s1, Collection<? extends E> s2) {
		HashSet<E> result = newSet(s1.size() + s2.size());
		result.addAll(s1);
		result.addAll(s2);
		return result;
	}

	/**
	 * Computes the union of the all collections in the given collection.
	 * 
	 * @param sources
	 *        The the collection to build union from .
	 * @return A new set that contains all entries from all collections in the given collection.
	 */
	public static <E> HashSet<E> union(Collection<? extends Collection<? extends E>> sources) {
		HashSet<E> result = new HashSet<>();
		for (Collection<? extends E> element : sources) {
			if (element == null) {
				continue;
			}
			result.addAll(element);
		}
		return result;
	}

	/**
	 * Computes the intersection of the two given {@link Collection}s.
	 * <p>
	 * It contains the elements which are contained in both collections. If one element exists twice
	 * in one of the collections, the result contains only one of those.
	 * </p>
	 * <p>
	 * The result is neither guaranteed to be mutable, nor to be a copy (may be identical to one of
	 * the two given collections if it is a {@link Set}).
	 * </p>
	 * 
	 * @param s1
	 *        The first collection. Is not modified by this operation.
	 * @param s2
	 *        The second collection. Is not modified by this operation.
	 * 
	 * @return The entries that are in both {@link Set}s.
	 */
	public static <T> Set<T> intersection(Collection<T> s1, Collection<T> s2) {
		if (s1.isEmpty()) {
			return Collections.emptySet();
		}
		if (s2.isEmpty()) {
			return Collections.emptySet();
		}
		if (s1 == s2) {
			return toSet(s1);
		}
		int s1Size = s1.size();
		int s2Size = s2.size();

		Collection<T> smallerSet;
		Collection<T> largerSet;
		if (s1Size < s2Size) {
			smallerSet = s1;
			largerSet = s2;
		} else {
			smallerSet = s2;
			largerSet = s1;
		}

		HashSet<T> smallCopy = new HashSet<>(smallerSet);
		if (smallCopy.retainAll(largerSet)) {
			return smallCopy;
		} else {
			if (smallerSet instanceof Set<?>) {
				return (Set<T>) smallerSet;
			} else {
				return smallCopy;
			}
		}
	}

	/**
	 * Computes the difference of the two given sets.
	 * 
	 * <p>
	 * The result is neither guaranteed to be mutable, nor to be a copy (may be identical to one of
	 * the two given sets).
	 * </p>
	 * 
	 * @param s1
	 *        The first set. Is not modified by this operation.
	 * @param s2
	 *        The second set. Is not modified by this operation.
	 * @return A set contains only those entries from the first set that are not contained in the
	 *         second set.
	 * 
	 * @see #getAdded(Collection, Collection)
	 * @see #getRemoved(Collection, Collection)
	 */
	public static <T> Set<T> difference(Set<T> s1, Set<?> s2) {
		if (s1.isEmpty()) {
			return Collections.emptySet();
		}
		if (s2.isEmpty()) {
			return s1;
		}
		if (s1 == s2) {
			return Collections.emptySet();
		}
		HashSet<T> result = new HashSet<>(s1);
		if (result.removeAll(s2)) {
			if (result.isEmpty()) {
				// Free memory that was uselessly allocated.
				return Collections.emptySet();
			} else {
				return result;
			}
		} else {
			// Free memory that was uselessly allocated.
			return s1;
		}
	}

	/**
	 * This method returns a {@link Set} which contains the symmetric difference of the given
	 * {@link Set}s, i.e. it contains the elements which are contained in the first but not in the
	 * second set or in the second but not in the first.
	 * 
	 * @param set1
	 *        the first set. may be <code>null</code>.
	 * @param set2
	 *        the second set. may be <code>null</code>.
	 * @return a {@link Set} containing the symmetric difference. never <code>null</code>
	 */
	public static <E> Set<E> symmetricDifference(Set<? extends E> set1, Set<? extends E> set2) {
		if (isEmptyOrNull(set1)) {
			if (isEmptyOrNull(set2)) {
				return Collections.emptySet();
			} else {
				return new HashSet<>(set2);
			}
		} else {
			if (isEmptyOrNull(set2)) {
				return new HashSet<>(set1);
			} else {
				HashSet<E> symmetricDifference = newSet(set1.size() + set2.size());
				for (Iterator<? extends E> iter = set1.iterator(); iter.hasNext();) {
					E next = iter.next();
					if (!set2.contains(next)) {
						symmetricDifference.add(next);
					}
				}
				for (Iterator<? extends E> iter = set2.iterator(); iter.hasNext();) {
					E next = iter.next();
					if (!set1.contains(next)) {
						symmetricDifference.add(next);
					}
				}
				return symmetricDifference;
			}
		}
	}

	/**
	 * Concatenates the given two lists and returns a new modifiable one.
	 * 
	 * @param <T>
	 *        Lower bound on the contents of the arguments.
	 * @param left
	 *        The first part of the result.
	 * @param right
	 *        The second part of the result.
	 * @return The concatenation of the first and second list.
	 */
	public static <T> ArrayList<T> concatNew(List<? extends T> left, List<? extends T> right) {
		ArrayList<T> result = new ArrayList<>(left.size() + right.size());
		result.addAll(left);
		result.addAll(right);
		return result;
	}

	/**
	 * Concatenates the given lists, or returns one of the given lists, if the other one is empty.
	 */
	public static <T> List<T> concat(List<T> left, List<T> right) {
		if (left.isEmpty()) {
			return right;
		}
		if (right.isEmpty()) {
			return left;
		}
		return concatNew(left, right);
	}

	/**
	 * Concatenates the given lists where duplicates are removed, or returns one of the given lists,
	 * if the other one is empty.
	 */
	public static <T> List<T> concatUnique(List<T> left, List<T> right) {
		if (left.isEmpty()) {
			return right;
		}
		if (right.isEmpty()) {
			return left;
		}

		LinkedHashSet<T> result = new LinkedHashSet<>();
		result.addAll(left);
		result.addAll(right);
		return new ArrayList<>(result);
	}

	/**
	 * Completes the given graph with its reflexive hull.
	 * 
	 * @param <N>
	 *        The node type.
	 * @param graph
	 *        A mapping that maps nodes to their children.
	 */
	public static <N> void reflexiveHull(Map<? extends N, Set<N>> graph) {
		for (Entry<? extends N, Set<N>> entry : graph.entrySet()) {
			entry.getValue().add(entry.getKey());
		}
	}

	/**
	 * Completes the given graph with its transitive hull.
	 * 
	 * <p>
	 * Precondition: The given graph must be acyclic.
	 * </p>
	 * 
	 * @param <N>
	 *        The node type.
	 * @param graph
	 *        A mapping that maps nodes to their children.
	 */
	public static <N> void transitiveHullAcyclic(Map<?, Set<N>> graph) {
		HashSet<Object> done = new HashSet<>();
		for (Object node : graph.keySet()) {
			transitiveHullAcyclicProcess(graph, done, node);
		}
	}

	private static <N> Set<N> transitiveHullAcyclicProcess(Map<?, Set<N>> graph, Set<Object> done, Object node) {
		Set<N> directChildren = graph.get(node);
		if (directChildren != null && (!directChildren.isEmpty())) {
			if (!done.contains(node)) {
				done.add(node);
				for (N child : new ArrayList<>(directChildren)) {
					Set<N> indirectChildren = transitiveHullAcyclicProcess(graph, done, child);
					if (indirectChildren != null && (!indirectChildren.isEmpty())) {
						directChildren.addAll(indirectChildren);
					}
				}
			}
		}
		return directChildren;
	}

	/**
	 * Sort the given input values topologically.
	 * 
	 * <p>
	 * In topological order, a dependency occur before the element that depends on it.
	 * </p>
	 * 
	 * @param <T>
	 *        The type of input elements.
	 * @param dependencies
	 *        A function that reports dependencies for a given element.
	 * @param input
	 *        The elements to sort.
	 * @param addDependencies
	 *        Whether missing dependencies should be added.
	 * @return The topologically sorted mutable list of elements.
	 * 
	 * @throws IllegalArgumentException
	 *         If the given graph is cyclic.
	 */
	public static <T> List<T> topsort(Function<T, ? extends Iterable<? extends T>> dependencies, Collection<T> input,
			boolean addDependencies) throws CyclicDependencyException {
		Set<T> inputSet = addDependencies ? null : new HashSet<>(input);
		List<T> result = new ArrayList<>();

		HashSet<T> seen = new HashSet<>();
		LinkedHashSet<T> pending = new LinkedHashSet<>();
		for (T element : input) {
			addInTopologicalOrder(dependencies, result, seen, pending, element, inputSet, addDependencies);
		}

		return result;
	}

	private static <T> void addInTopologicalOrder(Function<T, ? extends Iterable<? extends T>> dependencies,
			List<T> result, Set<T> seen, Set<T> pending, T element, Set<T> input, boolean addDependencies) {
		if (seen.contains(element)) {
			if (pending.contains(element)) {
				ArrayList<T> cycle = new ArrayList<>(pending);
				cycle.add(element);

				throw new CyclicDependencyException(cycle);
			}
			return;
		}
		seen.add(element);
		pending.add(element);
		for (T dependency : dependencies.apply(element)) {
			addInTopologicalOrder(dependencies, result, seen, pending, dependency, input, addDependencies);
		}
		pending.remove(element);
		if (addDependencies || input.contains(element)) {
			result.add(element);
		}
	}

	/**
	 * Finds the position where the entry should be inserted in the given sorted list.
	 * <p>
	 * If there are entries equal to the given entry, the insert position is after those entries.
	 * </p>
	 * 
	 * @param list
	 *        <em>Has to be in ascending order</em> according to the {@link Comparator}. Is allowed
	 *        to be null.
	 * @param entry
	 *        The object to insert.
	 * @param order
	 *        Is not allowed to be null.
	 */
	public static <T> int insertPosition(List<? extends T> list, T entry, Comparator<? super T> order) {
		if (order == null) {
			/* Without the explicit check, the NPE is only thrown for some inputs, making it a hard
			 * to reproduce bug. This way, it is always thrown, making it much easier to find. */
			throw new NullPointerException();
		}
		if (isEmptyOrNull(list)) {
			return 0;
		}
		int position = Collections.<T> binarySearch(list, entry, order);
		int insertPosition;
		if (position >= 0) {
			// Specified order is not sufficient to decide insert position. Insert after the last
			// object that is equal to the new object with respect to the current order.
			do {
				position++;
			} while (position < list.size() && order.compare(list.get(position), entry) == 0);
			insertPosition = position;
		} else {
			insertPosition = -position - 1;
		}
		return insertPosition;
	}

	/**
	 * Shrinks the given list to the given size.
	 * 
	 * <p>
	 * This method may return a view of the given list or a copy.
	 * </p>
	 * 
	 * @param <T>
	 *        The element type.
	 * @param list
	 *        The list to shrink.
	 * @param newSize
	 *        The new size for the given list.
	 * @return The shrunk list.
	 */
	public static <T> List<T> shrinkOptimized(List<T> list, int newSize) {
		int currentSize = list.size();
		if (newSize == currentSize) {
			return list;
		}
		assert currentSize > newSize;

		if (list instanceof ArrayList<?>) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			ArrayList<T> arrayList = (ArrayList) list;

			shrink(arrayList, newSize);

			return arrayList;
		} else {
			return new ArrayList<>(list.subList(0, newSize));
		}
	}

	/**
	 * Shrinks the given list to the given size.
	 * 
	 * @param <T>
	 *        The element type.
	 * @param arrayList
	 *        The list to modify.
	 * @param newSize
	 *        The new size for the given list.
	 */
	public static <T> void shrink(ArrayList<T> arrayList, int newSize) {
		int currentSize = arrayList.size();
		for (int n = currentSize - 1; n >= newSize; n--) {
			arrayList.remove(n);
		}

		if (newSize < currentSize / 2 && newSize > 16) {
			// Time-storage trade-off.
			arrayList.trimToSize();
		}
	}

	/** @see IteratorUtilShared#toIterable(Iterator) */
	public static <T> Iterable<T> toIterable(final Iterator<T> iterator) {
		return IteratorUtilShared.toIterable(iterator);
	}

	/**
	 * Replaces a <code>null</code> argument with {@link Collections#emptyList()}.
	 */
	public static <T> List<T> nonNull(List<T> value) {
		if (value == null) {
			return Collections.emptyList();
		} else {
			return value;
		}
	}

	/**
	 * Replaces a <code>null</code> argument with {@link Collections#emptyList()}.
	 */
	public static <T> Iterable<T> nonNull(Iterable<T> value) {
		if (value == null) {
			return Collections.emptyList();
		} else {
			return value;
		}
	}

	/**
	 * Replaces a <code>null</code> argument with {@link Collections#emptyList()}.
	 */
	public static <T> Collection<T> nonNull(Collection<T> value) {
		if (value == null) {
			return Collections.emptyList();
		} else {
			return value;
		}
	}

	/**
	 * Replaces a <code>null</code> argument with {@link Collections#emptySet()}.
	 */
	public static <T> Set<T> nonNull(Set<T> value) {
		if (value == null) {
			return Collections.emptySet();
		} else {
			return value;
		}
	}

	/**
	 * Replaces a <code>null</code> argument with {@link Collections#emptyMap()}.
	 */
	public static <K, V> Map<K, V> nonNull(Map<K, V> value) {
		if (value == null) {
			return Collections.emptyMap();
		} else {
			return value;
		}
	}

	/**
	 * Groups elements from a given {@link Iterator} into non-empty chunks of a given size.
	 * 
	 * <p>
	 * Note: A returned chunk is only valid until the next call to {@link Iterator#hasNext()} or
	 * {@link Iterator#next()} of the returned chunk iterator.
	 * </p>
	 * 
	 * <p>
	 * The returned {@link Iterator} does not support {@link Iterator#remove()}.
	 * </p>
	 * 
	 * <p>
	 * Usage e.g.:
	 * </p>
	 * 
	 * <pre>
	 * for (List&lt;Object&gt; chunk : toIterable(chunk(chunkSize, iterator))) {
	 * 	// Prepare for processing chunk.
	 * 
	 * 	for (Object instance : chunk) {
	 * 		// Process chunk contents.
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param chunkSize
	 *        The size of chunks to group iterated objects in. Must be &gt;0.
	 * @param iterator
	 *        The source of elements for grouping.
	 * @return An iterator of chunks.
	 * 
	 * @throws IndexOutOfBoundsException
	 *         if <code>chunkSize &lt;= 0</code>.
	 */
	public static <T> Iterator<List<T>> chunk(final int chunkSize, final Iterator<? extends T> iterator) {
		if (chunkSize <= 0) {
			throw new IndexOutOfBoundsException("Chunk size must be larger than 0: " + chunkSize);
		}
		return new Iterator<List<T>>() {
			private ArrayList<T> buffer = new ArrayList<>(chunkSize);

			private boolean isBuffered = false;

			private void fill() {
				// Can be differ from chunk size because returned buffer can be modified.
				int size = buffer.size();
				int index = 0;
				// fill buffer
				while (index < chunkSize && iterator.hasNext()) {
					T e = iterator.next();
					if (index < size) {
						buffer.set(index, e);
					} else {
						buffer.add(e);
					}
					index++;
				}
				if (index < size) {
					// clear tail of buffer
					if (index == 0) {
						buffer.clear();
					} else {
						for (int removeIndex = size - 1; removeIndex >= index; removeIndex--) {
							buffer.remove(removeIndex);
						}
					}
				}

				isBuffered = true;
			}

			@Override
			public boolean hasNext() {
				if (!isBuffered) {
					fill();
				}

				return !buffer.isEmpty();
			}

			@Override
			public List<T> next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}

				isBuffered = false;
				return buffer;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Moves the entry at index <code>from</code> to the index <code>to</code> in the given list.
	 * <p>
	 * Requires O(n) operations, but works even if the given {@link List} does not support
	 * {@link List#remove(int)}.
	 * </p>
	 * <p>
	 * All elements of the given list between the given indices (inclusive) change their position.
	 * </p>
	 * 
	 * @param list
	 *        The list to modify.
	 * @param from
	 *        The index of the element to move.
	 * @param to
	 *        The index at which the moved element is located after this method returns.
	 */
	public static <T> void moveEntry(List<T> list, int from, int to) {
		checkIndexElement(list, from);
		checkIndexElement(list, to);

		int distance = to - from;
		if (distance == 0) {
			return;
		}

		int direction = distance > 0 ? 1 : -1;

		T movedEntry = list.get(from);
		for (int n = from; n != to;) {
			int next = n + direction;
			list.set(n, list.get(next));
			n = next;
		}
		list.set(to, movedEntry);
	}

	private static void checkIndexElement(Collection<?> collection, int index) {
		if (index < 0) {
			throw new IndexOutOfBoundsException(index + " < " + 0);
		}

		if (index >= collection.size()) {
			throw new IndexOutOfBoundsException(index + " >= " + collection.size());
		}
	}

	/**
	 * Compares two <code>int</code> values compatible with
	 * {@link Comparator#compare(Object, Object)}.
	 * 
	 * <p>
	 * Note: For comparing <code>int</code>s, the values must not be subtracted from each other: An
	 * integer over- or underflow would violate the transitivity property of the {@link Comparator},
	 * otherwise.
	 * </p>
	 * 
	 * @see Integer#compareTo(Integer)
	 */
	public static int compareInt(int v1, int v2) {
		return v1 < v2 ? -1 : (v1 == v2 ? 0 : 1);
	}

	/**
	 * @see Long#compareTo(Long)
	 */
	public static int compareLong(long v1, long v2) {
		return v1 < v2 ? -1 : (v1 == v2 ? 0 : 1);
	}

	/**
	 * Null-safe version of {@link Comparable#compareTo(Object)}.
	 * 
	 * @param o1
	 *        The first operand or <code>null</code>.
	 * @param o2
	 *        The second operand or <code>null</code>.
	 * @return The result of {@link Comparable#compareTo(Object)} of the first operand using the
	 *         second operand as argument, if both operands are non-<code>null</code>.
	 *         <code>0</code>, if both operands are <code>null</code>. <code>-1</code>, if the first
	 *         operand is <code>null</code> but the second operand is non- <code>null</code>.
	 *         <code>1</code>, if the second operand is <code>null</code>, but the first operand is
	 *         non-<code>null</code>.
	 */
	public static <T extends Comparable<T>> int compareComparableNullIsSmaller(T o1, T o2) {
		if (o1 == o2) {
			return 0;
		}
		if (o1 == null) {
			return -1;
		}
		if (o2 == null) {
			return 1;
		}

		return o1.compareTo(o2);
	}

	/**
	 * Null-safe version of {@link Comparable#compareTo(Object)}.
	 * 
	 * @param o1
	 *        The first operand or <code>null</code>.
	 * @param o2
	 *        The second operand or <code>null</code>.
	 * @return The result of {@link Comparable#compareTo(Object)} of the first operand using the
	 *         second operand as argument, if both operands are non-<code>null</code>.
	 *         <code>0</code>, if both operands are <code>null</code>. <code>1</code>, if the first
	 *         operand is <code>null</code> but the second operand is non- <code>null</code>.
	 *         <code>-1</code>, if the second operand is <code>null</code>, but the first operand is
	 *         non-<code>null</code>.
	 */
	public static <T extends Comparable<T>> int compareComparableNullIsGreater(T o1, T o2) {
		if (o1 == o2) {
			return 0;
		}
		if (o1 == null) {
			return 1;
		}
		if (o2 == null) {
			return -1;
		}

		return o1.compareTo(o2);
	}

	/**
	 * @see Long#hashCode()
	 */
	public static int hashCodeLong(long value) {
		return (int) (value ^ (value >>> 32));
	}

	/**
	 * Checks whether the given {@link Iterable} has elements
	 * 
	 * @param elements
	 *        the {@link Iterable} to check.
	 * 
	 * @return Whether the {@link Iterator} returned by the {@link Iterable} has
	 *         {@link Iterator#next() any element}.
	 */
	public static boolean isEmpty(Iterable<?> elements) {
		if (elements instanceof Collection<?>) {
			return ((Collection<?>) elements).isEmpty();
		}
		return !elements.iterator().hasNext();
	}

	/**
	 * Adds all elements in the given {@link Iterable} to the collection.
	 * 
	 * <p>
	 * Alternative for missing API <code>Collection#addAll(Iterable)</code>.
	 * </p>
	 * 
	 * @param c
	 *        the collection into which <code>elements</code> are to be inserted
	 * @param elements
	 *        the elements to insert into <code>c</code>
	 * 
	 * @return Whether the collection changed as a result of the call.
	 * 
	 * @see Collection#addAll(Collection)
	 * @see #addAll(Collection, Iterator)
	 */
	public static <T> boolean addAll(Collection<T> c, Iterable<? extends T> elements) {
		boolean result = false;
		for (T element : elements) {
			result |= c.add(element);
		}
		return result;
	}

	/**
	 * Adds all elements in the given {@link Iterator} to the collection.
	 * 
	 * <p>
	 * Alternative for missing API <code>Collection#addAll(Iterator)</code>.
	 * </p>
	 * 
	 * @param c
	 *        the collection into which <code>elements</code> are to be inserted
	 * @param elements
	 *        the elements to insert into <code>c</code>
	 * 
	 * @return Whether the collection changed as a result of the call.
	 * 
	 * @see Collection#addAll(Collection)
	 * @see #addAll(Collection, Iterable)
	 */
	public static <T> boolean addAll(Collection<T> c, Iterator<? extends T> elements) {
		boolean result = false;
		while (elements.hasNext()) {
			result |= c.add(elements.next());
		}
		return result;
	}

	/**
	 * Removes all elements in the given {@link Iterable} from the collection.
	 * 
	 * <p>
	 * Alternative for missing API <code>Collection#removeAll(Iterable)</code>.
	 * </p>
	 * 
	 * @param c
	 *        the collection from which <code>elements</code> are to be removed
	 * @param elements
	 *        the elements to remove from <code>c</code>
	 * 
	 * @return <code>true</code> if the collection changed as a result of the call
	 * 
	 * @see Collection#removeAll(Collection)
	 * @see #removeAll(Collection, Iterator)
	 */
	public static <T> boolean removeAll(Collection<T> c, Iterable<?> elements) {
		boolean result = false;
		for (Object element : elements) {
			result |= c.remove(element);
		}
		return result;
	}

	/**
	 * Removes all elements in the given {@link Iterator} from the collection.
	 * 
	 * <p>
	 * Alternative for missing API <code>Collection#removeAll(Iterator)</code>.
	 * </p>
	 * 
	 * @param c
	 *        the collection from which <code>elements</code> are to be removed
	 * @param elements
	 *        the elements to remove from <code>c</code>
	 * 
	 * @return <code>true</code> if the collection changed as a result of the call
	 * 
	 * @see Collection#removeAll(Collection)
	 * @see #removeAll(Collection, Iterable)
	 */
	public static <T> boolean removeAll(Collection<T> c, Iterator<?> elements) {
		boolean result = false;
		while (elements.hasNext()) {
			result |= c.remove(elements.next());
		}
		return result;
	}

	/**
	 * Returns the number of elements in the given {@link Iterable}.
	 * 
	 * @param elements
	 *        The {@link Iterable} to check.
	 * 
	 * @return Number of elements in <code>elements</code>.
	 */
	public static int size(Iterable<?> elements) {
		if (elements instanceof Collection<?>) {
			return ((Collection<?>) elements).size();
		}
		return size(elements.iterator());
	}

	/**
	 * Counts the number of elements returned by the given {@link Iterator}.
	 * 
	 * @param iterator
	 *        The {@link Iterator} to iterate.
	 * @return The number of elements returned from the given {@link Iterator}.
	 */
	public static int size(Iterator<?> iterator) {
		int size = 0;
		while (iterator.hasNext()) {
			iterator.next();
			size++;
		}
		return size;
	}

	/**
	 * Utility for optimizing a large number of {@link List#contains(Object)} operations.
	 * 
	 * @param expectedCheckCount
	 *        The number of expected {@link Collection#contains(Object)} calls.
	 * @param potentialList
	 *        The {@link List} on which {@link List#contains(Object)} should be called.
	 * @return The {@link Collection} to call {@link Collection#contains(Object)} on instead.
	 */
	public static Collection<?> toContainsChecker(int expectedCheckCount, Collection<?> potentialList) {
		Collection<?> containsChecker;

		// Experimentally determined trade-off between allocation cost and iteration cost, see
		// BenchmarkToContainsChecker.
		if (expectedCheckCount >= 32 && ((long) expectedCheckCount) * potentialList.size() > 65536) {
			// Use a set to efficiently test containment in a large collection multiple times.
			containsChecker = toSet(potentialList);
		} else {
			// A small-size list is more efficient as a corresponding set.
			containsChecker = potentialList;
		}

		return containsChecker;
	}

	/**
	 * Whether the common elements of the two {@link Collection}s have the same order.
	 * <p>
	 * All elements not in both {@link Collection}s are completely ignored.
	 * </p>
	 * <p>
	 * Duplicate elements are ignored, only the first occurrence of each element is relevant.
	 * </p>
	 * 
	 * @param first
	 *        Null is treated as an empty {@link Collection}.
	 * @param second
	 *        Null is treated as an empty {@link Collection}.
	 */
	public static <E> boolean haveCommonElementsSameOrder(Collection<? extends E> first,
			Collection<? extends E> second) {
		LinkedHashSet<E> firstSet = linkedSet(first);
		LinkedHashSet<E> secondSet = linkedSet(second);
		firstSet.retainAll(secondSet);
		secondSet.retainAll(firstSet);
		return firstSet.equals(secondSet);
	}

	/**
	 * Adds the given value to a lazily allocated list.
	 * 
	 * @param lazyList
	 *        The list to add to or <code>null</code>, if a new list should be allocated.
	 * @param value
	 *        The value to add.
	 * @return The potentially allocated list.
	 * 
	 * @see #nonNull(List)
	 */
	public static <T> List<T> lazyAdd(List<T> lazyList, T value) {
		lazyList = allocate(lazyList);
		lazyList.add(value);
		return lazyList;
	}

	/**
	 * Adds the given values to a lazily allocated list.
	 * 
	 * @param lazyList
	 *        The list to add to or <code>null</code>, if a new list should be allocated.
	 * @param values
	 *        The values to add, may be <code>null</code> or empty.
	 * @return The potentially allocated list.
	 * 
	 * @see #nonNull(List)
	 */
	public static <T> List<T> lazyAddAll(List<T> lazyList, Collection<T> values) {
		if (isEmptyOrNull(values)) {
			return lazyList;
		}
		lazyList = allocate(lazyList);
		lazyList.addAll(values);
		return lazyList;
	}

	private static <T> List<T> allocate(List<T> lazyList) {
		if (lazyList == null) {
			lazyList = new ArrayList<>();
		}
		return lazyList;
	}

	/**
	 * Adds the given value to a lazily allocated set.
	 * 
	 * @param lazySet
	 *        The set to add to or <code>null</code>, if a new set should be allocated.
	 * @param value
	 *        The value to add.
	 * @return The potentially allocated set.
	 * 
	 * @see #nonNull(Set)
	 */
	public static <T> Set<T> lazyAdd(Set<T> lazySet, T value) {
		lazySet = allocate(lazySet);
		lazySet.add(value);
		return lazySet;
	}

	/**
	 * Adds the given values to a lazily allocated set.
	 * 
	 * @param lazySet
	 *        The set to add to or <code>null</code>, if a new set should be allocated.
	 * @param values
	 *        The values to add, may be <code>null</code> or empty.
	 * @return The potentially allocated set.
	 * 
	 * @see #nonNull(Set)
	 */
	public static <T> Set<T> lazyAddAll(Set<T> lazySet, Collection<T> values) {
		if (isEmptyOrNull(values)) {
			return lazySet;
		}
		lazySet = allocate(lazySet);
		lazySet.addAll(values);
		return lazySet;
	}

	private static <T> Set<T> allocate(Set<T> lazySet) {
		if (lazySet == null) {
			lazySet = new HashSet<>();
		}
		return lazySet;
	}

	/**
	 * Puts the given key value pair to a lazily allocated map.
	 * 
	 * @param lazyMap
	 *        The map to put to or <code>null</code>, if a new map should be allocated.
	 * @param key
	 *        The key to put.
	 * @param value
	 *        The value to put.
	 * @return The potentially allocated map.
	 * 
	 * @see #nonNull(Map)
	 */
	public static <K, V> Map<K, V> lazyPut(Map<K, V> lazyMap, K key, V value) {
		lazyMap = allocate(lazyMap);
		lazyMap.put(key, value);
		return lazyMap;
	}

	/**
	 * Puts the given values to a lazily allocated map.
	 * 
	 * @param lazyMap
	 *        The map to add to or <code>null</code>, if a new map should be allocated.
	 * @param values
	 *        The values to put, may be <code>null</code> or empty.
	 * @return The potentially allocated map.
	 * 
	 * @see #nonNull(Map)
	 */
	public static <K, V> Map<K, V> lazyPutAll(Map<K, V> lazyMap, Map<? extends K, ? extends V> values) {
		if (isEmptyOrNull(values)) {
			return lazyMap;
		}
		lazyMap = allocate(lazyMap);
		lazyMap.putAll(values);
		return lazyMap;
	}

	private static <K, V> Map<K, V> allocate(Map<K, V> lazyMap) {
		if (lazyMap == null) {
			lazyMap = new HashMap<>();
		}
		return lazyMap;
	}

	/**
	 * Removes the first n elements from the given {@link Collection}.
	 * 
	 * @param input
	 *        Null is equivalent to an empty collection.
	 * @param n
	 *        Is not allowed to be negative. 0 is allowed and means no element is removed, but a new
	 *        list object is still created.
	 * @return Never null. A new, mutable and resizable {@link List} which shares no state with the
	 *         given input.
	 */
	public static <T> List<T> removePrefix(Collection<? extends T> input, int n) {
		List<? extends T> inputList = list(input);
		List<? extends T> sublist = inputList.subList(n, inputList.size());
		/* Sublist is just a view of the original list and therefore keeps a reference to it. The
		 * removed elements can therefore not be garbage collected. To fix that, a new List object
		 * is created, to allow the garbage collector to remove the unnecessary elements. */
		return list(sublist);
	}

	/**
	 * Removes the last n elements from the given {@link Collection}.
	 * 
	 * @param input
	 *        Null is equivalent to an empty collection.
	 * @param n
	 *        Is not allowed to be negative. 0 is allowed and means no element is removed, but a new
	 *        list object is still created.
	 * @return Never null. A new, mutable and resizable {@link List} which shares no state with the
	 *         given input.
	 */
	public static <T> List<T> removeSuffix(Collection<? extends T> input, int n) {
		List<? extends T> inputAsList = list(input);
		List<? extends T> resultList = inputAsList.subList(0, input.size() - n);
		/* See: removePrefix for the explanation why the new list object is necessary. */
		return list(resultList);
	}

	/**
	 * The prefix of the too {@link Collection} where the elements are pairwise equal according to
	 * their {@link Object#equals(Object)} implementation.
	 * 
	 * @param first
	 *        Null is equivalent to an empty {@link Collection}. Null entries are allowed.
	 * @param second
	 *        Null is equivalent to an empty {@link Collection}. Null entries are allowed.
	 * @return Never null. A new, mutable and resizable {@link List} which shares no state with the
	 *         given input.
	 */
	public static <T> List<T> getCommonPrefix(Collection<? extends T> first, Collection<? extends T> second) {
		List<T> result = list();
		Iterator<? extends T> iteratorFirst = nonNull(first).iterator();
		Iterator<? extends T> iteratorSecond = nonNull(second).iterator();
		while (iteratorFirst.hasNext() && iteratorSecond.hasNext()) {
			T entryFirst = iteratorFirst.next();
			T entrySecond = iteratorSecond.next();
			if (!Objects.equals(entryFirst, entrySecond)) {
				break;
			}
			result.add(entryFirst);
		}
		return result;
	}

	/**
	 * Whether all objects in the given {@link Collection} are equal to each other.
	 * 
	 * @param collection
	 *        Null is equivalent to the empty {@link Collection}.
	 */
	public static boolean isEverythingEqual(Collection<?> collection) {
		return isEverythingEqual(collection, Objects::equals);
	}

	/**
	 * Whether all objects in the given {@link Collection} are equal to each other, according to the
	 * given {@link BiPredicate}.
	 * 
	 * @param collection
	 *        Null is equivalent to the empty {@link Collection}.
	 */
	public static <E> boolean isEverythingEqual(Collection<? extends E> collection, BiPredicate<E, E> equality) {
		if (isEmptyOrNull(collection)) {
			return true;
		}
		E first = getFirst(collection);
		for (E other : collection) {
			if (!equality.test(first, other)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Flattens the given {@link Collection} of {@link Collection}s to a {@link List}.
	 *
	 * @param nestedCollection
	 *        Null is equivalent to the empty {@link List}, for both the outer and the inner
	 *        {@link Collection}. Null as a value in the inner {@link Collection}s is treated as a
	 *        normal value.
	 * @return Never null. A new, mutable and resizable {@link List}.
	 */
	public static <E> List<E> flatten(Collection<? extends Collection<? extends E>> nestedCollection) {
		List<E> result = list();
		for (Collection<? extends E> inner : nonNull(nestedCollection)) {
			result.addAll(nonNull(inner));
		}
		return result;
	}

	/** Variant of {@link #max(Collection)} for arrays. */
	@SafeVarargs
	public static <T extends Comparable<T>> T max(T... values) {
		return reduce(CollectionUtilShared::maxUnsafe, values);
	}

	/** Returns null, if the {@link Collection} is null or empty, or contains only nulls. */
	public static <T extends Comparable<T>> T max(Collection<? extends T> values) {
		// Note: Java 11 requires a variable for the reduction to accept the types.
		BinaryOperator<T> reduction = CollectionUtilShared::maxUnsafe;
		return reduce(reduction, values);
	}

	/** The arguments must not be null. */
	private static <T extends Comparable<T>> T maxUnsafe(T a, T b) {
		return (a.compareTo(b) > 0) ? a : b;
	}

	/** Variant of {@link #min(Collection)} for arrays. */
	@SafeVarargs
	public static <T extends Comparable<T>> T min(T... values) {
		return reduce(CollectionUtilShared::minUnsafe, values);
	}

	/** Returns null, if the {@link Collection} is null or empty, or contains only nulls. */
	public static <T extends Comparable<T>> T min(Collection<? extends T> values) {
		// Note: Java 11 requires a variable for the reduction to accept the types.
		BinaryOperator<T> reduction = CollectionUtilShared::minUnsafe;
		return reduce(reduction, values);
	}

	/** The arguments must not be null. */
	private static <T extends Comparable<T>> T minUnsafe(T a, T b) {
		return (a.compareTo(b) < 0) ? a : b;
	}

	/** Variant of {@link #sum(Collection)} for arrays. */
	@SafeVarargs
	public static double sum(Number... values) {
		Number result = reduce(CollectionUtilShared::doubleUnsafe, values);
		if (result == null) {
			return 0;
		}
		return result.doubleValue();
	}

	/** Returns 0, if the {@link Collection} is null or empty, or contains only nulls. */
	public static double sum(Collection<? extends Number> values) {
		Number result = reduce(CollectionUtilShared::doubleUnsafe, values);
		if (result == null) {
			return 0;
		}
		return result.doubleValue();
	}

	/** The arguments must not be null. */
	private static Number doubleUnsafe(Number a, Number b) {
		return Double.sum(a.doubleValue(), b.doubleValue());
	}

	/** Variant of {@link #reduce(BinaryOperator, Collection)} for arrays. */
	@SafeVarargs
	public static <T> T reduce(BinaryOperator<T> reduction, T... values) {
		if ((values == null) || (values.length == 0)) {
			return null;
		}
		if (values.length == 1) {
			return values[0];
		}
		if (values.length == 2) {
			// Optimization for the most common use case.
			return reduceTwo(reduction, values[0], values[1]);
		}
		return reduceNonTrivial(reduction, Arrays.asList(values));
	}

	/** Returns null, if the {@link Collection} is null or empty, or contains only nulls. */
	public static <T> T reduce(BinaryOperator<T> reduction, Collection<? extends T> values) {
		if (isEmptyOrNull(values)) {
			return null;
		}
		if (size(values) == 1) {
			return getFirst(values);
		}
		return reduceNonTrivial(reduction, values);
	}

	private static <T> T reduceNonTrivial(BinaryOperator<T> reduction, Collection<? extends T> values) {
		T result = null;
		for (T current : values) {
			result = reduceTwo(reduction, result, current);
		}
		return result;
	}

	private static <T> T reduceTwo(BinaryOperator<T> reduction, T a, T b) {
		if (a == null) {
			return b;
		}
		if (b == null) {
			return a;
		}
		return reduction.apply(a, b);
	}

	/**
	 * The prefix of the too {@link Collection} where the elements are pairwise equal according to
	 * their {@link Object#equals(Object)} implementation.
	 * 
	 * @param first
	 *        Null is equivalent to an empty {@link Collection}. Null entries are allowed.
	 * @param second
	 *        Null is equivalent to an empty {@link Collection}. Null entries are allowed.
	 * @return Never null. A new, mutable and resizable {@link List} which shares no state with the
	 *         given input.
	 */
	public static <T> List<T> getCommonSuffix(Collection<? extends T> first, Collection<? extends T> second) {
		return reverse(getCommonPrefix(reverse(first), reverse(second)));
	}

	/**
	 * Returns a {@link List} with the elements from that are in the new {@link Collection} but not
	 * in the old {@link Collection}.
	 * <p>
	 * The differences to {@link #difference(Set, Set)} are:
	 * <ul>
	 * <li>The order of the entries is maintained.</li>
	 * <li>The number of occurrences of an element in the result of this method is the difference
	 * between the number of occurrences in the new entries and the old entries.</li>
	 * <li>This method treats null as equivalent to an empty {@link Collection}.</li>
	 * <li>This method always returns a new object, never one of the inputs.</li>
	 * <li>This method always returns a mutable and resizable {@link List}.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param oldEntries
	 *        Null is equivalent to an empty collection.
	 * @param newEntries
	 *        Null is equivalent to an empty collection.
	 * @return Never null. A new, mutable and resizable {@link List} which shares no state with the
	 *         given input.
	 * 
	 * @see #getRemoved(Collection, Collection)
	 */
	public static <T> List<T> getAdded(Collection<? extends T> oldEntries, Collection<? extends T> newEntries) {
		/* Reverse the list, as there is no "removeLast": */
		List<T> addedEntries = reverse(newEntries);
		for (T oldEntry : nonNull(oldEntries)) {
			addedEntries.remove(oldEntry);
		}
		return reverse(addedEntries);
	}

	/**
	 * Returns a {@link List} with the elements from that are in the old {@link Collection} but not
	 * in the new {@link Collection}.
	 * <p>
	 * The differences to {@link #difference(Set, Set)} are:
	 * <ul>
	 * <li>The order of the entries is maintained.</li>
	 * <li>The number of occurrences of an element in the result of this method is the difference
	 * between the number of occurrences in the old entries and the new entries.</li>
	 * <li>This method treats null as equivalent to an empty {@link Collection}.</li>
	 * <li>This method always returns a new object, never one of the inputs.</li>
	 * <li>This method always returns a mutable and resizable {@link List}.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param oldEntries
	 *        Null is equivalent to an empty collection.
	 * @param newEntries
	 *        Null is equivalent to an empty collection.
	 * @return Never null. A new, mutable and resizable {@link List} which shares no state with the
	 *         given input.
	 * 
	 * @see #getAdded(Collection, Collection)
	 */
	public static <T> List<T> getRemoved(Collection<? extends T> oldEntries, Collection<? extends T> newEntries) {
		List<T> removedEntries = reverse(oldEntries);
		for (T newEntry : nonNull(newEntries)) {
			removedEntries.remove(newEntry);
		}
		return reverse(removedEntries);
	}

	/**
	 * Convenience variant of {@link Collections#reverse(List)}, which returns a new {@link List}
	 * instead of changing the given one.
	 * 
	 * @param input
	 *        Null is equivalent to an empty collection.
	 * @return Never null. A new, mutable and resizable {@link List} which shares no state with the
	 *         given input.
	 */
	public static <T> List<T> reverse(Collection<? extends T> input) {
		List<T> result = list(input);
		Collections.reverse(result);
		return result;
	}

	/**
	 * Returns an immutable list. This contains only the specified object, or none, depending on
	 * whether the element is non-<code>null</code> or <code>null</code>.
	 * 
	 * <p>
	 * The returned list is serializable and {@link RandomAccess}.
	 * </p>
	 *
	 * @param <T>
	 *        The class of the objects in the list.
	 * @param o
	 *        The sole object to be stored in the returned list, or <code>null</code> to get an
	 *        empty list.
	 * @return An immutable list containing only the specified object.
	 * 
	 * @see #singletonOrEmptySet(Object)
	 */
	public static <T> List<T> singletonOrEmptyList(T o) {
		if (o == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(o);
	}

	/**
	 * Returns an immutable set. This contains only the specified object, or none, depending on
	 * whether the element is non-<code>null</code> or <code>null</code>.
	 *
	 * <p>
	 * The returned set is serializable.
	 * </p>
	 *
	 * @param <T>
	 *        The class of the objects in the set.
	 * @param o
	 *        The sole object to be stored in the returned set, or <code>null</code> to get an empty
	 *        set.
	 * @return An immutable set containing only the specified object.
	 * 
	 * @see #singletonOrEmptyList(Object)
	 */
	public static <T> Set<T> singletonOrEmptySet(T o) {
		if (o == null) {
			return Collections.emptySet();
		}
		return Collections.singleton(o);
	}

}
