/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.interpreter.transform;

import com.top_logic.basic.treexf.ValueCapture;

/**
 * Special {@link ValueCapture} that matches a {@link Boolean} value but expands to its inverse.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InvertedBooleanValueCapture extends ValueCapture {

	/**
	 * Creates a {@link InvertedBooleanValueCapture}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 */
	public InvertedBooleanValueCapture(String name) {
		super(name);
	}

	@Override
	protected boolean matches(Object value) {
		return value instanceof Boolean;
	}

	@Override
	protected Object transform(Object originalValue) {
		Boolean value = (Boolean) originalValue;
		return !value;
	}

}
