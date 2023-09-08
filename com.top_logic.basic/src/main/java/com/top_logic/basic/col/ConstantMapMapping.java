/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Map;

/**
 * {@link Mapping} view of a {@link Map}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConstantMapMapping<S, D> extends MapBasedMapping<S, D> {

	private final D defaultValue;

	/**
	 * Creates a {@link ConstantMapMapping}.
	 * 
	 * @param map
	 *        The map that should be viewed as {@link Mapping}.
	 * @param defaultValue
	 *        The value to return, if the map has no key.
	 */
	public ConstantMapMapping(Map<?, ? extends D> map, D defaultValue) {
		super(map);
		this.defaultValue = defaultValue;
	}

	@Override
	protected D getDefault(S input) {
		return defaultValue;
	}

}
