/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Queue;
import java.util.RandomAccess;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.collections4.MultiSet;
import org.apache.commons.collections4.multiset.HashMultiSet;

import com.top_logic.basic.col.EmptyQueue;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.UnmodifiableArrayList;
import com.top_logic.basic.col.UnmodifiableLinkedHashSet;
import com.top_logic.basic.col.iterator.IteratorUtil;
import com.top_logic.basic.col.iterator.UnmodifiableIterator;
import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.col.search.SearchResult;
import com.top_logic.basic.shared.collection.CollectionUtilShared;

/**
 * This class implements extended Collection operations.
 * 
 * @see IteratorUtil
 *
 * @author <a href="mailto:sko@top-logic.com">sko</a>
 */
public abstract class CollectionUtil extends CollectionUtilShared {

	/** A special marker index when something was searched but not found. */
	public static final int NOT_FOUND = -1;

    /**
	 * Creates a new empty multi set based on {@link HashMap}.
	 *
	 * @return The new {@link MultiSet}.
	 */
	public static final <T> HashMultiSet<T> newMultiSet() {
		return new HashMultiSet<>();
	}

     /**
     * Get all objects given by the source iterator, transform them via the mapping
     * and store the results in the destination collection.
     *
     * @param aSource          the source elements, if null the destination collection will not be changed.
     * @param aDestination     the destination collection, must not be null
     * @param aMapping         the mapping function, must not be null
     */
    public static <S,D> void map(Iterator<S> aSource, Collection<D> aDestination, Mapping<? super S, ? extends D> aMapping) {
        if (aSource == null) {
            return;
        }
        if (aDestination    == null) throw new IllegalArgumentException("Destination collection must not be null.");
        if (aMapping == null) throw new IllegalArgumentException("Mapping function must not be null.");
        while (aSource.hasNext()) {
            aDestination.add(aMapping.map(aSource.next()));
        }
    }

    /**
	 * Get an object from the collection that maps to the given object via the given mapping.
	 * 
	 * @param aCollection
	 *            the collection to search in
	 * @param anObject
	 *            the value to search for
	 * @param aMapping
	 *            the function to map the contained objects
	 * @return an object in the given {@link Collection} that maps to the given object if such an
	 *         object is contained in the collection, null otherwise
	 */
    public static <S> S find(Collection<S> aCollection, Object anObject, Mapping<? super S, ?> aMapping) {
        if (aCollection == null || aMapping == null) {
            return null;
        }
        for (Iterator<S> theIt = aCollection.iterator(); theIt.hasNext();) {
            S theElement = theIt.next();
            if (CollectionUtil.equals(anObject, aMapping.map(theElement))) {
                return theElement;
            }
        }
        return null;
    }

	/**
	 * Finds the single element accepted by the given {@link Predicate}.
	 * 
	 * @param candidates
	 *        Null is equivalent to the empty {@link Collection}.
	 * @param filter
	 *        The {@link Predicate} that accepts only the searched element.
	 * @return Null, if null is the searched element and one of the candidates.
	 * @throws RuntimeException
	 *         If none or multiple of the elements are accepted by the predicate.
	 */
	public static <T> T findSingleton(Collection<T> candidates, Predicate<? super T> filter) {
		SearchResult<T> result = new SearchResult<>();
		for (T candidate : nonNull(candidates)) {
			result.addCandidate(candidate);
			if (filter.test(candidate)) {
				result.add(candidate);
			}
		}
		return result.getSingleResult("Search failed.");
	}

	/**
	 * The index of the first occurrence of one of the searched objects in the base {@link List}.
	 * <p>
	 * If one of the collections is empty or null, the result is {@link #NOT_FOUND}.
	 * </p>
	 * 
	 * @return If the two collections have no common element, {@link #NOT_FOUND} is returned.
	 */
	public static int indexOfFirst(List<?> base, Collection<?> searched) {
		/* Performance optimization: Convert to collection to a set to speed up lookups from
		 * potentially O(n) to O(1). */
		Set<?> searchedAsSet = toSet(searched);
		if (isEmptyOrNull(base) || isEmptyOrNull(searched)) {
			return NOT_FOUND;
		}
		if (searched.size() == 1) {
			return base.indexOf(getFirst(searched));
		}
		if (base.size() == 1) {
			return searchedAsSet.contains(base.get(0)) ? 0 : NOT_FOUND;
		}
		int index = 0;
		for (Object candidate : base) {
			if (searchedAsSet.contains(candidate)) {
				return index;
			}
			index += 1;
		}
		return NOT_FOUND;
	}

