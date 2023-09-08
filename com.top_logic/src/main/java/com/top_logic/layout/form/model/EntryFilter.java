/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.Map.Entry;

import com.top_logic.basic.col.Filter;

/**
 * {@link Filter} of {@link Entry} objects that applies an inner {@link Filter} to
 * {@link Entry#getValue()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EntryFilter<V> implements Filter<Entry<?, ? extends V>> {

	private final Filter<? super V> inner;

	public EntryFilter(Filter<? super V> inner) {
		this.inner = inner;
	}
	
	@Override
	public boolean accept(Entry<?, ? extends V> anObject) {
		return inner.accept(anObject.getValue());
	}

}
