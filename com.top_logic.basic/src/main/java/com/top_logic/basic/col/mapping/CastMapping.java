/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.mapping;

import com.top_logic.basic.col.Mapping;

/**
 * A {@link Mapping} for {@link Class#cast(Object)}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class CastMapping<T> implements Mapping<Object, T> {

	private final Class<T> _type;

	/** Creates a {@link CastMapping} for the given {@link Class}. */
	public CastMapping(Class<T> type) {
		_type = type;
	}

	@Override
	public T map(Object input) {
		return _type.cast(input);
	}

}
