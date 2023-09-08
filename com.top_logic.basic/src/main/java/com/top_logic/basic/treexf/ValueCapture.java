/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;

import com.top_logic.basic.CollectionUtil;

/**
 * {@link Capture} that matches a primitive value
 * 
 * @see ExprCapture
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ValueCapture extends Capture implements Value {

	/**
	 * Creates a {@link ValueCapture}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 */
	public ValueCapture(String name) {
		super(name);
	}

	@Override
	public Kind kind() {
		return Kind.VALUE;
	}

	@Override
	public Object getValue(Match match) {
		return LiteralValue.getValue(match, match.getBinding(this));
	}

	@Override
	protected final boolean internalMatch(Match match, Node node) {
		Object nodeValue = LiteralValue.getValue(match, node);

		Node binding = match.getBinding(this);
		if (binding == null) {
			boolean result = matches(nodeValue);
			if (result) {
				match.bind(this, node);
			}
			return result;
		} else {
			Object boundValue = LiteralValue.getValue(match, binding);
			return CollectionUtil.equals(nodeValue, boundValue);
		}
	}

	/**
	 * Hook for subclasses to decide, whether a certain value is matched.
	 * 
	 * @param value
	 *        The value from the matched {@link Node}.
	 */
	protected boolean matches(Object value) {
		return true;
	}

	@Override
	public final Node expand(Match match) {
		Object originalValue = getValue(match);
		Object element = transform(originalValue);
		return new LiteralValue(element);
	}

	/**
	 * Hook for subclasses to transform the matched value when producing an expansion.
	 * 
	 * @param originalValue
	 *        The value originally matched.
	 */
	protected Object transform(Object originalValue) {
		return originalValue;
	}

}
