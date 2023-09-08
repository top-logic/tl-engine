/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;

/**
 * {@link AttributeValueLocator} that always returns <code>null</code>.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Null extends Operation {

	/**
	 * Singleton {@link Null} instance.
	 */
	public static final Null INSTANCE = new Null();

	private Null() {
		// Singleton constructor.
	}

	@Override
	public Object locateAttributeValue(Object anObject) {
		return null;
	}
}