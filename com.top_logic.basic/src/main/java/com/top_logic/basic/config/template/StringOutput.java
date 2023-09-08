/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;

import com.top_logic.basic.config.template.Expand.Output;

/**
 * {@link Output} that directly writes to a {@link StringBuilder}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StringOutput implements Output {

	private final StringBuilder _buffer = new StringBuilder();

	/**
	 * Creates a {@link StringOutput}.
	 */
	public StringOutput() {
		super();
	}

	@Override
	public void add(Object result) {
		_buffer.append(toString(result));
	}

	/**
	 * Hook to convert a value to its output representation.
	 * 
	 * @param value
	 *        The value to output.
	 * @return The values string representation.
	 */
	protected String toString(Object value) {
		return value.toString();
	}

	@Override
	public String toString() {
		return _buffer.toString();
	}

}