	/**
	 * The index of the last occurrence of one of the searched objects in the base {@link List}.
	 * <p>
	 * If one of the collections is empty or null, the result is {@link #NOT_FOUND}.
	 * </p>
	 * 
	 * @return If the two collections have no common element, {@link #NOT_FOUND} is returned.
	 */
	public static int indexOfLast(List<?> base, Collection<?> searched) {
		Set<?> searchedAsSet = toSet(searched);
		if (isEmptyOrNull(base) || isEmptyOrNull(searched)) {
			return NOT_FOUND;
		}
		if (searched.size() == 1) {
			return base.lastIndexOf(getFirst(searched));
		}
		if (base.size() == 1) {
			return searchedAsSet.contains(base.get(0)) ? 0 : NOT_FOUND;
		}
		ListIterator<?> iterator = base.listIterator(base.size());
		while (iterator.hasPrevious()) {
			Object candidate = iterator.previous();
			if (searchedAsSet.contains(candidate)) {
				return iterator.nextIndex();
			}
		}
		return NOT_FOUND;
	}

    /**
     * Get all objects given by the source iterator, transform them via the mapping
     * and store the results in the destination collection. If an object is mapped to null
     * it is not included.
     *
     * @param aSource          the source elements, must not be null
     * @param aDestination     the destination collection, must not be null
     * @param aMapping         the mapping function, must not be null
     */
    public static <S,D> void mapIgnoreNull(Iterator<S> aSource, Collection<D> aDestination, Mapping<? super S, ? extends D> aMapping) {
        if (aSource      == null) {
            throw new IllegalArgumentException("Source iterator must not be null.");
        }
        if (aDestination == null) {
            throw new IllegalArgumentException("Destination collection must not be null.");
        }
        if (aMapping     == null) {
            throw new IllegalArgumentException("Mapping function must not be null.");
        }

        while (aSource.hasNext()) {
            D theMappedObject = aMapping.map(aSource.next());
            if (theMappedObject != null) {
                aDestination.add(theMappedObject);
            }
        }
    }

	/**
	 * Inserts elements from the given source into a mutable {@link MultiSet}.
	 * 
	 * @param source
	 *        the {@link Iterable} source of elements.
	 * @return a {@link HashMultiSet} of elements fetched from the given source.
	 */
	public static <T> HashMultiSet<T> toMultiSetIterable(Iterable<? extends T> source) {
		return toMultiSet(source.iterator());
	}

	/**
	 * Inserts elements from the given {@link Iterator} into a mutable {@link MultiSet}.
	 *
	 * @param it
	 *        the {@link Iterator} to traverse.
	 * @return a {@link HashMultiSet} of elements fetched from the given iterator.
	 */
	public static <T> HashMultiSet<T> toMultiSet(Iterator<? extends T> it) {
		HashMultiSet<T> result = newMultiSet();
		while (it.hasNext()) {
			result.add(it.next());
		}
		return result;
	}

	/**
	 * Converts the given collection to a {@link MultiSet}.
	 *
	 * @param aCollection
	 *        The collection to convert
	 * @return The given collection, if it does implement the {@link MultiSet} interface, or a new
	 *         multi set containing the collection's elements.
	 */
	public static <E> MultiSet<E> toMultiSet(Collection<E> aCollection) {
		if (aCollection instanceof MultiSet<?>) {
			return (MultiSet<E>) aCollection;
		}
		if (aCollection == null) {
			return newMultiSet();
		}
		return new HashMultiSet<>(aCollection);
	}

