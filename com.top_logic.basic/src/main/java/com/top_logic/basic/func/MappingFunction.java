/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

import com.top_logic.basic.col.Mapping;

/**
 * Adapter from {@link Mapping} to {@link Function1}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MappingFunction<R, A> extends Function1<R, A> {

	private final Mapping<? super A, ? extends R> _impl;

	/**
	 * Creates a {@link MappingFunction}.
	 * 
	 * @param impl
	 *        The {@link Mapping} that implements this {@link Function1}.
	 */
	public MappingFunction(Mapping<? super A, ? extends R> impl) {
		_impl = impl;
	}

	@Override
	public R apply(A arg) {
		return _impl.map(arg);
	}

}
