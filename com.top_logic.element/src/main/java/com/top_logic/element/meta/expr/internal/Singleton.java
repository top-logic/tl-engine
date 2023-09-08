/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import java.util.Collection;

import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;

/**
 * {@link AttributeValueLocator} that unwraps a {@link Collection} to its single entry or
 * <code>null</code> for an empty {@link Collection}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Singleton extends Operation {

	/**
	 * Singleton {@link Singleton} instance.
	 */
	public static final Singleton INSTANCE = new Singleton();

	private Singleton() {
		// Singleton constructor.
	}

	@Override
	public Object locateAttributeValue(Object obj) {
		if (obj instanceof Collection<?>) {
			Collection<?> collection = (Collection<?>) obj;
			if (collection.size() != 1) {
				return null;
			} else {
				return collection.iterator().next();
			}
		} else {
			return obj;
		}
	}
}