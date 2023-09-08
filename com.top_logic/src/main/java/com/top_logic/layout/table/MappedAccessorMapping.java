/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.AccessorMapping;

/**
 * {@link AccessorMapping} that applies an additional {@link Mapping} to the accessed value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class MappedAccessorMapping<T> extends AccessorMapping<T> {
	private final Mapping<? super Object, ?> _valueMapping;

	private MappedAccessorMapping(Accessor<? super T> accessor, String property,
			Mapping<? super Object, ?> valueMapping) {
		super(accessor, property);

		_valueMapping = valueMapping;
	}

	@Override
	public Object map(T rowObject) {
		return _valueMapping.map(super.map(rowObject));
	}

	/**
	 * Creates a {@link MappedAccessorMapping} with an optional value mapping.
	 * 
	 * @param column
	 *        See {@link AccessorMapping#AccessorMapping(Accessor, String)}.
	 * @param accessor
	 *        See {@link AccessorMapping#AccessorMapping(Accessor, String)}.
	 * @param valueMapping
	 *        The additional {@link Mapping} to apply to values returned from the accessor.
	 */
	public static <T> Mapping<T, Object> createAccessorMapping(final String column, final Accessor<? super T> accessor,
			final Mapping<? super Object, ?> valueMapping) {
		if (valueMapping == Mappings.identity()) {
			return new AccessorMapping<>(accessor, column);
		} else {
			return new MappedAccessorMapping<>(accessor, column, valueMapping);
		}
	}
}