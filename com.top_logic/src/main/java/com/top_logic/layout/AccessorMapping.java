/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.table.MappedAccessorMapping;
import com.top_logic.layout.table.TableColumnMapping;

/**
 * Mapping that maps objects to the value provided by a delegate {@link Accessor} for a given fixed
 * property.
 * 
 * @see MappedAccessorMapping
 * @see TableColumnMapping
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AccessorMapping<T> implements Mapping<T, Object> {

	private final Accessor<? super T> accessor;
	private final String property;
	
	/**
	 * Creates a {@link AccessorMapping}.
	 * 
	 * @param accessor
	 *        The accessor to apply to input objects.
	 * @param property
	 *        The constant property to access.
	 */
	public AccessorMapping(Accessor<? super T> accessor, String property) {
		this.accessor = accessor;
		this.property = property;
	}

	@Override
	public Object map(T input) {
		return accessor.getValue(input, property);
	}

}
