/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.shared.collection.factory;

import static java.util.Objects.*;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.basic.shared.collection.map.MapUtilShared;

/**
 * Utilities for creating {@link List}s, {@link Set}s and {@link Map}s.
 * <p>
 * Every object created by this factory is mutable, resizable and not shared with previous / later
 * calls of any method. (Unlike for example {@link Collections#emptyList()}, which does the exact
 * opposite.)
 * </p>
 * <p>
 * The methods for creating an empty {@link List}, {@link Set} or {@link Map} are redundant, as the
 * "varargs"-methods cover these cases, too. But they are added for performance reasons: Creating an
 * empty {@link List}, {@link Set} or {@link Map} is a very common use-case and should be as fast as
 * possible.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class CollectionFactoryShared {

	/** @see #list(Object...) */
	public static <T> ArrayList<T> list() {
		return new ArrayList<>();
	}

	/**
	 * Creates a new, mutable, resizable {@link ArrayList}.
	 * 
	 * @param entries
	 *        The initial entries. Nulls are allowed and added like every other value. If the array
	 *        itself is null, an empty {@link List} is returned.
	 * @return Never null.
	 */
	@SafeVarargs
	public static <T> ArrayList<T> list(T... entries) {
		if (entries == null) {
			return new ArrayList<>(0);
		}
		return addAllTo(new ArrayList<>(entries.length), entries);
	}

	/** @see #list(Object...) */
	public static <T> ArrayList<T> list(Collection<? extends T> entries) {
		if (entries == null) {
			return new ArrayList<>(0);
		}
		return new ArrayList<>(entries);
	}

	/** @see #linkedList(Object...) */
	public static <T> LinkedList<T> linkedList() {
		return new LinkedList<>();
	}

	/**
	 * Creates a new, mutable, resizable {@link LinkedList}.
	 * 
	 * @param entries
	 *        The initial entries. Nulls are allowed and added like every other value. If the array
	 *        itself is null, an empty {@link List} is returned.
	 * @return Never null.
	 */
	@SafeVarargs
	public static <T> LinkedList<T> linkedList(T... entries) {
		if (entries == null) {
			return new LinkedList<>();
		}
		return addAllTo(new LinkedList<>(), entries);
	}

	/** @see #linkedList(Object...) */
	public static <T> LinkedList<T> linkedList(Collection<? extends T> entries) {
		if (entries == null) {
			return new LinkedList<>();
		}
		return new LinkedList<>(entries);
	}

	/** @see #set(Object...) */
	public static <T> HashSet<T> set() {
		return new HashSet<>();
	}

	/**
	 * Creates a new, mutable, resizable {@link HashSet}.
	 * 
	 * @param entries
	 *        The initial entries. Nulls are allowed and added like every other value. If the array
	 *        itself is null, an empty {@link Set} is returned.
	 * @return Never null.
	 */
	@SafeVarargs
	public static <T> HashSet<T> set(T... entries) {
		if (entries == null) {
			return new HashSet<>(0);
		}
		int initialCapacity = calcRequiredCapacity(entries.length);
		return addAllTo(new HashSet<>(initialCapacity), entries);
	}

	/** @see #set(Object...) */
	public static <T> HashSet<T> set(Collection<? extends T> entries) {
		if (entries == null) {
			return new HashSet<>(0);
		}
		return new HashSet<>(entries);
	}

	/** @see #linkedSet(Object...) */
	public static <T> LinkedHashSet<T> linkedSet() {
		return new LinkedHashSet<>();
	}

	/**
	 * Creates a new, mutable, resizable {@link LinkedHashSet}.
	 * 
	 * @param entries
	 *        The initial entries. Nulls are allowed and added like every other value. If the array
	 *        itself is null, an empty {@link Set} is returned.
	 * @return Never null.
	 */
	@SafeVarargs
	public static <T> LinkedHashSet<T> linkedSet(T... entries) {
		if (entries == null) {
			return new LinkedHashSet<>(0);
		}
		int initialCapacity = calcRequiredCapacity(entries.length);
		return addAllTo(new LinkedHashSet<>(initialCapacity), entries);
	}

	/** @see #linkedSet(Object...) */
	public static <T> LinkedHashSet<T> linkedSet(Collection<? extends T> entries) {
		if (entries == null) {
			return new LinkedHashSet<>(0);
		}
		return new LinkedHashSet<>(entries);
	}

	/** @see #treeSet(Comparable...) */
	public static <T extends Comparable<? super T>> TreeSet<T> treeSet() {
		return new TreeSet<>();
	}

	/**
	 * Creates a new, mutable, resizable {@link TreeSet}.
	 * 
	 * @param entries
	 *        The initial entries. Nulls are allowed and added like every other value. If the array
	 *        itself is null, an empty {@link Set} is returned.
	 * @return Never null.
	 */
	@SafeVarargs
	public static <T extends Comparable<? super T>> TreeSet<T> treeSet(T... entries) {
		if (entries == null) {
			return new TreeSet<>();
		}
		return addAllTo(new TreeSet<>(), entries);
	}

	/** @see #treeSet(Comparable...) */
	public static <T extends Comparable<? super T>> TreeSet<T> treeSet(Collection<? extends T> entries) {
		if (entries == null) {
			return new TreeSet<>();
		}
		return new TreeSet<>(entries);
	}

	/** @see #treeSet(Comparator, Object...) */
	public static <T> TreeSet<T> treeSet(Comparator<? super T> comparator) {
		return new TreeSet<>(comparator);
	}

	/**
	 * Creates a new, mutable, resizable {@link TreeSet} with the given {@link Comparator}.
	 * 
	 * @param entries
	 *        The initial entries. Nulls are allowed only if the {@link Comparator} supports null.
	 *        If the array itself is null, an empty {@link Set} is returned.
	 * @return Never null.
	 */
	@SafeVarargs
	public static <T> TreeSet<T> treeSet(Comparator<? super T> comparator, T... entries) {
		if (entries == null) {
			return new TreeSet<>(comparator);
		}
		return addAllTo(new TreeSet<>(comparator), entries);
	}

	/** @see #treeSet(Comparator, Object...) */
	public static <T> TreeSet<T> treeSet(Comparator<? super T> comparator, Collection<? extends T> entries) {
		if (entries == null) {
			return new TreeSet<>(comparator);
		}
		return addAllTo(new TreeSet<>(comparator), entries);
	}

	/** @see #map(Entry...) */
	public static <K, V> HashMap<K, V> map() {
		return new HashMap<>();
	}

	/**
	 * Creates a new, mutable, resizable {@link HashMap}.
	 * 
	 * @param entries
	 *        The initial entries. If the array itself is null, an empty {@link Map} is returned.
	 *        The entries are not allowed to be null. But the keys and values of the entries are
	 *        allowed to be null.
	 * @return Never null.
	 * 
	 * @see #mapEntry(Object, Object)
	 */
	@SafeVarargs
	public static <K, V> HashMap<K, V> map(Map.Entry<? extends K, ? extends V>... entries) {
		if (entries == null) {
			return new HashMap<>(0);
		}
		int initialCapacity = calcRequiredCapacity(entries.length);
		return putAll(new HashMap<>(initialCapacity), entries);
	}

	/** @see #map(Entry...) */
	public static <K, V> HashMap<K, V> map(Collection<? extends Map.Entry<? extends K, ? extends V>> entries) {
		if (entries == null) {
			return new HashMap<>(0);
		}
		int initialCapacity = calcRequiredCapacity(entries.size());
		return putAll(new HashMap<>(initialCapacity), entries);
	}

	/** @see #map(Entry...) */
	public static <K, V> HashMap<K, V> map(Map<? extends K, ? extends V> entries) {
		if (entries == null) {
			return new HashMap<>(0);
		}
		return new HashMap<>(entries);
	}

	/** @see #linkedMap(Entry...) */
	public static <K, V> LinkedHashMap<K, V> linkedMap() {
		return new LinkedHashMap<>();
	}

	/**
	 * Creates a new, mutable, resizable {@link LinkedHashMap}.
	 * 
	 * @param entries
	 *        The initial entries. If the array itself is null, an empty {@link Map} is returned.
	 *        The entries are not allowed to be null. But the keys and values of the entries are
	 *        allowed to be null.
	 * @return Never null.
	 * 
	 * @see #mapEntry(Object, Object)
	 */
	@SafeVarargs
	public static <K, V> LinkedHashMap<K, V> linkedMap(Map.Entry<? extends K, ? extends V>... entries) {
		if (entries == null) {
			return new LinkedHashMap<>(0);
		}
		int initialCapacity = calcRequiredCapacity(entries.length);
		return putAll(new LinkedHashMap<>(initialCapacity), entries);
	}

	/** @see #linkedMap(Entry...) */
	public static <K, V> LinkedHashMap<K, V> linkedMap(
			Collection<? extends Map.Entry<? extends K, ? extends V>> entries) {
		if (entries == null) {
			return new LinkedHashMap<>(0);
		}
		int initialCapacity = calcRequiredCapacity(entries.size());
		return putAll(new LinkedHashMap<>(initialCapacity), entries);
	}

	/** @see #linkedMap(Entry...) */
	public static <K, V> LinkedHashMap<K, V> linkedMap(Map<? extends K, ? extends V> entries) {
		if (entries == null) {
			return new LinkedHashMap<>(0);
		}
		return new LinkedHashMap<>(entries);
	}

	/** @see #treeMap(Entry...) */
	public static <K extends Comparable<? super K>, V> TreeMap<K, V> treeMap() {
		return new TreeMap<>();
	}

	/**
	 * Creates a new, mutable, resizable {@link TreeMap}.
	 * 
	 * @param entries
	 *        The initial entries. If the array itself is null, an empty {@link Map} is returned.
	 *        The entries are not allowed to be null, <b>neither are the the keys</b>. Only the
	 *        values of the entries are allowed to be null.
	 * @return Never null.
	 * 
	 * @see #mapEntry(Object, Object)
	 */
	@SafeVarargs
	public static <K extends Comparable<? super K>, V> TreeMap<K, V> treeMap(
			Map.Entry<? extends K, ? extends V>... entries) {
		if (entries == null) {
			return new TreeMap<>();
		}
		return putAll(new TreeMap<>(), entries);
	}

	/** @see #treeMap(Entry...) */
	public static <K extends Comparable<? super K>, V> TreeMap<K, V> treeMap(
			Collection<? extends Map.Entry<? extends K, ? extends V>> entries) {
		if (entries == null) {
			return new TreeMap<>();
		}
		return putAll(new TreeMap<>(), entries);
	}

	/**
	 * See: {@link #treeMap(Entry...)}
	 * <p>
	 * Does <b>not</b> take the comparator from the given {@link Map} if it is a {@link TreeMap}.
	 * </p>
	 */
	public static <K extends Comparable<? super K>, V> TreeMap<K, V> treeMap(
			Map<? extends K, ? extends V> entries) {
		if (entries == null) {
			return new TreeMap<>();
		}
		return new TreeMap<>(entries);
	}

	/** @see #treeMap(Comparator, Entry...) */
	public static <K, V> TreeMap<K, V> treeMap(Comparator<? super K> comparator) {
		return new TreeMap<>(comparator);
	}

	/**
	 * Creates a new, mutable, resizable {@link TreeMap} with the given {@link Comparator}.
	 * 
	 * @param entries
	 *        The initial entries. If the array itself is null, an empty {@link Map} is returned.
	 *        The entries are not allowed to be null. Their values are allowed to be null. Their
	 *        keys are allowed to be null only if the {@link Comparator} supports null.
	 * @return Never null.
	 * 
	 * @see #mapEntry(Object, Object)
	 */
	@SafeVarargs
	public static <K, V> TreeMap<K, V> treeMap(Comparator<? super K> comparator,
			Map.Entry<? extends K, ? extends V>... entries) {
		if (entries == null) {
			return new TreeMap<>(comparator);
		}
		return putAll(new TreeMap<>(comparator), entries);
	}

	/** @see #treeMap(Comparator, Entry...) */
	public static <K, V> TreeMap<K, V> treeMap(Comparator<? super K> comparator,
			Collection<? extends Map.Entry<? extends K, ? extends V>> entries) {
		if (entries == null) {
			return new TreeMap<>(comparator);
		}
		return putAll(new TreeMap<>(comparator), entries);
	}

	/** @see #treeMap(Comparator, Entry...) */
	public static <K, V> TreeMap<K, V> treeMap(Comparator<? super K> comparator,
			Map<? extends K, ? extends V> entries) {
		if (entries == null) {
			return new TreeMap<>(comparator);
		}
		return putAll(new TreeMap<>(comparator), entries.entrySet());
	}

	/**
	 * Creates an immutable {@link Entry}.
	 * <p>
	 * Intended to be used with {@link #map(Entry...)} & co.
	 * </p>
	 * 
	 * @param key
	 *        Is allowed to be null.
	 * @param value
	 *        Is allowed to be null.
	 * @return Never null.
	 */
	public static <K, V> Map.Entry<K, V> mapEntry(K key, V value) {
		return new SimpleImmutableEntry<>(key, value);
	}

	/**
	 * Adds the given elements to the given {@link Collection}.
	 * <p>
	 * This method is named "<code>addAllTo</code>" and not "<code>addAll</code>" to avoid a
	 * conflict with {@link CollectionUtilShared#addAll(Collection, Iterable)} which is no problem
	 * for Eclipse but cannot be compiled by <code>JavaC</code>, resulting in compile errors in
	 * <em>some</em> callers.
	 * </p>
	 * 
	 * @param collection
	 *        Is not allowed to be null.
	 * @param entries
	 *        Null as array is treated as an empty array. The entries are allowed to be null only if
	 *        the given {@link Collection} supports null.
	 * @return The given {@link Collection}, for convenience.
	 */
	@SafeVarargs
	public static <E, C extends Collection<? super E>> C addAllTo(C collection, E... entries) {
		if (entries == null) {
			return collection;
		}
		return addAllTo(collection, Arrays.asList(entries));
	}

	/** @see #addAllTo(Collection, Object...) */
	public static <E, C extends Collection<? super E>> C addAllTo(C collection, Collection<? extends E> entries) {
		if (entries == null) {
			return collection;
		}
		collection.addAll(entries);
		return collection;
	}

	/**
	 * Puts the given elements into the given {@link Map}.
	 * 
	 * @param map
	 *        Is not allowed to be null.
	 * @param entries
	 *        Null as array is treated as an empty array. The entries are not allowed to be null.
	 *        The keys and values are allowed to be null only if the given {@link Map} supports null
	 *        for its keys and values.
	 * @return The given {@link Map}, for convenience.
	 */
	@SafeVarargs
	public static <K, V, M extends Map<? super K, ? super V>> M putAll(M map,
			Map.Entry<? extends K, ? extends V>... entries) {
		if (entries == null) {
			return map;
		}
		return putAll(map, Arrays.asList(entries));
	}

	/** @see #putAll(Map, Entry[]) */
	public static <K, V, M extends Map<? super K, ? super V>> M putAll(M map,
			Collection<? extends Map.Entry<? extends K, ? extends V>> entries) {
		if (entries == null) {
			return map;
		}
		requireNonNull(map); // Consistently throw an NPE on null.
		for (Entry<? extends K, ? extends V> entry : entries) {
			map.put(entry.getKey(), entry.getValue());
		}
		return map;
	}

	/** @see #putAll(Map, Entry[]) */
	public static <K, V, M extends Map<? super K, ? super V>> M putAll(M map, Map<? extends K, ? extends V> entries) {
		if (entries == null) {
			return map;
		}
		map.putAll(entries);
		return map;
	}

	/**
	 * The capacity of a {@link Set} or {@link Map} to store the given number of elements for the
	 * {@link MapUtilShared#DEFAULT_LOAD_FACTOR}.
	 * 
	 * @param size
	 *        The number of elements to store. Is not allowed to be negative.
	 * @return The required capacity.
	 */
	public static int calcRequiredCapacity(int size) {
		if (size < 0) {
			throw new IllegalArgumentException("The parameter 'size' is negative: " + size);
		}
		return (int) (size / MapUtilShared.DEFAULT_LOAD_FACTOR) + 1;
	}

}
