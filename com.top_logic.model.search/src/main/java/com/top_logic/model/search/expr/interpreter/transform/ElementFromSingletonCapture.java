/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.interpreter.transform;

import java.util.Collection;

import com.top_logic.basic.treexf.ValueCapture;

/**
 * Special {@link ValueCapture} that matches a {@link Collection} value that only contains a single
 * element but expands to this single element (instead to the whole {@link Collection}).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ElementFromSingletonCapture extends ValueCapture {

	/**
	 * Creates a {@link ElementFromSingletonCapture}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 */
	public ElementFromSingletonCapture(String name) {
		super(name);
	}

	@Override
	protected boolean matches(Object value) {
		return value instanceof Collection<?> && ((Collection<?>) value).size() == 1;
	}

	@Override
	protected Object transform(Object originalValue) {
		Collection<?> value = (Collection<?>) originalValue;
		Object element = value.iterator().next();
		return element;
	}

}
