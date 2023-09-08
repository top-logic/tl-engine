/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.basic.config.annotation.DefaultValueProviderShared;
import com.top_logic.basic.func.Identity;

/**
 * Collection of static utility methods that deal with {@link Mapping}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Mappings {

	/**
	 * {@link DefaultValueProvider} that delivers the {@link Mappings#identity() identity mapping as
	 * default value}.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class IdentityMappingDefaultProvider extends DefaultValueProviderShared {
		@Override
		public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
			return Mappings.identity();
		}
	}

    /**
     * @see #entryToKey()
     */
	@SuppressWarnings("unchecked") // Generic implementation.
    private static final Mapping ENTRY_TO_KEY = new Mapping<Entry<?, ?>, Object>() {
        @Override
		public Object map(Entry<?, ?> input) {
            return input.getKey();
        }
        
        @Override
        public String toString() {
        	return getClass().getName() + "[entryToKey]";
        };
    };

    /**
     * @see #entryToValue()
     */
 	@SuppressWarnings("unchecked") // Generic implementation.
    private static final Mapping ENTRY_TO_VALUE = new Mapping<Entry<?, ?>, Object>() {
        @Override
		public Object map(Entry<?, ?> input) {
            return input.getValue();
        }
        
        @Override
        public String toString() {
        	return getClass().getName() + "[entryToValue]";
        };
    };
    
	/**
	 * Map the given source collection by applying the given mapping to each of
	 * its elements.
	 * 
	 * <p>
	 * Note: This method should only be used, if the whole result of the mapping
	 * is required at once as a single collection. Otherwise, the direct usage
	 * of {@link MappingIterator} is preferred, because it provides pipeline
	 * processing with reduced temporary memory overhead.
	 * </p>
	 * 
	 * @param mapping
	 *        A mapping to apply to the source list.
	 * @param collection
	 *        A collection of objects.
	 * @return A new list with the results of the mapping.
	 */
	public static <S,D> List<D> map(Mapping<? super S, ? extends D> mapping, Collection<S> collection) {
		ArrayList<D> result = new ArrayList<>(collection.size());
		for (Iterator<? extends D> it = new MappingIterator<S, D>(mapping, collection.iterator()); it.hasNext(); ) {
			result.add(it.next());
		}
		return result;
	}

	/**
	 * Same as {@link #map(Mapping, Collection)}, except that the result is
	 * stored in the given list.
	 * 
	 * <p>
	 * <b>Warning</b>: This is a storage optimized variant of
	 * {@link #map(Mapping, Collection)}. Callers must not access the given
	 * source list after this method returns, because the type-safety is lost.
	 * </p>
	 * 
	 * <p>
	 * <b>Warning</b>: Even if this methods completes exceptionally, the given source
	 * list must no longer be accessed, because it might be internally
	 * corrupted.
	 * </p>
	 * 
	 * @param mapping
	 *        The mapping to map elements in the given argument list.
	 * @param source
	 *        List of objects to map. The result is stored in the same list.
	 * @return A reference to the given source list with adjusted type
	 */
	@SuppressWarnings("unchecked") // Type safety is guaranteed, if the caller adheres to the warnings. 
	public static <S, D> List<D> mapInline(Mapping<? super S, ? extends D> mapping, List<? extends S> source) {
		List<D> destination = (List) source;
		for (int n = 0, cnt = source.size(); n < cnt; n++) {
			S input = source.get(n);
			destination.set(n, mapping.map(input));
		}
		return destination;
	}

	/**
	 * Same as {@link #mapInline(Mapping, List)}, except that the given list is
	 * not corrupted, if the method completes by throwing a
	 * {@link RuntimeException}.
	 * 
	 * <p>
	 * <b>Warning</b>: This is a storage optimized variant of
	 * {@link #map(Mapping, Collection)}. Callers must not access the given
	 * source list after this method returns, because the type-safety is lost.
	 * </p>
	 * 
	 * @param mapping
	 *        The wrapped, restorable mapping to map elements in the given
	 *        argument list.
	 * @param source
	 *        List of objects to map. The result is stored in the same list.
	 */
	@SuppressWarnings("unchecked") // Type safety is guaranteed, if the caller adheres to the warning. 
	public static <S, D> List<ObjectMapping<S, D>> mapInlineRestorable(Mapping<S, ObjectMapping<S, D>> mapping, List<S> source) {
		List<ObjectMapping<S, D>> result = (List) source;
		for (int n = 0, cnt = source.size(); n < cnt; n++) {
			S input = source.get(n);
			try {
				result.set(n, mapping.map(input));
			} catch (RuntimeException e) {
				// Undo previously done mappings
				for(int i = 0; i < n; i++) {
					source.set(i, result.get(i).getObject());
				}				
				
				// Inform about exception
				Logger.error("Could not perform mapping on list element " +
							 input + ", because of following reason: " + e.getMessage(), e, Mappings.class);
				throw(e);
			}
		}
		return result;
	}

	/**
	 * @see MappingIterator
	 */
	public static <S, D> Iterator<D> map(final Mapping<? super S, ? extends D> mapping, final Iterator<? extends S> inputIterator) {
		return new MappingIterator<S, D>(mapping, inputIterator);
	}

	/**
	 * Like {@link #map(Mapping, Collection)} but for array input.
	 */
	public static Object[] map(Mapping<Object, ?> mapping, Object[] array) {
		return map(mapping, array, new Object[array.length]);
	}

	/**
	 * Like {@link #map(Mapping, Object[])} but with an output array to write
	 * to.
	 * 
	 * @param mapping
	 *        The mapping to apply to the object in the given input array.
	 * @param input
	 *        Array with objects to map.
	 * @param outputTemplate
	 *        Array to write to, if its length is at least as long as the input
	 *        array. Otherwise, a fresh array of the same content type is
	 *        allocated and returned.
	 * @return An array with mapped values. The result is either the given
	 *         output template or a newly allocated array of the same content
	 *         type.
	 */
	public static Object[] map(Mapping<Object, ?> mapping, Object[] input, Object[] outputTemplate) {
		Object[] output;
		if (outputTemplate.length < input.length) {
			output = (Object[]) Array.newInstance(outputTemplate.getClass().getComponentType(), input.length);
		} else {
			output = outputTemplate;
		}
		for (int n = 0, cnt = output.length; n < cnt; n++) {
			output[n] = mapping.map(input[n]);
		}
		return output;
	}

	/**
	 * Returns a {@link Set} containing the images of the elements in the source
	 * under the given mapping.
	 * 
	 * @param mapping
	 *        the {@link Mapping} to apply to each element in the given
	 *        collection
	 * @param source
	 *        the collection containing the elements to map.
	 * @throws NullPointerException
	 *         iff the given mapping is <code>null</code> or the source
	 *         collection is <code>null</code>
	 */
	public static <S, D> Set<D> mapIntoSet(Mapping<S, D> mapping, Collection<? extends S> source) {
		if (source.isEmpty()) {
			return Collections.emptySet();
		}
		HashSet<D> result = CollectionUtil.newSet(source.size());
		for (S element : source) {
			result.add(mapping.map(element));
		}
		return result;
	}

	/**
	 * The typed identity mapping.
	 * 
	 * @param <T>
	 *        Type of source and destination.
	 * @return The identity mapping.
	 */
	public static <T> Mapping<T, T> identity() {
		return Identity.getInstance();
	}

	/**
     * {@link Mapping} that maps {@link Entry}s to their {@link Entry#getKey() keys}.
	 * 
	 * @param <K> The key type of the Entry.
	 */
	@SuppressWarnings("unchecked") // By implementation of the identity mapping.
	public static <K> Mapping<Entry<K, ?>, K> entryToKey() {
		return ENTRY_TO_KEY;
	}
	
	/**
     * {@link Mapping} that maps {@link Entry}s to their {@link Entry#getValue() values}.
	 * 
	 * @param <V> The value type of the Entry.
	 */
	@SuppressWarnings("unchecked") // By implementation of the identity mapping.
	public static <V> Mapping<Entry<?, V>, V> entryToValue() {
		return ENTRY_TO_VALUE;
	}

	/**
	 * Creates a {@link Mapping} based on the given {@link Map} which maps all
	 * inputs which are not contained as key in the given map to the given
	 * default value.
	 * 
	 * @param <S>
	 *        the input type of the mapping
	 * @param <D>
	 *        the output type of the mapping
	 * @param map
	 *        the map the returned {@link Mapping} based on. must not be <code>null</code>
	 * @param defaultValue
	 *        the value returned by {@link Mapping#map(Object)} if the input is
	 *        not a key in the given map. must not be <code>null</code>
	 */
	public static <S, D> Mapping<S, D> createMapBasedMapping(Map<?, ? extends D> map, D defaultValue) {
		return new ConstantMapMapping<>(map, defaultValue);
	}

	/**
	 * Creates a {@link Mapping} based on the given {@link Map} which maps all
	 * inputs which are not contained as key in the given map via the given <code>default</code>
	 * 
	 * @param <S>
	 *        the input type of the mapping
	 * @param <D>
	 *        the output type of the mapping
	 * @param map
	 *        the map the returned {@link Mapping} based on. must not be <code>null</code>
	 * @param defaultMapping
	 *        the value returned by {@link Mapping#map(Object)} if the input is
	 *        not a key in the given map. must not be <code>null</code>
	 */
	public static <S, D> Mapping<S, D> createMapBasedMapping(Map<?, ? extends D> map,
			final Mapping<? super S, ? extends D> defaultMapping) {
		return new MapMapping<>(map, defaultMapping);
	}
	
}
