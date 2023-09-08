/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Map.Entry;

/**
 * Space-optimized one-time {@link Iterable} buffer with name/value {@link Entry}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NameValueBuffer extends KeyValueBuffer<String, Object> implements NameValueBuilder {

	/**
	 * Creates a {@link NameValueBuffer}.
	 */
	public NameValueBuffer() {
		super();
	}

	/**
	 * Creates a {@link NameValueBuffer}.
	 * 
	 * @param initialSize
	 *        Initial capacity of this buffer.
	 */
	public NameValueBuffer(int initialSize) {
		super(initialSize);
	}

	@Override
	public final NameValueBuilder setValue(String name, Object value) {
		put(name, value);
		return this;
	}

}
