/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CollectionUtil;

/**
 * {@link Mapping} that applies a configurable number of
 * {@link Mapping functions} to the values of an input {@link Map}.
 * 
 * <p>
 * The functions to apply are themselves {@link Mapping} implementations. They
 * are registered under the keys that they should be applied to in the input
 * {@link Map}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MapConverter implements Mapping {

    /**
	 * Map keys to {@link Mapping}s.
	 * 
	 * <p>
	 * The {@link Mapping} function is {@link #convert(Map) applied} to the
	 * value that is stored under the same key in the input map under which the
	 * function is {@link #setMapping(Object, Mapping) registered} in the
	 * {@link #functions} map.
	 * </p>
	 */
	private final Map/*<Object, Mapping>*/ functions;
	
	/**
	 * Construct an empty {@link MapConverter} with no functions registered.
	 */
	public MapConverter() {
	    functions = new HashMap();
	}
	
	/**
	 * Construct a {@link MapConverter} with the given {@link Map} of keys to
	 * {@link Mapping} implementations.
	 */
	public MapConverter(Map functions) {
		this();
		
		assert CollectionUtil.containsOnly(Mapping.class, functions.values()) : "Functions map must only consist of Mapping values.";
		
		this.functions.putAll(functions);
	}
	
	/**
	 * Register the given {@link Mapping function} for all values that are
	 * stored under the given key in {@link #convert(Map) input maps}.
	 * 
	 * @return <code>this</code> for joining calls.
	 */
	public MapConverter setMapping(Object key, Mapping function) {
		functions.put(key, function);
		return this;
	}
	
	/**
	 * Adaptor method to the {@link Mapping} interface. 
	 * 
	 * @see #convert(Map)
	 */
	@Override
	public final Object map(Object input) {
		return convert((Map) input);
	}
	
	/**
	 * Applies the configured functions to the values of the given {@link Map}.
	 * 
	 * <p>
	 * Type-safe version of {@link #map(Object)}.
	 * </p>
	 * 
	 * @param source
	 *        The input {@link Map} whose values should be mapped.
	 * @return The resulting {@link Map} with all values
	 *         {@link Mapping#map(Object) mapped} by
	 *         {@link #setMapping(Object, Mapping) registered} mapping for their
	 *         respective keys.
	 */
	public Map convert(Map source) {
		Map result = new HashMap(source.size());
		for (Iterator it = source.entrySet().iterator(); it.hasNext(); ) {
			Entry entry = (Entry) it.next();
			Mapping function = (Mapping) functions.get(entry.getKey());
			if (function != null) {
				result.put(entry.getKey(), function.map(entry.getValue()));
			} else {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}
	
}
