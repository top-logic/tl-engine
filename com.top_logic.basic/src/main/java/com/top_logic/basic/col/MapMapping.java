/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Map;

/**
 * {@link Mapping} view of a {@link Map}. Each input which is not a key in the
 * map is mapped via a default mapping.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MapMapping<S, D> extends MapBasedMapping<S, D> {

	private final Mapping<? super S, ? extends D> defaultMapping;

	/**
	 * Creates a {@link MapMapping}.
	 * 
	 * @param map
	 *        The map that should be viewed as {@link Mapping}.
	 * @param defaultMapping
	 *        The mapping to map inputs which are not keys in the map.
	 */
	public MapMapping(Map<?, ? extends D> map, Mapping<? super S, ? extends D> defaultMapping) {
		super(map);
		if (defaultMapping == null) {
			throw new NullPointerException("'defaultMapping' must not be 'null'.");
		}
		this.defaultMapping = defaultMapping;
	}

	@Override
	protected D getDefault(S input) {
		return defaultMapping.map(input);
	}

}