	/**
	 * Converts the given array to a multi set.
	 *
	 * @param aArray
	 *        The array to convert
	 * @return A new multi set containing the array's elements. Returns an empty multi set, if the
	 *         argument was <code>null</code>.
	 */
	public static <E> MultiSet<E> toMultiSet(E[] aArray) {
		if (aArray == null || aArray.length == 0) {
			return newMultiSet();
		}
		MultiSet<E> theResult = newMultiSet();
		for (int i = 0; i < aArray.length; i++) {
			theResult.add(aArray[i]);
		}
		return theResult;
	}

	/**
	 * Creates a new modifiable multi set containing the elements given in the parameter list.
	 * 
	 * @param <E>
	 *        the type of the multi set
	 * @param elements
	 *        the elements to put into the multi set
	 * @return a modifiable multi set containing the elements given in the parameter list
	 */
	@SafeVarargs
	public static <E> MultiSet<E> createMultiSet(E... elements) {
		return toMultiSet(elements);
	}

    /** @see IteratorUtil#getIterator(Object) */
    public static Iterator<?> getIterator(Object aObject) {
    	return IteratorUtil.getIterator(aObject);
    }


    /**
     * Converts the given Collection into a Map.
     *
     * @param keyMapping Used to extract a Key form the elements of aCol.
     * @return A new Map containing the Collections Elements, index
     *         as defined by keyMapping.
     *         Returns an empty map, if the argument was <code>null</code>.
     */
    public static <K, V> Map<K, V> toMap(Collection<V> aCol, Mapping<? super V, K> keyMapping) {
        if (aCol == null || aCol.isEmpty()) {
            return new HashMap<>(0);
        }
        int size = aCol.size();
        Map<K, V> theResult = new HashMap<>(size);
        if (aCol instanceof RandomAccess) {
            List<V> l = (List<V>) aCol;
            for (int i = 0; i < size; i++) {
                V o = l.get(i);
                theResult.put(keyMapping.map(o), o);
            }
        } else {
            for (Iterator<V> iter = aCol.iterator(); iter.hasNext();) {
                V o = iter.next();
                theResult.put(keyMapping.map(o), o);
            }
        }
        return theResult;
    }

