/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;

/**
 * {@link AttributeValueLocator} that returns the given base object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Self extends Operation {

	/**
	 * Singleton {@link Self} instance.
	 */
	public static final Self INSTANCE = new Self();

	private Self() {
		// Singleton constructor.
	}

	@Override
	public Object locateAttributeValue(Object anObject) {
		return anObject;
	}
}