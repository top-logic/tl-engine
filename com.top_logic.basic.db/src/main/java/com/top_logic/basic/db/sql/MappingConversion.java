/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import com.top_logic.basic.col.Mapping;

/**
 * {@link Conversion} that delegates to a {@link Mapping}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MappingConversion<D> extends AbstractConstantConversion<D> {

	private final Mapping<Object, ? extends D> _impl;

	/**
	 * Creates a new {@link MappingConversion}.
	 * 
	 * @param impl
	 *        The mapping to delegate {@link #map(Object)} to.
	 */
	public MappingConversion(Mapping<Object, ? extends D> impl) {
		_impl = impl;
	}

	@Override
	public D map(Object input) {
		return _impl.map(input);
	}

}