	/**
	 * Partitions the given collection into lists of objects that are equal with
	 * respect to the given key mapping.
	 * 
	 * @param <V>
	 *        The type of values to partition
	 * @param <K>
	 *        The type of the keys to infer by the given mapping.
	 * @param collection
	 *        The collection of items to partition.
	 * @param keyMapping
	 *        A mapping of items to keys.
	 * @return A map indexed by keys produced by the given mapping. The value of
	 *         a map entry is a list with objects that have the same key
	 *         according to the given key mapping.
	 */
	public static <V, K> Map<K, List<V>> partition(Collection<? extends V> collection, Mapping<? super V, ? extends K> keyMapping) {
		if (collection == null || collection.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<K, List<V>> partitionByKey = new HashMap<>();
		for (Iterator<? extends V> iter = collection.iterator(); iter.hasNext();) {
			V item = iter.next();
			K key = keyMapping.map(item);

			List<V> partition = partitionByKey.get(key);
			if (partition == null) {
				partition = new ArrayList<>();
				partitionByKey.put(key, partition);
			}

			partition.add(item);
		}
		return partitionByKey;
	}

	/**
	 * Partitions the given collection according to two {@link Mapping}s.
	 * 
	 * @param <K1>
	 *        The type of the outer (first) inferred key.
	 * @param <K2>
	 *        The type of the inner (second) inferred key.
	 * @param <V>
	 *        The type of the values.
	 * @param collection
	 *        The values to index.
	 * @param keyMapping1
	 *        The mapping that infers the first key from the given values.
	 * @param keyMapping2
	 *        The mapping that infers the second key from the given values.
	 * @return A two-stage index of the given values.
	 * @see #partition(Collection, Mapping)
	 */
	public static <V, K1, K2> Map<K1, Map<K2, List<V>>> partition(Collection<? extends V> collection, Mapping<? super V, ? extends K1> keyMapping1, Mapping<? super V, ? extends K2> keyMapping2) {
		Map<K1, List<V>> partition1 = partition(collection, keyMapping1);
		return partitionInner(keyMapping2, partition1);
	}

	/**
	 * Storage optimized implementation that reuses the result from a first call
	 * to partition() for the inner partitioning.
	 * 
	 * @see #partition(Collection, Mapping, Mapping)
	 */
	@SuppressWarnings("unchecked") 
	private static <V, K1, K2> Map<K1, Map<K2, List<V>>> partitionInner(Mapping<? super V, ? extends K2> keyMapping2, Map<K1, List<V>> partition1) {
		for (Iterator<Entry<K1, List<V>>> it = partition1.entrySet().iterator(); it.hasNext(); ) {
			Entry<K1, List<V>> entry = it.next();
			
			final List<V> value = entry.getValue();
			final Map<K2, List<V>> partition2 = partition(value, keyMapping2);
			
			((Entry) entry).setValue(partition2);
		}
		Map<K1, Map<K2, List<V>>> result = (Map) partition1;
		return result;
	}

    /**
     * Returns an unmodifiable list containing the base collection and all values
     * in the given array.
     */
    public static <E> List<E> unmodifiableList(Collection<? extends E> base, E[] addedValues) {
    	if (base == null) {
    		return unmodifiableList(addedValues);
    	}
        return unmodifiableList(base, Arrays.asList(addedValues));
    }

    /**
     * Returns the given array as unmodifiable list.
     */
	public static <E> List<E> unmodifiableList(E[] values) {
		return UnmodifiableArrayList.newUnmodifiableList(values);
	}

	/** Creates an {@link UnmodifiableLinkedHashSet} from the given {@link Collection}. */
	public static <E> UnmodifiableLinkedHashSet<E> unmodifiableLinkedHashSet(Collection<? extends E> collection) {
		return new UnmodifiableLinkedHashSet<>(collection);
	}

	/** Wraps the given {@link Iterator} into an {@link UnmodifiableIterator}. */
	public static <E> UnmodifiableIterator<E> unmodifiableIterator(Iterator<? extends E> iterator) {
		return new UnmodifiableIterator<>(iterator);
	}

    /**
     * Check, whether the given collection only contains elements that are assignment compatible with the given class.
     * 
     * @see #copyOnly(Class, Collection)
     * @see #dynamicCastView(Class, List)
     */
    public static boolean containsOnly(Class<?> elementClass, List<?> aList) {
        for (int i=0, n= aList.size(); i < n; i++) {
            if (! elementClass.isInstance(aList.get(i))) {
                return false;
            }
        }
        
        return true;
    }

    /**
	 * Check, whether the given collection only contains elements that are assignment compatible with the given class.
     * 
     * @see #copyOnly(Class, Collection)
     * @see #dynamicCastView(Class, Collection)
	 */
	public static boolean containsOnly(Class<?> elementClass, Collection<?> collection) {
	    if (collection instanceof RandomAccess) {
	        return containsOnly(elementClass, (List<?>) collection);
        } 
		for (Iterator<?> it = collection.iterator(); it.hasNext(); ) {
			if (! elementClass.isInstance(it.next())) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Check, whether the recursive non-collection elements of the given
	 * collection are assignment compatible with the given class.
     * 
     * @see #dynamicCastView(Class, Collection)
	 */
	public static boolean containsOnlyRecursivly(Class<?> elementClass, Collection<?> collection) {
		boolean theResult = true;
		for (Iterator<?> it = collection.iterator(); it.hasNext(); ) {
			Object next = it.next();
			if (next instanceof Collection<?>) {
				theResult = containsOnlyRecursivly(elementClass, (Collection<?>) next);
			}
			else if (! elementClass.isInstance(next)) {
				theResult =  false;
			}
			
			if (!theResult) {
				break;
			}
		}
		
		return theResult;
	}

    /**
	 * The copied list contains only elements that are instances of the given class.
     * 
     * @param elementClass Must not be <code>null</code>.
     * @param collection Can be <code>null</code>. In this case, a mutable empty list is returned.
     * @return Never <code>null</code>.
     * 
     * @see #containsOnly(Class, Collection)
     * @see #dynamicCastView(Class, Collection)
	 */
	public static <T> List<T> copyOnly(Class<T> elementClass, Collection<?> collection) {
		List<T> copy = new ArrayList<>();
		for (Object element : nonNull(collection)) {
			if (elementClass.isInstance(element)) {
				copy.add(elementClass.cast(element));
			}
		}
		return copy;
	}

	/**
	 * The copied set contains only elements that are instances of the given class.
	 * 
     * @param elementClass Must not be <code>null</code>.
     * @param set Can be <code>null</code>. In this case, a mutable empty list is returned.
     * @return Never <code>null</code>.
     * 
	 * @see #dynamicCastView(Class, Set)
	 */
	public static <T> Set<T> copyOnly(Class<T> elementClass, Set<?> set) {
		Set<T> copy = new HashSet<>();
		for (Object element : nonNull(set)) {
			if (elementClass.isInstance(element)) {
				copy.add(elementClass.cast(element));
			}
		}
		return copy;
	}

	/**
	 * TODO #6121: Delete TL 5.8 deprecation
	 * 
	 * @deprecated Compatibility redirect to: {@link MultiMaps#add(Map, Object, Object)}
	 */
	@Deprecated
	public static <K, V> boolean addMultiMap(Map<K, Set<V>> multiMap, K key, V newValue) {
		return MultiMaps.add(multiMap, key, newValue);
	}

	/**
	 * TODO #6121: Delete TL 5.8 deprecation
	 * 
	 * @deprecated Compatibility redirect to: {@link MultiMaps#add(Map, Map)}
	 */
	@Deprecated
	public static <K, V> boolean addMultiMap(Map<K, Set<V>> multiMap, Map<? extends K, ? extends V> newValues) {
		return MultiMaps.add(multiMap, newValues);
	}

	/**
	 * TODO #6121: Delete TL 5.8 deprecation
	 * 
	 * @deprecated Compatibility redirect to: {@link MultiMaps#get(Map, Object)}
	 */
	@Deprecated
	public static <K, V> Set<V> getMultiMap(Map<K, Set<V>> multiMap, K key) {
		return MultiMaps.get(multiMap, key);
	}
	
	/**
	 * TODO #6121: Delete TL 5.8 deprecation
	 * 
	 * @deprecated Compatibility redirect to: {@link MultiMaps#remove(Map, Object, Object)}
	 */
	@Deprecated
	public static <K, V> boolean removeMultiMap(Map<K, Set<V>> multiMap, K key, Object oldValue) {
		return MultiMaps.remove(multiMap, key, oldValue);
	}

	/**
	 * TODO #6121: Delete TL 5.8 deprecation
	 * 
	 * @deprecated Compatibility redirect to: {@link MultiMaps#remove(Map, Object, Collection)}
	 */
	@Deprecated
	public static <K, V> boolean removeAllMultiMap(Map<K, Set<V>> multiMap, K key, Collection<?> oldValues) {
		return MultiMaps.remove(multiMap, key, oldValues);
	}
	
	/**
	 * TODO #6121: Delete TL 5.8 deprecation
	 * 
	 * @deprecated Compatibility redirect to: {@link MultiMaps#clone(Map)}
	 */
	@Deprecated
	public static <K, V> Map<K, Set<V>> cloneMultiMap(Map<? extends K, ? extends Collection<? extends V>> multiMap) {
		return MultiMaps.clone(multiMap);
	}
	
	/**
	 * TODO #6121: Delete TL 5.8 deprecation
	 * 
	 * @deprecated Compatibility redirect to: {@link MultiMaps#addAll(Map, Object, Collection)}
	 */
	@Deprecated
	public static <K, V> boolean addAllMultiMap(Map<K, Set<V>> multiMap, K key, Collection<? extends V> newValues) {
		return MultiMaps.addAll(multiMap, key, newValues);
	}

	/**
	 * TODO #6121: Delete TL 5.8 deprecation
	 * 
	 * @deprecated Compatibility redirect to: {@link MultiMaps#addAll(Map,Map)}
	 */
	@Deprecated
	public static <K, V> boolean addAllMultiMap(Map<K, Set<V>> multiMap, Map<? extends K, ? extends Collection<? extends V>> newValues) {
		return MultiMaps.addAll(multiMap, newValues);
	}
	
	/**
	 * Safely cast to a generic type with runtime check.
	 * 
	 * <p>
	 * Non-null-safe variant of {@link Class#cast(Object)} with better exception message.
	 * </p>
	 * 
	 * @param <T>
	 *        The type to cast to.
	 * @param expectedType
	 *        The expected runtime type.
	 * @param obj
	 *        The object to cast.
	 * @return the casted reference to the given object.
	 * 
	 * @throws ClassCastException
	 *         If the given object is <code>null</code> or not assignment compatible to the given
	 *         expected type.
	 */
	public static <T> T dynamicCast(Class<T> expectedType, Object obj) throws ClassCastException {
		if (! expectedType.isInstance(obj)) {
			String foundTypeName;
			if (obj == null) {
				foundTypeName = "null";
			} else {
				foundTypeName = obj.getClass().getName();
			}
			
			throw new ClassCastException("Expected '" + expectedType.getName() + "', found '" + foundTypeName + "'");
		}
		@SuppressWarnings("unchecked") // Safe due to dynamic check.
		T entry = (T) obj;
		return entry;
	}

	/**
	 * (Almost) safely cast a {@link Set} to a {@link Set} of generic type with runtime check.
	 * 
	 * <p>
	 * Note: Since the result is view (not a copy) of the original collection, type safe access to
	 * the result may fail, after the original collection has been modified.
	 * </p>
	 * 
	 * @see #containsOnly(Class, Collection)
	 * @see #copyOnly(Class, Set)
	 * 
	 * @param <T>
	 *        The type to cast to.
	 * @param expectedType
	 *        The expected runtime type.
	 * @param values
	 *        The values to cast.
	 * @return immutable view to the casted values.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Set<T> dynamicCastView(Class<T> expectedType, Set<?> values) {
		assert checkCast(expectedType, values);
		return (Set<T>) Collections.<Object>unmodifiableSet(values);
	}
	
	/**
	 * (Almost) safely cast a {@link List} to a {@link List} of generic type with runtime check.
	 * 
	 * <p>
	 * Note: Since the result is view (not a copy) of the original collection, type safe access to
	 * the result may fail, after the original collection has been modified.
	 * </p>
	 * 
	 * @see #containsOnly(Class, List)
	 * @see #copyOnly(Class, Collection)
	 * 
	 * @param <T>
	 *        The type to cast to.
	 * @param expectedType
	 *        The expected runtime type.
	 * @param values
	 *        The values to cast.
	 * @return immutable view to the casted values.
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> dynamicCastView(Class<T> expectedType, List<?> values) {
		assert checkCast(expectedType, values);
		return (List<T>) Collections.<Object>unmodifiableList(values);
	}

	/**
	 * (Almost) safely cast a {@link Collection} to a {@link Collection} of generic type with runtime check.
	 * 
	 * <p>
	 * Note: Since the result is view (not a copy) of the original collection, type safe access to
	 * the result may fail, after the original collection has been modified.
	 * </p>
	 * 
	 * @see #containsOnly(Class, Collection)
	 * @see #copyOnly(Class, Collection)
	 * 
	 * @param <T>
	 *        The type to cast to.
	 * @param expectedType
	 *        The expected runtime type.
	 * @param values
	 *        The values to cast.
	 * @return view to the casted values.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Collection<T> dynamicCastView(Class<T> expectedType, Collection<?> values) {
		assert checkCast(expectedType, values);
		return (Collection<T>) Collections.<Object>unmodifiableCollection(values);
	}
	
	private static <T> boolean checkCast(Class<T> expectedType, Iterable<?> values) {
		for (Object element : values) {
			dynamicCast(expectedType, element);
		}
		return true;
	}

	/** @see IteratorUtil#dynamicCastView(Class, Iterable) */
	public static <T> Iterable<T> dynamicCastView(Class<T> expectedType, Iterable<?> values) {
		return IteratorUtil.dynamicCastView(expectedType, values);
	}

	/** The empty {@link UnmodifiableLinkedHashSet}. */
	public static <E> UnmodifiableLinkedHashSet<E> empty() {
		return UnmodifiableLinkedHashSet.empty();
	}

	/**
	 * An empty unmodifiable {@link Queue}.
	 * 
	 * @see Queue#add(Object)
	 * @see Queue#addAll(Collection)
	 * @see Queue#offer(Object)
	 */
	public static <E> Queue<E> emptyQueue() {
		// EmptyQueue does not depend on type parameter
		@SuppressWarnings("unchecked")
		EmptyQueue<E> emptyQueue = EmptyQueue.INSTANCE;
		return emptyQueue;
	}
	
    /**
	 * Returns a synchronized (thread-safe) queue backed by the specified queue.
	 * 
	 * <p>
	 * In order to
	 * guarantee serial access, it is critical that <strong>all</strong> access to the backing queue
	 * is accomplished through the returned queue.
	 * </p>
	 *
	 * <p>
	 * It is imperative that the user manually synchronize on the returned queue when traversing it
	 * via {@link Iterator}, {@link Spliterator} or {@link Stream}:
	 * </p>
	 * 
	 * <pre>
	 *  Queue c = Collections.synchronizedQueue(myQueue);
	 *     ...
	 *  synchronized (c) {
	 *      Iterator i = c.iterator(); // Must be in the synchronized block
	 *      while (i.hasNext())
	 *         foo(i.next());
	 *  }
	 * </pre>
	 * 
	 * Failure to follow this advice may result in non-deterministic behavior.
	 *
	 * The returned queue will be serializable if the specified queue is serializable.
	 *
	 * @param <E>
	 *        the class of the objects in the collection
	 * @param q
	 *        the collection to be "wrapped" in a synchronized queue.
	 * @return a synchronized view of the specified queue.
	 */
	public static <E> Queue<E> synchronizedQueue(Queue<E> q) {
        return new SynchronizedQueue<>(q);
    }

	/**
	 * @serial include
	 */
	private static class SynchronizedQueue<E> implements Queue<E>, Serializable {
		private static final long serialVersionUID = 1978198479659022715L;

		private final Queue<E> q; // Backing Queue

		final Object mutex; // Object on which to synchronize

		SynchronizedQueue(Queue<E> q) {
			this.q = Objects.requireNonNull(q);
			mutex = this;
		}

		@Override
		public int size() {
			synchronized (mutex) {
				return q.size();
			}
		}

		@Override
		public boolean isEmpty() {
			synchronized (mutex) {
				return q.isEmpty();
			}
		}

		@Override
		public void clear() {
			synchronized (mutex) {
				q.clear();
			}
		}

		@Override
		public String toString() {
			synchronized (mutex) {
				return q.toString();
			}
		}

		private void writeObject(ObjectOutputStream s) throws IOException {
			synchronized (mutex) {
				s.defaultWriteObject();
			}
		}

		@Override
		public boolean contains(Object o) {
			synchronized (mutex) {
				return q.contains(o);
			}
		}

		@Override
		public Iterator<E> iterator() {
			return q.iterator();
		}

		@Override
		public Object[] toArray() {
			synchronized (mutex) {
				return q.toArray();
			}
		}

		@Override
		public <T> T[] toArray(T[] a) {
			synchronized (mutex) {
				return q.toArray(a);
			}
		}

		@Override
		public boolean remove(Object o) {
			synchronized (mutex) {
				return q.remove(o);
			}
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			synchronized (mutex) {
				return q.containsAll(c);
			}
		}

		@Override
		public boolean addAll(Collection<? extends E> c) {
			synchronized (mutex) {
				return q.addAll(c);
			}
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			synchronized (mutex) {
				return q.removeAll(c);
			}
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			synchronized (mutex) {
				return q.retainAll(c);
			}
		}

		@Override
		public boolean add(E e) {
			synchronized (mutex) {
				return q.add(e);
			}
		}

		@Override
		public boolean offer(E e) {
			synchronized (mutex) {
				return q.offer(e);
			}
		}

		@Override
		public E remove() {
			synchronized (mutex) {
				return q.remove();
			}
		}

		@Override
		public E poll() {
			synchronized (mutex) {
				return q.poll();
			}
		}

		@Override
		public E element() {
			synchronized (mutex) {
				return q.element();
			}
		}

		@Override
		public E peek() {
			synchronized (mutex) {
				return q.peek();
			}
		}
	}

}
