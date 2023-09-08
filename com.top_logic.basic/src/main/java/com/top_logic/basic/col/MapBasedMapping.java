/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Map;

/**
 * The class {@link MapBasedMapping} is a {@link Mapping} view to a map.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class MapBasedMapping<S, D> implements Mapping<S, D> {

	/**
	 * {@link Map} used as oracle for {@link #map(Object)}
	 */
	protected final Map<?, ? extends D> _map;

	/** 
	 * Creates a {@link MapBasedMapping}.
	 * 
	 * @param map the oracle contacted in {@link #map(Object)}
	 */
	public MapBasedMapping(Map<?, ? extends D> map) {
		if (map == null) {
			throw new NullPointerException("'map' must not be 'null'.");
		}
		this._map = map;

	}

	@Override
	public D map(S input) {
		D result = _map.get(input);
		if (result == null) {
			if (!_map.containsKey(input)) {
				return getDefault(input);
			}
		}
		return result;
	}

	/**
	 * returns the return value of {@link #map(Object)} if the given input is
	 * not contained in the map, this mapping based on
	 * 
	 * @param input
	 *        the input from {@link #map(Object)}
	 * 
	 * @return the output of {@link #map(Object)}
	 * 
	 * @see #map(Object)
	 */
	protected abstract D getDefault(S input);

}
