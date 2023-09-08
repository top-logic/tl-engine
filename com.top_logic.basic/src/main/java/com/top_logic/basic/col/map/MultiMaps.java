/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.map;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.MapUtil;

/**
 * Methods for working with <a href="https://en.wikipedia.org/wiki/Multimap">Multi Maps
 * (Wikipedia)</a>.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class MultiMaps {

	/**
	 * Adds the given key value pair to the given multi-map.
	 * 
	 * @param <K>
	 *        The key type.
	 * @param <V>
	 *        the value type.
	 * @param <C>
	 *        the actual collection type.
	 * @param multiMap
	 *        The multi-map
	 * @param key
	 *        The key object.
	 * @param newValue
	 *        The value object.
	 * @param collectionFactory
	 *        Factory creating the {@link Collection} when key is added first time.
	 * @return Whether the collection changed.
	 */
	public static <K, V, C extends Collection<V>> boolean add(Map<K, C> multiMap, K key, V newValue,
			Supplier<C> collectionFactory) {
		C values = multiMap.get(key);
		if (values == null) {
			values = collectionFactory.get();
			multiMap.put(key, values);
		}
		return values.add(newValue);
	}

	/**
	 * Adds the given key value pair to the given multi-map.
	 * <p>
	 * If the multiMap is a {@link LinkedHashMap}, a {@link LinkedHashSet} will be build if a new
	 * {@link Set} is necessary.
	 * </p>
	 * 
	 * @param <K>
	 *        The key type.
	 * @param <V>
	 *        the value type.
	 * @param multiMap
	 *        The multi-map
	 * @param key
	 *        The key object.
	 * @param newValue
	 *        The value object.
	 * @return Whether the collection changed.
	 */
	public static <K, V> boolean add(Map<K, Set<V>> multiMap, K key, V newValue) {
		Set<V> values = multiMap.get(key);
		if (values == null) {
			if (multiMap instanceof LinkedHashMap) {
				values = new LinkedHashSet<>();
			} else {
				values = new HashSet<>();
			}
			multiMap.put(key, values);
		}
		return values.add(newValue);
	}

	/**
	 * Adds the given key value pairs to the given multi-map.
	 * 
	 * @param <K>
	 *        The key type.
	 * @param <V>
	 *        the value type.
	 * @param multiMap
	 *        The multi-map
	 * @param newValues
	 *        The key value pairs to insert into the given multi-map.
	 * @return Whether the collection changed.
	 * 
	 * @see MultiMaps#addAll(Map, Object, Collection) Inserting multiple values for a single
	 *      key.
	 * @see MultiMaps#addAll(Map, Map) Inserting a second multi-map into a given multi-map.
	 */
	public static <K, V> boolean add(Map<K, Set<V>> multiMap, Map<? extends K, ? extends V> newValues) {
		boolean hasChanged = false;
		for (Entry<? extends K, ? extends V> entry : newValues.entrySet()) {
			hasChanged |= add(multiMap, entry.getKey(), entry.getValue());
		}
		return hasChanged;
	}

	/**
	 * Gets the non-null set of values from the given multi map that are associated with the given
	 * key.
	 * 
	 * @param <K>
	 *        The key type.
	 * @param <V>
	 *        The value type.
	 * @param multiMap
	 *        The multi-map
	 * @param key
	 *        The key to look up.
	 * @return The (potentially unmodifiable) set of values associated with the given key. Never
	 *         <code>null</code>. Must not be modified.
	 */
	public static <K, V> Set<V> get(Map<K, Set<V>> multiMap, K key) {
		Set<V> values = multiMap.get(key);
		if (values == null) {
			return Collections.emptySet();
		} else {
			return values;
		}
	}

	/**
	 * Removes the given mapping from the given multi map.
	 * 
	 * @param <K>
	 *        The key type.
	 * @param <V>
	 *        The value type.
	 * @param multiMap
	 *        The multi map.
	 * @param key
	 *        The key.
	 * @param oldValue
	 *        The value to remove.
	 * @return Whether the map has changed.
	 */
	public static <K, V> boolean remove(Map<K, ? extends Collection<V>> multiMap, K key, Object oldValue) {
		Collection<V> values = multiMap.get(key);
		if (values == null) {
			return false;
		} else {
			return values.remove(oldValue);
		}
	}

	/**
	 * Removes all given mappings from the given multi map.
	 * 
	 * @param <K>
	 *        The key type.
	 * @param <V>
	 *        The value type.
	 * @param multiMap
	 *        The multi map.
	 * @param key
	 *        The key.
	 * @param oldValues
	 *        The values to remove. If the given values are <code>null</code> or empty, the map is
	 *        not changed.
	 * @return Whether the map has changed.
	 */
	public static <K, V> boolean remove(Map<K, ? extends Collection<V>> multiMap, K key, Collection<?> oldValues) {
		if (oldValues == null || oldValues.isEmpty()) {
			return false;
		}
		Collection<V> values = multiMap.get(key);
		if (values == null) {
			return false;
		} else {
			return values.removeAll(oldValues);
		}
	}

	/**
	 * Clones the given multi map (including the value sets).
	 * <p>
	 * If the multiMap is a {@link LinkedHashMap}, the clone will be a {@link LinkedHashMap} with
	 * {@link LinkedHashSet} as value {@link Set}s.
	 * </p>
	 * 
	 * @param <K>
	 *        The key type.
	 * @param <V>
	 *        The value type.
	 * @param multiMap
	 *        The multi map.
	 * @return A new map with shared key and value objects but copied value sets.
	 */
	public static <K, V> Map<K, Set<V>> clone(Map<? extends K, ? extends Collection<? extends V>> multiMap) {
		boolean sorted = multiMap instanceof LinkedHashMap;
		HashMap<K, Set<V>> result;
		if (sorted) {
			result = new LinkedHashMap<>(multiMap.size());
		} else {
			result = MapUtil.newMap(multiMap.size());
		}

		for (Entry<? extends K, ? extends Collection<? extends V>> entry : multiMap.entrySet()) {
			HashSet<V> value;
			if (sorted) {
				value = new LinkedHashSet<>(entry.getValue());
			} else {
				value = new HashSet<>(entry.getValue());
			}
			result.put(entry.getKey(), value);
		}

		return result;
	}

	/**
	 * Adds all values under the given key to the given multi-map.
	 * <p>
	 * If the multiMap is a {@link LinkedHashMap}, a {@link LinkedHashSet} will be build if a new
	 * {@link Set} is necessary.
	 * </p>
	 * 
	 * @param <K>
	 *        The key type.
	 * @param <V>
	 *        The value type.
	 * @param multiMap
	 *        The multi-map
	 * @param key
	 *        The key object.
	 * @param newValues
	 *        The values to add object. If the values are <code>null</code> or empty, map is not
	 *        changed. The given collection is neither touched nor inserted by reference into the
	 *        given multi-map.
	 * @return Whether the map has been changed by this operation.
	 */
	public static <K, V> boolean addAll(Map<K, Set<V>> multiMap, K key, Collection<? extends V> newValues) {
		if (newValues == null || newValues.isEmpty()) {
			return false;
		}
		Set<V> values = multiMap.get(key);
		if (values == null) {
			if (multiMap instanceof LinkedHashMap) {
				values = new LinkedHashSet<>(newValues);
			} else {
				values = new HashSet<>(newValues);
			}
			multiMap.put(key, values);
			return true;
		} else {
			return values.addAll(newValues);
		}
	}

	/**
	 * Adds all entries of the second multi-map to the first one.
	 * 
	 * @param <K>
	 *        The key type.
	 * @param <V>
	 *        the value type.
	 * @param multiMap
	 *        The multi-map to which entries are added.
	 * @param newValues
	 *        The multi-map whose values are added. The given collection is neither touched nor
	 *        inserted by reference into the first multi-map.
	 * @return Whether the modified map has been changed by this operation.
	 */
	public static <K, V> boolean addAll(Map<K, Set<V>> multiMap,
			Map<? extends K, ? extends Collection<? extends V>> newValues) {
		boolean hasChanged = false;
		for (Entry<? extends K, ? extends Collection<? extends V>> entry : newValues.entrySet()) {
			hasChanged |= addAll(multiMap, entry.getKey(), entry.getValue());
		}
		return hasChanged;
	}

	/**
	 * Creates an unmodifiable {@link Map} where the value {@link Set}s are unmodifiable, too.
	 * 
	 * @param multiMap
	 *        Null is treated as an empty {@link Map}. Null values are converted to an empty
	 *        {@link Set}. Null is not allowed as a key or set entry.
	 */
	public static <K, V> ImmutableMap<K, Set<V>> unmodifiableMultiMap(
			Map<? extends K, ? extends Set<? extends V>> multiMap) {
		if (CollectionUtil.isEmptyOrNull(multiMap)) {
			return ImmutableMap.of();
		}
		if (multiMap.size() == 1) {
			/* Performance optimization: Avoid creating a builder for size 1. */
			Entry<? extends K, ? extends Set<? extends V>> entry = CollectionUtil.getFirst(multiMap.entrySet());
			return ImmutableMap.of(entry.getKey(), toUnmodifiableSet(entry.getValue()));
		}
		Builder<K, Set<V>> builder = ImmutableMap.builder();
		for (Entry<? extends K, ? extends Set<? extends V>> entry : multiMap.entrySet()) {
			Set<? extends V> set = entry.getValue();
			builder.put(entry.getKey(), toUnmodifiableSet(set));
		}
		return builder.build();
	}

	private static <V> ImmutableSet<V> toUnmodifiableSet(Set<? extends V> set) {
		if (CollectionUtil.isEmptyOrNull(set)) {
			return ImmutableSet.of();
		}
		if (set.size() == 1) {
			/* Performance optimization. */
			return ImmutableSet.of(CollectionUtil.getFirst(set));
		}
		return ImmutableSet.copyOf(set);
	}

}
