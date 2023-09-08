/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.shared.collection.map;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.nonNull;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.basic.shared.string.StringServicesShared.*;
import static java.util.Arrays.*;
import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.basic.shared.collection.factory.CollectionFactoryShared;

/**
 * The MapUtil contains useful static methods for maps of Lists or Sets.
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class MapUtilShared {

	/** The default separator for the append methods. */
	public static final String SEPARATOR = ";";

	/** The default load factor of new HashMaps. */
	public static final float DEFAULT_LOAD_FACTOR = 0.75f;

	/**
	 * Creates a new map which is big enough to hold the given amount of elements without need of
	 * resizing. This is required because in a map created with new HashMap(size) can only be put
	 * size * DEFAULT_LOAD_FACTOR (= size * 0.75) elements without resizing the map.
	 *
	 * @see CollectionUtilShared#newSet(int)
	 * @param size
	 *        the requested capacity of the new map
	 * @return the new HashMap with the specified capacity
	 */
	public static final <K, V> HashMap<K, V> newMap(int size) {
		return new HashMap<>((int) (size / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a new map which is big enough to hold the given amount of elements without need of
	 * resizing. This is required because in a map created with new HashMap(size) can only be put
	 * size * DEFAULT_LOAD_FACTOR (= size * 0.75) elements without resizing the map.
	 *
	 * @see CollectionUtilShared#newSet(int)
	 * @param size
	 *        the requested capacity of the new map
	 * @return the new {@link LinkedHashMap} with the specified capacity
	 */
	public static final <K, V> LinkedHashMap<K, V> newLinkedMap(int size) {
		return new LinkedHashMap<>((int) (size / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * This method stores the given object into the list with the given key.
	 * 
	 * If the map does not contains a list for the given key, a new {@link ArrayList} will be
	 * created.
	 * 
	 * @param aMap
	 *        must not be <code>null</code>.
	 * @param aKey
	 *        must not be <code>null</code>.
	 * @param anObject
	 *        The Object to store in a List inside the map.
	 */
	public static <K, V> void addObject(Map<K, List<V>> aMap, K aKey, V anObject) {
		List<V> theList = aMap.get(aKey);

		// Create a new list and store it under the given key
		if (theList == null) {
			theList = new ArrayList<>();
			aMap.put(aKey, theList);
		}

		// Append the object to the list for the given key
		theList.add(anObject);
	}

	/**
	 * This method stores the given object into a list set for the given key.
	 * 
	 * If the map does not contains a set for the given key, a new {@link Set} will be created.
	 * 
	 * @param aMap
	 *        must not be <code>null</code>.
	 * @param aKey
	 *        must not be <code>null</code>.
	 * @param anObject
	 *        The Object to store in the Set inside the Map.
	 */
	public static <K, V> void addObjectToSet(Map<K, Set<V>> aMap, K aKey, V anObject) {
		Set<V> theSet = aMap.get(aKey);

		// Create a new set and store it under the given key
		if (theSet == null) {
			theSet = new HashSet<>();
			aMap.put(aKey, theSet);
		}

		// Add the object to the set for the given key
		theSet.add(anObject);
	}

	/**
	 * This method stores the given object into a set for the given key.
	 * 
	 * If the map does not contains a set for the given key, a new {@link Set} will be created.
	 * 
	 * @param aMap
	 *        must not be <code>null</code>.
	 * @param aKey
	 *        must not be <code>null</code>.
	 * @param aComp
	 *        The comparator for the TreeSet; may be <code>null</code> Using different Comparators
	 *        for successive call will result in a Mess.
	 * @param anObject
	 *        The Object to store in the Set inside the Map.
	 */
	public static <K, V> void addObjectToSortedSet(Map<K, SortedSet<V>> aMap, K aKey, V anObject,
			Comparator<? super V> aComp) {
		SortedSet<V> theSet = aMap.get(aKey);

		// Create a new set and store it under the given key
		if (theSet == null) {
			theSet = new TreeSet<>(aComp);
			aMap.put(aKey, theSet);
		}

		// Add the object to the set for the given key
		theSet.add(anObject);
	}

	/**
	 * This method stores the given object into a set for the given key.
	 * 
	 * If the map does not contains a set for the given key, a new {@link Set} will be created.
	 * 
	 * @param aMap
	 *        must not be <code>null</code>.
	 * @param aKey
	 *        must not be <code>null</code>.
	 * @param anObject
	 *        The Object to store in the Set inside the Map.
	 */
	public static <K, V> void addObjectToSortedSet(Map<K, SortedSet<V>> aMap, K aKey, V anObject) {
		addObjectToSortedSet(aMap, aKey, anObject, null);
	}

	/**
	 * This method removes the given object from the collection with the given key.
	 * 
	 * @param aMap
	 *        must not be <code>null</code>.
	 * @param aKey
	 *        must not be <code>null</code>.
	 * @param anObject
	 *        The Object to remove from a Collection inside the map.
	 */
	public static boolean removeObject(Map<?, ? extends Collection<?>> aMap, Object aKey, Object anObject) {
		Collection<?> theCol = aMap.get(aKey);
		return theCol != null && theCol.remove(anObject);
	}

	/**
	 * This method stores the given object into the list with the given key.
	 *
	 * If the map does not contain a list for the given key, a new {@link ArrayList} will be
	 * created.
	 *
	 * @param aMap
	 *        must not be <code>null</code>.
	 * @param aKey
	 *        must not be <code>null</code>.
	 * @param anObject
	 *        The Object to append inside the map.
	 * 
	 * @deprecated No useful type parameters possible.
	 */
	@Deprecated
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void appendObject(Map aMap, Object aKey, Object anObject) {
		Object theValue = aMap.get(aKey);

		if (!(theValue instanceof Collection)) {
			List theList = new ArrayList();
			// If the value is null use containsKey to distinguish whether the value
			// was explicitly set to null or the key was not set.
			if (aMap.containsKey(aKey))
				theList.add(theValue);
			aMap.put(aKey, theList);
			theValue = theList;
		}

		Collection theList = (Collection) theValue;
		theList.add(anObject);
	}

	/**
	 * This method stores the given objects into the list with the given key.
	 *
	 * If the map does not contain a list for the given key, a new {@link ArrayList} will be
	 * created.
	 *
	 * @param aMap
	 *        must not be <code>null</code>.
	 * @param aKey
	 *        must not be <code>null</code>.
	 * @param objects
	 *        The objects to append inside the map.
	 * 
	 * @deprecated No useful type parameters possible.
	 */
	@Deprecated
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void appendAll(Map aMap, Object aKey, Collection objects) {
		Object theValue = aMap.get(aKey);

		if (!(theValue instanceof Collection)) {
			List theList = new ArrayList();
			// If the value is null use containsKey to distinguish whether the value
			// was explicitly set to null or the key was not set.
			if (aMap.containsKey(aKey))
				theList.add(theValue);
			aMap.put(aKey, theList);
			theValue = theList;
		}

		Collection theList = (Collection) theValue;
		theList.addAll(objects);
	}

	/**
	 * This method returns for multiple calls with equal objects always the first instance. Use this
	 * method to share the same object instance for multiple objects that are equal to reduce memory
	 * usage.
	 *
	 * @param aSharedObjectsMap
	 *        The map which is used to store the shared instances. At first call of this method,
	 *        this may be an empty map. Next calls of this method must be called with the same map
	 *        to find the shared objects again.
	 * @param aObject
	 *        the object to share; may be <code>null</code>
	 * @return the given object, if the method wasn't called with an equal object before or a shared
	 *         instance which is equal to the given object
	 */
	public static <O> O shareObject(Map<O, O> aSharedObjectsMap, O aObject) {
		if (aObject == null) {
			return null;
		}
		O theSharedObject = aSharedObjectsMap.get(aObject);
		if (theSharedObject == null) {
			aSharedObjectsMap.put(aObject, aObject);
			return aObject;
		}
		return theSharedObject;
	}

	/**
	 * Create a {@link Map} of the elements to their index in the given list.
	 * 
	 * @param <T>
	 *        The type of elements.
	 * @param list
	 *        The list to index.
	 * @return An index mapping for the given list.
	 */
	public static <T, TT extends T> Map<T, Integer> createIndexMap(List<TT> list) {
		int cnt = list.size();
		HashMap<T, Integer> result = newMap(cnt);
		for (int n = 0; n < cnt; n++) {
			result.put(list.get(n), n);
		}
		return result;
	}

	/**
	 * This method creates a new map with the given keys and values. The given collections must not
	 * be <code>null</code>.
	 *
	 * @param keys
	 *        the objects which are the keys of the returned map.
	 * @param values
	 *        the objects which are the values of the returned map.
	 * @return Returns a {@link Map} and never <code>null</code>.
	 */
	public static <K, V, KK extends K, VV extends V> Map<K, V> createMap(Collection<KK> keys, Collection<VV> values) {
		Map<K, V> map = newMap(keys.size());
		Iterator<KK> it1 = keys.iterator();
		Iterator<VV> it2 = values.iterator();
		while (it1.hasNext()) {
			V value = it2.hasNext() ? it2.next() : null;

			map.put(it1.next(), value);
		}
		return map;
	}

	/**
	 * This method creates a new map with the given keys and values. The given arrays must not be
	 * <code>null</code>.
	 *
	 * @param keys
	 *        the objects which are the keys of the returned map.
	 * @param values
	 *        the objects which are the values of the returned map.
	 * @return Returns a {@link Map} and never <code>null</code>.
	 */
	public static <K, V, KK extends K, VV extends V> Map<K, V> createMap(KK[] keys, VV[] values) {
		int keyLength = keys.length;
		Map<K, V> map = newMap(keyLength);

		int valuesLength = values.length;
		for (int i = 0; i < keyLength; i++) {
			V value = i < valuesLength ? values[i] : null;

			map.put(keys[i], value);
		}
		return map;
	}

	/**
	 * Returns the list of values stored in the maps under the given key. The values are in the same
	 * order as the maps they were stored in.
	 * 
	 * @param maps
	 *        Can be <code>null</code>. In this case, an mutable empty list is returned. The list of
	 *        maps can contain <code>null</code>. That is treated as an empty map. The maps are
	 *        allowed to contain <code>null</code> as key and as value. If a map does not contain
	 *        the given key, <code>null</code> is stored in the list.
	 * @param key
	 *        Can be <code>null</code>. It is treated as any other key.
	 * @return Never <code>null</code>.
	 */
	public static <V, VV extends V> List<V> getFromAll(Collection<? extends Map<?, ? extends VV>> maps, Object key) {
		if (maps == null) {
			return new ArrayList<>();
		}
		List<V> values = new ArrayList<>(maps.size());
		for (Map<?, ? extends VV> map : maps) {
			values.add(nonNull(map).get(key));
		}
		return values;
	}

	/**
	 * Joins the {@link Map}s into a new result {@link HashMap}.
	 * <p>
	 * If a key is contained in both {@link Map}s, the second {@link Map} wins.
	 * </p>
	 * 
	 * @param first
	 *        Is not allowed to be null.
	 * @param second
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static <K, V> HashMap<K, V> join(Map<? extends K, ? extends V> first, Map<? extends K, ? extends V> second) {
		return join(asList(first, second));
	}

	/**
	 * Joins the {@link Map}s into a new result {@link HashMap}.
	 * <p>
	 * If a key is contained in multiple {@link Map}s, the last {@link Map} wins.
	 * </p>
	 * 
	 * @param maps
	 *        Is not allowed to be or contain null.
	 * @return Never null.
	 */
	public static <K, V> HashMap<K, V> join(Collection<? extends Map<? extends K, ? extends V>> maps) {
		return joinInto(CollectionFactoryShared.<K, V> map(), maps);
	}

	/**
	 * Joins the {@link Map}s into the given result {@link Map}.
	 * <p>
	 * If a key is contained in multiple {@link Map}s, the last {@link Map} wins.
	 * </p>
	 * 
	 * @param result
	 *        The {@link Map} into which all the other maps should be copied. Is not allowed to be
	 *        null.
	 * @param maps
	 *        Is not allowed to be or contain null.
	 * @return Never null. The given result {@link Map} to support method chaining.
	 */
	public static <K, V, M extends Map<? super K, ? super V>> M joinInto(M result,
			Collection<? extends Map<? extends K, ? extends V>> maps) {
		requireNonNull(result); // Ensure it fails for null even if the collection is empty.
		for (Map<? extends K, ? extends V> map : maps) {
			result.putAll(map);
		}
		return result;
	}

	/**
	 * Convenience variant of {@link #createSubMap(Map, Collection, Map)} where the result is a
	 * {@link HashMap}.
	 * <p>
	 * Unlike {@link List#subList(int, int)} the result is not a view but is completely independent
	 * from the source. This is necessary, as a view would require a special source {@link Map}
	 * implementation.
	 * </p>
	 * <p>
	 * Like {@link List#subList(int, int)} an exception is thrown when one of the requested keys is
	 * not contained in the source {@link Map}. Unlike {@link List#subList(int, int)}, a
	 * {@link NoSuchElementException} is thrown.
	 * </p>
	 * 
	 * @param source
	 *        Is not allowed to be null. Is allowed to contain null as key and value.
	 * @param keys
	 *        The keys whose entries in the source map should be copied to the result map. Is
	 *        allowed to be null if {@link Map#containsKey(Object)} of the source {@link Map} allows
	 *        null.
	 * @return Never null. A new, mutable {@link HashMap}.
	 * @throws NoSuchElementException
	 *         If one of the keys is not {@link Map#containsKey(Object) contained} in the source
	 *         {@link Map}.
	 */
	public static <K, V> HashMap<K, V> createSubMap(Map<K, V> source, Collection<? extends K> keys)
			throws NoSuchElementException {
		HashMap<K, V> subMap = map();
		createSubMap(source, keys, subMap);
		return subMap;
	}

	/**
	 * Copies the specified entries from the source {@link Map} to the result {@link Map}.
	 * <p>
	 * Unlike {@link List#subList(int, int)} the result is not a view but is completely independent
	 * from the source. This is necessary, as a view would require a special source {@link Map}
	 * implementation.
	 * </p>
	 * <p>
	 * Like {@link List#subList(int, int)} an exception is thrown when one of the requested keys is
	 * not contained in the source {@link Map}. Unlike {@link List#subList(int, int)}, a
	 * {@link NoSuchElementException} is thrown.
	 * </p>
	 * 
	 * @param source
	 *        Is not allowed to be null. Is allowed to contain null as key and value.
	 * @param keys
	 *        The keys whose entries in the source map should be copied to the result map. Is
	 *        allowed to be null if the result {@link Map} allows null as key and
	 *        {@link Map#containsKey(Object)} of the source {@link Map} allows null.
	 * @param result
	 *        Existing entries that exist in the source {@link Map}, too, will be overridden. Is not
	 *        allowed to be null.
	 * @return Never null. The given result {@link Map} to support method chaining.
	 * @throws NoSuchElementException
	 *         If one of the keys is not {@link Map#containsKey(Object) contained} in the source
	 *         {@link Map}.
	 */
	public static <K, V, M extends Map<? super K, ? super V>> M createSubMap(Map<K, V> source,
			Collection<? extends K> keys, M result) throws NoSuchElementException {
		requireNonNull(source); // Ensure it fails for null even if the keys-collection is empty.
		requireNonNull(result);
		for (K key : keys) {
			if (!source.containsKey(key)) {
				throw failNoSuchKey(source, key, keys);
			}
			result.put(key, source.get(key));
		}
		return result;
	}

	private static NoSuchElementException failNoSuchKey(Map<?, ?> source, Object missingKey,
			Collection<?> requestedKeys) {
		String message = "No entry for key " + debug(missingKey) + ". All entries: " + debug(source)
			+ ". All requested keys: " + debug(requestedKeys);
		throw new NoSuchElementException(message);
	}

	/**
	 * Returns a memory optimization of the given original. This method can be used to replace
	 * almost always empty or "single element" maps by optimizes variants to omit overhead.
	 * 
	 * <ol>
	 * <li>If the given map is empty, {@link Collections#emptyMap()} is returned.</li>
	 * <li>If the given map contains one element, a {@link Collections#singletonMap(Object, Object)}
	 * with the single entry is returned.</li>
	 * <li>Otherwise the original is returned.</li>
	 * </ol>
	 */
	public static <K, V> Map<K, V> memoryOptimization(Map<K, V> orig) {
		switch (orig.size()) {
			case 0:
				return Collections.emptyMap();
			case 1:
				Entry<K, V> singleEntry = orig.entrySet().iterator().next();
				return Collections.singletonMap(singleEntry.getKey(), singleEntry.getValue());
			default:
				return orig;
		}

	}

}
