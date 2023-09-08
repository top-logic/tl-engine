/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;

/**
 * {@link Accessor} that answers all requests with <code>null</code>.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class NullAccessor extends ReadOnlyAccessor<Object> {

	/**
	 * Singleton {@link NullAccessor} instance.
	 */
	public static final NullAccessor INSTANCE = new NullAccessor();

	private NullAccessor() {
		// Singleton constructor.
	}

	@Override
	public Object getValue(Object object, String property) {
		return null;
	}

}