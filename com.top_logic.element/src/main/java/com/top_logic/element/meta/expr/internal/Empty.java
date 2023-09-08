/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import java.util.Collections;

import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;

/**
 * {@link AttributeValueLocator} that always returns {@link Collections#emptySet()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Empty extends Operation {

	/**
	 * Singleton {@link Empty} instance.
	 */
	public static final Empty INSTANCE = new Empty();

	private Empty() {
		// Singleton constructor.
	}

	@Override
	public Object locateAttributeValue(Object anObject) {
		return Collections.emptySet();
	}
}