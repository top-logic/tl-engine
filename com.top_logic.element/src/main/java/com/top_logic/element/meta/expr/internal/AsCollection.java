/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import java.util.Collection;

import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;

/**
 * {@link AttributeValueLocator} that wraps singleton values into a {@link Collection}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class AsCollection extends Operation {

	/**
	 * Singleton {@link AsCollection} instance.
	 */
	public static final AsCollection INSTANCE = new AsCollection();

	private AsCollection() {
		// Singleton constructor.
	}

	@Override
	public Object locateAttributeValue(Object obj) {
		if (obj instanceof Collection<?>) {
			return obj;
		} else {
			return CollectionUtilShared.singletonOrEmptySet(obj);
		}
	}
}