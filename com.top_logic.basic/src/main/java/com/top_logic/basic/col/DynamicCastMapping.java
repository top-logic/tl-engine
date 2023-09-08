/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import com.top_logic.basic.CollectionUtil;

/**
 * {@link Mapping} that applies a {@link CollectionUtil#dynamicCast(Class, Object)} to all source
 * objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DynamicCastMapping<D> implements Mapping<Object, D> {

	private final Class<D> destinationType;

	/**
	 * Creates a {@link DynamicCastMapping}.
	 * 
	 * @param destinationType
	 *        The type to which input objects are casted.
	 */
	public DynamicCastMapping(Class<D> destinationType) {
		this.destinationType = destinationType;
	}

	@Override
	public D map(Object input) {
		return CollectionUtil.dynamicCast(destinationType, input);
	}

}
