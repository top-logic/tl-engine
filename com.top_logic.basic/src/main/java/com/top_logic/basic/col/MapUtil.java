/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.basic.Logger;
import com.top_logic.basic.shared.collection.map.MapUtilShared;

/**
 * The MapUtil contains useful static methods for maps of Lists or Sets.
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class MapUtil extends MapUtilShared {

	/**
	 * Creates a cached mapping with the given keys and the values retrieved from the given dynamic
	 * mapping.
	 * 
	 * @param <K>
	 *        The key type of the map the result can be assigned to.
	 * @param <KK>
	 *        The super type of the elements in the key collection.
	 * @param <V>
	 *        The value type of the map the result can be assigned to.
	 * @param <VV>
	 *        The super type of the each result of the given Mapping.
	 * 
	 * @param keys
	 *        the objects which are the keys of the returned map.
	 * @param valueMapping
	 *        the mapping to use to create values from objects.
	 * @return Returns a {@link Map} and never <code>null</code>.
	 */
    public static <K, V, KK extends K, VV extends V> Map<K, V> createKeyMap(Collection<? extends KK> keys, Mapping<? super K, ? extends VV> valueMapping) {
        Map<K, V> map = newMap(keys.size());
        for (Iterator<? extends KK> iterator = keys.iterator(); iterator.hasNext();) {
            K key = iterator.next();
            V value = valueMapping.map(key);
            map.put(key, value);
        }
        if (map.size() != keys.size()) {
            Logger.warn("Multiple values with same key detected.", MapUtil.class);
        }
        return map;
    }

	/**
	 * Creates a new map with the given objects as values and the keys provided by the given
	 * mapping.
	 * 
	 * <p>
	 * Produces warning, if generated keys are not unique.
	 * </p>
	 * 
	 * @param <K>
	 *        The key type.
	 * @param <V>
	 *        The value type.
	 * 
	 * @param values
	 *        the objects which are the values of the returned map.
	 * @param keyMapping
	 *        the mapping to use to create keys from objects.
	 * @return Returns a {@link Map} and never <code>null</code>.
	 */
    public static <K, V, KK extends K, VV extends V> Map<K, V> createValueMap(Collection<? extends VV> values, Mapping<? super V, ? extends KK> keyMapping) {
        return mapValuesIntoChecked(MapUtil.<K, V>newMap(values.size()), keyMapping, values);
    }

	/**
	 * Adds the given objects as values to the given map (the keys are provided by the given
	 * mapping).
	 * 
	 * <p>
	 * Produces warning, if generated keys are not unique.
	 * </p>
	 * 
	 * @param <K>
	 *        The key type.
	 * @param <V>
	 *        The value type.
	 * 
	 * @param destination
	 *        The map to add entries to.
	 * @param values
	 *        the objects which are the values of the returned map.
	 * @param keyMapping
	 *        the mapping to use to create keys from objects.
	 * @return Returns a {@link Map} and never <code>null</code>.
	 */
	public static <K, V> Map<K, V> mapValuesIntoChecked(Map<K, V> destination, Mapping<? super V, ? extends K> keyMapping, Collection<? extends V> values) {
		int sizeBefore = destination.size();
		
		mapValuesInto(destination, keyMapping, values);
		
        int sizeAfter = destination.size();
        
		if (sizeAfter - sizeBefore != values.size()) {
            Logger.warn("Multiple values with same key detected.", MapUtil.class);
        }
		
		return destination;
	}

	/**
	 * Adds the given objects as values to the given map (the keys are provided by the given
	 * mapping).
	 * 
	 * @param <K>
	 *        The key type.
	 * @param <V>
	 *        The value type.
	 * 
	 * @param destination
	 *        The map to add entries to.
	 * @param values
	 *        the objects which are the values of the returned map.
	 * @param keyMapping
	 *        the mapping to use to create keys from objects.
	 * @return Returns a {@link Map} and never <code>null</code>.
	 */
	public static <K, V> Map<K, V> mapValuesInto(Map<K, V> destination, Mapping<? super V, ? extends K> keyMapping, Collection<? extends V> values) {
		for (Iterator<? extends V> iterator = values.iterator(); iterator.hasNext();) {
            V value = iterator.next();
            K key = keyMapping.map(value);
            destination.put(key, value);
        }

		return destination;
	}

	/**
	 * If the specified key is not already associated with a value, associate it with the given
	 * value.
	 * 
	 * @param <K>
	 *        type of the keys in the given map
	 * @param <V>
	 *        type of the values in the given map
	 * @param map
	 *        the map to put the given key/value pair into. Must not be <code>null</code>.
	 * @param key
	 *        the key for the given value. Must not be <code>null</code>.
	 * @param value
	 *        the value to put into the map if none was added for the given key. Must not be
	 *        <code>null</code>.
	 * 
	 * @return the value associated with the key after the call of this method
	 * 
	 * @see ConcurrentMap#putIfAbsent(Object, Object)
	 * 
	 * @implNote This utility cannot be used for general {@link Map}s, because these allow
	 *           <code>null</code> as value, which is not expected by this implementation.
	 */
	public static <K, V> V putIfAbsent(ConcurrentMap<K, V> map, K key, V value) {
		V formerlyValue = map.putIfAbsent(key, value);
		if (formerlyValue == null) {
			// as ConcurrentHashMap does not allow null as value, there was no
			// value for that key added before.
			return value;
		} else {
			return formerlyValue; 
		}
	}

	/**
	 * Parses a {@link Map} from the given value.
	 * <p>
	 * The keys and values are trimmed with {@link String#trim()}. Must not contain a key twice. One
	 * key and all of the values are allowed to be the empty String. It is explicit unspecified what
	 * happens if one or both of the separators contain characters that would be trimmed by
	 * {@link String#trim()}.
	 * </p>
	 * 
	 * @param input
	 *        The {@link String} to parse. Must not be <code>null</code>.
	 * @param keyValueSeparator
	 *        The separator between the key and value. Must not be <code>null</code> or
	 *        {@link String#isEmpty()}.
	 * @param entrySeparator
	 *        The separator between two entries. Must not be <code>null</code> or
	 *        {@link String#isEmpty()}.
	 * @return A {@link Map} from {@link String}s to {@link String}s. Never <code>null</code>.
	 * @throws ParseException
	 *         If splitting an entry with the keyValueSeparator does not return an array with two
	 *         elements.
	 * @throws NullPointerException
	 *         If any of the parameters is <code>null</code>.
	 * @throws IllegalArgumentException
	 *         If the entrySeparator or keyValueSeparator is the empty String.
	 */
	public static Map<String, String> parse(String input, String keyValueSeparator, String entrySeparator)
			throws ParseException, NullPointerException, IllegalArgumentException {
		parseCheckParams(keyValueSeparator, entrySeparator);

		Map<String, String> result = new HashMap<>();
		if (input.trim().isEmpty()) {
			return result;
		}
		String[] entries = input.split(Pattern.quote(entrySeparator));
		int position = 0;
		for (String entry : entries) {
			String[] keyValuePair = entry.split(Pattern.quote(keyValueSeparator), -1);
			if (keyValuePair.length != 2) {
				throw new ParseException("Expected a key-value pair, separated by '" + keyValueSeparator
					+ "' but found: '" + entry + "'", position);
			}
			String key = keyValuePair[0].trim();
			String value = keyValuePair[1].trim();
			if (result.containsKey(key)) {
				throw new ParseException("Duplicate key '" + key + "'. Values: '" + result.get(key) + "' and '"
					+ value + "'", position);
			}
			result.put(key, value);
			position += entry.length() + entrySeparator.length();
		}
		return result;
	}

	private static void parseCheckParams(String keyValueSeparator, String entrySeparator) {
		if (keyValueSeparator == null) {
			throw new NullPointerException();
		}
		if (entrySeparator == null) {
			throw new NullPointerException();
		}
		if (keyValueSeparator.isEmpty()) {
			throw new IllegalArgumentException("Key-Value Separator must not be empty.");
		}
		if (entrySeparator.isEmpty()) {
			throw new IllegalArgumentException("Entry Separator must not be empty.");
		}
	}

	/**
	 * An unmodifiable empty {@link BidiMap}.
	 */
	public static <K,V> BidiMap<K, V> emptyBidiMap(){
		@SuppressWarnings({ "unchecked" })
		BidiMap<K, V> emptyMap = EmptyBidiMap.INSTANCE;
		return emptyMap;
	}
}
