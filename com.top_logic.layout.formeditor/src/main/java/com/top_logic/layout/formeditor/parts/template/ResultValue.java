/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.layout.formeditor.parts.template;

import com.top_logic.layout.DisplayContext;

/**
 * A {@link ConstantEvalResult} with a given value.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ResultValue extends ConstantEvalResult {

	private Object _value;

	/**
	 * Creates a {@link ResultValue}.
	 */
	public ResultValue(Object value) {
		_value = value;
	}

	@Override
	public Object getValue(DisplayContext displayContext) {
		return _value;
	}

}
