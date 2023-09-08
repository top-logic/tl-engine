/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;

import com.top_logic.basic.CollectionUtil;

/**
 * {@link Value} that represents a literal primitive value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LiteralValue extends AbstractValue {

	private final Object _value;

	/**
	 * Creates a {@link LiteralValue}.
	 * 
	 * @param value
	 *        See {@link #getValue()}.
	 */
	protected LiteralValue(Object value) {
		_value = value;
	}

	@Override
	public Object getValue(Match match) {
		return getValue();
	}

	/**
	 * The represented literal value.
	 */
	public Object getValue() {
		return _value;
	}

	@Override
	protected boolean internalMatch(Match match, Node node) {
		return CollectionUtil.equals(_value, getValue(match, node));
	}

	@Override
	public Node expand(Match match) {
		return new LiteralValue(_value);
	}

	@Override
	public void appendTo(StringBuilder buffer) {
		if (_value instanceof String) {
			buffer.append('"');
			buffer.append(_value);
			buffer.append('"');
		} else {
			buffer.append(_value);
		}
	}

	/**
	 * {@link #getValue(Match)} of the given node assuming it is of type {@link Value}.
	 */
	public static final Object getValue(Match match, Node node) {
		return ((Value) node).getValue(match);
	}
}
